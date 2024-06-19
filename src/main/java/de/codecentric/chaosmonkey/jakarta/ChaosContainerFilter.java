package de.codecentric.chaosmonkey.jakarta;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import static de.codecentric.chaosmonkey.jakarta.ChaosLocation.CONTAINER;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class ChaosContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject
    ChaosConfigs chaosConfigs;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUriInfo().getRequestUri();
        log.warn("container got {} {}", method, uri);
        chaosConfigs.at(CONTAINER).on(method).get(uri.getPath())
                .apply(requestContext::abortWith);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        log.warn("container return {} {}", responseContext.getStatus(), responseContext.getStatusInfo().getReasonPhrase());
    }
}
