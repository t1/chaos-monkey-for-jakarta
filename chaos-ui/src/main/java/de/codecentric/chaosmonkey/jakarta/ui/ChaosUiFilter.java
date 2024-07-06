package de.codecentric.chaosmonkey.jakarta.ui;

import de.codecentric.chaosmonkey.jakarta.ui.Messages.Message;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import static de.codecentric.chaosmonkey.jakarta.ui.Application.CHAOS_UI_ROOT;
import static de.codecentric.chaosmonkey.jakarta.ui.Messages.Message.Level.INFO;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class ChaosUiFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject Messages messages;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUriInfo().getRequestUri();
        if (uri.getPath().startsWith(CHAOS_UI_ROOT + "/") || uri.getPath().equals(CHAOS_UI_ROOT)) {
            log.trace("{} chaos-ui request: {}", method, uri);
        } else if (uri.getPath().startsWith("/chaos/")) {
            log.debug("{} chaos request: {}", method, uri);
        } else {
            messages.add(new Message(INFO, method + " " + uri));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    }
}
