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
import java.sql.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns = "/product", name = "ProductServlet")
public class ProductServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String DB_URL = "jdbc:mysql://localhost:3306/enterprise";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "root";

	@Override
  public void init() throws ServletException {
		super.init();
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new ServletException("MySQL JDBC Driver not found", e);
		}
  }

  @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");
        String id = request.getParameter("id");

        try (Connection conn = MySQLDataStoreUtilities.getConnection()) {
            List<Product> products;

            // Fetch all products if no category or id is provided
            if (category == null && id == null) {
                products = MySQLDataStoreUtilities.getAllProducts(conn);
            } 
            // Fetch products by category
            else if (category != null && id == null) {
                products = MySQLDataStoreUtilities.getProductsByCategory(conn, category);
            } 
            // Fetch a specific product by ID and category
            else if (category != null && id != null) {
                products = MySQLDataStoreUtilities.getProductByIdAndCategory(conn, Integer.parseInt(id), category);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            JsonArray jsonArray = new JsonArray();
            for (Product product : products) {
                JsonObject jsonProduct = new JsonObject();
                jsonProduct.addProperty("id", product.getId());
                jsonProduct.addProperty("name", product.getName());
                jsonProduct.addProperty("price", product.getPrice());
                jsonProduct.addProperty("description", product.getDescription());
                jsonProduct.addProperty("manufacturer", product.getManufacturer());
                jsonProduct.addProperty("imageUrl", product.getImageUrl());
                jsonProduct.addProperty("category", product.getCategory());
                jsonArray.add(jsonProduct);
            }

            response.getWriter().write(jsonArray.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
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
    String action = jsonObject.has("action") ? jsonObject.get("action").getAsString() : "add";

    try {
        MySQLDataStoreUtilities dbUtil = new MySQLDataStoreUtilities();
        JsonObject jsonResponse = new JsonObject();

        if ("delete".equals(action)) {
            String idToDelete = jsonObject.get("id").getAsString();
            boolean success = dbUtil.deleteProduct(idToDelete);
            jsonResponse.addProperty("success", success);

        } else if ("update".equals(action)) {
            String id = jsonObject.get("id").getAsString();
            String name = jsonObject.get("name").getAsString();
            double price = jsonObject.get("price").getAsDouble();
            String description = jsonObject.get("description").getAsString();
            String manufacturer = jsonObject.get("manufacturer").getAsString();
            String imageUrl = jsonObject.get("imageUrl").getAsString();
            String category = jsonObject.get("category").getAsString();

            boolean success = dbUtil.updateProduct(id, name, price, description, manufacturer, imageUrl, category);
            jsonResponse.addProperty("success", success);

        } else {
            String name = jsonObject.get("name").getAsString();
            double price = jsonObject.get("price").getAsDouble();
            String description = jsonObject.get("description").getAsString();
            String manufacturer = jsonObject.get("manufacturer").getAsString();
            String imageUrl = jsonObject.get("imageUrl").getAsString();
            String category = jsonObject.get("category").getAsString();
            JsonArray aidsArray = jsonObject.getAsJsonArray("aids");

            boolean success = dbUtil.addProduct(name, price, description, manufacturer, imageUrl, category, aidsArray);
            jsonResponse.addProperty("success", success);
        }

        response.getWriter().write(jsonResponse.toString());

    } catch (SQLException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	}