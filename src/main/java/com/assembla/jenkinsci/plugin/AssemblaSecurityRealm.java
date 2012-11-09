
package com.assembla.jenkinsci.plugin;

import com.assembla.jenkinsci.plugin.api.ApiService;
import com.assembla.jenkinsci.plugin.api.TokenAssembla;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;

/**
 * hudson.security.SecurityRealm implementation for Assembla oAuth.
 * See Assembla authorization more info at:
 * http://api-doc.assembla.com/content/authentication.html#web_service
 * 
 * @author Damir Milovic
 */
public class AssemblaSecurityRealm extends SecurityRealm {
    
    private static final String REFERER_ATTRIBUTE = AssemblaSecurityRealm.class.getName() + ".referer";
    private static final Logger LOGGER = Logger.getLogger(AssemblaSecurityRealm.class.getName());
    
    private String apiUri;
    private String clientID;
    private String clientSecret;
    private String spaceId;

    @DataBoundConstructor
    public AssemblaSecurityRealm(String apiUri, String clientID, String clientSecret, String spaceId) {
        super();
        LOGGER.log(Level.FINE, "AssemblaSecurityRealm(apiUri, clientID, clientSecret, spaceId) ");
        this.apiUri = Util.fixEmptyAndTrim(apiUri);
        this.clientID = Util.fixEmptyAndTrim(clientID);
        this.clientSecret = Util.fixEmptyAndTrim(clientSecret);
        this.spaceId = Util.fixEmptyAndTrim(spaceId);
        
    }

    private AssemblaSecurityRealm() {
        LOGGER.log(Level.FINE, "AssemblaSecurityRealm()");
    }

    /**
     * @param apiUri the apiUri to set
     */
    private void setApiUri(String apiUri) {
        this.apiUri = apiUri;
    }

    /**
     * @param clientID the clientID to set
     */
    private void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    private void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
/**
     * @param spaceId the clientSecret to set
     */
    private void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public static final class ConverterImpl implements Converter {

        public boolean canConvert(Class type) {
            return type == AssemblaSecurityRealm.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer,
                MarshallingContext context) {

            AssemblaSecurityRealm realm = (AssemblaSecurityRealm) source;

            writer.startNode("apiUri");
            writer.setValue(realm.getApiUri());
            writer.endNode();

            writer.startNode("clientID");
            writer.setValue(realm.getClientID());
            writer.endNode();

            writer.startNode("clientSecret");
            writer.setValue(realm.getClientSecret());
            writer.endNode();
            //DACHA
            writer.startNode("spaceId");
            writer.setValue(realm.getSpaceId());
            writer.endNode();
            

        }

        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {

            String node = reader.getNodeName();

            reader.moveDown();

            AssemblaSecurityRealm realm = new AssemblaSecurityRealm();

            node = reader.getNodeName();

            String value = reader.getValue();

            setValue(realm, node, value);

            reader.moveUp();

            reader.moveDown();

            node = reader.getNodeName();

            value = reader.getValue();

            setValue(realm, node, value);

            reader.moveUp();
            
            if (reader.hasMoreChildren()) {
                reader.moveDown();

                node = reader.getNodeName();

                value = reader.getValue();

                setValue(realm, node, value);

                reader.moveUp();
            }
            // Added for spaceId (space name)
            if (reader.hasMoreChildren()) {
                reader.moveDown();

                node = reader.getNodeName();

                value = reader.getValue();

                setValue(realm, node, value);

                reader.moveUp();
            }
            return realm;
        }

        private void setValue(AssemblaSecurityRealm realm, String node,
                String value) {

            if (node.equalsIgnoreCase("clientid")) {
                realm.setClientID(value);
            } else if (node.equalsIgnoreCase("clientsecret")) {
                realm.setClientSecret(value);
            } else if (node.equalsIgnoreCase("apiUri")) {
                realm.setApiUri(value);
            } else if (node.equalsIgnoreCase("spaceid")) {
                realm.setSpaceId(value);
            } else {
                throw new ConversionException("invalid node value = " + node);
            }

        }
    }

    /**
     * @return the uri to Assembla API.
     */
    public String getApiUri() {

        return apiUri;
    }

    /**
     * @return the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }
    
    public String getSpaceId(){
        return spaceId;
    }

    /**
     * Redirect to Assembla oAuth request by authorization type = code.
     * @param request
     * @param referer
     * @return
     * @throws IOException 
     */
    public HttpResponse doCommenceLogin(StaplerRequest request, @Header("Referer") final String referer)
            throws IOException {

        request.getSession().setAttribute(REFERER_ATTRIBUTE, referer);

        return new HttpRedirect(ApiService.createAuthorizationCodeURL(clientID));
    }

    /**
     * This is where the user comes back to at the end of the oAuth redirect
     * ping-pong.
     */
    public HttpResponse doFinishLogin(StaplerRequest request)
            throws IOException {
        String code = request.getParameter("code");

        if (code == null || code.trim().length() == 0) {
            LOGGER.log(Level.SEVERE, "doFinishLogin() code = null");
            return HttpResponses.redirectToContextRoot();
        }
        TokenAssembla tokensAssembla = ApiService.getTokenByAuthorizationCode(code, clientID, clientSecret);
        if (tokensAssembla != null && tokensAssembla.access_token != null && tokensAssembla.access_token.trim().length() > 0) {
            AssemblaAuthenticationToken auth = new AssemblaAuthenticationToken(tokensAssembla, spaceId);
            SecurityContextHolder.getContext().setAuthentication(auth);
            User u = User.current();
            u.setFullName(auth.getName());
        } else {
            LOGGER.log(Level.SEVERE, "doFinishLogin() accessToken = null");
        }

        return HttpResponses.redirectToContextRoot();   // referer should be always there, but be defensive
    }



    /*
     * (non-Javadoc)
     * 
     * @see hudson.security.SecurityRealm#allowsSignup()
     */
    @Override
    public boolean allowsSignup() {
        return false;
    }

    @Override
    public SecurityRealm.SecurityComponents createSecurityComponents() {

        return new SecurityRealm.SecurityComponents(new AuthenticationManager() {
            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {
                if (authentication instanceof AssemblaAuthenticationToken ) {
                    return authentication;
                }
    
                throw new BadCredentialsException(
                        "Unexpected authentication type: " + authentication);
            }
        }, new UserDetailsService() {
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException, DataAccessException {
                throw new UsernameNotFoundException(username);
            }
        });
    }

    @Override
    public String getLoginUrl() {
        return "securityRealm/commenceLogin";
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        @Override
        public String getHelpFile() {
            return "/plugin/assembla-oauth/help/help-security-realm.html";
        }

        @Override
        public String getDisplayName() {
            return "Assembla Authentication Plugin";
        }

        public DescriptorImpl() {
            super();
        }

        public DescriptorImpl(Class<? extends SecurityRealm> clazz) {
            super(clazz);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        UserDetails result = null;
        AssemblaAuthenticationToken authToken = (AssemblaAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authToken == null) {
            throw new UsernameNotFoundException("AssemblaAuthenticationToken = null, no known user: " + username);
        }
        result = authToken.getUserByUsername(username);
        if (result == null) {
            throw new UsernameNotFoundException("User does not exist for login: " + username);
        }
        return result;
    }
    
    @Override
    public GroupDetails loadGroupByGroupname(String groupName){
        throw new UsernameNotFoundException("groups not supported");
    }
    
}

