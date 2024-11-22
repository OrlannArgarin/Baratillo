package com.example.baratillo;

public class Product {
    private int id;
    private String name;
    private Float price;
    private String imageUri;

    public Product(int id, String name, Float price, String imageUri) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUri = imageUri;
    }

    // Getter methods for the fields
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Float getPrice() {
        return price;
    }

    public String getImageUri() {
        return imageUri;
    }
}

