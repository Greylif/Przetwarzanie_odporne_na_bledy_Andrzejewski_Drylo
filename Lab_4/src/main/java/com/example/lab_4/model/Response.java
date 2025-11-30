package com.example.lab_4.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Response {
  private String name;
  private String url;
  private int weight;
  private int servedByLB;
}
