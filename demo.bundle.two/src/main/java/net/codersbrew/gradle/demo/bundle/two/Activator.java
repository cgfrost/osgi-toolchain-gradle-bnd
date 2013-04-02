/*******************************************************************************
 * Copyright (c) 2013 Christopher Frost.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.codersbrew.gradle.demo.bundle.two;

import java.util.Dictionary;
import java.util.Hashtable;

import net.codersbrew.gradle.demo.bundle.one.PropertiesService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *	This class is ThreadSafe
 *
 */
public class Activator implements BundleActivator {

	private static final Logger log = LoggerFactory.getLogger(Activator.class);
	
	protected static final String APPLICATION_NAME = "Demo app";
	
	private static final String CONTENT_CONTEXT_PATH = "/content";
	
	private static final String RESOURCES_CONTEXT_PATH = "/resources";
		
	protected static String contextPath = null;
	
	private ServiceTracker<HttpService, HttpService> httpServiceTracker;
	
	private ServiceTracker<URLStreamHandlerService, URLStreamHandlerService> urlEncoderServiceTracker;
	
	private transient HttpService registeredHttpService = null;
	
	private transient boolean isRegisteredWithHttpService = false;
	
	private final Object lock = new Object();

	private BundleContext bundleContext;
	
	protected static PropertiesService service = null;

	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;
		
		ServiceReference<PropertiesService> serviceReference = this.bundleContext.getServiceReference(PropertiesService.class);
		Activator.service = this.bundleContext.getService(serviceReference);
		
		Activator.contextPath = this.bundleContext.getBundle().getHeaders().get("Web-ContextPath");
		this.httpServiceTracker = new ServiceTracker<HttpService, HttpService>(context, HttpService.class, new HttpServiceTrackerCustomizer(context));
		
		Filter createFilter = context.createFilter("(&(" + Constants.OBJECTCLASS + "=" + URLStreamHandlerService.class.getSimpleName() + ")(url.handler.protocol=webbundle))");
		this.urlEncoderServiceTracker = new ServiceTracker<URLStreamHandlerService, URLStreamHandlerService>(context, createFilter, new UrlEncoderServiceTrackerCustomizer(context));

		this.httpServiceTracker.open();
		this.urlEncoderServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		this.httpServiceTracker.close();
		this.urlEncoderServiceTracker.close();
	}
	
	private void registerWithHttpService(){
		synchronized (this.lock) {
			if(this.registeredHttpService != null){
				try {
					Dictionary<String, String> contentServletInitParams = new Hashtable<String, String>();
					contentServletInitParams.put(ContentServlet.CONTENT_SERVLET_PREFIX, "/WEB-INF/layouts");
					contentServletInitParams.put(ContentServlet.CONTENT_SERVLET_SUFFIX, ".html");
					this.registeredHttpService.registerServlet(Activator.contextPath, 							new IndexServlet(), 		null,	null);
					this.registeredHttpService.registerServlet(Activator.contextPath + CONTENT_CONTEXT_PATH, 	new ContentServlet(), contentServletInitParams,		null);
					this.registeredHttpService.registerServlet(Activator.contextPath + RESOURCES_CONTEXT_PATH, 	new ResourceServlet(), 					null,	null);
					this.isRegisteredWithHttpService = true;
					log.info("Demo web bundle registered to HttpService: " + Activator.contextPath);
				} catch (Exception e) {
					log.error("Failed to register Demo web bundle with HttpService", e);
					this.unRegisterWithHttpService();
				} 
			}
		}
	}
	
	private void unRegisterWithHttpService(){
		synchronized (this.lock) {
			if(this.registeredHttpService != null){
				this.doSafeUnregister(Activator.contextPath + Activator.CONTENT_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath + Activator.RESOURCES_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath);
			}
			this.isRegisteredWithHttpService = false;
			log.info("Demo web bundle unregistering from HttpService at " + Activator.contextPath);
		}
	}
	
	private void doSafeUnregister(String path){
		try{
			this.registeredHttpService.unregister(path);
		}catch(IllegalArgumentException e){
			log.warn("Failed to unregister '" + path + "' from HttpService");
		}
	}
	
	/**
	 * Tracker event handler for HttpService
	 */
	private class HttpServiceTrackerCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {

		private final BundleContext context;

		public HttpServiceTrackerCustomizer(BundleContext context) {
			this.context = context;
		}

		public HttpService addingService(ServiceReference<HttpService> reference) {
			HttpService service = this.context.getService(reference);
			if(urlEncoderServiceTracker.isEmpty() && !isRegisteredWithHttpService){
				registeredHttpService = service;
				registerWithHttpService();
			}
			return service;
		}
		
		public void modifiedService(ServiceReference<HttpService> reference, HttpService service) {
			// no-op
		}

		public void removedService(ServiceReference<HttpService> reference,	HttpService service) {
			if(registeredHttpService != null && service.equals(registeredHttpService)){
				unRegisterWithHttpService();
				registeredHttpService = null;
			}
		}
		
	}

	/**
	 * Tracker event handler for URLStreamHandlerService
	 */
	private class UrlEncoderServiceTrackerCustomizer implements ServiceTrackerCustomizer<URLStreamHandlerService, URLStreamHandlerService>{

		private final BundleContext context;

		public UrlEncoderServiceTrackerCustomizer(BundleContext context) {
			this.context = context;
		}
		
		public URLStreamHandlerService addingService(ServiceReference<URLStreamHandlerService> reference) {
			if(registeredHttpService != null){
				unRegisterWithHttpService();
			}
			return this.context.getService(reference);
		}
		
		public void modifiedService(ServiceReference<URLStreamHandlerService> reference, URLStreamHandlerService service) {
			// no-op
		}

		public void removedService(ServiceReference<URLStreamHandlerService> reference, URLStreamHandlerService service) {
			if(urlEncoderServiceTracker.isEmpty() && !isRegisteredWithHttpService && registeredHttpService != null){
				registerWithHttpService();
			}
		}
		
	}
	
}
