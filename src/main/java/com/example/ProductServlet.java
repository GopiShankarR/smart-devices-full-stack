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

    private String getFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "productData.txt";
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

    List<Product> products = new ArrayList<>();
    String filePath = getFilePath();
    Map<String, Accessory> accessories = getAccessoryData();
    System.out.println("category------>" + category);
    
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] productData = line.split(";");
            if (productData.length < 8) {
                System.err.println("Malformed line: " + line);
                continue;
            }

            String productId = productData[0];
            String name = productData[1];
            double price = Double.parseDouble(productData[2]);
            String description = productData[3];
            String manufacturer = productData[4];
            String imageUrl = productData[5];
            String type = productData[6];
            String aidsString = productData[7];
            
            String[] aidsArray;
            if (aidsString.equals("[]")) {
                aidsArray = new String[0]; 
            } else {
                aidsArray = aidsString.substring(1, aidsString.length() - 1).split(",\\s*");
            }

            if (id != null && id.equals(productId)) {
                Product product = new Product(productId, name, price, description, manufacturer, imageUrl, type, Arrays.asList(aidsArray));
                products.add(product);
                break;
            } else if (category == null || category.equalsIgnoreCase(type)) {
                Product product = new Product(productId, name, price, description, manufacturer, imageUrl, type, Arrays.asList(aidsArray));
                products.add(product);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
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
        jsonProduct.addProperty("type", product.getType());

        JsonArray aidsArray = new JsonArray();
        for (String aid : product.getAids()) {
            Accessory accessory = accessories.get(aid); 
            if (accessory != null) {
                JsonObject jsonAccessory = new JsonObject();
                jsonAccessory.addProperty("aid", accessory.getAid());
                jsonAccessory.addProperty("name", accessory.getName());
                jsonAccessory.addProperty("imageURL", accessory.getImageURL());
                aidsArray.add(jsonAccessory);
            }
        }

        jsonProduct.add("aids", aidsArray);

        jsonArray.add(jsonProduct);
    }
    System.out.println("jsonProductttttttttttttttttttttttttttttt" + jsonArray.toString());

    response.getWriter().write(jsonArray.toString());
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
			String filePath = getFilePath();
			
			if ("delete".equals(action)) {
				String idToDelete = jsonObject.get("id").getAsString();

				List<String> lines = new ArrayList<>();
				try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
					String line;
					while ((line = br.readLine()) != null) {
						if (!line.startsWith(idToDelete + ";")) {
								lines.add(line);
						}
					}
				}

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
					for (String line : lines) {
						bw.write(line);
						bw.newLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if ("update".equals(action)) {
				String id = jsonObject.get("id").getAsString();
				String name = jsonObject.get("name").getAsString();
				double price = jsonObject.get("price").getAsDouble();
				String description = jsonObject.get("description").getAsString();
				String manufacturer = jsonObject.get("manufacturer").getAsString();
				String imageUrl = jsonObject.get("imageUrl").getAsString();
				String type = jsonObject.get("type").getAsString();
				JsonArray aidsArray = jsonObject.has("aids") && !jsonObject.get("aids").isJsonNull() ? jsonObject.getAsJsonArray("aids") : null;
				String aids = aidsArray != null ? aidsArray.toString() : "[]";

				List<String> lines = new ArrayList<>();
				try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
					String line;
					while ((line = br.readLine()) != null) {
						if (line.startsWith(id + ";")) {
								line = id + ";" + name + ";" + price + ";" + description + ";" + manufacturer + ";" + imageUrl + ";" + type + ";" + aids;
						}
						lines.add(line);
					}
				}

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
					for (String line : lines) {
						bw.write(line);
						bw.newLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				String id = String.valueOf(System.currentTimeMillis());
				String name = jsonObject.get("name").getAsString();
				double price = jsonObject.get("price").getAsDouble();
				String description = jsonObject.get("description").getAsString();
				String manufacturer = jsonObject.get("manufacturer").getAsString();
				String imageUrl = jsonObject.get("imageUrl").getAsString();
				String type = jsonObject.get("type").getAsString();

				JsonArray aidsArray = jsonObject.has("aids") && !jsonObject.get("aids").isJsonNull() ? jsonObject.getAsJsonArray("aids") : null;
				String aids = aidsArray != null ? aidsArray.toString() : "[]";

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
						bw.write(id + ";" + name + ";" + price + ";" + description + ";" + manufacturer + ";" + imageUrl + ";" + type + ";" + aids);
						bw.newLine();
				} catch (IOException e) {
						e.printStackTrace();
				}
			}

			JsonObject jsonResponse = new JsonObject();
			jsonResponse.addProperty("success", true);
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

    private Map<String, Accessory> getAccessoryData() throws IOException {
        Map<String, Accessory> accessories = new HashMap<>();
        String accessoryFilePath = getAccessoryFilePath(); 
        try (BufferedReader br = new BufferedReader(new FileReader(accessoryFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] accessoryData = line.split(",");
                String aid = accessoryData[0];
                String name = accessoryData[1];
                String imageURL = accessoryData[2];
                accessories.put(aid, new Accessory(aid, name, imageURL)); 
            }
        }
        return accessories;
    }

    private String getAccessoryFilePath() {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "accessory.txt";
    }
	}
