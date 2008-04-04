/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.vfs.JavadocPath;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class NBJavadocPath extends JavadocPath {
  private String[] paths;
  private WeakReference ideProjectRef;
  
  public NBJavadocPath(Object ideProject) {
    this.ideProjectRef = new WeakReference(ideProject);
  }

  public String[] getElements() {
    if (this.paths == null) {
      // FIXME: Treats all paths as strings -- CVS filesystems do not work, for example
      // Works for local filesystems only!
      PathItemReference[] javadocPaths = getJavadocPath();
      this.paths = new String[javadocPaths.length];

      for (int i = 0; i < javadocPaths.length; i++) {
        this.paths[i] = javadocPaths[i].getFreeform();
      }
    }

    return this.paths;
  }

  private PathItemReference[] getJavadocPath() {
    final Object ideProject = ideProjectRef.get();
    if(ideProject == null) {
      return new PathItemReference[]{};
    }
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
    .getIdeProjectIdentifier(ideProject);
    NBProjectOptions options = NBProjectOptions.getInstance(projectKey);
    if (options.getAutodetectPaths()) {
      return getAutodetectedJavadocPath(ideProject);
    } else {
      return options.getUserSpecifiedJavadocPath();
    }
  }

  public static PathItemReference[] getAutodetectedJavadocPath(Object ideProject) {
    List list = new ArrayList(10);
    list.add(new PathItemReference("http://java.sun.com/j2se/1.5.0/docs/api"));
    final Enumeration e = PathUtil.getInstance().getFileSystems();
    while (e.hasMoreElements()) {
      FileSystem fs = (FileSystem) e.nextElement();
      if ((fs instanceof LocalFileSystem)
          && (fs.getCapability().capableOf(FileSystemCapability.DOC))) {
        list.add(new PathItemReference(
            ((LocalFileSystem) fs).getRootDirectory().getAbsolutePath()));
      }

      if ((fs instanceof JarFileSystem)
          && (fs.getCapability().capableOf(FileSystemCapability.DOC))) {
        list.add(new PathItemReference(((JarFileSystem) fs).getJarFile().getAbsolutePath()));
      }
    }

    return (PathItemReference[]) list.toArray(new PathItemReference[list.size()]);
  }

}
