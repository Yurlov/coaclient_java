package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.exception.TokenNotGeneratedException;
import com.coursera.oauth2_0.model.AuthTokens;
import com.coursera.oauth2_0.model.ClientConfig;
import java.util.List;
import java.util.Set;

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
    void addClientConfig(String clientName,
                         String clientId,
                         String clientSecret,
                         Set<String> scope) throws CreateClientAppException;

    /**
     * Delete client config by client name
     *
     * @param clientName Client name
     */
    void deleteClientConfig(String clientName);

    /**
     * Generate authentication tokens
     *
     * @param clientName Client name
     * @throws TokenNotGeneratedException if any error occured in process
     */
    void generateAuthTokens(String clientName) throws TokenNotGeneratedException;

    /**
     * Get client authentication tokens
     *
     * @param clientName Client name
     * @return Map of authentication tokens
     */
    AuthTokens getAuthTokens(String clientName);

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
    List<ClientConfig> getClientConfigs();

    /**
     * Stop server for listening callback
     *
     */
    void stopServerCallbackListener();

}
