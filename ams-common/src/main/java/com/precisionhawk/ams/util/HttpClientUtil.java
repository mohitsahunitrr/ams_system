/*
 * All rights reserved.
 */

package com.precisionhawk.ams.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class HttpClientUtil {
    private HttpClientUtil() {}
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);
    
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
    
    public static boolean downloadResource(String location, OutputStream destination)
        throws IOException
    {
        URL url;
        HttpURLConnection conn = null;
        url = new URL(location);
        // It may redirect to AWS, so account for that
        boolean searching = true;
        boolean found = false;
        while (searching) {
            LOGGER.info("Downloading resource from {}", url);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0...");
            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_OK:
                    searching = false;
                    found = true;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    LOGGER.error("Resource not found at {}.", location);
                    searching = false;
                    found = false;
                    break;
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case 307: // Temporary redirect
                    location = conn.getHeaderField("Location");
                    url = new URL(url, location);  // Deal with relative URLs
                    LOGGER.info("{} redirected to {}", url);
                    break;
                default:
                    LOGGER.error("Unexpected HTTP response {} downloading resource from {}.", conn.getResponseCode(), location);
                    searching = false;
                    found = false;
                    break;
            }
        }
        if (found && conn != null) {
            InputStream is = null;
            try {
                is = conn.getInputStream();
                IOUtils.copy(is, destination);
                return true;
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(destination);
            }
        } else {
            return false;
        }
    }
}
