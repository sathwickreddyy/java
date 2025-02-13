package com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy;


/*
    Payment has it's own table (Id, amount)
    Card had its own table (Id, cardNumber)
    Cheque had its own table (Id, chequeNumber)

    Id in parent table is the PK and Id in the child tables are FK's

    - Best : Stores minimal data and no redundant data
    - Drawback : More number of tables and complex queries and joins

    Parent Entity Annotations: @Inheritance(strategy = InheritanceType.JOINED)
    Child Entity Annotations: @PrimaryKeyJoinColumn(name = "id")


    Pre-req's: MySQL

        drop table payment;
        drop table bankcheck;
        drop table card;

        create table payment(
            id int PRIMARY KEY,
            amount decimal(8,3)
        );

        create table bankcheck(
            id int,
            checknumber varchar(20),
            foreign key (id) references payment(id)
        );

        create table card(
            id int,
            cardnumber varchar(20),
            foreign key (id) references payment(id)
        );
 */

public class JoinedTableStrategy {

}
