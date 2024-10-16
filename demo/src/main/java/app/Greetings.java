package app;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.LocalTime;

@Path("/greetings")
public class Greetings {
    @RegisterRestClient(baseUri = "http://localhost:8080")
    @Path("/greetings")
    public interface Api {
        @GET @Path("/direct")
        @Retry
        Greeting greeting();
    }

    @Inject Api api;

    @GET @Path("/direct")
    public Greeting greeting() {
        return Greeting.builder().greeting("Hello").target("World").time(LocalTime.now()).build();
    }

    @GET @Path("/indirect")
    public Greeting indirectGreeting() {
        return api.greeting().withGreeting("Indirect Hello");
    }
}
