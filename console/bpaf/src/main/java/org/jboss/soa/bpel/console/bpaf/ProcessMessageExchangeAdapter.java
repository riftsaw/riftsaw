/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ProcessMessageExchangeEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ProcessMessageExchangeAdapter implements
		EventDetailMapping<ProcessMessageExchangeEvent> {
	
	public static final String ACTIVITY_NAME = "PROCESS_MESSAGE_EXCHANGE";
	
	public Event adoptDetails(Event target, ProcessMessageExchangeEvent source) {
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
