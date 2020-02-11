package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.exception.TokenNotGeneratedException;
import com.coursera.oauth2_0.model.CourseraOAuth2Config;
import com.coursera.oauth2_0.util.CourseraOAuth2Constants;
import com.coursera.oauth2_0.util.CourseraOAuth2FileUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseraOAuth2FileUtils.class})
public class CourseraOAuth2ServiceImplTest {

    private CourseraOAuth2Service service;

    @Before
    public void init() {
        spy(CourseraOAuth2FileUtils.class);
        service = new CourseraOAuth2ServiceImpl();
    }

    @Test
    public void testAddClient() throws Exception {
        doNothing().when(CourseraOAuth2FileUtils.class,
                "writeClientConfigToFile",
                anyString(), anyString(), anyString(), anyString());
        service.addClient("test", "test","test", "access_business_api");
        verifyStatic();
    }

    @Test(expected = CreateClientAppException.class)
    public void testAddClientShouldThrowExceptionIfInvalidParameters() throws CreateClientAppException {
        service.addClient(anyString(), anyString(), anyString(), anyString());
    }

    @Test(expected = CreateClientAppException.class)
    public void testAddClientShouldThrowExceptionIfScopeInvalid() throws CreateClientAppException {
        service.addClient("testName", "testId", "testSecret", "invalidScope");
    }

    @Test
    public void testDeleteClient() throws Exception {
        doNothing().when(CourseraOAuth2FileUtils.class, "deleteClientConfig", anyString());
        service.deleteClient(anyString());
        verifyStatic();
    }

    @Test(expected = TokenNotGeneratedException.class)
    public void testGenerateOAuth2Tokens() throws Exception {
        when(CourseraOAuth2FileUtils.getClientConfigByNameOrId(anyString())).thenReturn(null);
        service.generateOAuth2Tokens(anyString());
    }

    @Test(expected = TokenNotGeneratedException.class)
    public void testGenerateOAuth2TokensShouldThrowExceptionIfConfigNotFound() throws TokenNotGeneratedException {
        service.generateOAuth2Tokens(anyString());
    }

    @Test
    public void testGetAuthTokens() {
        Map<String, String> tokens = getTestTokens();
        when(CourseraOAuth2FileUtils.getAuthTokensFromFile(anyString())).thenReturn(tokens);
        assertEquals(tokens, service.getAuthTokens(anyString()));
        verifyStatic();
    }

    @Test
    public void testGetAccessToken() {
        when(CourseraOAuth2FileUtils.getAuthTokensFromFile(anyString())).thenReturn(getTestTokens());
        assertEquals("testAccessToken", service.getAccessToken(anyString()));
        verifyStatic();
    }

    @Test
    public void testGetClients() {
        CourseraOAuth2Config config = new CourseraOAuth2Config(
                "testName",
                "testId",
                "testSecret",
                "testScope");
        when(CourseraOAuth2FileUtils.getClientsFromConfigFile()).thenReturn(Arrays.asList(config));
        assertEquals(1, service.getClients().size());
        assertEquals(config.getClientName(), service.getClients().get(0).getClientName());
        assertEquals(config.getClientId(), service.getClients().get(0).getClientId());
        assertEquals(config.getClientSecretKey(), service.getClients().get(0).getClientSecretKey());
        assertEquals(config.getClientScope(), service.getClients().get(0).getClientScope());
        verifyStatic();
    }

    private Map<String, String> getTestTokens() {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("refresh_token", "testRefreshToken");
        tokens.put("access_token", "testAccessToken");
        tokens.put(CourseraOAuth2Constants.EXPIRES_IN, String.valueOf(System.currentTimeMillis() + (6 * 1000)));
        return tokens;
    }
}