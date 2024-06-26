package org.jenkinsci.plugins.github.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import org.kohsuke.stapler.QueryParameter;

/**
 * Manages storing/retrieval of the shared secret for the hook.
 */
public class HookSecretConfig extends AbstractDescribableImpl<HookSecretConfig> {

    private String credentialsId;

    @DataBoundConstructor
    public HookSecretConfig(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    /**
     * Gets the currently used secret being used for payload verification.
     *
     * @return Current secret, null if not set.
     */
    @Nullable
    public Secret getHookSecret() {
        return GitHubServerConfig.secretFor(credentialsId).orNull();
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * @param credentialsId a new ID
     * @deprecated rather treat this field as final and use {@link GitHubPluginConfig#setHookSecretConfigs}
     */
    @Deprecated
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<HookSecretConfig> {

        @Override
        public String getDisplayName() {
            return "Hook secret configuration";
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.MANAGE)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }

            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            Jenkins.getInstance(),
                            StringCredentials.class,
                            Collections.<DomainRequirement>emptyList(),
                            CredentialsMatchers.always()
                    );
        }

        @NonNull
        @Override
        public Permission getRequiredGlobalConfigPagePermission() {
            return Jenkins.MANAGE;
        }
    }
}
