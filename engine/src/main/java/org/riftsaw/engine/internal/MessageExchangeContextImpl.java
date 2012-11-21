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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

    private ServiceLocator _locator=null;
    
    private static final Log LOG = LogFactory.getLog(MessageExchangeContextImpl.class);
    
    /**
     * This is the constructor.
     * 
     * @param locator The service locator
     */
    public MessageExchangeContextImpl(ServiceLocator locator) {
        _locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    public void invokePartnerUnreliable(PartnerRoleMessageExchange partnerRoleMessageExchange)
        throws ContextException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());
        }

        PartnerRoleChannelImpl channel=(PartnerRoleChannelImpl)
                        partnerRoleMessageExchange.getChannel();
     
        Service service=_locator.getService(partnerRoleMessageExchange.getCaller(),
                            channel.getEndpoint().serviceName,
                            channel.getEndpoint().portName);
        
        if (service != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invoke service="+service);
            }
            
            try {
                Map<String, Node> headerParts = partnerRoleMessageExchange.getRequest().getHeaderParts();
                Map<String, Object> transferedHeaderParts = new HashMap<String, Object>();
                Set<String> keys = headerParts.keySet();
                for (String key : keys) {
                    Element e = partnerRoleMessageExchange.getRequest().getHeaderPart(key);
                    String k;
                    if (e.getNamespaceURI() == null || e.getNamespaceURI().isEmpty()) {
                        k = e.getLocalName();
                    } else {
                        k = "{"+e.getNamespaceURI()+"}" + e.getLocalName();
                    }
                    transferedHeaderParts.put(k, e);
                }
                Element resp=service.invoke(partnerRoleMessageExchange.getOperationName(),
                            partnerRoleMessageExchange.getRequest().getMessage(),
                                    transferedHeaderParts); 
                
                if (partnerRoleMessageExchange.getMessageExchangePattern()
                        == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE) {
                    Message responseMessage = partnerRoleMessageExchange.createMessage(
                            partnerRoleMessageExchange.getOperation().getOutput().getMessage().getQName());
                    responseMessage.setMessage(resp);
                    
                    partnerRoleMessageExchange.reply(responseMessage);
                }
            } catch (Fault f) {
                QName faultName=f.getFaultName();
                javax.wsdl.Fault fault=null;
                
                if (faultName != null) {
                    fault = partnerRoleMessageExchange.getOperation().getFault(faultName.getLocalPart());
                }
                
                QName faultType=null;
                
                if (fault == null) {
                    faultType = new QName(f.getFaultMessage().getNamespaceURI(),
                            f.getFaultMessage().getLocalName());
                    
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Fault type from element = "+faultType);
                    }
                } else {
                    faultType = fault.getMessage().getQName();
                    
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Fault type from message QName = "+faultType);
                    }
                }
                
                Message faultMessage = partnerRoleMessageExchange.createMessage(
                        faultType);
                faultMessage.setMessage(f.getFaultMessage());
                
                partnerRoleMessageExchange.replyWithFault(faultName, faultMessage);
            } catch (Exception e) {
                throw new ContextException("Failed to invoke external service", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invokePartnerReliable(PartnerRoleMessageExchange mex) throws ContextException {
        // TODO: tie in to WS-RELIABLE* stack. 
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void invokePartnerTransacted(PartnerRoleMessageExchange mex) throws ContextException {
        // TODO: should we check if the partner actually supports transactions?
        invokePartnerUnreliable(mex);
    }    

    /**
     * {@inheritDoc}
     */
    public void onMyRoleMessageExchangeStateChanged(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
        // Add code here to handle MEXs that we've "forgotten" about due to system failure etc.. mostly
        // useful for RELIABLE, but nice to have with ASYNC/BLOCKING as well. 
    }

    /**
     * {@inheritDoc}
     */
    public void cancel(PartnerRoleMessageExchange mex) throws ContextException {

    }

    /**
     * {@inheritDoc}
     */
    public void invokePartner(PartnerRoleMessageExchange mex)
            throws ContextException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoking a partner operation: " + mex.getOperationName());
        }
        invokePartnerUnreliable(mex);
    }

    /**
     * {@inheritDoc}
     */
    public void onAsyncReply(MyRoleMessageExchange mex)
            throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing an async reply from service " + mex.getServiceName());
        }

        // Nothing to do, no callback is necessary, the client just synchornizes itself with the
        // mex reply when invoking the engine.
    }

}
