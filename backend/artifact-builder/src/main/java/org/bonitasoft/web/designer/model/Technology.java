package org.bonitasoft.web.designer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Technology {
    WEB_COMPONENT,
    ANGULARJS;

    @JsonCreator
    public static Technology fromJson(String text) {
        return valueOf(text.toUpperCase(Locale.ENGLISH));
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
