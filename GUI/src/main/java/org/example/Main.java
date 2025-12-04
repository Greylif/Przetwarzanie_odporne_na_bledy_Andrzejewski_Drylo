package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main extends Application {

  private TextArea outputArea;
  private TextField weightNameField;
  private TextField weightValueField;

  private TextField autoCountField;
  private TextField autoDelayField;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Load Balancer");

    VBox root = new VBox(15);
    root.setPadding(new Insets(20));
    root.setAlignment(Pos.TOP_CENTER);

    Button btnRequest = new Button("Request");
    Button btnStatus = new Button("Status");
    Button btnUpdateWeight = new Button("Aktualizacja Wagi");
    btnRequest.setPrefWidth(200);
    btnStatus.setPrefWidth(200);
    btnUpdateWeight.setPrefWidth(200);

    weightNameField = new TextField("S1");
    weightValueField = new TextField("10");
    weightNameField.setPromptText("Nazwa serwera");
    weightValueField.setPromptText("Waga");

    HBox weightBox = new HBox(10, weightNameField, weightValueField, btnUpdateWeight);
    weightBox.setAlignment(Pos.CENTER);

    Label autoLabel = new Label("Requesty:");

    autoCountField = new TextField("100");
    autoDelayField = new TextField("10");

    autoCountField.setPromptText("Ilosc requestow");
    autoDelayField.setPromptText("Opoznienie");

    Button btnAuto = new Button("Wyslij N requestow");
    btnAuto.setPrefWidth(200);

    HBox autoBox = new HBox(10, autoCountField, autoDelayField, btnAuto);
    autoBox.setAlignment(Pos.CENTER);

    outputArea = new TextArea();
    outputArea.setPrefHeight(300);
    outputArea.setEditable(false);

    root.getChildren().addAll(
        btnRequest,
        btnStatus,
        weightBox,
        autoLabel,
        autoBox,
        outputArea
    );

    btnRequest.setOnAction(e -> {
      String response = sendGet("http://localhost:8000/lb/request");
      outputArea.appendText("\n/lb/request: " + response + "\n");
    });

    btnStatus.setOnAction(e -> {
      String response = sendGet("http://localhost:8000/lb/status");
      outputArea.appendText("\n/lb/status: " + response + "\n");
    });

    btnUpdateWeight.setOnAction(e -> {
      String name = weightNameField.getText().trim();
      String value = weightValueField.getText().trim();
      String url = "http://localhost:8000/lb/weight/" + name + "/" + value;
      String response = sendPost(url);
      outputArea.appendText("\n/lb/weight: " + response + "\n");
    });

    btnAuto.setOnAction(e -> startAutoRequests());

    primaryStage.setScene(new Scene(root, 600, 500));
    primaryStage.show();
  }

  private void startAutoRequests() {
    int count = Integer.parseInt(autoCountField.getText().trim());
    int delay = Integer.parseInt(autoDelayField.getText().trim());

    Thread thread = new Thread(() -> {
      for (int i = 1; i <= count; i++) {
        String response = sendGet("http://localhost:8000/lb/request");

        int finalI = i;
        Platform.runLater(() ->
            outputArea.appendText(finalI + ": " + response + "\n")
        );

        try {
          Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  private String sendGet(String urlStr) {
    try {
      URL url = new URL(urlStr);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = in.readLine()) != null) sb.append(line);
      in.close();

      return sb.toString();

    } catch (Exception e) {
      return "ERROR: " + e.getMessage();
    }
  }

  private String sendPost(String urlStr) {
    try {
      URL url = new URL(urlStr);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.getOutputStream().write(0);

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = in.readLine()) != null) sb.append(line);
      in.close();

      return sb.toString();

    } catch (Exception e) {
      return "ERROR: " + e.getMessage();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
