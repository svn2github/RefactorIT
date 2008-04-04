/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.parser.FastJavaLexer;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


public class BinItemReferenceTest extends BinItemReferencingTests {
  private Project project;

  public BinItemReferenceTest(String testName) {
    super(testName);
  }

  public static Test suite()  {
    TestSuite suite = new TestSuite(BinItemReferenceTest.class);
    suite.addTest(BinItemReferenceTestOnRefactorItSource.suite());
    return suite;
  }

  public void setUp() throws Exception {
    super.setUp();
    this.project = Utils.createTestRbProject("BinItemReferenceTest");

    try {
      this.project.getProjectLoader().build();
    } catch (Exception e) {
      e.printStackTrace();

      for (Iterator i = (this.project.getProjectLoader().getErrorCollector())
          .getUserFriendlyErrors(); i.hasNext(); ) {
        System.out.println("---- " + i.next());
      }
    }
  }

  public void testParameterizedMethods() throws Exception {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    BinItemReference.cacheEnabled = false;
    try {
      Project project = Utils.createTestRbProjectWithManyFiles(new String[] {
          "Test.java",
          "public class Test {\n" 
          + "public <MT1 extends java.util.List, MT2 extends MT1>" 
          + "   MT1 method(MT1 param1, MT2 param2){\n" 
          + " return null;\n" 
          + "}\n"
          + "\n"
          + "public <MT1 extends java.util.Map, MT2 extends MT1>"
          + "   MT1 method(MT1 param1, MT2 param2){\n"
          + " return null;\n"
          + "}\n"
          + "\n"
          + "public <MT1, MT2> MT1 method(MT1 param1, MT2 param2){\n"
          + " return null;\n"
          + "}\n"
          + "}\n",
          "Test2.java",
          "public class Test2 {\n"
          + "public <A extends Integer & java.util.Map & java.util.Comparable,"
          + "   B extends A, C extends B> void method(A a, B b, C c){\n"
          + " return null;\n"
          + "}\n"
          + "\n"
          + "public <A extends String & java.util.Map, B extends A, C extends B>" 
          + "   void method(A a, B b, C c){\n"
          + " return null;\n"
          + "}\n"
          + "}\n"
      });

      performTestForParametrizedMethods(project, "Test");
      performTestForParametrizedMethods(project, "Test2");

    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }

  private void performTestForParametrizedMethods(final Project project,
      final String className) {
    BinTypeRef testRef = project.getTypeRefForName(className);
    assertNotNull(testRef);

    BinMethod[] methods = testRef.getBinCIType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++){
      BinItemReference methodReference = methods[i].createReference();
      assertEquals(methods[i], methodReference.restore(project));
    }
  }

  protected Project getLoadedProject() {
    return this.project;
  }

  protected List getCompilationUnitsToTest() {
    return project.getCompilationUnits();
  }
}
