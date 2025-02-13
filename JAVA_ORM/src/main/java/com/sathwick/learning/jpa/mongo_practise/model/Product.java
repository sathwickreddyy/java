package com.sathwick.learning.jpa.mongo_practise.model;


import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
public class Product {
    @Id
    private String id;
    private String name;
    private float price;
}
