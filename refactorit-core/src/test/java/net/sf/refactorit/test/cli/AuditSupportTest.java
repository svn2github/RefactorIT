/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import junit.framework.TestCase;


public class AuditSupportTest extends TestCase {
//  private String oldDecimalSeparator;
//  private String oldGroupingSeparator;
//
//  private boolean hasBeenSetUp = false;
//
//  public void setUp() {
//    setUpFormatting();
//  }
//
//  public void tearDown() {
//    tearDownFormatting();
//  }
//
//  public void setUpFormatting() {
//    oldDecimalSeparator = GlobalOptions.getOption("separator.decimal");
//    oldGroupingSeparator = GlobalOptions.getOption("separator.grouping");
//
//    GlobalOptions.setOption("separator.decimal", ",");
//    GlobalOptions.setOption("separator.grouping", "");
//
//    hasBeenSetUp = true;
//  }
//
//  public void tearDownFormatting() {
//    if (!hasBeenSetUp) {
//      throw new RuntimeException(
//          "setUpXXX() was NOT called before tearDownXXX()");
//    }
//
//    GlobalOptions.setOption("separator.decimal", oldDecimalSeparator);
//    GlobalOptions.setOption("separator.grouping", oldGroupingSeparator);
//  }
//
//  // Learning test
//  public void testSimpleAuditInvocation() throws Exception {
//    Project p = Utils.createSimpleProject();
//
//    Profile profile = Profile.createDefaultAudit();
//    
//    AuditTreeTableModel mdl = new AuditTreeTableModel();
//    AuditRule[] rules = Audit.createActiveRulesAndSetToModel(profile, mdl);
//    AuditRunner runner = new AuditRunner(mdl);
//    
//    AuditTreeTableModel model = runner.findViolationsToModel(new HashSet(p
//        .getCompilationUnits()), rules);
//    
//    final String expected = createSimpleOutput();
//    final String got = model.getClipboardText(new PlainTextTableFormat());
//    
//    Utils.assertEqualsIgnoreWhitespace("Got: " + got, expected, got);
//  }
//
//  public static String createSimpleOutput(){
//      return "\"Type\"\t\"Location\"\t\"Line\"\t\"Source\"\t\"Priority\"\t"
//        + "\"Density\"\t\"Type\"\t\"Package\"\t\"Class\"\n\t<html>Audit results "
//        + "<b>(right-click on item for corrective actions)</b></html>\t"
//        + "  \t\tLow\t20,0\t\t\t\nPackage\t<default package>\t  \t\tLow\t20"
//        + ",0\t\t<default package>\t\nClass\tX\t1  \tclass X { void m(){ret"
//        + "urn;} }\tLow\t20,0\t\t<default package>\tX\n\tm() does not use e"
//        + "nclosing state - should be static\t1  \tclass X { void m(){retur"
//        + "n;} }\tLow\t20,0\tmStatic\t\t\n\tClass X should be \'final\': ne"
//        + "ither it nor its methods are overriden.\t1  \tclass X { void m()"
//        + "{return;} }\tLow\t20,0\tmFinal\t\t\n\tm() is never overriden - s"
//        + "hould be \'final\'\t1  \tclass X { void m(){return;} }\tLow\t20,"
//        + "0\tmFinal\t\t\n\tMethod body has less than 2 statements: 1\t1  "
//        + "\tclass X { void m(){return;} }\tLow\t20,0\tMBodyLen\t\t\n";
//  }
//
//  public static String createSimpleOutputCommaSeparated() {
//    return "\"Type\",\"Location\",\"Line\",\"Source\",\"Priority\",\"Density"
//        + "\",\"Type\",\"Package\",\"Class\"\n,<html>Audit results <b>(right-click"
//        + " on item for corrective actions)</b></html>,  ,,Low,\"20,0\","
//        + ",,\nPackage,<default package>,  ,,Low,\"20,0\",,<default package>,"
//        + "\nClass,X,1  ,class X { void m(){return;} },Low,\"20,0\",,<default "
//        + "package>,X\n,m() does not use enclosing state - should be static,1 "
//        + " ,class X { void m(){return;} },Low,\"20,0\",mStatic,,\n,Class X sh"
//        + "ould be \'final\': neither it nor its methods are overriden.,1  ,cl"
//        + "ass X { void m(){return;} },Low,\"20,0\",mFinal,,\n,m() is never ov"
//        + "erriden - should be \'final\',1  ,class X { void m(){return;} },Low"
//        + ",\"20,0\",mFinal,,\n,Method body has less than 2 statements: 1,1  ,"
//        + "class X { void m(){return;} },Low,\"20,0\",MBodyLen,,\n";
//  }
//
//  public static String createNoOutput() {
//    return "'Type' 'Location' 'Line' 'Source' 'Priority' 'Density' 'Type' 'Package' 'Class'\n" +
//        " <html>Audit results <b>(right-click on item for corrective actions)</b></html> Low \n";
//  }
}
