package org.sharedhealth.mci.web.infrastructure.security;

import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.web.utils.URLParser.ensureEndsWithBackSlash;


@Component
public class IdentityServiceClient {
    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;
    private ClientAuthenticator clientAuthenticator;

    @Autowired
    public IdentityServiceClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciRestTemplate,
                                 MCIProperties mciProperties, ClientAuthenticator clientAuthenticator) {
        this.mciRestTemplate = mciRestTemplate;
        this.mciProperties = mciProperties;
        this.clientAuthenticator = clientAuthenticator;
    }

    public TokenAuthentication authenticate(UserAuthInfo userAuthInfo, String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = ensureEndsWithBackSlash(mciProperties.getIdentityServerBaseUrl()) + token;
        HttpHeaders httpHeaders = HttpUtil.getHrmIdentityHeaders(mciProperties);
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = mciRestTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                new HttpEntity(httpHeaders), UserInfo.class);
        ResponseEntity<UserInfo> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        UserInfo userInfo = responseEntity.getBody();
        boolean isAuthenticated = clientAuthenticator.authenticate(userAuthInfo, token, userInfo);
        return new TokenAuthentication(userInfo, isAuthenticated);
    }
}
