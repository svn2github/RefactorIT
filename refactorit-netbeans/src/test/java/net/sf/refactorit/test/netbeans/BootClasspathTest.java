/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.loader.ErrorCollector;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;
import net.sf.refactorit.test.netbeans.vfs.TestFileCreator;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.vfs.local.LocalClassPath;

import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class BootClasspathTest extends NbTestCase {
  public void myTearDown() {
    VersionSpecific.getInstance().setFakeBootClasspath(null);
  }

  public void testBootClasspathUsedForParsing() {
    appendToBootClasspath(new File(Utils.getSomeJarFile().getAbsolutePath()));

    TestFileCreator.createFile(
        "X.java",
        "public class X extends LibraryClass {}");

    Project activeProject = IDEController.getInstance().getActiveProject();
    activeProject.clean();
    IDEController.getInstance().ensureProject(new LoadingProperties(false));

    assertFalse(CollectionUtil.toList((activeProject.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()).toString(),
        (activeProject.getProjectLoader().getErrorCollector()).hasErrors());
  }

  public void appendToBootClasspath(File f) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    List bootClasspath = VersionSpecific.getInstance().getBootClasspath(ideProject);
    bootClasspath.add(new PathItemReference(f));
    VersionSpecific.getInstance().setFakeBootClasspath(bootClasspath);
  }
}

