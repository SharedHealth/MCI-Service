package org.sharedhealth.mci.web.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class ClientAuthenticator {
    public boolean authenticate(UserAuthInfo userAuthInfo, String token, UserInfo userInfo) throws AuthenticationException {
        String exceptionMessage = "User credentials is invalid";
        if (isInactiveUser(userInfo)) {
            throw new AuthenticationException(exceptionMessage);
        } else if (isInvalidToken(userInfo, token))
            throw new AuthenticationException(exceptionMessage);
        else if (isInvalidClient(userInfo, userAuthInfo))
            throw new AuthenticationException(exceptionMessage);
        else if (isInvalidEmail(userInfo, userAuthInfo))
            throw new AuthenticationException(exceptionMessage);

        return true;
    }

    private boolean isInvalidEmail(UserInfo userInfo, UserAuthInfo userAuthInfo) {
        return !userAuthInfo.getEmail().equals(userInfo
                .getProperties().getEmail());
    }

    private boolean isInvalidClient(UserInfo userInfo, UserAuthInfo userAuthInfo) {
        return !userAuthInfo.getClientId().equals(userInfo
                .getProperties().getId());
    }

    private boolean isInvalidToken(UserInfo userInfo, String token) {
        return !token.equals(userInfo.getProperties().getAccessToken());
    }

    private boolean isInactiveUser(UserInfo userInfo) {
        return !userInfo.getProperties().isActivated() || userInfo.getProperties().getIsActive() != 1;
    }
}
