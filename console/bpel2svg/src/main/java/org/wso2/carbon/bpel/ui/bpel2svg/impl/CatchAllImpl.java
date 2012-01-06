// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.apache.axiom.om.OMElement;


public class CatchAllImpl extends SequenceImpl implements org.wso2.carbon.bpel.ui.bpel2svg.CatchAllInterface {

    public CatchAllImpl(String token) {
        super(token);
        name = "CATCHALL" + System.currentTimeMillis();
        displayName = "Catch All";
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public CatchAllImpl(OMElement omElement) {
        super(omElement);

        name = "CATCHALL" + System.currentTimeMillis();
        displayName = "Catch All";

        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public CatchAllImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        name = "CATCHALL" + System.currentTimeMillis();
        displayName = "Catch All";

        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getEndTag() {
        return org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.CATCHALL_END_TAG;
    }

}
