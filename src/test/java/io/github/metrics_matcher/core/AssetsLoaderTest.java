package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssetsLoaderTest {

    @Test
    public void loadDataSources_Valid() throws MetricsException {
        List<DataSource> items = AssetsLoader.loadDataSources("src/test/resources/jsons/data-sources.json");
        assertEquals(2, items.size());
        assertEquals("DEV DB", items.get(0).getName());
        assertEquals("TEST DB", items.get(1).getName());
    }

    @Test
    public void loadDataSources_EmptyList() throws MetricsException {
        List<DataSource> items = AssetsLoader.loadDataSources("src/test/resources/jsons/empty-list.json");
        assertEquals(0, items.size());
    }

    @Test
    public void loadDataSources_FileNotFound() {
        assertThrows(MetricsException.class, () -> {
            AssetsLoader.loadDataSources("notfound.json");
        });
    }

    @Test
    public void loadDataSources_BrokenJson() {
        assertThrows(MetricsException.class, () -> {
            AssetsLoader.loadDataSources("src/test/resources/jsons/broken.json");
        });
    }

    @Test
    public void loadMetricsProfiles_Correct() throws MetricsException {
        List<MetricsProfile> items = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/metrics-profiles.json");
        assertEquals(2, items.size());
        assertEquals("Dummy study fast check", items.get(0).getName());
        assertEquals("Dummy study full check", items.get(1).getName());
    }

    @Test
    public void loadMetricsProfiles_EmptyList() throws MetricsException {
        List<MetricsProfile> items = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/empty-list.json");
        assertEquals(0, items.size());
    }

    @Test
    public void loadMetricsProfiles_FileNotFound() {
        assertThrows(MetricsException.class, () -> {
            AssetsLoader.loadMetricsProfiles("notfound.json");
        });
    }

    @Test
    public void loadMetricsProfiles_BrokenJson() {
        assertThrows(MetricsException.class, () -> {
            AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/broken.json");
        });
    }


    @Test
    void loadQueries() throws MetricsException {
        List<Query> queries = AssetsLoader.loadQueries("src/test/resources/sqls");
        assertEquals(2, queries.size());
        assertEquals("select-1", queries.get(0).getId());
        assertEquals("Connection check", queries.get(0).getTitle());
        assertEquals("SELECT 1 FROM DUAL", queries.get(0).getSql());

        assertEquals("select-1-notitle", queries.get(1).getId());
        assertNull(queries.get(1).getTitle());
        assertEquals("SELECT 1 FROM DUAL", queries.get(1).getSql());
    }
}