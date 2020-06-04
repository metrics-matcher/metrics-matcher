package io.github.metrics_matcher.core;

import lombok.Value;

import java.util.Map;

@Value
public class JdbcResult {
    public enum Error {
        EMPTY_RESULT("No result"),
        MULTIPLE_ROWS("Multiple results");

        private String title;

        Error(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private Map<String, Object> data;
    private Error error;

    public JdbcResult(Map<String, Object> data) {
        this.data = data;
        this.error = null;
    }

    public JdbcResult(Error error) {
        this.data = null;
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }
}
