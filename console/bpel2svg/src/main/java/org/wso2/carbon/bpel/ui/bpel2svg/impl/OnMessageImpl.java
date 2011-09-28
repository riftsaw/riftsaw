// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

public class OnMessageImpl extends ActivityImpl implements org.wso2.carbon.bpel.ui.bpel2svg.OnMessageInterface {

    public OnMessageImpl(String token) {
        String partnerLink = "";
        String operation = "";
        // Get Partner Link Name
        int plIndex = token.indexOf("partnerLink");
        int firstQuoteIndex = 0;
        int lastQuoteIndex = 0;
        if (plIndex >= 0) {
            firstQuoteIndex = token.indexOf("\"", plIndex + 1);
            if (firstQuoteIndex >= 0) {
                lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    partnerLink = token.substring(firstQuoteIndex + 1, lastQuoteIndex);
                }
            }
        }
        // Get Operation Name
        int opIndex = token.indexOf("operation");
        if (opIndex >= 0) {
            firstQuoteIndex = token.indexOf("\"", opIndex + 1);
            if (firstQuoteIndex >= 0) {
                lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    operation = token.substring(firstQuoteIndex + 1, lastQuoteIndex);
                    setDisplayName(operation);
                }
            }
        }
        setName(partnerLink + "." + operation);

        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public OnMessageImpl(OMElement omElement) {
        super(omElement);

        String partnerLink = null;
        String operation = null;
        // Get Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = new String(omElement.getAttribute(new QName("partnerLink")).getAttributeValue());      //attention-  consider about namespace
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = new String(omElement.getAttribute(new QName("operation")).getAttributeValue());      //attention-  consider about namespace

        setName(partnerLink + "." + operation);
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public OnMessageImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        String partnerLink = null;
        String operation = null;
        // Get Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = new String(omElement.getAttribute(new QName("partnerLink")).getAttributeValue());      //attention-  consider about namespace
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = new String(omElement.getAttribute(new QName("operation")).getAttributeValue());      //attention-  consider about namespace

        setName(partnerLink + "." + operation);
        // Set Icon and Size
        startIconPath = org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getId() {
        return getName(); // + "-OnMessage";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ONMESSAGE_END_TAG;
    }

    @Override
    public org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = 0;
            int height = 0;
            dimensions = new org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension(width, height);

            SVGDimension subActivityDim = null;
            org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
            Iterator<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> itr = getSubActivities().iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                subActivityDim = activity.getDimensions();
                if (subActivityDim.getWidth() > width) {
                    width += subActivityDim.getWidth();
                }
                height += subActivityDim.getHeight();
            }

            height += getYSpacing() + getStartIconHeight() + (getYSpacing() / 2);
            width += getXSpacing();

            dimensions.setWidth(width);
            dimensions.setHeight(height);
        }

        return dimensions;
    }

    @Override
    public void layout(int startXLeft, int startYTop) {
        if (layoutManager.isVerticalLayout()) {
            layoutVertical(startXLeft, startYTop);
        } else {
            layoutHorizontal(startXLeft, startYTop);
        }
    }

    public void layoutVertical(int startXLeft, int startYTop) {
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);

        org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft;
        while (itr.hasNext()) {
            activity = itr.next();
            childXLeft = centreOfMyLayout - activity.getDimensions().getWidth() / 2;
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);

    }

    public void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
        Iterator<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop;
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
            childYTop = centreOfMyLayout - (activity.getDimensions().getHeight() / 2);
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }

        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);

    }

    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop();
        } else {
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

        }

        org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    @Override
    public org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates getExitArrowCoords() {
        SVGCoordinates coords = getStartIconExitArrowCoords();

        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(subActivities.size() - 1);
            coords = activity.getExitArrowCoords();
        }
        return coords;
    }

    protected org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates getStartIconExitArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop() + getStartIconHeight();
        } else {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

        }

        SVGCoordinates coords = new org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates(xLeft, yTop);

        return coords;
    }

    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group.appendChild(getBoxDefinition(doc));
        group.appendChild(getImageDefinition(doc));
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Add Arrow
        group.appendChild(getArrows(doc));

        return group;
    }

    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates exitCoords = null;
            SVGCoordinates entryCoords = null;
            Iterator<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> itr = subActivities.iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                if (prevActivity != null) {
                    exitCoords = prevActivity.getExitArrowCoords();
                    entryCoords = activity.getEntryArrowCoords();
                    id = prevActivity.getId() + "-" + activity.getId();
                    subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), 
                    		exitCoords.getYTop(), entryCoords.getXLeft(), 
                    		entryCoords.getYTop(), id, prevActivity, activity));
                } else {
                    entryCoords = activity.getEntryArrowCoords();
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), 
                    		myStartCoords.getYTop(), entryCoords.getXLeft(), 
                    		entryCoords.getYTop(), id, prevActivity, activity));
                }
                prevActivity = activity;
            }
        }
        return subGroup;
    }

    @Override
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }

    @Override
    public double getOpacity() {
        return getCompositeOpacity();
    }
}
