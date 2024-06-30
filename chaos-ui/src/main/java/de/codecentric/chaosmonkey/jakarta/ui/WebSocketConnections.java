package de.codecentric.chaosmonkey.jakarta.ui;

import com.github.t1.bulmajava.basic.Renderable;
import jakarta.ejb.Singleton;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Singleton
@ServerEndpoint("/connect/{httpSession}")
public class WebSocketConnections {
    private final Map<SessionId, Connection> connections = new ConcurrentHashMap<>();
    private final Map<ListenerName, Consumer<SessionId>> openListeners = new ConcurrentHashMap<>();

    private record Connection(String httpSessionId, Session wsSession) {
        public void send(Renderable renderable) {
            wsSession.getAsyncRemote()
                    .sendObject(renderable.render(), result -> {
                        if (result.isOK()) {
                            log.debug("successfully broadcast to {}: {}", wsSession.getId(), renderable);
                        } else {
                            log.error("failed to broadcast to {}: {}", wsSession.getId(), renderable, result.getException());
                        }
                    });
        }
    }

    public void onOpen(ListenerName name, Consumer<SessionId> consumer) {openListeners.put(name, consumer);}

    public void send(SessionId sessionId, Renderable renderable) {
        connections.get(sessionId).send(renderable);
    }

    public boolean broadcast(String httpSessionId, Renderable renderable) {
        var foundAny = new AtomicBoolean(false);
        connections.values().stream()
                .filter(connection -> connection.httpSessionId.equals(httpSessionId))
                .peek(connection -> foundAny.set(true))
                .forEach(connection -> connection.send(renderable));
        return foundAny.get();
    }

    public void broadcast(Renderable renderable) {
        log.debug("broadcast: {}", renderable);
        connections.values().forEach(connection -> connection.send(renderable));
    }

    @OnOpen
    public void onOpen(Session wsSession, @PathParam("httpSession") String httpSession) {
        var sessionId = SessionId.from(wsSession);
        connections.put(sessionId, new Connection(httpSession, wsSession));
        openListeners.values().forEach(c -> c.accept(sessionId));
        log.info("ws-session {} open (now {}): {}", wsSession.getId(), connections.size(), httpSession);
    }

    @OnClose
    public void onClose(Session wsSession) {
        connections.remove(SessionId.from(wsSession));
        log.info("ws-session {} close (now {})", wsSession.getId(), connections.size());
    }

    @OnError
    public void onError(Session wsSession, Throwable throwable) {
        connections.remove(SessionId.from(wsSession));
        log.info("ws-session {} error (now {}): {}", wsSession.getId(), connections.size(), throwable.getMessage());
    }

    @OnMessage
    public void onMessage(Session wsSession, String message) {
        log.debug("ws-session {} message: {}", wsSession.getId(), message);
    }

    public record SessionId(String value) {
        public static SessionId from(Session wsSession) {return new SessionId(wsSession.getId());}
    }

    public record ListenerName(String value) {}
}
