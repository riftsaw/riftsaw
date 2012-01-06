// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

/**
 * Created by IntelliJ IDEA.
 * User: waruna
 * Date: Jul 17, 2009
 * Time: 10:10:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class Link {

    public ActivityInterface getSource() {
        return source;
    }

    public void setSource(ActivityInterface source) {
        this.source = source;
    }

    public ActivityInterface getTarget() {
        return target;
    }

    public void setTarget(ActivityInterface target) {
        this.target = target;
    }

    private ActivityInterface source;
    private ActivityInterface target;
}
