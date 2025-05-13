package com.java.lld.oops.hierarchical.cache.refresh.system.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SampleResponse {
    private String id;
    private String name;
    private Map<String, String> otherDetails;
}
