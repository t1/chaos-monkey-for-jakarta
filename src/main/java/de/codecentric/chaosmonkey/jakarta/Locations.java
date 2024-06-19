package de.codecentric.chaosmonkey.jakarta;

import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static jakarta.json.stream.JsonParser.Event.KEY_NAME;

@JsonbTypeSerializer(Locations.Serializer.class)
@JsonbTypeDeserializer(Locations.Deserializer.class)
public class Locations {
    private final Map<ChaosLocation, RestMethods> locations = new EnumMap<>(ChaosLocation.class);

    public Locations() {
        // we fully initialize the map right in the constructor to avoid concurrency issues
        Arrays.stream(ChaosLocation.values()).forEach(location -> locations.put(location, new RestMethods()));
    }

    public RestMethods at(ChaosLocation chaosLocation) {
        return locations.get(chaosLocation);
    }

    public boolean active() {return locations.values().stream().anyMatch(RestMethods::active);}

    public void reset() {locations.values().forEach(RestMethods::clear);}

    public static class Serializer implements JsonbSerializer<Locations> {
        @Override public void serialize(Locations obj, JsonGenerator generator, SerializationContext ctx) {
            generator.writeStartObject();
            obj.locations.forEach((key, value) -> {
                if (value.active()) ctx.serialize(key.name(), value, generator);
            });
            generator.writeEnd();
        }
    }

    public static class Deserializer implements JsonbDeserializer<Locations> {
        @Override public Locations deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            var locations = new Locations();
            while (parser.hasNext()) {
                var event = parser.next();
                if (event == KEY_NAME) {
                    var location = ChaosLocation.valueOf(parser.getString());
                    parser.next();
                    var restMethods = ctx.deserialize(RestMethods.class, parser);
                    locations.locations.put(location, restMethods);
                }
            }
            return locations;
        }
    }
}
