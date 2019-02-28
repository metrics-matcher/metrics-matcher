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
            AssetsLoader.loadDataSources("src/test/resources/jsons/data-sources-broken.json");
        });
    }

}