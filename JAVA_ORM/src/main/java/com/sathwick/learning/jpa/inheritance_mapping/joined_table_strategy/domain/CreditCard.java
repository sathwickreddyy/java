package com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "card")
@PrimaryKeyJoinColumn(name = "id")
public class CreditCard extends Payment {
    private String cardnumber;
}
