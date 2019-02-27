package io.github.metrics_matcher.xyz;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public final class ConfigLoader {

    public static List<DataSource> loadDataSources(String filename) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("demo", filename))) {
            Gson gson = new Gson();
            DataSource[] dataSources = gson.fromJson(reader, DataSource[].class);
            return Arrays.asList(dataSources);
        }
    }

    public static List<MetricsProfile> loadMetricsProfiles(String filename) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("demo", filename))) {
            Gson gson = new Gson();
            MetricsProfile[] dataSources = gson.fromJson(reader, MetricsProfile[].class);
            return Arrays.asList(dataSources);
        }
    }

}
