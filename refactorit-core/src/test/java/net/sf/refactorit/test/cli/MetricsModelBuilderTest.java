/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;



import net.sf.refactorit.cli.actions.MetricsModelBuilder;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;


public class MetricsModelBuilderTest extends TestCase {
  public static final File cliFolder = AuditModelBuilderTest.cliFolder;

  public static final File nothing = new File(cliFolder, "metrics-none.profile");
  public static final File loc = new File(cliFolder, "metrics-loc.profile");

  public void testProfilesExist() {
    assertTrue(nothing.exists());
    assertTrue(loc.exists());
  }

  public void testVisibleCoumnsCount_allVisible() {
    ArrayList columns = new ArrayList(Arrays.asList(new Object[] {
        "Name", "Type"}));

    MetricsModel m = new MetricsModel(columns, new int[] {0, 1}) {
      public boolean isShowing(int column) {
        return true;
      }
    };
    m.getState().setProfile(Profile.createDefaultMetrics());

    assertEquals(2, m.getVisibleColumnsCount());
  }

  public void testVisibleCoumnsCount_allInvisible() {
    ArrayList columns = new ArrayList(Arrays.asList(new Object[] {
        "Name", "Type"}));

    MetricsModel m = new MetricsModel(columns, new int[] {0, 1}) {
      public boolean isShowing(int column) {
        return false;
      }
    };
    m.getState().setProfile(Profile.createDefaultMetrics());

    assertEquals(0, m.getVisibleColumnsCount());
  }

  public void testNothing() throws Exception {
    MetricsModelBuilder b = new MetricsModelBuilder(nothing.getAbsolutePath());
    assertEquals(0, getResultCount(b));
  }

  public void testLoc() throws Exception {
    MetricsModelBuilder b = new MetricsModelBuilder(loc.getAbsolutePath());
    assertEquals(1, getResultCount(b));
  }

  private static int getResultCount(final MetricsModelBuilder b) throws
      Exception {
    BinTreeTableModel model = b.populateModel(Utils.createSimpleProject());
    return model.getVisibleColumnsCount();
  }
}
