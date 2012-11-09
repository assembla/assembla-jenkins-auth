package com.assembla.jenkinsci.plugin.api;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

/**
 * Represents Assembla API entity User. It extends org.acegisecurity.userdetails.UserDetails.
 *
 * @author Damir Milovic
 */
public class UserAssembla implements UserDetails {
    // Only id, login and name values are required from Assembla API.
    public String id;
    public String login;
    public String name;
    
    // Preserve required parameters populated by ApiService during session -----
    private transient String spaceId;
    private transient String role; // set by user roles
    private transient int assembla_permission = AssemblaPermission.NONE; // defualt

    public int getAssembla_permission() {
        return assembla_permission;
    }

    public void setAssembla_permission(int assembla_permission) {
        this.assembla_permission = assembla_permission;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // UserDetails implementation ----------------------------------------------
    
    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[]{};
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return login;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
