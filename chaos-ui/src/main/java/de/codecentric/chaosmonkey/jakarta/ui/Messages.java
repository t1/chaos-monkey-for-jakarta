package de.codecentric.chaosmonkey.jakarta.ui;

import com.github.t1.bulmajava.basic.Color;
import com.github.t1.bulmajava.basic.Element;
import com.github.t1.bulmajava.basic.Modifier;
import com.github.t1.bulmajava.basic.Renderable;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.github.t1.bulmajava.basic.Basic.div;
import static com.github.t1.bulmajava.basic.Basic.p;
import static com.github.t1.bulmajava.basic.Color.DANGER;
import static com.github.t1.bulmajava.basic.Color.PRIMARY;
import static com.github.t1.bulmajava.basic.Color.WARNING;
import static com.github.t1.bulmajava.helpers.ColorsHelper.dark;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

@Singleton
public class Messages {
    private static final int MAX = 10_000;
    private static final WebSocketConnections.ListenerName THIS = new WebSocketConnections.ListenerName(Messages.class.getName());

    @Inject
    WebSocketConnections connections;

    private final Queue<Message> messages = new ConcurrentLinkedDeque<>();

    @PostConstruct
    void start() {
        connections.onOpen(THIS, sessionId -> connections.send(sessionId, renderable()));
    }

    public void add(Message message) {
        while (messages.size() > MAX) messages.remove();
        messages.add(message);
        connections.broadcast(renderable());
    }

    private Element renderable() {
        return div().id("event-stream")
                .attr("hx-swap", "innerHTML")
                .content(messages.stream()
                        .skip(Math.max(0, messages.size() - 20))
                        .limit(20)
                        .map(Message::toRenderable));
    }

    public void clear() {
        messages.clear();
        connections.broadcast(renderable());
    }

    public record Message(Instant timestamp, Level level, String text) {
        public Message(Level level, String text) {this(Instant.now(), level, text);}

        @RequiredArgsConstructor @Getter @Accessors(fluent = true)
        public enum Level {
            DEBUG(dark(PRIMARY)), INFO(Color.INFO), WARN(WARNING), ERROR(DANGER);
            private final Modifier color;
        }

        public Renderable toRenderable() {
            return p(formatted(timestamp) + " " + text())
                    .hasText(level.color())
                    .classes("is-family-monospace");
        }

        private static String formatted(Instant timestamp) {
            return LocalTime.ofInstant(timestamp, ZoneId.systemDefault()).format(LOG_TIME);
        }

        // This is like the ISO_LOCAL_TIME, but with fixed-width millis
        private static final DateTimeFormatter LOG_TIME = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(MILLI_OF_SECOND, 3, 3, true)
                .toFormatter();
    }
}
