package com.sathwick.learning.jpa.relationships.one_to_many.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@Table(name = "phone")
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String number;
    private String type;

    // ManyToOne Associations
    @ManyToOne
    @JoinColumn(name = "customer_id")
    // tell's hibernate which column in phone number table should be associated with Customer2 table
    private Customer customer;

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", type='" + type + '\'' +
                ", customer_id=" + customer.getId() +
                '}';
    }
}
