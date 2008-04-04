/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb.om.parser;


import net.sf.refactorit.ejb.EjbEntityResolver;
import net.sf.refactorit.ejb.RitEjbConstants;
import net.sf.refactorit.ejb.om.LocationAwareEjbPart;
import net.sf.refactorit.ejb.om.RitEjbAppData;
import net.sf.refactorit.ejb.om.RitEjbData;
import net.sf.refactorit.ejb.om.RitEjbSessionAndEntityData;
import net.sf.refactorit.ejb.om.RitEntityEjbData;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;

import java.util.LinkedList;

/**
 * @author Juri Reinsalu
 */
public class RitEjbDescriptorhandler extends DefaultHandler {

  private RitEjbData currentBean = null;
  private LocationAwareEjbPart currentPart = null;

  private LinkedList curHierarchy;
  private RitEjbAppData beansCreationListener;
  Locator locator;

  public RitEjbDescriptorhandler() {
    this.curHierarchy = new LinkedList();
  }

  public void setBeansCreationListener(RitEjbAppData listener) {
    this.beansCreationListener = listener;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startElement(String uri, String localName, String qName,
          Attributes attributes) throws SAXException {
    curHierarchy.add(qName);
    if (!curHierarchy.getFirst().equals("ejb-jar"))
      throw new SAXException("invalid ejb-descriptor: parent element is <"
              + curHierarchy.getFirst() + "> while should be <ejb-jar>");
    if (curHierarchy.size() == 1)
      return;
    if (!curHierarchy.get(1).equals("enterprise-beans"))
      return;
    if (curHierarchy.size() == 3) {
      if (qName.equals("session")) {
        currentBean = new RitEjbSessionAndEntityData();
      } else if (qName.equals("entity")) {
        currentBean = new RitEntityEjbData();
      } else if (qName.equals("message-driven")) {
        currentBean = new RitEjbData();
      }
      if (currentBean != null)
        currentBean.setStartLocator(locator);
      return;
    }
    if (curHierarchy.size() == 4)
      for (int i = 0; i < RitEjbConstants.TAGS.length; i++) {
        if (qName.equals(RitEjbConstants.TAGS[i])) {
          currentPart = new LocationAwareEjbPart(i, locator);
          currentBean.setPart(i, currentPart);
          break;
        }
      }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (!curHierarchy.getLast().equals(qName)) {
      throw new SAXException("invalid xml: " + curHierarchy.getLast()
              + " was opend but " + qName + " closed");
    }
    curHierarchy.removeLast();
    if (curHierarchy.size() == 2 && currentBean != null) {
      currentBean.setEndLocator(locator);
      fireBean(currentBean);
      currentBean = null;
    }
    if (curHierarchy.size() == 3)
      currentPart=null;
  }

  public void characters(char[] ch, int start, int length) {
    if (currentPart == null)
      return;
    currentPart.setValue(new String(ch,start,length));
    currentPart.setEndLocator(locator);
  }

  private void fireBean(RitEjbData bean) {
    beansCreationListener.beanCreated(bean);
  }

  public static void main(String a[]) throws Exception {
    XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
            .getXMLReader();
    xmlReader.setContentHandler(new RitEjbDescriptorhandler());
    xmlReader.setEntityResolver(new EjbEntityResolver());
    xmlReader.parse("D:\\eclipse_workspace\\test\\META-INF\\ejb-jar.xml");
  }

}
