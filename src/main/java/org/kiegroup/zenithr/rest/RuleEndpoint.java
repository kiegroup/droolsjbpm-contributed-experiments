package org.kiegroup.zenithr.rest;


import org.kiegroup.zenithr.drools.RuleService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path("/")
public class RuleEndpoint {

    @GET
    @Produces("application/json")
    public Response doGet(@Context HttpServletRequest request) {
        try {
            Object output = RuleService.getOutput(request.getParameterMap());
            return Response.ok(output).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
