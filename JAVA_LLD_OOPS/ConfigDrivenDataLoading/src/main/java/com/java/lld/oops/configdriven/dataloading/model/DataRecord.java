package com.java.lld.oops.configdriven.dataloading.model;

import java.util.Map;

public record DataRecord(
        Map<String, Object> data,
        int rowNumber,
        boolean valid,
        String errorMessage
) {

    public static DataRecord valid(Map<String, Object> data, int rowNumber) {
        return new DataRecord(data, rowNumber, true, null);
    }

    public static DataRecord invalid(Map<String, Object> data, int rowNumber, String errorMessage) {
        return new DataRecord(data, rowNumber, false, errorMessage);
    }
}
