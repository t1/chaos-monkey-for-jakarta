package de.codecentric.chaosmonkey.jakarta;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import static de.codecentric.chaosmonkey.jakarta.ChaosLocation.CLIENT;

@Provider
@Priority(Integer.MAX_VALUE)
@Slf4j
public class ChaosClientFilter implements ClientRequestFilter, ClientResponseFilter {
    @Inject
    ChaosConfigs chaosConfigs;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var method = requestContext.getMethod();
        var uri = requestContext.getUri();
        log.warn("client send {} {}", method, uri);
        chaosConfigs.at(CLIENT).on(method).get(uri.getPath())
                .apply(requestContext::abortWith);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        log.warn("client got {} {}", responseContext.getStatus(), responseContext.getStatusInfo().getReasonPhrase());
    }
}
