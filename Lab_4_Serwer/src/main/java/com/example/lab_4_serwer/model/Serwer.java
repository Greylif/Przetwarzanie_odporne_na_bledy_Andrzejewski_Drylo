package com.example.lab_4_serwer.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class Serwer {

  @Getter
  @Setter
  private int counter = 0;


  public void incrementCounter() { counter++; }
}