/**
 * 
 */
package org.jboss.soa.bpel.console;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.server.plugin.ProcessHistoryPlugin;
import org.jboss.bpm.monitor.model.BPAFDataSource;
import org.jboss.bpm.monitor.model.DefaultBPAFDataSource;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.State;
import org.jboss.bpm.monitor.model.bpaf.Tuple;
import org.jboss.bpm.monitor.model.json.XYDataSetJSO;
import org.jboss.bpm.monitor.model.metric.Grouping;
import org.jboss.bpm.monitor.model.metric.Timespan;
import org.jboss.bpm.monitor.model.metric.TimespanFactory;
import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.BPELEngineFactory;

/**
 * @author Jeff Yu
 * @date: Mar 17, 2011
 */
public class ProcessHistoryPluginImpl implements ProcessHistoryPlugin {
	
	private BPAFDataSource ds = null;
	
	private BPELEngine engine = null;
	
	public ProcessHistoryPluginImpl() {
	    try
	    {
	      engine = BPELEngineFactory.getEngine();

	      InitialContext ctx = new InitialContext();	      
	      EntityManagerFactory emf  = (EntityManagerFactory)ctx.lookup(JNDINamingUtils.BPEL_EMF);
	      if (null == emf) {
	    	  throw new IllegalStateException("EntityManagerFactory is null");
	      }
	      ds = new DefaultBPAFDataSource(emf);
	    }
	    catch (Exception e)
	    {
	      throw new RuntimeException("Failed to initialize BPAF datasource or BPEL Engine", e);
	    }
	}

	/* (non-Javadoc)
	 * @see org.jboss.bpm.console.server.plugin.ProcessHistoryPlugin#getHistoryProcessInstances(java.lang.String, java.lang.String, long, long, java.lang.String)
	 */
	public List<HistoryProcessInstanceRef> getHistoryProcessInstances(
			String definitionkey, String status, long starttime, long endtime,
			String correlationkey) {
		
		String dkey =  ModelAdaptor.decodeId(definitionkey);
		List<Event> events = ds.getInstanceEvents(dkey, new Timespan(starttime, endtime, "Custom"), getStatus(status));
		List<String> instanceIds = null;
		if (correlationkey != null && !("".equals(correlationkey.trim()))) {
			String ckey = null;
			try {
				ckey = URLDecoder.decode(correlationkey.replace("~", "="), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				throw new IllegalStateException("Decode correlation key of " + correlationkey + " failed.");
			}	
			instanceIds = ds.getProcessInstances(dkey, "correlation-key", ckey);
		}
		
		Set<String> historyInstanceIds = new HashSet<String>();
		
		for(Event e : events)
	    {
			if(instanceIds == null || instanceIds.contains(e.getProcessInstanceID())) {
				if (e.getEventDetails().getCurrentState().equals(getStatus(status))) {
					historyInstanceIds.add(e.getProcessInstanceID());
				}
			}
	    }
		
		List<HistoryProcessInstanceRef> refs = new ArrayList<HistoryProcessInstanceRef>();

		for (String theInstanceID : historyInstanceIds) {
			List<Event> theEvents = ds.getPastActivities(theInstanceID);
			HistoryProcessInstanceRef ref = new HistoryProcessInstanceRef();
			for (Event e : theEvents) {
				ref.setProcessInstanceId(e.getProcessInstanceID());
				ref.setState(status);
				ref.setProcessDefinitionId(e.getProcessDefinitionID());
				for (Tuple tuple : e.getDataElement()) {
					if ("correlation-key".equals(tuple.getName())) {
						ref.setKey(tuple.getValue());
					}
					if ("process-start-time".equals(tuple.getName())) {
						ref.setStartTime(new Date(new Long(tuple.getValue())));
					}
					if ("process-end-time".equals(tuple.getName())) {
						ref.setEndTime(new Date(new Long(tuple.getValue())));
					}
				}
			}
		   refs.add(ref);
		}
		
		return refs;
	}



    public List<ProcessDefinitionRef> getProcessDefinitions() {
        List<ProcessDefinitionRef> refs = new ArrayList<ProcessDefinitionRef>();

        List<String> keys = ds.getProcessDefinitions();

        for (String id : keys) {
            ProcessDefinitionRef ref = new ProcessDefinitionRef();
            ref.setName(id);
            ref.setId(ModelAdaptor.encodeId(id));

            refs.add(ref);
        }

        return refs;
    }

    public List<String> getProcessInstanceKeys(String definitionId) {
        String decodedId = ModelAdaptor.decodeId(definitionId);
        return ds.getProcessInstances(decodedId);
    }

    public List<String> getActivityKeys(String instanceId) {
        return ds.getActivityDefinitions(instanceId);
    }

    public List<String> getAllEvents(String instanceId) {
    	List<Event> events = ds.getPastActivities(instanceId);

    	List<String> result = new LinkedList<String>();
    	for (Event event: events) {
    		for (Tuple tuple : event.getDataElement()) {
    			if ("data".equals(tuple.getName())) {
    				result.add(tuple.getValue());
    			}
    		}
    	}

    	return result;
    }

    public Set<String> getCompletedInstances(String definitionKey, long timestamp, String timespan) {
        return getInstances(definitionKey, timestamp, timespan, State.Closed_Completed);
    }

    public Set<String> getFailedInstances(String definitionKey, long timestamp, String timespan) {

        return getInstances(definitionKey, timestamp, timespan, State.Closed_Completed_Failed);
    }

    public Set<String> getTerminatedInstances(String definitionKey, long timestamp, String timespan) {
        return getInstances(definitionKey, timestamp, timespan, State.Closed_Cancelled_Terminated);
    }

    public String getCompletedInstances4Chart(String processDefinition, String timespanValue) {
        final Timespan timespan = TimespanFactory.fromValue(timespanValue);

        String decodedId = ModelAdaptor.decodeId(processDefinition);

        List<Event> events = ds.getInstanceEvents(decodedId, timespan, State.Closed_Completed);

        return createDatasetJSO(
                new String[]{"Completed"}, timespan, true, events);
    }

    public String getFailedInstances4Chart(String processDefinition, String timespanValue) {
        final Timespan timespan = TimespanFactory.fromValue(timespanValue);
        String decodedId = ModelAdaptor.decodeId(processDefinition);

        List<Event> completed = ds.getInstanceEvents(decodedId, timespan, State.Closed_Completed);
        List<Event> failed  = ds.getInstanceEvents(decodedId, timespan, State.Closed_Completed_Failed);
        List<Event> terminated = ds.getInstanceEvents(decodedId, timespan, State.Closed_Cancelled_Terminated);

        return createDatasetJSO(
                new String[]{"Completed", "Failed", "Terminated"}, timespan, true, completed, failed, terminated);
    }


    private Set<String> getInstances(String definitionKey, long timestamp, String timespan, State completionState) {

        String decodedId = ModelAdaptor.decodeId(definitionKey);

        Set<String> instanceIds = new HashSet<String>();

        Timespan chartTimespan = TimespanFactory.fromValue(timespan);
        long[] bounds = TimespanFactory.getLeftBounds(chartTimespan, new Date(timestamp));

        List<Event> events = ds.getInstanceEvents(
                decodedId,
                new Timespan(bounds[0], bounds[1], chartTimespan.getUnit(), "custom"),
                completionState
        );

        // parity matched, only consider actual 'Closed_...' events
        for(Event e : events)
        {
            if(e.getEventDetails().getCurrentState().equals(completionState))
                instanceIds.add(e.getProcessInstanceID());
        }

        //Retrieve the Correlation information from the process instances.

        Set<String> result = new HashSet<String>();

        for (String instanceId : instanceIds) {
        	List<Event> theEvents = ds.getPastActivities(instanceId);
        	StringBuffer sbuffer = new StringBuffer();
        	sbuffer.append("Instance Id: " + instanceId + " ");
        	addCorrelationInformation(theEvents, sbuffer);
        	result.add(sbuffer.toString());
        }

        return result;
    }


	private void addCorrelationInformation(List<Event> theEvents, StringBuffer sbuffer) {
		for (Event theEvent : theEvents) {
			if ("CORRELATION_SET_WRITE".equals(theEvent.getActivityName())) {
				for (Tuple tuple : theEvent.getDataElement()) {
					if ("correlation-key".equals(tuple.getName())) {
						sbuffer.append(" Correlation key: ");
						sbuffer.append(tuple.getValue());
					}
				}
			}
		}
	}

    private State getStatus(String status) {
		if ("COMPLETED".equalsIgnoreCase(status)) {
			return State.Closed_Completed;
		}
		if ("FAILED".equalsIgnoreCase(status)) {
			return State.Closed_Completed_Failed;
		}
		if ("TERMINATED".equalsIgnoreCase(status)) {
			return State.Closed_Cancelled_Terminated;
		}
		return null;
	}


    private static String createDatasetJSO(String[] title, Timespan timespan, boolean matchParity, List<Event>... events) {

        XYDataSetJSO dataSet = new XYDataSetJSO(
                title,
                UUID.randomUUID().toString()
        );

        for(List<Event> subset : events)
        {

            SortedMap<Date, List<Event>> grouped = group(timespan, subset);

            List<Long> domainData = new ArrayList<Long>(grouped.size());
            List<Long> rangeData = new ArrayList<Long>(grouped.size());
            for(Date d : grouped.keySet())
            {
                domainData.add(d.getTime());

                // if parity matched datasets, then we get Open and Closed events.
                int actualSize = matchParity ? grouped.get(d).size()/2 : grouped.get(d).size();
                rangeData.add(new Integer(actualSize).longValue());
            }

            dataSet.getDomain().add(domainData);
            dataSet.getRange().add(rangeData);
        }

        dataSet.setAxis("date");

        return dataSet.toJSO();
    }

    private static SortedMap<Date, List<Event>> group(Timespan timespan, List<Event> events) {
        SortedMap<Date, List<Event>> grouped;

        switch (timespan.getUnit())
        {
            case HOUR:
                grouped = Grouping.byHour(events, timespan);
                break;
            case DAY:
                grouped = Grouping.byDay(events, timespan);
                break;
            case WEEK:
                grouped = Grouping.byWeek(events, timespan);
                break;
            case MONTH:
                grouped = Grouping.byMonth(events, timespan);
                break;
            default:
                throw new IllegalArgumentException("UNIT not supported: "+timespan.getUnit());

        }
        return grouped;
    }

	public boolean recoveryAction(String iid, String aid, String action) {
		boolean result = true;
		try {
			engine.getManagementInterface().recoverActivity(Long.valueOf(iid), Long.valueOf(aid), action);
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

}
