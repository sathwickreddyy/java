package com.sathwick.learning.jpa.practise_2.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "patient")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String age;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ClinicalData> clinicalData = new ArrayList<>();
}
