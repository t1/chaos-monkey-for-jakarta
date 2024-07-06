package de.codecentric.chaosmonkey.jakarta;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Consumer;

import static de.codecentric.chaosmonkey.jakarta.ChaosEvent.Type.APPLY;
import static jakarta.json.stream.JsonParser.Event.KEY_NAME;
import static jakarta.ws.rs.core.Response.StatusType;

@Data @NoArgsConstructor @AllArgsConstructor @Builder @With @Accessors(chain = true)
// TODO is there an easier way to do this?
@JsonbTypeSerializer(Chaos.Serializer.class)
@JsonbTypeDeserializer(Chaos.Deserializer.class)
public class Chaos {
    private transient Integer failureCount;
    private transient Integer statusCode;
    private transient Integer delay;

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
        --failureCount;
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

    public static class Serializer implements JsonbSerializer<Chaos> {
        @Override public void serialize(Chaos obj, JsonGenerator generator, SerializationContext ctx) {
            generator.writeStartObject();
            if (obj.failureCount != null) ctx.serialize("failureCount", obj.failureCount, generator);
            if (obj.statusCode != null) ctx.serialize("statusCode", obj.statusCode, generator);
            if (obj.delay != null) ctx.serialize("delay", obj.delay, generator);
            generator.writeEnd();
        }
    }

    public static class Deserializer implements JsonbDeserializer<Chaos> {
        @Override public Chaos deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            var chaos = new Chaos();
            while (parser.hasNext()) {
                var event = parser.next();
                if (event == KEY_NAME) {
                    var fieldName = parser.getString();
                    parser.next();
                    var value = ctx.deserialize(Integer.class, parser);
                    switch (fieldName) {
                        case "failureCount" -> chaos.failureCount = value;
                        case "statusCode" -> chaos.statusCode = value;
                        case "delay" -> chaos.delay = value;
                    }
                }
            }
            return chaos;
        }
    }
}
