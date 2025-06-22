package com.java.lld.oops.configdriven.dataloading.model;

public record LoadingStats(
        long readTimeMs,
        long processTimeMs,
        long writeTimeMs,
        int batchCount,
        double recordsPerSecond
) {}