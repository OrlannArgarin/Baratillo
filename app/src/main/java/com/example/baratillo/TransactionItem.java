package com.example.baratillo;

import java.io.Serializable;

public class TransactionItem implements Serializable {
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalCost;
    private double discount; // new field for discount
    private double amountPaid; // new field for amount paid
    private double change; // new field for change

    // Constructor without discount, amountPaid, and change
    public TransactionItem(String productName, int quantity, double unitPrice, double totalCost) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalCost = totalCost;
    }

    // Constructor with discount, amountPaid, and change
    public TransactionItem(String productName, int quantity, double unitPrice, double totalCost,
                           double discount, double amountPaid, double change) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalCost = totalCost;
        this.discount = discount;
        this.amountPaid = amountPaid;
        this.change = change;
    }

    // Getters and setters for new fields

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    // Remaining getters and setters for existing fields

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

}
