/*******************************************************************************
 * Copyright (c) 2013 Christopher Frost.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.codersbrew.gradle.demo.bundle.one;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import net.codersbrew.gradle.demo.bundle.one.internal.StandardPropertiesService;

/**
 * 
 * This class is thread safe
 */
public class Activator implements BundleActivator{

	private ServiceRegistration<PropertiesService> registerService = null;

	public void start(BundleContext bundleContext) throws Exception {
		PropertiesService propertiesService = new StandardPropertiesService(bundleContext);
		registerService = bundleContext.registerService(PropertiesService.class, propertiesService, null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		ServiceRegistration<PropertiesService> localRegisterService = this.registerService;
		if(localRegisterService != null){
			localRegisterService.unregister();
			this.registerService = null;
		}
	}

}
