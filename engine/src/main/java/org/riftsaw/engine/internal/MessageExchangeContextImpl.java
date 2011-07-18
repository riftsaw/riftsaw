/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.riftsaw.engine.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.*;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.w3c.dom.Element;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

	private ServiceLocator m_locator=null;
	
    private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);
    
    public MessageExchangeContextImpl(ServiceLocator locator) {
    	m_locator = locator;
    }

    public void invokePartnerUnreliable(PartnerRoleMessageExchange partnerRoleMessageExchange)
        throws ContextException {
        if (__log.isDebugEnabled())
            __log.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());

        PartnerRoleChannelImpl channel=(PartnerRoleChannelImpl)
        				partnerRoleMessageExchange.getChannel();
        
        Service service=m_locator.getService(channel.getEndpoint().serviceName,
        					channel.getEndpoint().portName);
        
        if (service != null) {
        	if (__log.isDebugEnabled()) {
                __log.debug("Invoke service="+service);
        	}
        	
        	try {
        		Element resp=service.invoke(partnerRoleMessageExchange.getOperationName(),
        					partnerRoleMessageExchange.getRequest().getMessage(),
        							null);
        		
        		if (partnerRoleMessageExchange.getMessageExchangePattern() ==
        				MessageExchange.MessageExchangePattern.REQUEST_RESPONSE) {
        			Message responseMessage = partnerRoleMessageExchange.createMessage(
        					partnerRoleMessageExchange.getOperation().getOutput().getMessage().getQName());
        			responseMessage.setMessage(resp);
        			
        			partnerRoleMessageExchange.reply(responseMessage);
        		}
        	} catch(Fault f) {
        		javax.wsdl.Fault fault=partnerRoleMessageExchange.getOperation().getFault(f.getFaultName().getLocalPart());
     			Message faultMessage = partnerRoleMessageExchange.createMessage(
    					fault.getMessage().getQName());
     			faultMessage.setMessage(f.getFaultMessage());
    			
    			partnerRoleMessageExchange.replyWithFault(f.getFaultName(), faultMessage);
        	} catch(Exception e) {
        		throw new ContextException("Failed to invoke external service", e);
        	}
        }
    }

    public void invokePartnerReliable(PartnerRoleMessageExchange mex) throws ContextException {
        // TODO: tie in to WS-RELIABLE* stack. 
        throw new UnsupportedOperationException();
    }

    public void invokePartnerTransacted(PartnerRoleMessageExchange mex) throws ContextException {
        // TODO: should we check if the partner actually supports transactions?
        invokePartnerUnreliable(mex);
    }

    

    public void onMyRoleMessageExchangeStateChanged(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
        // Add code here to handle MEXs that we've "forgotten" about due to system failure etc.. mostly
        // useful for RELIABLE, but nice to have with ASYNC/BLOCKING as well. 
    }


    public void cancel(PartnerRoleMessageExchange mex) throws ContextException {

    }


	//@Override
	public void invokePartner(PartnerRoleMessageExchange mex)
			throws ContextException {
        if (__log.isDebugEnabled())
            __log.debug("Invoking a partner operation: " + mex.getOperationName());
       invokePartnerUnreliable(mex);
	}

	//@Override
	public void onAsyncReply(MyRoleMessageExchange mex)
			throws BpelEngineException {
        if (__log.isDebugEnabled())
            __log.debug("Processing an async reply from service " + mex.getServiceName());

        // Nothing to do, no callback is necessary, the client just synchornizes itself with the
        // mex reply when invoking the engine.
	}

}
