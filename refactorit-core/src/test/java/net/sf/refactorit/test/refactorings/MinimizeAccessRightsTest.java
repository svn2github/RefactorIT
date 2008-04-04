/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccess;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessNode;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessTableModel;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessUtil;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author vadim
 */
public class MinimizeAccessRightsTest extends RefactoringTestCase {
  private static final Category cat =
      Category.getInstance(MinimizeAccessRightsTest.class.getName());

  // FIXME legacy stuff
  private static final String PROJECTS_PATH = "MinimizeAccessRightsTest";
  private String testName = null;

  public static final String checkPossibleEjbMethodsTest =
      "CheckPossibleEjbMethods";

  static final Object[][] protectedAccess = new Object[][] { {"protected"}
  };

  static final Object privateAccess[][] = new Object[][] { {"protected",
      "package private", "private"}
  };

  static final Object packageAccess[][] = { {"protected", "package private"}
  };
  static final Object publicAccess[][] = new Object[][] { {"public"}
  };
  static final Object sameAccess[][] = new Object[][] { {}
  };

  public MinimizeAccessRightsTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "MinimizeAccessRightsTest/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(MinimizeAccessRightsTest.class);
    suite.setName("Minimize Access Rights");
    return suite;
  }

  private void changeAccess(
      String memberName, String className, Object[] selectedAccesses
      ) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();
    BinTypeRef typeRef = findTypeRef(className, project);
    List binMembers = findMembers(memberName, typeRef);

    MinimizeAccess minAccess = new MinimizeAccess(
        new NullContext(project), typeRef.getBinCIType());

    MinimizeAccessTableModel model =
        new MinimizeAccessTableModel(typeRef.getBinCIType(), true);

    List nodes = model.getNodes();
    List selectedNodes = new ArrayList();
    for (int i = 0, max = nodes.size(); i < max; i++) {
      MinimizeAccessNode node = (MinimizeAccessNode) nodes.get(i);
      if (binMembers.contains(node.getBin())) {
        selectedNodes.add(node);
      }
    }

    for (int i = 0, max = selectedNodes.size(); i < max; i++) {
      MinimizeAccessNode node = (MinimizeAccessNode) selectedNodes.get(i);
      node.setSelectedAccess(selectedAccesses[i]);
    }

    minAccess.setNodes(nodes);

    RefactoringStatus status = minAccess.apply();
    assertTrue("perform change: " + status.getAllMessages(), status.isOk());

    final Project expected = getExpectedProject();
    RwRefactoringTestUtils.assertSameSources("", expected, project);
  }

  private void checkPossibleAccessRights(
      String memberName, String className, Object[][] compareThis
      ) throws Exception {
    if (testName == null) {
      return;
    }

    cat.info("Testing " + getStrippedTestName());

    // FIXME some legacy stuff, must be fixed!!!
    final Project project = getProjectForTest(testName);

    BinTypeRef typeRef = findTypeRef(className, project);
    List binMembers = findMembers(memberName, typeRef);

    MinimizeAccessTableModel model =
        new MinimizeAccessTableModel(typeRef.getBinCIType(), true);
    List nodes = new ArrayList(model.getNodes());
    assertNotNull("list of nodes is null", nodes);

    for (int i = 0, max = binMembers.size(); i < max; i++) {
      BinMember member = (BinMember) binMembers.get(i);
      MinimizeAccessNode node = null;
      for (int j = 0, maxJ = nodes.size(); j < maxJ; j++) {
        MinimizeAccessNode o = (MinimizeAccessNode) nodes.get(j);
        if (member == o.getMember()) {
          node = o;
          break;
        }
      }

      if (compareThis[i].length != 0) {
        assertTrue(member.toString()
            + " doesn't have stricter access, this is wrong",
            (node != null));
        Object[] stricter = node.getStricterAccessesAsStrings();
        compareArrays(stricter, compareThis[i], member);
      } else {
        if (node != null) {
          assertTrue(member.toString() + " has stricter access (" +
              Arrays.asList(node.getStricterAccessesAsStrings())
              + "), this is wrong", (node == null));
        }
      }
    }

    cat.info("SUCCESS");
  }

  private BinTypeRef findTypeRef(String className,
      Project project) throws Exception {
    BinTypeRef typeRef = project.findTypeRefForName(className);

    if (typeRef == null) {
      typeRef = findTypeRefForLocalType(className, project);
    }

    if (typeRef == null) {
      throw new RuntimeException("type with name " + className
          + " was not found");
    }

    return typeRef;
  }

  private BinTypeRef findTypeRefForLocalType(String className,
      Project project) throws Exception {
    List localTypes = new ArrayList();
    List definedTypes = project.getDefinedTypes();
    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      BinTypeRef typeRef = (BinTypeRef) definedTypes.get(i);
      BinMethod[] methods = typeRef.getBinCIType().getDeclaredMethods();
      for (int j = 0; j < methods.length; j++) {
        localTypes.addAll(methods[j].getDeclaredTypes());
      }
    }

    for (int i = 0, max = localTypes.size(); i < max; i++) {
      BinType localType = (BinType) localTypes.get(i);
      if (localType.getQualifiedName().equals(className)) {
        return localType.getTypeRef();
      }
    }

    return null;
  }

  private List findMembers(String memberName, BinTypeRef typeRef) {
    List binMembers = new ArrayList();
    List notFoundMembers = new ArrayList();
    StringTokenizer tokens = new StringTokenizer(memberName, ",");
    while (tokens.hasMoreTokens()) {
      String nextToken = tokens.nextToken().trim();
      BinMember foundMember = findMember(typeRef, nextToken);

      if (foundMember == null) {
        notFoundMembers.add(nextToken + " ");
      } else {
        binMembers.add(foundMember);
      }
    }

    if (notFoundMembers.size() > 0) {
      binMembers.clear();
    }

    assertTrue("the following members were not found:" + notFoundMembers,
        (binMembers.size() > 0));

    return binMembers;
  }

  private BinMember findMember(BinTypeRef owner, String memberName) {
    BinMember[] members;

    members = owner.getBinCIType().getDeclaredMethods();
    for (int i = 0; i < members.length; i++) {
      if (memberName.equals(members[i].getName())) {
        return members[i];
      }
    }

    members = owner.getBinCIType().getDeclaredFields();
    for (int i = 0; i < members.length; i++) {
      if (memberName.equals(members[i].getName())) {
        return members[i];
      }
    }

    BinConstructor[] constructors = ((BinClass) owner.getBinCIType()).
        getDeclaredConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (memberName.equals(constructors[i].getName())) {
        return constructors[i];
      }
    }

    BinTypeRef[] inners = owner.getBinCIType().getDeclaredTypes();
    for (int i = 0; i < inners.length; i++) {
      if (memberName.equals(inners[i].getName())) {
        return inners[i].getBinCIType();
      }
    }

    if (owner.getName().equals(memberName)) {
      return owner.getBinCIType();
    }

    return null;
  }

  public static Project getProjectForTest(String testName) throws Exception {
    Project project = Utils.createTestRbProject(PROJECTS_PATH + "/" + testName);
    project.getProjectLoader().build();

    return project;
  }

  private void compareArrays(Object[] compareWith, Object[] compareThis,
      BinMember member) {
    assertEquals("number of access rights is not equal for " + member +
        "(" + Arrays.asList(compareWith) + " vs " + Arrays.asList(compareThis)
        + ")",
        compareWith.length, compareThis.length);

    for (int i = 0; i < compareThis.length; i++) {
      assertEquals("access right is not equal for " + member,
          compareThis[i], compareWith[i]);
    }
  }

  public void testCheckPossibleAccessRights1() throws Exception {
    testName = "CheckPossibleAccessRights1";

    String names1 = "tmp1_1,tmp2_1,tmp3_1,tmp4_1,tmp5_1," +
        "tmp1_2,tmp2_2,tmp3_2,tmp4_2,tmp5_2," +
        "tmp1_3,tmp2_3,tmp3_3,tmp4_3,tmp5_3," +
        "tmp1_4,tmp2_4,tmp3_4,tmp4_4,tmp5_4," +
        "tmp1_5,tmp2_5,tmp3_5,tmp4_5,tmp5_5," +
        "tmp1_6,tmp2_6," +
        "tmp1_7";

    Object[][] accesses1 = { {"protected", "package private", "private"}
        , {"package private", "private"}
        , {"private"}
        , {"private"}
        , {}
        , {"protected", "package private"}
        , {"package private"}
        , {}
        , {}
        , {}
        , {"protected", "package private"}
        , {"package private"}
        , {}
        , {}
        , {}
        , {"protected", "package private"}
        , {"package private"}
        , {}
        , {}
        , {}
        , {"protected", "package private"}
        , {"package private"}
        , {}
        , {}
        , {}
        , {"protected"}
        , {}
        , {}
    };

    checkPossibleAccessRights(names1, "com.p1.Class1", accesses1);
  }

  public void testCheckPossibleAccessRights2() throws Exception {
    testName = "CheckPossibleAccessRights2";
    String names1 = "foo1_1,foo2_1,foo3_1," +
        "foo1_2,foo2_2,foo3_2," +
        "foo1_3,foo2_3,foo3_3";

    Object[][] accesses1 = { {}
        , {"protected"}
        , {"protected", "package private"}
        , {}
        , {"protected"}
        , {"protected", "package private"}
        , {}
        , {"protected"}
        , {"protected", "package private"}
    };

    checkPossibleAccessRights(names1, "com.p1.Class2", accesses1);

    String names2 = "foo1_4,foo2_4,foo3_4,foo1_5,foo2_5,foo3_5";

    Object[][] accesses2 = { {}
        , {"protected"}
        , {"protected"}
        , {}
        , {}
        , {}
    };

    checkPossibleAccessRights(names2, "com.p1.Class11", accesses2);
  }

  public void testCheckPossibleAccessRights3() throws Exception {
    testName = "CheckPossibleAccessRights3";

    checkPossibleAccessRights("f", "com.p1.Class2", new Object[][] { {}
    });
  }

  public void testCheckPossibleAccessRights4() throws Exception {
    testName = "CheckPossibleAccessRights4";

    checkPossibleAccessRights("f", "com.p1.Class1", new Object[][] { {}
    });
    checkPossibleAccessRights("f", "com.p1.Class3",
        new Object[][] { {"protected"}
    });
    checkPossibleAccessRights("f", "com.p2.Class10",
        new Object[][] { {"protected"}
    });
  }

  public void testCheckPossibleAccessRights5() throws Exception {
    testName = "CheckPossibleAccessRights5";

    checkPossibleAccessRights("f", "com.p1.Class1", new Object[][] { {}
    });
    checkPossibleAccessRights("f", "com.p1.Class2",
        new Object[][] { {"package private"}
    });
    checkPossibleAccessRights("f", "com.p1.Class3",
        new Object[][] { {"protected"}
    });
  }

  public void testCheckPossibleAccessRights6() throws Exception {
    testName = "CheckPossibleAccessRights6";

    checkPossibleAccessRights("f", "p1.Class1", new Object[][] { {"protected"}
    });
  }

  public void testCheckPossibleAccessRights7() throws Exception {
    testName = "CheckPossibleAccessRights7";

    checkPossibleAccessRights("f", "p1.Class0", new Object[][] { {"protected"}
    });
  }

  public void testCheckPossibleAccessRights8() throws Exception {
    testName = "CheckPossibleAccessRights8";

    checkPossibleAccessRights("f", "p1.Class1",
        new Object[][] { {"protected", "package private"}
    });
  }

  public void testCheckPossibleAccessRights9() throws Exception {
    testName = "CheckPossibleAccessRights9";

    checkPossibleAccessRights("main", "p1.Class1", new Object[][] { {}
    });
  }

  public void testCheckPossibleAccessRights10() throws Exception {
    testName = "CheckPossibleAccessRights10";

    checkPossibleAccessRights("f1", "p1.Class1",
        new Object[][] { {"protected", "package private"}
    });
  }

  public void testCheckPossibleAccessRights11() throws Exception {
    testName = "CheckPossibleAccessRights11";

    String names = "f1,f2,f3";

    Object[][] accesses = { {}
        , {"protected", "package private"}
        , {"protected", "package private", "private"}
    };

    checkPossibleAccessRights(names, "p1.Class1", accesses);
  }

  public void testCheckPossibleAccessForInner1() throws Exception {
    testName = "CheckPossibleAccessForInner1";

    String names = "Inner1,Inner2,Inner3";

    Object[][] accesses = { {"protected", "package private", "private"}
        , {"protected", "package private"}
        , {}
    };

    checkPossibleAccessRights(names, "p1.A", accesses);
  }

  public void testCheckPossibleAccessIfUsedInAnonymous() throws Exception {
    testName = "CheckPossibleAccessIfUsedInAnonymous";
    checkPossibleAccessRights("a,b,c,d,e,f,g,h,i,j", "a.A",
        new Object[][] { {"protected", "package private", "private"}
        , {"protected", "package private", "private"}
        , {"protected", "package private", "private"}
        , {}
        , {}
        , {"protected"}
        , {"protected"}
        , {"protected"}
        , {"protected"}
        , {"protected"}
    });
  }

  public void testCheckPossibleEjbMethods() throws Exception {
    testName = checkPossibleEjbMethodsTest;

    String names =
        "ejbCreateSome,ejbPostCreateSome,ejbHomeSome,ejbSelect,ejbFindAll";

    Object[][] accesses = { {}
        , {}
        , {}
        , {}
        , {}
    };

    checkPossibleAccessRights(names, "a.MainBean", accesses);
  }

  public void testCheckPossibleEjbMethods_virtualFieldMethods() throws
      Exception {
    testName = checkPossibleEjbMethodsTest;

    checkPossibleAccessRights("getId,setId", "a.MainBean",
        new Object[][] { {}
        , {}
    });
  }

  public void testCheckPossibleEjbMethods_SessionBean() throws Exception {
    testName = checkPossibleEjbMethodsTest;

    checkPossibleAccessRights("ejbHomeSome", "a.SBean", new Object[][] { {}
    });

    // Tests NPE fix
    checkPossibleAccessRights("regularMethod", "a.SBean",
        new Object[][] { {"protected", "package private", "private"}
    });
  }

  public void testCheckPossibleEjbMethods_BeanSubclass() throws Exception {
    testName = checkPossibleEjbMethodsTest;

    checkPossibleAccessRights("ejbHomeSome", "a.SBeanSubclass",
        new Object[][] { {}
    });
  }

  public void testCheckPossibleEjbMethods_bussinessMethods() throws Exception {
    testName = checkPossibleEjbMethodsTest;

    String names =
        "remoteBussinessMethod,localBussinessMethod,notBussinessMethod";

    Object[][] accesses = { {}
        , {}
        , {"protected", "package private", "private"}
    };

    checkPossibleAccessRights(names, "a.MainBean", accesses);
  }

  public void testCheckPossibleEjbMethods_messageBean() throws Exception {
    testName = checkPossibleEjbMethodsTest;

    checkPossibleAccessRights("ejbCreate", "a.MBean", new Object[][] { {}
    });
  }

  public void testReturnValueMutable_MinimizeAccessUtil_findMethodAccessRights() throws
      Exception {
    Project p = getProjectForTest(checkPossibleEjbMethodsTest);
    BinCIType type = p.getTypeRefForName("a.MainBean").getBinCIType();
    BinMethod method = type.getDeclaredMethod("notBussinessMethod",
        BinTypeRef.NO_TYPEREFS);

    int[] result1 = MinimizeAccessUtil.findMethodAccessRights(method,
        new ArrayList());
    int[] result2 = MinimizeAccessUtil.findMethodAccessRights(method,
        new ArrayList());

    assertTrue(result1 != result2);
  }

  public void testCheckPossibleAccessRights12() throws Exception {
    testName = "CheckPossibleAccessRights12";

    checkPossibleAccessRights("isShowSource", "p1.A", new Object[][] { {}
    });
  }

  public void testChangeAccess1() throws Exception {
    String names = "a1,a2,a3";
    Object accesses[][] = { {"protected", "package private", "private"}
        , {"protected", "package private", "private"}
        , {"protected", "package private", "private"}
    };

    checkPossibleAccessRights(names, "A", accesses);

    Object selected[] = {"private", "private", "private"};

    changeAccess(names, "A", selected);
  }

  public void testChangeAccessWithAnnotations() throws Exception {
    String names = "method1,method2,method3";

    Object accesses[][] = { {"protected", "package private", "private"}
        , {"protected", "package private", "private"}
        , {"protected", "package private", "private"}};

    checkPossibleAccessRights(names, "Test", accesses);

    Object selected[] = {"private", "package private", "private"};

    changeAccess(names, "Test", selected);
  }

  public void testCheckPossibleAccessRights13() throws Exception {
    testName = "CheckPossibleAccessRights13";

    String names = "a1,a2,a3,a4,a5,a6";

    Object[][] accesses = { {"protected"}
        , {"protected"}
        , {"protected"}
        , {"protected"}
        , {"protected"}
        , {"protected"}
    };

    checkPossibleAccessRights(names, "p1.A", accesses);
  }

  public void testCheckPossibleAccessRights14() throws Exception {
    testName = "CheckPossibleAccessRights14";

    checkPossibleAccessRights("f1", "p1.A", new Object[][] { {"protected"}
    });
  }

  public void testCheckExplicitConstructorCall() throws Exception {
    testName = "CheckExplicitConstructorCall";

    checkPossibleAccessRights("A", "p1.A", protectedAccess);
  }

  public void testImplicitConstructorCall() throws Exception {
    testName = "bug2027";
    checkPossibleAccessRights("A", "p1.A", new Object[][] { {"protected"}
    });
    checkPossibleAccessRights("doSomething", "p1.A",
        new Object[][] { {"protected"}
    });

  }

  public void testProtectedFromSamePackage() throws Exception {
    testName = "checkProtectedFromSamePackage";

    Object[][] protectedAccess = new Object[][] { {"protected",
        "package private"}
    };
    checkPossibleAccessRights("x", "Point", protectedAccess);
    checkPossibleAccessRights("y", "Point", protectedAccess);

    checkPossibleAccessRights("delta", "Point3d", protectedAccess);

  }

  public void testCheckOverrides() throws Exception {
    testName = "checkExtends1";
    //if ( RefactorItConstants.runNotImplementedTests ) {
    checkPossibleAccessRights("f", "checkExtends1.B",
        sameAccess);

    checkPossibleAccessRights("f", "checkExtends1.A",
        packageAccess);

    //}
  }

  public void testCheckExtendsConstructor() throws Exception {
    testName = "checkExtends2";

    checkPossibleAccessRights("B", "checkExtends2.B",
        privateAccess);
  }

  public void testCheckOverridesInDifferentPackage() throws Exception {
    testName = "checkExtends3A";

    checkPossibleAccessRights("f", "checkExtends3A.p2.B",
        sameAccess);
    checkPossibleAccessRights("f", "checkExtends3A.A",
        protectedAccess);

  }

  public void testCheckProtectedAccessInDifferentPackage() throws Exception {
    testName = "checkExtends4";
    checkPossibleAccessRights("f", "checkExtends4.A",
        sameAccess);
    //checkPossibleAccessRights("f", "checkExtends3.A",
    //                          privateAccess);
  }

  public void testBug2198() throws Exception {
    testName = "bug2198";
    checkPossibleAccessRights("Inner", "a.X", privateAccess);
  }

  public void testIssue596() throws Exception {
    testName = "issue596";
    checkPossibleAccessRights("foo", "A", privateAccess);
    checkPossibleAccessRights("bar", "A", packageAccess);
  }
}
