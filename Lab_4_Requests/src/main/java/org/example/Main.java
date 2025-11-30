package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

  public static void main(String[] args) throws Exception {

    String targetUrl = "http://localhost:8000/lb/request";
    int requests = 1000;
    int delayMs = 10;

    for (int i = 1; i <= requests; i++) {
      String response = sendGet(targetUrl);
      System.out.println(i + ": " + response);

        Thread.sleep(delayMs);
    }
  }

  private static String sendGet(String url) throws IOException {

    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    con.setRequestMethod("GET");

    int responseCode = con.getResponseCode();

    if (responseCode != 200) {
      return "Error: HTTP code " + responseCode;
    }

    BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));

    String inputLine;
    StringBuilder response = new StringBuilder();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }

    in.close();
    return response.toString();
  }
}
