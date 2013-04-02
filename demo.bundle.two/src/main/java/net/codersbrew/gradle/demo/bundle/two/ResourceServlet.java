/*******************************************************************************
 * Copyright (c) 2013 Christopher Frost.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.codersbrew.gradle.demo.bundle.two;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.codersbrew.ant.demo.bundle.two.internal.GZIPResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special servlet to load static resources and render the admin HTML pages
 * 
 * @author Jeremy Grelle
 * @author Scott Andrews
 * @author Christopher Frost
 */
public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String HTTP_CONTENT_LENGTH_HEADER = "Content-Length";

	private static final String HTTP_LAST_MODIFIED_HEADER = "Last-Modified";

	private static final String HTTP_EXPIRES_HEADER = "Expires";

	private static final String HTTP_CACHE_CONTROL_HEADER = "Cache-Control";

	private static final Logger log = LoggerFactory.getLogger(ResourceServlet.class);

	private final String protectedPath = "/?WEB-INF/.*";

	private String jarPathPrefix = "META-INF";

	private boolean gzipEnabled = true;
	
	private int cacheTimeout = 31556926; //The number of seconds resources should be cached by the client. Zero disables caching, default is one year.
	
	private Set<String> allowedResourcePaths = new HashSet<String>();
	{
		allowedResourcePaths.add("/.*/.*css");
		allowedResourcePaths.add("/.*/.*gif");
		allowedResourcePaths.add("/.*/.*ico");
		allowedResourcePaths.add("/.*/.*jpeg");
		allowedResourcePaths.add("/.*/.*jpg");
		allowedResourcePaths.add("/.*/.*js");
		allowedResourcePaths.add("/.*/.*png");
		allowedResourcePaths.add("META-INF/.*/*css");
		allowedResourcePaths.add("META-INF/.*/*gif");
		allowedResourcePaths.add("META-INF/.*/*ico");
		allowedResourcePaths.add("META-INF/.*/*jpeg");
		allowedResourcePaths.add("META-INF/.*/*jpg");
		allowedResourcePaths.add("META-INF/.*/*js");
		allowedResourcePaths.add("META-INF/.*/*png");
	}

	private Map<String, String> defaultMimeTypes = new HashMap<String, String>();
	{
		defaultMimeTypes.put(".css", "text/css");
		defaultMimeTypes.put(".gif", "image/gif");
		defaultMimeTypes.put(".ico", "image/vnd.microsoft.icon");
		defaultMimeTypes.put(".jpeg", "image/jpeg");
		defaultMimeTypes.put(".jpg", "image/jpeg");
		defaultMimeTypes.put(".js", "text/javascript");
		defaultMimeTypes.put(".png", "image/png");
	}

	private Set<String> compressedMimeTypes = new HashSet<String>();
	{
		compressedMimeTypes.add("text/.*");
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rawRequestPath = request.getPathInfo();
		
		if (log.isDebugEnabled()) {
			log.debug("Attempting to GET resource: " + rawRequestPath);
		}
		
		URL[] resources = getRequestResourceURLs(request);
		if (resources == null || resources.length == 0) {
			if (log.isDebugEnabled()) {
				log.debug("Resource not found: " + rawRequestPath);
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		prepareResourcesResponse(response, resources, rawRequestPath);
		OutputStream out = selectOutputStream(request, response, resources, rawRequestPath);
		try {
			for (int i = 0; i < resources.length; i++) {
				URLConnection resourceConn = resources[i].openConnection();
				InputStream in = resourceConn.getInputStream();
				try {
					byte[] buffer = new byte[1024];
					int bytesRead = -1;
					while ((bytesRead = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
				} finally {
					in.close();
				}
			}
		} finally {
			out.close();
		}
	}

	private OutputStream selectOutputStream(final HttpServletRequest request, final HttpServletResponse response, final URL[] resources, final String rawResourcePath) throws IOException {
		String acceptEncoding = request.getHeader("Accept-Encoding");
		String mimeType;
		try {
			mimeType = response.getContentType();
		} catch(UnsupportedOperationException e){
			mimeType = getResponseMimeType(resources, rawResourcePath);
		}
		if (gzipEnabled && acceptEncoding != null && acceptEncoding.indexOf("gzip") > -1 && matchesCompressedMimeTypes(mimeType)) {
			log.debug("Enabling GZIP compression for the current response.");
			return new GZIPResponseStream(response);
		} else {
			return response.getOutputStream();
		}
	}

	private boolean matchesCompressedMimeTypes(String mimeType) {
		for(String compressedMimeType: compressedMimeTypes){
			if(mimeType.matches(compressedMimeType)){
				return true;
			}
		}
		return false;
	}
	
	private void prepareResourcesResponse(HttpServletResponse response, URL[] resources, String rawResourcePath) throws IOException {
		long lastModified = -1;
		int contentLength = 0;
		String mimeType = null;
		for (int i = 0; i < resources.length; i++) {
			URLConnection resourceConn = resources[i].openConnection();
			if (resourceConn.getLastModified() > lastModified) {
				lastModified = resourceConn.getLastModified();
			}
			mimeType = getMimeType(rawResourcePath, resources[i], mimeType);
			contentLength += resourceConn.getContentLength();
		}

		response.setContentType(mimeType);
		response.setHeader(HTTP_CONTENT_LENGTH_HEADER, Long.toString(contentLength));
		response.setDateHeader(HTTP_LAST_MODIFIED_HEADER, lastModified);
		if (cacheTimeout > 0) {
			configureCaching(response, cacheTimeout);
		}
	}
	
	
	private String getResponseMimeType(final URL[] resources, String rawResourcePath) throws IOException{
		String mimeType = null;
		for (int i = 0; i < resources.length; i++) {
			mimeType = getMimeType(rawResourcePath, resources[i], mimeType);
		}
		return mimeType;
	}
	
	private String getMimeType(final String rawResourcePath, final URL resource, String expectedMimeType) throws MalformedURLException{
		String extension = resource.getPath().substring(resource.getPath().lastIndexOf('.'));
		String currentMimeType = (String) defaultMimeTypes.get(extension);
		if (currentMimeType == null) {
			currentMimeType = getServletContext().getMimeType(resource.getPath());
		}
		if (expectedMimeType == null) {
			expectedMimeType = currentMimeType;
		} else if (!expectedMimeType.equals(currentMimeType)) {
			throw new MalformedURLException("Combined resource path: " + rawResourcePath + " is invalid. All resources in a combined resource path must be of the same mime type.");
		}
		return expectedMimeType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected long getLastModified(HttpServletRequest request) {
		if (log.isDebugEnabled()) {
			log.debug("Checking last modified of resource: " + request.getPathInfo());
		}
		URL[] resources;
		try {
			resources = getRequestResourceURLs(request);
		} catch (MalformedURLException e) {
			return -1;
		}

		if (resources == null || resources.length == 0) {
			return -1;
		}

		long lastModified = -1;

		for (int i = 0; i < resources.length; i++) {
			URLConnection resourceConn;
			try {
				resourceConn = resources[i].openConnection();
			} catch (IOException e) {
				return -1;
			}
			if (resourceConn.getLastModified() > lastModified) {
				lastModified = resourceConn.getLastModified();
			}
		}
		return lastModified;
	}

	private URL[] getRequestResourceURLs(HttpServletRequest request) throws MalformedURLException {

		String rawResourcePath = request.getPathInfo();
		String appendedPaths = request.getParameter("appended");
		if (appendedPaths != null && appendedPaths.length() < 0) {
			rawResourcePath = rawResourcePath + "," + appendedPaths;
		}
		String[] localResourcePaths = this.delimitedListToStringArray(rawResourcePath, ",");
		URL[] resources = new URL[localResourcePaths.length];
		for (int i = 0; i < localResourcePaths.length; i++) {
			String localResourcePath = localResourcePaths[i];
			if (!isAllowed(localResourcePath)) {
				if (log.isWarnEnabled()) {
					log.warn("An attempt to access a protected resource at " + localResourcePath + " was disallowed.");
				}
				return null;
			}
			URL resource = getServletContext().getResource(localResourcePath);
			if (resource == null) {
				resource = getJarResource(jarPathPrefix, localResourcePath);
			}
			if (resource == null) {
				if (resources.length > 1) {
					log.debug("Combined resource not found: " + localResourcePath);
				}
				return null;
			} else {
				resources[i] = resource;
			}
		}
		return resources;
	}

	private URL getJarResource(String jarPrefix, String resourcePath) {
		String jarResourcePath = jarPrefix + resourcePath;
		if (!isAllowed(jarResourcePath)) {
			if (log.isWarnEnabled()) {
				log.warn("An attempt to access a protected resource at " + jarResourcePath + " was disallowed.");
			}
			return null;
		}
		if (jarResourcePath.startsWith("/")) {
			jarResourcePath = jarResourcePath.substring(1);
		}
		if (log.isDebugEnabled()) {
			log.debug("Searching classpath for resource: " + jarResourcePath);
		}
		return getDefaultClassLoader().getResource(jarResourcePath);
	}
	
	/*
	 * TODO think I can delete this and just use the this classes classloader.
	 */
	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ResourceServlet.class.getClassLoader();
		}
		return cl;
	}

	private boolean isAllowed(String resourcePath) {
		if (resourcePath.matches(protectedPath)) {
			return false;
		}
		for(String allowedResourcePath: allowedResourcePaths){
			if(resourcePath.matches(allowedResourcePath)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Set HTTP headers to allow caching for the given number of seconds.
	 * @param seconds number of seconds into the future that the response should be cacheable for
	 */
	private void configureCaching(HttpServletResponse response, int seconds) {
		response.setDateHeader(HTTP_EXPIRES_HEADER, System.currentTimeMillis() + seconds * 1000L);// HTTP 1.0 header
		response.setHeader(HTTP_CACHE_CONTROL_HEADER, "max-age=" + seconds);// HTTP 1.1 header
	}
	
	private String[] delimitedListToStringArray(String str, String delimiter) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(str.substring(i, i + 1));
			}
		}
		else {
			int pos = 0;
			int delPos = 0;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(str.substring(pos, delPos));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(str.substring(pos));
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

}
