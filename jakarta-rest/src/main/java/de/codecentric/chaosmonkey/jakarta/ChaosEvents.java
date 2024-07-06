package de.codecentric.chaosmonkey.jakarta;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@Startup @Singleton
public class ChaosEvents {
    private static ChaosEvents instance;

    public static void send(ChaosEvent.Type type, String message, Object... args) {
        instance.doSend(new ChaosEvent(type, String.format(message, args)));
    }

    @Inject
    Event<ChaosEvent> sender;

    public ChaosEvents() {ChaosEvents.instance = this;}

    private void doSend(ChaosEvent event) {sender.fire(event);}
}
