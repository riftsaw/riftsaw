package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.PartnerLinkModificationEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.Tuple;
import org.jboss.soa.bpel.console.bpaf.EventAdapter.EventDetailMapping;

/**
 * 
 * @author Jeff Yu
 * @date: Feb 22, 2011
 */
public class PartnerLinkModificationAdapter implements
		EventDetailMapping<PartnerLinkModificationEvent> {
	
	public static final String ACTIVITY_NAME = "PARTNERLINK_MODIFICATION";
	
	public Event adoptDetails(Event target, PartnerLinkModificationEvent source) {
			target.setActivityName(ACTIVITY_NAME);
			Tuple tuple = new Tuple();
			tuple.setName("partner_link_name");
			tuple.setValue(source.getpLinkName());
			target.addData(tuple);
		return target;
	}

}
