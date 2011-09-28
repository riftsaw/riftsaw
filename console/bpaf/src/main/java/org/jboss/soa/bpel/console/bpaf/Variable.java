/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.utils.DOMUtils;
import org.jboss.bpm.monitor.model.bpaf.Tuple;
import org.w3c.dom.Node;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class Variable extends Tuple {
	
	public static final String name = "variable";
	
	public Variable(String varName, Node value) {
		setName(name);
		String theValue = varName + "=" + DOMUtils.domToString(value);
		
		setValue(theValue);
	}
	
}
