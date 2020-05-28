package org.jbpm.prediction.service.seldon;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class AuthTokenRequestFilter extends StubRequestFilter {

    @Override
    public RequestFilterAction filter(Request request) {
        // bypass authorization for token request endpoint
        if (request.getUrl().equals("/oauth/token")) {
            return RequestFilterAction.continueWith(request);
        }
        else {
            // use authorization for all other endpoints
            if (request.header("Authorization").firstValue().equals("Bearer foobar")) {
                return RequestFilterAction.continueWith(request);
            } else {
                return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
            }
        }
    }

    @Override
    public String getName() {
        return "bearer";
    }
}
