package com.sathwick.learning.jpa.relationships.one_to_many;

/*
    Client 1 ---------- * PhoneNumber
    OneToMany -->
                       <-- ManyToOne

    BiDirectional Association
   ------------------------------
     Client (Parent Entity)

     @OneToMany
     List<PhoneNumber> phoneNumbers;


     PhoneNumber (Child Entity)

     @ManyToOne
     Client client;
   --------------------------------

   Unidirectional Association
   ------------------------------
     Client (Parent Entity)
     @OneToMany
     List<PhoneNumber> phoneNumbers;

     PhoneNumber (Child Entity)

     --------------------------------
 */

/*
    My SQL Definition


    create table customer (
        id int primary key AUTO_INCREMENT,
        name varchar(20)
    );

    create table phone (
        id int primary key AUTO_INCREMENT,
        number varchar(20),
        customer_id int,
        type varchar(20),
        foreign key (customer_id) references customer(id)
    );

    select * from customer;
    select * from phone;


 */
public class OneToMany {

}

/*
    Cascading
    This helps in propagating the changes made to the parent entity to the child entities.
    We can control how the propagation happens and at what level with an enumeration.

    cascade = "persist"
        The insert operation on main object should be propagated to child object.
    cascade = "merge"
        An insert or update operation on main object should be propagated to child object.
    cascade = "remove"
        A delete operation on main object should be propagated to child object.
    cascade = "refresh"
        if manually refreshing the main object then it will be propagated to child object.
    cascade = "detach"
        if manually detaching the main object will detach all the child objects.
    cascade = "all"
        All the operations will be cascaded to child object.
 */
