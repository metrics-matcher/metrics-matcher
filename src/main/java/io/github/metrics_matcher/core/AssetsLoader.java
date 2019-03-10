package io.github.metrics_matcher.core;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public final class AssetsLoader {
    private static final String CAN_NOT_READ = "Can't read file \"%s\"";
    private static final String CAN_NOT_PARSE = "Can't parse file \"%s\"";
    private static final String FILES_NOT_FOUND = "Files not found in \"%s\"";

    private static Gson GSON = new Gson();

    private static List<File> listFiles(String directory, String extension) {
        File[] files = new File(directory).listFiles(file -> file.isFile() && file.getName().endsWith(extension));
        if (files == null) {
            return Collections.emptyList();
        } else {
            return Stream.of(files).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
    }

    public static List<DataSource> loadDataSources(String filepath) throws MetricsException {
        log.info("Loading data sources from [{}]", filepath);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            DataSource[] dataSources = GSON.fromJson(reader, DataSource[].class);
            log.info("Loaded {} data sources", dataSources.length);
            return Arrays.asList(dataSources);
        } catch (IOException e) {
            throw new MetricsException(format(CAN_NOT_READ, filepath), e);
        } catch (JsonParseException e) {
            throw new MetricsException(format(CAN_NOT_PARSE, filepath), e);
        }
    }

    public static List<MetricsProfile> loadMetricsProfiles(String filepath) throws MetricsException {
        log.info("Loading metrics profiles from [{}]", filepath);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            MetricsProfile[] dataSources = GSON.fromJson(reader, MetricsProfile[].class);
            log.info("Loaded {} metrics profiles", dataSources.length);
            return Arrays.asList(dataSources);
        } catch (IOException e) {
            throw new MetricsException(format(CAN_NOT_READ, filepath), e);
        } catch (JsonParseException e) {
            throw new MetricsException(format(CAN_NOT_PARSE, filepath), e);
        }
    }

    public static List<Query> loadQueries(String directory) throws MetricsException {
        log.info("Loading queries from [{}]", directory);
        List<File> files = listFiles(directory, ".sql");
        if (files.isEmpty()) {
            throw new MetricsException(format(FILES_NOT_FOUND, directory));
        }
        final List<Query> queries = new ArrayList<>(files.size());
        for (File file : files) {
            String filename = file.getName();
            log.info("Loading query [{}]", filename);
            String id = filename.substring(0, filename.length() - 4).trim();
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
                queries.add(Query.of(id, title, String.join("\n", lines)));
            } catch (IOException e) {
                throw new MetricsException(format(CAN_NOT_READ, filename), e);
            }
        }
        log.info("Loaded {} queries", queries.size());
        return queries;
    }

    public static void loadDrivers(String directory) throws MetricsException {
        log.info("Loading drivers from [{}]", directory);
        List<File> files = listFiles(directory, ".jar");
        if (files.isEmpty()) {
            throw new MetricsException(format(FILES_NOT_FOUND, directory));
        }
        try {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            for (File file : files) {
                String filename = file.getName();
                log.info("Loading driver [{}]", filename);
                try {
                    method.invoke(classLoader, file.toURI().toURL());
                } catch (Exception e) {
                    throw new MetricsException(format("Can't load driver \"%s\"", filename), e);
                }
            }
        } catch (Exception e) {
            throw new MetricsException(format("Can't load drivers from \"%s\"", directory), e);
        }
        log.info("Loaded {} drivers", files.size());
    }
}
