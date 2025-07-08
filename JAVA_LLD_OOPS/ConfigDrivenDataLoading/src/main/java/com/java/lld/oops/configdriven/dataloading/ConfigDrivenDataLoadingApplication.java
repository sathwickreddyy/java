package com.java.lld.oops.configdriven.dataloading;

import com.java.lld.oops.configdriven.dataloading.dto.MarketDataDTO;
import com.java.lld.oops.configdriven.dataloading.model.ModelLoadingResult;
import com.java.lld.oops.configdriven.dataloading.service.DataOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class ConfigDrivenDataLoadingApplication implements CommandLineRunner {

    @Autowired
    DataOrchestrator dataOrchestrator;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataLoaderConfiguration initialized:");

        try {
            ModelLoadingResult<MarketDataDTO> result1 = dataOrchestrator.executeModelLoading("market_data_model", MarketDataDTO.class);

////            String reportingDate = LocalDate.now().toString();
////            ExecutionResult result2 = dataOrchestrator.executeDataSourcingWithBiTemporality(
////                    "market_data_csv_bitemporal", reportingDate);
//
////            List<ExecutionResult> results = List.of(result1); //, result2);
//
//            // Print results
//            log.info("Execution results:");
//            results.forEach(result -> {
//                log.info("Data source: {}, Records processed: {}, Records per second: {}, Errors: {}",
//                        result.dataSourceName(),
//                        result.processedRecords(),
//                        result.stats() != null ? result.stats().recordsPerSecond() : "N/A",
//                        result.errors());
//            });
            printModelLoadingResult(result1);
        } catch (Exception e) {
            log.error("Unexpected error during data loading execution: {}", e.getMessage(), e);
            throw e; // Rethrow if you want the application to fail fast
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
        System.out.println("Models Loaded       : ");
        result.models().forEach(model -> System.out.println(" - " + model));
        System.out.println("--------------------------------");
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigDrivenDataLoadingApplication.class, args);
    }

}
