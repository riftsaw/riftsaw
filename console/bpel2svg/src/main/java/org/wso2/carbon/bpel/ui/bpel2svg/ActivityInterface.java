// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.apache.axiom.om.OMElement;

import java.util.List;
import java.util.Set;
import java.util.Map;

public interface ActivityInterface {
	
	// States associated with an activity
	public enum ActivityState {
		Ready,
		Active,
		Completed,
		Failed
	}

	/**
	 * This method returns the activity's current state.
	 * 
	 * @return The state
	 */
	public ActivityState getState();
	
	/**
	 * This method sets the activity's current state.
	 * 
	 * @param state The state
	 */
	public void setState(ActivityState state);
	
    //public ActivityInterface processSubActivities(StringTokenizer bpelST);
    public ActivityInterface processSubActivities(OMElement om);

    public void layout(int startXLeft, int startYTop);

    // public String getSVGString();
    public Element getSVGString(SVGDocument doc);

    //public String getSubActivitiesSVGString();
    public Element getSubActivitiesSVGString(SVGDocument doc);

    public SVGCoordinates getEntryArrowCoords();

    public SVGCoordinates getExitArrowCoords();

    public List<ActivityInterface> getSubActivities();

    /**
     * The starting line number associated with the activity.
     * 
     * @return The start line number
     */
    public int getStartLineNumber();
    
    /**
     * The end line number associated with the activity.
     * 
     * @return The end line number, or -1 if could not be determined
     */
    public int getEndLineNumber();
    
    /**
     * This method returns the activity located at the specified
     * line number.
     * 
     * @param lineNumber The line number
     * @return The activity, or null if not found
     */
    public ActivityInterface getActivityAtLineNumber(int lineNumber);
    
    public SVGDimension getDimensions() ;

    public void switchDimensionsToHorizontal();

    public String getId();

    public String getName();

    public void setName(String name);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    // Start Icon Methods
    public int getStartIconXLeft();

    public void setStartIconXLeft(int xLeft);

    public int getStartIconYTop();

    public void setStartIconYTop(int yTop);

    public int getStartIconWidth();

    public int getStartIconHeight();

    public void setStartIconHeight(int iconHeight);

    public void setStartIconWidth(int iconWidth);

    public String getStartIconPath();

    public void setStartIconPath(String iconPath);

    // End Icon methods
    public int getEndIconXLeft();

    public void setEndIconXLeft(int xLeft);

    public int getEndIconYTop();

    public void setEndIconYTop(int yTop);

    public int getEndIconWidth();

    public int getEndIconHeight();

    public String getEndIconPath();

    public boolean isIncludeAssigns();

    public boolean isVerticalChildLayout();

    public void setVerticalChildLayout(boolean verticalChildLayout);

    public boolean isHorizontalChildLayout();

    public String getEndTag();

    public Element getRoot();

    public String getActivityInfoString();

    public List<BPELAttributeValuePair> getAttributes();

    public Set<ActivityInterface> getLinkRoots();

    public ActivityInterface getParent();

    public int getCorrectionY();

    public void setCorrectionY(int correctionY);

    public void setLinkProperties(Map<String, Link> links, Set<ActivityInterface> sources,
                                  Set<org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface> targets);

    public Map<String, Link> getLinks();
}
