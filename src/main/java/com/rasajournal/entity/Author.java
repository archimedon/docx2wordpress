package com.rasajournal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    @JsonProperty(value = "ID")
    private String id;

    @JsonProperty(value = "user_email")
    private String userEmail;

    @JsonProperty(value = "display_name")
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override public String toString() {
	return this.displayName;
    }
}
