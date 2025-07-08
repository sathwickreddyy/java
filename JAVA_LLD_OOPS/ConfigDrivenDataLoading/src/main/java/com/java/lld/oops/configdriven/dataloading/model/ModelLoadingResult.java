package com.java.lld.oops.configdriven.dataloading.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced result object for model loading operations with error tracking
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
        String errorMessage,
        List<String> validationErrors  // New field for detailed error messages
) {

    public static <T> ModelLoadingResult<T> success(String modelType, List<T> models,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs,
                                                    List<String> validationErrors) {
        return new ModelLoadingResult<>(
                modelType,
                true,
                models,
                totalRecords,
                successfulRecords,
                errorRecords,
                durationMs,
                LocalDateTime.now(),
                null,
                validationErrors
        );
    }

    public static <T> ModelLoadingResult<T> failure(String modelType, String errorMessage,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs,
                                                    List<String> validationErrors) {
        return new ModelLoadingResult<>(
                modelType,
                false,
                List.of(),
                totalRecords,
                successfulRecords,
                errorRecords,
                durationMs,
                LocalDateTime.now(),
                errorMessage,
                validationErrors
        );
    }
}
