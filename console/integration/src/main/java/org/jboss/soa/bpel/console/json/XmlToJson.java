/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.soa.bpel.console.json;

import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XmlToJson
{

  public static String parse(InputStream in)
  {
    try {


      DocumentBuilder builder =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = builder.parse(in);
      Element root = doc.getDocumentElement();

      normalize(root);

      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      Writer writer = new PrintWriter(bout);

      MappedNamespaceConvention con = new MappedNamespaceConvention();
      XMLStreamWriter streamWriter = new ResultAdapter(con, writer);

      Source source = new DOMSource(root);
      //Result output = new StAXResult(streamWriter); JDK 6 only
      Result output = new SAXResult((ContentHandler)streamWriter);

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(source, output);

      streamWriter.flush();
      writer.flush();

      return new String(bout.toByteArray());

    }
    catch (Exception e)
    {
      throw new RuntimeException("XMLToJson failed", e);
    }

  }
  private static void normalize(Node root)
  {
    NodeList childNodes = root.getChildNodes();
    List<Node> tobeRemoved = new ArrayList<Node>();
    for(int i=0; i<childNodes.getLength(); i++)
    {
      Node node = childNodes.item(i);
      short type = node.getNodeType();
      if(Node.ELEMENT_NODE == type)
        normalize(node);
      else if(Node.TEXT_NODE == type)
      {
        if(childNodes.getLength()>1) // mixed content
          tobeRemoved.add(node);
      }

    }

    for(Node n : tobeRemoved)
      root.removeChild(n);
  }

  public static void main(String[] args)
  {
    String xml = "<message>\n" +
        "  <ID>\n" +
        "    <id>1</id>\n" +
        "  </ID>\n" +
        "  <Message>Hello World</Message>\n" +
        "</message>";

    System.out.println( XmlToJson.parse(new ByteArrayInputStream(xml.getBytes())) );
  }
}
