- Used when there is an HAS-A relation between tables
  - Example: Employee has-a Address 
    - (id, name)Employee Details, (street, city, state, country) Address table attributes
    - If we wanted to store entire information here we use this component mapping.

### How to Use this?

1. We need to put `@Embeddable` annotation to the **Address Class** here
    - This annotation say's to JPA that this class altogether is not an entity but embedded into an entity.
2. `@Embedded` annotation to the field in the Employee Class.

### Use case

MySQL:
```
    create table employee (
        id int, 
        streetaddress varchar(30),
        city varchar(20),
        state varchar(20),
        zipcode varchar(20),
        country varchar(20)
    )
```