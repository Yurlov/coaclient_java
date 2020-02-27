package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.exception.TokenNotGeneratedException;
import com.coursera.oauth2_0.model.AuthTokens;
import com.coursera.oauth2_0.model.ClientConfig;
import com.coursera.oauth2_0.util.FileOAuth2Utils;
import com.coursera.oauth2_0.util.CourseraOAuth2ServiceType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FileOAuth2Utils.class})
public class FileOAuth2ServiceTest {

    private CourseraOAuth2Service service;

    @Before
    public void init() {
        spy(FileOAuth2Utils.class);
        service = CourseraOAuth2ServiceFactory.getInstance(CourseraOAuth2ServiceType.FILE);
    }

    @Test
    public void testAddClient() throws Exception {
        doNothing().when(FileOAuth2Utils.class,
                "writeClientConfigToFile",
                anyString(), anyString(), anyString(), anySet());
        Set<String> scopes = new HashSet<>();
        scopes.add("access_business_api");
        service.addClientConfig("test", "test","test", scopes);
        verifyStatic();
    }

    @Test(expected = CreateClientAppException.class)
    public void testAddClientShouldThrowExceptionIfInvalidParameters() throws CreateClientAppException {
        service.addClientConfig(anyString(), anyString(), anyString(), anySet());
    }

    @Test(expected = CreateClientAppException.class)
    public void testAddClientShouldThrowExceptionIfScopeInvalid() throws CreateClientAppException {
        Set<String> scopes = new HashSet<>();
        scopes.add("invalidScope");
        service.addClientConfig("testName", "testId", "testSecret", scopes);
    }

    @Test
    public void testDeleteClient() throws Exception {
        doNothing().when(FileOAuth2Utils.class, "deleteClientConfig", anyString());
        service.deleteClientConfig(anyString());
        verifyStatic();
    }

    @Test(expected = TokenNotGeneratedException.class)
    public void testGenerateOAuth2Tokens() throws Exception {
        when(FileOAuth2Utils.getClientConfigByNameOrId(anyString())).thenReturn(null);
        service.generateAuthTokens(anyString());
    }

    @Test(expected = TokenNotGeneratedException.class)
    public void testGenerateOAuth2TokensShouldThrowExceptionIfConfigNotFound() throws TokenNotGeneratedException {
        service.generateAuthTokens(anyString());
    }

    @Test
    public void testGetAuthTokens() {
        AuthTokens authTokens = getTestTokens();
        when(FileOAuth2Utils.getAuthTokensFromFile(anyString())).thenReturn(authTokens);
        assertEquals(authTokens, service.getAuthTokens(anyString()));
        verifyStatic();
    }

    @Test
    public void testGetAccessToken() {
        when(FileOAuth2Utils.getAuthTokensFromFile(anyString())).thenReturn(getTestTokens());
        assertEquals("testAccessToken", service.getAccessToken(anyString()));
        verifyStatic();
    }

    @Test
    public void testGetClients() {
        ClientConfig config = new ClientConfig(
                "testName",
                "testId",
                "testSecret",
                "testScope");
        when(FileOAuth2Utils.getClientConfigsFromConfigFile()).thenReturn(Arrays.asList(config));
        assertEquals(1, service.getClientConfigs().size());
        assertEquals(config.getClientName(), service.getClientConfigs().get(0).getClientName());
        assertEquals(config.getClientId(), service.getClientConfigs().get(0).getClientId());
        assertEquals(config.getClientSecretKey(), service.getClientConfigs().get(0).getClientSecretKey());
        assertEquals(config.getClientScope(), service.getClientConfigs().get(0).getClientScope());
        verifyStatic();
    }

    private AuthTokens getTestTokens() {
        return new AuthTokens(
                "testRefreshToken",
                "testAccessToken",
                String.valueOf(System.currentTimeMillis() + (6 * 1000)));
    }
}