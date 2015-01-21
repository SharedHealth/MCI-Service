package org.sharedhealth.mci.web.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IdentityServiceClient identityServiceClient;

    public TokenAuthenticationProvider() {
    }

    public TokenAuthenticationProvider(IdentityServiceClient identityServiceClient) {
        this.identityServiceClient = identityServiceClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getPrincipal();
        try {
            return identityServiceClient.authenticate(token);
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid token or token expired");
        } catch (Exception ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}