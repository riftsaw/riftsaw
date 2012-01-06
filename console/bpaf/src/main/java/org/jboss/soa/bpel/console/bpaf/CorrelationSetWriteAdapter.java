package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.CorrelationSetWriteEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * 
 * @author Jeff Yu
 *
 */
public final class CorrelationSetWriteAdapter implements EventDetailMapping<CorrelationSetWriteEvent> {
	
	public static final String ACTIVITY_NAME = "CORRELATION_SET_WRITE";
	
	public Event adoptDetails(Event target, CorrelationSetWriteEvent source) {
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append(source.getKey().getCorrelationSetName());
		
		sbuffer.append("=[");
		String[] value = source.getKey().getValues();
		for (int i = 0; i< value.length; i++) {
			sbuffer.append(value[i]);
			if (i < value.length - 1) {
				sbuffer.append(",");
			}
		}
		sbuffer.append("]");
		
		target.addData(new CorrelationKey(sbuffer.toString()));
		target.setActivityName(ACTIVITY_NAME);
		return target;
	}

}
