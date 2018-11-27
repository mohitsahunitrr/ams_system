/*
 * All rights reserved.
 */
package com.precisionhawk.ams.webapi.swagger;

import com.esotericsoftware.yamlbeans.YamlReader;
import io.swagger.jaxrs2.Reader;
import static io.swagger.jaxrs2.integration.OpenApiServlet.ACCEPT_HEADER;
import static io.swagger.jaxrs2.integration.OpenApiServlet.APPLICATION_JSON;
import static io.swagger.jaxrs2.integration.OpenApiServlet.APPLICATION_YAML;
import io.swagger.oas.integration.GenericOpenApiContext;
import io.swagger.oas.integration.OpenApiConfigurationException;
import io.swagger.oas.integration.OpenApiContextLocator;
import io.swagger.oas.integration.SwaggerConfiguration;
import io.swagger.oas.integration.api.OpenApiContext;
import io.swagger.oas.models.OpenAPI;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A servlet which will produce the OpenAPI specification v3.0.0 service spec.
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
// Code here taken from https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-JAX-RS-Project-Setup-2.0.X#using-openapiservlet-with-webxml
// and io.swagger.jaxrs2.integration.OpenApiServlet because I couldn't get said servlet to configure properly as well as the fact that I had to
// subclass io.swagger.jaxrs2.integration.JaxrsApplicationAndAnnotationScanner to make it scan classes rather than just packages.
public class OpenAPISpecServlet extends HttpServlet {
    
    private static final String CONFIG_URI = "ams.swagger.config.uri";
    private static final String CONTEXT_ID = "com.precisionhawk.ams.webservices.context";

    //TODO: Get version from maven.
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        URL configURL = getClass().getClassLoader().getResource(config.getInitParameter(CONFIG_URI));
        if (configURL == null) {
            throw new ServletException(String.format("Servlet init parameter %s not provided.", CONFIG_URI));
        }
        SwaggerConfiguration oasConfig = null;
        java.io.Reader reader = null;
        try {
            reader = new InputStreamReader(configURL.openStream());
            YamlReader yamlreader = new YamlReader(reader);
            oasConfig = yamlreader.read(SwaggerConfiguration.class);
        } catch (IOException ex) {
            throw new ServletException("Unable to load configuration.", ex);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        
        OpenAPI oas = new OpenAPI();
        try {
            OpenApiContext ctx = new GenericOpenApiContext()
                .openApiConfiguration(oasConfig)
                .openApiReader(new Reader(oasConfig))
                .openApiScanner(new JaxrsAnnotationScanner().openApiConfiguration(oasConfig))
                .init();
            OpenApiContextLocator.getInstance().putOpenApiContext(CONTEXT_ID, ctx);
        } catch (OpenApiConfigurationException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        OpenApiContext ctx = OpenApiContextLocator.getInstance().getOpenApiContext(CONTEXT_ID);
        OpenAPI oas = ctx.read();

        String type = "json";

        String acceptHeader = req.getHeader(ACCEPT_HEADER);
        if (!StringUtils.isBlank(acceptHeader) && acceptHeader.toLowerCase().contains(APPLICATION_YAML)) {
            type = "yaml";
        } else {
            // check URL:
            if (req.getRequestURL().toString().toLowerCase().endsWith("yaml")) {
                type = "yaml";
            }
        }

        boolean pretty = false;
        if (ctx.getOpenApiConfiguration() != null && Boolean.TRUE.equals(ctx.getOpenApiConfiguration().isPrettyPrint())) {
            pretty = true;
        }

        resp.setStatus(200);

        if (type.equalsIgnoreCase("yaml")) {
            resp.setContentType(APPLICATION_YAML);
            PrintWriter pw = resp.getWriter();
            pw.write(pretty ? Yaml.pretty(oas) : Yaml.mapper().writeValueAsString(oas));
            pw.close();
        } else {
            resp.setContentType(APPLICATION_JSON);
            PrintWriter pw = resp.getWriter();
            pw.write(pretty ? Json.pretty(oas) : Json.mapper().writeValueAsString(oas));
            pw.close();
        }
    }
}
