package org.sharedhealth.mci.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;

public class FieldPropertyNameReader {

    public static String getFieldPropertyName(Class objectClass, String fieldName) {

        if (isNestedField(fieldName)) {
            return getNestedFieldPropertyName(objectClass, fieldName);
        }

        try {
            Field field = objectClass.getDeclaredField(fieldName);
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            return annotation.value();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return fieldName;
    }

    public static String getNestedFieldPropertyName(Class objectClass, String fieldName) {

        String name;

        HashMap<String, String> fieldParts = getFieldParts(fieldName);

        try {
            String rest = fieldParts.get("rest");
            String plainFieldName = getPlainFieldName(fieldParts.get("first"));

            Field field = objectClass.getDeclaredField(plainFieldName);
            name = getFieldPropertyName(objectClass, plainFieldName);

            if (field.getType() == List.class) {
                name = name + getFieldIndex(fieldParts.get("first"));
            }

            name = name + "." + getFieldPropertyNameByFieldObject(field, rest);

        } catch (NoSuchFieldException e) {
            name = fieldName;
            e.printStackTrace();
        }

        return name;
    }

    private static String getFieldPropertyNameByFieldObject(Field field, String propertyName) {

        Class<?> fieldType = field.getType();

        if (fieldType == List.class) {
            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            fieldType = (Class<?>) stringListType.getActualTypeArguments()[0];
        }

        return getFieldPropertyName(fieldType, propertyName);
    }


    private static String getPlainFieldName(String str) {

        if (str.contains("[")) {
            return str.substring(0, str.indexOf("["));
        }

        return str;
    }

    private static String getFieldIndex(String str) {
        return str.substring(str.indexOf("["));
    }

    private static HashMap<String, String> getFieldParts(String property) {
        int dotPosition = property.indexOf(".");

        HashMap<String, String> fieldParts = new HashMap<>();
        fieldParts.put("first", property.substring(0, dotPosition));
        fieldParts.put("rest", property.substring(dotPosition + 1));

        return fieldParts;
    }

    private static boolean isNestedField(String fieldName) {
        return fieldName.contains(".");
    }
}
