/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb.om;


import net.sf.refactorit.ejb.RitEjbConstants;
import net.sf.refactorit.source.SourceCoordinate;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Juri Reinsalu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RitEjbData {

  protected Element nodeInDescriptor;
  protected short ejbType;
  final public short ENTITY = 0;
  final public short SESSION = 1;
  final public short MESSAGE_DRIVEN = 2;

  protected HashMap parts = new HashMap();
  protected ArrayList locationAwareParts = new ArrayList();

  private LocationRange range;

  public RitEjbData() {
    range=new LocationRange();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append('{');
    Iterator i = parts.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry element = (Map.Entry) i.next();
      sb.append(RitEjbConstants.TAGS[((Integer) element.getKey()).intValue()]);
      sb.append('=');
      sb.append(element.getValue().toString());
      sb.append(',');
    }
    sb.setLength(sb.length() - 1);
    sb.append('}');
    return sb.toString();
  }

  public RitEjbData(Element nodeInDescriptor) {
    ejbType = MESSAGE_DRIVEN;
    if (nodeInDescriptor == null)
      throw new NullPointerException();
    this.nodeInDescriptor = nodeInDescriptor;
  }

  public String getLogicalName() {
    return getPartValue(RitEjbConstants.EJB_NAME);
  }

  public String getEjbClass() {
    return getPartValue(RitEjbConstants.EJB_CLASS);
  }

  public void setPart(int partType, LocationAwareEjbPart locAwareEjbPart) {
    parts.put(new Integer(partType), locAwareEjbPart);
  }

  /**
   * returns null if not one of mandatory tags(ejb-class or ejb-name)
   * @param partType
   * @return String or null
   */
  public String getPartValue(int partType) {
    if (partType != RitEjbConstants.EJB_CLASS
            && partType != RitEjbConstants.EJB_NAME)
      return null;
    LocationAwareEjbPart locAwPart = (LocationAwareEjbPart) parts
            .get(new Integer(partType));
    if (locAwPart == null)
      return null;
    Object partValue = locAwPart.getValue();
    return partValue.toString();
  }

  public void renameDeclarationPart(int partType, String newValue) {
    //setPartValue(partType, newValue);
  }

  //  public void renameLogicalName(String newName) {
  //    Node ejbName = getMustBeNode("ejb-name");
  //    ejbName.getFirstChild().setNodeValue(newName);
  //  }
  //
  //  public void renameEjbClass(String newFullyQualifiedName) {
  //    Node ejbClass=getMustBeNode("ejb-class");
  //    ejbClass.getFirstChild().setNodeValue(newFullyQualifiedName);
  //  }

  public boolean isRelatedType(String fullyQualifiedType) {
    return fullyQualifiedType.equals(getEjbClass());
  }

  /**
   * @return Returns the ejbType.
   */
  public short getEjbType() {
    return ejbType;
  }

  public void setEndLocator(Locator endLocator) {
    this.range.setEndLocator(endLocator);
  }

  public void setStartLocator(Locator startLocator) {
    this.range.setStartLocator(startLocator);
  }

  public void setPartEndLocator(int partType, Locator locator) {
    ((LocationAwareEjbPart) parts.get(new Integer(partType)))
            .setEndLocator(locator);
  }

  public boolean hasCoordinate(SourceCoordinate coordinate) {
    return range.hasCoordinate(coordinate);
  }

  public Object getPartAt(SourceCoordinate coordinate) {
    Iterator i = parts.values().iterator();
    while (i.hasNext()) {
      LocationAwareEjbPart element = (LocationAwareEjbPart) i.next();
      if (element.hasCoordinate(coordinate))
        return element.getValue();
    }
    return null;
  }

  public Map getEjbPats() {
    return parts;
  }

  public Locator getStartLocator() {
    return this.range.getStartLocator();
  }

  public LocationRange getLocationRangeFor(String fullyQualifiedName) {
    Iterator i=parts.values().iterator();
    while (i.hasNext()) {
      LocationAwareEjbPart part = (LocationAwareEjbPart) i.next();
      if(fullyQualifiedName.equals(part.getValue()))
        return part.getLocationRange(); 
    }
    return  null;
  }

  public String getEjbLocalInterface() {
		return getPartValue(RitEjbConstants.LOCAL);
  }

  public String getEjbRemoteInterface() {
		return getPartValue(RitEjbConstants.REMOTE);
  }
  


}
