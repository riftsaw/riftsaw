// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

public interface SVGInterface {
    
    public org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface getRootActivity();

    public void setRootActivity(org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface rootActivity);

    public ActivityInterface getActivityAtLineNumber(int lineNumber);
    
    public String generateSVGString();

    public String toPNGBase64String();

    public byte[] toPNGBytes();

    public String getHeaders();
    
}
