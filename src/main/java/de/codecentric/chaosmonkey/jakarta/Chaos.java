package de.codecentric.chaosmonkey.jakarta;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.Response.StatusType;

@Data @NoArgsConstructor @AllArgsConstructor @Builder @With @Accessors(chain = true)
@Slf4j
public class Chaos {
    private Integer failureCount;
    private Integer statusCode;
    private Long delay;

    public boolean active() {return failureCount != null && failureCount > 0 && (statusCode != null || delay != null);}

    /** Convenience over the raw {@link #statusCode} */
    @JsonbTransient
    public StatusType getStatusCodeType() {return (statusCode == null) ? null : OtherStatusType.statusFromCode(statusCode);}

    @SuppressWarnings("unused") public static class ChaosBuilder {
        public ChaosBuilder statusCode(StatusType failureStatus) {return statusCode(failureStatus.getStatusCode());}

        public ChaosBuilder statusCode(int statusCode) {this.statusCode = statusCode;return this;}


        public ChaosBuilder delay(Duration duration) {return delay(duration.toMillis());}

        public ChaosBuilder delay(long duration) {this.delay = duration;return this;}
    }

    public Chaos merge(Chaos patch) {
        if (patch.failureCount != null) this.setFailureCount(patch.getFailureCount());
        if (patch.statusCode != null) this.setStatusCode(patch.getStatusCode());
        if (patch.delay != null) this.setDelay(patch.getDelay());
        return this;
    }


    public void apply(Consumer<Response> consumer) {
        if (countDownFailure()) {
            if (delay != null) applyDelay();
            if (statusCode != null) applyStatusCode(consumer);
        }
    }

    private boolean countDownFailure() {
        if (!active()) return false;
        --failureCount;
        return true;
    }

    private void applyDelay() {
        log.error("applying chaos: delay {} ms", delay);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyStatusCode(Consumer<Response> consumer) {
        var failureStatus = getStatusCodeType();
        log.error("applying chaos: fail with {} {}", failureStatus.getStatusCode(), failureStatus.getReasonPhrase());
        consumer.accept(Response.status(failureStatus).build());
    }
}
