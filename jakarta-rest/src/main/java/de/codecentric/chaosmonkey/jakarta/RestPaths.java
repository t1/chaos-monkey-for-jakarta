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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static jakarta.json.stream.JsonParser.Event.KEY_NAME;

@JsonbTypeSerializer(RestPaths.Serializer.class)
@JsonbTypeDeserializer(RestPaths.Deserializer.class)
@EqualsAndHashCode
public class RestPaths {
    public record RestPath(String path) {
        public static RestPath of(String restPath) {return new RestPath(restPath);}
    }

    private final Map<RestPath, Chaos> paths = new ConcurrentHashMap<>();

    @Override public String toString() {return "RestPaths:" + paths;}

    public boolean active() {return paths.values().stream().anyMatch(Chaos::active);}


    public Chaos at(String restPath) {return at(RestPath.of(restPath));}

    // could be many paths in an application and we don't want to store an empty config for all of them.
    public Chaos at(RestPath restPath) {
        return Optional.ofNullable(paths.get(restPath)).orElseGet(Chaos::new);
    }

    public Chaos put(String path, Chaos config) {return put(RestPath.of(path), config);}

    private Chaos put(RestPath restPath, Chaos config) {return paths.put(restPath, config);}

    public Chaos patch(String path, Chaos config) {return patch(RestPath.of(path), config);}

    private Chaos patch(RestPath restPath, Chaos config) {return paths.merge(restPath, config, Chaos::merge);}


    public static class Serializer implements JsonbSerializer<RestPaths> {
        @Override public void serialize(RestPaths obj, JsonGenerator generator, SerializationContext ctx) {
            generator.writeStartObject();
            obj.paths.forEach((key, value) -> {
                if (value.active()) ctx.serialize(key.path(), value, generator);
            });
            generator.writeEnd();
        }
    }

    public static class Deserializer implements JsonbDeserializer<RestPaths> {
        @Override public RestPaths deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
            var paths = new RestPaths();
            while (parser.hasNext()) {
                var event = parser.next();
                if (event == KEY_NAME) {
                    var method = RestPath.of(parser.getString());
                    parser.next();
                    var restPaths = ctx.deserialize(Chaos.class, parser);
                    paths.paths.put(method, restPaths);
                }
            }
            return paths;
        }
    }
}
