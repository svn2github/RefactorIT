/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.notused.NotUsedIndexer;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;



/**
 * @author Risto
 */
public class NotUsedSupportTest extends ConsoleTestCase {
//  Project p;
//  NotUsedIndexer i;
//  String expectedReport;

//  public void setUp() throws Exception {
//    super.setUp();
//
//    p = Utils.createTestRbProjectFromArray(new String[] {
//        "class Unused { Used usage; }",
//        "class Used { int unusedField; void unusedMethod(); }",
//        ""
//    });
//
//    i = new NotUsedIndexer();
//    p.accept(i);
//
//    createExpectedReport();
//  }

//  private void createExpectedReport() {
//    String newline = "\n";
//    String tab = "\t";
//    String twoEmptyColumns = tab + "  " + tab;
//
//    expectedReport =
//        "\"Type\"" + tab + "\"Not Used\"" + tab + "\"Line\"" + tab
//        + "\"Source\"" + tab + "\"Package\"" + tab + "\"Class\"" + newline +
//        tab + "Overall Not Used (3)" + twoEmptyColumns + tab + tab + newline +
//        tab + "Whole Types (1)" + twoEmptyColumns + tab + tab + newline +
//        "Package" + tab + "<default package>" + twoEmptyColumns + tab
//        + "<default package>" + tab + newline +
//        "Class" + tab + "Unused" + tab + "1  " + tab
//        + "class Unused { Used usage; }" + tab + "<default package>" + tab
//        + "Unused" + newline +
//        "Field" + tab + "usage" + tab + "1  " + tab
//        + "class Unused { Used usage; }" + tab + "<default package>" + tab
//        + "Unused" + newline +
//        tab + "Single Members (2)" + twoEmptyColumns + tab + tab + newline +
//        "Package" + tab + "<default package>" + twoEmptyColumns + tab
//        + "<default package>" + tab + newline +
//        "Class" + tab + "Used (2)" + twoEmptyColumns + tab
//        + "<default package>" + tab + "Used" + newline +
//        "Field" + tab + "unusedField" + tab + "2  " + tab
//        + "class Used { int unusedField; void unusedMethod(); }" + tab
//        + "<default package>" + tab + "Used" + newline +
//        "Method" + tab + "unusedMethod()" + tab + "2  " + tab
//        + "class Used { int unusedField; void unusedMethod(); }" + tab
//        + "<default package>" + tab + "Used" + newline;
//  }

  // Learning tests

  public void testInvokingNotUsed() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "class X { int unused; void unusedMethod(){} }", "X.java", null);
    NotUsedIndexer i = new NotUsedIndexer();
    p.accept(i);

    assertEquals(1, i.getNotUsedTypes().size());
    assertEquals(0, i.getNotUsedFields().size());
    assertEquals(0, i.getNotUsedMethods().size());
  }

//  public void testReportGeneration() throws Exception {
//    NotUsedTreeTableModel m = new NotUsedTreeTableModel(p, p,
//        ExcludeFilterRule.getDefaultRules());
//
//    assertEquals(expectedReport,
//        LinePositionUtil.useUnixNewlines(m.getClipboardText(new
//        PlainTextTableFormat())));
//  }
//
//  // Production code tests
//
//  public void testReportGenerationMethod() throws Exception {
//    Arguments args = new StringArrayArguments("-notused -format text");
//    new Runner().runAction(p, args);
//
//    assertEquals(expectedReport,
//        LinePositionUtil.useUnixNewlines(getOut()));
//  }
}
