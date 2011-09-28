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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bpel.ui.bpel2svg.*;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.BPELImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.SVGImpl;

/**
 * This class provides the utility for converting a BPEL process description
 * into a SVG format, which can then optionally be transformed into a range of
 * alternative supported image types.
 *
 */
public class BPEL2SVGUtil {
	
	//public static final String JPEG_IMAGE = "jpeg";
	public static final String PNG_IMAGE = "png";
	public static final String SVG_IMAGE = "svg";
	
	private static java.util.Map<String, SVGImageTransformer> m_transformers=
						new java.util.HashMap<String, SVGImageTransformer>();
	
	static {
		m_transformers.put(SVG_IMAGE, new SVGToSVGImageTransformer());
		m_transformers.put(PNG_IMAGE, new SVGToPNGImageTransformer());
		//m_transformers.put(JPEG_IMAGE, new SVGToJPEGImageTransformer()); - commented out as JPEG not working currently
	}
	
	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: BPEL2SVGUtil <bpelFile> <outputFile> [ <transformType> ]");
			System.err.println("(transformerType values are: "+PNG_IMAGE+","+SVG_IMAGE+")");
			System.exit(1);
		}
		
		SVGImageTransformer transformer=null;
		
		if (args.length == 3) {
			transformer = BPEL2SVGUtil.getTransformer(args[2]);
			
			if (transformer == null) {
				System.err.println("Unknown transformerType '"+args[2]+
							"', valid values are: "+PNG_IMAGE+","+SVG_IMAGE);
				System.exit(1);
			}
		} else {
			transformer = BPEL2SVGUtil.getTransformer("svg");
		}
		
		try {
			java.io.FileInputStream fis=new java.io.FileInputStream(args[0]);
			
			java.io.FileOutputStream os=new java.io.FileOutputStream(args[1]);
			
			SVGInterface svg=BPEL2SVGUtil.generate(fis);
			
			ActivityInterface seq=svg.getActivityAtLineNumber(53);
			ActivityInterface recv=svg.getActivityAtLineNumber(61);
			ActivityInterface assign=svg.getActivityAtLineNumber(64);
			
			/*
			seq.setState(ActivityState.Failed);
			recv.setState(ActivityState.Completed);
			assign.setState(ActivityState.Failed);
			*/
			/*
			seq.setState(ActivityState.Active);
			recv.setState(ActivityState.Completed);
			assign.setState(ActivityState.Active);
			*/
			
			transformer.transform(svg, os);
			
			fis.close();
			os.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method generates a SVG representation of a supplied BPEL description,
	 * and optionally transforms it into an image format. If an image
	 * transformer is not specified, then the SVG representation will be
	 * returned in the output stream.
	 * 
	 * @param is The textual representation of a BPEL description
	 * @param os The output stream for the SVG or image representation
	 * @param transformer The optional image transformer
	 * @throws java.io.IOException Failed to generate the representation
	 */
    public static void generate(java.io.InputStream is, java.io.OutputStream os,
    						SVGImageTransformer transformer) throws java.io.IOException {
        SVGImpl svg = generateSVGImpl(is);
        
        if (transformer == null) {
        	String str=svg.getHeaders()+svg.generateSVGString();
        	os.write(str.getBytes());
        } else {
        	transformer.transform(svg, os);
        }
    }
    
	/**
	 * This method generates a SVG representation of a supplied BPEL description.
	 * 
	 * @param is The textual representation of a BPEL description
	 * @throws java.io.IOException Failed to generate the representation
	 */
    public static SVGInterface generate(java.io.InputStream is) throws java.io.IOException {
    	return(generateSVGImpl(is));
    }
    
    protected static SVGImpl generateSVGImpl(java.io.InputStream is) throws java.io.IOException {
    	byte[] b=new byte[is.available()];
    	is.read(b);
		
    	BPELInterface bpel = new BPELImpl();
        OMElement bpelStr = bpel.load(new String(b));
        
        bpel.processBpelString(bpelStr);

        LayoutManager layoutManager = BPEL2SVGFactory.getInstance().getLayoutManager();
        layoutManager.setVerticalLayout(true);
        layoutManager.setYSpacing(20);
        layoutManager.setYSpacing(50);
        layoutManager.layoutSVG(bpel.getRootActivity());

        SVGImpl svg = new SVGImpl();
        svg.setRootActivity(bpel.getRootActivity());
        
        return(svg);
    }
    
    /**
     * This method returns the SVG image transformer associated with the
     * supplied code.
     * 
     * @param code The image transformer code
     * @return The transformer, or null if not found
     */
    public static SVGImageTransformer getTransformer(String code) {
    	return(m_transformers.get(code));
    }
}
