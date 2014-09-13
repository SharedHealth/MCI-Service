package org.sharedhealth.mci.web.handler;


import java.lang.Comparable;
import java.lang.String;

public class MCIError implements Comparable<MCIError>{

    private int code;

    private String field;

    private String message;

    public MCIError() {

    }

    public MCIError(int code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int compareTo(MCIError e) {
        return this.code > e.getCode() ? 1 : this.code < e.getCode() ? -1 : 0;
    }
}