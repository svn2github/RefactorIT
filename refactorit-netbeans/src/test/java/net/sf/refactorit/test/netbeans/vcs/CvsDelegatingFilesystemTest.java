/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vcs;


import net.sf.refactorit.netbeans.common.vcs.FileSystemProperties;
import net.sf.refactorit.vfs.Source;

import org.netbeans.modules.vcscore.VcsFileSystem;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

import java.beans.PropertyVetoException;
import java.io.IOException;

import junit.framework.Assert;

/**
 *
 * @author  risto
 */
public class CvsDelegatingFilesystemTest extends Assert {
  private Source source;

  public CvsDelegatingFilesystemTest(Source source) {
    this.source = source;
  }

  public void testDelegatingFilesystemIsVcsFilesystem() throws Exception {
    assertTrue(FileSystemProperties.isVcsFileSystem(new DelegateToVcsFilesystem(source)));
    assertFalse(FileSystemProperties.isVcsFileSystem(new SelfDelegatingFilesystem(source)));
    assertTrue(FileSystemProperties.isVcsFileSystem(new DelegateToVcsFilesystemAndSelf(source)));
    assertTrue(FileSystemProperties.isVcsFileSystem(new DelegateToDelegateVcsFilesystem(source, // "recursive" delegation
        new DelegateToVcsFilesystem(source))));
  }

  public static class DelegateToVcsFilesystem extends LocalFileSystem {
    public DelegateToVcsFilesystem(Source root) throws PropertyVetoException, IOException {
      setRootDirectory(root.getFileOrNull());

      Repository.getDefault().addFileSystem(this);
    }

    public boolean correspondsTo(FileSystem other) {
      return other instanceof VcsFileSystem;
    }
  }

  public static class SelfDelegatingFilesystem extends LocalFileSystem {
    public SelfDelegatingFilesystem(Source root) throws PropertyVetoException, IOException {
      setRootDirectory(root.getFileOrNull());

      Repository.getDefault().addFileSystem(this);
    }

    public boolean correspondsTo(FileSystem other) {
      return other == this;
    }
  }

  public static class DelegateToVcsFilesystemAndSelf extends LocalFileSystem {
    public DelegateToVcsFilesystemAndSelf(Source root) throws PropertyVetoException, IOException  {
      setRootDirectory(root.getFileOrNull());

      Repository.getDefault().addFileSystem(this);
    }

    public boolean correspondsTo(FileSystem other) {
      return (other instanceof VcsFileSystem) || (other == this);
    }
  }

  public static class DelegateToDelegateVcsFilesystem extends LocalFileSystem {
    private DelegateToVcsFilesystem delegate;

    public DelegateToDelegateVcsFilesystem(Source root, DelegateToVcsFilesystem delegate) throws PropertyVetoException, IOException  {
      setRootDirectory(root.getFileOrNull());

      Repository.getDefault().addFileSystem(this);

      this.delegate = delegate;
    }

    public boolean correspondsTo(FileSystem other) {
      return other == delegate;
    }
  }
}
