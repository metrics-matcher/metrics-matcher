package io.github.metrics_matcher;

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
}
