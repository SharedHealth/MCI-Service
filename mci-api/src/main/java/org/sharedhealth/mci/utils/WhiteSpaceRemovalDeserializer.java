package org.sharedhealth.mci.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class WhiteSpaceRemovalDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) {

        try {
            return jp.getText().trim();
        } catch (IOException e) {
            return null;
        }
    }
}