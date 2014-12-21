package org.sharedhealth.mci.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static java.lang.String.format;

public class JsonMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String writeValueAsString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(format("Error converting %s to string.", o), e);
        }
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(format("Error converting content string to %s.", valueType.toString()), e);
        }
    }
}
