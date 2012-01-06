// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.apache.axiom.om.OMElement;

public class FaultHandlerImpl extends EventHandlerImpl implements org.wso2.carbon.bpel.ui.bpel2svg.FaultHandlerInterface {

    public FaultHandlerImpl(String token) {
        super(token);

        if (name == null) {
            name = "FAULTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Fault Handlers";
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public FaultHandlerImpl(OMElement omElement) {
        super(omElement);

        if (name == null) {
            name = "FAULTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Fault Handlers";
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public FaultHandlerImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        if (name == null) {
            name = "FAULTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Fault Handlers";
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    @Override
    public String getEndTag() {
        return org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.FAULTHANDLER_END_TAG;
    }
}
