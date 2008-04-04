/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.test.ProjectMetadata;
import net.sf.refactorit.test.Utils;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Aleksei Sosnovski
 */
public class GenericsInReturnTypeTest extends TestCase {
  private static final String PROJECT_ID = "generics_in_return_type";

  public GenericsInReturnTypeTest(String name) {
    super(name);
  }

  int oldJvmMode = FastJavaLexer.JVM_14;
  private Project project;

  public static Test suite() {
    return new TestSuite(GenericsInReturnTypeTest.class);
  }

  protected void setUp() throws Exception {
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    // INIT PROJECT FROM SOURCE AND PATH
    final ProjectMetadata metadata = Utils.getTestProjects().getProject(
        PROJECT_ID);
    if (metadata == null) {
      fail("Wasn`t able to find project with such id '" + PROJECT_ID + "'");
    }

    project = Utils.createTestRbProject(metadata);
    if (project == null) {
      fail("Failed to create project: " + PROJECT_ID);
    }

    try {
      project.getProjectLoader().build();
    } catch (Exception e) {
      fail("Failed project.load(): " + e);
    }

    assertFalse("Project was not built properly - has errors: "
        + CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()),
        (project.getProjectLoader().getErrorCollector()).hasErrors());
  }

  protected void tearDown() throws Exception {
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  public void test() {
    String qName = "Test2";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    BinMethod[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {

      if (methods[i].getName().equals("main")) {
        MethodVisitor visitor = new MethodVisitor();
        methods[i].accept(visitor);

        for (int j = 0; j < visitor.METHODS.size(); j++) {

          if (((String) visitor.METHODS.get(j)).equals("target2")) {
            String retType = (String) visitor.RETTYPES.get(j);

//            System.out.println(retType);

            assertTrue("Return type must be String, found: "
                + retType,
            retType.equals("String"));
          } else

          if (((String) visitor.METHODS.get(j)).equals("target")) {
            String retType = (String) visitor.RETTYPES.get(j);

//            System.out.println(retType);

            assertTrue("Return type must be Integer, found: "
                + retType,
            retType.equals("Integer"));
          }
        }
      }
    }
  }
}

class MethodVisitor extends BinItemVisitor {
  public ArrayList RETTYPES = new ArrayList();
  public ArrayList METHODS = new ArrayList();

  public void visit(BinMethodInvocationExpression expr) {
    METHODS.add(expr.getMethod().getName());
//    System.out.println(expr.getMethod().getName());
    RETTYPES.add(expr.getReturnType().getName());
    super.visit(expr);
  }
}
