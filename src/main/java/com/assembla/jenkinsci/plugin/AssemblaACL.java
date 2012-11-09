package com.assembla.jenkinsci.plugin;

import com.assembla.jenkinsci.plugin.api.ApiService;
import com.assembla.jenkinsci.plugin.api.AssemblaPermission;
import com.assembla.jenkinsci.plugin.api.SpaceAssembla;
import com.assembla.jenkinsci.plugin.api.UserRoleAssembla;
import hudson.security.ACL;
import hudson.security.Permission;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

/**
 * hudson.security.ACL implementation. Used for checking authenticated user
 * permissions. Jenkins permissions depends on user permission in Assembla
 * space. Since for every web request Jenkins is checking permission for several
 * items Assembla API calls have to be reduced. If last API calls are older than
 * USER_API_CALL_CACHE_TIME then API calls are invoked again,
 *
 * @see getAssemblaPermissionFromAPI(). Otherwise existing permission is used.
 *
 * @author Damir Milovic
 */
public class AssemblaACL extends ACL {

    private static final Logger LOG = Logger.getLogger(AssemblaACL.class.getName());
    // Cache time for userId from last time API calls.
    private static final long USER_API_CALL_CACHE_TIME = 20000; // 20 sec
    private final List<String> adminUserNameList;

    /**
     * Note: Constructor is called just once when plugin is setup (save or apply). It is not
     * invoked again when restarting plugin!
     */
    public AssemblaACL(String adminUserNames) {
        super();
        LOG.fine("**************** AssemblaACL() ****************");
        adminUserNameList = new LinkedList<String>();

        String[] parts = adminUserNames.split(",");

        for (String part : parts) {
            adminUserNameList.add(part.trim());
        }
    }

    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        boolean result = false;
//        LOG.log(Level.FINE, "hasPermission()? id: {0}", permission.getId());
//        LOG.log(Level.SEVERE, "{0}, owner: {1}", new Object[]{permission.toString(), permission.owner.toString()});
            
        String authenticatedUserName = a.getName();
        if (adminUserNameList.contains(authenticatedUserName)) {
            // if he/she is admin then give him/her all permissions.
            // It applies to both authentication types: UsernamePasswordAuthenticationToken and AssemblaAuthenticationToken
            // UsernamePasswordAuthenticationToken is used when run external script with (username/API key)
            result = true;
            
        } else if (a instanceof AssemblaAuthenticationToken) {
            AssemblaAuthenticationToken aat = (AssemblaAuthenticationToken) a;
            int assemblaPermission = getAssemblaPermissionFromAPI(aat);
            switch (assemblaPermission) {
                case AssemblaPermission.ALL:
                    // Jenkins All permissions
                    result = true;
                    break;
                case AssemblaPermission.EDIT:
                    // by default all permission except ADMIN
                    result = true;
                    if(permission.getId().equals("hudson.model.Hudson.Administer")){
//                        LOG.log(Level.SEVERE, " - - - " + permission.toString());
                        result = false;
                    }
                    
                    
                    break;
                case AssemblaPermission.VIEW:
                    // Jenkins read permissions
                    result = isReadPermission(permission);
                    break;
                case AssemblaPermission.NONE:
                default:
                    result = false;
            }
        } else if (a instanceof UsernamePasswordAuthenticationToken) {
            if (authenticatedUserName.equals(SYSTEM.getPrincipal())) {
                // give system user full access
                result = true;
            }
        }
        return result;
    }

    private int getAssemblaPermissionFromAPI(AssemblaAuthenticationToken aat) {

        Calendar currentCal = Calendar.getInstance();

        if ((currentCal.getTimeInMillis() - aat.getLastTimeApiCall()) > USER_API_CALL_CACHE_TIME) {
            // reload data from Assembla API
            LOG.fine("getAssemblaPermissionFromAPI() - - Reloading user data from Assembla API ...");
            aat.getUser().setRole(null); // reset role (if user is not space member)
            try {

                // 1. refresh token just in case 15 mins inactivity
                String access_token = ApiService.postRefreshAccessToken(
                        aat.getTokensAssembla().refresh_token,
                        aat.getTokensAssembla().clientId,
                        aat.getTokensAssembla().clientSecret);
                aat.getTokensAssembla().access_token = access_token; // set new access token.

                // 2. get space data (need relation between role and permission)
                SpaceAssembla space = ApiService.getSpace(aat.getTokensAssembla().access_token, aat.getUser().getSpaceId());

                // 3. get user roles in selected space (need to determine role for authenticated user)
                UserRoleAssembla[] userRoles = ApiService.getUserRoles(aat.getTokensAssembla().access_token, aat.getUser().getSpaceId());
                if (userRoles != null) {
                    for (UserRoleAssembla ur : userRoles) {
                        if (aat.getUser().id.equals(ur.user_id)) {
                            aat.getUser().setRole(ur.role);
                            break;
                        }
                    }
                }
                aat.getUser().setAssembla_permission(AssemblaPermission.getPermission(aat.getUser().getRole(), space));
                // set new time
                aat.setLastTimeApiCall(currentCal.getTimeInMillis());
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "getAssemblaPermissionFromAPI()", e);
                aat.getUser().setAssembla_permission(AssemblaPermission.NONE); // clear permission
            }
        }


        return aat.getUser().getAssembla_permission();
    }

    private boolean isReadPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Read")
                || permission.getId().equals("hudson.model.Item.Read")) {
              
       
            return true;
        } else {
            return false;
        }
    }

    public List<String> getAdminUserNameList() {
        return adminUserNameList;
    }
}
