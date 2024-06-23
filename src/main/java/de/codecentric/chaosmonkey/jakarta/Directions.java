package de.codecentric.chaosmonkey.jakarta;

import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static jakarta.json.stream.JsonParser.Event.KEY_NAME;

@JsonbTypeSerializer(Directions.Serializer.class)
@JsonbTypeDeserializer(Directions.Deserializer.class)
@EqualsAndHashCode
public class Directions {
    private final Map<ChaosDirection, RestMethods> directions = new EnumMap<>(ChaosDirection.class);

    public Directions() {
        // we fully initialize the map right in the constructor to avoid concurrency issues
        Arrays.stream(ChaosDirection.values()).forEach(direction -> directions.put(direction, new RestMethods()));
    }

    @Override public String toString() {return "Directions:" + directions;}

    public RestMethods at(ChaosDirection chaosDirection) {
        return directions.get(chaosDirection);
    }

    public boolean active() {return directions.values().stream().anyMatch(RestMethods::active);}

    public void reset() {directions.values().forEach(RestMethods::clear);}

    public static class Serializer implements JsonbSerializer<Directions> {
        @Override public void serialize(Directions obj, JsonGenerator generator, SerializationContext ctx) {
            generator.writeStartObject();
            obj.directions.forEach((key, value) -> {
                if (value.active()) ctx.serialize(key.name(), value, generator);
            });
            generator.writeEnd();
        }
    }

    public static class Deserializer implements JsonbDeserializer<Directions> {
        @Override public Directions deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            var directions = new Directions();
            while (parser.hasNext()) {
                var event = parser.next();
                if (event == KEY_NAME) {
                    var direction = ChaosDirection.valueOf(parser.getString());
                    parser.next();
                    var restMethods = ctx.deserialize(RestMethods.class, parser);
                    directions.directions.put(direction, restMethods);
                }
            }
            return directions;
        }
    }
}
