package com.example;

import java.io.Serializable;

public class Accessory implements Serializable {
    private String aid;
    private String name;
    private String imageURL;

    public Accessory(String aid, String name, String imageURL) {
        this.aid = aid;
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
