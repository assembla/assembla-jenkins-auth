/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.assembla.jenkinsci.plugin.api;

/**
 * Utility class to determine Assembla permission based on user role in selected space.
 * @author Damir Milovic
 */
public class AssemblaPermission {

    static public final int NONE = 0;
    static public final int VIEW = 1;
    static public final int EDIT = 2;
    static public final int ALL = 3;

    static public int getPermission(String role, SpaceAssembla space) {
        int result = NONE;
        if (role != null && space != null) {
            if (role.equalsIgnoreCase(ApiService.ASSEMBLA_ROLE_OWNER)) {
                // owner always has ALL permission
                result = ALL;
            } else if (role.equalsIgnoreCase(ApiService.ASSEMBLA_ROLE_MEMBER)) {

                result = space.team_permissions;
            } else if (role.equalsIgnoreCase(ApiService.ASSEMBLA_ROLE_WATCHER)) {
                // owner always has ALL permission
                result = space.watcher_permissions;
            }
        }

        return result;
    }
}
