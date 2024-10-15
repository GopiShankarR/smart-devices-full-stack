package com.example;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private double price;
    private String description;
    private String manufacturer;
    private String imageUrl;
    private String category;
    private List<Accessory> accessories;

    // Constructor with accessories
    public Product(int id, String name, double price, String description, String manufacturer, String imageUrl, String category, List<Accessory> accessories) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.manufacturer = manufacturer;
        this.imageUrl = imageUrl;
        this.category = category;
        this.accessories = accessories != null ? accessories : new ArrayList<>();
    }

    // Constructor without accessories (new constructor)
    public Product(int id, String name, double price, String description, String manufacturer, String imageUrl, String category) {
        this(id, name, price, description, manufacturer, imageUrl, category, new ArrayList<>()); // Empty accessories list by default
    }

    // Getters and setters for all fields
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public List<Accessory> getAccessories() {
        return accessories;
    }

    public void addAccessory(Accessory accessory) {
        this.accessories.add(accessory);
    }
}
