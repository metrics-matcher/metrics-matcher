package io.github.metrics_matcher.assets;

import lombok.Value;

@Value(staticConstructor = "of")
public class DataSource {
    String name;
    String url;
    int timeout;
    String username;
    String password;
}
