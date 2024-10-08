package com.example;

import java.io.Serializable;

public class OrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String confirmationNumber;
    private String deliveryDate;
    private String orderPlacedDate;
    private String status;

    public OrderInfo(String username, String confirmationNumber, String deliveryDate, String orderPlacedDate, String status) {
        this.username = username;
        this.confirmationNumber = confirmationNumber;
        this.deliveryDate = deliveryDate;
        this.orderPlacedDate = orderPlacedDate;
        this.status = status;
    }

    public String getUsername() {
      return username;
    }

    public String getConfirmationNumber() {
      return confirmationNumber;
    }

    public String getDeliveryDate() {
      return deliveryDate;
    }

    public String getOrderPlacedDate() {
      return orderPlacedDate;
    }

    public String getStatus() {
      return status;
    }
}
