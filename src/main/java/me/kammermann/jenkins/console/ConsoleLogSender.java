package me.kammermann.jenkins.console;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


/**
 * {@link Builder} that add metadata to an artefact in the Nexus repository
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link ConsoleLogSender} is created. The created
 * instance is persisted to the project configuration XML by using
 * to remember the configuration.
 *
 * @author Marcel Birkner
 */
public class ConsoleLogSender extends BuildWrapper {

    private final Boolean send;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ConsoleLogSender(Boolean send) {
        this.send = send;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public Boolean getSend() {
        return send;
    }

    /**
     * Descriptor for {@link ConsoleLogSender}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/NexusMetadataBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String nexusUrl;
        private String nexusUser;
        private Secret nexusPassword;

        /**
         * Performs on-the-fly validation of the form field 'key'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a key");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the key too short?");
            return FormValidation.ok();
        }
        public FormValidation doCheckValue(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a value");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the key too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillColorMapNameItems() {
            ListBoxModel m = new ListBoxModel();
            m.add("test1");
            m.add("test2");
            m.add("test3");
            return m;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Send console over REST";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            nexusUrl = formData.getString("nexusUrl");
            nexusUser = formData.getString("nexusUser");
            nexusPassword = Secret.fromString( formData.getString("nexusPassword") );

            // Can also use req.bindJSON(this, formData);
            // (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         *  Nexus connection test
         */
        public FormValidation doTestConnection(
                @QueryParameter("nexusUrl") final String nexusUrl,
                @QueryParameter("nexusUser") final String nexusUser,
                @QueryParameter("nexusPassword") final String nexusPassword) throws IOException, ServletException {

            try {

                    return FormValidation.ok("Success. Connection with Nexus Repository verified.");

            } catch (Exception e) {
                System.out.println("Exception " + e.getMessage() );
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        public String getNexusUrl() {
            return nexusUrl;
        }
        public String getNexusUser() {
            return nexusUser;
        }
        public Secret getNexusPassword() {
            return nexusPassword;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }
}
