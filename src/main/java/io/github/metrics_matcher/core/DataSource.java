package io.github.metrics_matcher.core;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value(staticConstructor = "of")
@EqualsAndHashCode(of = {"name", "url", "username"})
public class DataSource {
    String name;
    String url;
    int timeout;
    String username;
    String password;
}
