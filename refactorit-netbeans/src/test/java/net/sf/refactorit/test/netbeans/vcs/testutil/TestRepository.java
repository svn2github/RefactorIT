/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vcs.testutil;



import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.loader.ErrorCollector;
import net.sf.refactorit.netbeans.common.testmodule.NBTestRunnerModule;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.netbeans.vcs.CrLfTest;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalSource;

import junit.framework.Assert;


public class TestRepository {
  private static final String MODULE = "for-refactorit-tests";
  private static NbCvsCheckOut nb;

  public static Project nbProject;
  public static Source localCheckoutDir;

  public static void setUp() throws Exception {
    nb = new NbCvsCheckOut();

    nb.commit(); // we need to commit all modificatios so we can do fresh update on files
    setUpRepositoryContents(Utils.createTestRbProject("NbCvs"));
    nb.update();

    nbProject = IDEController.getInstance().getActiveProject();
    nbProject.clean(); // Can be removed later; It's a hack for the current reloading bug
    IDEController.getInstance().ensureProject(new LoadingProperties(false)); // Makes getCompilationUnitForName() work with newly added files

    Assert.assertFalse("Parse errors: "
        + CollectionUtil.toList((nbProject.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()),
        (nbProject.getProjectLoader().getErrorCollector()).hasErrors());
  }

  private static void setUpRepositoryContents(Project project) throws Exception {
    project.getProjectLoader().build(null, false);

    CvsCheckOut checkout = cleanRepository();
    checkout.commit();

    importAll(project, checkout);
    CrLfTest.setUp(checkout);
    checkout.commit();
  }

  private static void importAll(Project contents,
      CvsCheckOut checkout) throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    File toCopyFrom = contents.getPaths().getSourcePath().getRootSources()[0].
        getFileOrNull();

    FileUtil.fixCrLf(toCopyFrom);
    FileUtil.copy(new LocalSource(toCopyFrom), new LocalSource(checkout.dir));
    checkout.addDirContents(checkout.dir);
  }

  private static CvsCheckOut cleanRepository() throws IOException,
      InterruptedException, NBTestRunnerModule.CancelledException {
    CvsCheckOut checkout = new CvsCheckOut(MODULE);
    checkout.removeAll();

    return checkout;
  }

  public static void commitChanges() throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    nb.commit();
    localCheckoutDir = new LocalSource(new CvsCheckOut(MODULE).dir);
  }
}
