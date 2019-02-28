package io.github.metrics_matcher;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public final class ConfigLoader {

    public static List<DataSource> loadDataSources() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("configs", "data-sources.json"))) {
            Gson gson = new Gson();
            DataSource[] dataSources = gson.fromJson(reader, DataSource[].class);
            return Arrays.asList(dataSources);
        }
    }

    public static List<MetricsProfile> loadMetricsProfiles() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("configs", "metrics-profiles.json"))) {
            Gson gson = new Gson();
            MetricsProfile[] dataSources = gson.fromJson(reader, MetricsProfile[].class);
            return Arrays.asList(dataSources);
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
