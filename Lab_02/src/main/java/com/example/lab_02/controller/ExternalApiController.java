package com.example.lab_02.controller;

import com.example.lab_02.model.model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

  @Autowired
  private RestTemplate restTemplate;

  @RequestMapping("favicon.ico")
  public void favicon() {
  }

  @GetMapping("/posts")
  public ResponseEntity<model[]> getPosts(@RequestParam("userId") String userId) {

    String url = "https://jsonplaceholder.typicode.com/posts";

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("userId", userId);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<model[]> response = restTemplate.exchange(
        builder.toUriString(),
        HttpMethod.GET,
        entity,
        model[].class
    );

    System.out.println("External API Response: " + response.getStatusCode());
    return response;
  }
}
