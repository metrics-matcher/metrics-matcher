package io.github.metrics_matcher;

import io.github.metrics_matcher.core.Task;

import java.util.Arrays;
import java.util.List;


public class TempData {
    public static final List<Task> DATA = Arrays.asList(Task.builder()
                    .metricsProfile("Dummy study fast check").queryTitle("Connection check")
                    .expected("1").result("1").status(Task.Status.OK).duration(1.23).build(),
            Task.builder()
                    .metricsProfile("Dummy study fast check").queryTitle("select-1-notitle")
                    .expected("1").result("1").status(Task.Status.MISMATCH).duration(1.23).build(),
            Task.builder()
                    .metricsProfile("Dummy study full check").queryTitle("select-1-notitle")
                    .expected("1").result("Oralce error: #123").status(Task.Status.ERROR).duration(1.23).build()
    );
}
