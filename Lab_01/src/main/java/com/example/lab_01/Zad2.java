package com.example.lab_01;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class Zad2 {

  private static final Logger LOGGER = Logger.getLogger(Zad2.class.getName());

  public static void main(String[] args) {

    File file = new File("Zad2.txt");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String currentDateTime = LocalDateTime.now().format(formatter);
    String content = "Aktualna data i godzina: " + currentDateTime + System.lineSeparator();

    try (FileWriter writer = new FileWriter(file, false)) {
      writer.write(content);
      LOGGER.info("Plik został zapisany pomyślnie: " + file.getAbsolutePath());
    } catch (IOException e) {
      LOGGER.severe("Błąd podczas zapisu pliku: " + e.getMessage());
      return;
    }

    try (FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader)) {

      String line;
      LOGGER.info("Zawartość pliku:");
      while ((line = bufferedReader.readLine()) != null) {
        LOGGER.info(line);
      }

    } catch (IOException e) {
      LOGGER.severe("Błąd podczas odczytu pliku: " + e.getMessage());
    }
  }
}
