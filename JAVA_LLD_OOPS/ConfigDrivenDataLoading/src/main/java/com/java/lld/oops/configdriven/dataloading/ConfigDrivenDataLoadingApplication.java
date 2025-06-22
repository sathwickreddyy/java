package com.java.lld.oops.configdriven.dataloading;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.ExecutionResult;
import com.java.lld.oops.configdriven.dataloading.service.DataOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class ConfigDrivenDataLoadingApplication implements CommandLineRunner {

    @Autowired
    DataLoaderConfiguration config;

    @Autowired
    DataOrchestrator dataOrchestrator;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataLoaderConfiguration initialized:");

        try {
            ExecutionResult result1 = dataOrchestrator.executeDataSourcing("market_data_csv");
            String reportingDate = LocalDate.now().toString();
            ExecutionResult result2 = dataOrchestrator.executeDataSourcingWithBiTemporality(
                    "market_data_csv_bitemporal", reportingDate);

            List<ExecutionResult> results = List.of(result1, result2);

            // Print results
            log.info("Execution results:");
            results.forEach(result -> {
                log.info("Data source: {}, Records processed: {}, Records per second: {}, Errors: {}",
                        result.dataSourceName(),
                        result.processedRecords(),
                        result.stats() != null ? result.stats().recordsPerSecond() : "N/A",
                        result.errors());
            });

        } catch (Exception e) {
            log.error("Unexpected error during data loading execution: {}", e.getMessage(), e);
            throw e; // Rethrow if you want the application to fail fast
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigDrivenDataLoadingApplication.class, args);
    }

}
