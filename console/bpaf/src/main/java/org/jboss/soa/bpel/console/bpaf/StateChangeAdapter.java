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

import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.State;
import org.jboss.bpm.monitor.model.bpaf.Tuple;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 18, 2010
 */
public class StateChangeAdapter
        implements EventAdapter.EventDetailMapping<ProcessInstanceStateChangeEvent>{

    public Event adoptDetails(Event target, ProcessInstanceStateChangeEvent source) {

        Event.EventDetails details = target.getEventDetails();

        InstanceStartAdapter.mapDefault(target, source);

        switch (source.getNewState())
        {
            case ProcessState.STATE_TERMINATED:
                target.getEventDetails().setCurrentState(State.Closed_Cancelled_Terminated);
                
                Tuple endTime = new Tuple();
                endTime.setName("process-end-time");
                endTime.setValue(String.valueOf(source.getTimestamp().getTime()));
                
                target.addData(endTime);
                
                break;
            /*
            Equivalent to 'ProcessCompletionEvent' with fault!=null 
            case ProcessState.STATE_COMPLETED_WITH_FAULT:
                target.getEventDetails().setCurrentState(State.Closed_Completed_Failed);
                break;*/
            case ProcessState.STATE_SUSPENDED:
                target.getEventDetails().setCurrentState(State.Open_NotRunning_Suspended);
                break;
            /*
            Equivalent to 'ProcessInstanceStartedEvent' 
            case ProcessState.STATE_ACTIVE:
                target.getEventDetails().setCurrentState(State.Open_Running);
                break;*/
            default:
                return null; // not logged
        }

        return target;
    }
}
