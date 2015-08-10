package org.sharedhealth.mci.domain.util;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DateStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) {
        final String trimmedValue = getTrimmedValue(jp);
        final String dateString = DateUtil.toIsoMillisFormat(trimmedValue);

        return (dateString == null) ? trimmedValue : dateString;
    }

    private String getTrimmedValue(JsonParser jp) {
        try {
            final String text = jp.getText();
            return (text != null) ? text.trim() : null;
        } catch (IOException e) {
            return null;
        }
    }
}