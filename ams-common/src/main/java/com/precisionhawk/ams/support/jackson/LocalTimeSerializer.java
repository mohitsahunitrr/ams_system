package com.precisionhawk.ams.support.jackson;

import com.precisionhawk.ams.support.time.TimeFormattingConstants;
import java.io.IOException;
import java.time.LocalTime;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 *
 * @author Philip A. Chapman
 */
public class LocalTimeSerializer extends JsonSerializer<LocalTime> implements TimeFormattingConstants {

    @Override
    public void serialize(LocalTime t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        String s = t.format(TIME_FORMATTER);
        jg.writeString(s);
    }
    
}
