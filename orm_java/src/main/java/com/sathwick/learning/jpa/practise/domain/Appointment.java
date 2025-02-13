package com.sathwick.learning.jpa.practise.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/*
create table appointment(
    id int primary key,
    patient_id int,
    doctor_id int,
    appointment_time datetime,
    started tinyint(1),
    ended tinyint(1),
    reason varchar(200),
    foreign key(patient_id) references patient(id),
    foreign key(doctor_id) references doctor(id)
);

 */
@Setter
@Getter
@Entity
@Table(name = "appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "appointment_time")
    private Timestamp appointmentTime;
    private boolean started;
    private boolean ended;
    private String reason;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;
}
