/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;



import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.cli.actions.AuditModelBuilder;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.io.File;

import junit.framework.TestCase;


public class AuditModelBuilderTest extends TestCase {
  public static final File cliFolder = new File(
      Utils.getTestProjectsDirectory(), "CLI");

  public static final File nothing = new File(cliFolder, "audit-none.profile");
  public static final File staticizable = new File(cliFolder,
      "audit-staticizable.profile");

  public void testProfilesExist() {
    assertTrue(nothing.exists());
    assertTrue(staticizable.exists());
  }

  public void testNoResultsWithNothingProfile() throws Exception {
    AuditModelBuilder b = new AuditModelBuilder(nothing.getAbsolutePath());
    assertEquals(0, getResultCount(b));
  }

  public void testAResultWithStaticizableProfile() throws Exception {
    AuditModelBuilder b = new AuditModelBuilder(staticizable.getAbsolutePath());
    assertEquals(1, getResultCount(b));
  }

  public void testProfileParameter() throws Exception {
    Arguments a = new StringArrayArguments("-profile " + nothing.getPath()
        + " -audit");
    assertTrue(a.getProfile(), a.getProfile().endsWith("audit-none.profile"));

    AuditModelBuilder audit = (AuditModelBuilder) a.getModelBuilder();
    assertEquals(0, getResultCount(audit));
  }

  private static int getResultCount(final AuditModelBuilder b) throws Exception {
    BinTreeTableModel model = b.populateModel(Utils.createSimpleProject());
    int childCount = model.getChildCount(model.getRoot());

    return childCount;
  }
}
