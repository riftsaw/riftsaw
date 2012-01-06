// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.TargetInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.apache.axiom.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: waruna
 * Date: Jul 17, 2009
 * Time: 10:12:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TargetImpl extends TargetsImpl implements TargetInterface{
    public TargetImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

        
    }

    public TargetImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

  /*  public ActivityImpl getParent() {
        return parent;
    }

    public void setParent(ActivityImpl parent) {
        this.parent = parent;
    }
    */
  //  protected ActivityImpl parent;

    @Override
    public String getEndTag() {
        return org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TARGET_END_TAG;
    }
}
