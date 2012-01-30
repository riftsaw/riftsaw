/*
 * Copyright 2009 Red Hat, Inc.
 * Portions licensed by WSO2, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jboss.soa.bpel.bpel2svg;

import java.io.IOException;

/**
 * This class represents the  transformer from the SVG document object model
 * to an output stream representing the image to be displayed.
 *
 */
public class SVGToSVGImageTransformer implements SVGImageTransformer {

	/**
	 * This method transforms a supplied SVG document into an image written
	 * to the supplied output stream.
	 * 
	 * @param svg The SVG document
	 * @param os The output stream
	 * @throws IOException Failed to transform the SVG doc into an image
	 */
	public void transform(org.wso2.carbon.bpel.ui.bpel2svg.SVGInterface svg,
						java.io.OutputStream os) throws IOException {
    	String str=svg.getHeaders()+svg.generateSVGString();
    	os.write(str.getBytes());
	}
	
}