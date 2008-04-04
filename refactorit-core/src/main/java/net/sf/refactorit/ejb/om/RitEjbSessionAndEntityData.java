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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author jura
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RitEjbSessionAndEntityData extends RitEjbData {

  public RitEjbSessionAndEntityData() {
    super();
  }
  public RitEjbSessionAndEntityData(Element nodeInDescriptor) {
    super(nodeInDescriptor);
    ejbType = SESSION;
  }

  protected String getTagValue(String tagName) {
    Node tagNode = nodeInDescriptor.getElementsByTagName(tagName).item(0);
    if (tagNode == null)
      return null;
    return tagNode.getFirstChild().getNodeValue().trim();
  }

  public String getPartValue(int partType) {
    String ret = super.getPartValue(partType);
    if (ret != null)
      return ret;
    LocationAwareEjbPart locAwPart = (LocationAwareEjbPart) parts.get(new Integer(partType));
    if (locAwPart == null)
      return null;
    Object partValue = locAwPart.getValue();
    return partValue.toString();
  }

  public String getHomeInterface() {
    return getPartValue(RitEjbConstants.HOME);
  }

  public String getLocalHomeInterface() {
    return getPartValue(RitEjbConstants.LOCAL_HOME);
  }

  public String getRemoteInterface() {
    return getPartValue(RitEjbConstants.REMOTE);
  }

  public String getLocalInterface() {
    return getPartValue(RitEjbConstants.LOCAL);
  }

  public void renameDeclarationPart(int partType, String newValue) {
    if (partType == RitEjbConstants.EJB_CLASS
            || partType == RitEjbConstants.EJB_NAME)
      super.renameDeclarationPart(partType, newValue);
    renameInterface(partType, newValue);
  }

  /**
   * @param interfaceType -
   *          a pick from <code>RitEjbConstants</code>
   * @param newValue
   */
  public void renameInterface(int interfaceType, String newValue) {
    quietValueChange(RitEjbConstants.TAGS[interfaceType], newValue);
  }

  //  public void renameHomeInterface(String newValue) {
  //    quietValueChange("home",newValue);
  //  }
  //  
  //  public void renameLocalHomeInterface(String newValue) {
  //    quietValueChange("local-home",newValue);
  //  }
  //
  //  public void renameRemoteInterface(String newValue) {
  //    quietValueChange("remote",newValue);
  //  }
  //  
  //  public void renameLocalInterface(String newValue) {
  //    quietValueChange("local",newValue);
  //  }

  /**
   * If tag exists it's value is changed, otherwise method quietly returns.
   * 
   * @param newValue
   * @param tagName
   */
  protected void quietValueChange(String tagName, String newValue) {
    Node tag = nodeInDescriptor.getElementsByTagName(tagName).item(0);
    if (tag == null)
      return;
    tag.getFirstChild().setNodeValue(newValue);
  }

  public boolean isRelatedType(String fullyQualifiedType) {
    return super.isRelatedType(fullyQualifiedType)
            || fullyQualifiedType.equals(getHomeInterface())
            || fullyQualifiedType.equals(getLocalHomeInterface())
            || fullyQualifiedType.equals(getRemoteInterface())
            || fullyQualifiedType.equals(getRemoteInterface())
            || fullyQualifiedType.equals(getLocalInterface());
  }
}
