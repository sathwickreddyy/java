package com.java.lld.oops.configdriven.dataloading;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigDrivenDataLoadingApplication implements CommandLineRunner {

    @Autowired
    DataLoaderConfiguration config;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataLoaderConfiguration initialized");
        log.info("{}", config);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigDrivenDataLoadingApplication.class, args);
    }

}
