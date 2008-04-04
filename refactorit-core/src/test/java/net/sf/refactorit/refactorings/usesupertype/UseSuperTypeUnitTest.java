/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class UseSuperTypeUnitTest extends RefactoringTestCase {

  public static Test suite() {
    final TestSuite suite = new TestSuite(UseSuperTypeUnitTest.class);
    return suite;
  }

  public UseSuperTypeUnitTest() {
    super(UseSuperTypeUnitTest.class.getName());
  }

  public void testOverrides() throws Exception {
    Project project;
    project = getMutableProject();

    BinCIType interfaceType = project.getTypeRefForName(
        "p1.MyInterface").getBinCIType();

    BinCIType classType = project.getTypeRefForName(
        "p1.MyClass3").getBinCIType();

    final String methodName = "size";
    final BinParameter[] methodArgs = BinParameter.NO_PARAMS;
    BinMethod interfaceMethod = interfaceType.getDeclaredMethod(methodName,
        methodArgs);
    BinMethod classMethod = classType.getDeclaredMethod(methodName, methodArgs);

    assertTrue(interfaceMethod != null && classMethod != null);

    final List interfaceOverrides = UseSuperTypeUtil.getAllOverrides(
        interfaceMethod);

    final List classOverrides = UseSuperTypeUtil.getAllOverrides(classMethod);

    Comparator cmp = new Comparator() {

      public int compare(Object obj1, Object obj2) {
        BinMethod m1 = (BinMethod) obj1, m2 = (BinMethod) obj2;

        int result = m1.getOwner().getName().compareTo(m2.getOwner().getName());

        return result;
      }
    };

    Collections.sort(interfaceOverrides, cmp);
    Collections.sort(classOverrides, cmp);

//    System.out.println("interfaceOverrides:"+interfaceOverrides);
//    System.out.println("classOverrides:"+classOverrides);

    assertTrue("interface overrides does not contain " + classMethod + ":" +
        interfaceOverrides, interfaceOverrides.remove(classMethod));

    assertTrue("class overrides does not contain " + interfaceMethod + ":" +
        classOverrides, classOverrides.remove(interfaceMethod));

    assertEquals("Wrong overrides size: "
        + interfaceOverrides.size(), 5, interfaceOverrides.size());

    assertEquals(interfaceOverrides, classOverrides);

  }

  public String getTemplate() {
    return "UseSuperType/unit/<test_name>";
  }

}
