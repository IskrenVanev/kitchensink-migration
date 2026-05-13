package com.iskren.repository;

import com.iskren.model.Order;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String>{
    List<Order> findByMemberId(String memberId);

    List<Order> findByStatus(String status);
}
