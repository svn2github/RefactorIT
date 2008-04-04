/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.refactorings.EjbUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class EjbUtilTest extends TestCase {
  public EjbUtilTest(String s) {super(s);
  }

  public static Test suite() {
    return new TestSuite(EjbUtilTest.class);
  }
  
  
  Project p = null;
  BinTypeRef beanRef;
  BinClass bean;
  BinMethod ejbFindAll;
  BinMethod ejbSelect;
  BinMethod toStringMethod;
  
  public void setUp(){
    if (p == null){
      try {
        p = MinimizeAccessRightsTest.getProjectForTest(
            MinimizeAccessRightsTest.checkPossibleEjbMethodsTest);
      } catch (Exception e) {
        throw new ChainableRuntimeException(e);
      }

      beanRef = p.getTypeRefForName("a.MainBean");
      bean = (BinClass) beanRef.getBinCIType();
      ejbFindAll = bean.getDeclaredMethod("ejbFindAll", BinTypeRef.NO_TYPEREFS);
      ejbSelect = bean.getDeclaredMethod("ejbSelect", BinTypeRef.NO_TYPEREFS);
      toStringMethod = bean.getDeclaredMethod("toString", BinTypeRef.NO_TYPEREFS);
    }
  }

  public void testIsEntityBean() throws Exception {
    assertTrue(EjbUtil.isEnterpriseBean(bean.getTypeRef()));
    assertFalse(EjbUtil.isEnterpriseBean(p.getObjectRef()));
  }

  public void testRemoteClass() throws Exception {
    assertEquals("a.Main", EjbUtil.getRemoteInterface(beanRef).getQualifiedName());
  }

  public void testLocalClass() throws Exception {
    assertEquals("a.MainLocal",
        EjbUtil.getLocalInterface(beanRef).getQualifiedName());
  }
}
