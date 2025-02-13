package com.sathwick.learning.jpa.practise_2.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "clinicaldata")
@JsonIgnoreProperties({"patient"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String componentName;
    private String componentValue;
    private Timestamp measuredDateTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient2 patient;
}
