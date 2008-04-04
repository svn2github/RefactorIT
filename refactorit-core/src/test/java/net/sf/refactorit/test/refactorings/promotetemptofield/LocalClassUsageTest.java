/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;



import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.promotetemptofield.DependencyAnalyzer;
import net.sf.refactorit.test.Utils;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class LocalClassUsageTest extends TestCase {

  public LocalClassUsageTest(String name) {super(name);
  }

  public static Test suite() {
    return new TestSuite(LocalClassUsageTest.class);
  }

  public void testNoUsedLocalClasses() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int i;",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(0, uses.size());
  }

  public void testUsesLocalClass_constructorInvocation() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class Local{}",
        "    Object i = new Local();",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(2, uses.size());
    assertEquals("Object", ((InvocationData) uses.get(0)).getWhatType().getName());
    assertEquals("Local", ((InvocationData) uses.get(1)).getWhatType().getName());
  }

  public void testUsesLocalClass_className() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class Local{}",
        "    Object i = Local.class;",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(2, uses.size());
    assertEquals("Object", ((InvocationData) uses.get(0)).getWhatType().getName());
    assertEquals("Local", ((InvocationData) uses.get(1)).getWhatType().getName());
  }

  public void testUsesLocalClass_declaration() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class Local{}",
        "    Local i;",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(1, uses.size());
    assertEquals("Local", ((InvocationData) uses.get(0)).getWhatType().getName());
  }

  public void testUsesLocalClass_typecast() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m(Object x) {",
        "    class Local{}",
        "    Object i = (Local) x;",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(2, uses.size());
    assertEquals("Object", ((InvocationData) uses.get(0)).getWhatType().getName());
    assertEquals("Local", ((InvocationData) uses.get(1)).getWhatType().getName());
  }

  public void testUsesNonLocalClass() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    String i;",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, var);
    List uses = supervisor.getInvocationsFor(var);

    assertEquals(1, uses.size());
    assertEquals("String", ((InvocationData) uses.get(0)).getWhatType().getName());
  }

  public void testUsesLocalClass_finalMethod() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class Local{}",
        "    Object i = new Local();",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    assertTrue(new DependencyAnalyzer().usesLocalClasses(var));
  }

  public void testUsesNoLocalClasses_finalMethod() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    Object i = new String();",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    assertFalse(new DependencyAnalyzer().usesLocalClasses(var));
  }

  public void testUsesNoLocalClasses_finalMethod_2() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class OuterLocal {}",
        "    class Local {",
        "      Object i = new OuterLocal();",
        "    }",
        "  }",
        "}"
    });

    BinVariable var = ItemByNameFinder.findVariable(p, "i");

    assertFalse(new DependencyAnalyzer().usesLocalClasses(var));
  }

}
