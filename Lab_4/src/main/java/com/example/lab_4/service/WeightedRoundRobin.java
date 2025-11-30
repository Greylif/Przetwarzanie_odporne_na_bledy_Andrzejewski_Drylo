package com.example.lab_4.service;

import com.example.lab_4.model.Serwer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeightedRoundRobin {

  private final List<Serwer> serwers = new ArrayList<>();
  private int currentIndex = -1;
  private int currentWeight = 0;

  public WeightedRoundRobin() {
    serwers.add(new Serwer("S1", "http://localhost:8001/backend/handle", 5));
    serwers.add(new Serwer("S2", "http://localhost:8002/backend/handle", 3));
    serwers.add(new Serwer("S3", "http://localhost:8003/backend/handle", 2));
  }

  public synchronized Serwer selectServer() {

    int totalWeight = serwers.stream().mapToInt(Serwer::getWeight).sum();

    while (true) {
      currentIndex = (currentIndex + 1) % serwers.size();

      if (currentIndex == 0) {
        currentWeight--;
        if (currentWeight <= 0) {
          currentWeight = totalWeight;
        }
      }

      if (serwers.get(currentIndex).getWeight() >= currentWeight) {
        return serwers.get(currentIndex);
      }
    }
  }

  public List<Serwer> getServers() {
    return serwers;
  }

  public void updateWeight(String name, int weight) {
    serwers.stream()
        .filter(s -> s.getName().equals(name))
        .findFirst()
        .ifPresent(s -> s.setWeight(weight));
  }
}
