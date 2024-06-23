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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jakarta.json.stream.JsonParser.Event.KEY_NAME;

@JsonbTypeSerializer(RestMethods.Serializer.class)
@JsonbTypeDeserializer(RestMethods.Deserializer.class)
@EqualsAndHashCode
public class RestMethods {
    private final Map<RestMethod, RestPaths> methods = new ConcurrentHashMap<>();

    @Override public String toString() {return "RestMethods:" + methods;}

    public boolean active() {return methods.values().stream().anyMatch(RestPaths::active);}

    public RestPaths with(String method) {return with(RestMethod.of(method));}

    // there's only a limited number of REST methods, so we can store them all
    public RestPaths with(RestMethod method) {return methods.computeIfAbsent(method, t -> new RestPaths());}


    public void clear() {methods.clear();}


    public static class Serializer implements JsonbSerializer<RestMethods> {
        @Override public void serialize(RestMethods obj, JsonGenerator generator, SerializationContext ctx) {
            generator.writeStartObject();
            obj.methods.forEach((key, value) -> {
                if (value.active()) ctx.serialize(key.name(), value, generator);
            });
            generator.writeEnd();
        }
    }

    public static class Deserializer implements JsonbDeserializer<RestMethods> {
        @Override public RestMethods deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            var methods = new RestMethods();
            while (parser.hasNext()) {
                var event = parser.next();
                if (event == KEY_NAME) {
                    var method = RestMethod.of(parser.getString());
                    parser.next();
                    var restPaths = ctx.deserialize(RestPaths.class, parser);
                    methods.methods.put(method, restPaths);
                }
            }
            return methods;
        }
    }
}
