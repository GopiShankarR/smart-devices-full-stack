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
import java.util.Date;
import java.util.Random;
import java.util.Calendar;

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
import com.google.gson.JsonElement;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;

@WebServlet(urlPatterns = "/checkout", name = "CheckoutServlet")
public class CheckoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private String getCheckoutFilePath() {
    String TOMCAT_HOME = System.getProperty("catalina.home");
    return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "Orders.txt";
  }

    private String getCartFilePath() {
      String TOMCAT_HOME = System.getProperty("catalina.home");
      return TOMCAT_HOME + File.separator + "webapps" + File.separator + "backend" + File.separator + "cartData.txt";
    }

  @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		System.out.println("Inside Post method!");
		HttpSession session = request.getSession();
		String username = request.getParameter("username");
		session.setAttribute("username", username);

		if (username == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
		}

		String confirmationNumber = generateConfirmationNumber();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, 2);
		Date deliveryDate = calendar.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		String deliveryDateString = dateFormat.format(deliveryDate);

		BufferedReader reader = request.getReader();

		JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
		JsonArray itemsArray = jsonObject.getAsJsonArray("cartItems");
		String deliveryOption = jsonObject.get("deliveryOption").getAsString();
		JsonObject addressObject = jsonObject.get("address").getAsJsonObject();
		String orderPlacedDate = jsonObject.get("orderPlacedDate").getAsString();
		String status = "orderPlaced";

		saveOrder(username, confirmationNumber, deliveryDateString, itemsArray, deliveryOption, addressObject, orderPlacedDate, status);

		removeItemsFromCart(username, itemsArray);

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("confirmationNumber", confirmationNumber);
		jsonResponse.addProperty("deliveryDate", deliveryDateString);
		System.out.println("jsonResponse---------->" + jsonResponse.toString());
		response.getWriter().write(jsonResponse.toString());
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
			String filePath = getCheckoutFilePath();

			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
					String line;
					while ((line = br.readLine()) != null) {
							String[] parts = line.split(",");
							if (parts.length == 5 && parts[0].equals(username)) {
									orders.add(new OrderInfo(parts[0], parts[1], parts[2], parts[4], parts[3]));
							}
					}
			} catch (IOException e) {
					e.printStackTrace();
			}

			JsonArray jsonArray = new JsonArray();
			for (OrderInfo order : orders) {
					JsonObject jsonItem = new JsonObject();
					jsonItem.addProperty("username", order.getUsername());
					jsonItem.addProperty("confirmationNumber", order.getConfirmationNumber());
					jsonItem.addProperty("deliveryDate", order.getDeliveryDate());
					jsonItem.addProperty("orderPlacedDate", order.getOrderPlacedDate());
					jsonItem.addProperty("status", order.getStatus());
					jsonArray.add(jsonItem);
			}

			response.getWriter().write(jsonArray.toString());
		}


    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.addHeader("Access-Control-Max-Age", "3600");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String generateConfirmationNumber() {
      String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      int CONFIRMATION_NUMBER_LENGTH = 10;
      SecureRandom random = new SecureRandom();
      StringBuilder sb = new StringBuilder(CONFIRMATION_NUMBER_LENGTH);
      for (int i = 0; i < CONFIRMATION_NUMBER_LENGTH; i++) {
          int index = random.nextInt(CHARACTERS.length());
          sb.append(CHARACTERS.charAt(index));
      }
      return sb.toString();
    }

    private void saveOrder(String username, String confirmationNumber, String deliveryDate, JsonArray itemsArray, String deliveryOption, JsonObject addressObject, String orderPlacedDate, String status) {
			String filePath = getCheckoutFilePath();
			StringBuilder itemsString = new StringBuilder();

			for (int i = 0; i < itemsArray.size(); i++) {
					JsonObject item = itemsArray.get(i).getAsJsonObject();
					itemsString.append(item.get("id").getAsString()).append(",");
					itemsString.append(item.get("name").getAsString()).append(",");
					itemsString.append(item.get("image").getAsString()).append(",");
					itemsString.append(item.get("price").getAsDouble());
					if (i < itemsArray.size() - 1) {
							itemsString.append("|");
					}
			}

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
					bw.write(username + "," + confirmationNumber + "," + deliveryDate + "," + deliveryOption + "," + orderPlacedDate + "," + status + "|" + itemsString.toString());
					bw.newLine();
			} catch (IOException e) {
					e.printStackTrace();
			}
    }

    private void removeItemsFromCart(String username, JsonArray itemsArray) {
      String cartFilePath = getCartFilePath();
      List<String> updatedLines = new ArrayList<>();

      try (BufferedReader br = new BufferedReader(new FileReader(cartFilePath))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] itemData = line.split(",");
              boolean isPurchasedItem = false;
              for (int i = 0; i < itemsArray.size(); i++) {
                  JsonObject item = itemsArray.get(i).getAsJsonObject();
                  if (itemData[0].equals(username) && itemData[1].equals(item.get("id").getAsString())) {
                      isPurchasedItem = true;
                      break;
                  }
              }
              if (!isPurchasedItem) {
                  updatedLines.add(line);
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(cartFilePath))) {
          for (String updatedLine : updatedLines) {
              bw.write(updatedLine);
              bw.newLine();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
}