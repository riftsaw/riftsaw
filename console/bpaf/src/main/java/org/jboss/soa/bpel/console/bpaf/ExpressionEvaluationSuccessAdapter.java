/**
 * 
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ExpressionEvaluationSuccessEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * 
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class ExpressionEvaluationSuccessAdapter implements EventDetailMapping<ExpressionEvaluationSuccessEvent> {
	
	public static final String ACTIVITY_NAME = "Expression_EVALUATION_SUCCESS";
	
	public Event adoptDetails(Event target, ExpressionEvaluationSuccessEvent source) {
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
