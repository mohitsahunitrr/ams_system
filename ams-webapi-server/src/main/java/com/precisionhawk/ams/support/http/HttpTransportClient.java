/*
 * All rights reserved.
 */

package com.precisionhawk.ams.support.http;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class HttpTransportClient {
    
    public static final String MIME_JSON = "application/json";
    public static final String MIME_TEXT = "text/plain";
//    public static final HttpRequestFactory REQ_FACTORY = new ApacheHttpTransport().createRequestFactory();
    public static final HttpRequestFactory REQ_FACTORY = new NetHttpTransport().createRequestFactory();

    public static void delete(String url, final ResponseCallback callback)
        throws IOException
    {
        HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
        req.getHeaders().setAccept(MIME_JSON);
        req.setUnsuccessfulResponseHandler(new _HttpUnsuccessfulResponseHandler(callback));
        HttpResponse resp = req.execute();
        callback.receive(resp.getStatusCode(), loadContent(resp));
    }

    public static void getJSON(String url, final ResponseCallback callback)
        throws IOException
    {
        HttpRequest req = REQ_FACTORY.buildGetRequest(new GenericUrl(url));
        req.getHeaders().setAccept(MIME_JSON);
        req.setUnsuccessfulResponseHandler(new _HttpUnsuccessfulResponseHandler(callback));
        HttpResponse resp = req.execute();
        callback.receive(resp.getStatusCode(), loadContent(resp));
    }
        
    public static void postForm(String url, Map<String, String> data, final ResponseCallback callback)
        throws IOException
    {
        HttpContent content = new UrlEncodedContent(data);
        HttpRequest req = REQ_FACTORY.buildPostRequest(new GenericUrl(url), content);
        req.getHeaders().setAccept(MIME_JSON);
        req.setUnsuccessfulResponseHandler(new _HttpUnsuccessfulResponseHandler(callback));
        HttpResponse resp = req.execute();
        callback.receive(resp.getStatusCode(), loadContent(resp));
    }
    
    public static void postJSON(String url, String json, final ResponseCallback callback)
        throws IOException
    {
        HttpContent content = new ByteArrayContent("application/json", json.getBytes());
        HttpRequest req = REQ_FACTORY.buildPostRequest(new GenericUrl(url), content);
        req.getHeaders().setAccept(MIME_JSON);
        req.setUnsuccessfulResponseHandler(new _HttpUnsuccessfulResponseHandler(callback));
        HttpResponse resp = req.execute();
        callback.receive(resp.getStatusCode(), loadContent(resp));
    }
    
    public static void putJSON(String url, String json, final ResponseCallback callback)
        throws IOException
    {
        HttpContent content = new ByteArrayContent("application/json", json.getBytes());
        HttpRequest req = REQ_FACTORY.buildPutRequest(new GenericUrl(url), content);
        req.getHeaders().setAccept(MIME_JSON);
        req.setUnsuccessfulResponseHandler(new _HttpUnsuccessfulResponseHandler(callback));
        HttpResponse resp = req.execute();
        callback.receive(resp.getStatusCode(), loadContent(resp));
    }
    
    public static String loadContent(HttpResponse resp) throws IOException {
        if (resp.getContentType().startsWith(MIME_JSON)  || MIME_TEXT.equals(resp.getContentType())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = resp.getContent();
                IOUtils.copy(is, baos);
                baos.close();
                return new String(baos.toByteArray());
            } finally {
                IOUtils.closeQuietly(baos);
                IOUtils.closeQuietly(is);
            }
        }
        return "";
    }

    // Do not instantiate
    private HttpTransportClient() {}
}

class _HttpUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler
{    
    private ResponseCallback callback;

    _HttpUnsuccessfulResponseHandler(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean handleResponse(HttpRequest hr, HttpResponse hr1, boolean bln) throws IOException {
        Logger LOGGER = LoggerFactory.getLogger(getClass());
        LOGGER.error("Response code " + hr1.getStatusCode() + " for " + hr.getRequestMethod() + " to " + hr.getUrl());
        if (401 == hr1.getStatusCode()) {
            LOGGER.error("WWW-Authenticate: {}", hr1.getHeaders().get("WWW-Authenticate"));
        }
        String s = HttpTransportClient.loadContent(hr1);
        callback.receive(hr1.getStatusCode(), s);
        return true;
    }
}
