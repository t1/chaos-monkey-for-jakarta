package de.codecentric.chaosmonkey.jakarta;

import jakarta.ejb.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ChaosConfigs {
    private final Locations locations = new Locations();

    public Locations all() {return locations;}

    public RestMethods at(ChaosLocation location) {return locations.at(location);}

    public void reset() {locations.reset();}
}
