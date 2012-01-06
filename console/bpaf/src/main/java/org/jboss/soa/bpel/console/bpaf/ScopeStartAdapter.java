/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ScopeStartEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ScopeStartAdapter implements EventDetailMapping<ScopeStartEvent> {
	
	public static final String ACTIVITY_NAME = "SCOPE_START";
	
	public Event adoptDetails(Event target, ScopeStartEvent source) {
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
