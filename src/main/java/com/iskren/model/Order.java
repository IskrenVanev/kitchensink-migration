package com.iskren.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private ObjectId memberId;

    private double amount;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}