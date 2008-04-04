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
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.cli.actions.Runner;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;



public class MetricsSupportTest extends ConsoleTestCase {
//  private MetricsModel m;
//
//  // Learning tests
//  public void testGettingHtmlOutputFromMetrcs() throws Exception {
//    initModel();
//
//    StringWriter w = new StringWriter();
//    new PrintWriter(w).print(TableLayout.getClipboardText(new HtmlTableFormat(),
//        m, "title"));
//
//    assertTrue(w.getBuffer().toString(),
//        w.getBuffer().toString().startsWith("<html>"));
//  }
//
//  public void testGettingClipboardOutputFromMetrics() throws Exception {
//    initModel();
//
//    assertTrue(m.getClipboardText(new PlainTextTableFormat()),
//        m.getClipboardText(new PlainTextTableFormat()).startsWith("\"Type\""));
//  }
//
//  private MetricsModel initModel() throws Exception {
//    Project p = createProject();
//
//    m = new MetricsModel(
//        MetricsAction.getDefaultColumnNames(),
//        MetricsAction.getDefaultActionIndexes());
//    m.getState().setProfile(Profile.createDefaultMetrics());
//
//    m.populate(p, p);
//    return m;
//  }

  private Project createProject() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "class X {}", "X.java", null);
    return p;
  }

  // Real tests

  public void testCliActions() throws Exception {
    Project p = createProject();

    Arguments cl = new StringArrayArguments("-metrics");
    new Runner().runAction(p, cl);

    assertTrue(getOut(), getOut().trim().startsWith("Location"));
  }
}
