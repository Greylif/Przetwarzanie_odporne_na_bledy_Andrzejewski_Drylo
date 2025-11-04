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
  public void favicon() {}

  @GetMapping("/posts")
  public ResponseEntity<String> getPosts(
      @RequestParam("userId") String userId,
      @RequestParam("url") String url
  ) {

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

    model[] posts = response.getBody();

    StringBuilder Bodies = new StringBuilder();
    for (model post : posts) {
      Bodies.append(post.getId()).append("\n");
      Bodies.append(post.getBody()).append("\n\n");
    }

    return ResponseEntity.ok(Bodies.toString());
  }
}
