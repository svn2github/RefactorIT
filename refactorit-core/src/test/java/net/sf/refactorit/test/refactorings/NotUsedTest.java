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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.query.notused.NotUsedIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests Not Used refactoring.
 */
public class NotUsedTest extends TestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(NotUsedTest.class.getName());

  public NotUsedTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(NotUsedTest.class);
    suite.setName("Not Used");
    return suite;
  }

  /* template:
     public void testBugXXX() throws Exception {
    cat.info("Testing bug NXXX");
    final Project project =
   Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #XXX"));
    project.load();

    // test here

    cat.info("SUCCESS");
     }
   */

  public void testBug154() throws Exception {
    cat.info("Testing bug N154");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #154"));
    project.getProjectLoader().build();

    NotUsedIndexer nui = new NotUsedIndexer();
    nui.visit(project);

    Collection methods = nui.getNotUsedMethods();

    Iterator it = methods.iterator();
    int count = 0;
    while (it.hasNext()) {
      BinMethod method = (BinMethod) it.next();
      String methodName = method.getQualifiedName();
      if (methodName.equals("X.method")) {
        ++count;
      }
//      System.err.println("Method: "+methodName);
    }

    if (count != 1 || methods.size() != 1) {
      assertTrue("Wrong list of not used methods reported", false);
    }

    cat.info("SUCCESS");
  }

  public void testBug155() throws Exception {
    cat.info("Testing bug N155");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #155"));
    project.getProjectLoader().build();

    NotUsedIndexer nui = new NotUsedIndexer(new ExcludeFilterRule[0]);
    nui.visit(project);

    Collection methods = nui.getNotUsedMethods();

    int count = 0;
    Iterator it = methods.iterator();
    while (it.hasNext()) {
      BinMethod method = (BinMethod) it.next();
      String methodName = method.getQualifiedName();
//			System.out.println();
      if (methodName.equals("A.method")) {
        ++count;
      }
//      System.err.println("Method: "+methodName);
    }

    if (count != 1 || methods.size() != 1) {
      assertTrue("Wrong list of not used methods reported", false);
    }

    cat.info("SUCCESS");
  }

  public void testMethodInvocationInSubclass() throws Exception {
    cat.info("Testing NotUsed_invocation_in_subclass");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "NotUsed_invocation_in_subclass"));
    project.getProjectLoader().build();

    NotUsedIndexer nui = new NotUsedIndexer(new ExcludeFilterRule[0]);
    nui.visit(project);

    Collection methods = nui.getNotUsedMethods();

    int count = 0;
    Iterator it = methods.iterator();
    while (it.hasNext()) {
      BinMethod method = (BinMethod) it.next();
      String methodName = method.getQualifiedName();
      if (methodName.equals("A.method")) {
        ++count;
      }
//      System.err.println("Method: "+methodName);
    }

    assertTrue("Doesn't check for subclass methods in \"this\" invocation",
        count == 1 && count == methods.size());

    cat.info("SUCCESS");
  }

  /**
   * Tests situation when type is used only inside own declaration.
   */
  public void testUsageInsideTypeOnly() throws Exception {
    cat.info("Testing usage of type inside type only");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/used_inside_only"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {"Test"})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  /**
   * Tests situation when types, methods and fields are used only inside
   * declaring type.
   */
  public void testUsageInsideOnly() throws Exception {
    cat.info("Testing usage of types, methods and fields inside declaring type"
        + " only");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/used_inside_only2"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {"Test"})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug #1364.
   */
  public void testBug1364() throws Exception {
    cat.info("Testing bug #1364");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug1364"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug #1605.
   */
  public void testBug1605() throws Exception {
    cat.info("Testing bug #1605");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug1605"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
//    indexer.setOverridesAndImplementsExcluded(true);
//    indexer.setPublicAndProtectedExcluded(false);
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    Set methods = new TreeSet(new ToStringComparator());
    methods.addAll(indexer.getNotUsedMethods());
    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        methods);

    cat.info("SUCCESS");
  }

  public class ToStringComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      return String.valueOf(o1).compareTo(String.valueOf(o2));
    }
  }


  /**
   * Tests with a subclass that does not get used.
   */
  public void testUnusedSublassA() throws Exception {
    cat.info("Testing usage in of an unused subclass");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/testUnusedSubclassA"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(
        typeRefsForNames(project,
        new String[] {"B"})),
        new HashSet(indexer.getNotUsedTypes()));

    cat.info("SUCCESS");
  }

  public void testUnusedSublassB() throws Exception {
    cat.info("Testing usage in of an unused subclass with an overriding method");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/testUnusedSubclassB"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer
        = new NotUsedIndexer(new ExcludeFilterRule[0]);
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    cat.info("SUCCESS");
  }

  /**
   * Tests situation where only one branch of hierarchy is used.
   */
  public void testTwoBranches() throws Exception {
    cat.info("Testing usage in one of the branches in hierarchy");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/two_branches"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer(new ExcludeFilterRule[0]);
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(
        typeRefsForNames(project,
        new String[] {"C2", "Test"})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

//    final BinCIType a = project.getTypeRefForName("A").getBinCIType();
//    assertEquals("Unused methods",
//                 new HashSet(Arrays.asList(new BinMethod[] {
//                       a.getDeclaredMethod("test", BinParameter.NO_PARAMS)})),
//                 new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  /**
   * Tests new expression.
   */
  public void testNew() throws Exception {
    cat.info("Testing new expression");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/new"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  public void testImplicitConstructorCall() throws Exception {
    cat.info("Testing implicit constructor calls");

    Project project = Utils.createTestRbProjectFromString(
        "public class X {" +
        "  public X() {}" +
        "}" +
        "public class Y extends X {" +
        "  public Y() {}" + // on this line is an implicit X() constructor call
        "}"
        );

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);
    final Collection result = indexer.getNotUsedMethods();

    assertEquals("no unused constructors", 0, result.size());

    cat.info("SUCESS");
  }

  public void testImplicitConstructorCallInImplicitConstructor() throws
      Exception {
    cat.info("Testing implicit constructor calls");

    Project project = Utils.createTestRbProjectFromString(
        "public class X {" +
        "  public X() {}" +
        "}" +
        "public class Y extends X {}"
        );

    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);
    final Collection result = indexer.getNotUsedMethods();

    assertEquals("no unused constructors", 0, result.size());

    cat.info("SUCESS");
  }

  public void testImplementation_search() throws Exception {
    cat.info("Testing NotUsed/implementation_search");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/implementation_search"));
    project.getProjectLoader().build();

    final NotUsedIndexer indexer = new NotUsedIndexer(new ExcludeFilterRule[0]);
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused fields",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedFields()));

    final BinCIType a = project.getTypeRefForName("Vehicle").getBinCIType();
    Set methods = new TreeSet(new ToStringComparator());
    methods.addAll(indexer.getNotUsedMethods());
    assertEquals("Unused methods",
        new HashSet(Arrays.asList(new BinMethod[] {
        a.getDeclaredMethod("start", BinTypeRef.NO_TYPEREFS),
        a.getDeclaredMethod("move", BinTypeRef.NO_TYPEREFS)
    })),
        methods);

    cat.info("SUCCESS");
  }

  public void testBug1695() throws Exception {
    cat.info("Testing NotUsed/bug1695");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/bug1695"));
    project.getProjectLoader().build();

    NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    indexer = new NotUsedIndexer(new ExcludeFilterRule[0]);
    indexer.visit(project);

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  public void testExcludePublicAndProtected() throws Exception {
    cat.info("Testing NotUsed/ExcludePublicAndProtected");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/ExcludePublicAndProtected"));
    project.getProjectLoader().build();

    final BinCIType a = project.getTypeRefForName("A").getBinCIType();
    final NotUsedIndexer indexer = new NotUsedIndexer(
        new ExcludeFilterRule[] {
        new ExcludeFilterRule.PublicRule(), new ExcludeFilterRule.ProtectedRule()});
    indexer.visit(project);

    Set methods = new TreeSet(new ToStringComparator());
    methods.addAll(indexer.getNotUsedMethods());

    assertEquals("Unused types",
        new HashSet(typeRefsForNames(project, new String[] {})),
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        new HashSet(Arrays.asList(new BinMethod[] {
        ((BinClass) a).getDeclaredConstructor(new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0),
        new BinParameter("b", BinPrimitiveType.INT_REF, 0)}),
        ((BinClass) a).getDeclaredConstructor(new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0),
        new BinParameter("b", BinPrimitiveType.INT_REF, 0),
        new BinParameter("c", BinPrimitiveType.INT_REF, 0)}),
        a.getDeclaredMethod("f3", BinTypeRef.NO_TYPEREFS),
        a.getDeclaredMethod("f4", BinTypeRef.NO_TYPEREFS)
    })), methods);

    Set fields = new TreeSet(new ToStringComparator());
    fields.addAll(indexer.getNotUsedFields());

    assertEquals("Unused fields",
        new HashSet(Arrays.asList(new BinField[] {
        a.getDeclaredField("c"),
        a.getDeclaredField("d")})), fields);

    cat.info("SUCCESS");
  }

  /**
   * Gets list of types corresponding to FQNs.
   *
   * @param project project types are resovled with respect to.
   * @param fqns list of FQNs of types.
   *
   * @return list of types ({@link net.sf.refactorit.classmodel.BinTypeRef}
   *         corresponding to the list of FQNs. Never returns <code>null</code>
   */
  private List typeRefsForNames(Project project, String[] fqns) {
    final List types = new ArrayList();
    for (int i = 0, len = fqns.length; i < len; i++) {
      final String fqn = fqns[i];
      final BinTypeRef typeRef = project.findTypeRefForName(fqn);
      if (typeRef == null) {
        throw new IllegalArgumentException("Cannot find type for \""
            + fqn + "\"");
      }
      types.add(typeRef);
    }

    return types;
  }

  public void testUsedPrivateConstructor() throws Exception {
    cat.info("Testing NotUsed/UsedPrivateConstructor");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed_used_private_constructor"));
    project.getProjectLoader().build();

    NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    indexer = new NotUsedIndexer(new ExcludeFilterRule[0]);
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedMethods()));

    cat.info("SUCCESS");
  }

  public void testUseOfUSEDTag1() throws Exception {
    cat.info("Testing NotUsed/UseOfUSEDTag1");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/UseOfUSEDTag1"));
    project.getProjectLoader().build();

    final BinCIType a = project.getTypeRefForName("A").getBinCIType();
    final NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    Set methods = new TreeSet(new ToStringComparator());
    methods.addAll(indexer.getNotUsedMethods());

    assertEquals("Unused methods",
        new HashSet(Arrays.asList(new BinMethod[] {
        a.getDeclaredMethod("f2", BinTypeRef.NO_TYPEREFS) /*,
                  a.getDeclaredMethod("f4", BinTypeRef.NO_TYPEREFS),*/
    })), methods);

    cat.info("SUCCESS");
  }

  public void testUseOfUSEDTag2() throws Exception {
    cat.info("Testing NotUsed/UseOfUSEDTag2");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("NotUsed/UseOfUSEDTag2"));
    project.getProjectLoader().build();

    NotUsedIndexer indexer = new NotUsedIndexer();
    indexer.visit(project);

    assertEquals("Unused types",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    assertEquals("Unused methods",
        Collections.EMPTY_SET,
        new HashSet(indexer.getNotUsedTypes()));

    cat.info("SUCCESS");
  }
}
