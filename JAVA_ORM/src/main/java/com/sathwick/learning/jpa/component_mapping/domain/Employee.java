package com.sathwick.learning.jpa.component_mapping.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    private int id;
    private String name;
    @Embedded
    private Address address;
}
