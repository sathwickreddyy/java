package com.sathwick.learning.jpa.practise.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Insurance {
    private String providerName;
    private double copay;
}
