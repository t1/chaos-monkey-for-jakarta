package de.codecentric.chaosmonkey.jakarta;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import static de.codecentric.chaosmonkey.jakarta.ChaosDirection.INCOMING;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class IncomingChaosFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject
    ChaosConfigs configs;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUriInfo().getRequestUri();
        log.debug("incoming {} {}", method, uri);
        configs.when(INCOMING).with(method).at(uri.getPath())
                .apply(requestContext::abortWith);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        log.debug("return incoming {} {}", responseContext.getStatus(), responseContext.getStatusInfo().getReasonPhrase());
    }
}
