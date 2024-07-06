package de.codecentric.chaosmonkey.jakarta;

import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class ChaosEventLogger {
    public void log(@Observes ChaosEvent event) {
        log.atLevel(logLevel(event)).log(event.message());
    }

    private static Level logLevel(ChaosEvent event) {
        return switch (event.type()) {
            case ADD, UPDATE -> WARN;
            case APPLY -> ERROR;
        };
    }
}
