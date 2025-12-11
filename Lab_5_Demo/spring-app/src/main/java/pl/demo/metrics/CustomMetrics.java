package pl.demo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wlasne metryki biznesowe dla demonstracji.
 * 
 * W rzeczywistej aplikacji te metryki bylyby aktualizowane
 * przez prawdziwe zdarzenia biznesowe (np. logowanie uzytkownika,
 * zlozenie zamowienia, dodanie zadania do kolejki).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    private final Random random = new Random();
    
    // ===========================================
    // Gauge - wartosci biezace (moga rosnac i malec)
    // ===========================================
    
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger taskQueueSize = new AtomicInteger(0);
    private final AtomicLong externalApiLatencyMs = new AtomicLong(0);
    private double cacheHitRatio = 0.0;
    
    // ===========================================
    // Counter - zliczaja zdarzenia (tylko rosna)
    // ===========================================
    
    private Counter ordersCreatedCounter;
    private Counter ordersCompletedCounter;
    private Counter ordersCancelledCounter;
    private Counter ordersFailedCounter;
    
    // ===========================================
    // Timer - mierzy czas wykonania operacji
    // ===========================================
    
    private Timer orderProcessingTimer;
    
    /**
     * Inicjalizacja metryk przy starcie aplikacji.
     * Rejestrujemy wszystkie metryki w rejestrze Micrometer.
     */
    @PostConstruct
    public void init() {
        // Gauge dla aktywnych uzytkownikow
        Gauge.builder("app.active.users", activeUsers, AtomicInteger::get)
                .description("Liczba aktualnie aktywnych uzytkownikow")
                .tag("type", "business")
                .register(meterRegistry);
        
        // Gauge dla rozmiaru kolejki zadan
        Gauge.builder("app.task.queue.size", taskQueueSize, AtomicInteger::get)
                .description("Aktualny rozmiar kolejki zadan do przetworzenia")
                .tag("type", "business")
                .register(meterRegistry);
        
        // Gauge dla opoznienia zewnetrznego API (w sekundach)
        Gauge.builder("app.external.api.latency.seconds", this, 
                      metrics -> externalApiLatencyMs.get() / 1000.0)
                .description("Aktualne opoznienie wywolan zewnetrznego API w sekundach")
                .tag("api", "external")
                .register(meterRegistry);
        
        // Gauge dla wskaznika trafien cache (0.0 - 1.0)
        Gauge.builder("app.cache.hit.ratio", this, metrics -> cacheHitRatio)
                .description("Wskaznik trafien cache (0.0 - 1.0)")
                .tag("cache", "main")
                .register(meterRegistry);
        
        // Countery dla zamowien z roznymi statusami
        ordersCreatedCounter = Counter.builder("app.orders.total")
                .description("Calkowita liczba zamowien")
                .tag("status", "created")
                .register(meterRegistry);
        
        ordersCompletedCounter = Counter.builder("app.orders.total")
                .description("Calkowita liczba zamowien")
                .tag("status", "completed")
                .register(meterRegistry);
        
        ordersCancelledCounter = Counter.builder("app.orders.total")
                .description("Calkowita liczba zamowien")
                .tag("status", "cancelled")
                .register(meterRegistry);
        
        ordersFailedCounter = Counter.builder("app.orders.total")
                .description("Calkowita liczba zamowien")
                .tag("status", "failed")
                .register(meterRegistry);
        
        // Timer dla przetwarzania zamowien z percentylami
        orderProcessingTimer = Timer.builder("app.order.processing.duration")
                .description("Czas przetwarzania zamowienia")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);
        
        log.info("Wlasne metryki zostaly zainicjalizowane pomyslnie");
    }
    
    // ===========================================
    // Symulacja zdarzen biznesowych (co sekunde)
    // ===========================================
    
    /**
     * Symuluje zmiany w metrykach biznesowych.
     * Uruchamiane automatycznie co 1 sekunde.
     */
    @Scheduled(fixedRate = 1000)
    public void simulateBusinessEvents() {
        // Symulacja aktywnych uzytkownikow (50-200, z malymi wahaniami)
        int currentUsers = activeUsers.get();
        int change = random.nextInt(21) - 10; // -10 do +10
        int newUsers = Math.max(50, Math.min(200, currentUsers + change));
        activeUsers.set(newUsers);
        
        // Symulacja kolejki zadan (0-150, z wahaniami)
        int currentQueue = taskQueueSize.get();
        int queueChange = random.nextInt(11) - 5; // -5 do +5
        int newQueue = Math.max(0, Math.min(150, currentQueue + queueChange));
        taskQueueSize.set(newQueue);
        
        // Symulacja opoznienia zewnetrznego API (50-500ms)
        long latency = 50 + random.nextInt(450);
        externalApiLatencyMs.set(latency);
        
        // Symulacja wskaznika trafien cache (70-95%)
        cacheHitRatio = 0.7 + random.nextDouble() * 0.25;
    }
    
    /**
     * Symuluje tworzenie i przetwarzanie zamowien.
     * Uruchamiane automatycznie co 2 sekundy.
     */
    @Scheduled(fixedRate = 2000)
    public void simulateOrders() {
        // Symulacja tworzenia zamowien (1-5 na raz)
        int ordersToCreate = random.nextInt(5) + 1;
        for (int i = 0; i < ordersToCreate; i++) {
            ordersCreatedCounter.increment();
            
            // Symulacja czasu przetwarzania (100-1000ms)
            long processingTime = 100 + random.nextInt(900);
            orderProcessingTimer.record(Duration.ofMillis(processingTime));
        }
        
        // Symulacja ukonczonych zamowien (wiekszosc)
        int ordersToComplete = random.nextInt(4) + 1;
        for (int i = 0; i < ordersToComplete; i++) {
            ordersCompletedCounter.increment();
        }
        
        // Okazjonalnie anulowane zamowienie (10% szans)
        if (random.nextDouble() < 0.1) {
            ordersCancelledCounter.increment();
        }
        
        // Rzadko - nieudane zamowienie (2% szans)
        if (random.nextDouble() < 0.02) {
            ordersFailedCounter.increment();
        }
    }
    
    // ===========================================
    // Publiczne metody do recznej aktualizacji metryk
    // (uzywane przez kontroler REST)
    // ===========================================
    
    public void incrementActiveUsers(int delta) {
        activeUsers.addAndGet(delta);
    }
    
    public void setActiveUsers(int count) {
        activeUsers.set(count);
    }
    
    public void addToTaskQueue(int count) {
        taskQueueSize.addAndGet(count);
    }
    
    public void removeFromTaskQueue(int count) {
        taskQueueSize.addAndGet(-count);
    }
    
    public void recordOrderCreated() {
        ordersCreatedCounter.increment();
    }
    
    public void recordOrderCompleted() {
        ordersCompletedCounter.increment();
    }
    
    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTimer.record(duration);
    }
    
    public void updateExternalApiLatency(long latencyMs) {
        externalApiLatencyMs.set(latencyMs);
    }
    
    public void updateCacheHitRatio(double ratio) {
        this.cacheHitRatio = ratio;
    }
    
    // Gettery dla testow i diagnostyki
    public int getActiveUsersCount() {
        return activeUsers.get();
    }
    
    public int getTaskQueueSize() {
        return taskQueueSize.get();
    }
}
