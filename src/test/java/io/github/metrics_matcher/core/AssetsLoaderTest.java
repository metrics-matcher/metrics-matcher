package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class AssetsLoaderTest {

    @Test
    public void loadDataSources_Valid() throws MetricsException {
        List<DataSource> items = AssetsLoader.loadDataSources("src/test/resources/jsons/data-sources.json");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("DEV DB");
        assertThat(items.get(1).getName()).isEqualTo("TEST DB");
    }

    @Test
    public void loadDataSources_EmptyList() throws MetricsException {
        List<DataSource> items = AssetsLoader.loadDataSources("src/test/resources/jsons/empty-list.json");
        assertThat(items).isEmpty();
    }

    @Test(expected = MetricsException.class)
    public void loadDataSources_FileNotFound() throws MetricsException {
        AssetsLoader.loadDataSources("notfound.json");
    }

    @Test(expected = MetricsException.class)
    public void loadDataSources_BrokenJson() throws MetricsException {
        AssetsLoader.loadDataSources("src/test/resources/jsons/broken.json");
    }

    @Test
    public void loadMetricsProfiles_Correct() throws MetricsException {
        List<MetricsProfile> items = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/metrics-profiles.json");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Dummy study fast check");
        assertThat(items.get(1).getName()).isEqualTo("Dummy study full check");
    }

    @Test
    public void loadMetricsProfiles_EmptyList() throws MetricsException {
        List<MetricsProfile> items = AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/empty-list.json");
        assertThat(items).isEmpty();
    }

    @Test(expected = MetricsException.class)
    public void loadMetricsProfiles_FileNotFound() throws MetricsException {
        AssetsLoader.loadMetricsProfiles("notfound.json");
    }

    @Test(expected = MetricsException.class)
    public void loadMetricsProfiles_BrokenJson() throws MetricsException {
        AssetsLoader.loadMetricsProfiles("src/test/resources/jsons/broken.json");
    }

    @Test
    public void loadQueries_Correct() throws MetricsException {
        List<Query> items = AssetsLoader.loadQueries("src/test/resources/sqls");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getId()).isEqualTo("select-1");
        assertThat(items.get(0).getTitle()).isEqualTo("Connection check");
        assertThat(items.get(0).getSql()).isEqualTo("SELECT 1 FROM DUAL");

        assertThat(items.get(1).getId()).isEqualTo("select-1-notitle");
        assertThat(items.get(1).getTitle()).isNull();
        assertThat(items.get(1).getSql()).isEqualTo("SELECT 1 FROM DUAL");
    }

    @Test(expected = MetricsException.class)
    public void loadQueries_EmptyDirectory() throws MetricsException {
        AssetsLoader.loadQueries("src/test/resources/empty");
    }

    @Test(expected = MetricsException.class)
    public void loadQueries_NotFoundDirectory() throws MetricsException {
        AssetsLoader.loadQueries("src/test/resources/not-found");
    }

    @Test
    public void loadDrivers_Correct() throws MetricsException, ClassNotFoundException {
        AssetsLoader.loadDrivers("src/test/resources/jars");
        Class.forName("io.github.xantorohara.dummy.HelloWorld");
    }

    @Test(expected = MetricsException.class)
    public void loadDrivers_EmptyDirectory() throws MetricsException {
        AssetsLoader.loadDrivers("src/test/resources/empty");
    }
}
