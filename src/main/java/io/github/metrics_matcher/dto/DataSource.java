package io.github.metrics_matcher.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(of = {"name", "url", "username"})
public class DataSource {
    private String name;
    private String url;
    private int timeout;
    private String username;
    private String password;
}
