package org.sharedhealth.mci.web.infrastructure;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ExecutionException;

public class WebClient<T> {

    protected final MCIProperties properties;
    private final AsyncRestTemplate restTemplate;
    private Class<T> responseType;

    public WebClient(AsyncRestTemplate restTemplate, MCIProperties properties) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.responseType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected HttpEntity buildHeaders() {
        return new HttpEntity(new LinkedMultiValueMap<String, String>());
    }

    protected T getResponse(final String url) throws ExecutionException, InterruptedException {
        return new ListenableFutureAdapter<T, ResponseEntity<T>>(restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildHeaders(),
                this.responseType)) {

            @Override
            protected T adapt(ResponseEntity<T> result) throws ExecutionException {
                HttpStatus statusCode = result.getStatusCode();
                if (statusCode.is2xxSuccessful()) {
                    return result.getBody();
                }
                return null;
            }
        }.get();
    }
}
