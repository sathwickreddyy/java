## Composite Key

- More than one primary key in a table.

Two ways:

1. @IdClass

2. @Embeddable and @EmbeddedId


Steps:

1. Create the key class
2. Use the Key class
3. Create Repository
4. Configure Datasource


## Use case

```

use user;

create table customer2(
    id int not null,
    email varchar(255) not null,
    name varchar(244) not null,
    primary key (id, email)
);

select * from customer2;

```