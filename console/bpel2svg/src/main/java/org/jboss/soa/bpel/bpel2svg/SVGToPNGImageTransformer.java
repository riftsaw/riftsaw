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
import java.io.Reader;
import java.io.StringReader;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * This class represents the  transformer from the SVG document object model
 * to an output stream representing the image to be displayed.
 *
 */
public class SVGToPNGImageTransformer implements SVGImageTransformer {

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
        // Create a PNG transcoder
        PNGTranscoder pngTranscoder = new PNGTranscoder();
        
        // Create the transcoder input.
        String inputString = svg.getHeaders() + svg.generateSVGString();
        
        //SVGDocument svgDoc = svg.getRootActivity().getSVGDocument();
        
        //TranscoderInput transcoderInput = new TranscoderInput(svgDoc);
        Reader stringReader = new StringReader(inputString);
        TranscoderInput transcoderInput = new TranscoderInput(stringReader);
        
        // Create the transcoder output.
        TranscoderOutput transcoderOutput = new TranscoderOutput(os);
        try {
            pngTranscoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException te) {
        	// code compatible with jdk 1.5
            IOException ioe = new IOException("Transcoder error");
            ioe.initCause(te);
            throw ioe;
        }
	}
	
}
