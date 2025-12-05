package com.ecoembes.webclient.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Session-scoped model holding user session data.
 * Represents the Model component in the Web Client architecture.
 */
@Component
@SessionScope
public class Model {

    private String token;
    private String currentURL;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCurrentURL() {
        return currentURL;
    }

    public void setCurrentURL(String currentURL) {
        this.currentURL = currentURL;
    }

    public boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }
}

