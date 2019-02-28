package io.github.metrics_matcher.assets;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AssetsLoaderTest {

    @Test
    public void loadDataSources_AllOk() throws AssetError {
        List<DataSource> dataSources = AssetsLoader.loadDataSources("src/test/resources/jsons/data-sources.json");
        assertEquals(2, dataSources.size());
        assertEquals("DEV DB", dataSources.get(0).getName());
        assertEquals("TEST DB", dataSources.get(1).getName());
    }

    @Test
    public void loadDataSources_EmptyList() throws AssetError {
        List<DataSource> dataSources = AssetsLoader.loadDataSources("src/test/resources/jsons/empty-list.json");
        assertEquals(0, dataSources.size());
    }

    @Test
    public void loadDataSources_FileNotFound() {
        assertThrows(AssetError.class, () -> {
            AssetsLoader.loadDataSources("notfound.json");
        });
    }

    @Test
    public void loadDataSources_BrokenJson() {
        assertThrows(AssetError.class, () -> {
            AssetsLoader.loadDataSources("src/test/resources/jsons/broken.json");
        });
    }

    @Test
    public void loadMetricsProfiles_AllOk() throws AssetError {
        List<MetricsProfile> metricsProfiles = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/metrics-profiles.json");
        assertEquals(2, metricsProfiles.size());
        assertEquals("Dummy study fast check", metricsProfiles.get(0).getName());
        assertEquals("Dummy study full check", metricsProfiles.get(1).getName());
    }

    @Test
    void loadQueries() throws AssetError {
        List<Query> queries = AssetsLoader.loadQueries("src/test/resources/sqls");
        assertEquals(2, queries.size());
        assertEquals("select-1", queries.get(0).getId());
        assertEquals("Connection check", queries.get(0).getTitle());
        assertEquals("select-1-notitle", queries.get(1).getId());
        assertNull(queries.get(1).getTitle());
    }
}