package com.sathwick.learning.jpa.composite_key.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

//@Entity
//@Setter
//@Getter
//@IdClass(CustomerId.class)
//@Table(name = "customer")
//public class Customer2 {
//    @Id
//    private int id;
//    @Id
//    private String email;
//    private String name;
//}

@Setter
@Getter
@Table(name = "customer")
@Entity
public class Customer2 {
    @EmbeddedId
    private CustomerId customerId;
    private String name;
}