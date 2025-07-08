package com.java.lld.oops.configdriven.dataloading;

import com.java.lld.oops.configdriven.dataloading.dto.MarketDataDTO;
import com.java.lld.oops.configdriven.dataloading.dto.PortfolioDTO;
import com.java.lld.oops.configdriven.dataloading.dto.RiskMetricsDTO;
import com.java.lld.oops.configdriven.dataloading.model.ModelLoadingResult;
import com.java.lld.oops.configdriven.dataloading.service.DataOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class ConfigDrivenDataLoadingApplication implements CommandLineRunner {

    @Autowired
    DataOrchestrator dataOrchestrator;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Config-Driven Data Loading Framework Testing");

        // Test Scenario 1: Perfect Data - All fields present and valid
        testScenario1_PerfectData();

        // Test Scenario 2: Missing Required Fields
        testScenario2_MissingRequiredFields();

        // Test Scenario 3: Invalid Data Types and Formats
        testScenario3_InvalidDataTypes();

        // Test Scenario 4: Null Values and Empty Strings
        testScenario4_NullAndEmptyValues();

        // Test Scenario 5: Boundary Value Testing
        testScenario5_BoundaryValues();

        // Test Scenario 6: Mixed Valid/Invalid Records
        testScenario6_MixedData();

        // Test Scenario 7: Portfolio Data with Pattern Validation
        testScenario7_PortfolioData();

        // Test Scenario 8: Risk Metrics with Financial Validation
        testScenario8_RiskMetrics();

        log.info("All test scenarios completed");
    }

    private void testScenario1_PerfectData() {
        log.info("=== Test Scenario 1: Perfect Data ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_perfect", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 1: {}", e.getMessage(), e);
        }
    }

    private void testScenario2_MissingRequiredFields() {
        log.info("=== Test Scenario 2: Missing Required Fields ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_missing_required", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 2: {}", e.getMessage(), e);
        }
    }

    private void testScenario3_InvalidDataTypes() {
        log.info("=== Test Scenario 3: Invalid Data Types ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_invalid_types", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 3: {}", e.getMessage(), e);
        }
    }

    private void testScenario4_NullAndEmptyValues() {
        log.info("=== Test Scenario 4: Null and Empty Values ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_null_empty", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 4: {}", e.getMessage(), e);
        }
    }

    private void testScenario5_BoundaryValues() {
        log.info("=== Test Scenario 5: Boundary Values ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_boundary", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 5: {}", e.getMessage(), e);
        }
    }

    private void testScenario6_MixedData() {
        log.info("=== Test Scenario 6: Mixed Valid/Invalid Data ===");
        try {
            ModelLoadingResult<MarketDataDTO> result = dataOrchestrator.executeModelLoading(
                    "market_data_mixed", MarketDataDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 6: {}", e.getMessage(), e);
        }
    }

    private void testScenario7_PortfolioData() {
        log.info("=== Test Scenario 7: Portfolio Data with Pattern Validation ===");
        try {
            ModelLoadingResult<PortfolioDTO> result = dataOrchestrator.executeModelLoading(
                    "portfolio_data_validation", PortfolioDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 7: {}", e.getMessage(), e);
        }
    }

    private void testScenario8_RiskMetrics() {
        log.info("=== Test Scenario 8: Risk Metrics with Financial Validation ===");
        try {
            ModelLoadingResult<RiskMetricsDTO> result = dataOrchestrator.executeModelLoading(
                    "risk_metrics_validation", RiskMetricsDTO.class);
            printModelLoadingResult(result);
        } catch (Exception e) {
            log.error("Error in scenario 8: {}", e.getMessage(), e);
        }
    }

    public static <T> void printModelLoadingResult(ModelLoadingResult<T> result) {
        System.out.println("---- Model Loading Result ----");
        System.out.println("Model Type          : " + result.modelType());
        System.out.println("Success             : " + result.success());
        System.out.println("Total Records       : " + result.totalRecords());
        System.out.println("Successful Records  : " + result.successfulRecords());
        System.out.println("Error Records       : " + result.errorRecords());
        System.out.println("Duration (ms)       : " + result.durationMs());
        System.out.println("Execution Time      : " + result.executionTime());
        System.out.println("Error Message       : " + (result.errorMessage() != null ? result.errorMessage() : "None"));

        // Display validation errors
        if (result.validationErrors() != null && !result.validationErrors().isEmpty()) {
            System.out.println("Validation Errors   : ");
            result.validationErrors().forEach(error -> System.out.println("  - " + error));
        }

        System.out.println("Models Loaded       : ");
        if (result.models().isEmpty()) {
            System.out.println(" - No models loaded");
        } else {
            result.models().forEach(model -> System.out.println(" - " + model));
        }
        System.out.println("--------------------------------\n");
    }


    public static void main(String[] args) {
        SpringApplication.run(ConfigDrivenDataLoadingApplication.class, args);
    }
}
