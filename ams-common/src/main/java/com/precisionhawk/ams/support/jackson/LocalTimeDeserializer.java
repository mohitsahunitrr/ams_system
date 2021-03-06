package com.precisionhawk.ams.support.jackson;

import com.precisionhawk.ams.support.time.TimeFormattingConstants;
import java.io.IOException;
import java.time.LocalTime;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Philip A. Chapman
 */
public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> implements TimeFormattingConstants {

    private final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public LocalTime deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        String s = mapper.readValue(dc.getParser(), String.class);
        if (s == null || s.length() == 0) {
            return null;
        } else {
            return LocalTime.parse(s, TIME_FORMATTER);
        }
    }
    
}
