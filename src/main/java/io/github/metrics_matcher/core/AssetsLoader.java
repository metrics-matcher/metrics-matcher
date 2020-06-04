package io.github.metrics_matcher.core;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.github.metrics_matcher.dto.DataSource;
import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Log
@UtilityClass
public final class AssetsLoader {
    private static final String MSG_CAN_NOT_READ = "Can't read file \"%s\"";
    private static final String MSG_CAN_NOT_PARSE = "Can't parse file \"%s\"";
    private static final String MSG_FILES_NOT_FOUND = "Files not found in \"%s\"";
    private static final String SQL_FILE_EXTENSION = ".sql";
    private static final String JAR_FILE_EXTENSION = ".jar";

    private static final Gson GSON = new Gson();

    private static List<File> listFiles(String directory, String extension) throws IOException {
        return Files.walk(Paths.get(directory)).
                filter(Files::isRegularFile).
                map(Path::toFile).
                filter(file -> file.getName().endsWith(extension)).
                sorted(Comparator.comparing(file -> file.getPath().
                        substring(0, file.getPath().length() - extension.length()))).
                collect(Collectors.toList());
    }

    public static List<DataSource> loadDataSources(String filepath) throws MetricsException {
        log.info(format("Loading data sources from [%s]]", filepath));
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            DataSource[] data = GSON.fromJson(reader, DataSource[].class);
            log.info(format("Loaded [%s] data sources", data.length));
            List<DataSource> datasources = Arrays.asList(data);
            if (datasources.size() != datasources.stream().map(DataSource::getName).distinct().count()) {
                throw new MetricsException("Data source names must be unique");
            }
            return datasources;
        } catch (IOException e) {
            throw new MetricsException(format(MSG_CAN_NOT_READ, filepath), e);
        } catch (JsonParseException e) {
            throw new MetricsException(format(MSG_CAN_NOT_PARSE, filepath), e);
        }
    }

    public static List<MetricsProfile> loadMetricsProfiles(String filepath) throws MetricsException {
        log.info(format("Loading metrics profiles from [%s]", filepath));
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            MetricsProfile[] data = GSON.fromJson(reader, MetricsProfile[].class);
            log.info(format("Loaded [%s] metrics profiles", data.length));
            return Arrays.asList(data);
        } catch (IOException e) {
            throw new MetricsException(format(MSG_CAN_NOT_READ, filepath), e);
        } catch (JsonParseException e) {
            throw new MetricsException(format(MSG_CAN_NOT_PARSE, filepath), e);
        }
    }

    public static List<Query> loadQueries(String directory) throws MetricsException {
        log.info(format("Loading queries from [%s]", directory));

        List<File> files;
        try {
            files = listFiles(directory, SQL_FILE_EXTENSION);
        } catch (IOException e) {
            throw new MetricsException(format(MSG_CAN_NOT_READ, directory));
        }

        if (files.isEmpty()) {
            throw new MetricsException(format(MSG_FILES_NOT_FOUND, directory));
        }

        final List<Query> queries = new ArrayList<>(files.size());
        for (File file : files) {
            String filename = file.getPath();
            log.info(format("Loading query [%s]", filename));
            String id = filename.
                    substring(directory.length() + 1, filename.length() - SQL_FILE_EXTENSION.length()).
                    replace('\\', '/').
                    trim();
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                String title = null;
                if (!lines.isEmpty()) {
                    String firstLine = lines.get(0).trim();
                    if (firstLine.startsWith("--")) {
                        title = firstLine.substring(2).trim();
                        lines.remove(0);
                    }
                }
                queries.add(new Query(id, title, String.join("\n", lines)));
            } catch (IOException e) {
                throw new MetricsException(format(MSG_CAN_NOT_READ, filename), e);
            }
        }
        log.info(format("Loaded [%s] queries", queries.size()));
        return queries;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public static void loadDrivers(String directory) throws MetricsException {
        log.info(format("Loading drivers from [%s]", directory));

        List<File> files;
        try {
            files = listFiles(directory, JAR_FILE_EXTENSION);
        } catch (IOException e) {
            throw new MetricsException(format(MSG_CAN_NOT_READ, directory));
        }

        if (files.isEmpty()) {
            throw new MetricsException(format(MSG_FILES_NOT_FOUND, directory));
        }

        try {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            for (File file : files) {
                String filename = file.getName();
                log.info(format("Loading driver [%s]", filename));
                try {
                    method.invoke(classLoader, file.toURI().toURL());
                } catch (Exception e) {
                    throw new MetricsException(format("Can't load driver \"%s\"", filename), e);
                }
            }
        } catch (Exception e) {
            throw new MetricsException(format("Can't load drivers from \"%s\"", directory), e);
        }
        log.info(format("Loaded [%s] drivers", files.size()));
    }
}
