package test;

import de.codecentric.chaosmonkey.jakarta.Chaos;
import de.codecentric.chaosmonkey.jakarta.Directions;
import de.codecentric.chaosmonkey.jakarta.RestMethods;
import de.codecentric.chaosmonkey.jakarta.RestPaths;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import static de.codecentric.chaosmonkey.jakarta.ChaosDirection.INCOMING;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.DELETE;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.GET;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.POST;
import static de.codecentric.chaosmonkey.jakarta.RestMethod.PUT;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static test.CustomAssertions.then;

class JsonTest {
    public static final Jsonb JSONB = JsonbBuilder.create();

    @Test void shouldSerializeChaosConfig() {
        var chaosConfig = Chaos.builder()
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

    @Test void shouldDeserializeChaosConfig() {
        var chaos = JSONB.fromJson("""
                {
                    "failureCount": 3,
                    "statusCode": 501
                }
                """, Chaos.class);

        then(chaos).isEqualTo(Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
    }

    @Test void shouldSerializeRestPaths() {
        var restPaths = new RestPaths();
        restPaths.patch("/foo", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        restPaths.patch("/bar", Chaos.builder()
                .failureCount(1)
                .statusCode(BAD_REQUEST)
                .build());
        restPaths.patch("/inactive", Chaos.builder().build());

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

    @Test void shouldDeserializeRestPaths() {
        var actual = JSONB.fromJson("""
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
                """, RestPaths.class);

        var expected = new RestPaths();
        expected.patch("/foo", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        expected.patch("/bar", Chaos.builder()
                .failureCount(1)
                .statusCode(BAD_REQUEST)
                .build());
        then(actual).isEqualTo(expected);
    }

    @Test void shouldSerializeOnlyInactiveRestPaths() {
        var restPaths = new RestPaths();
        restPaths.at("/inactive");

        var json = JSONB.toJson(restPaths);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }

    @Test void shouldSerializeRestMethods() {
        var restMethods = new RestMethods();
        restMethods.with(PUT).patch("/foo/bar", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        restMethods.with(POST).patch("/inactive", Chaos.builder().build());

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

    @Test void shouldDeserializeRestMethods() {
        var actual = JSONB.fromJson("""
                {
                    "PUT": {
                        "/foo/bar": {
                            "failureCount": 3,
                            "statusCode": 501
                        }
                    }
                }
                """, RestMethods.class);

        var expected = new RestMethods();
        expected.with(PUT).patch("/foo/bar", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        then(actual).isEqualTo(expected);
    }

    @Test void shouldSerializeOnlyInactiveRestMethods() {
        var restMethods = new RestMethods();
        restMethods.with(GET).at("/inactive");

        var json = JSONB.toJson(restMethods);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }

    @Test void shouldSerializeDirections() {
        var directions = new Directions();
        directions.at(INCOMING).with(DELETE).patch("/foo/bar", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var json = JSONB.toJson(directions);

        then(json).isJsonEqualTo("""
                {
                    "INCOMING": {
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

    @Test void shouldDeserializeDirections() {
        var actual = JSONB.fromJson("""
                {
                    "INCOMING": {
                        "DELETE": {
                            "/foo/bar": {
                                "failureCount": 3,
                                "statusCode": 501
                            }
                        }
                    }
                }
                """, Directions.class);

        var expected = new Directions();
        expected.at(INCOMING).with(DELETE).patch("/foo/bar", Chaos.builder()
                .failureCount(3)
                .statusCode(NOT_IMPLEMENTED)
                .build());
        then(actual).isEqualTo(expected);
    }

    @Test void shouldSerializeOnlyInactiveDirections() {
        var directions = new Directions();
        directions.at(INCOMING).with(DELETE).at("/foo/bar").setDelay(1000);

        var json = JSONB.toJson(directions);

        then(json).isJsonEqualTo("""
                {
                }
                """);
    }

    @Test void shouldSerializeAtypicalDirections() {
        var directions = new Directions();
        directions.at(INCOMING).with("AHOY").patch("/", Chaos.builder()
                .failureCount(3)
                .statusCode(418) // I'm a teapot
                .build());

        var json = JSONB.toJson(directions);

        then(json).isJsonEqualTo("""
                {
                    "INCOMING": {
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
}
