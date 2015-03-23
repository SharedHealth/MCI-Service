package org.sharedhealth.mci.web.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class ClientAuthenticator {
    public boolean authenticate(UserAuthInfo userAuthInfo, String token, UserInfo userInfo) throws AuthenticationException {
        String message = null;
        if (!userInfo.getProperties().isActivated() || userInfo.getProperties().getIsActive() != 1) {
            message = "Client is not activated.";
        } else if (!token.equals(userInfo.getProperties().getAccessToken())) {
            message = "Token is invalid or expired.";
        } else if (!userAuthInfo.getClientId().equals(userInfo.getProperties().getId())) {
            message = "Client ID is invalid.";
        } else if (!userAuthInfo.getEmail().equals(userInfo.getProperties().getEmail())) {
            message = "Email is invalid.";
        }
        if (message != null) {
            throw new AuthenticationException(message);
        }
        return true;
    }
}
