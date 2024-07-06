package de.codecentric.chaosmonkey.jakarta;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import static de.codecentric.chaosmonkey.jakarta.ChaosEvent.Type.ADD;
import static de.codecentric.chaosmonkey.jakarta.ChaosEvent.Type.UPDATE;

@Path("/chaos")
public class ChaosController {
    @Inject
    ChaosConfigs configs;

    @GET
    public Directions getAll() {
        return configs.all();
    }

    @DELETE
    public Directions deleteAllChaos() {
        configs.reset();
        return configs.all();
    }

    @GET @Path("{direction}")
    public RestMethods get(@PathParam("direction") ChaosDirection direction) {
        return configs.when(direction);
    }

    @GET @Path("{direction}/{method}")
    public RestPaths get(
            @PathParam("direction") ChaosDirection direction,
            @PathParam("method") String method) {
        return configs.when(direction).with(method);
    }

    @GET @Path("/{direction}/{method}/{path:.*}")
    public Chaos getConfig(
            @PathParam("direction") ChaosDirection direction,
            @PathParam("method") String method,
            @PathParam("path") String path) {
        return configs.when(direction).with(method).at("/" + path);
    }

    @PUT @Path("/{direction}/{method}/{path:.*}")
    public Chaos putConfig(
            @PathParam("direction") ChaosDirection direction,
            @PathParam("method") String method,
            @PathParam("path") String path,
            Chaos config) {
        ChaosEvents.send(ADD, direction + " " + method + " " + config);
        return configs.when(direction).with(method).put("/" + path, config);
    }

    @Path("/{direction}/{method}/{path:.*}")
    public Chaos patchConfig(
            @PathParam("direction") ChaosDirection direction,
            @PathParam("method") String method,
            @PathParam("path") String path,
            Chaos config) {
        ChaosEvents.send(UPDATE, direction + " " + method + " " + config);
        return configs.when(direction).with(method).patch("/" + path, config);
    }
}
