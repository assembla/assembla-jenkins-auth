package com.assembla.jenkinsci.plugin.api;

/**
 * Represents defined Assembla permissions for roles in selected space.
 * 
 * @author Damir Milovic
 */
public class SpaceAssembla {
    public String id;
    public String name;
    // Permissions by role (Owner has ALL permissions always)
    // see ApiService for permission types
    public Integer team_permissions; // member
    public Integer watcher_permissions; // watcher

}
