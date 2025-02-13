package com.sathwick.learning.jpa.mongo_practise;

import com.sathwick.learning.jpa.mongo_practise.model.Product;
import com.sathwick.learning.jpa.mongo_practise.repos.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MongoDBTest {

    @Autowired
    ProductRepository repository;

    @Test
    void testSave(){
        Product product = new Product();
        product.setName("Macbook pro");
        product.setPrice(2000.00f);
        Product savedProduct = repository.save(product);
        assertNotNull(savedProduct);
    }


    @Test
    void testFindById(){
        List<Product> products = repository.findAll();
        assertTrue(products.size() > 0);
        assertNotNull(products);
    }

    @Test
    void testDelete(){
        List<Product> products = repository.findAll();
        products.forEach(product -> repository.delete(product));
        assertTrue(repository.findAll().isEmpty());
    }
}