package de.codecentric.chaosmonkey.jakarta.ui;

import de.codecentric.chaosmonkey.jakarta.ChaosEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import static de.codecentric.chaosmonkey.jakarta.ui.Messages.Message;
import static de.codecentric.chaosmonkey.jakarta.ui.Messages.Message.Level.ERROR;
import static de.codecentric.chaosmonkey.jakarta.ui.Messages.Message.Level.WARN;

public class ChaosEventMessages {
    @Inject Messages messages;

    public void log(@Observes ChaosEvent event) {
        messages.add(new Message(levelFor(event.type()), event.type() + " " + event.message()));
    }

    private Message.Level levelFor(ChaosEvent.Type type) {
        return switch (type) {
            case ADD, UPDATE -> WARN;
            case APPLY -> ERROR;
        };
    }
}
