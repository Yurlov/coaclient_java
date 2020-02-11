package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.exception.TokenNotGeneratedException;
import com.coursera.oauth2_0.model.CourseraOAuth2Config;
import com.coursera.oauth2_0.util.CourseraOAuth2Constants;
import com.coursera.oauth2_0.util.CourseraOAuth2FileUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
/**
 * Implementation of service for managing Coursera authentication tokens
 *
 * @author Viktor Yurlov
 */
public class CourseraOAuth2ServiceImpl implements CourseraOAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(CourseraOAuth2ServiceImpl.class);

    private static RestTemplate restTemplate = new RestTemplate();

    private HttpServer server = null;

    /**
     * Add new client config
     *
     * @param clientName Client name
     * @param clientId Client id
     * @param clientSecret Client secret key
     * @param scopes Scopes of request
     * @throws CreateClientAppException if any error occured in process
     */
    public void addClient(String clientName,
                          String clientId,
                          String clientSecret,
                          String scopes) throws CreateClientAppException {

        if (StringUtils.isEmpty(clientName) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
            throw new CreateClientAppException("Invalid parameters");
        }

        if (StringUtils.isEmpty(scopes)) {
            scopes = CourseraOAuth2Constants.SCOPE_VIEW_PROFILE;
        } else if (scopes.equals(CourseraOAuth2Constants.SCOPE_ACCESS_BUSINESS)) {
            scopes = CourseraOAuth2Constants.SCOPE_VIEW_PROFILE + "+" + CourseraOAuth2Constants.SCOPE_ACCESS_BUSINESS;
        } else {
            throw new CreateClientAppException(
                    "Scope is invalid: " + scopes + ". Valid scope are 'view_profile' or 'access_business_api'");
        }
        CourseraOAuth2FileUtils.writeClientConfigToFile(clientName, clientId, clientSecret, scopes);
        logger.info("Client {} successfully added.", clientName);
    }

    /**
     * Delete client config by client name
     *
     * @param clientName Client name
     */
    public void deleteClient(String clientName) {
        CourseraOAuth2FileUtils.deleteClientConfig(clientName);
    }

    /**
     * Generate authentication tokens
     *
     * @param clientName Client name
     * @throws TokenNotGeneratedException if any error occured in process
     */
    public void generateOAuth2Tokens(String clientName) throws TokenNotGeneratedException {
        CourseraOAuth2Config config = CourseraOAuth2FileUtils.getClientConfigByNameOrId(clientName);
        if (config != null) {
            String courseraCodeURI = String.format(CourseraOAuth2Constants.COURSERA_CODE_URI,
                    config.getClientScope(),
                    CourseraOAuth2Constants.COURSERA_CALLBACK_URI + config.getClientId(),
                    config.getClientId());

            startServerCallbackListener();
            try {
                Desktop desktop = java.awt.Desktop.getDesktop();
                URI oURL = new URI(courseraCodeURI);
                desktop.browse(oURL);
            } catch (Exception e) {
                logger.error("Error open desktop browser.");
                server.stop(0);
            }
        } else {
            throw new TokenNotGeneratedException("Failed to generate new tokens: " + clientName + " config not found.");
        }
    }

    /**
     * Get client authentication tokens
     *
     * @param clientName Client name
     * @return Map of authentication tokens
     */
    public Map<String, String> getAuthTokens(String clientName) {
        return CourseraOAuth2FileUtils.getAuthTokensFromFile(clientName);
    }

    /**
     * Get client access token
     *
     * @param clientName Client name
     * @return Access token
     */
    public String getAccessToken(String clientName) {
        Map<String, String> authTokens = getAuthTokens(clientName);
        if (!authTokens.isEmpty()) {
            if (Long.parseLong(authTokens.get(CourseraOAuth2Constants.EXPIRES_IN)) < System.currentTimeMillis()) {
                logger.info("Access token is expired. Start generating new one.");
                return refreshAccessToken(authTokens, clientName);
            }
            return authTokens.get(CourseraOAuth2Constants.ACCESS_TOKEN_KEY);
        } else {
            return null;
        }
    }

    /**
     * Get list of client config
     *
     * @return Client config entities
     */
    public List<CourseraOAuth2Config> getClients() {
        return CourseraOAuth2FileUtils.getClientsFromConfigFile();
    }

    private String refreshAccessToken(Map<String,String> authTokens, String clientName) {
        CourseraOAuth2Config config = CourseraOAuth2FileUtils.getClientConfigByNameOrId(clientName);
        if (config == null) {
            logger.error("Client config not found. Please add configuration.");
            return null;
        }

        MultiValueMap<String, String> tokenRequestPayload =
                getCourseraTokenRequestPayload(
                        CourseraOAuth2Constants.REFRESH_TOKEN_KEY,
                        config.getClientId(),
                        config.getClientSecretKey());

        tokenRequestPayload.add(
                CourseraOAuth2Constants.REFRESH_TOKEN_KEY, authTokens.get(CourseraOAuth2Constants.REFRESH_TOKEN_KEY));

        String courseraAccessToken = null;
        Long expiredTime = null;
        try {
            HttpEntity<Object> tokenRequestEntity = getCourseraTokenRequestEntity(tokenRequestPayload);
            logger.info("Sending request for refresh access token: {} {} {}",
                    HttpMethod.POST,
                    CourseraOAuth2Constants.COURSERA_AUTH_TOKEN_URI,
                    tokenRequestEntity);

            ResponseEntity<String> courseraTokenApiResponse = restTemplate.exchange(
                    CourseraOAuth2Constants.COURSERA_AUTH_TOKEN_URI,
                    HttpMethod.POST,
                    tokenRequestEntity,
                    String.class);

            courseraAccessToken = getCourseraRequiredToken(
                    courseraTokenApiResponse,
                    CourseraOAuth2Constants.ACCESS_TOKEN_KEY);

            int expiredIn = Integer.parseInt(getCourseraRequiredToken(
                    courseraTokenApiResponse,
                    CourseraOAuth2Constants.EXPIRES_IN));
            expiredTime = System.currentTimeMillis() + (expiredIn * 1000);
        } catch (RestClientResponseException ex) {
            String accessTokenException = String.format(
                    "New access token is not generated using refresh token: %s", ex.getResponseBodyAsString());
            logger.error(accessTokenException);
        }
        if (courseraAccessToken != null) {
            CourseraOAuth2FileUtils.saveAuthTokens(
                    clientName,
                    authTokens.get(CourseraOAuth2Constants.REFRESH_TOKEN_KEY),
                    courseraAccessToken,
                    String.valueOf(expiredTime));
        }
        return courseraAccessToken;
    }

    private static MultiValueMap<String, String> getCourseraTokenRequestPayload(String grantType,
                                                                                String clientId,
                                                                                String secretKey) {
        MultiValueMap<String, String> tokenRequestPayload = new LinkedMultiValueMap<>();
        tokenRequestPayload.add(CourseraOAuth2Constants.GRANT_TYPE_KEY, grantType);
        tokenRequestPayload.add(CourseraOAuth2Constants.CLIENT_ID_KEY, clientId);
        tokenRequestPayload.add(CourseraOAuth2Constants.CLIENT_SECRET_KEY, secretKey);
        return tokenRequestPayload;
    }

    private static HttpEntity<Object> getCourseraTokenRequestEntity(MultiValueMap<String, String> tokenRequestPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity(tokenRequestPayload, headers);
    }

    private static String getCourseraRequiredToken(ResponseEntity<String> courseraTokenApiResponse, String token) {
        String courseraTokenResponseBody = courseraTokenApiResponse.getBody();
        JSONObject courseraTokenJsonObj = new JSONObject(courseraTokenResponseBody);
        if (token.equals(CourseraOAuth2Constants.EXPIRES_IN)) {
            return String.valueOf(courseraTokenJsonObj.get(token));
        }
        return (String) courseraTokenJsonObj.get(token);
    }

    public void startServerCallbackListener() throws TokenNotGeneratedException {
        try {
            if (server != null) {
                logger.info("Server already is running");
            } else {
                logger.info("Server is starting on port: {}", CourseraOAuth2Constants.PORT);
                server = HttpServer.create(new InetSocketAddress(CourseraOAuth2Constants.PORT), 0);
                server.createContext("/", new CodeCallbackHandler());
                server.setExecutor(Executors.newFixedThreadPool(1));
                server.start();
            }
        } catch (IOException e) {
            throw new TokenNotGeneratedException(
                    "Could not initialize HTTP Server on port :" +
                            CourseraOAuth2Constants.PORT + ", " + e.getMessage());
        }
    }

    public void stopServerCallbackListener() {
        if (server != null) {
            logger.info("Server is shutdown...");
            server.stop(0);
        }
    }

    private class CodeCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
            URI requestURI = t.getRequestURI();
            String paramSeparator = "=";
            String rawQuery = requestURI.getRawQuery();
            String[] params = rawQuery.split("&");
            String[] clientIdPair = params[0].split(paramSeparator);
            String[] codePair = params[1].split(paramSeparator);

            if (!StringUtils.isEmpty(clientIdPair[1]) && !StringUtils.isEmpty(codePair[1])) {
                logger.info("Code from Coursera: {}", codePair[1]);
                try {
                    sendAuthTokensRequest(clientIdPair[1], codePair[1]);
                } catch (TokenNotGeneratedException e) {
                    logger.error(e.getMessage());
                }
            } else {
                logger.error("Code is not generated, check client id and secret key. " +
                        "Make sure you are logged to the right Coursera account.");
            }
        }

        private void sendAuthTokensRequest(String clientId, String courseraCode) throws TokenNotGeneratedException {
            CourseraOAuth2Config config = CourseraOAuth2FileUtils.getClientConfigByNameOrId(clientId);
            if (config == null) {
                throw new TokenNotGeneratedException("Client config not found. Please add configuration.");
            }

            MultiValueMap<String, String> tokenRequestPayload = getCourseraTokenRequestPayload(
                    CourseraOAuth2Constants.AUTHORIZATION_CODE_VALUE,
                    config.getClientId(),
                    config.getClientSecretKey());

            tokenRequestPayload.add(CourseraOAuth2Constants.CODE_KEY, courseraCode);
            tokenRequestPayload.add(
                    CourseraOAuth2Constants.REDIRECT_URI_KEY,
                    CourseraOAuth2Constants.COURSERA_CALLBACK_URI + config.getClientId());
            tokenRequestPayload.add(CourseraOAuth2Constants.ACCESS_TYPE_KEY, CourseraOAuth2Constants.ACCESS_TYPE_VALUE);

            String refreshToken = null;
            String accessToken = null;
            Long expiredTime = null;
            try {
                ResponseEntity<String> courseraTokenApiResponse = restTemplate.exchange(
                        CourseraOAuth2Constants.COURSERA_AUTH_TOKEN_URI,
                        HttpMethod.POST,
                        getCourseraTokenRequestEntity(tokenRequestPayload),
                        String.class);

                refreshToken =
                        getCourseraRequiredToken(courseraTokenApiResponse, CourseraOAuth2Constants.REFRESH_TOKEN_KEY);
                accessToken =
                        getCourseraRequiredToken(courseraTokenApiResponse, CourseraOAuth2Constants.ACCESS_TOKEN_KEY);

                int expiredIn = Integer.parseInt(
                        getCourseraRequiredToken(courseraTokenApiResponse, CourseraOAuth2Constants.EXPIRES_IN));
                expiredTime = System.currentTimeMillis() + (expiredIn * 1000);
            } catch (RestClientResponseException ex) {
                String tokenException = String.format(
                        "Coursera auth tokens are not generated : %s", ex.getResponseBodyAsString());
                logger.error(tokenException);
            }

            if (!StringUtils.isEmpty(refreshToken) && !StringUtils.isEmpty(accessToken)) {
                CourseraOAuth2FileUtils.saveAuthTokens(
                        config.getClientName(),
                        refreshToken,
                        accessToken,
                        String.valueOf(expiredTime));
                logger.info("Auth tokens successfully saved to file.");
            }
        }
    }
}
