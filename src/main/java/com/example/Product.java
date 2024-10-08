package com.example;

import java.util.List;
import java.io.Serializable;

public class Product implements Serializable {
  private String id;
  private String name;
  private double price;
  private String description;
  private String manufacturer;
  private String imageUrl;
  private String type;
  private List<String> aids;

  public Product(String id, String name, double price, String description, String manufacturer, String imageUrl, String type, List<String> aids) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.description = description;
    this.manufacturer = manufacturer;
    this.imageUrl = imageUrl;
    this.type = type;
    this.aids = aids;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public double getPrice() {
    return price;
  }

  public void setName(double price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getAids() {
    return aids;
  }

  public void setAids(List<String> aids) {
    this.aids = aids;
  }
}