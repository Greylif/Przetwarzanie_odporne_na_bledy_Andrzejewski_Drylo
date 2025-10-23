package com.example.lab_01;

import java.util.logging.Logger;

public class Zad3 {


  private static final Logger LOGGER = Logger.getLogger(Zad3.class.getName());

  public static int add(int a, int b) {
    return a + b;
  }

  public static void main(String[] args){
    String sum = String.valueOf(add(1,4));
    LOGGER.info(sum);
  }
}
