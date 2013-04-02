/*******************************************************************************
 * Copyright (c) 2013 Christopher Frost.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.codersbrew.gradle.demo.bundle.two.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cgfrost
 *
 */
public class ContentURLFetcher {

	private static final Logger log = LoggerFactory.getLogger(ContentURLFetcher.class);

	private Set<String> protectedPaths = new HashSet<String>();
	{
		protectedPaths.add("/?WEB-INF/.*");
		protectedPaths.add(".*css");
		protectedPaths.add(".*gif");
		protectedPaths.add(".*ico");
		protectedPaths.add(".*jpeg");
		protectedPaths.add(".*jpg");
		protectedPaths.add(".*js");
		protectedPaths.add(".*png");
	}
	
	private String prefix = "";
	
	private String suffix = "";

	private final ServletContext context;
	
	/**
	 * 
	 * @param suffix
	 * @param prefix
	 */
	public ContentURLFetcher(ServletContext context, String prefix, String suffix) {
		this.context = context;
		this.prefix = prefix != null ?  prefix : "";
		this.suffix = suffix != null ?  suffix : "";
	}

	public URL getRequestedContentURL(String rawRequestPath) throws MalformedURLException {
		if (!isAllowed(rawRequestPath)) {
			if (log.isWarnEnabled()) {
				log.warn("An attempt to access protected content at " + rawRequestPath + " was disallowed.");
			}
			return null;
		}
		String localResourcePath = String.format("%s%s%s", this.prefix, rawRequestPath, this.suffix);
		
		URL resource = this.context.getResource(localResourcePath);
		if (resource == null) {
			if (log.isDebugEnabled()) {
				log.debug("Content not found: " + localResourcePath);
			}
		}
		return resource;
	}

	private boolean isAllowed(String resourcePath) {
		for(String protectedPath: protectedPaths){
			if(resourcePath.matches(protectedPath)){
				return false;
			}
		}
		return true;
	}
	
}
