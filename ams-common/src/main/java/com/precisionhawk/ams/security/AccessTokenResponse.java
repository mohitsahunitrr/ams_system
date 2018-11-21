/*
 * All rights reserved.
 */

package com.precisionhawk.ams.security;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @link https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-protocols-oauth-service-to-service
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
/*
Example from Microsoft:
{
    "expires_in": "3599",
    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkZTaW11RnJGTm9DMHNKWEdtdjEzbk5aY2VEYyIsImtpZCI6IkZTaW11RnJGTm9DMHNKWEdtdjEzbk5aY2VEYyJ9.eyJhdWQiOiIxNzcyMjQ0OS1lZjJlLTRhZGMtYWQxZC04YjdlYWQzMzRlYzkiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80YjA3MDQzZi00MGNlLTQ2NGQtODcxNS04ZTJhNGZkOGQ3ZDEvIiwiaWF0IjoxNTIyMjY2Mjc1LCJuYmYiOjE1MjIyNjYyNzUsImV4cCI6MTUyMjI3MDE3NSwiYWlvIjoiWTJOZ1lPaWY4SDNodmRtYjVadCttTW43ZHIyNUJBQT0iLCJhcHBpZCI6Ijk1N2ZjMzljLWVjMWYtNGU3Yy1hM2ZiLTVhZDU1NDNiYTc3NSIsImFwcGlkYWNyIjoiMSIsImlkcCI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzRiMDcwNDNmLTQwY2UtNDY0ZC04NzE1LThlMmE0ZmQ4ZDdkMS8iLCJvaWQiOiIwYWQ1ZjNmYi00MjQ5LTQxZjYtOGU1ZC0xZWY0Nzg2MzZiMmIiLCJzdWIiOiIwYWQ1ZjNmYi00MjQ5LTQxZjYtOGU1ZC0xZWY0Nzg2MzZiMmIiLCJ0aWQiOiI0YjA3MDQzZi00MGNlLTQ2NGQtODcxNS04ZTJhNGZkOGQ3ZDEiLCJ1dGkiOiI5Yi1FdkxrR21VZUZCNlZZWkVFV0FBIiwidmVyIjoiMS4wIn0.gqINh-e4hkdewCzLA7QZUD8jT91pUElYzEiooi9vSaB5GfHPRMfRwD4HI2zYulMreUJyCWGhDtkk-3Hlaxa0d1Wr4gjNIX9LwwtDcwGYXF2xAI-15x-z6jguL69_PBBc28X-YQ3z8GWALzDjStt5hC9Bx575MHR19XcHSj62X43VCfqjdSTS-pIBUROHCxMOl8Ur8-edr8jxjz-5LRjbB7NAfkZAvZF8dTwjDLPSaqS1ARD947w3kJahYmMIFi2cMeY6OARa80OTh23HxQTsGFGu_BLbhMBO_m8fRmxbGZGT-QOd6nuwMVZCgdguwkvoojZ0FVsy8uOMGYg3FoLvew",
    "ext_expires_in": "0",
    "token_type": "Bearer",
    "resource": "17722449-ef2e-4adc-ad1d-8b7ead334ec9",
    "not_before": "1522266275",
    "expires_on": "1522270175"
}
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenResponse {

    /**
     * The requested access token. The calling web service can use this token to authenticate to the
     * receiving web service.
     */
    @JsonProperty("access_token")
    private String accessToken;
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Indicates the token type value. The only type that Azure AD supports is Bearer. For more
     * information about bearer tokens, see The OAuth 2.0 Authorization Framework: Bearer Token Usage
     * (RFC 6750).
     * @link http://www.rfc-editor.org/rfc/rfc6750.txt
     */
    @JsonProperty("token_type")
    private String tokenType;
    public String getTokenType() {
        return tokenType;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /** How long the access token is valid (in seconds). */
    @JsonProperty("expires_in")
    private Long expiresIn;
    public Long getExpiresIn() {
        return expiresIn;
    }
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * The time when the access token expires. The date is represented as the number of seconds from
     * 1970-01-01T0:0:0Z UTC until the expiration time. This value is used to determine the lifetime
     * of cached tokens.
     */
    @JsonProperty("expires_on")
    private Long expiresOn;
    public Long getExpiresOn() {
        return expiresOn;
    }
    public void setExpiresOn(Long expiresOn) {
        this.expiresOn = expiresOn;
    }

    /**
     * The time from which the access token becomes usable. The date is represented as the number of
     * seconds from 1970-01-01T0:0:0Z UTC until time of validity for the token.
     */
    @JsonProperty("not_before")
    private Long notBefore;
    public Long getNotBefore() {
        return notBefore;
    }
    public void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    /** The App ID URI of the receiving web service. */
    private String resource;
    public String getResource() {
        return resource;
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
}
