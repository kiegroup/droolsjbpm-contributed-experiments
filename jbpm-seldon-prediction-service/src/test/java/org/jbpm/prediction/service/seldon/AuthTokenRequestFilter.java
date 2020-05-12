package org.jbpm.prediction.service.seldon;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class AuthTokenRequestFilter extends StubRequestFilter {

    @Override
    public RequestFilterAction filter(Request request) {
        if (request.header("Authorization").firstValue().equals("Bearer foobar")) {
            return RequestFilterAction.continueWith(request);
        }

        return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());

    }

    @Override
    public String getName() {
        return "bearer";
    }
}
