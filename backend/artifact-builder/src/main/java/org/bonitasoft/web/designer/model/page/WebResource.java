package org.bonitasoft.web.designer.model.page;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

import java.util.HashSet;
import java.util.Set;
@EqualsAndHashCode
public class WebResource {

    private String verb;
    private String value;
    private Set<String> scopes = new HashSet<>();
    private boolean isAutomaticDetection = false;

    @JsonCreator
    public WebResource(@JsonProperty("verb") String verb, @JsonProperty("value") String value) {
        this.verb = verb;
        this.value = value;
    }

    public WebResource(String verb, String value, String scope) {
        this.verb = verb.toLowerCase();
        this.value = value;
        this.scopes.add(scope);
        this.isAutomaticDetection = true;
    }

    @JsonView({JsonViewPersistence.class})
    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb= verb;
    }

    @JsonView({JsonViewPersistence.class})
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public Set<String> getScopes() {
        return this.scopes;
    }

    public void addToScopes(String value) {
        this.scopes.add(value);
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String toDefinition(){
        return this.verb.toUpperCase().concat("|").concat(this.value);
    }


    @JsonView
    public boolean isAutomatic() {
        return this.isAutomaticDetection;
    }

    @JsonIgnore
    public void setAutomatic(boolean isAutomaticDetection) {
        this.isAutomaticDetection = isAutomaticDetection;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("verb", verb).append("value", value).
                append("isAutomaticDetection", isAutomaticDetection).append("scope", scopes.toString()).
                toString();
    }
}
