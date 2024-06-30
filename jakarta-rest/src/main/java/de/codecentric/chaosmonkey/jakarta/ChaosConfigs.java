package de.codecentric.chaosmonkey.jakarta;

import jakarta.ejb.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ChaosConfigs {
    private final Directions directions = new Directions();

    public Directions all() {return directions;}

    public RestMethods when(ChaosDirection direction) {return directions.at(direction);}

    public void reset() {directions.reset();}
}
