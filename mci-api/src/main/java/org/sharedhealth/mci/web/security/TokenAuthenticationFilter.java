package org.sharedhealth.mci.web.security;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.StringUtils.isEmpty;


public class TokenAuthenticationFilter extends GenericFilterBean {
    public static final String TEMPORARY_TOKEN = "TEMPORARY_TOKEN";
    private AuthenticationManager authenticationManager;
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String token = httpRequest.getHeader(MCIProperties.SECURITY_TOKEN_HEADER);

        //temporarily ignored - till MCI Admin begins sending the token
        if (isEmpty(token)) {
            token = TEMPORARY_TOKEN;
            //uncomment following when MCI Admin begins sending the token
//            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not provided");
//            return;
        }

        logger.debug("Authenticating token: {}", token);
        try {
            processTokenAuthentication(token);
            chain.doFilter(request, response);

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }

    private void processTokenAuthentication(String token) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(token, null);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Unable to authenticate provided token");
        }
        logger.debug("User successfully authenticated");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
