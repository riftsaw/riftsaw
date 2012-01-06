/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ScopeCompletionEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ScopeCompletionAdapter implements
		EventDetailMapping<ScopeCompletionEvent> {
	
	public static final String ACTIVITY_NAME = "SCOPE_COMPLETION";
	
	public Event adoptDetails(Event target, ScopeCompletionEvent source) {
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
