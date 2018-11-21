/*
 * All rights reserved.
 */

package com.precisionhawk.ams.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class HttpClientUtil {
    private HttpClientUtil() {}
    
    public static String consumeEntity(HttpEntity entity) throws IOException {
        if (entity == null || entity.getContentLength() == 0L) {
            return "";
        }
        Reader reader = null;
        StringWriter writer = null;
        try {
            reader = new InputStreamReader(entity.getContent());
            writer = new StringWriter();
            IOUtils.copy(reader, writer);
            IOUtils.closeQuietly(writer);
            return writer.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
        }
    }
}
