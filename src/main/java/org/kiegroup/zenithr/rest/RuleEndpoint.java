package org.kiegroup.zenithr.rest;


import org.kiegroup.zenithr.drools.RuleService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;


@Path("/")
public class RuleEndpoint {

    @GET
    @Produces("application/json")
    public Response doGet(@QueryParam("grade") String grade) {
        try {
            Double gradeNumber = Double.parseDouble(grade);
            String gradeLetter = RuleService.getGradeLetter(gradeNumber);
            return Response.ok(gradeLetter).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
