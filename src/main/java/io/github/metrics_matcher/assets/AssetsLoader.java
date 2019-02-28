package io.github.metrics_matcher.assets;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@UtilityClass
public final class AssetsLoader {

    public static List<DataSource> loadDataSources(String filepath) throws AssetError {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            Gson gson = new Gson();
            DataSource[] dataSources = gson.fromJson(reader, DataSource[].class);
            return Arrays.asList(dataSources);
        } catch (IOException e) {
            throw new AssetError("Can't read file", e);
        } catch (JsonParseException e) {
            throw new AssetError("Can't parse data", e);
        }
    }

    public static List<MetricsProfile> loadMetricsProfiles(String filepath) throws AssetError {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filepath))) {
            Gson gson = new Gson();
            MetricsProfile[] dataSources = gson.fromJson(reader, MetricsProfile[].class);
            return Arrays.asList(dataSources);
        } catch (IOException e) {
            throw new AssetError("Can't read file", e);
        } catch (JsonParseException e) {
            throw new AssetError("Can't parse data", e);
        }
    }

    public static void loadDrivers() throws Exception {
        URL url = new URL("jar", "", "file:drivers/h2-1.4.198.jar!/");
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }

}

/*

import lombok.Value;

        import java.io.*;
        import java.lang.reflect.Method;
        import java.net.URL;
        import java.net.URLClassLoader;
        import java.nio.file.*;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.stream.Collectors;

public class Main {

    @Value(staticConstructor = "of")
    public static class Query {
        String id;
        String title;
        String sql;
    }

    private static File[] listFiles(String directory, String extension) {
        return new File(directory).listFiles(file -> file.isFile() && file.getName().endsWith(extension));
    }

    public static void loadQueries() {
        File[] files = listFiles("queries", ".sql");

        List<Query> queries = new ArrayList<>(files.length);
        for (File file : files) {
            try {
                String filename = file.getName();
                String id = filename.substring(0, filename.length() - 4);
                List<String> lines = Files.readAllLines(file.toPath());
                System.out.println(id);
//                queries.add(Query.of(file.getName()))
            } catch (IOException e) {
                System.out.println("Can't read file");
            }
        }
    }

    public static void loadDrivers() throws Exception {
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);

        Files.list(Paths.get("drivers"))
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".jar"))
                .forEach(jar -> {
                    System.out.println("Loading library " + jar);
                    try {
                        method.invoke(classLoader, jar.toUri().toURL());
                    } catch (Exception e) {
                        System.out.println("Failed, " + e.getMessage());
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        loadQueries();
//        loadDrivers();
    }
}

*/