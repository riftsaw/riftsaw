/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.soa.bpel.console.bpaf;

import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.State;
import org.jboss.bpm.monitor.model.bpaf.Tuple;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 21, 2010
 */
public final class InstanceStartAdapter
        implements EventAdapter.EventDetailMapping<ProcessInstanceStartedEvent> {
	
	
	
    public Event adoptDetails(Event target, ProcessInstanceStartedEvent source) {

        Event.EventDetails details = target.getEventDetails();
        details.setCurrentState(State.Open_Running);

        mapDefault(target, source);
        
        Tuple startTime = new Tuple();
        startTime.setName("process-start-time");
        startTime.setValue(String.valueOf(source.getTimestamp().getTime()));
        
        target.addData(startTime);
        
        return target;
    }

    static void mapDefault(Event target, ProcessInstanceEvent source) {
        target.setProcessInstanceID(source.getProcessInstanceId().toString());
    }
}
