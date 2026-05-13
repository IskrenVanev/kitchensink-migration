package com.iskren.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private ObjectId memberId;

    private List<OrderItem> items;

    private double totalPrice;

    private String status;

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId != null ? memberId.toHexString() : null;
    }
    
    public void setMemberId(String memberId) {
        if (ObjectId.isValid(memberId)) {
            this.memberId = new ObjectId(memberId);
        } else {
            throw new IllegalArgumentException("Invalid ObjectId");
        }
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}