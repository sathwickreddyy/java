package com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonAlias("data")
    private Map<String, Object> otherDetails;
}
