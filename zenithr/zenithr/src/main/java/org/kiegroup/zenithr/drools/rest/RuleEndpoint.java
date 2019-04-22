/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiegroup.zenithr.drools.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kiegroup.zenithr.drools.model.OutputFact;
import org.kiegroup.zenithr.drools.model.exceptions.TypeParseException;
import org.kiegroup.zenithr.drools.service.RuleService;
import org.kiegroup.zenithr.drools.service.SessionFactory;

@Path("/")
public class RuleEndpoint {

    @Inject
    SessionFactory sessionFactory;
    @Inject
    RuleService ruleService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet(@Context HttpServletRequest request) {
        try {
            if (request.getParameterMap().size() > 0) {
                Object output = ruleService.process(toInputParameters(request.getParameterMap()));
                return Response.ok(output).build();
            } else {
                return Response.ok(getHTML(request.getParameterMap(), "")).build();
            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(Map<String, String> parameters) {
        try {
            Object output = ruleService.process(parameters);
            return Response.ok(output).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response doPost(Form form, @Context HttpServletRequest request) {
        try {
            Map<String, String[]> parameters = form.asMap().entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().toArray(new String[0])));
            Object output = ruleService.process(toInputParameters(parameters));
            String result = String.format(getFileContent("/META-INF/resources/result.html"), outputToJson(output));
            return Response.ok(getHTML(request.getParameterMap(), result)).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private String getHTML(Map<String, String[]> parameterMap, String result) {
        String template = getFileContent("/META-INF/resources/form.html");
        String serviceName = sessionFactory.getServiceName();
        StringWriter inputSection = getInputSection(parameterMap);
        return String.format(template, serviceName, serviceName, inputSection, result);
    }

    private StringWriter getInputSection(Map<String, String[]> parameterMap) {
        String inputTemplate = getFileContent("/META-INF/resources/input.html");
        StringWriter inputSection = new StringWriter();
        for (String name : sessionFactory.getInputNames()) {
            String value = "";
            if (parameterMap.get(name) != null) {
                value = parameterMap.get(name)[0];
            }
            String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
            // Support only string. Otherwise we will need to define the input types ONLY for the form rendering
            inputSection.append(String.format(inputTemplate, name, capitalized, "string", "", name, name, value));
        }
        return inputSection;
    }

    private String getFileContent(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
    }

    private String outputToJson(Object output) {
        if (OutputFact.class.equals(output.getClass())) {
            return output.toString();
        }
        try {
            return objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new TypeParseException(e);
        }
    }

    private Map<String, String> toInputParameters(Map<String, String[]> formParams) {
        Map<String, String> inputParams = new HashMap<>();
        formParams.entrySet().stream().forEach(e -> inputParams.put(e.getKey(), e.getValue()[0]));
        return inputParams;
    }
}
