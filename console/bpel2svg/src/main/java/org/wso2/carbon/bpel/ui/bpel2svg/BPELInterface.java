// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

import org.apache.axiom.om.OMElement;

public interface BPELInterface {
    public void processBpelString(OMElement om);

   // public OMElement load(String filename);
    public OMElement load(String bpelStr);

    public void setBpelElement(OMElement bpelElement);

    public OMElement getBpelElement();

    public void setIncludeAssign(boolean includeAssign);

    public boolean isIncludeAssign();

    public void setVertical(boolean vertical);

    public boolean isVertical();

    public ProcessInterface getRootActivity();

}
