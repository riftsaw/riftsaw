// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.CompensateInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.apache.axiom.om.OMElement;

public class CompensateImpl extends SimpleActivityImpl implements CompensateInterface {

    public CompensateImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public CompensateImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public CompensateImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getEndTag() {
        return org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.COMPENSATE_END_TAG;
    }
}
