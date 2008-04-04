/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.j2se5.GenericsArgumentsAnalyzer;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.transformations.TransformationManager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class GenericsArgumentsAnalyzerTest extends RefactoringTestCase {
  
  public GenericsArgumentsAnalyzerTest(String name){
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(GenericsArgumentsAnalyzerTest.class);
  }
  
  public String getTemplate() {
    return "GenericsArgumentsAnalyzer/<test_name>/<in_out><extra_name>/";
  }
  
//  public String getName() {
//    return StringUtil.capitalizeFirstLetter(super.getName());
//  }
  int oldJvmMode = FastJavaLexer.JVM_14;
  public void setUp(){
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }
  
  public void tearDown(){
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  private void performTest(String className, String methodName,
      String variableName, boolean shouldSuccess) throws Exception {
    Project project = getInitialProject();
    project.getProjectLoader().build();
    project = getMutableProject(project);
    renameToOut(project);

    assertTrue("has sources", project.getCompilationUnits().size() > 0);
    
    if ((project.getProjectLoader().getErrorCollector()).hasErrors()) {
      fail("shouldn't have errors: " + CollectionUtil.toList(
          (project.getProjectLoader().getErrorCollector())
          .getUserFriendlyErrors()));
    }
    
    BinVariable targetVariable = findTargetVariable(project, className,
        methodName, variableName);
    assertFalse("target variable should have no type arguments",
        targetVariable.getTypeRef().getTypeArguments() != null
        && targetVariable.getTypeRef().getTypeArguments().length > 0);

    GenericsArgumentsAnalyzer analyzer = new GenericsArgumentsAnalyzer(
        targetVariable);
    final RefactoringStatus status = analyzer.run();
 
    final boolean statusOk = status.isOk();
    assertTrue("The analysis failed: " + status.getAllMessages(),
        statusOk || (!statusOk && !shouldSuccess));
    
    if (shouldSuccess){
      TransformationManager manager = new TransformationManager(null);
      analyzer.createEditors(manager);
      manager.performTransformations();

      RwRefactoringTestUtils.assertSameSources("same sources",
          getExpectedProject(getExtraName(className, methodName, variableName)),
          project);
    }
  }
  
  private static String getExtraName(String className, String methodName,
      String variableName) {
    return "_" + className.substring(className.lastIndexOf(".")+1)
        + "_" + methodName + "_" + variableName;
  }
  
  private BinVariable findTargetVariable(Project project,
      String className, String methodName, String variableName){
    BinTypeRef ownerType = project.getTypeRefForName(className);
    assertNotNull("Owner class not found", ownerType);
    BinCIType ownerClass = ownerType.getBinCIType();
    BinMethod[] methods = ownerClass.getDeclaredMethods();
    BinMethod ownerMethod = null;
    for (int i = 0; i < methods.length; i++){
      if (methods[i].getName().equals(methodName)){
        ownerMethod = methods[i];
        break;
      }
    }
    assertNotNull("Owner method not found", ownerMethod);
    BinVariableFinder finder = new BinVariableFinder();
    BinVariable variable = finder.findVariable(ownerMethod, variableName);
    assertNotNull("Variable not found", variable);
    return variable;
  }
  
  // test calls
  public void test0() throws Exception {
    performTest("genericsrefact.test0.Test0", "a", "a", true);
    performTest("genericsrefact.test0.Test0", "b", "a", true);
    performTest("genericsrefact.test0.Test0", "c", "a", true);
  }
  
  public void test0_1() throws Exception {
    performTest("genericsrefact.test0_1.Test0_1", "test0", "a", true);
    performTest("genericsrefact.test0_1.Test0_1", "test1", "a", true);
  }
  
  public void test1() throws Exception {
    performTest("test1.Test1", "main", "a", true);
    performTest("test1.Test1", "main", "t", true);
  }
  
  public void test1_1() throws Exception {
    performTest("genericsrefact.test1_1.Test1_1", "main", "a", true);
  }
  
  public void test2() throws Exception {
    performTest("genericsrefact.test2.Test2", "a", "list", true);
  }
  
  public void test3() throws Exception {
    performTest("genericsrefact.test3.Test3", "main", "outside1", true);
    performTest("genericsrefact.test3.A", "aaa", "smpl1", true);
  }
  
  public void test3_1() throws Exception {
    performTest("genericsrefact.test3_1.Test3", "a", "outside", true);
  }
  
  public void test4() throws Exception {
    performTest("genericsrefact.test4.Test4", "a", "proxy", true);
  }
  
  public void test5() throws Exception {
    performTest("genericsrefact.test5.Test5", "testMethod", "tb", true);
  }
  
  public void test5_1() throws Exception {
    performTest("genericsrefact.test5_1.Test5_1", "testMethod", "tb", false);
  }
  
  public void test6() throws Exception {
    performTest("genericsrefact.test6.Test6", "a", "it_v", true);
    performTest("genericsrefact.test6.Test6", "a", "l", true);
  }
  
  public void test6_1() throws Exception {
    performTest("genericsrefact.test6_1.Test6_1", "a", "it", true);
  }
  
  public void test6_2() throws Exception {
    performTest("genericsrefact.test6_2.Test6_2", "a", "list", true);
  }
  
  public void test6_3() throws Exception {
    performTest("genericsrefact.test6_3.Test6_3", "a", "it", true);
  }
  
  public void test8() throws Exception {
    performTest("genericsrefact.test8.Test8", "test8", "b8", true);
  }

  public void test9() throws Exception {
    performTest("genericsrefact.test9.Test9", "test9", "map", true);
  }
}
  
class BinVariableFinder extends BinItemVisitor {
  private String varName;
  private BinVariable result;

  public BinVariable findVariable(BinMethod method, String varName){
    this.varName = varName;
    result = null;
    method.accept(this);
    return result;
  }
  
  public void visit(BinLocalVariable var){
    if (var.getName().equals(this.varName)){
      result = var;
    }
  }
}
