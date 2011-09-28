// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.TerminationHandlerInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.apache.axiom.om.OMElement;

public class TerminationHandlerImpl extends SequenceImpl implements TerminationHandlerInterface {

    public TerminationHandlerImpl(String token) {
        super(token);

        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public TerminationHandlerImpl(OMElement omElement) {
        super(omElement);

        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

     public TerminationHandlerImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getEndTag() {
        return org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TERMINATIONHANDLER_END_TAG;
    }

}
