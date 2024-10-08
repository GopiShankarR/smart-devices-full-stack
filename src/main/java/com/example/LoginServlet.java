package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime; 
import java.util.HashMap;
import java.io.BufferedReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns ="/login", name="LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
      LocalDateTime now = LocalDateTime.now();  
      System.out.println(dtf.format(now)); 

      BufferedReader reader = request.getReader();
      JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();

      String username = jsonObject.get("username").getAsString();
      String password = jsonObject.get("password").getAsString();
      String userType = jsonObject.get("userType").getAsString();

      String redirectPath = authenticateUser(username, password, userType);

      JsonObject jsonResponse = new JsonObject();
      if (redirectPath.equals("/login")) {
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("redirect", redirectPath);
      } else {
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("redirect", redirectPath);
      }
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

    private String authenticateUser(String username, String password, String userType) {
        String TOMCAT_HOME = System.getProperty("catalina.home");
        HashMap<String, UserInfo> userMap = new HashMap<String, UserInfo>();
        try{
          FileInputStream fileInputStream = new FileInputStream(new File(TOMCAT_HOME + "\\webapps\\backend\\UserInfo.txt"));
          ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
          userMap = (HashMap)objectInputStream.readObject();
        } catch(Exception e) {
          System.out.println(e);
        }
        UserInfo userInfo = userMap.get(username);
        System.out.println("INSIDE LOGINSERVLET------->" + userType);
        if(userInfo.getPassword().equals(password) && userInfo.getUsertype().equals(userType)) {
          switch (userType) {
            case "Customer":
                return "/home";
            case "Store Manager":
                return "/store-manager";
            case "Salesman":
                return "/salesman";
            default:
                return "/login";
          }
        }
        return "/login";
    }
}