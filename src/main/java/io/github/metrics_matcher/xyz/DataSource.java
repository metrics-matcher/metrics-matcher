package io.github.metrics_matcher.xyz;

import lombok.Value;

@Value
public class DataSource {
    String name;
    String url;
    int timeout;
    String username;
    String password;
}
