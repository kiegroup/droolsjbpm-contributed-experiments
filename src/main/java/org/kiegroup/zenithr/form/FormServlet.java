package org.kiegroup.zenithr.form;

import org.kiegroup.zenithr.drools.RuleService;
import org.kiegroup.zenithr.drools.SessionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/*")
public class FormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameterMap().size() > 0) {
            request.getRequestDispatcher("/rest").forward(request, response);
        } else {
            response.getWriter().print(getHTML(""));
            response.getWriter().close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameterMap().size() > 0) {
            Object output = RuleService.getOutput(request.getParameterMap());
            String result = String.format(getFileContent("/result.html"), output);
            response.getWriter().print(getHTML(result));
            response.getWriter().close();
        } else {
            request.getRequestDispatcher("/rest").forward(request, response);
        }
    }

    private String getHTML(String result) {
        String template = getFileContent("/form.html");
        String serviceName = getServiceName();
        StringWriter inputSection = getInputSection();
        String html = String.format(template, serviceName, inputSection, result);
        System.out.println(html);
        return html;
    }

    private StringWriter getInputSection() {
        String inputTemplate = getFileContent("/input.html");
        Map<String, String> inputTypes = SessionFactory.getInstance().getInputTypeMap();
        StringWriter inputSection = new StringWriter();
        for (String name : inputTypes.keySet()) {
            String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
            inputSection.append(String.format(inputTemplate, name, capitalized, getHtmlType(inputTypes.get(name)), name, name));
        }
        System.out.println(inputSection.toString());
        return inputSection;
    }

    private String getHtmlType(String type) {
        switch (type) {
            case "boolean":
                return "checkbox";
            case "int":
            case "double":
            case "long":
                return "number";
            case "date":
                return "date";
            case "datetime":
                return "datetime-local";
            case "string":
            default:
                return "text";
        }
    }

    private String getFileContent(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
    }

    private static String getServiceName() {
        Optional<String> name = Optional.ofNullable(System.getenv("NAME"));
        return name.orElse("Zenithr");
    }
}
