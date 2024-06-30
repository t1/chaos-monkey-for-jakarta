package de.codecentric.chaosmonkey.jakarta.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Serve files in <code>resources/META-INF/static</code>.
 * This would work out-of-the-box, if the {@link jakarta.ws.rs.ApplicationPath} was not empty.
 */
@Slf4j
@Path("/static")
@ApplicationScoped
public class StaticFilesResource {
    private final StaticFilesLoader files = new StaticFilesLoader("static", "META-INF/static/");

    @GET
    @Path("/{filePath:.*}")
    public Response getStaticResource(@PathParam("filePath") String filePath) {
        log.debug("getStaticResource({})", filePath);
        return files.response(filePath);
    }
}
