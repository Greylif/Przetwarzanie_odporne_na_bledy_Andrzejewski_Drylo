package pl.demo.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.demo.metrics.CustomMetrics;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Kontroler REST z przykladowymi endpointami.
 * 
 * Kazdy endpoint automatycznie generuje metryki HTTP dzieki
 * integracji Spring Boot Actuator + Micrometer.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DemoController {

    private final CustomMetrics customMetrics;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();

    /**
     * Prosty endpoint sprawdzajacy stan aplikacji.
     * Zwraca podstawowe informacje o systemie.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("aktywniUzytkownicy", customMetrics.getActiveUsersCount());
        response.put("rozmiarKolejki", customMetrics.getTaskQueueSize());
        return ResponseEntity.ok(response);
    }

    /**
     * Symulacja tworzenia zamowienia z roznym czasem przetwarzania.
     * Uzywa adnotacji @Timed do automatycznego pomiaru czasu.
     */
    @PostMapping("/orders")
    @Timed(value = "api.orders.create", description = "Czas tworzenia zamowienia")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody(required = false) Map<String, Object> orderData) {
        try {
            // Symulacja przetwarzania (100-800ms)
            long processingTime = 100 + random.nextInt(700);
            Thread.sleep(processingTime);
            
            // Rejestracja metryki
            customMetrics.recordOrderCreated();
            customMetrics.recordOrderProcessingTime(Duration.ofMillis(processingTime));
            
            Map<String, Object> response = new HashMap<>();
            response.put("idZamowienia", "ZAM-" + System.currentTimeMillis());
            response.put("status", "utworzone");
            response.put("czasPrzetwarzaniaMs", processingTime);
            
            log.info("Zamowienie utworzone w {}ms", processingTime);
            return ResponseEntity.ok(response);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint z kontrolowanym opoznieniem (do testowania latency).
     * Parametr delayMs okresla opoznienie w milisekundach.
     */
    @GetMapping("/slow/{delayMs}")
    @Timed(value = "api.slow.endpoint", description = "Celowo wolny endpoint")
    public ResponseEntity<Map<String, Object>> slowEndpoint(@PathVariable long delayMs) {
        try {
            // Limituj maksymalne opoznienie do 5 sekund
            long actualDelay = Math.min(delayMs, 5000);
            Thread.sleep(actualDelay);
            
            Map<String, Object> response = new HashMap<>();
            response.put("zadaneOpoznienie", delayMs);
            response.put("rzeczywisteOpoznienie", actualDelay);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint ktory losowo generuje bledy (do testowania wskaznika bledow).
     * Parametr errorRate okresla prawdopodobienstwo bledu (0.0 - 1.0).
     */
    @GetMapping("/random-error")
    @Timed(value = "api.random.error", description = "Endpoint losowo zwracajacy bledy")
    public ResponseEntity<Map<String, Object>> randomError(
            @RequestParam(defaultValue = "0.2") double errorRate) {
        
        if (random.nextDouble() < errorRate) {
            // Zlicz blad w custom counter
            meterRegistry.counter("api.errors", "endpoint", "random-error").increment();
            log.warn("Wygenerowano losowy blad (wskaznik: {})", errorRate);
            return ResponseEntity.internalServerError()
                    .body(Map.of("blad", "Losowa awaria", "wskaznikBledow", errorRate));
        }
        
        return ResponseEntity.ok(Map.of(
                "sukces", true,
                "wskaznikBledow", errorRate,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Endpoint do symulacji obciazenia cache.
     * Losowo generuje trafienia (hit) lub pudla (miss).
     */
    @GetMapping("/cache-test")
    public ResponseEntity<Map<String, Object>> cacheTest() {
        // Symulacja cache hit/miss (80% trafien)
        boolean cacheHit = random.nextDouble() < 0.8;
        
        meterRegistry.counter("api.cache.operations", 
                "wynik", cacheHit ? "trafienie" : "pudlo").increment();
        
        // Symulacja roznego czasu odpowiedzi
        long responseTime = cacheHit ? 5 : 100 + random.nextInt(200);
        
        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return ResponseEntity.ok(Map.of(
                "trafienieCahe", cacheHit,
                "czasOdpowiedziMs", responseTime
        ));
    }

    /**
     * Endpoint do recznej kontroli metryki aktywnych uzytkownikow.
     * Przydatne do testowania dashboardu.
     */
    @PostMapping("/metrics/users")
    public ResponseEntity<Map<String, Object>> setActiveUsers(@RequestParam int count) {
        customMetrics.setActiveUsers(count);
        return ResponseEntity.ok(Map.of(
                "aktywniUzytkownicy", count,
                "wiadomosc", "Zaktualizowano liczbe aktywnych uzytkownikow"
        ));
    }

    /**
     * Endpoint do dodawania zadan do kolejki.
     * Przydatne do testowania dashboardu.
     */
    @PostMapping("/metrics/queue")
    public ResponseEntity<Map<String, Object>> addToQueue(@RequestParam int count) {
        customMetrics.addToTaskQueue(count);
        return ResponseEntity.ok(Map.of(
                "dodano", count,
                "aktualnyRozmiar", customMetrics.getTaskQueueSize()
        ));
    }

    /**
     * Endpoint generujacy wiele zapytan dla testow obciazeniowych.
     * Parametr requests okresla liczbe symulowanych zapytan.
     */
    @GetMapping("/load-test")
    public ResponseEntity<Map<String, Object>> loadTest(
            @RequestParam(defaultValue = "10") int requests) {
        
        Timer.Sample sample = Timer.start(meterRegistry);
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < Math.min(requests, 100); i++) {
            try {
                // Symulacja przetwarzania
                Thread.sleep(random.nextInt(50));
                
                if (random.nextDouble() < 0.05) { // 5% bledow
                    errorCount++;
                } else {
                    successCount++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errorCount++;
            }
        }
        
        sample.stop(Timer.builder("api.load.test.duration")
                .description("Czas wykonania testu obciazeniowego")
                .register(meterRegistry));
        
        return ResponseEntity.ok(Map.of(
                "calkowitaLiczbaZapytan", requests,
                "sukcesy", successCount,
                "bledy", errorCount,
                "wskaznikSukcesu", (double) successCount / requests
        ));
    }
}
