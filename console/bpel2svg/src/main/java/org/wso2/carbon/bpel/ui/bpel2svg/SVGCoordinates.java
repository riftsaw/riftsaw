// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

public class SVGCoordinates {
    int xLeft = 0;
    int yTop = 0;

    public SVGCoordinates(int xLeft, int yTop) {
        this.xLeft = xLeft;
        this.yTop = yTop;
    }

    public SVGCoordinates() {
    }

    public int getXLeft() {
        return xLeft;
    }

    public void setXLeft(int xLeft) {
        this.xLeft = xLeft;
    }

    public int getYTop() {
        return yTop;
    }

    public void setYTop(int yTop) {
        this.yTop = yTop;
    }

}
