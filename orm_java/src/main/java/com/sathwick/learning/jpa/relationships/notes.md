## Associations

- We use normalisation to avoid redundancy.
- Tables have relationships with other tables.

## Jpa Associations

- 1-1 (One to One)
  - Example: Person ---1----1--- License
    (Long Id, License lic)       (Long Id, Person person)
- Many-to-Many
  - An order can have multiple products
  - A product can belong to multiple orders
    - Order(Long id, Set<Product> products)
    - Product(Long id, String name, Set<Order> orders)
- One to Many / Many to One
  - Customer can have multiple phone numbers
  - A phone number always belongs to a single customer2
    - Customer(Long id, Set<PhoneNumber> numbers) One
    - PhoneNumber(Long id, String number) Many
- Above associations can be occurred in 2 modes: **Uni-directional and Bi-directional**.
