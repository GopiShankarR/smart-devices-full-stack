package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.io.BufferedReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns ="/signup", name="SignupServlet")
public class SignupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      String error_message = "";

      BufferedReader reader = request.getReader();
      JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
      String username = jsonObject.get("username").getAsString();
      String password = jsonObject.get("password").getAsString();
      String userType = jsonObject.get("userType").getAsString();
      System.out.println(username + " " + password + " " + userType);

      String TOMCAT_HOME = System.getProperty("catalina.home");
      System.out.println("TOMCAT_HOME: " + TOMCAT_HOME);
      HashMap<String, UserInfo> userMap = new HashMap<String, UserInfo>();
      try
      {
        FileInputStream fileInputStream = new FileInputStream(new File(TOMCAT_HOME+"\\webapps\\backend\\UserInfo.txt"));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);	      
        userMap = (HashMap)objectInputStream.readObject();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      System.out.println("Entire path:" + TOMCAT_HOME + "\\webapps\\backend\\UserInfo.txt");
      
      boolean isSaved = false;
      if(userMap.containsKey(username)) {
        error_message = "Username already exist as " + userType;
      } else {
        try {
          UserInfo userInfo = new UserInfo(username, password, userType);
          userMap.put(username, userInfo);
          FileOutputStream fileOutputStream = new FileOutputStream(TOMCAT_HOME+"\\webapps\\backend\\UserInfo.txt");
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
          objectOutputStream.writeObject(userMap);
          isSaved = true;
          objectOutputStream.flush();
          objectOutputStream.close();       
          fileOutputStream.close();
        } catch(Exception e) {
          System.out.println(e);
        }
      }
      JsonObject jsonResponse = new JsonObject();
      if (isSaved) {
          jsonResponse.addProperty("success", true);
          jsonResponse.addProperty("userType", userType); 
          jsonResponse.addProperty("redirect", getRedirectPath(userType));
      } else {
          jsonResponse.addProperty("success", false);
          jsonResponse.addProperty("error", "Failed to save user");
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

    private String getRedirectPath(String userType) {
      System.out.println("INSIDE SIGNUPSERVLET------->" + userType);
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
}