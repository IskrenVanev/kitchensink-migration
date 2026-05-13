package com.iskren.dto;

import java.util.List;

import com.iskren.model.OrderItem;

public class CreateOrderRequestDTO {
    private String memberId;

    private List<OrderItem> items;

    // getters
    public String getMemberId() {
        return memberId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    // setters
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
