package com.sathwick.learning.jpa.practise.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;


/*
create table patient(
    id int primary key,
    first_name varchar(20),
    last_name varchar(20),
    phone varchar(20),
    provider_name varchar(30),
    copay decimal(10.5)
);
 */
@Setter
@Getter
@Entity
@Table(name = "patient")
@ToString(exclude = {"appointments", "doctors"})
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id; // primary key
    private String firstName;
    private String lastName;
    private String phone;
    @Embedded
    private Insurance insurance;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "patients_doctors",
            joinColumns = @JoinColumn(name = "patient_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "doctor_id", referencedColumnName = "id"))
    private List<Doctor> doctors;
}
