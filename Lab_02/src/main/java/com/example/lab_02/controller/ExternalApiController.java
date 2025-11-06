package com.example.lab_02.controller;

import com.example.lab_02.model.model;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

  private final RestTemplate restTemplate;

  public ExternalApiController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @RequestMapping("favicon.ico")
  public void favicon() {
    //Pusta funkcja pomijajaca blad zwiazany z pobraniem favicon.ico
  }

  @GetMapping("/posts")
  public ResponseEntity<String> getPosts(
      @RequestParam("paramname") String name,
      @RequestParam("paramvalue") String value,
      @RequestParam("url") String url
  ) {

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam(name, value);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
        builder.toUriString(),
        HttpMethod.GET,
        entity,
        String.class
    );

    System.out.println("External API Response: " + response.getStatusCode());

    return ResponseEntity.ok(response.getBody());
  }
}
