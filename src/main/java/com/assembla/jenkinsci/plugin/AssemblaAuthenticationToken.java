package com.assembla.jenkinsci.plugin;

import com.assembla.jenkinsci.plugin.api.ApiService;
import com.assembla.jenkinsci.plugin.api.TokenAssembla;
import com.assembla.jenkinsci.plugin.api.UserAssembla;
import java.util.logging.Logger;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;

/**
 * org.acegisecurity.providers.AbstractAuthenticationToken implementation.
 *
 * @author Damir Milovic
 */
public class AssemblaAuthenticationToken extends AbstractAuthenticationToken {

    private static final Logger LOG = Logger.getLogger(AssemblaAuthenticationToken.class.getName());
    private UserAssembla user = null;
    private UserAssembla[] users;
    private TokenAssembla tokensAssembla;
    // used to check is API called recently to avoid calling Assembla API 
    // in every AssemblaACL.hasPermission() invocation
    private long lastTimeApiCall;

    public AssemblaAuthenticationToken(TokenAssembla tokensAssembla, String spaceId) {
        super(new GrantedAuthority[]{});
        LOG.fine("*************** AssemblaAuthenticationToken() *****************");
        this.tokensAssembla = tokensAssembla;
        lastTimeApiCall = 0;

        boolean authenticate = false;

        if (tokensAssembla != null) {
            user = ApiService.getUserByToken(tokensAssembla.access_token);
            
            // authenticate user even he is not space member (has no role in selected space)
            if(user != null){
                authenticate = true;
                user.setSpaceId(spaceId); // preserve space Id (or space wiki name)
            }
            
    
        }
        
        setAuthenticated(authenticate);

    }

    public long getLastTimeApiCall() {
        return lastTimeApiCall;
    }

    public void setLastTimeApiCall(long lastTimeApiCall) {
        this.lastTimeApiCall = lastTimeApiCall;
    }

    public TokenAssembla getTokensAssembla() {
        return tokensAssembla;
    }

    public UserAssembla getUser() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return (user != null) ? user.login : null;
    }

    @Override
    public String getName() {
        return (user != null) ? user.login : null;
    }

    public UserAssembla[] getUsers() {
        return users;
    }

    public UserAssembla getUserByUsername(String login) {
        UserAssembla result = null;
        for (UserAssembla u : users) {
            if (u.login.equalsIgnoreCase(login)) {
                result = u;
                break;
            }
        }
        return result;
    }
}
