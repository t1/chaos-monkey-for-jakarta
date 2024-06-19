package de.codecentric.chaosmonkey.jakarta;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/chaos")
public class ChaosController {
    @Inject
    ChaosConfigs configs;

    @GET
    public Locations getAll() {
        return configs.all();
    }

    @DELETE
    public Locations deleteAllChaos() {
        configs.reset();
        return configs.all();
    }

    @GET @Path("{location}")
    public RestMethods get(@PathParam("location") ChaosLocation location) {
        return configs.at(location);
    }

    @GET @Path("{location}/{method}")
    public RestPaths get(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method) {
        return configs.at(location).on(method);
    }

    @GET @Path("/{location}/{method}/{path:.*}")
    public ChaosConfig getConfig(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path) {
        return configs.at(location).on(method).get("/" + path);
    }

    @PUT @Path("/{location}/{method}/{path:.*}")
    public ChaosConfig putConfig(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path,
            ChaosConfig config) {
        return configs.at(location).on(method).set("/" + path, config);
    }

    @GET @Path("/{location}/{method}/{path:.*}/failureCount")
    public int getFailureCount(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path) {
        return getConfig(location, method, path).getFailureCount();
    }

    @GET @Path("/{location}/{method}/{path:.*}/failureStatus")
    public int getFailureStatus(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path) {
        return getConfig(location, method, path).getStatusCode();
    }

    @PUT @Path("/{location}/{method}/{path:.*}/failureCount")
    public ChaosConfig putFailureCount(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path,
            int value) {
        var chaosConfig = getConfig(location, method, path);
        chaosConfig.setFailureCount(value);
        return chaosConfig;
    }

    @PUT @Path("/{location}/{method}/{path:.*}/failureStatus")
    public ChaosConfig putFailureStatus(
            @PathParam("location") ChaosLocation location,
            @PathParam("method") String method,
            @PathParam("path") String path,
            int statusCode) {
        var chaosConfig = getConfig(location, method, path);
        chaosConfig.setStatusCode(statusCode);
        return chaosConfig;
    }
}
