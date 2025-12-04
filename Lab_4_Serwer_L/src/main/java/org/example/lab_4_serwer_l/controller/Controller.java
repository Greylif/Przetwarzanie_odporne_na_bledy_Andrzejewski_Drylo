package org.example.lab_4_serwer_l.controller;

import org.example.lab_4_serwer_l.model.Serwer;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend")
public class Controller {

  private final Serwer state;

  @Autowired
  public Controller(Serwer state) {
    this.state = state;
  }

  @GetMapping("/handle")
  public String handle() {
    state.incrementCounter();
    return "Backend response. Requests served: " + state.getCounter();
  }


  @GetMapping("/status")
  public Map<String, Object> status() {
    return Map.of(
        "servedRequests", state.getCounter()
    );
  }
}
