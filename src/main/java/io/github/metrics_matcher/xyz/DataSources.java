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
public final class DataSources {

    public static List<DataSource> loadFromJsonFile(String filename) throws IOException {

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("demo", filename))) {
            Gson gson = new Gson();
            DataSource[] dataSources = gson.fromJson(reader, DataSource[].class);
            return Arrays.asList(dataSources);
        }
    }

}
