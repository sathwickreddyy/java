package com.sathwick.learning.jpa.inheritance_mapping.single_table_strategy;

/*

    Domain classes are POJO's where we can use inheritance as well.

    Say Payment use case

    We can make a payment using Card or Check
                                    Payment (id, amount)                => DB : discriminator : PMODE (cc, ch)
                                        /   \
                                    Card      Check
                (cardNumber, expiryDate)     (checkNumber, bankCode)

    Inheritance Mapping

    -------------------
    Single table strategy
        -------------------
        We can use a single table to store all the classes
        We can use a discriminator column to differentiate between the classes
        We can use a foreign key column to create a relationship between the classes
        We can use a join table to create a relationship between the classes


    Example:
        Table:
            create table payment(
                id int PRIMARY KEY,
                pmode varchar(2), # discrimnator
                amount decimal(8,3),
                cardnumber varchar(20),
                checknumber varchar(20)
            );

        Two important annotations used here are
        @inheritance(strategy=InheritanceType.SINGLE_TABLE)
        @DiscriminatorColumn(name="pmode", discriminatorType=DiscriminatorType.STRING)
        on top of parent entity
 */

public class SingleTableStrategy {




}
