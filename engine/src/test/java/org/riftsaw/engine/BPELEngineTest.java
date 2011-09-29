/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.riftsaw.engine;

import static org.junit.Assert.*;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.utils.DOMUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.riftsaw.engine.internal.BPELEngineImpl;
import org.w3c.dom.Element;

public class BPELEngineTest {

    private static BPELEngine m_engine=null;
    private static TestServiceLocator m_locator=new TestServiceLocator();
    
    @BeforeClass
    public static void runBeforeClass() {
        m_engine = BPELEngineFactory.getEngine();
        
        try {
            java.util.Properties props=new java.util.Properties();

            java.io.InputStream is=BPELEngineImpl.class.getClassLoader().getResourceAsStream("bpel.properties");    
            props.load(is);
    
            m_engine.init(m_locator, props);

        } catch (Exception e) {
            fail("Failed to initialize the engine: "+e);
        }
    }
    
    @AfterClass
    public static void runAfterClass() {
        try {
            m_engine.close();
        } catch (Exception e) {
            fail("Failed to close down the engine: "+e);
        }
    }
    
    public DeploymentRef deploy(String descriptor) throws Exception {
        java.net.URL url=BPELEngineImpl.class.getResource(descriptor);
        
        java.io.File deployFile=new java.io.File(url.getFile());
        
        // Deploy the process
        return (m_engine.deploy(deployFile.getParentFile()));
    }
    
    public void invoke(QName serviceName, String portName, String operation, String reqFile, String respFile,
                                QName faultName) throws Exception {
        java.net.URL url=BPELEngineImpl.class.getResource(reqFile);
        
        java.io.InputStream is=url.openStream();
        
        byte[] b=new byte[is.available()];
        is.read(b);
        
        is.close();
    
        org.w3c.dom.Element mesgElem=DOMUtils.stringToDOM(new String(b));
    
        // invoke ODE
        Element resp=null;

        try {
            resp  = m_engine.invoke(serviceName, portName, operation, mesgElem, null);
        } catch (Fault fault) {
            if (faultName == null) {
                fail("Unexpected fault '"+fault.getFaultName()+"' has occurred");
            } else if (faultName.equals(fault.getFaultName()) == false) {
                fail("Fault has occurred, but has different name. Expecting '"+
                                faultName+"' but got '"+fault.getFaultName()+"'");
            } else {
                resp = fault.getFaultMessage();
            }
        }
            
        if (resp != null) {
            if (respFile == null) {
                fail("Response received but no file to verify it against");
            }
            
            String respText=DOMUtils.domToString(resp);
            
            url = BPELEngineImpl.class.getResource(respFile);
            
            is = url.openStream();
            
            b = new byte[is.available()];
            is.read(b);
            
            is.close();
            
            String respFileText=new String(b);
            
            if (respFileText.equals(respText) == false) {
                fail("Responses differ: file="+respFileText+" message="+respText);
            }
        }
    }
    
    @Test
    public void testHelloWorldRedeploy() {
        
        try {
            DeploymentRef ref1=deploy("/hello_world/deploy.xml");
            m_engine.undeploy(ref1);

            DeploymentRef ref2=deploy("/hello_world/deploy.xml");
            m_engine.undeploy(ref2);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    public void testHelloWorld() {
        
        try {
            DeploymentRef ref1=deploy("/hello_world/deploy.xml");
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloService"), "HelloPort",
                    "hello", "/hello_world/hello_request1.xml", "/hello_world/hello_response1.xml", null);
            m_engine.undeploy(ref1);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    public void testHelloWorldVersioned() {
        
        try {
            DeploymentRef ref1=deploy("/hello_world_versioned/deploy.xml");
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloService"), "HelloPort",
                    "hello", "/hello_world_versioned/hello_request1.xml", "/hello_world_versioned/hello_response1.xml", null);
            m_engine.undeploy(ref1);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    public void testSimpleCorrelation() {
        
        try {
            DeploymentRef ref1=deploy("/simple_correlation/deploy.xml");
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "hello", "/simple_correlation/hello_request1.xml", "/simple_correlation/hello_response1.xml", null);
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "goodbye", "/simple_correlation/goodbye_request1.xml", "/simple_correlation/goodbye_response1.xml", null);
            m_engine.undeploy(ref1);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    @org.junit.Ignore
    public void testSimpleCorrelationVersioned() {
        
        try {
            deploy("/version1/simple_correlation/deploy.xml");
            
            // Start process instance 1
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "hello", "/version1/simple_correlation/hello_request1.xml", 
                    "/version1/simple_correlation/hello_response1.xml", null);
            
            DeploymentRef ref2=deploy("/version2/simple_correlation/deploy.xml");
            
            // Start process instance 2
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "hello", "/version2/simple_correlation/hello_request2.xml", 
                    "/version2/simple_correlation/hello_response2.xml", null);
            
            // Complete process instance 2
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "goodbye", "/version2/simple_correlation/goodbye_request2.xml", 
                    "/version2/simple_correlation/goodbye_response2.xml", null);
            
            // Complete process instance 1
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","HelloGoodbyeService"), "HelloGoodbyePort",
                    "goodbye", "/version2/simple_correlation/goodbye_request1.xml", 
                    "/version2/simple_correlation/goodbye_response1.xml", null);
            
            m_engine.undeploy(ref2);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    public void testSimpleInvoke() {
        
        m_locator.clear();
        
        m_locator.addService(new QName("http://simple_invoke/helloworld","HelloWorldWSService"),
                            "HelloWorldPort", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                // TODO Auto-generated method stub
                return DOMUtils.stringToDOM(
                        "<message><sayHelloResponse><ns2:sayHelloResponse xmlns:ns2=\"http://simple_invoke/helloworld\">"+
                        "    <return>Hello Joe Bloggs. Sincerely, JBossWS</return>"+
                        "</ns2:sayHelloResponse></sayHelloResponse></message>");
            }
        });
        
        try {
            DeploymentRef ref1=deploy("/simple_invoke/deploy.xml");
            invoke(new QName("http://www.jboss.org/bpel/examples/wsdl","SimpleInvoke_Service"), "SimpleInvoke_Port",
                    "sayHelloTo", "/simple_invoke/hello_request1.xml", "/simple_invoke/hello_response1.xml", null);
            m_engine.undeploy(ref1);

        } catch (Exception e) {
            fail("Failed: "+e);
        }
    }
    
    @Test
    public void testLoanApproval1() {
        
        m_locator.clear();
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
                            "loanApprover_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                fail("Should not be contacting the loan approver");
                return null;
            }
        });
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
                            "riskAssessor_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                // TODO Auto-generated method stub
                return DOMUtils.stringToDOM(
                        "<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
                        "         <level>low</level>"+
                        "      </wsdl:checkResponse>");
            }
        });

        DeploymentRef ref1=null;
        try {
            ref1=deploy("/loan_approval/deploy.xml");
            invoke(new QName("http://example.com/loan-approval/wsdl/","loanService"), "loanService_Port",
                    "request", "/loan_approval/loanreq1.xml", "/loan_approval/loanresp1.xml", null);
        } catch (Exception e) {
            fail("Failed: "+e);
        } finally {
            try {
                m_engine.undeploy(ref1);
            } catch (Exception e) {
                fail("Failed to undeploy: "+e);
            }
        }
    }
    
    @Test
    public void testLoanApproval2() {
        
        m_locator.clear();
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
                            "loanApprover_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                // TODO Auto-generated method stub
                return DOMUtils.stringToDOM(
                        "<wsdl:approveResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
                                "<accept>Evaluated and Approved</accept>"+
                                "</wsdl:approveResponse>");
            }
        });
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
                            "riskAssessor_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                // TODO Auto-generated method stub
                return DOMUtils.stringToDOM(
                        "<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
                        "         <level>high</level>"+
                        "      </wsdl:checkResponse>");
            }
        });

        DeploymentRef ref1=null;
        try {
            ref1=deploy("/loan_approval/deploy.xml");
            invoke(new QName("http://example.com/loan-approval/wsdl/","loanService"), "loanService_Port",
                    "request", "/loan_approval/loanreq2.xml", "/loan_approval/loanresp2.xml", null);
        } catch (Exception e) {
            fail("Failed: "+e);
        } finally {
            try {
                m_engine.undeploy(ref1);
            } catch (Exception e) {
                fail("Failed to undeploy: "+e);
            }
        }
    }
    
    @Test
    public void testLoanApproval3() {
        
        m_locator.clear();
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
                            "loanApprover_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                
                Fault f=new Fault(new QName("http://example.com/loan-approval/wsdl/","loanProcessFault"),
                            DOMUtils.stringToDOM(
                        "<message><errorCode><ns1:integer xmlns:ns1=\"http://example.com/loan-approval/xsd/error-messages/\">21000</ns1:integer></errorCode></message>"));
                throw f;
            }
        });
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
                            "riskAssessor_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                // TODO Auto-generated method stub
                return DOMUtils.stringToDOM(
                        "<wsdl:checkResponse xmlns:wsdl=\"http://example.com/loan-approval/wsdl/\">"+
                        "         <level>high</level>"+
                        "      </wsdl:checkResponse>");
            }
        });

        DeploymentRef ref1=null;
        try {
            ref1=deploy("/loan_approval/deploy.xml");
            invoke(new QName("http://example.com/loan-approval/wsdl/","loanService"), "loanService_Port",
                    "request", "/loan_approval/loanreq3.xml", "/loan_approval/loanresp3.xml",
                    new QName("http://example.com/loan-approval/wsdl/","unableToHandleRequest"));
        } catch (Exception e) {
            fail("Failed: "+e);
        } finally {
            try {
                m_engine.undeploy(ref1);
            } catch (Exception e) {
                fail("Failed to undeploy: "+e);
            }
        }
    }
    
    @Test
    public void testLoanApproval4() {
        
        m_locator.clear();
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","loanApprover"),
                            "loanApprover_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                
                fail("Should not be contacting the loan approver");
                return null;
            }
        });
        
        m_locator.addService(new QName("http://example.com/loan-approval/wsdl/","riskAssessor"),
                            "riskAssessor_Port", new Service() {

            public Element invoke(String operationName, Element mesg,
                        Map<String, Object> headers) throws Exception {
                fail("Should not be contacting the loan assessor");
                return null;
            }
        });

        DeploymentRef ref1=null;
        try {
            ref1=deploy("/loan_approval/deploy.xml");
            invoke(new QName("http://example.com/loan-approval/wsdl/","loanService"), "loanService_Port",
                    "request", "/loan_approval/loanreq4.xml", "/loan_approval/loanresp4.xml",
                    new QName("http://example.com/loan-approval/wsdl/","unableToHandleRequest"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed: "+e);
        } finally {
            try {
                m_engine.undeploy(ref1);
            } catch (Exception e) {
                fail("Failed to undeploy: "+e);
            }
        }
    }
    
    public static class TestServiceLocator implements ServiceLocator {

        private java.util.Map<String,Service> m_services=new java.util.HashMap<String, Service>();
        
        public void addService(QName serviceName, String portName, Service service) {
            m_services.put(serviceName.toString()+"-"+portName, service);
        }
        
        public Service getService(QName processName, QName serviceName, String portName) {
            return (m_services.get(serviceName.toString()+"-"+portName));
        }
        
        public void clear() {
            m_services.clear();
        }
    }
}
