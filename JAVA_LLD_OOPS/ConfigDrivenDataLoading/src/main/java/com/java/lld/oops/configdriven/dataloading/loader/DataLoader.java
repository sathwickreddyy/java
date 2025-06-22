package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;

import java.util.stream.Stream;

public interface DataLoader {
    String getType();
    Stream<DataRecord> loadData(DataLoaderConfiguration.DataSourceDefinition config);
}
