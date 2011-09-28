// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import java.util.*;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGIcons;
import org.wso2.carbon.bpel.ui.bpel2svg.Link;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPELAttributeValuePair;

public abstract class ActivityImpl implements org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface {
    // Local Variables
    protected org.wso2.carbon.bpel.ui.bpel2svg.LayoutManager layoutManager =
            org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.getInstance().getLayoutManager();
    protected String name = null;
    protected String displayName = null;
    protected List<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> subActivities =
            new ArrayList<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface>();
    protected List<org.wso2.carbon.bpel.ui.bpel2svg.BPELAttributeValuePair> attributes =
            new ArrayList<org.wso2.carbon.bpel.ui.bpel2svg.BPELAttributeValuePair>();
    
    protected int startLineNumber=-1;
    
    protected ActivityState state=ActivityState.Ready;
    
    public List<BPELAttributeValuePair> getAttributes() {
        return attributes;
    }

    public Map<String, Link> links;
    protected Set<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> sources;
    protected Set<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> targets;

    public int getCorrectionY() {
        return correctionY;
    }

    public void setCorrectionY(int correctionY) {
        this.correctionY += correctionY;
    }

    protected int correctionY = 0;

    public ActivityInterface getParent() {
        return parent;
    }

    public void setParent(ActivityInterface parent) {
        this.parent = parent;
    }

    protected ActivityInterface parent = null;

    // Start Icon
    protected String startIconPath = null;
    protected int startIconHeight = layoutManager.getStartIconDim();
    protected int startIconWidth = layoutManager.getStartIconDim();
    protected int startIconXLeft = 0;
    protected int startIconYTop = 0;
    protected int startIconTextXLeft = 0;
    protected int startIconTextYTop = 0;
    // End Icon
    protected String endIconPath = null;
    protected int endIconHeight = layoutManager.getEndIconDim();
    protected int endIconWidth = layoutManager.getEndIconDim();
    protected int endIconXLeft = 0;
    protected int endIconYTop = 0;
    protected int endIconTextXLeft = 0;
    protected int endIconTextYTop = 0;
    // Layout
    protected boolean verticalChildLayout = true;
    // SVG Specific 
    protected org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension dimensions = null;
    protected boolean exitIcon = false;


    //SVG Batik Specific - I modify
    protected /*static*/ SVGGraphics2D generator = null;

    protected /*static*/ DOMImplementation dom = SVGDOMImplementation.getDOMImplementation();
    protected /*static*/ String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    protected /*static*/ SVGDocument doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
    protected /*static*/ Element root = doc.getDocumentElement();


    // Box
    public final static int BOX_MARGIN = 10;
    protected int boxXLeft = 0;
    protected int boxYTop = 0;
    protected int boxHeight = 0;
    protected int boxWidth = 0;
    protected String boxStyle = "fill-opacity:0.04;fill-rule:evenodd;stroke:#0000FF;stroke-width:1.99999988;" +
            "stroke-linecap:square;stroke-linejoin:round;stroke-miterlimit:10;stroke-dasharray:none;" +
            "bbbbbbbstroke-opacity:1;fill:url(#orange_red);stroke-opacity:0.2";
    protected String failureBoxStyle = "fill-opacity:0.04;fill-rule:evenodd;stroke:#FF0000;stroke-width:2.99999988;" +
    		"stroke-linecap:square;stroke-linejoin:round;stroke-miterlimit:10;stroke-dasharray:none;" +
    		"bbbbbbbstroke-opacity:1;fill:url(#orange_red);stroke-opacity:0.8";

    // Constructor
    public ActivityImpl() {
        super();
    }

    public ActivityImpl(String token) {
        int nameIndex = token.indexOf("name");
        if (nameIndex >= 0) {
            int firstQuoteIndex = token.indexOf("\"", nameIndex + 1);
            if (firstQuoteIndex >= 0) {
                int lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    setName(token.substring(firstQuoteIndex + 1, lastQuoteIndex));
                    setDisplayName(getName());
                }
            }
        }
    }

    public ActivityImpl(OMElement omElement) {
        Iterator tmpIterator = omElement.getAllAttributes();

        while (tmpIterator.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) tmpIterator.next();
            String tmpAttribute = new String(omAttribute.getLocalName());
            String tmpValue = new String(omAttribute.getAttributeValue());

            if (tmpAttribute != null && tmpValue != null) {
                attributes.add(new org.wso2.carbon.bpel.ui.bpel2svg.BPELAttributeValuePair(tmpAttribute, tmpValue));

                if (tmpAttribute.equals(new String("name"))) {
                    setName(tmpValue);
                    setDisplayName(getName());
                }
            }
        }
        
        // Record source line number
        startLineNumber = omElement.getLineNumber();
    }

    // Properties
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * The starting line number associated with the activity.
     * 
     * @return The start line number
     */
    public int getStartLineNumber() {
    	return(startLineNumber);
    }
    
    /**
     * The end line number associated with the activity.
     * 
     * @return The end line number, or -1 if could not be determined
     */
    public int getEndLineNumber() {
    	int ret=-1;
    	
    	ActivityInterface parent=getParent();
    	
    	if (parent != null) {
    		int index=parent.getSubActivities().indexOf(this);
    		
    		if (index != -1) {
    			if (index < (parent.getSubActivities().size()-1)) {
    				ActivityInterface other=parent.getSubActivities().get(index+1);
    				
    				ret = other.getStartLineNumber()-1;
    			} else {
    				ret = parent.getEndLineNumber();
    			}
    		}
    	}
    	
    	return(ret);
    }

    /**
     * This method returns the activity located at the specified
     * line number.
     * 
     * @param lineNumber The line number
     * @return The activity, or null if not found
     */
    public ActivityInterface getActivityAtLineNumber(int lineNumber) {
    	ActivityInterface ret=null;
    	
    	int endline=getEndLineNumber();
    	
    	if (getStartLineNumber() <= lineNumber && (endline == -1 || endline >= lineNumber)) {
    		
    		java.util.Iterator<ActivityInterface> iter=subActivities.iterator();
    		
    		while (ret == null && iter.hasNext()) {
    			ret = iter.next().getActivityAtLineNumber(lineNumber);
    		}
    		
    		if (ret == null) {
    			ret = this;
    		}
    	}
    	
    	return(ret);
    }
    
    public ActivityState getState() {
    	return(this.state);
    }
    
    public void setState(ActivityState state) {
    	this.state = state;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLayerId() {
        return getLayerId(getId());
    }

    public String getLayerId(String id) {
        return id; //+"-Layer";
    }

    public boolean isAddOpacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isAddCompositeActivityOpacity() {
        return layoutManager.isAddCompositeActivityOpacity();
    }

    public boolean isAddIconOpacity() {
        return layoutManager.isAddIconOpacity();
    }

    public boolean isAddSimpleActivityOpacity() {
        return layoutManager.isAddSimpleActivityOpacity();
    }

    public double getOpacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getSimpleActivityOpacity() {
        return layoutManager.getSimpleActivityOpacity(getState());
    }

    public double getCompositeOpacity() {
        return layoutManager.getCompositeActivityOpacity();
    }

    public double getIconOpacity(ActivityState state) {
        return layoutManager.getIconOpacity(state);
    }

    public String getBoxId() {
        return getId(); // + "-Box";
    }

    public String getStartImageId() {
        return getId(); // + "-StartImage";
    }

    public String getEndImageId() {
        return getId(); // + "-EndImage";
    }

    public String getArrowId(String startId, String endId) {
        return startId + "-" + endId + "-Arrow";
    }

    public String getStartImageTextId() {
        return getStartImageId(); // + "-Text";
    }

    public String getEndImageTextId() {
        return getEndImageId(); // + "-Text";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIconHeight() {
        return startIconHeight;
    }

    public String getStartIconPath() {
        return startIconPath;
    }

    public void setStartIconPath(String iconPath) {
        this.startIconPath = iconPath;
    }

    public String getEndIconPath() {
        return endIconPath;
    }

    public int getStartIconWidth() {
        return startIconWidth;
    }

    public int getEndIconHeight() {
        return endIconHeight;
    }

    public int getEndIconWidth() {
        return endIconWidth;
    }

    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }

    public void setStartIconWidth(int iconWidth) {
        this.startIconWidth = iconWidth;
    }

    public int getStartIconXLeft() {
        return startIconXLeft;
    }

    public void setStartIconXLeft(int xLeft) {
        this.startIconXLeft = xLeft;
    }

    public int getStartIconYTop() {
        return startIconYTop + correctionY;
    }

    public void setStartIconYTop(int yTop) {
        this.startIconYTop = yTop;
    }

    public int getStartIconTextXLeft() {
        return startIconTextXLeft;
    }

    public void setStartIconTextXLeft(int startIconTextXLeft) {
        this.startIconTextXLeft = startIconTextXLeft;
    }

    public int getStartIconTextYTop() {
        return startIconTextYTop + correctionY;
    }

    public void setStartIconTextYTop(int startIconTextYTop) {
        this.startIconTextYTop = startIconTextYTop;
    }

    public int getEndIconXLeft() {
        return endIconXLeft;
    }

    public void setEndIconXLeft(int xLeftEnd) {
        this.endIconXLeft = xLeftEnd;
    }

    public int getEndIconYTop() {
        return endIconYTop + correctionY;
    }

    public void setEndIconYTop(int yTopEnd) {
        this.endIconYTop = yTopEnd;
    }

    public int getEndIconTextXLeft() {
        return endIconTextXLeft;
    }

    public void setEndIconTextXLeft(int endIconTextXLeft) {
        this.endIconTextXLeft = endIconTextXLeft;
    }

    public int getEndIconTextYTop() {
        return endIconTextYTop;
    }

    public void setEndIconTextYTop(int endIconTextYTop) {
        this.endIconTextYTop = endIconTextYTop;
    }

    public int getXSpacing() {
        return layoutManager.getXSpacing();
    }

    public int getYSpacing() {
        return layoutManager.getYSpacing();
    }

    public int getBoxHeight() {
        return boxHeight;
    }

    public void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }

    public String getBoxStyle() {
    	if (getState() == ActivityState.Failed) {
    		return(failureBoxStyle);
    	}
        return boxStyle;
    }

    public void setBoxStyle(String boxStyle) {
        this.boxStyle = boxStyle;
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }

    public int getBoxXLeft() {
        return boxXLeft;
    }

    public void setBoxXLeft(int boxXLeft) {
        this.boxXLeft = boxXLeft;
    }

    public int getBoxYTop() {
        return boxYTop;
    }

    public void setBoxYTop(int boxYTop) {
        this.boxYTop = boxYTop;
    }

    public boolean isExitIcon() {
        return exitIcon;
    }

    public void setExitIcon(boolean exitIcon) {
        this.exitIcon = exitIcon;
    }

    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }

    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }

    public boolean isIncludeAssigns() {
        return layoutManager.isIncludeAssigns();
    }

    public List<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> getSubActivities() {
        return subActivities;
    }

    public boolean isVerticalChildLayout() {
        return verticalChildLayout;
    }

    public void setVerticalChildLayout(boolean verticalChildLayout) {
        this.verticalChildLayout = verticalChildLayout;
    }

    public boolean isHorizontalChildLayout() {
        return !isVerticalChildLayout();
    }

    public Element getSVGString(SVGDocument doc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  public Element getSubActivitiesSVGString(SVGDocument doc) {
        Iterator<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> itr = subActivities.iterator();
        org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
        Element subElement = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        while (itr.hasNext()) {
            activity = itr.next();
            subElement.appendChild(activity.getSVGString(doc));   //attention check this probably should be changed
            name = activity.getId();
        }
        return subElement;
    }

    protected Element getImageDefinition(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                         int imgWidth, int imgHeight, String id) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());

        if (getStartIconPath() != null) {         // TODO looks like redundent, imgPath in method arguments
            //if (isAddIconOpacity() && !isAddSimpleActivityOpacity()) {
                group.setAttributeNS(null, "style", "opacity:" + getIconOpacity(getState()));
            //}
            Element image = doc.createElementNS("http://www.w3.org/2000/svg", "image");
            image.setAttributeNS(null, "xlink:href", imgPath);
            //image.setAttributeNS(null, "transform", BPEL2SVGIcons.TRANSFORMATION_MATRIX);
            image.setAttributeNS(null, "x", String.valueOf(imgXLeft));
            image.setAttributeNS(null, "y", String.valueOf(imgYTop));
            image.setAttributeNS(null, "width", String.valueOf(imgWidth));
            image.setAttributeNS(null, "height", String.valueOf(imgHeight));
            image.setAttributeNS(null, "id", id);
            image.setAttributeNS("xlink", "title", getActivityInfoString());

            //if (isAddIconOpacity() && !isAddSimpleActivityOpacity()) {
                group.appendChild(image);
                return group;
            //} else {
             //   return image;
            //}
        }
        return group;
    }

    protected Element getImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getStartIconPath(), getStartIconXLeft(), getStartIconYTop(),
                getStartIconWidth(), getStartIconHeight(), getStartImageId());
    }

    protected Element getEndImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getEndIconPath(), getEndIconXLeft(), getEndIconYTop(), getEndIconWidth(),
                getEndIconHeight(), getEndImageId());
    }

    protected Element getImageText(SVGDocument doc, int imgXLeft, int imgYTop, int imgWidth, int imgHeight,
                                   String imgName, String imgDisplayName) {
        int txtXLeft = imgXLeft;
        int txtYTop = imgYTop; // + imgHeight + BPEL2SVGFactory.TEXT_ADJUST;

        Element a = doc.createElementNS("http://www.w3.org/2000/svg", "a");
        if (imgDisplayName != null) {
            a.setAttributeNS(null, "id", imgName);

            Element text1 = doc.createElementNS("http://www.w3.org/2000/svg", "text");
            text1.setAttributeNS(null, "x", String.valueOf(txtXLeft));
            text1.setAttributeNS(null, "y", String.valueOf(txtYTop));
            text1.setAttributeNS(null, "id", imgName + ".Text");
            text1.setAttributeNS(null, "xml:space", "preserve");
            text1.setAttributeNS(null, "style", "font-size:12px;font-style:normal;font-variant:normal;font-weight:" +
                    "normal;font-stretch:normal;text-align:start;line-height:125%;writing-mode:lr-tb;text-anchor:" +
                    "start;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;" +
                    "stroke-linejoin:miter;stroke-opacity:1;font-family:Arial Narrow;" +
                    "-inkscape-font-specification:Arial Narrow");

            Element tspan = doc.createElementNS("http://www.w3.org/2000/svg", "tspan");
            tspan.setAttributeNS(null, "x", String.valueOf(txtXLeft));
            tspan.setAttributeNS(null, "y", String.valueOf(txtYTop));
            tspan.setAttributeNS(null, "id", "tspan-" + imgName);

            Text text2 = doc.createTextNode(imgDisplayName);
            tspan.appendChild(text2);

            text1.appendChild(tspan);
            a.appendChild(text1);
        }
        return a;
    }

    protected Element getStartImageText(SVGDocument doc) {
        return getImageText(doc, getStartIconTextXLeft(), getStartIconTextYTop(), getStartIconWidth(),
                getStartIconHeight(), getStartImageTextId(), getDisplayName());
    }

    protected void getEndImageText(SVGDocument doc) {
        getImageText(doc, getEndIconTextXLeft(), getEndIconTextYTop(), getStartIconWidth(), getStartIconHeight(),
                getEndImageTextId(), getDisplayName());
    }

    protected boolean isLargeArrow() {
        return largeArrow;
    }

    protected void setLargeArrow(boolean largeArrow) {
        this.largeArrow = largeArrow;
    }

    private boolean largeArrow = false;

    private String getArrowStyle(ActivityInterface start, ActivityInterface end) {
    	
    	String op="1";
    	if ((start != null && start.getState() == ActivityState.Ready) || (end != null && end.getState() == ActivityState.Ready)) {
    		op="0.3";
    	}
    	
        String largeArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:" +
                "butt;stroke-linejoin:round;marker-end:url(#Arrow1Lend);stroke-miterlimit:4;stroke-dasharray:" +
                "none;stroke-opacity:"+op;
        String mediumArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:" +
                "butt;stroke-linejoin:round;marker-end:url(#Arrow1Mend);stroke-miterlimit:4;stroke-dasharray:" +
                "none;stroke-opacity:"+op;

        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }

    protected String getLinkArrowStyle() {
        String largeArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:" +
                "butt;stroke-linejoin:round;marker-end:url(#LinkArrow);stroke-miterlimit:4;stroke-dasharray:" +
                "none;stroke-opacity:1;opacity: 0.25;"; // + getIconOpacity();
        String mediumArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:" +
                "butt;stroke-linejoin:round;marker-end:url(#LinkArrow);stroke-miterlimit:4;stroke-dasharray:" +
                "none;stroke-opacity:1;opacity: 0.25;"; // + getIconOpacity();

        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }

    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY,
    				String id, ActivityInterface start, ActivityInterface end) {         //here we have to find whether
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

        if (startX == endX || startY == endY) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + endX + "," + endY);
        }
        else {
            if(layoutManager.isVerticalLayout()){
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," +
                        ((startY + 2 * endY) / 3) + " L " + endX + "," + ((startY + 2 * endY) / 3) + " L " + endX +
                        "," + endY);                            //use constants for these propotions
            }else{
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1* endX) / 2) +
                        "," + startY + " L " + ((startX + 1* endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
            }
        }
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle(start, end));

        return path;
    }

    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int midX, int midY, int endX,
                                         int endY, String id, ActivityInterface start, ActivityInterface end) {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + midX + "," + midY + "L " + endX +
                "," + endY);
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle(start, end));

        return path;
    }
    
    protected Element getBoxDefinition(SVGDocument doc) {
        return getBoxDefinition(doc, getDimensions().getXLeft() + BOX_MARGIN, getDimensions().getYTop() + BOX_MARGIN,
                getDimensions().getWidth() - (BOX_MARGIN * 2), getDimensions().getHeight() - (BOX_MARGIN * 2), getBoxId());
    }

    protected Element getBoxDefinition(SVGDocument doc, int boxXLeft, int boxYTop, int boxWidth, int boxHeight, String id) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", "Layer-" + id);

        if (layoutManager.isShowSequenceBoxes()) {

            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            rect.setAttributeNS(null, "width", String.valueOf(boxWidth));
            rect.setAttributeNS(null, "height", String.valueOf(boxHeight));
            rect.setAttributeNS(null, "x", String.valueOf(boxXLeft));
            rect.setAttributeNS(null, "y", String.valueOf(boxYTop));
            rect.setAttributeNS(null, "id", "Rect" + id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", getBoxStyle());

            group.appendChild(rect);
        }
        return group;
    }

    public org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension getDimensions() {
//        return dimensions;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void switchDimensionsToHorizontal() {
        int width = 0;
        int height = 0;

        org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
        Iterator<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> itr = getSubActivities().iterator();
        while (itr.hasNext()) {
            activity = itr.next();
            activity.switchDimensionsToHorizontal();
        }

        width = getDimensions().getWidth();
        height = getDimensions().getHeight();
        // Switch
        getDimensions().setHeight(width);
        getDimensions().setWidth(height);
    }

    public void layout(int startXLeft, int startYTop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEndTag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return getId();
    }

    public org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates getEntryArrowCoords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates getExitArrowCoords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void passContent() {
        root = doc.getDocumentElement();
        generator.getRoot(root);
    }

    // Methods

    public Set<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> getLinkRoots () {
        sources.removeAll(targets);
        return sources;
    }

    public org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface processSubActivities(OMElement omElement) {
        org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface endActivity = null;
        if (omElement != null) {
            org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface activity = null;
            Iterator iterator = omElement.getChildElements();
            while (iterator.hasNext()) {
                OMElement tmpElement = (OMElement) iterator.next();

                if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ASSIGN_START_TAG) && isIncludeAssigns()) {
                    activity = new AssignImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.CATCHALL_START_TAG)) {
                    activity = new CatchAllImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.CATCH_START_TAG)) {
                    activity = new CatchImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.COMPENSATESCOPE_START_TAG)) {
                    activity = new CompensateScopeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.COMPENSATE_START_TAG)) {
                    activity = new CompensateImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.COMPENSATIONHANDLER_START_TAG)) {
                    activity = new CompensationHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ELSEIF_START_TAG)) {
                    activity = new ElseIfImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ELSE_START_TAG)) {
                    activity = new ElseImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.EVENTHANDLER_START_TAG)) {
                    activity = new EventHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.EXIT_START_TAG)) {
                    activity = new ExitImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.FAULTHANDLER_START_TAG)) {
                    activity = new FaultHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.FLOW_START_TAG)) {
                    activity = new FlowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.FOREACH_START_TAG)) {
                    activity = new ForEachImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.IF_START_TAG)) {
                    activity = new IfImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.INVOKE_START_TAG)) {
                    activity = new InvokeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ONALARM_START_TAG)) {
                    activity = new OnAlarmImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ONEVENT_START_TAG)) {
                    activity = new OnEventImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.ONMESSAGE_START_TAG)) {
                    activity = new OnMessageImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.PICK_START_TAG)) {
                    activity = new PickImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.PROCESS_START_TAG)) {
                    activity = new ProcessImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.RECEIVE_START_TAG)) {
                    activity = new ReceiveImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.REPEATUNTIL_START_TAG)) {
                    activity = new RepeatUntilImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.REPLY_START_TAG)) {
                    activity = new ReplyImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.RETHROW_START_TAG)) {
                    activity = new ReThrowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.SCOPE_START_TAG)) {
                    activity = new ScopeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.SEQUENCE_START_TAG)) {
                    activity = new SequenceImpl(tmpElement, this);
/*                    
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.SOURCE_START_TAG)) {
                    activity = new SourceImpl(tmpElement, this);//source;
                    if (activity.getAttributes().get(0).getAttribute().equals("linkName")) {
                        if (links.containsKey(activity.getAttributes().get(0).getValue())) {    //if a entry for the particular link name already exists
                            links.get(activity.getAttributes().get(0).getValue()).setSource(this.parent);
                        }
                        else {
                            Link link = new Link();
                            link.setSource(this.parent);
                            links.put(activity.getAttributes().get(0).getValue(), link);
                        }
                        sources.add(this.parent);
                    }
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.SOURCES_START_TAG)) {
                    activity = new SourcesImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TARGET_START_TAG)) {
                    activity = new TargetImpl(tmpElement, this);//target;
                    if (activity.getAttributes().get(0).getAttribute().equals("linkName")) {
                        if (links.containsKey(activity.getAttributes().get(0).getValue())) {
                            links.get(activity.getAttributes().get(0).getValue()).setTarget(this.parent);
                        }
                        else {
                            Link link = new Link();
                            link.setTarget(this.parent);
                            links.put(activity.getAttributes().get(0).getValue(), link);
                        }
                        targets.add(this.parent);
                    }
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TARGETS_START_TAG)) {
                    activity = new TargetsImpl(tmpElement, this);
*/
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.TERMINATIONHANDLER_START_TAG)) {
                    activity = new TerminationHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.THROW_START_TAG)) {
                    activity = new ThrowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.WAIT_START_TAG)) {
                    activity = new WaitImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.WHILE_START_TAG)) {
                    activity = new WhileImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(getEndTag())) {
                    break;
                } else {
                    continue;
                }

                activity.setLinkProperties(links, sources, targets);
                subActivities.add(activity);

                if (tmpElement.getChildElements().hasNext()) {
                    org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface replyActivity = activity.processSubActivities(tmpElement);
                    if (replyActivity != null) {
                        subActivities.add(replyActivity);
                    }
                }
                if (tmpElement.getLocalName().equals(org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory.PROCESS_START_TAG)) {
                    break;
                }
            }
        }
        return endActivity;
    }

    public Element getRoot() {
        return root;
    }

    public String getActivityInfoString() {
        String infoString = null;
        for(org.wso2.carbon.bpel.ui.bpel2svg.BPELAttributeValuePair x : attributes){
            String attrib = x.getAttribute();
            String val = x.getValue();
            if(infoString == null) infoString = "<" + attrib + "="  + val + "> ";
            else infoString += "<" + attrib + "="  + val + "> ";
        }

        if(infoString != null) return infoString;
        else return "No Attributes defined";
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinkProperties(Map<String, Link> links, Set<ActivityInterface> sources, Set<ActivityInterface> targets) {
        this.links = links;
        this.sources = sources;
        this.targets = targets;
    }
}
