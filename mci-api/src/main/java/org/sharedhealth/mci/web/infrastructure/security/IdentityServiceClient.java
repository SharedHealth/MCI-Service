package org.sharedhealth.mci.web.infrastructure.security;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Component
public class IdentityServiceClient {
    private AsyncRestTemplate mciRestTemplate;
    private MCIProperties mciProperties;

    @Autowired
    public IdentityServiceClient(@Qualifier("MCIRestTemplate") AsyncRestTemplate mciRestTemplate,
                                 MCIProperties mciProperties) {
        this.mciRestTemplate = mciRestTemplate;
        this.mciProperties = mciProperties;
    }

    public TokenAuthentication authenticate(String token) throws AuthenticationException, ExecutionException,
            InterruptedException {

        //temporarily ignored - till MCI Admin begins sending the token
        if(TokenAuthenticationFilter.TEMPORARY_TOKEN.equals(token))
            return new TokenAuthentication(buildTemporaryUserInfo(), token);

        String userInfoUrl = mciProperties.getIdentityServerBaseUrl() + token;
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = mciRestTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY, UserInfo.class);
        ResponseEntity<UserInfo> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        UserInfo userInfo = responseEntity.getBody();
        return new TokenAuthentication(userInfo, token);
    }

    private UserInfo buildTemporaryUserInfo() {
        ArrayList<String> roles = new ArrayList<>();
        roles.add("ROLE_MCI_USER");
        return new UserInfo("TEMPORARY", roles);}
}
