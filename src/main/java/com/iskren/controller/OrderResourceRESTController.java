package com.iskren.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iskren.dto.CreateOrderRequestDTO;
import com.iskren.service.OrderService;

@RestController
@RequestMapping("/rest/orders")
public class OrderResourceRESTController {
    
    private final OrderService orderService;

    public OrderResourceRESTController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequestDTO request) {

        orderService.createOrder(request.getMemberId(), request.getItems());

        return ResponseEntity.ok("Order created successfully");
    }
}
