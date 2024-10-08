package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns = "/cart", name = "CartServlet")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String getFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "cartData.txt";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      HttpSession session = request.getSession();
      String username = request.getParameter("username");
      session.setAttribute("username", username);
      
      if (username == null) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
      } else {
        System.out.println("Session username:" + username);
      }

      List<CartItem> cartItems = new ArrayList<>();
      String filePath = getFilePath();
      double totalPrice = 0.0;

      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] itemData = line.split(",");
              if (itemData[0].equals(username)) {
                double price = Double.parseDouble(itemData[4]);
                cartItems.add(new CartItem(itemData[0], itemData[1], itemData[2], itemData[3], price));
                totalPrice += price;
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }

      JsonArray jsonArray = new JsonArray();
      for (CartItem item : cartItems) {
          JsonObject jsonItem = new JsonObject();
          jsonItem.addProperty("username", item.getUsername());
          jsonItem.addProperty("id", item.getId());
          jsonItem.addProperty("name", item.getName());
          jsonItem.addProperty("image", item.getImage());
          jsonItem.addProperty("price", item.getPrice());
          jsonArray.add(jsonItem);
      }

      response.getWriter().write(jsonArray.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      HttpSession session = request.getSession();
      String username = request.getParameter("username");
      session.setAttribute("username", username);

      BufferedReader reader = request.getReader();
      JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

      String action = jsonObject.get("action").getAsString();

      if ("delete".equals(action)) {
        System.out.println("Inside delete action in cart");
        String itemName = jsonObject.get("name").getAsString();
        handleDeleteItem(response, username, itemName);
      } else {
        System.out.println("Inside add action in cart");
        String itemId = jsonObject.get("id").getAsString();
        String itemName = jsonObject.get("name").getAsString();
        String itemImage = jsonObject.get("image").getAsString();
        double itemPrice = jsonObject.get("price").getAsDouble();

        if (username == null) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
        String filePath = getFilePath();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
          bw.write(username + "," + itemId + "," + itemName + "," + itemImage + "," + itemPrice);
          bw.newLine();
        } catch (IOException e) {
          e.printStackTrace();
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        response.getWriter().write(jsonResponse.toString());
      }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
      response.addHeader("Access-Control-Max-Age", "3600");
      response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleDeleteItem(HttpServletResponse response, String username, String itemName) throws IOException {
      String filePath = getFilePath();
      List<String> updatedLines = new ArrayList<>();
      boolean itemDeleted = false;

      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] itemData = line.split(",");
          if (!(itemData[0].equals(username) && itemData[2].equals(itemName))) { 
            updatedLines.add(line);
          } else {
            itemDeleted = true;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        for (String updatedLine : updatedLines) {
          bw.write(updatedLine);
          bw.newLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("success", itemDeleted);
      response.getWriter().write(jsonResponse.toString());
    }
}
