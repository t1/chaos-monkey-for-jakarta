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

import java.util.function.Consumer;

import static jakarta.ws.rs.core.Response.Status.BAD_GATEWAY;
import static jakarta.ws.rs.core.Response.StatusType;

@Data @NoArgsConstructor @AllArgsConstructor @Builder @With @Accessors(chain = true)
@Slf4j
public class ChaosConfig {
    private Integer failureCount;
    private Integer statusCode;

    public boolean active() {return failureCount != null && failureCount > 0;}

    /** Convenience over the raw {@link #statusCode} */
    @JsonbTransient
    public StatusType getStatusCodeType() {return (statusCode == null) ? BAD_GATEWAY : statusFromCode(statusCode);}

    @SuppressWarnings("unused") public static class ChaosConfigBuilder {
        public ChaosConfigBuilder statusCode(StatusType failureStatus) {return statusCode(failureStatus.getStatusCode());}

        public ChaosConfigBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
    }

    public ChaosConfig merge(ChaosConfig that) {
        if (that.failureCount != null) this.setFailureCount(that.getFailureCount());
        if (that.statusCode != null) this.setStatusCode(that.getStatusCode());
        return this;
    }


    public void apply(Consumer<Response> consumer) {
        if (countDownFailure()) {
            var failureStatus = getStatusCodeType();
            log.error("applying chaos: fail with {} {}", failureStatus.getStatusCode(), failureStatus.getReasonPhrase());
            consumer.accept(Response.status(failureStatus).build());
        }
    }

    private boolean countDownFailure() {
        if (!active()) return false;
        --failureCount;
        return true;
    }


    private static StatusType statusFromCode(int statusCode) {
        var status = (StatusType) Response.Status.fromStatusCode(statusCode);
        if (status == null) status = new OtherStatusType(statusCode);
        return status;
    }

    private static class OtherStatusType implements StatusType {
        private final int statusCode;

        public OtherStatusType(int statusCode) {this.statusCode = statusCode;}

        @Override public int getStatusCode() {return statusCode;}

        @Override public Response.Status.Family getFamily() {return Response.Status.Family.familyOf(statusCode);}

        @Override public String getReasonPhrase() {return "Other Status Code";}

        @Override public String toString() {return "OTHER:" + statusCode;}
    }
}
