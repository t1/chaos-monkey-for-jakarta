package de.codecentric.chaosmonkey.jakarta.ui;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import static de.codecentric.chaosmonkey.jakarta.ui.Messages.Level.INFO;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class ChaosUiContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject Messages messages;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUriInfo().getRequestUri();
        messages.add(Instant.now(), INFO, method + " " + uri);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    }
}
