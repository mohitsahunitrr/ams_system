package com.precisionhawk.ams.support.httpclient;

import com.precisionhawk.ams.util.HttpClientUtil;
import com.precisionhawk.ams.webservices.client.Environment;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Philip A. Chapman
 */
public class HttpClientUtilities {
    
    private final static CloseableHttpClient CLIENT = HttpClients.createDefault();
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientUtilities.class);
    private static final String UPLOAD_URL = "%s/resource/%s/upload";
    
    private HttpClientUtilities() {}
    
    public static void postFile(Environment env, String resourceId, String contentType, File file) throws IOException, URISyntaxException
    {
        HttpPost httpPost = new HttpPost(uploadURL(env, resourceId));
        uploadFile(httpPost, env.obtainAccessToken(), contentType, file);
    }
    
    public static void putFile(Environment env, String resourceId, String contentType, File file) throws IOException, URISyntaxException
    {
        HttpPost httpPut = new HttpPost(uploadURL(env, resourceId));
        uploadFile(httpPut, env.obtainAccessToken(), contentType, file);
    }
    
    private static void uploadFile(HttpEntityEnclosingRequestBase req, String authJWT, String contentType, File file) throws IOException
    {
        try {
            req.setHeader("Authorization", authJWT);
            req.setHeader("content-disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            FileEntity content = new FileEntity(file, ContentType.create(contentType));
            req.setEntity(content);
            CloseableHttpResponse resp = CLIENT.execute(req);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT) {
                String cont = HttpClientUtil.consumeEntity(resp.getEntity());
                throw new IOException(String.format("Error uploading file %s response status: %d response: %s", file, statusCode, cont));
            }
        } finally {
            req.reset();
        }
    }
    
    private static URI uploadURL(Environment env, String resourceId) throws URISyntaxException {
        return new URI(String.format(UPLOAD_URL, env.getServiceURI(), resourceId));
    }
}
