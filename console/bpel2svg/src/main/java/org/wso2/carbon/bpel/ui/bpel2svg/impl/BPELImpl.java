// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.Link;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;

public class BPELImpl implements org.wso2.carbon.bpel.ui.bpel2svg.BPELInterface {
    private Log log = LogFactory.getLog(BPELImpl.class);
    private ProcessInterface processActivity = null;
    private boolean vertical = true;
    private boolean includeAssign = true;

    //To handle links
    public Map<String, Link> links = new HashMap<String, Link>();
    public Set<ActivityInterface> sources = new HashSet<ActivityInterface>();
    public Set<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> targets =
            new HashSet<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface>();
    
    private XMLStreamReader parser = null;
    private StAXOMBuilder builder = null;
    private OMElement bpelElement = null;

    public void processBpelString(OMElement om) {

        if (om != null) {

            OMElement startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.SEQUENCE_START_TAG)); // namesapce should be changed, exceptions should be handled.
            if (startElement != null) {
                processActivity = new ProcessImpl(bpelElement);
                processActivity.setLinkProperties(links, sources,targets);
                processActivity.processSubActivities(bpelElement);
            }else{
                startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.FLOW_START_TAG)); // namesapce should be changed, exceptions should be handled.
                if(startElement != null){
                    processActivity = new ProcessImpl(bpelElement);
                    processActivity.setLinkProperties(links, sources,targets);
                    processActivity.processSubActivities(bpelElement);   
                }
                else {
                    startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.SCOPE_START_TAG)); // namesapce should be changed, exceptions should be handled.
                    if(startElement != null){
                        processActivity = new ProcessImpl(bpelElement);
                        processActivity.setLinkProperties(links, sources,targets);
                        processActivity.processSubActivities(bpelElement);
                    } else {
                                    processActivity = new ProcessImpl(bpelElement);
                        processActivity.setLinkProperties(links, sources,targets);
                        processActivity.processSubActivities(bpelElement);
                    }
                }
            }
        }
    }

    public OMElement load(String bpelStr) {
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(bpelStr));

            builder = new StAXOMBuilder(parser);
            bpelElement = builder.getDocumentElement();
            //check whether the paser needed to be closed
            return bpelElement;
        } catch (XMLStreamException e) {
            log.error("XMLStreamReader creation failed", e);
            throw new NullPointerException("Document Element is NULL");
        }
    }

    public ProcessInterface getRootActivity() {
        return processActivity;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public boolean isIncludeAssign() {
        return includeAssign;
    }

    public void setIncludeAssign(boolean includeAssign) {
        this.includeAssign = includeAssign;
    }

    public OMElement getBpelElement() {
        return bpelElement;
    }

    public void setBpelElement(OMElement bpelElement) {
        this.bpelElement = bpelElement;
    }
}
