package io.github.metrics_matcher.dto;

import lombok.Value;

@Value
public class DataSource {
    private String name;
    private String url;
    private int timeout;
    private String username;
    private String password;
    private String schema;
}
