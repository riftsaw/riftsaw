// This code is taken from WSO2 Carbon and is licensed by WSO2, Inc.
// under the Apache License version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
package org.wso2.carbon.bpel.ui.bpel2svg;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public interface ScopeInterface extends ActivityInterface {
    public Element getTerminationHandlerIcon(SVGDocument doc);

    public Element getFaultHandlerIcon(SVGDocument doc);

    public Element getCompensationHandlerIcon(SVGDocument doc);

    public Element getEventHandlerIcon(SVGDocument doc);
}
