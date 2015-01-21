package org.sharedhealth.mci.web.infrastructure.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.utils.concurrent.PreResolvedListenableFuture;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class IdentityServiceClientTest {
    @Mock
    AsyncRestTemplate asyncRestTemplate;
    @Mock
    MCIProperties mciProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCallIdentityServerToAuthenticate() throws Exception {
        String token = UUID.randomUUID().toString();
        when(mciProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, HttpMethod.GET, new HttpEntity(null),
                UserInfo.class)).thenReturn(createResponse(token, HttpStatus.OK));
        TokenAuthentication tokenAuthentication = new IdentityServiceClient(asyncRestTemplate,
                mciProperties).authenticate(token);

        assertEquals(tokenAuthentication.getPrincipal().toString(), token);
        assertEquals(tokenAuthentication.getName(), "bar");
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldFailIfIdentityServerGetFails() throws Exception {
        String token = UUID.randomUUID().toString();
        when(mciProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, HttpMethod.GET, new HttpEntity(null),
                UserInfo.class)).thenReturn(createResponse(token, HttpStatus.UNAUTHORIZED));
        new IdentityServiceClient(asyncRestTemplate,
                mciProperties).authenticate(token);
    }

    private ListenableFuture<ResponseEntity<UserInfo>> createResponse(String token, HttpStatus statusCode) {
        return new
                PreResolvedListenableFuture<>(new ResponseEntity<>(userInfo(token), statusCode));
    }

    private UserInfo userInfo(String token) {
        return new UserInfo("bar", Arrays.asList("MCI_USER", "SHR_USER"));
    }
}