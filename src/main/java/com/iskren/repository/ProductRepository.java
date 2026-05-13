package com.iskren.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.iskren.model.Product;

public interface ProductRepository extends MongoRepository<Product, String>{

}
