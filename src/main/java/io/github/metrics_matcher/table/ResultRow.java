package io.github.metrics_matcher.table;

import javafx.beans.property.SimpleStringProperty;

public class ResultRow {
    private final SimpleStringProperty metricsProfile = new SimpleStringProperty("");
    private final SimpleStringProperty query = new SimpleStringProperty("");
    private final SimpleStringProperty expectedValue = new SimpleStringProperty("");
    private final SimpleStringProperty actualValue = new SimpleStringProperty("");
    private final SimpleStringProperty executionStatus = new SimpleStringProperty("");
    private final SimpleStringProperty executionTime = new SimpleStringProperty("");


}
