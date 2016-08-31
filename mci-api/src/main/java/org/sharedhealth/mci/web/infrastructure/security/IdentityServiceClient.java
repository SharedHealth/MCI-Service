package org.sharedhealth.mci.web.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sharedhealth.mci.domain.config.MCICacheConfiguration;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.utils.HttpUtil;
import org.sharedhealth.mci.web.model.IdentityStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.sharedhealth.mci.utils.HttpUtil.ACCESS_TOKEN_KEY;


@Component
public class IdentityServiceClient {
    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;
    private ClientAuthenticator clientAuthenticator;
    private IdentityStore identityStore;

    @Autowired
    public IdentityServiceClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciRestTemplate,
                                 MCIProperties mciProperties, ClientAuthenticator clientAuthenticator, IdentityStore identityStore) {
        this.mciRestTemplate = mciRestTemplate;
        this.mciProperties = mciProperties;
        this.clientAuthenticator = clientAuthenticator;
        this.identityStore = identityStore;
    }

    @Cacheable(value = MCICacheConfiguration.IDENTITY_CACHE, unless = "#result == null")
    public TokenAuthentication authenticate(UserAuthInfo userAuthInfo, String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = mciProperties.getIdentityServerUserInfoUrl() + token;
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

    public String getOrCreateToken() throws ExecutionException, InterruptedException, IOException {
        if (identityStore.hasIdentityToken()) {
            return identityStore.getIdentityToken();
        }
        String signInUrl = mciProperties.getIdentityServerSignInUrl();
        HttpEntity hrmSinginEntity = HttpUtil.getHrmSinginEntity(mciProperties);
        ListenableFuture<ResponseEntity<String>> listenableFuture = mciRestTemplate.exchange(signInUrl,
                HttpMethod.POST, hrmSinginEntity, String.class);
        ResponseEntity<String> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        Map map = new ObjectMapper().readValue(responseEntity.getBody(), Map.class);
        String token = (String) map.get(ACCESS_TOKEN_KEY);
        identityStore.setIdentityToken(token);
        return token;

    }

    public void clearToken() {
        identityStore.clearIdentityToken();
    }
}
