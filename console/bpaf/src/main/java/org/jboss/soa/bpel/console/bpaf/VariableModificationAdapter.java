/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class VariableModificationAdapter implements
		EventDetailMapping<VariableModificationEvent> {
	
	public static final String ACTIVITY_NAME = "VARIABLE_MODIFICATION";
	
	public Event adoptDetails(Event target, VariableModificationEvent source) {
		target.addData(new Variable(source.getVarName(), source.getNewValue()));
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
