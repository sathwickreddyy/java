package com.sathwick.learning.jpa.inheritance_mapping.table_per_class_strategy;


/*
    Improves performance as for each child class a table is created and used thus we don't need to filter
    based on discriminator value.
    drop table payment;
    create table card(
        id int PRIMARY KEY,
        amount decimal(8,3),
        cardnumber varchar(20)
    );

    create table bankcheck(
        id int PRIMARY KEY,
        amount decimal(8,3),
        checknumber varchar(20)
    );
 */

public class TablePerClassStartegy {

}
