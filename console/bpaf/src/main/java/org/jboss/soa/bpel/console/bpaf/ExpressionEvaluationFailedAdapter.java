/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ExpressionEvaluationFailedEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ExpressionEvaluationFailedAdapter implements EventDetailMapping<ExpressionEvaluationFailedEvent> {
	
	public static final String ACTIVITY_NAME = "EXPRESSION_EVALUATION_FAIL";
	
	public Event adoptDetails(Event target, ExpressionEvaluationFailedEvent source) {
		target.setActivityName(ACTIVITY_NAME);		
		return target;
	}

}
