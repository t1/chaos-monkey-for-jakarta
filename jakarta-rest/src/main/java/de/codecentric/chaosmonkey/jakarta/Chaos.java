package de.codecentric.chaosmonkey.jakarta;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.function.Consumer;

import static de.codecentric.chaosmonkey.jakarta.ChaosEvent.Type.APPLY;
import static jakarta.ws.rs.core.Response.StatusType;

@Data @NoArgsConstructor @AllArgsConstructor @Builder @With @Accessors(chain = true)
public class Chaos {
    private volatile Integer failureCount;
    private volatile Integer statusCode;
    private volatile Integer delay;

    public boolean active() {return failureCount() > 0 && (statusCode != null || delay != null);}

    private int failureCount() {return (failureCount == null) ? 0 : failureCount;}

    private int statusCode() {return (statusCode == null) ? 0 : statusCode;}

    private int delay() {return (delay == null) ? 0 : delay;}

    /** Convenience over the raw {@link #statusCode} */
    @JsonbTransient
    public StatusType getStatusCodeType() {return (statusCode == null) ? null : OtherStatusType.statusFromCode(statusCode());}

    @SuppressWarnings("unused") public static class ChaosBuilder {
        public ChaosBuilder statusCode(StatusType failureStatus) {return statusCode(failureStatus.getStatusCode());}

        public ChaosBuilder statusCode(int statusCode) {this.statusCode = statusCode; return this;}


        public ChaosBuilder delay(Duration duration) {return delay((int) duration.toMillis());}

        public ChaosBuilder delay(int delay) {this.delay = delay; return this;}
    }

    public Chaos merge(Chaos patch) {
        if (patch.failureCount != null) this.setFailureCount(patch.getFailureCount());
        if (patch.statusCode != null) this.setStatusCode(patch.getStatusCode());
        if (patch.delay != null) this.setDelay(patch.getDelay());
        return this;
    }


    public void apply(Consumer<Response> abort) {
        if (nextFailure()) {
            // the order of these is relevant
            if (delay != null) applyDelay();
            if (statusCode != null) applyStatusCode(abort);
        }
    }

    private boolean nextFailure() {
        if (!active()) return false;
        synchronized (this) {
            --failureCount;
        }
        return true;
    }

    private void applyDelay() {
        ChaosEvents.send(APPLY, "applying chaos: delay %d ms", delay());
        try {
            Thread.sleep(delay());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyStatusCode(Consumer<Response> abort) {
        var failureStatus = getStatusCodeType();
        ChaosEvents.send(APPLY, "applying chaos: fail with %d %s", failureStatus.getStatusCode(), failureStatus.getReasonPhrase());
        abort.accept(Response.status(failureStatus).build());
    }

}
