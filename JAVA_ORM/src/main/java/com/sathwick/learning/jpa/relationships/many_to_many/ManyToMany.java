package com.sathwick.learning.jpa.relationships.many_to_many;


/*
    Example:
                ---->
       Patient2          Doctor
                <----
            A patient can have multiple doctors and a doctor can have many patients.
       Programmer <------> Project
            A programmer can work on many projects and a project can have many programmers.

      In case of Patient2 and Doctor
              Patient2 1 -----> * Doctor
              Patient2 * -----> 1 Doctor
      Patient2 Class
        List<Doctor> doctors;
      Doctor Class
        List<Patient2> patients;

      In Database we should have 3 tables
        Patience(id, fname, lname)
        Doctor(id, fname, lname)
        Patient_Doctors(patient_id, doctor_id)

 */

/*
    MySQL: Programmer Project UseCase

    create table programmer (
        id int primary key,
        name varchar(30),
        salary int
    );

    create table project (
        id int primary key,
        name varchar(30)
    );

    create table programmers_projects (
        programmer_id int,
        project_id int,
        foreign key (programmer_id) references programmer(id),
        foreign key (project_id) references project(id)
    );

    select * from programmer;
    select * from project;
    select * from programmers_projects;

 */
public class ManyToMany {

}
