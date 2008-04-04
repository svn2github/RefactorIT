/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.projectoptions;

import java.io.IOException;
import java.beans.PropertyVetoException;

import java.io.File;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalSource;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class PathUtilTest extends NbTestCase {

  public void testNbInstallFolder() {
    File f = new File(PathUtil.getInstance().getNbInstallFolder());
    
    assertTrue(f.exists());
    
    if(RefactorItActions.isNetBeansFour()) {
      assertEquals("netbeans-4.0", f.getName());
    } else {
      assertEquals("NetBeans3.6", f.getName());
    }
  }
  
  public void testNbConfigFile() {
    File f = new File(PathUtil.getInstance().getNbConfigFile());
    
    assertTrue(f.exists());
    
    if(RefactorItActions.isNetBeansFour()) {
      assertTrue(f.getAbsolutePath(),
          f.getAbsolutePath().endsWith(
          FileUtil.useSystemPS("/netbeans-4.0/etc/netbeans.conf")));
    } else {
      assertTrue(f.getAbsolutePath(),
          f.getAbsolutePath().endsWith(
          FileUtil.useSystemPS("/NetBeans3.6/bin/ide.cfg")));
    }
  }
  
  public void testSourceOnSourcepath_ignored() throws Exception {
    NBSource source = createSampleSource();
    assertTrue(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
    
    setIgnoredOnSourcepath((NBSource) source.getParent());
    assertFalse(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
  }
  
  public void testSourceOnSourcepath_notOnPath() throws Exception {
    NBSource source = createSampleSource();
    setSourcepathRoot((NBSource) getRoot().mkdir("some_other_sourcepath_root"));
    
    assertFalse(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
  }
  
  public void testSourceOnSourcepath_ignored_localSource() throws Exception {
    NBSource source = createSampleSource();
    setSourcepathRoot(source.getParent().getParent().getFileOrNull());
    assertTrue(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
    
    setIgnoredOnSourcepath(source.getParent().getFileOrNull());
    assertFalse(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
  }
  
  public void testSourceOnSourcepath_notOnPath_localSource() throws Exception {
    NBSource source = createSampleSource();
    setSourcepathRoot(getRoot().mkdir("some_other_sourcepath_root").getFileOrNull());
    
    assertFalse(PathUtil.getInstance().sourceOnSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(),
        source.getFileObject()));
  }
}
