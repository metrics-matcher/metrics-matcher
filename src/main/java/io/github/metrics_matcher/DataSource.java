package io.github.metrics_matcher;

import lombok.Value;

@Value
public class DataSource {
    String name;
    String url;
    int timeout;
    String username;
    String password;
}
