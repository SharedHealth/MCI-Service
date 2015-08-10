package org.sharedhealth.mci.web.infrastructure.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.ArrayList;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class IdentityServiceClientTest {
    @Mock
    AsyncRestTemplate asyncRestTemplate;
    @Mock
    MCIProperties mciProperties;
    @Mock
    private ClientAuthenticator clientAuthenticator;
    private HttpHeaders httpHeaders;
    private final String identityServerBaseUrl = "http://localhost:9997/token/";

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, "123");
        httpHeaders.add(AUTH_TOKEN_KEY, "xyz");

        when(mciProperties.getIdpClientId()).thenReturn("123");
        when(mciProperties.getIdpAuthToken()).thenReturn("xyz");
    }

    @Test
    public void shouldCallIdentityServerToAuthenticate() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");

        when(mciProperties.getIdentityServerBaseUrl()).thenReturn(identityServerBaseUrl);
        when(asyncRestTemplate.exchange(identityServerBaseUrl + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, OK));
        when(clientAuthenticator.authenticate(userAuthInfo, token, userInfo(token))).thenReturn(true);

        TokenAuthentication tokenAuthentication = new IdentityServiceClient(asyncRestTemplate,
                mciProperties, clientAuthenticator).authenticate(userAuthInfo, token);

        assertEquals(tokenAuthentication.getCredentials().toString(), token);
        UserInfo expectedUserInfo = userInfo(token);
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getProperties().getId(), expectedUserInfo.getProperties().getId());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getProperties().getAccessToken(), expectedUserInfo.getProperties().getAccessToken());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getProperties().getEmail(), expectedUserInfo.getProperties().getEmail());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getProperties().getIsActive(), expectedUserInfo.getProperties().getIsActive());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getProperties().isActivated(), expectedUserInfo.getProperties().isActivated());
        assertEquals(tokenAuthentication.getName(), expectedUserInfo.getProperties().getName());
        assertEquals(tokenAuthentication.getName(), "bar");
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldFailIfIdentityServerGetFails() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");

        when(mciProperties.getIdentityServerBaseUrl()).thenReturn(identityServerBaseUrl);
        when(asyncRestTemplate.exchange(identityServerBaseUrl + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, UNAUTHORIZED));
        new IdentityServiceClient(asyncRestTemplate,
                mciProperties, clientAuthenticator).authenticate(userAuthInfo, token);
    }

    @Test
    public void shouldCallClientAuthenticator() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");
        UserInfo userInfo = userInfo(token);

        when(mciProperties.getIdentityServerBaseUrl()).thenReturn(identityServerBaseUrl);
        when(asyncRestTemplate.exchange(identityServerBaseUrl + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, OK));
        when(clientAuthenticator.authenticate(userAuthInfo, token, userInfo)).thenReturn(true);

        new IdentityServiceClient(asyncRestTemplate,
                mciProperties, clientAuthenticator).authenticate(userAuthInfo, token);

        verify(clientAuthenticator, times(1)).authenticate(eq(userAuthInfo), eq(token), any(UserInfo.class));
    }

    private ListenableFuture<ResponseEntity<UserInfo>> createResponse(String token, HttpStatus statusCode) {
        SettableListenableFuture<ResponseEntity<UserInfo>> response = new
                SettableListenableFuture<>();
        response.set(new ResponseEntity<>(userInfo(token), statusCode));
        return response;
    }

    private UserInfo userInfo(String token) {
        return new UserInfo("123", "bar", "email@gmail.com", 1, true,
                token.toString(), new ArrayList<>(asList("MCI_ADMIN", "SHR_USER")), new ArrayList<UserProfile>());
    }
}