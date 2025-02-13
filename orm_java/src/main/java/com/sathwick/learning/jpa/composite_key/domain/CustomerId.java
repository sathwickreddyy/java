package com.sathwick.learning.jpa.composite_key.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
@Embeddable
public class CustomerId implements Serializable {
    private static final long serialVersionUID = 1L;
    private String email;
    private int id;
}
