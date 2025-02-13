package com.sathwick.learning.jpa.relationships.one_to_many.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "customer")
@ToString
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;

    // OneToMany Associations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER) //
    // This is the owning side of the relationship
    // Cascade the effects on the phone number class
    private Set<PhoneNumber> numbers;

    public void addPhoneNumber(PhoneNumber number) {
        if(!Objects.isNull(number)) {
            if (Objects.isNull(numbers)) {
                numbers = new HashSet<>();
            }
            number.setCustomer(this);
            numbers.add(number);
        }
    }
}
