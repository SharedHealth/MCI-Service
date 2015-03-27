package org.sharedhealth.mci.web.infrastructure.security;

import org.junit.Test;

import javax.naming.AuthenticationException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class ClientAuthenticatorTest {

    @Test
    public void shouldAuthenticateUser() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";

        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email);
        UserInfo userInfo = getUserInfo(client_id, email, true, token);

        assertTrue(new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo));
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfClientIsNotActivated() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email);
        UserInfo userInfo = getUserInfo(client_id, email, false, token);

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfClientIdIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", email);
        UserInfo userInfo = getUserInfo("432", email, true, token);

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfTokenIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email);
        UserInfo userInfo = getUserInfo(clientId, email, true, "abc");

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfEmailIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email);
        UserInfo userInfo = getUserInfo(clientId, "abc@gmail.com", true, token);

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }


    private UserInfo getUserInfo(String id, String email, boolean activated, String xyz) {
        return new UserInfo(id, "foo", email, 1, activated, xyz,
                new ArrayList<String>(), new ArrayList<UserProfile>());
    }
}