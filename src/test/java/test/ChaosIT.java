package test;

import app.Greeting;
import de.codecentric.chaosmonkey.jakarta.ChaosConfig;
import de.codecentric.chaosmonkey.jakarta.ChaosLocation;
import de.codecentric.chaosmonkey.jakarta.Locations;
import de.codecentric.chaosmonkey.jakarta.RestMethod;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static de.codecentric.chaosmonkey.jakarta.ChaosLocation.CLIENT;
import static de.codecentric.chaosmonkey.jakarta.ChaosLocation.CONTAINER;
import static jakarta.ws.rs.core.Response.Status.BAD_GATEWAY;
import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static test.CustomAssertions.then;

class ChaosIT {
    @Path("/greetings")
    public interface GreetingsApi {
        @GET @Path("/indirect")
        Greeting get();

        @GET @Path("/indirect")
        Response getResponse();
    }

    @Path("/chaos")
    public interface ChaosApi {
        @PUT @Path("/{LOCATION}/GET/rest/greetings/direct")
        ChaosConfig put(@PathParam("LOCATION") ChaosLocation location, ChaosConfig config);

        @DELETE Locations reset();

        @GET Locations getAll();
    }

    URI baseUri = URI.create("http://localhost:8080/rest");
    GreetingsApi greetings = rest(GreetingsApi.class);
    ChaosApi chaos = rest(ChaosApi.class);

    <T> T rest(Class<T> api) {
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .property("microprofile.rest.client.disable.default.mapper", true)
                .build(api);
    }

    @BeforeEach void setUp() {chaos.reset();}

    @AfterEach
    void shouldHaveNoMoreChaos() {
        System.out.println("-".repeat(50));
        then(chaos.getAll().active()).isFalse();
        System.out.println("=".repeat(50));
    }

    @Test void shouldSucceedWithoutChaos() {
        var greeting = greetings.get();

        then(greeting.getGreeting()).isEqualTo("Indirect Hello");
        then(greeting.getTarget()).isEqualTo("World");
    }

    @Test void shouldReadEmptyChaos() {
        var chaosLocations = chaos.getAll();

        then(chaosLocations.active()).isFalse();
    }

    @Test void shouldFailWithTooMuchClientChaos() {
        chaos.put(CLIENT, ChaosConfig.builder()
                .failureCount(4) // exactly one more failures than there are retries
                .build());

        var response = greetings.getResponse();

        then(response.getStatusInfo()).isEqualTo(BAD_GATEWAY); // this is the default
    }

    @Test void shouldFailWithTooMuchContainerChaos() {
        chaos.put(CONTAINER, ChaosConfig.builder()
                .failureCount(4) // exactly one more failures than there are retries
                .build());

        var response = greetings.getResponse();

        then(response.getStatusInfo()).isEqualTo(BAD_GATEWAY); // this is the default
    }

    @Test void shouldReadChaos() {
        var chaosConfig = ChaosConfig.builder().failureCount(1).statusCode(NOT_IMPLEMENTED).build();
        var created = chaos.put(CLIENT, chaosConfig);
        then(created).isEqualTo(chaosConfig);

        var chaosLocations = chaos.getAll();

        then(chaosLocations.active()).isTrue();
        then(chaosLocations.at(CLIENT).on(RestMethod.GET).get("/rest/greetings/direct"))
                .isEqualTo(chaosConfig);
        chaos.reset();
    }

    @Test void shouldMergeChaos() {
        var chaosConfig = ChaosConfig.builder().failureCount(1).build();
        var created = chaos.put(CLIENT, chaosConfig);
        then(created).isEqualTo(chaosConfig);

        var updated = chaos.put(CLIENT, ChaosConfig.builder().statusCode(NOT_IMPLEMENTED).build());

        var merged = ChaosConfig.builder().failureCount(1).statusCode(NOT_IMPLEMENTED).build();
        then(updated).isEqualTo(merged);
        var chaosLocations = chaos.getAll();
        then(chaosLocations.active()).isTrue();
        then(chaosLocations.at(CLIENT).on(RestMethod.GET).get("/rest/greetings/direct"))
                .isEqualTo(merged);
        chaos.reset();
    }
}
