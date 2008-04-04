/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli.actions;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.cli.actions.Runner;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;



public class RunnerTest extends ConsoleTestCase {
  public void testHtmlFormatWithMetrics() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "class X {}", "X.java", null);
    Arguments args = new StringArrayArguments("-metrics -format html");
    new Runner().runAction(p, args);
    assertTrue(getOut(), getOut().trim().startsWith("<html>"));
  }
}
