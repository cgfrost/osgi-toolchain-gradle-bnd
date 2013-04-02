/*******************************************************************************
 * Copyright (c) 2013 Christopher Frost.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.codersbrew.gradle.demo.bundle.one.internal;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.codersbrew.gradle.demo.bundle.one.PropertiesService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 */
final public class StandardPropertiesService implements PropertiesService{

    private final Map<String, String> result;
    
    public StandardPropertiesService(BundleContext bundleContext) {
		this.result = new HashMap<String, String>();
		this.result.put("Operating System", bundleContext.getProperty(Constants.FRAMEWORK_OS_NAME) + ' ' + bundleContext.getProperty(Constants.FRAMEWORK_OS_VERSION));
		this.result.put("JVM", System.getProperty("java.vendor") + ' ' + System.getProperty("java.version"));
		this.result.put("Request Time", DateFormat.getInstance().format(new Date()));
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties(){
		this.result.put("Request Time", DateFormat.getInstance().format(new Date()));
        return this.result;
    }

}
