package com.example.lab_01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testy Lab1")
class Zad3Tests {

  @Test
  @DisplayName("Testy dodawania")
  void testAdd() {
    Zad3 calculator = new Zad3();

    int result = calculator.add(5, 3);
    assertEquals(8, result, "Metoda add powinna zwrocic poprawna sume liczb.");
  }
}

