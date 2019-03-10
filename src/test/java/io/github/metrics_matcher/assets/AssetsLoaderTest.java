package io.github.metrics_matcher.assets;

import static org.junit.jupiter.api.Assertions.*;

import io.github.metrics_matcher.core.AssetsLoader;
import io.github.metrics_matcher.core.DataSource;
import io.github.metrics_matcher.core.MetricsException;
import io.github.metrics_matcher.core.MetricsProfile;
import io.github.metrics_matcher.core.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssetsLoaderTest {

    @Test
    public void loadDataSources_AllOk() throws MetricsException {
        List<DataSource> dataSources = AssetsLoader.loadDataSources("src/test/resources/jsons/data-sources.json");
        assertEquals(2, dataSources.size());
        assertEquals("DEV DB", dataSources.get(0).getName());
        assertEquals("TEST DB", dataSources.get(1).getName());
    }

    @Test
    public void loadDataSources_EmptyList() throws MetricsException {
        List<DataSource> dataSources = AssetsLoader.loadDataSources("src/test/resources/jsons/empty-list.json");
        assertEquals(0, dataSources.size());
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
    public void loadMetricsProfiles_AllOk() throws MetricsException {
        List<MetricsProfile> metricsProfiles = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/metrics-profiles.json");
        assertEquals(2, metricsProfiles.size());
        assertEquals("Dummy study fast check", metricsProfiles.get(0).getName());
        assertEquals("Dummy study full check", metricsProfiles.get(1).getName());
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