/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;


import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.commonIDE.IDEController;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 *
 *
 * @author Igor Malinin
 */
public final class Profile {
  public static final String ELEMENT_AUDIT = "audit";
  public static final String ELEMENT_METRICS = "metrics";
  public static final String ELEMENT_ITEM = "item";

  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_ACTIVE = "active";
  public static final String PRIORITY = "priority";


  public static final String DEFAULT_AUDIT_PROFILE = "resources/DefaultAudit.profile";
  public static final String DEFAULT_METRICS_PROFILE = "resources/DefaultMetrics.profile";

  private static Document createDocument(String resource) {
    try {
      InputStream in = Profile.class.getResourceAsStream(resource);
      try {
        return parse(in);
      } finally {
        in.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }

  private static Document parse(final InputStream in)
  throws IOException, SAXException {
    DocumentBuilder builder = createDocumentBuilder();
    Document document = builder.parse(in);

    // HACK JDeveloper adds bogus xml PI which we kill here
    Node item = document.getFirstChild();
    if (item.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
      document.removeChild(item);
    }

    return document;
  }

  private static DocumentBuilder createDocumentBuilder() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setCoalescing(true);
      factory.setIgnoringComments(false);

      return factory.newDocumentBuilder();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private final Document document;
  private String fileName;

  public Profile(Document doc) {
    document = doc;
  }

//  public Profile(String url) throws IOException, SAXException {
//    document = createDocumentBuilder().parse(url);
//  }

  public Profile(File file) throws IOException, SAXException {
    InputStream in = new FileInputStream(file);
    try {
      document = parse(in);
      this.fileName = file.getCanonicalPath();
    } finally {
      in.close();
    }
  }

//  public Profile(InputStream in) throws IOException, SAXException {
//    document = createDocumentBuilder().parse(in);
//  }

  public void serialize(File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try {
      serialize(out);
    } finally {
      out.close();
    }
  }

  public void serialize(OutputStream out) throws IOException {
    IDEController.getInstance().getXMLSerializer().serialize(document, out);
  }

  public Element getAuditItem(String id) {
    return getItem(getAudit(true), id, true);
  }

  public Element getMetricsItem(String id) {
    return getItem(getMetrics(true), id, true);
  }

  public boolean isDefault() {
    return fileName==null;
  }
  
  public boolean isOldVersion(){
      Element section = getAudit(true);
      NodeList list = section.getElementsByTagName(ELEMENT_ITEM);

      for (int i = 0, max = list.getLength(); i < max; i++) {
        Element e = (Element) list.item(i);
        String attr = e.getAttribute(ATTRIBUTE_ID);
        if ((attr == null) || !e.hasAttribute(PRIORITY)
                || "unused_locals".equals(attr)){
            return true;
        }
      }
      return false;
  }

  public void upgradeToNewVersion(){

      final AuditProfileType profileType = new AuditProfileType();
      final Profile defaultProfile = profileType.createDefaultProfile();

      Element section = getAudit(true);
      NodeList list = section.getElementsByTagName(ELEMENT_ITEM);

      for (int i = 0, max = list.getLength(); i < max; i++) {
        Element e = (Element) list.item(i);
        String id = e.getAttribute(ATTRIBUTE_ID);

        if(id.equals("unused_locals")){
            e.setAttribute(ATTRIBUTE_ID, "unused_variable");
            id = e.getAttribute(ATTRIBUTE_ID);
        }

        if (( id != null) && (!e.hasAttribute(PRIORITY))){
            Element item = defaultProfile.getAuditItem(id);
            String priority = item.getAttribute(PRIORITY);
            if(priority == null){
                priority = Priority.NORMAL.getName();
            }
            e.setAttribute(PRIORITY, priority);
        }
      }
  }

  public Element getItem(Element section, String id, boolean create) {
    NodeList list = section.getElementsByTagName(ELEMENT_ITEM);

    Element item = null;
    for (int i = 0, max = list.getLength(); i < max; i++) {
      Element e = (Element) list.item(i);
      if (id.equals(e.getAttribute(ATTRIBUTE_ID))) {
        item = e;
        break;
      }
    }

    if (item == null && create) {
      Node last = section.getLastChild();
      if (last != null && last.getNodeType() != Node.TEXT_NODE) {
        last = null;
      }

      Text text = document.createTextNode("\n    ");
      section.insertBefore(text, last);

      item = document.createElement(ELEMENT_ITEM);
      item.setAttribute(ATTRIBUTE_ID, id);
      section.insertBefore(item, last);
    }

    return item;
  }

  public Element getAudit(boolean create) {
    Element root = document.getDocumentElement();
    Node element = root.getElementsByTagName(ELEMENT_AUDIT).item(0);
    if (element == null && create) {
      element = document.createElement(ELEMENT_AUDIT);
      root.appendChild(element);
    }
    return (Element) element;
  }

  public Element getMetrics(boolean create) {
    Element root = document.getDocumentElement();
    Node element = root.getElementsByTagName(ELEMENT_METRICS).item(0);
    if (element == null && create) {
      element = document.createElement(ELEMENT_METRICS);
      root.appendChild(element);
    }
    return (Element) element;
  }

  public boolean isActiveItem(Element section, String id) {
    return "true".equals(getAttribute(section, id, ATTRIBUTE_ACTIVE));
  }

  public String getAttribute(final Element section, final String id,
      final String attribute) {
    if (section == null) {
      return null;
    }

    Element item = getItem(section, id, false);
    if (item == null) {
      return null;
    }

    return item.getAttribute(attribute);
  }

  public void save() {
    try {
      if(fileName != null) {
        File f = new File(fileName);
        serialize(f);
      }
    } catch(IOException e) {
      e.printStackTrace();
      System.err.println("Can not save options file " + e);
    }
  }

  public static Profile createDefaultAudit() {
    return new Profile(createDocument(DEFAULT_AUDIT_PROFILE));
  }

  public static Profile createDefaultMetrics() {
    return new Profile(createDocument(DEFAULT_METRICS_PROFILE));
  }

  public String getFileName() {
    return this.fileName;
  }
}
