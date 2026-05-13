package com.iskren.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iskren.model.Member;
import com.iskren.model.Order;
import com.iskren.model.OrderItem;
import com.iskren.model.Product;
import com.iskren.repository.MemberRepository;
import com.iskren.repository.OrderRepository;
import com.iskren.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
        MemberRepository memberRepository,
        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void createOrder(String memberId, List<OrderItem> items) {
        
        double totalPrice = 0.0;

        //validate and prepare items
        for (OrderItem item : items){
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found."));
            
            if(product.getStock() < item.getQuantity()){
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            
            item.setProductName(product.getName());
            item.setPriceAtPurchase(product.getPrice());

            totalPrice += product.getPrice() * item.getQuantity();
        }

        //create order
        Order order = new Order();
        order.setMemberId(memberId);
        order.setItems(items);
        order.setTotalPrice(totalPrice);
        order.setStatus("CREATED");
        orderRepository.save(order);

        //update stock
        for (OrderItem item : items){
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow();

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        //update member stats
        Member member = memberRepository.findById(memberId)
            .orElseThrow();
        
        member.setOrderCount(member.getOrderCount() + 1);
        member.setTotalSpent(member.getTotalSpent() + totalPrice);

        memberRepository.save(member);
    }
}
