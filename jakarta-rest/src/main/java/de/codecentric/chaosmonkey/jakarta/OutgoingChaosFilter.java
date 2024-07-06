package de.codecentric.chaosmonkey.jakarta;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import static de.codecentric.chaosmonkey.jakarta.ChaosDirection.OUTGOING;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class OutgoingChaosFilter implements ClientRequestFilter, ClientResponseFilter {
    @Inject
    ChaosConfigs configs;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUri();
        log.debug("outgoing {} {}", method, uri);
        configs.when(OUTGOING).with(method).at(uri.getPath())
                .apply(requestContext::abortWith);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        log.debug("return outgoing {} {}", responseContext.getStatus(), responseContext.getStatusInfo().getReasonPhrase());
    }
}
