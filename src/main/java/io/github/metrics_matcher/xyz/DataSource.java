package io.github.metrics_matcher.xyz;

import lombok.Value;

@Value
public class DataSource {
    private final String name;
    private final String url;

    private final int timeout;

    private final String username;
    private final String password;

}
