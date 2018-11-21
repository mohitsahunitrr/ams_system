package com.precisionhawk.ams.bean.orgconfig;

/**
 *
 * @author pchapman
 */
public class FieldValidation {
    
    public enum FieldType {
        date,
        dateTime,
        dimension,
        float32,
        float64,
        geoPoint,
        imagePosition,
        integer32,
        integer64,
        list,
        map,
        string,
        uuid
    }
    
    private String key;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    private FieldType type;
    public FieldType getType() {
        return type;
    }
    public void setType(FieldType type) {
        this.type = type;
    }

    private boolean enabled;
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean required;
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    private String defaultValue;
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String minValue;
    public String getMinValue() {
        return minValue;
    }
    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    private String maxValue;
    public String getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    private Integer minLength;
    public Integer getMinLength() {
        return minLength;
    }
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    private Integer maxLength;
    public Integer getMaxLength() {
        return maxLength;
    }
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    private String regex;
    public String getRegex() {
        return regex;
    }
    public void setRegex(String regex) {
        this.regex = regex;
    }

    private boolean listValuesOnly;
    public boolean isListValuesOnly() {
        return listValuesOnly;
    }
    public void setListValuesOnly(boolean listValuesOnly) {
        this.listValuesOnly = listValuesOnly;
    }
}
