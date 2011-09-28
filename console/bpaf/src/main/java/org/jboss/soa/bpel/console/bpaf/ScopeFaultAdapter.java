/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ScopeFaultEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ScopeFaultAdapter implements EventDetailMapping<ScopeFaultEvent> {
	
	public static final String ACTIVITY_NAME = "SCOPE_FAULT";
	
	public Event adoptDetails(Event target, ScopeFaultEvent source) {
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
