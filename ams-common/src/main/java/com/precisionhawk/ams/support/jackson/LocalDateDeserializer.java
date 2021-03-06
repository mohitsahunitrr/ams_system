package com.precisionhawk.ams.support.jackson;

import com.precisionhawk.ams.support.time.TimeFormattingConstants;
import java.io.IOException;
import java.time.LocalDate;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Philip A. Chapman
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> implements TimeFormattingConstants {

    private final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        String s = mapper.readValue(dc.getParser(), String.class);
        if (s == null || s.length() == 0) {
            return null;
        } else {
            return LocalDate.parse(s, DATE_FORMATTER);
        }
    }
    
}
