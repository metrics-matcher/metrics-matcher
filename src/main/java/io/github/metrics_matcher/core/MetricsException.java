package io.github.metrics_matcher.core;

public class MetricsException extends Exception {
    MetricsException(String message) {
        super(message);
    }

    MetricsException(String message, Throwable cause) {
        super(message, cause);
    }
}
