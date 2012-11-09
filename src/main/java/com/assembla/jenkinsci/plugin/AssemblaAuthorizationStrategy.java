package com.assembla.jenkinsci.plugin;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Assembla implementation of hudson.security.AuthorizationStrategy
 * @author Damir Milovic
 */
public class AssemblaAuthorizationStrategy extends AuthorizationStrategy {

    private final AssemblaACL rootACL;

    @DataBoundConstructor
    public AssemblaAuthorizationStrategy(String adminUserNames) {
        rootACL = new AssemblaACL(adminUserNames);

    }

    @Override
    public ACL getRootACL() {
        return rootACL;
    }

    @Override
    public Collection<String> getGroups() {
        return new ArrayList<String>(0);
    }

    public String getAdminUserNames() {
        return StringUtils.join(rootACL.getAdminUserNameList().iterator(), ", ");
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<AuthorizationStrategy> {

        public String getDisplayName() {
            return "Assembla Authorization Strategy";
        }
		public String getHelpFile() {
			return "/plugin/assembla-oauth/help/help-authorization-strategy.html";
		}
    }
}
