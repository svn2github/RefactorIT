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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.ui.module.extractsuper.ExtractSuperDialog;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test driver for
 * {@link net.sf.refactorit.refactorings.extractsuper.ExtractSuper
 * ExtractSuper}.
 *
 * @author Anton Safonov
 */
public class ExtractSuperTest extends RefactoringTestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(ExtractSuperTest.class.getName());

  public ExtractSuperTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(ExtractSuperTest.class);
    suite.setName("ExtractSuper tests");
    suite.addTest(ExtractSuperDialog.TestDriver.suite());
    suite.addTest(ExtractSuperWithOldNameTest.suite());
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public String getTemplate() {
    return "ExtractSuper/<stripped_test_name>/<in_out><extra_name>";
  }

  public void performExtractTest(final ExtractSuper extractor,
      final Project project) throws Exception {
    if (extractor.getNewPackageName() == null) {
      extractor.setNewPackageName
          (extractor.getTypeRef().getPackage().getQualifiedName());
    }

    RefactoringStatus status = extractor.checkPreconditions();
    assertNotNull("Preconditions check status is not null", status);
    assertTrue("Allows extract: "
        + status.getAllMessages(), status.isOk());

    status = extractor.checkUserInput();
    assertNotNull("User input check status is not null", status);
    assertTrue("Entered data OK: "
        + status.getAllMessages(), status.isOk());

    status = extractor.apply();
    assertTrue("Extracted successfully", status == null || status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "Extracted super", getExpectedProject(), project);
  }

  /**
   * Tests extracting super class.
   */
  public void testImplementsExtractSuperclass() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("XXX");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting super class.
   */
  public void testSimpleExtractSuperclass() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("XXX");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting interface.
   */
  public void testSimpleExtractInterface() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName(
        "Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("XXX");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting superclass implementing interface.
   */
  public void testSimpleExtend() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting interface extending (and overriding) interface.
   */
  public void testSimpleImplement() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("X");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting superclass extending (and overriding) class.
   */
  public void testSimpleOverride() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests extracting javadoc comments also.
   */
  public void testExtractJavadocs() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    List toExtract = new ArrayList(Arrays.asList(type.getDeclaredFields()));
    toExtract.addAll(Arrays.asList(type.getDeclaredMethods()));
    extractor.setMembersToExtract(toExtract);
    extractor.setNewTypeName("Super");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests creating proxy constructor and abstract method.
   */
  public void testProxyConstructor() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    List toExtract = new ArrayList(Arrays.asList(type.getDeclaredFields()));
    toExtract.addAll(Arrays.asList(type.getDeclaredMethods()));
    extractor.setMembersToExtract(toExtract);
    Set abstractMethods = new HashSet();
    abstractMethods.add(type.getAccessibleMethods("method2", type)[0]);
    extractor.setExplicitlyAbstractMethods(abstractMethods);
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests creating proxy constructor and moving one field named "field".
   */
  public void testProxyConstructor2() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    List toExtract = new ArrayList();
    toExtract.add(type.getDeclaredField("field"));
    extractor.setMembersToExtract(toExtract);
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testAddImplement() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodYYY", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("YYY");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testAddImplementToExtend() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodYYY", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("YYY");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testSimpleExtendButImplementLeft() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testSimpleExtendButImplementLeft2() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("X");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testReplaceExtend() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodA", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("B");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testAddMoreImplementsToExtend() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodZZZ", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("ZZZ");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testReplaceImplement() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List extractMembers = new ArrayList(2);
    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodZ", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);
    extractable = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodX", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Z");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testReplaceExtendAndRemoveImplement() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List extractMembers = new ArrayList(1);
    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Z");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testReplaceExtendAndRemoveImplement2() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("B");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List extractMembers = new ArrayList(1);
    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Z");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testTreatComments() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("A");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor = new ExtractSuper(new NullContext(project),
        typeRef);

    List extractMembers = new ArrayList(1);
    BinMethod extractable = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "compareTo", new BinTypeRef[] {project.getObjectRef() });
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Z");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testBugRIM729() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("A");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor = new ExtractSuper(new NullContext(project),
        typeRef);

    List extractMembers = new ArrayList(1);
    extractMembers.add(MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "b", BinTypeRef.NO_TYPEREFS));
    extractMembers.add(MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "c", BinTypeRef.NO_TYPEREFS));
    extractMembers.add(MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "d", BinTypeRef.NO_TYPEREFS));

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Z");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 1735 where super class names reuses existing place, but leaves
   * old package name infront.
   */
  public void testBug1735() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName(
        "x.X2");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("SuperX");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testReturnTypeIsInnerArray() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName(
        "Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List memberToExtract = new ArrayList();
    memberToExtract.add(type.getDeclaredMethods()[0]);
    extractor.setMembersToExtract(memberToExtract);
    extractor.setNewTypeName("Super");
    extractor.setExtractClass(false);
    extractor.setNewPackageName(extractor.getOldPackageName());

    RefactoringStatus status = extractor.checkPreconditions();
    assertNotNull("Preconditions check status is not null", status);
    assertTrue("Allows extract: "
        + status.getAllMessages(), status.isOk());

    status = extractor.checkUserInput();
    assertNotNull("User input check status is not null", status);
    assertTrue("Entered is not OK: "
        + status.getAllMessages(), status.isErrorOrFatal());

    cat.info("SUCCESS");
  }

  public void testPrivateToProtected() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List extractMembers = new ArrayList(2);
    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);
    extractable = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method1", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("SuperTest");
    extractor.setExtractClass(true);
    extractor.setConvertPrivate(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testImportOfAbstractReturn() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    extractor.setMembersToExtract(Arrays.asList(type.getDeclaredMethods()));
    extractor.setNewTypeName("SuperTest");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testImportOfAbstractReturn2() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    List extractMembers = new ArrayList(2);
    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method1", BinTypeRef.NO_TYPEREFS);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("SuperTest");
    extractor.setExtractClass(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testBug1853() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName(
        "Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);
    ArrayList toExtract = new ArrayList(3);
    toExtract.add(type.getDeclaredMethod("method",
        BinTypeRef.NO_TYPEREFS));
    toExtract.addAll(Arrays.asList(type.getDeclaredFields()));
    extractor.setMembersToExtract(toExtract);
    extractor.setNewTypeName("SuperTest");
    extractor.setExtractClass(true);
    extractor.setConvertPrivate(true);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testExtractJavadocsForInterface() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef
        = project.getTypeRefForName("A");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    extractor.setMembersToExtract(new ArrayList(
        Arrays.asList(type.getDeclaredMethods())));
    extractor.setNewTypeName("I");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testExtractInterfaceFromInterface() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("Interface1");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "methodB", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("Interface0");
    extractor.setExtractClass(false);

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }

  public void testTypeParams1() throws Exception {
    int oldJvm = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    try {
      cat.info("Testing " + getStrippedTestName());

      final Project project = getMutableProject();

      final BinTypeRef typeRef = project.getTypeRefForName("a.b.c.X");
      final BinCIType type = typeRef.getBinCIType();

      final ExtractSuper extractor
          = new ExtractSuper(new NullContext(project), typeRef);

      BinMethod extractable
          = MethodInvocationRules.getMethodDeclaration(
          type, typeRef, "main", BinTypeRef.NO_TYPEREFS);
      List extractMembers = new ArrayList(1);
      extractMembers.add(extractable);

      extractor.setMembersToExtract(extractMembers);
      extractor.setNewTypeName("Super");
      extractor.setExtractClass(true);

      performExtractTest(extractor, project);

      cat.info("SUCCESS");
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvm);
    }
  }

  public void testExtractToNewPackage() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    final BinTypeRef typeRef = project.getTypeRefForName("Test");
    final BinCIType type = typeRef.getBinCIType();

    final ExtractSuper extractor
        = new ExtractSuper(new NullContext(project), typeRef);

    BinMethod extractable
        = MethodInvocationRules.getMethodDeclaration(
        type, typeRef, "method1", BinTypeRef.NO_TYPEREFS);
    List extractMembers = new ArrayList(1);
    extractMembers.add(extractable);

    extractor.setMembersToExtract(extractMembers);
    extractor.setNewTypeName("SuperTest");
    extractor.setExtractClass(true);
    extractor.setNewPackageName("A");

    performExtractTest(extractor, project);

    cat.info("SUCCESS");
  }
}
