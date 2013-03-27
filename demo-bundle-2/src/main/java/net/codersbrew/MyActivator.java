package net.codersbrew;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
* A sample OSGi bundle built with Gradle
*
* @author Christopher Frost
*/ 
public class MyActivator implements BundleActivator {

    public void start(BundleContext context) {
        System.out.println("Hello from a Groovy Gradle Activator");
    }

    public void stop(BundleContext context) { 
    }
}
