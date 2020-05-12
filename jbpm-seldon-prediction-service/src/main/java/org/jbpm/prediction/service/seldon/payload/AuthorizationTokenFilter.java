package org.jbpm.prediction.service.seldon.payload;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class AuthorizationTokenFilter implements ClientRequestFilter {

    private final String token;

    public AuthorizationTokenFilter(String token) {
        this.token = token;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add("Authorization", "Bearer " + token);
    }
}