package com.sathwick.learning.jpa.practise.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/*
create table doctor(
    id int primary key,
    first_name varchar(20),
    last_name varchar(30),
    speciality varchar(30)
);
 */
@Setter
@Getter
@Table(name = "doctor")
@Entity
@ToString(exclude = {"appointments", "patients"})
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String firstName;
    private String lastName;
    private String speciality;
    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;
    @ManyToMany(mappedBy = "doctors")
    private List<Patient> patients;
}
