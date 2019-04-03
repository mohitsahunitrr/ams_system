package com.precisionhawk.ams.bean.orgconfig;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pchapman
 */
public class TranslationView {
    private String key;
    private String header;
    private List<String> fields = new LinkedList();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
