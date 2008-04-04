/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb.om;


import net.sf.refactorit.ejb.EjbEntityResolver;
import net.sf.refactorit.ejb.RitEjbConstants;
import net.sf.refactorit.ejb.om.parser.RitEjbDescriptorhandler;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.vfs.Source;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Juri Reinsalu
 */
public class RitEjbAppData {

  private Source descriptorXmlSource;

  private String encoding;

  private String lineSeparator;

  private Document descriptorDom;

  private ArrayList sessionEjbs = new ArrayList();

  private ArrayList entityEjbs = new ArrayList();

  private ArrayList messageDrivenEjbs = new ArrayList();

  private StringBuffer versionTag;

  private String fileEnding;// last new line and whitespace if any

  public RitEjbAppData(Source descriptorXml) {
    this.descriptorXmlSource = descriptorXml;
    initAppData();
  }

  private void initAppData() {
    versionTag = extractVersionTag();
    encoding = extractEncoding(versionTag);
    lineSeparator = extractLineSeparator(descriptorXmlSource);
    fileEnding = extractFileEnding();

    try {
      XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
          .getXMLReader();

      RitEjbDescriptorhandler ritEjbDescriptorhandler =
          new RitEjbDescriptorhandler();

      ritEjbDescriptorhandler.setBeansCreationListener(this);

      xmlReader.setContentHandler(ritEjbDescriptorhandler);
      xmlReader.setEntityResolver(new EjbEntityResolver());

      xmlReader.parse(new InputSource(
          new InputStreamReader(descriptorXmlSource.getInputStream())));
    } catch (Exception e) {
      e.printStackTrace();
    }

    // descriptorDom=DomUtil.getDOM(descriptorNoDoctype);
    // createOMfromDescriptorDOM();
  }

  public void beanCreated(RitEjbData bean) {
    if (bean instanceof RitEntityEjbData) {
      entityEjbs.add(bean);
    } else if (bean instanceof RitEjbSessionAndEntityData) {
      sessionEjbs.add(bean);
    } else if (bean != null) {
      messageDrivenEjbs.add(bean);
    }
  }

  private String extractFileEnding() {
    InputStream fis = null;
    InputStreamReader reader = null;
    StringBuffer ret = null;
    try {
      fis = descriptorXmlSource.getInputStream();
      reader = new InputStreamReader(fis);
      int c;
      while ((c = reader.read()) != -1 && '>' != (char) c) {
        ;
      }
      if (c == '>')
        ret = new StringBuffer();
      while ((c = reader.read()) != -1) {
        if (c == '>')
          ret = new StringBuffer();
        else
          ret.append((char) c);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (fis != null)
          fis.close();
      } catch (Exception e) {
      }
    }
    return ret == null ? "" : ret.toString();
  }

  private String extractLineSeparator(Source source) {
    InputStream fis = null;
    InputStreamReader reader = null;
    try {
      fis = source.getInputStream();
      reader = new InputStreamReader(fis);
      char c;
      while ((c = (char) reader.read()) != -1) {
        if (c != '\r') {
          continue;
        }
        if ((c = (char) reader.read()) == '\n') {
          return "\r\n";
        }
        return "\r";
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (fis != null)
          fis.close();
      } catch (Exception e) {
      }
    }
    return getSystemLineSeparator();
  }

  private String getSystemLineSeparator() {
    return (String) AccessController.doPrivileged(
        new PrivilegedAction() {
          public Object run() {
            return System.getProperty("line.separator");
          }
        });
  }

  private String extractEncoding(StringBuffer versionTag) {
    String vtagStr = versionTag.toString().toLowerCase().replaceAll("'", "\"")
        .replaceAll(" ", "");
    int encodingStart = vtagStr.indexOf("encoding=\"") + 10;
    int encodingEnd = vtagStr.indexOf('"', encodingStart);
    return vtagStr.substring(encodingStart, encodingEnd);
  }

  private StringBuffer extractVersionTag() {
    StringBuffer sb = new StringBuffer();
    InputStream fis = null;
    InputStreamReader reader = null;
    try {
      fis = descriptorXmlSource.getInputStream();
      reader = new InputStreamReader(fis);
      char c;
      boolean inVersionTag = false;
      while ((c = (char) reader.read()) != -1) {
        if (!inVersionTag && c == '<' && (c = (char) reader.read()) != -1
            && c == '?' && (c = (char) reader.read()) != -1 && c == 'x'
            && (c = (char) reader.read()) != -1 && c == 'm'
            && (c = (char) reader.read()) != -1 && c == 'l'
            && (c = (char) reader.read()) != -1) {
          sb.append("<?xml");
          inVersionTag = true;
        }
        if (!inVersionTag)
          continue;
        sb.append(c);
        if (c == '>' && sb.charAt(sb.length() - 2) == '?')
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (fis != null)
          fis.close();
      } catch (Exception e) {
      }
    }
    return sb;
  }

  // private void createOMfromDescriptorDOM() {
  // NodeList beansList =
  // descriptorDom.getElementsByTagName("enterprise-beans");
  // Element ejbsNode = (Element) beansList.item(0);
  // addEjbsByTypeToOM(ejbsNode, "session");
  // addEjbsByTypeToOM(ejbsNode, "entity");
  // addEjbsByTypeToOM(ejbsNode, "message-driven");
  // }

  // /**
  // * @param ejbsNode
  // * @param ejbType
  // */
  // private void addEjbsByTypeToOM(Element ejbsNode, String ejbType) {
  // NodeList beanNodesList = ejbsNode.getElementsByTagName(ejbType);
  // if (beanNodesList.getLength() > 0) {
  // if (ejbType.equals("session")) {
  // if (sessionEjbs == null)
  // sessionEjbs = new ArrayList(beanNodesList.getLength());
  // addAllBeansFromNodeListToOM(beanNodesList, sessionEjbs);
  // } else if (ejbType.equals("entity")) {
  // if (entityEjbs == null)
  // entityEjbs = new ArrayList(beanNodesList.getLength());
  // addAllBeansFromNodeListToOM(beanNodesList, entityEjbs);
  // } else if (ejbType.equals("message-driven")) {
  // if (messageDrivenEjbs == null)
  // messageDrivenEjbs = new ArrayList(beanNodesList.getLength());
  // addAllBeansFromNodeListToOM(beanNodesList, messageDrivenEjbs);
  // }
  // }
  // }

  // /**
  // * @param fromNodesList
  // */
  // private void addAllBeansFromNodeListToOM(NodeList fromNodesList,
  // Collection toOMCollection) {
  // for (int i = 0; i < fromNodesList.getLength(); i++) {
  // Element sessionEjbNode = (Element) fromNodesList.item(i);
  // RitEjbData sessionRitEjbData = constructRitEjbDataObject(sessionEjbNode);
  // toOMCollection.add(sessionRitEjbData);
  // }
  // }

  // public RitEjbData constructRitEjbDataObject(Element xmlElement) {
  // String nodeName = xmlElement.getNodeName();
  // if (nodeName.equals("session")) {
  // return new RitEjbSessionAndEntityData(xmlElement);
  // } else if (nodeName.equals("entity")) {
  // return new RitEntityEjbData(xmlElement);
  // }
  // return new RitEjbData(xmlElement);
  // }

  public Source getDescriptorXml() {
    return this.descriptorXmlSource;
  }

  public boolean isInEjbDeclarations(String fullyQualifiedTypeName) {
    if (fullyQualifiedTypeName == null)
      throw new NullPointerException(
          "fullyQuolifiedTypeName argument should't be null !");
    return isInEjbsMetadata(sessionEjbs, fullyQualifiedTypeName)
        || isInEjbsMetadata(entityEjbs, fullyQualifiedTypeName)
        || isInEjbsMetadata(messageDrivenEjbs, fullyQualifiedTypeName);
  }

  public Document getCurrentDom() {
    return descriptorDom;
  }

  private boolean isInEjbsMetadata(Collection ejbs,
      String fullyQualifiedTypeName) {
    if (ejbs == null)
      return false;
    Iterator i = ejbs.iterator();
    while (i.hasNext()) {
      RitEjbData ritEjbData = (RitEjbData) i.next();
      if (ritEjbData.isRelatedType(fullyQualifiedTypeName))
        return true;
    }
    return false;
  }

  private Collection getRelatedEjbs(Collection ejbsFrom,
      String fullyQualifiedTypeName) {
    ArrayList retList = new ArrayList();
    Iterator i = ejbsFrom.iterator();
    while (i.hasNext()) {
      RitEjbData ritEjbData = (RitEjbData) i.next();
      if (ritEjbData.isRelatedType(fullyQualifiedTypeName))
        retList.add(ritEjbData);
    }
    return retList;
  }

  private Collection getRelatedEjbs(Collection ejbsFrom, int partType,
      String fqTypeName) {
    ArrayList retList = new ArrayList();
    Iterator i = ejbsFrom.iterator();
    while (i.hasNext()) {
      RitEjbData ritEjbData = (RitEjbData) i.next();
      if (fqTypeName.equals(ritEjbData.getPartValue(partType)))
        retList.add(ritEjbData);
    }
    return retList;
  }

  private Collection getEjbsByLocalHomeInterface(Collection ejbsFrom,
      String fqLocalHomeInterfaceName) {
    ArrayList retList = new ArrayList();
    Iterator i = ejbsFrom.iterator();
    while (i.hasNext()) {
      RitEjbSessionAndEntityData ritEjbData = (RitEjbSessionAndEntityData) i
          .next();
      if (ritEjbData.getLocalHomeInterface().equals(fqLocalHomeInterfaceName))
        retList.add(ritEjbData);
    }
    return retList;
  }

  /**
   * @return Returns the encoding.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @return Returns the versionTag.
   */
  public StringBuffer getVersionTag() {
    return versionTag;
  }

  /**
   * @return Returns the lineSeparator.
   */
  public String getLineSeparator() {
    return lineSeparator;
  }

  /**
   * @return Returns the sessionEjbs.
   */
  public Collection getSessionEjbs() {
    return sessionEjbs;
  }

  /**
   * @return Returns the entityEjbs.
   */
  public Collection getEntityEjbs() {
    return entityEjbs;
  }

  public Collection getRelatedEjbs(String fullyQualifiedTypeName) {
    ArrayList retList = new ArrayList();
    retList.addAll(getRelatedEjbs(sessionEjbs, fullyQualifiedTypeName));
    retList.addAll(getRelatedEjbs(entityEjbs, fullyQualifiedTypeName));
    retList.addAll(getRelatedEjbs(messageDrivenEjbs, fullyQualifiedTypeName));
    return retList;
  }

  /**
   *
   * @param partType
   * @param fqPartTypeName -
   *          fully qualified ejb part (ejb class, interface or primary key
   *          class) type name
   * @return Collection
   */
  public Collection getRelatedEjbs(int partType, String fqPartTypeName) {
    ArrayList retList = new ArrayList();
    retList.addAll(getRelatedEjbs(sessionEjbs, partType, fqPartTypeName));
    retList.addAll(getRelatedEjbs(entityEjbs, partType, fqPartTypeName));
    retList.addAll(getRelatedEjbs(messageDrivenEjbs, partType, fqPartTypeName));
    return retList;
  }

  public Collection getEjbsByBeanClass(String fullyQualifiedTypeName) {
    ArrayList retList = new ArrayList();
    retList.addAll(getRelatedEjbs(sessionEjbs, RitEjbConstants.EJB_CLASS,
        fullyQualifiedTypeName));
    retList.addAll(getRelatedEjbs(entityEjbs, RitEjbConstants.EJB_CLASS,
        fullyQualifiedTypeName));
    retList.addAll(getRelatedEjbs(messageDrivenEjbs, RitEjbConstants.EJB_CLASS,
        fullyQualifiedTypeName));
    return retList;
  }

  public Collection getEjbsByLocalHomeInterface(String fullyQualifiedTypeName) {
    ArrayList retList = new ArrayList();
    retList.addAll(getEjbsByLocalHomeInterface(sessionEjbs,
        fullyQualifiedTypeName));
    retList.addAll(getEjbsByLocalHomeInterface(entityEjbs,
        fullyQualifiedTypeName));
    return retList;
  }

  public String getFileEnding() {
    return fileEnding;
  }

  private Object getTypeValueAt(Collection from, SourceCoordinate coordinate) {
    Iterator i = from.iterator();
    while (i.hasNext()) {
      RitEjbData element = (RitEjbData) i.next();
      Object part = element.getPartAt(coordinate);
      if (part != null)
        return part;
    }
    return null;
  }

  public Object getTypeValueAt(SourceCoordinate coordinate) {
    Object ret = getTypeValueAt(sessionEjbs, coordinate);
    if (ret != null)
      return ret;
    ret = getTypeValueAt(entityEjbs, coordinate);
    if (ret != null)
      return ret;
    ret = getTypeValueAt(messageDrivenEjbs, coordinate);
    return ret;
  }

  /**
   * @param fullyQualifiedName
   * @return Locator object
   */
  public LocationRange getStartLocatorFor(String fullyQualifiedName) {
    Collection relatedEjbs = getRelatedEjbs(fullyQualifiedName);
    if (relatedEjbs.size() > 0) {
      RitEjbData ejb = (RitEjbData) relatedEjbs.iterator().next();
      return ejb.getLocationRangeFor(fullyQualifiedName);
    }
    return null;
  }

}
