// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface;

import java.io.*;

import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SVGImpl implements org.wso2.carbon.bpel.ui.bpel2svg.SVGInterface {

    private org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface rootActivity = null;
    private Log log = LogFactory.getLog(SVGImpl.class);
    private String svgStr = null;

    private SVGDocument svgDoc = null;

    public ProcessInterface getRootActivity() {
        return rootActivity;
    }

    public void setRootActivity(org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface rootActivity) {
        this.rootActivity = rootActivity;
    }
    
    public ActivityInterface getActivityAtLineNumber(int lineNumber) {
    	ActivityInterface ret=null;
    	
    	if (this.rootActivity != null) {
    		ret = this.rootActivity.getActivityAtLineNumber(lineNumber);
    		
    	}
    	return(ret);
    }

    /*
    * Return the image as a SVG string
    * */
    public String generateSVGString() {
         try {
            StringWriter writer = new StringWriter();
            SVGDocument svgDoc = getRootActivity().getSVGDocument();
             this.svgDoc = svgDoc;
            
            DOMUtilities.writeDocument(svgDoc, writer);
            writer.close();
             svgStr = writer.toString();
            return svgStr;
        } catch (IOException ioe) {
             log.error("Error Generating SVG String", ioe);
             return null;
        }
    }

    /*
    * Return the image as a base64 encoded string of a PNG
    * */
    public String toPNGBase64String() {
        // Create a JPEG transcoder
        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        // Set the transcoding hints.
        jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8));
        // Create the transcoder input.
        String inputString = getHeaders() + generateSVGString();  //svgDoc is set from generateSVGString()
        TranscoderInput transcoderInput = new TranscoderInput(svgDoc);
        Reader stringReader = new StringReader(inputString);
        TranscoderInput transcoderInput2 = new TranscoderInput(stringReader);
        // Create the transcoder output.
        OutputStream osByteArray = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(osByteArray);
        try {
            jpegTranscoder.transcode(transcoderInput2, transcoderOutput);
        } catch (TranscoderException e) {
            log.error("JPEGTranscoder error", e);
            return null;
        }
        try {
             osByteArray.flush();
        } catch (IOException e) {
            log.error("Error while flushing OutputStreamByteArray", e);
            return null;
        }

        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) osByteArray).toByteArray()));
        return base64;
    }

    /*
    * Return the image as a byte array of a PNG
    * */
    public byte[] toPNGBytes() {
        // Create a JPEG transcoder
        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        // Set the transcoding hints.
        jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8));
        // Create the transcoder input.
        String inputString = getHeaders() + generateSVGString();    //svgDoc is set from generateSVGString()
        TranscoderInput transcoderInput = new TranscoderInput(svgDoc);
        Reader stringReader = new StringReader(inputString);
        TranscoderInput transcoderInput2 = new TranscoderInput(stringReader);
        // Create the transcoder output.
        OutputStream osByteArray = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(osByteArray);
        try {
            jpegTranscoder.transcode(transcoderInput2, transcoderOutput);
        } catch (TranscoderException e) {
            log.error("JPEGTranscoder transcode error", e);
            return null;
        }

        try {
             osByteArray.flush();
        } catch (IOException e) {
            log.error("Error while flushing OutputStreamByteArray", e);
            return null;
        }
        return ((ByteArrayOutputStream) osByteArray).toByteArray();
    }

    public String getHeaders() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<!DOCTYPE svg PUBLIC '" +
                SVGConstants.SVG_PUBLIC_ID + "' '" + SVGConstants.SVG_SYSTEM_ID + "'>\n\n";
    }
}
