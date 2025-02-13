package com.sathwick.learning.jpa.mongo_practise.repos;

import com.sathwick.learning.jpa.mongo_practise.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

}
