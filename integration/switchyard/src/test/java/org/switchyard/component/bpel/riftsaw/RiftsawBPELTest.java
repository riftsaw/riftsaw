/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
package org.switchyard.component.bpel.riftsaw;

import static org.junit.Assert.fail;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.utils.DOMUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.BPELEngineFactory;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.component.bpel.config.model.v1.V1BPELComponentImplementationModel;
import org.switchyard.component.bpel.exchange.BPELExchangeHandler;
import org.switchyard.component.bpel.exchange.BPELExchangeHandlerFactory;
import org.switchyard.test.InvocationFaultException;
import org.switchyard.test.SwitchYardTestCase;
import org.w3c.dom.Element;

/**
 * Tests the Riftsaw BPEL implementation.
 *
 */
public class RiftsawBPELTest extends SwitchYardTestCase {

	private static BPELEngine m_engine=null;
	private static RiftsawServiceLocator m_locator=new RiftsawServiceLocator(null);
	
	@BeforeClass
	public static void runBeforeClass() {
		m_engine = BPELEngineFactory.getEngine();
		
		try {
			m_engine.init(m_locator);
		} catch(Exception e) {
			fail("Failed to initialize the engine: "+e);
		}
	}
	
	@AfterClass
	public static void runAfterClass() {
		try {
			m_engine.close();
		} catch(Exception e) {
			fail("Failed to close down the engine: "+e);
		}
	}
	
    //@Test
    public void testHelloWorldService() throws Exception {
        ServiceDomain serviceDomain = getServiceDomain();
        
        QName qname=new QName("http://www.jboss.org/bpel/examples/wsdl", "HelloService");
        
        BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(serviceDomain);

        BPELComponentImplementationModel bci_model = new V1BPELComponentImplementationModel();
        bci_model.setProcessDescriptor("hello_world/deploy.xml");
        bci_model.setServiceName(qname.toString());
        bci_model.setPortName("HelloPort");
        //bci_model.setVersion("1");

        handler.init(qname, bci_model, m_engine);

        java.net.URL url=RiftsawBPELTest.class.getResource("/hello_world/hello_request1.xml");
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		// Register the service
		registerInOutService("HelloWorld", handler);
		
		Message resp=newInvoker("HelloWorld.hello").sendInOut(new String(b));
		
		if (resp == null) {
			fail("No response returned");
		}
		
		if ((resp.getContent() instanceof Element) == false) {
			fail("Was expecting a DOM element");
		}
		
		Element elem=(Element)resp.getContent();
		
		verifyMessage(elem, "/hello_world/hello_response1.xml");
    }
    
    @Test
    public void testLoanApproval1() throws Exception {
        ServiceDomain serviceDomain = getServiceDomain();
        m_locator.setServiceDomain(serviceDomain);
       
        QName qname=new QName("http://example.com/loan-approval/wsdl/", "loanService");
        
        BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(serviceDomain);

        BPELComponentImplementationModel bci_model = new V1BPELComponentImplementationModel();
        bci_model.setProcessDescriptor("loan_approval/deploy.xml");
        bci_model.setServiceName(qname.toString());
        bci_model.setPortName("loanService_Port");
        //bci_model.setVersion("1");

        handler.init(qname, bci_model, m_engine);

        java.net.URL url=RiftsawBPELTest.class.getResource("/loan_approval/loanreq1.xml");
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		// Register the external services
        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
				fail("Should not be contacting the loan approver");
            }
        });

        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
            	try {
            		Element resp=DOMUtils.stringToDOM(
						"<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
						"         <level>low</level>"+
						"      </wsdl:checkResponse>");
            		
            		Message respMessage=exchange.createMessage();
            		respMessage.setContent(resp);
            		
            		exchange.send(respMessage);
            	} catch(Exception e) {
            		throw new HandlerException(e);
            	}
            }
        });

		// Register the service
		registerInOutService("loanApprovalProcess", handler);
		
		Message resp=newInvoker("loanApprovalProcess.request").sendInOut(new String(b));
		
		if (resp == null) {
			fail("No response returned");
		}
		
		if ((resp.getContent() instanceof Element) == false) {
			fail("Was expecting a DOM element");
		}
		
		Element elem=(Element)resp.getContent();
		
		verifyMessage(elem, "/loan_approval/loanresp1.xml");
    }

    @Test
    public void testLoanApproval2() throws Exception {
        ServiceDomain serviceDomain = getServiceDomain();
        m_locator.setServiceDomain(serviceDomain);
       
        QName qname=new QName("http://example.com/loan-approval/wsdl/", "loanService");
        
        BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(serviceDomain);

        BPELComponentImplementationModel bci_model = new V1BPELComponentImplementationModel();
        bci_model.setProcessDescriptor("loan_approval/deploy.xml");
        bci_model.setServiceName(qname.toString());
        bci_model.setPortName("loanService_Port");
        //bci_model.setVersion("1");

        handler.init(qname, bci_model, m_engine);

        java.net.URL url=RiftsawBPELTest.class.getResource("/loan_approval/loanreq2.xml");
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		// Register the external services
        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
            	try {
            		Element resp=DOMUtils.stringToDOM(
    						"<wsdl:approveResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
    								"<accept>Evaluated and Approved</accept>"+
    								"</wsdl:approveResponse>");
         		
            		Message respMessage=exchange.createMessage();
            		respMessage.setContent(resp);
            		
            		exchange.send(respMessage);
            	} catch(Exception e) {
            		throw new HandlerException(e);
            	}
            }
        });

        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
            	try {
            		Element resp=DOMUtils.stringToDOM(
    						"<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
    								"         <level>high</level>"+
    								"      </wsdl:checkResponse>");
         		
            		Message respMessage=exchange.createMessage();
            		respMessage.setContent(resp);
            		
            		exchange.send(respMessage);
            	} catch(Exception e) {
            		throw new HandlerException(e);
            	}
            }
        });

		// Register the service
		registerInOutService("loanApprovalProcess", handler);
		
		Message resp=newInvoker("loanApprovalProcess.request").sendInOut(new String(b));
		
		if (resp == null) {
			fail("No response returned");
		}
		
		if ((resp.getContent() instanceof Element) == false) {
			fail("Was expecting a DOM element");
		}
		
		Element elem=(Element)resp.getContent();
		
		verifyMessage(elem, "/loan_approval/loanresp2.xml");
    }

    @Test
    public void testLoanApproval3() throws Exception {
        ServiceDomain serviceDomain = getServiceDomain();
        m_locator.setServiceDomain(serviceDomain);
       
        QName qname=new QName("http://example.com/loan-approval/wsdl/", "loanService");
        
        BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(serviceDomain);

        BPELComponentImplementationModel bci_model = new V1BPELComponentImplementationModel();
        bci_model.setProcessDescriptor("loan_approval/deploy.xml");
        bci_model.setServiceName(qname.toString());
        bci_model.setPortName("loanService_Port");
        //bci_model.setVersion("1");

        handler.init(qname, bci_model, m_engine);

        java.net.URL url=RiftsawBPELTest.class.getResource("/loan_approval/loanreq3.xml");
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		// Register the external services
        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
            	try {
            		// TODO: How to record the fault name?
    				//Fault f=new Fault(new QName("http://example.com/loan-approval/wsdl/","loanProcessFault"),

            		Element resp=DOMUtils.stringToDOM(
    						"<message><errorCode><ns1:integer " +
    						"xmlns:ns1=\"http://example.com/loan-approval/xsd/error-messages/\">" +
    						"21000</ns1:integer></errorCode></message>");
         		
            		Message respMessage=exchange.createMessage();
            		respMessage.setContent(resp);
            		
            		exchange.sendFault(respMessage);
            	} catch(Exception e) {
            		throw new HandlerException(e);
            	}
            }
        });

        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
            	try {
            		Element resp=DOMUtils.stringToDOM(
    						"<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
    								"         <level>high</level>"+
    								"      </wsdl:checkResponse>");
         		
            		Message respMessage=exchange.createMessage();
            		respMessage.setContent(resp);
            		
            		exchange.send(respMessage);
            	} catch(Exception e) {
            		throw new HandlerException(e);
            	}
            }
        });

		// Register the service
		registerInOutService("loanApprovalProcess", handler);
		
		Message resp=newInvoker("loanApprovalProcess.request").sendInOut(new String(b));

		// TODO: Why is the response not returned as a InvocationFaultException??
		
		if (resp == null) {
			fail("No response returned");
		}
		
		if ((resp.getContent() instanceof Element) == false) {
			fail("Was expecting a DOM element");
		}
		
		Element elem=(Element)resp.getContent();
		
		verifyMessage(elem, "/loan_approval/loanresp3.xml");
    }

    @Test
    public void testLoanApproval4() throws Exception {
        ServiceDomain serviceDomain = getServiceDomain();
        m_locator.setServiceDomain(serviceDomain);
       
        QName qname=new QName("http://example.com/loan-approval/wsdl/", "loanService");
        
        BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(serviceDomain);

        BPELComponentImplementationModel bci_model = new V1BPELComponentImplementationModel();
        bci_model.setProcessDescriptor("loan_approval/deploy.xml");
        bci_model.setServiceName(qname.toString());
        bci_model.setPortName("loanService_Port");
        //bci_model.setVersion("1");

        handler.init(qname, bci_model, m_engine);

        java.net.URL url=RiftsawBPELTest.class.getResource("/loan_approval/loanreq4.xml");
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		// Register the external services
        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
				fail("Should not be contacting the loan approver");
            }
        });

        serviceDomain.registerService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
        		new BaseHandler(){
            public void handleMessage(Exchange exchange) throws HandlerException {
				fail("Should not be contacting the loan assessor");
            }
        });

		// Register the service
		registerInOutService("loanApprovalProcess", handler);
		
		try {
			newInvoker("loanApprovalProcess.request").sendInOut(new String(b));
			fail("Should have thrown an exception");
		} catch(InvocationFaultException ife) {
					
			if ((ife.getFaultMessage().getContent() instanceof Element) == false) {
				fail("Was expecting a DOM element");
			}
			
			Element elem=(Element)ife.getFaultMessage().getContent();
			
			verifyMessage(elem, "/loan_approval/loanresp4.xml");			
		}
    }

    protected void verifyMessage(Element elem, String responseFile) throws Exception {
		String respText=DOMUtils.domToString(elem);
		
        java.net.URL url=RiftsawBPELTest.class.getResource(responseFile);
		
		java.io.InputStream is=url.openStream();
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();

		String fileText=new String(b);
		
		if (respText.equals(fileText) == false) {
			fail("Response is not as expected:\r\n   response="+respText+"\r\n   file="+fileText);
		}
    }
}
