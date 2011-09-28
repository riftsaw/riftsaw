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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.evt.*;
import org.apache.ode.utils.CollectionUtils;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.Tuple;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 21, 2010
 */
public class EventAdapter {

    protected final static Log log = LogFactory.getLog(EventAdapter.class);

    static Map<Class<?>, EventDetailMapping> detailMapping = new HashMap<Class<?>, EventDetailMapping>();
    
    private static Set<String> excludedMethods = new HashSet<String>();

    static
    {
        detailMapping.put(ProcessInstanceStartedEvent.class, new InstanceStartAdapter());
        detailMapping.put(ProcessCompletionEvent.class, new InstanceEndAdapter());
        detailMapping.put(ProcessTerminationEvent.class, new InstanceTerminationAdapter());

        detailMapping.put(ActivityExecStartEvent.class, new ActivityStartAdapter());
        detailMapping.put(ActivityExecEndEvent.class, new ActivityEndAdapter());
        detailMapping.put(ActivityFailureEvent.class, new ActivityFailedAdapter());

        detailMapping.put(ProcessInstanceStateChangeEvent.class, new StateChangeAdapter());
        
        detailMapping.put(CorrelationSetWriteEvent.class, new CorrelationSetWriteAdapter());
        detailMapping.put(PartnerLinkModificationEvent.class, new PartnerLinkModificationAdapter());
        detailMapping.put(VariableModificationEvent.class, new VariableModificationAdapter());
        detailMapping.put(ProcessMessageExchangeEvent.class, new ProcessMessageExchangeAdapter());
        
        detailMapping.put(ExpressionEvaluationSuccessEvent.class, new ExpressionEvaluationSuccessAdapter());
        detailMapping.put(ExpressionEvaluationFailedEvent.class, new ExpressionEvaluationFailedAdapter());
        
        detailMapping.put(ScopeStartEvent.class, new ScopeStartAdapter());
        detailMapping.put(ScopeCompletionEvent.class, new ScopeCompletionAdapter());
        detailMapping.put(ScopeFaultEvent.class, new ScopeFaultAdapter());
        
        
        excludedMethods.add("getProcessInstanceId");
        excludedMethods.add("getScopeId");
        excludedMethods.add("getScopeDeclarationId");
        excludedMethods.add("getClass");
        excludedMethods.add("getActivityId");
        excludedMethods.add("getProcessId");
        excludedMethods.add("getProcessName");
        excludedMethods.add("getActivityDeclarationId");
        excludedMethods.add("getLineNo");
    }

    public static Event createBPAFModel(BpelEvent bpelEvent)
    {
        Event target = null;
        EventDetailMapping mapping = detailMapping.get(bpelEvent.getClass());

        if(mapping!=null)
        {
            ProcessEvent source = (ProcessEvent)bpelEvent;

            // base event data
            target = new Event(false);
            target.setTimestamp(bpelEvent.getTimestamp().getTime());
            target.setProcessDefinitionID(source.getProcessId().toString());
            target.setProcessName(source.getProcessName().toString());
            
            //set default value for activity name
            target.setActivityName("n/a");

            // required for bpel2svg. See https://jira.jboss.org/browse/RIFTSAW-264
            target.addData(new LineNumber(bpelEvent.getLineNo()));
            
            if (source instanceof ProcessInstanceEvent) {
            	ProcessInstanceEvent instanceEvent = (ProcessInstanceEvent) source;
            	target.setProcessInstanceID(instanceEvent.getProcessInstanceId().toString());
            }
           
           //put the data as a property.
           Tuple data = new Tuple();
           data.setName("data");
           data.setValue(getProcessEventAsString(source));
           
           target.addData(data);
           
            // can return null and thus invalidate the event
            target = mapping.adoptDetails(target, source);
        }
        
        return target;
    }
    
    private static String getProcessEventAsString(ProcessEvent event) {
        StringBuilder sb = new StringBuilder(getEventName(event));

        Method[] methods = event.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0 && !excludedMethods.contains(method.getName())) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(event, CollectionUtils.EMPTY_OBJECT_ARRAY);
                    if (value == null) {
                        continue;
                    }
                    sb.append("~").append(field).append(" = ").append(value == null ? "null" : value.toString());
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        return sb.toString();
    }
    
    
    private static String getEventName(ProcessEvent event) {
    	String name = event.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public interface EventDetailMapping<T extends ProcessEvent>
    {
        Event adoptDetails(Event target, T source);
    }
}
