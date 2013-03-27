/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.samples.configuration.properties.core;

import java.util.HashMap;
import java.util.Map;

/**
 */
final class StandardPropertiesService implements PropertiesService{

    private final Map<String, String> result;
    
    public StandardPropertiesService(String driverClassName, String url, String username, String password) {
		this.result = new HashMap<String, String>();
		this.result.put("driverClassName", driverClassName);
		this.result.put("url", url);
		this.result.put("username", username);
		this.result.put("password", password);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties(){
        return this.result;
    }

}
