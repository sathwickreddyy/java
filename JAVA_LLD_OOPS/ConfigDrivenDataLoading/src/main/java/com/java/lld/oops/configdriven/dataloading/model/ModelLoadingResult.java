package com.java.lld.oops.configdriven.dataloading.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result object for model loading operations
 */
public record ModelLoadingResult<T>(
        String modelType,
        boolean success,
        List<T> models,
        int totalRecords,
        int successfulRecords,
        int errorRecords,
        long durationMs,
        LocalDateTime executionTime,
        String errorMessage
) {

    public static <T> ModelLoadingResult<T> success(String modelType, List<T> models,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs) {
        return new ModelLoadingResult<>(
                modelType,
                true,
                models,
                totalRecords,
                successfulRecords,
                errorRecords,
                durationMs,
                LocalDateTime.now(),
                null
        );
    }

    public static <T> ModelLoadingResult<T> failure(String modelType, String errorMessage,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs) {
        return new ModelLoadingResult<>(
                modelType,
                false,
                List.of(),
                totalRecords,
                successfulRecords,
                errorRecords,
                durationMs,
                LocalDateTime.now(),
                errorMessage
        );
    }
}