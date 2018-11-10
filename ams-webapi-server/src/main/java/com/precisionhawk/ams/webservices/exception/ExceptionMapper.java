package com.precisionhawk.ams.webservices.exception;

import com.precisionhawk.ams.config.ServicesConfig;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles WebApplicationExceptions writing out JSON which contains error information.
 *
 * @author Philip A. Chapman
 */
@Named
@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<WebApplicationException> {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    @Inject private ServicesConfig config;
    
    @Override
    public Response toResponse(WebApplicationException ex) {
        LOGGER.error("Handling error", ex);
        Map<String, Object> data = new HashMap<>();
        data.put("error", ex.getMessage());
        data.put("timestamp", ZonedDateTime.now());
        data.put("name", config.getAppName());
        data.put("environment", config.getEnvironment());
        data.put("version", config.getVersion());
        Response resp = ex.getResponse();
        return Response.status(resp.getStatus()).entity(data).type(MediaType.APPLICATION_JSON).build();
    }
}
