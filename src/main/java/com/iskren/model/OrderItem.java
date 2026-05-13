package com.iskren.model;

public class OrderItem {

    private String productId;

    private String productName; // snapshot (important)

    private double priceAtPurchase; // snapshot

    private int quantity;

    // getters 
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public int getQuantity() {
        return quantity;
    }

    // setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPriceAtPurchase(double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
