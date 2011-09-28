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
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.Writer;

/**
 * Acts as both a SAX ContentHandler and a StAXResult
 * to bridge between orig xml and Jettison calls.
 */
public class ResultAdapter extends MappedXMLStreamWriter
  implements ContentHandler
{

  public void setDocumentLocator(Locator locator)
  {
    
  }

  public void startDocument() throws SAXException
  {
    try
    {
      super.writeStartDocument();
    }
    catch (XMLStreamException e)
    {
      throw new SAXException(e);
    }
  }

  public void endDocument() throws SAXException
  {
    try
    {
      super.writeEndDocument();
    }
    catch (XMLStreamException e)
    {
      throw new SAXException(e);
    }
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException
  {
    
  }

  public void endPrefixMapping(String prefix) throws SAXException
  {
    
  }

  public void startElement(String uri, String localName, String qName, Attributes atts)
      throws SAXException
  {
    try
    {
      this.writeStartElement(uri, localName);
    }
    catch (XMLStreamException e)
    {
      throw new SAXException(e);
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    try
    {
      this.writeEndElement();
    }
    catch (XMLStreamException e)
    {
      throw new SAXException(e);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    try
    {
      writeCharacters(new String(ch).substring(start,length));
    }
    catch (XMLStreamException e)
    {
       throw new SAXException(e);
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
    
  }

  public void processingInstruction(String target, String data) throws SAXException
  {
    
  }

  public void skippedEntity(String name) throws SAXException
  {
    
  }

  public ResultAdapter(MappedNamespaceConvention convention, Writer writer) {
    super(convention, writer);
  }

  @Override
  public void writeStartElement(String local) throws XMLStreamException
  {
    if (local.contains(":"))
      local = local.substring(local.indexOf(":") + 1);
    super.writeStartElement(local);    
  }

  @Override
  public void writeStartElement(String ns, String local) throws XMLStreamException {
    if (local.contains(":"))
      local = local.substring(local.indexOf(":") + 1);
    super.writeStartElement(ns, local);
  }

  @Override
  public void writeStartElement(String prefix, String local, String ns) throws XMLStreamException {
    if (local.contains(":"))
      local = local.substring(local.indexOf(":") + 1);
    super.writeStartElement(prefix, local, ns);
  }

  @Override
  public void writeCharacters(String text) throws XMLStreamException
  {
    super.writeCharacters(text);
  }

  @Override
  public void writeEndElement() throws XMLStreamException
  {
    super.writeEndElement();
  }
}
