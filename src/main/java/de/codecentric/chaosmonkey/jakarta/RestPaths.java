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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static jakarta.json.stream.JsonParser.Event.KEY_NAME;

@JsonbTypeSerializer(RestPaths.Serializer.class)
@JsonbTypeDeserializer(RestPaths.Deserializer.class)
public class RestPaths {
    public record RestPath(String path) {
        public static RestPath of(String restPath) {return new RestPath(restPath);}
    }

    private final Map<RestPath, ChaosConfig> paths = new ConcurrentHashMap<>();

    public boolean active() {
        return paths.values().stream().anyMatch(ChaosConfig::active);
    }

    public ChaosConfig get(String restPath) {return get(RestPath.of(restPath));}

    // there may be many paths in an application and we don't want to store an empty config for all of them.
    public ChaosConfig get(RestPath restPath) {
        return Optional.ofNullable(paths.get(restPath)).orElseGet(ChaosConfig::new);
    }

    public ChaosConfig set(String path, ChaosConfig config) {return set(RestPath.of(path), config);}

    private ChaosConfig set(RestPath restPath, ChaosConfig config) {
        return paths.merge(restPath, config, ChaosConfig::merge);
    }

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
                    var restPaths = ctx.deserialize(ChaosConfig.class, parser);
                    paths.paths.put(method, restPaths);
                }
            }
            return paths;
        }
    }
}
