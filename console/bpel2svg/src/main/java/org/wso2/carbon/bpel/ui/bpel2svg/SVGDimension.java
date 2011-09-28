// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

public class SVGDimension {

    protected LayoutManager layoutManager = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getLayoutManager();
    private int xLeft = 0;
    private int yTop = 0;
    private int width = 0;
    private int height = 0;

    public SVGDimension() {
    }

    public SVGDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
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
