package com.assembla.jenkinsci.plugin.api;

/**
 * Represents Assembla API response when refreshing token.
 * 
 * @author Damir Milovic
 */
public class RefreshTokenResponse {

    public String token_type;
    public int expires_in;
    public String access_token;
}
