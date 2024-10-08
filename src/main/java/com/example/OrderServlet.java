package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

@WebServlet(urlPatterns = "/order", name = "OrderServlet")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();

    private String getOrderFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "Orders.txt";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BufferedReader reader = request.getReader();
        JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
        String action = jsonObject.get("action").getAsString();
        String confirmationNumber = jsonObject.get("confirmationNumber").getAsString();
        System.out.println("action: " + action + "confirmationNumber:" + confirmationNumber);
        if ("cancelOrder".equals(action)) {
            cancelOrder(confirmationNumber, response);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<OrderInfo> orders = new ArrayList<>();
        String filePath = getOrderFilePath();

        JsonArray jsonOrders = new JsonArray();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
              if(orderDetails[0].equals(username)) {
                jsonOrders.add(jsonOrder);
              }
          }
      }

      JsonObject jsonResponse = new JsonObject();
      jsonResponse.add("orders", jsonOrders);
      response.getWriter().write(jsonResponse.toString());
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.addHeader("Access-Control-Max-Age", "3600");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void cancelOrder(String confirmationNumber, HttpServletResponse response) throws IOException {

        if (confirmationNumber == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username and confirmation number are required.");
            return;
        }

        File file = new File(getOrderFilePath());
        boolean orderFound = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                String[] orderDetails = parts[0].split(",");
                
                if (orderDetails[1].equals(confirmationNumber)) {
                    orderFound = true;
                    lines.add(orderDetails[0] + "," + orderDetails[1] + "," + orderDetails[2] + "," + orderDetails[3] + "," + "orderCanceled" + parts[1]);
                } else {
                    lines.add(line);
                }
            }

            try (BufferedWriter tempBw = new BufferedWriter(new FileWriter(file))) {
                for (String l : lines) {
                    tempBw.write(l);
                    tempBw.newLine();
                }
            }
        }

        if (orderFound) {
            sendSuccessResponse(response, "Order canceled successfully.");
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Order not found.");
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
