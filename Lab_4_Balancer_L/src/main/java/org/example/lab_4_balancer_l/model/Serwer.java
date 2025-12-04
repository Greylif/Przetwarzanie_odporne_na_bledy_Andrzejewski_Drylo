package org.example.lab_4_balancer_l.model;

import lombok.Getter;
import lombok.Setter;


public class Serwer {

  @Getter
  @Setter
  private String name;
  @Getter
  @Setter
  private String url;
  @Getter
  @Setter
  private int weight;
  @Getter
  @Setter
  private int counter;

  public Serwer(String name, String url, int weight) {
    this.name = name;
    this.url = url;
    this.weight = weight;
    this.counter = 0;
  }

  public void incrementCounter() { counter++; }
}
