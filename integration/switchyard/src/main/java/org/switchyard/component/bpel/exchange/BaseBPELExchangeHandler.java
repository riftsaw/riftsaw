/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.switchyard.component.bpel.exchange;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.switchyard.BaseHandler;
import org.switchyard.common.type.Classes;
import org.switchyard.exception.SwitchYardException;

/**
 * Contains base BPELExchangeHandler functionality and/or utility methods.
 *
 */
public abstract class BaseBPELExchangeHandler extends BaseHandler implements BPELExchangeHandler {

    /**
     * Creates a URL for the given resource location.
     * @param location the resource location (http://, https://, file:, or classpath location)
     * @return the resource URL
     */
    protected URL getResourceURL(String location) {
        URL url;
        try {
            if (location.startsWith("http:") || location.startsWith("https:")) {
                url = new URL(location);
            } else if (location.startsWith("file:")) {
                url = new File(location.substring(5)).toURI().toURL();
            } else {
                url = Classes.getResource(location, getClass());
            }
        } catch (IOException ioe) {
            throw new SwitchYardException(ioe);
        }
        return url;
    }

}
