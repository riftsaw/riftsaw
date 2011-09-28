/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.jboss.bpm.monitor.model.bpaf.Tuple;

/**
 * The correlation key information
 * 
 * @author Jeff Yu
 * 
 */
public class CorrelationKey extends Tuple {

	public static final String name = "correlation-key";
	
	public CorrelationKey(String value) {
		setName(name);
		setValue(value);
	}
	
}
