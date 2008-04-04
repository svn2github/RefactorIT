/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.ui.audit.numericliterals;
///*
// * NumLitTreeTableModelTest.java
// *
// * Created on October 4, 2004, 3:07 PM
// */
//
//package net.sf.refactorit.test.ui.audit.numericliterals;
//
//import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixCreateField;
//
//import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixUseField;
//import net.sf.refactorit.classmodel.BinCIType;
//import net.sf.refactorit.classmodel.BinField;
//import net.sf.refactorit.classmodel.CompilationUnit;
//
//import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixConstantalize;
//
//import net.sf.refactorit.audit.Audit;
//import net.sf.refactorit.audit.AuditRule;
//import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteralField;
//import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteralsRule;
//import net.sf.refactorit.classmodel.BinTypeRef;
//import net.sf.refactorit.classmodel.Project;
//import net.sf.refactorit.ui.audit.numericliterals.NumLitTreeTableModel;
//import net.sf.refactorit.ui.audit.numericliterals.NumLitTreeTableNode;
//
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
//
//import net.sf.refactorit.test.Utils;
//
///**
// *
// * @author Arseni Grigorjev
// */
//public class NumLitTreeTableModelTest extends TestCase{
//
//  public NumLitTreeTableModelTest(String name) {
//    super(name);
//  }
//
//  public static Test suite() {
//    return new TestSuite(NumLitTreeTableModelTest.class);
//  }
//
//  public void testChangesPreview(){
//    Project project = null;
//    try{
//      project = Utils.createTestRbProjectFromString(getTestSource());
//    } catch (Exception e){
//      fail("Was not able to create test project");
//    }
//
//    AuditRule rule =(new Audit(NumericLiteralsRule.class)).createAuditingRule();
//    rule.clearViolations();
//    rule.visit(project);
//
//    NumericLiteralField violation
//        = (NumericLiteralField) rule.getViolations().get(0);
//    violation.setConstantalizable(true);
//
//    NumLitTreeTableNode node = new NumLitTreeTableNode(violation);
//
//    // no changes
//    assertEquals(getNoChangesPreview(), NumLitTreeTableModel.getPreview(node));
//
//    // solution: make field 'final static'
//    violation.setFix(new NumLitFixConstantalize(null, violation.getField()));
//    assertEquals(getMakeFieldConstantPreview(),
//        NumLitTreeTableModel.getPreview(node));
//
//    // solution: use existing field
//    BinField fieldToUse = ((BinTypeRef) ((CompilationUnit) project
//        .getCompilationUnits().get(0)).getDefinedTypes().get(0)).getBinCIType()
//        .getDeclaredField("const");
//    violation.setSolution(new UseExistingFieldSolution(fieldToUse, true));
//    assertEquals(getUseExistingPreview(),
//        NumLitTreeTableModel.getPreview(node));
//
//    // solution: create new field
//    BinCIType where = ((BinTypeRef) ((CompilationUnit) project
//        .getCompilationUnits().get(0)).getDefinedTypes().get(0)).getBinCIType();
//    violation.setSolution(new CreateNewFieldSolution(null, "new_const", where, "public", true, "5"));
//    assertEquals(getCreateNewPreview(),
//        NumLitTreeTableModel.getPreview(node));
//
//    // solution: use existing field and make field constant
//    violation.setSolution(new UseExistingFieldSolution(fieldToUse, true));
//    violation.createAdditionalSolution();
//    assertEquals(getComplexPreview(),
//        NumLitTreeTableModel.getPreview(node));
//
//  }
//
//  private static final String getTestSource(){
//    return "public class A{ \npublic static final int const = 5;"
//        + "\npublic int a_field = 5;\n }\n\n";
//  }
//
//  private static final String getNoChangesPreview(){
//    return "<html>&nbsp;&nbsp;<FONT style='font-family: Dialog; font-size: 12pt;'>public int a_field = <FONT color='#C02040'>5"
//        + "</FONT>;</FONT></html>";
//  }
//
//  private static final String getMakeFieldConstantPreview(){
//    return "<html>&nbsp;&nbsp;<FONT style='font-family: Dialog; font-size: 12pt;'>public int a_field = <FONT color='#C02040'>5</FONT>;</FONT> </html>";
//  }
//
//  private static final String getUseExistingPreview(){
//    return "<html>&nbsp;&nbsp;<FONT style='font-family: Dialog; font-size: 12pt;'>public int a_field = <FONT color='#C02040'>"
//        + "this.const</FONT>;</FONT> </html>";
//  }
//
//  private static final String getCreateNewPreview(){
//    return "<html>&nbsp;&nbsp;<FONT style='font-family: Dialog; font-size: 12pt;'>public int a_field = <FONT color='#C02040'>"
//        + "this.new_const</FONT>;</FONT> </html>";
//  }
//
//  private static final String getComplexPreview(){
//    return "<html>&nbsp;&nbsp;<FONT style='font-family: Dialog; font-size: 12pt;'>public static final int a_field = <FONT color="
//        + "'#C02040'>this.const</FONT>;</FONT> </html>";
//  }
//}
