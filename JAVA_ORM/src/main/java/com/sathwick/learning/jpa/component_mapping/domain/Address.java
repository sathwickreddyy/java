package com.sathwick.learning.jpa.component_mapping.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Setter
@Getter
public class Address {
    private String streetaddress;
    private String city;
    private String state;
    private String zipcode;
    private String country;
}
