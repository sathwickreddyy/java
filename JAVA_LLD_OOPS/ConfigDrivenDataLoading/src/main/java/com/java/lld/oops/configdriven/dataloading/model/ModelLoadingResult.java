package com.java.lld.oops.configdriven.dataloading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Java 11 compatible result object for model loading operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelLoadingResult<T> {
    private String modelType;
    private boolean success;
    private List<T> models;
    private int totalRecords;
    private int successfulRecords;
    private int errorRecords;
    private long durationMs;
    private LocalDateTime executionTime;
    private String errorMessage;
    private List<String> validationErrors;

    public static <T> ModelLoadingResult<T> success(String modelType, List<T> models,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs,
                                                    List<String> validationErrors) {
        ModelLoadingResult<T> result = new ModelLoadingResult<>();
        result.setModelType(modelType);
        result.setSuccess(true);
        result.setModels(models);
        result.setTotalRecords(totalRecords);
        result.setSuccessfulRecords(successfulRecords);
        result.setErrorRecords(errorRecords);
        result.setDurationMs(durationMs);
        result.setExecutionTime(LocalDateTime.now());
        result.setValidationErrors(validationErrors);
        return result;
    }

    public static <T> ModelLoadingResult<T> failure(String modelType, String errorMessage,
                                                    int totalRecords, int successfulRecords,
                                                    int errorRecords, long durationMs,
                                                    List<String> validationErrors) {
        ModelLoadingResult<T> result = new ModelLoadingResult<>();
        result.setModelType(modelType);
        result.setSuccess(false);
        result.setModels(List.of());
        result.setTotalRecords(totalRecords);
        result.setSuccessfulRecords(successfulRecords);
        result.setErrorRecords(errorRecords);
        result.setDurationMs(durationMs);
        result.setExecutionTime(LocalDateTime.now());
        result.setErrorMessage(errorMessage);
        result.setValidationErrors(validationErrors);
        return result;
    }
}