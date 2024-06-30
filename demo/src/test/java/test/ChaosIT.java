package test;

import app.Greeting;
import de.codecentric.chaosmonkey.jakarta.Chaos;
import de.codecentric.chaosmonkey.jakarta.ChaosDirection;
import de.codecentric.chaosmonkey.jakarta.Directions;
import de.codecentric.chaosmonkey.jakarta.RestMethod;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.net.URI;

import static de.codecentric.chaosmonkey.jakarta.ChaosDirection.INCOMING;
import static de.codecentric.chaosmonkey.jakarta.ChaosDirection.OUTGOING;
import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

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
        @PUT @Path("/{direction}/GET/greetings/direct")
        Chaos put(@PathParam("direction") ChaosDirection direction, Chaos config);

        @PATCH @Path("/{direction}/GET/greetings/direct")
        Chaos patch(@PathParam("direction") ChaosDirection direction, Chaos config);

        @DELETE Directions reset();

        @GET Directions getAll();
    }

    URI baseUri = URI.create("http://localhost:8080");
    GreetingsApi greetingsApi = rest(GreetingsApi.class);
    ChaosApi chaosApi = rest(ChaosApi.class);

    <T> T rest(Class<T> api) {
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .property("microprofile.rest.client.disable.default.mapper", true)
                .readTimeout(500, MILLISECONDS)
                .build(api);
    }

    @BeforeEach void setUp() {
        chaosApi.reset();
        System.out.println("#".repeat(120));
    }

    @AfterEach
    void shouldHaveNoMoreChaos() {
        System.out.println("-".repeat(50));
        then(chaosApi.getAll().active()).isFalse();
        System.out.println("=".repeat(50));
    }

    @Test void shouldReadEmptyChaos() {
        var chaos = chaosApi.getAll();

        then(chaos.active()).isFalse();
    }

    @Test void shouldSucceedWithoutChaos() {
        var greeting = greetingsApi.get();

        then(greeting.getGreeting()).isEqualTo("Indirect Hello");
        then(greeting.getTarget()).isEqualTo("World");
    }

    @Test void shouldSucceedWithSingleIncomingStatusChaos() {
        chaosApi.put(INCOMING, Chaos.builder()
                .failureCount(1)
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var greeting = greetingsApi.get();

        then(greeting.getGreeting()).isEqualTo("Indirect Hello");
        then(greeting.getTarget()).isEqualTo("World");
    }

    @Test void shouldSucceedWithSingleOutgoingStatusChaos() {
        chaosApi.put(OUTGOING, Chaos.builder()
                .failureCount(1)
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var greeting = greetingsApi.get();

        then(greeting.getGreeting()).isEqualTo("Indirect Hello");
        then(greeting.getTarget()).isEqualTo("World");
    }

    @Test void shouldFailWithTooMuchIncomingStatusChaos() {
        chaosApi.put(INCOMING, Chaos.builder()
                .failureCount(4) // exactly one more failures than there are retries
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var response = greetingsApi.getResponse();

        then(response.getStatusInfo()).isEqualTo(NOT_IMPLEMENTED);
    }

    @Test void shouldFailWithTooMuchOutgoingStatusChaos() {
        chaosApi.put(OUTGOING, Chaos.builder()
                .failureCount(4) // exactly one more failures than there are retries
                .statusCode(NOT_IMPLEMENTED)
                .build());

        var response = greetingsApi.getResponse();

        then(response.getStatusInfo()).isEqualTo(NOT_IMPLEMENTED);
    }

    @Test void shouldFailWithTooMuchIncomingDelayChaos() {
        chaosApi.put(INCOMING, Chaos.builder()
                .failureCount(1)
                .delay(2_000)
                .build());

        var throwable = catchThrowable(greetingsApi::get);

        then(throwable).isInstanceOf(ProcessingException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }

    @Test void shouldFailWithTooMuchOutgoingDelayChaos() {
        chaosApi.put(OUTGOING, Chaos.builder()
                .failureCount(1)
                .delay(2_000)
                .build());

        var throwable = catchThrowable(greetingsApi::get);

        then(throwable).isInstanceOf(ProcessingException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }

    @Test void shouldReadChaos() {
        var chaos = Chaos.builder().failureCount(1).statusCode(NOT_IMPLEMENTED).build();
        chaosApi.put(OUTGOING, chaos);

        var all = chaosApi.getAll();

        then(all.active()).isTrue();
        then(all.at(OUTGOING).with(RestMethod.GET).at("/greetings/direct"))
                .isEqualTo(chaos);
        chaosApi.reset();
    }

    @Disabled("jersey mp client doesn't support PATCH")
    @Test void shouldPatchChaos() {
        var failOnce = Chaos.builder().failureCount(1).build();
        chaosApi.put(OUTGOING, failOnce);

        var updated = chaosApi.patch(OUTGOING, Chaos.builder().statusCode(NOT_IMPLEMENTED).build());

        then(updated).isEqualTo(Chaos.builder().failureCount(1).statusCode(NOT_IMPLEMENTED).build());
        var chaos = chaosApi.getAll();
        then(chaos.active()).isTrue();
        then(chaos.at(OUTGOING).with(RestMethod.GET).at("/greetings/direct"))
                .isEqualTo(updated);
        chaosApi.reset();
    }
}
