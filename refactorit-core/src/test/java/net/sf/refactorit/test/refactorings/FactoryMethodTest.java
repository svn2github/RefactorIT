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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.factorymethod.FactoryMethod;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test driver for
 * {@link net.sf.refactorit.refactorings.factorymethod.FactoryMethod}.
 *
 * @author Anton Safonov
 */
public class FactoryMethodTest extends RefactoringTestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(FactoryMethodTest.class.getName());

  public FactoryMethodTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(FactoryMethodTest.class);
    suite.setName("FactoryMethod tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public String getTemplate() {
    return "FactoryMethod/<stripped_test_name>/<in_out>";
  }

  private void performCreate(String nativeType, String targetType,
      boolean optimizeVisibility) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    BinTypeRef aRef = project.findTypeRefForName(nativeType);
    BinConstructor cnstr = ((BinClass) aRef.getBinCIType())
        .getDeclaredConstructors()[0];

    final FactoryMethod refactorer
        = new FactoryMethod(cnstr, new NullContext(project));

    RefactoringStatus status = refactorer.checkPreconditions();
    assertTrue("Preconditions check " + status.getAllMessages(), status.isOk());

    refactorer.setMethodName("create" + nativeType);
    refactorer.setHostingClass(
        (BinClass) project.findTypeRefForName(targetType).getBinType());
    refactorer.setOptimizeVisibility(optimizeVisibility);

    status = refactorer.checkUserInput();
    assertTrue("User input check " + status.getAllMessages(), status.isOk());

    status = refactorer.apply();
    assertTrue("Perform change check " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "FactoryMethod", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testBug1866() throws Exception {
    performCreate("Manager", "EmployeeType", true);
  }

  public void testUsedInAnonymous() throws Exception {
    performCreate("Test", "Test", true);
  }

  public void testBug1877() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public abstract class A{ public A(){} }");

    BinConstructor abstractClassConstructor =
        ((BinClass) p.getTypeRefForName("A").getBinType()).
        getDeclaredConstructors()[0];
    FactoryMethod refactorer = new FactoryMethod(
        abstractClassConstructor, new NullContext(p));

    assertFalse(refactorer.checkPreconditions().isOk());
  }
}
