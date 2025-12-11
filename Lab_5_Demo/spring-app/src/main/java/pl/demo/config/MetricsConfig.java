package pl.demo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Konfiguracja Micrometer dla metryk.
 * 
 * Ta klasa zawiera beany potrzebne do:
 * - Dodawania wspolnych tagow do wszystkich metryk
 * - Wlaczenia obslugi adnotacji @Timed
 * - Filtrowania niechcianych metryk
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizer dodajacy wspolne tagi do wszystkich metryk.
     * Przydatne gdy mamy wiele instancji aplikacji - mozemy
     * filtrowac metryki po nazwie aplikacji i srodowisku.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(List.of(
                        Tag.of("aplikacja", "monitoring-demo"),
                        Tag.of("srodowisko", getEnvironment())
                ));
    }

    /**
     * Aspect do obslugi adnotacji @Timed na metodach.
     * WAZNE: Bez tego beana adnotacja @Timed nie dziala!
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Filter do ignorowania niektorych metryk (opcjonalne).
     * Mozna uzyc do redukcji ilosci zbieranych metryk
     * jesli mamy ich za duzo.
     */
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id -> {
            String name = id.getName();
            // Przyklad: ignoruj metryki logbacka jesli nie sa potrzebne
            // return name.startsWith("logback");
            return false;
        });
    }

    /**
     * Pobiera nazwe srodowiska ze zmiennej srodowiskowej.
     * Domyslnie zwraca "local" jesli nie ustawiono profilu.
     */
    private String getEnvironment() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        return env != null ? env : "local";
    }
}
