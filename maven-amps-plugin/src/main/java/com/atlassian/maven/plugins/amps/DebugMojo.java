package com.atlassian.maven.plugins.amps;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoExecute;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;

/**
 * Debug the webapp
 */
@MojoGoal ("debug")
@MojoExecute (phase = "package")
@MojoRequiresDependencyResolution
public class DebugMojo extends RunMojo
{
    /**
     * port for debugging
     */
    @MojoParameter (expression = "${jvm.debug.port}", defaultValue = "5005")
    protected int jvmDebugPort;

    /**
     * Suspend when debugging
     */
    @MojoParameter (expression = "${jvm.debug.suspend}")
    protected boolean jvmDebugSuspend = false;


    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
    	String debugArgs = " -Xdebug -Xrunjdwp:transport=dt_socket,address=" +
    				    String.valueOf(jvmDebugPort) + ",suspend=" + (jvmDebugSuspend ? "y" : "n") + ",server=y ";
        
        // add the debug jvm args for the global config
        if (jvmArgs == null)
        {
            jvmArgs = "-Xmx512m -XX:MaxPermSize=160m";
        }
        jvmArgs += debugArgs;

        // add the debug jvm args for each of the product configs
        for (Product product : products)
        {
            if (product.getJvmArgs() == null)
            {
                product.setJvmArgs("-Xmx512m -XX:MaxPermSize=160m");
            }
            product.setJvmArgs(product.getJvmArgs() + debugArgs);
        }
        
        if (writePropertiesToFile)
        {
            properties.put("debug.port", String.valueOf(jvmDebugPort));
        }

        if (ProductHandlerFactory.FECRU.equals(getDefaultProductId()) && debugNotSet()) {
            String message = "You must set the ATLAS_OPTS environment variable to the following string:'" + jvmArgs + "' when calling atlas-debug to enable Fisheye/Crucible debugging.";
            getLog().error(message);            
            throw new MojoFailureException(message);
        }

        super.doExecute();
    }

    private boolean debugNotSet()
    {
        String atlasOpts = System.getenv("ATLAS_OPTS");
        return atlasOpts == null || !atlasOpts.contains("-Xdebug");
    }
}
