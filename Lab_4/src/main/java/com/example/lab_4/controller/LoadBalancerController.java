package com.example.lab_4.controller;

import com.example.lab_4.model.Response;
import com.example.lab_4.model.Serwer;
import com.example.lab_4.service.WeightedRoundRobin;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/lb")
public class LoadBalancerController {

  private final WeightedRoundRobin wrr;
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  public LoadBalancerController(WeightedRoundRobin wrr) {
    this.wrr = wrr;
  }

  @GetMapping("/request")
  public String forwardRequest() {
    Serwer serwer = wrr.selectServer();
    serwer.incrementCounter();

    return restTemplate.getForObject(serwer.getUrl(), String.class);
  }

  @GetMapping("/status")
  public List<Response> status() {
    return wrr.getServers().stream()
        .map(s -> new Response(
            s.getName(),
            s.getUrl(),
            s.getWeight(),
            s.getCounter()
        )).toList();
  }


  @PostMapping("/weight/{name}/{value}")
  public String updateWeight(@PathVariable String name, @PathVariable int value) {
    wrr.updateWeight(name, value);
    return "Updated weight of " + name + " to " + value;
  }
}
