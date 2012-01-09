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
package org.jboss.soa.bpel.console;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.jboss.bpm.console.client.model.ActiveNodeInfo;
import org.jboss.bpm.console.client.model.DiagramInfo;
import org.jboss.bpm.console.server.plugin.GraphViewerPlugin;
import org.jboss.bpm.console.server.plugin.ProcessActivityPlugin;
import org.jboss.bpm.monitor.model.BPAFDataSource;
import org.jboss.bpm.monitor.model.DataSourceFactory;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.Tuple;
import org.jboss.soa.bpel.bpel2svg.BPEL2SVGUtil;
import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.internal.BPELEngineImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGInterface;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Provides the BPEL process images to the console
 * leveraging the {@link org.jboss.soa.bpel.bpel2svg.BPEL2SVGUtil}
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 12, 2010
 */
public class SVGPlugin implements GraphViewerPlugin, ProcessActivityPlugin {

    protected final static Log log = LogFactory.getLog(SVGPlugin.class);
    
    protected final static String WEB_CONTEXT = "/gwt-console-server/rs";

    protected String webServiceHost = null;
    protected int webServicePort = 8080;

    private BPELEngine engine;
    
    private BPAFDataSource bpafDataSource;

    public SVGPlugin() {
    	
    	//TODO: FIXME with real host and port.
        // host & port resolution
        //ServerConfig serverConfig = JBossDSPFactory.getServerConfig();
        //this.webServiceHost = serverConfig.getWebServiceHost();
        //this.webServicePort = serverConfig.getWebServicePort();

        try
        {
            InitialContext ctx = new InitialContext();
            engine = (BPELEngine)ctx.lookup(JNDINamingUtils.BPEL_ENGINE);
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Failed to initialize BPEL engine");
        }

        this.bpafDataSource = DataSourceFactory.createDataSource();
    }

    protected StringBuilder getBaseUrl() {
        StringBuilder spec = new StringBuilder();
        spec.append("http://");
        spec.append(webServiceHost);
        spec.append(":");
        spec.append(webServicePort);
        spec.append(WEB_CONTEXT);
        return spec;
    }

    public byte[] getProcessInstanceImage(String definitionId, String instanceId) {

        QName qName = decode(definitionId);
        SVGInterface svg = createSVG(qName);

        List<Event> executionHistory =
                bpafDataSource.getPastActivities(instanceId);// no need to decode?


        for(Event event : executionHistory)
        {
            for(Tuple t : event.getDataElement())
            {
                if("line-number".equals(t.getName()))
                {
                    Integer line = Integer.valueOf(t.getValue());
                    if(line>0) // TODO: still carries -1 for begin/end 
                    {
                        ActivityInterface activity =
                                svg.getActivityAtLineNumber(line);
                        if(null==activity)
                            throw new RuntimeException("No activity matching line number "+
                                    t.getValue() + " in process "
                                    + qName);

                        activity.setState(ActivityInterface.ActivityState.Completed);
                    }
                }
            }
        }

        return svg.toPNGBytes();
    }

    public byte[] getProcessImage(String processId) {

        QName qName = decode(processId);
        SVGInterface svg = createSVG(qName);
        return svg.toPNGBytes();
    }

    private SVGInterface createSVG(QName qName) {

        // generate new
        InputStream in = getBpelDescriptor(qName);

        SVGInterface svg = null;

        try {
            svg = BPEL2SVGUtil.generate(in);

            if (svg == null)
                log.error("Failed to get SVGInterface");

        } catch(Exception e) {
            throw new RuntimeException("Failed to render process image", e);
        }
        return svg;
    }

    private static QName decode(String processId) {
        String actualId = ModelAdaptor.decodeId(processId);
        QName qName= QName.valueOf(actualId);
        return qName;
    }

    private InputStream getBpelDescriptor(QName qName)
    {
        ProcessConf pconf = ((BPELEngineImpl)engine).getStore().getProcessConfiguration(qName);

        try {
            File deployDir = new File(pconf.getBaseURI());
            File bpelDoc = new File(deployDir, pconf.getBpelDocument());
            return new FileInputStream(bpelDoc);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Faile to load bpel file", e);
        }
    }

    public DiagramInfo getDiagramInfo(String processId) {
        throw new RuntimeException("Not implemented");
    }

    public List<ActiveNodeInfo> getActiveNodeInfo(String instanceId) {
        return Collections.EMPTY_LIST; // Not used
    }

    public URL getDiagramURL(String id) {
        URL result = null;

        StringBuilder sb = getBaseUrl().append("/process/definition/");
        sb.append(id);
        sb.append("/image");

        try {
            result = new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create url", e);
        }

        return result;
    }

    public List<ActiveNodeInfo> getNodeInfoForActivities(String processDefinitionId, List<String> activities) {
        return Collections.EMPTY_LIST; // Not used 
    }
}
