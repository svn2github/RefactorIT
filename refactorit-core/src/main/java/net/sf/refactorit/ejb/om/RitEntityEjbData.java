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

/**
 * @author jura
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RitEntityEjbData extends RitEjbSessionAndEntityData {
  public RitEntityEjbData() {
    super();
  }
  
  public RitEntityEjbData(Element nodeInDescriptor) {
    super(nodeInDescriptor);
    ejbType = ENTITY;
  }
  
  public boolean isRelatedType(String fullyQualifiedType) {    
    return super.isRelatedType(fullyQualifiedType)
            || fullyQualifiedType.equals(getPrimaryKeyClass());
  }

  public String getPrimaryKeyClass() {
    return getPartValue(RitEjbConstants.PRIM_KEY_CLASS);
  }
  
//  public void renamePrimaryKeyClass(String newValue) {
//    quietValueChange("prim-key-class",newValue);
//  }
  
  
}
