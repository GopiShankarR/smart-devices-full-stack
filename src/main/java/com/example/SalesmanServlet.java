package com.example;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns = "/salesman", name = "SalesmanServlet")
public class SalesmanServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String getOrderFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "Orders.txt";
    }

    private String getUserFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "UserInfo.txt";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      String action = request.getParameter("action");

      switch (action) {
        case "getCustomers":
          getCustomers(response);
          break;
        case "getOrders":
          getOrders(response);
          break;
        case "getOrderDetails":
          getOrderDetails(request, response);
        default:
          response.getWriter().write("{\"error\": \"Invalid action\"}");
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BufferedReader reader = request.getReader();
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        String action = jsonObject.get("action").getAsString();

        switch (action) {
            case "addCustomer":
              addCustomer(jsonObject, response);
              break;
            case "add":
              addOrder(jsonObject, response);
              break;
            case "update":
              updateOrder(jsonObject, response);
              break;
            case "delete":
              deleteOrder(jsonObject, response);
              break;
            default:
              response.getWriter().write("{\"error\": \"Invalid action\"}");
        }
    }

    private void addCustomer(JsonObject jsonObject, HttpServletResponse response) throws IOException {
      String username = jsonObject.get("username").getAsString();
      String password = jsonObject.get("password").getAsString();
      String userType = "Customer"; 

      HashMap<String, UserInfo> userMap = new HashMap<>();
      boolean isSaved = false;
      String error_message = "";

      try (FileInputStream fis = new FileInputStream(new File(getUserFilePath()));
            ObjectInputStream ois = new ObjectInputStream(fis)) {
          userMap = (HashMap<String, UserInfo>) ois.readObject();
      } catch (Exception e) {
          e.printStackTrace();
      }

      if (userMap.containsKey(username)) {
        error_message = "Username already exists.";
      } else {
        try {
          UserInfo userInfo = new UserInfo(username, password, userType);
          userMap.put(username, userInfo);

          try (FileOutputStream fos = new FileOutputStream(new File(getUserFilePath()));
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
              oos.writeObject(userMap);
              isSaved = true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      JsonObject jsonResponse = new JsonObject();
      if (isSaved) {
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Customer added successfully.");
      } else {
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", error_message);
      }
      response.getWriter().write(jsonResponse.toString());
    }

    private void addOrder(JsonObject jsonObject, HttpServletResponse response) throws IOException {
        String username = jsonObject.get("username").getAsString();
        String confirmationNumber = jsonObject.get("confirmationNumber").getAsString();
        String deliveryDate = jsonObject.get("deliveryDate").getAsString();
        String deliveryOption = jsonObject.get("deliveryOption").getAsString(); 
        String status = jsonObject.get("status").getAsString(); 
        String items = jsonObject.get("items").getAsString();
        String orderPlacedDate = jsonObject.get("orderPlacedDate").getAsString();

        try (FileWriter fw = new FileWriter(new File(getOrderFilePath()), true);
            BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(username + "," + confirmationNumber + "," + deliveryDate + "," + deliveryOption + "," + status + "," + orderPlacedDate + "|" + items);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Order added successfully.");
        response.getWriter().write(jsonResponse.toString());
    }

    private void getOrderDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String confirmationNumber = request.getParameter("confirmationNumber");
      if (confirmationNumber == null || confirmationNumber.isEmpty()) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write("{\"error\": \"Confirmation number is required\"}");
          return;
      }

      File file = new File(getOrderFilePath());
      JsonObject jsonOrderDetails = new JsonObject();
      boolean orderFound = false;

      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] parts = line.split("\\|");
              String[] orderDetails = parts[0].split(",");
              if (orderDetails[1].equals(confirmationNumber)) {
                  jsonOrderDetails.addProperty("customerName", orderDetails[0]);
                  jsonOrderDetails.addProperty("confirmationNumber", orderDetails[1]);
                  jsonOrderDetails.addProperty("deliveryDate", orderDetails[2]);
                  jsonOrderDetails.addProperty("orderPlacedDate", orderDetails[4]);
                  jsonOrderDetails.addProperty("status", orderDetails[5]);
                  jsonOrderDetails.addProperty("items", parts[1]);
                  orderFound = true;
                  break;
              }
          }
      }

      if (orderFound) {
          response.getWriter().write(jsonOrderDetails.toString());
      } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.getWriter().write("{\"error\": \"Order not found\"}");
      }
    }


    private void updateOrder(JsonObject jsonObject, HttpServletResponse response) throws IOException {
    String confirmationNumber = jsonObject.get("confirmationNumber").getAsString();
    String newDeliveryDate = jsonObject.get("deliveryDate").getAsString();
    String newDeliveryOption = jsonObject.get("deliveryOption").getAsString();
    String newStatus = jsonObject.get("status").getAsString(); 
    String newItems = jsonObject.get("items").getAsString();
    String newOrderPlacedDate = jsonObject.get("orderPlacedDate").getAsString();

    File file = new File(getOrderFilePath());
    List<String> updatedLines = new ArrayList<>();
    
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(confirmationNumber)) {
                String[] parts = line.split("\\|");
                updatedLines.add(parts[0].split(",")[0] + "," + parts[0].split(",")[1] + "," + newDeliveryDate + "," + newDeliveryOption + "," + newOrderPlacedDate + "," + newStatus + "|" + newItems);
            } else {
                updatedLines.add(line);
            }
        }
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
        for (String updatedLine : updatedLines) {
            bw.write(updatedLine);
            bw.newLine();
        }
    }

    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("success", true);
    jsonResponse.addProperty("message", "Order updated successfully.");
    response.getWriter().write(jsonResponse.toString());
}

    private void deleteOrder(JsonObject jsonObject, HttpServletResponse response) throws IOException {
      String confirmationNumber = jsonObject.get("confirmationNumber").getAsString();
      List<String> remainingOrders = new ArrayList<>();
      boolean orderDeleted = false;

      try (BufferedReader br = new BufferedReader(new FileReader(new File(getOrderFilePath())))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] orderDetails = line.split(",");
              if (!orderDetails[1].equals(confirmationNumber)) {
                  remainingOrders.add(line);
              } else {
                  orderDeleted = true;
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOrderFilePath()))) {
        for (String remainingOrder : remainingOrders) {
          bw.write(remainingOrder);
          bw.newLine();
        }
      }

      JsonObject jsonResponse = new JsonObject();
      if (orderDeleted) {
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Order deleted successfully.");
      } else {
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", "Order not found.");
      }
      response.getWriter().write(jsonResponse.toString());
    }

    private void getCustomers(HttpServletResponse response) throws IOException {
        HashMap<String, UserInfo> userMap = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(getUserFilePath()));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            userMap = (HashMap<String, UserInfo>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonArray jsonArray = new JsonArray();
        for (UserInfo userInfo : userMap.values()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", userInfo.getUsername());
            jsonObject.addProperty("userType", userInfo.getUsertype());
            jsonArray.add(jsonObject);
        }
        System.out.println("getCustomers output:::::::::::" + jsonArray.toString());

        response.getWriter().write(jsonArray.toString());
    }

    private void getOrders(HttpServletResponse response) throws IOException {
      File file = new File(getOrderFilePath());
      JsonArray jsonOrders = new JsonArray();
      
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
          String line;
          while ((line = br.readLine()) != null) {
              JsonObject jsonOrder = new JsonObject();
              String[] parts = line.split("\\|");
              String[] orderDetails = parts[0].split(",");
              jsonOrder.addProperty("username", orderDetails[0]);
              jsonOrder.addProperty("confirmationNumber", orderDetails[1]);
              jsonOrder.addProperty("deliveryDate", orderDetails[2]);
              jsonOrder.addProperty("deliveryOption", orderDetails[3]);
              jsonOrder.addProperty("orderPlacedDate", orderDetails[4]);
              jsonOrder.addProperty("status", orderDetails[5]);
              jsonOrders.add(jsonOrder);
          }
      }

      JsonObject jsonResponse = new JsonObject();
      jsonResponse.add("orders", jsonOrders);
      response.getWriter().write(jsonResponse.toString());
    }

}
