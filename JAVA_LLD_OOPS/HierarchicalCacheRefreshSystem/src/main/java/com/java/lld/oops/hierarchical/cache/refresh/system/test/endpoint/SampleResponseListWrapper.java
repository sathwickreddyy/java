package com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleResponseListWrapper implements Serializable {
    private List<SampleResponse> sampleResponseList;
}
