package com.assembla.jenkinsci.plugin.api;

/**
 * Represents response from https://api.assembla.com/token?grant_type=authorization_code&code=_authorization_code
 * 
 * @author Damir Milovic
 */
public class TokenAssembla {

    public String token_type;
    public int expires_in;
    public String access_token;
    public String refresh_token;
    
    // Preserve Client ID and secret (needed for refresh token API)
    public transient String clientId;
    public transient String clientSecret;
}
