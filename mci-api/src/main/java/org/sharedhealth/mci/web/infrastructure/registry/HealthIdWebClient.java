package org.sharedhealth.mci.web.infrastructure.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class HealthIdWebClient {
    protected final MCIProperties properties;
    private final AsyncRestTemplate restTemplate;

    private static final String HEALTH_ID_LIST_KEY = "hids";

    @Autowired
    public HealthIdWebClient(MCIProperties properties, AsyncRestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public List getNextHealthIDs(String hidServiceNextBlockURL, HttpEntity<Object> requestEntity) throws Exception {
        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(hidServiceNextBlockURL,
                HttpMethod.GET, requestEntity, String.class);
        ResponseEntity<String> responseEntity = future.get();
        if (UNAUTHORIZED.equals(responseEntity.getStatusCode())) {
            throw new UnauthorizedException("invalid token");
        }
        String content = responseEntity.getBody();
        Map map = new ObjectMapper().readValue(content, Map.class);
        return (List) map.get(HEALTH_ID_LIST_KEY);
    }

    public String markUsed(String markUsedUrl, HttpEntity<Map<String, String>> requestEntity) throws Exception {
        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(markUsedUrl,
                HttpMethod.PUT, requestEntity, String.class);
        ResponseEntity<String> responseEntity = future.get();
        if (UNAUTHORIZED.equals(responseEntity.getStatusCode())) {
            throw new UnauthorizedException("invalid token");
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new Exception(String.format("Unexpected Response %s from HID Service", responseEntity.getStatusCode()));
        }
        return responseEntity.getBody();
    }

    public Map validateHID(String checkHIDUrl, HttpEntity<Object> httpEntity) throws Exception {
        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(checkHIDUrl,
                HttpMethod.GET, httpEntity, String.class);
        ResponseEntity<String> responseEntity = future.get();
        if (UNAUTHORIZED.equals(responseEntity.getStatusCode())) {
            throw new UnauthorizedException("invalid token");
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new Exception(String.format("Unexpected Response %s from HID Service", responseEntity.getStatusCode()));
        }
        String content = responseEntity.getBody();
        return new ObjectMapper().readValue(content, Map.class);
    }
}
