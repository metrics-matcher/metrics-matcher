package io.github.metrics_matcher.assets;

import io.github.metrics_matcher.DataSource;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class AssetsLoaderTest {

    @Test
    public void loadDataSources_AllOk() throws IOException {
        List<DataSource> dataSources = AssetsLoader.loadDataSources("src/test/resources/data-sources.json");
        assertEquals(2, dataSources.size());
        assertEquals("DEV DB", dataSources.get(0).getName());
        assertEquals("TEST DB", dataSources.get(1).getName());
    }

    @Test
    public void loadDataSources_FileNotFound() {
        assertThrows(Exception.class, () -> {
            AssetsLoader.loadDataSources("notfound/data-sources.json");
        });
    }

    @Test
    public void loadDataSources_BrokenJson() {
        assertThrows(IOException.class, () -> {
            AssetsLoader.loadDataSources("src/test/resources/data-sources-broken.json");
        });
    }

}