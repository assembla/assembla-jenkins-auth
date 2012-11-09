package com.assembla.jenkinsci.plugin.api;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * ApiService class provides helper static methods for accessing Assembla API.
 * More info about Assembla API can be found here http://api-doc.assembla.com/
 * 
 * @author Damir Milovic
 */
public class ApiService {

    static public final String ASSEMBLA_ROLE_OWNER = "owner";
    static public final String ASSEMBLA_ROLE_MEMBER = "member";
    static public final String ASSEMBLA_ROLE_WATCHER = "watcher";
    static private final Logger LOGGER = Logger.getLogger(ApiService.class.getName());
    static private final String API_HOST_ASSEMBLA = "https://api.assembla.com";
    static private final String API_VERSION = "v1";
    static private final String URL_ASSEMBLA_API_VERSION = API_HOST_ASSEMBLA + "/" + API_VERSION;
    static private final String CONTENT_TYPE_JSON = "application/json";
    // Authentication URLs
    static private final String URL_COMMENCE_LOGIN = API_HOST_ASSEMBLA + "/authorization?response_type=code&client_id="; // application id
    static private final String URL_POST_TOKEN_BY_AUTHORIZATION_CODE = API_HOST_ASSEMBLA + "/token?grant_type=authorization_code&code="; // code returned by commence login
    static private final String URL_POST_REFRESH_TOKEN = API_HOST_ASSEMBLA + "/token?grant_type=refresh_token&refresh_token=%s";
    // API methods URLs
    static private final String URL_GET_USER_BY_TOKEN = URL_ASSEMBLA_API_VERSION + "/user.json";
    static private final String URL_GET_SPACE_USERS = URL_ASSEMBLA_API_VERSION + "/spaces/%s/users.json";
    static private final String URL_GET_SPACE_USER_ROLES = URL_ASSEMBLA_API_VERSION + "/spaces/%s/user_roles.json";
    static private final String URL_GET_SPACE = URL_ASSEMBLA_API_VERSION + "/spaces/%s.json"; // space_id

    /**
     * Creates Assembla URL authorization by code using provided clientId.
     *
     * @param clientId - application ID for Jenkins instance
     * @return URL used for getting authorization by code.
     */
    static public String createAuthorizationCodeURL(String clientId) {
        LOGGER.log(Level.FINER, "getCommencLoginUrl() clientId = " + clientId);
        return URL_COMMENCE_LOGIN + clientId;
    }


    /**
     * HTTP post request to API using clientId and secret authentication.
     * @param url API method url
     * @param clientId
     * @param clientSecret
     * @return String representing response from API
     * @throws IOException
     * @throws AuthenticationException 
     */
    static private String httpPostAuthenticated(String url, String clientId, String clientSecret) throws IOException, AuthenticationException {
        String result = null;
        HttpPost httpost = new HttpPost(url);
        // Basic preemtive authorization, found at:
        // http://stackoverflow.com/questions/2014700/preemptive-basic-authentication-with-apache-httpclient-4
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(clientId, clientSecret);
        Header authenticateHeader = new BasicScheme().authenticate(credentials, httpost);
        httpost.addHeader(authenticateHeader);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        result = EntityUtils.toString(entity);
        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
        LOGGER.log(Level.FINER, "HTTP POST  content = " + result);
        return result;
    }

    /**
     * After getting the code request for access (and refresh) token
     *
     * @param code
     * @param clientId
     * @param clientSecret
     * @return TokenAssembla - response includes refresh_token and access_token
     */
    static public TokenAssembla getTokenByAuthorizationCode(String code, String clientId, String clientSecret) {
        TokenAssembla result = null;
        String url = URL_POST_TOKEN_BY_AUTHORIZATION_CODE + code;
        LOGGER.log(Level.FINER, "finishLoginGetAccessToken() URL = " + url);
        try {
            String content = httpPostAuthenticated(url, clientId, clientSecret);
            // JSON parse
            Gson gson = new Gson();
            result = gson.fromJson(content, TokenAssembla.class);
            // preserve for further API refresh token calls;
            result.clientId = clientId;
            result.clientSecret = clientSecret;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Refresh access token (avoid expiration)
     *
     * @param refresh_token
     * @param clientId
     * @param clientSecret
     * @return String new access_token
     */
    static public String postRefreshAccessToken(String refresh_token, String clientId, String clientSecret) {
        String url = String.format(URL_POST_REFRESH_TOKEN, refresh_token);
        String accessToken = null;
        LOGGER.log(Level.FINER, "postRefreshAccessToken() URL = " + url);
        try {
            String content = httpPostAuthenticated(url, clientId, clientSecret);
            // JSON parse
            Gson gson = new Gson();
            RefreshTokenResponse refreshTokenresponse = gson.fromJson(content, RefreshTokenResponse.class);
            accessToken = refreshTokenresponse.access_token;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return accessToken;
    }

    static public UserAssembla getUserByToken(String access_token) {
        LOGGER.log(Level.FINER, "getUserByToken() URL = " + URL_GET_USER_BY_TOKEN);
        UserAssembla user = null;
        try {
            String content = httpGet(URL_GET_USER_BY_TOKEN, access_token);
            LOGGER.log(Level.FINER, "content = {0}", content);
            Gson gson = new Gson();
            user = gson.fromJson(content, UserAssembla.class);

        } catch (Exception ex) {
            Logger.getLogger(ApiService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    static public UserAssembla[] getSpaceUsers(String access_token, String spaceId) {
        String url = String.format(URL_GET_SPACE_USERS, spaceId);
        LOGGER.log(Level.FINER, "getSpaceUsers() URL = {0}", url);
        UserAssembla[] users = null;
        try {
            String content = httpGet(url, access_token);
            Gson gson = new Gson();
            users = gson.fromJson(content, UserAssembla[].class);
            LOGGER.log(Level.FINER, "content users count: {0}", users.length);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return users;
    }

    static public SpaceAssembla getSpace(String access_token, String spaceId) {
        String url = String.format(URL_GET_SPACE, spaceId);
        LOGGER.log(Level.FINER, "getSpace() URL = {0}", url);
        SpaceAssembla result = null;
        try {
            String content = httpGet(url, access_token);
            LOGGER.log(Level.FINER, "getSpace() content = {0}", content);
            Gson gson = new Gson();
            result = gson.fromJson(content, SpaceAssembla.class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    static public UserRoleAssembla[] getUserRoles(String access_token, String spaceId) {
        String url = String.format(URL_GET_SPACE_USER_ROLES, spaceId);
        LOGGER.log(Level.FINER, "getUserRoles() URL = {0}", url);
        UserRoleAssembla[] result = null;
        String content = null;
        try {
            content = httpGet(url, access_token);
            LOGGER.log(Level.FINER, "getUserRoles() content = {0}", content);
            Gson gson = new Gson();
            result = gson.fromJson(content, UserRoleAssembla[].class);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "getUserRoles() content = "+content +"\n"+ ex.toString());
        }
        return result;
    }

    /**
     * Invokes HTTP GET request to Assembla API.
     *
     * @param url
     * @param access_token
     * @return String representing response from API (content)
     * @throws IOException
     */
    static private String httpGet(String url, String access_token) throws IOException {
        String result = null;
        HttpGet httpGet = new HttpGet(url);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpGet.addHeader("Authorization", "Bearer " + access_token);
        httpGet.addHeader("Content-type", CONTENT_TYPE_JSON);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        result = EntityUtils.toString(httpEntity);
        httpClient.getConnectionManager().shutdown();
        return result;
    }
}
