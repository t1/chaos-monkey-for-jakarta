package test;

import de.codecentric.chaosmonkey.jakarta.ChaosConfig;
import de.codecentric.chaosmonkey.jakarta.Locations;
import de.codecentric.chaosmonkey.jakarta.RestMethods;
import de.codecentric.chaosmonkey.jakarta.RestPaths;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import static de.codecentric.chaosmonkey.jakarta.ChaosLocation.CONTAINER;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.DELETE;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.GET;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.POST;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.PUT;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static test.CustomAssertions.then;

class JsonTest {
    public static final Jsonb JSONB = JsonbBuilder.create();

    @Test
    void shouldSerializeChaosConfig() {
        var chaosConfig = ChaosConfig.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build();

        var json = JSONB.toJson(chaosConfig);

        then(json).isJsonEqualTo("""
                {
                    "failureCount": 3,
                    "statusCode": 501
                }
                """);
    }

    @Test
    void shouldSerializeRestPaths() {
        var restPaths = new RestPaths();
        restPaths.set("/foo", ChaosConfig.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        restPaths.set("/bar", ChaosConfig.builder()
                .failureCount(1)
                .statusCode(BAD_REQUEST)
                .build());
        restPaths.set("/inactive", ChaosConfig.builder().build());

        var json = JSONB.toJson(restPaths);

        then(json).isJsonEqualTo("""
                {
                    "/foo": {
                        "failureCount": 3,
                        "statusCode": 501
                    },
                    "/bar": {
                        "failureCount": 1,
                        "statusCode": 400
                    }
                }
                """);
    }

    @Test
    void shouldSerializeOnlyInactiveRestPaths() {
        var restPaths = new RestPaths();
        restPaths.get("/inactive");

        var json = JSONB.toJson(restPaths);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }

    @Test
    void shouldSerializeRestMethods() {
        var restMethods = new RestMethods();
        restMethods.on(PUT).set("/foo/bar", ChaosConfig.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        restMethods.on(POST).set("/inactive", ChaosConfig.builder().build());

        var json = JSONB.toJson(restMethods);

        then(json).isJsonEqualTo("""
                {
                    "PUT": {
                        "/foo/bar": {
                            "failureCount": 3,
                            "statusCode": 501
                        }
                    }
                }
                """);
    }

    @Test
    void shouldSerializeOnlyInactiveRestMethods() {
        var restMethods = new RestMethods();
        restMethods.on(GET).get("/inactive");

        var json = JSONB.toJson(restMethods);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }

    @Test
    void shouldSerializeLocations() {
        var locations = new Locations();
        locations.at(CONTAINER).on(DELETE).set("/foo/bar", ChaosConfig.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var json = JSONB.toJson(locations);

        then(json).isJsonEqualTo("""
                {
                    "CONTAINER": {
                        "DELETE": {
                            "/foo/bar": {
                                "failureCount": 3,
                                "statusCode": 501
                            }
                        }
                    }
                }
                """);
    }

    @Test
    void shouldSerializeAtypicalChaosConfigs() {
        var locations = new Locations();
        locations.at(CONTAINER).on("AHOY").set("/", ChaosConfig.builder()
                .failureCount(3)
                .statusCode(418) // I'm a teapot
                .build());

        var json = JSONB.toJson(locations);

        then(json).isJsonEqualTo("""
                {
                    "CONTAINER": {
                        "AHOY": {
                            "/": {
                                "failureCount": 3,
                                "statusCode": 418
                            }
                        }
                    }
                }
                """);
    }

    @Test
    void shouldSerializeOnlyInactiveLocations() {
        var locations = new Locations();
        locations.at(CONTAINER).on(DELETE).get("/foo/bar");

        var json = JSONB.toJson(locations);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }
}
