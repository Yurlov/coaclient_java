package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.exception.TokenNotGeneratedException;
import com.coursera.oauth2_0.model.CourseraOAuth2Config;
import java.util.List;
import java.util.Map;

/**
 * Interface for managing Coursera OAuth2 API tokens
 *
 * @author Viktor Yurlov
 */
public interface CourseraOAuth2Service {
    /**
     * Add new client config
     *
     * @param clientName Client name
     * @param clientId Client id
     * @param clientSecret Client secret key
     * @param scope Scope of request
     * @throws CreateClientAppException if any error occured in process
     */
    void addClient(String clientName,
                   String clientId,
                   String clientSecret,
                   String scope) throws CreateClientAppException;

    /**
     * Delete client config by client name
     *
     * @param clientName Client name
     */
    void deleteClient(String clientName);

    /**
     * Generate authentication tokens
     *
     * @param clientName Client name
     * @throws TokenNotGeneratedException if any error occured in process
     */
    void generateOAuth2Tokens(String clientName) throws TokenNotGeneratedException;

    /**
     * Get client authentication tokens
     *
     * @param clientName Client name
     * @return Map of authentication tokens
     */
    Map<String, String> getAuthTokens(String clientName);

    /**
     * Get client access token
     *
     * @param clientName Client name
     * @return Access token
     */
    String getAccessToken(String clientName);

    /**
     * Get list of client config
     *
     * @return Client config entities
     */
    List<CourseraOAuth2Config> getClients();

    /**
     * Start server for listening callback
     *
     */
    void startServerCallbackListener() throws TokenNotGeneratedException;

    /**
     * Stop server for listening callback
     *
     */
    void stopServerCallbackListener();
}
