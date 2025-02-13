package com.sathwick.learning.jpa.relationships.one_to_one;

/*
    Person   1-------1 License
    A person can have only one license and a license can be owned by only one person.
    Student 1 -------  1 LibraryMembership
    A student can have only one library membership card and a library membership card can be owned by only one student.

        Case 1:
            Same primary key being used to both entities for example
            Person PK and License PK are same. (Shared PK) alway bidirectional
            Student PK and LibraryMembership PK are same.

        Case 2: Person PK and License PK are different. (Owned PK) unidirectional default
            Person(pk ... ) and License(pk, fk ... )
            Parent              Child
 */

public class OneToOne {
}
