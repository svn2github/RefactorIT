/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v3;


import javax.swing.JMenuItem;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.ProjectId;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.ProjectsManager;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.utils.ClasspathUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.JavaCompilerType;
import org.netbeans.modules.java.settings.JavaSettings;
import org.openide.TopManager;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.options.SystemOption;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author risto
 */
public class Nb3VersionSpecific extends VersionSpecific {
  private List fakeBootClasspath;

  public FileObject getProjectFolder() {
    FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
    return fileSystem.findResource("Workplace");
  }

  // For PathUtil...

  public FileObject[] getIdeSourcepath(Object ideProject) {
    FileObject defaultRoot = Repository.getDefault().getDefaultFileSystem().getRoot();
    return ClassPath.getClassPath(defaultRoot, ClassPath.COMPILE).getRoots();
  }

  public List getClasspath(Object ideProject) {
    List list = new ArrayList();

    JavaSettings settings = (JavaSettings) SystemOption.findObject(
        JavaSettings.class, true);
    if (settings.getCompiler() instanceof JavaCompilerType) {
      JavaCompilerType jtype = (JavaCompilerType) settings.getCompiler();

      NbClassPath cp = jtype.getBootClassPath();
      // FIXME: These things are Strings that are added to the list on lines
      // (1) and (2);
      // but the list is later coverted to an array of FileObjectReferences.

      if (cp != null) {
        append(list, cp.getClassPath()); // (1)
      }

      cp = jtype.getClassPath();
      if (cp != null) {
        append(list, cp.getClassPath()); // (2)
      }
    }

    List classpath = CollectionUtil.toList(Repository.getDefault().getFileSystems());

    for (Iterator i = classpath.iterator(); i.hasNext();) {
      FileSystem fs = (FileSystem) i.next();

      if (canBeUsedForClasspath(fs)) {
        PathItemReference ref = new PathItemReference(fs.getRoot());
        if (!list.contains(ref)) {
          list.add(ref);
        }
      }
    }

    return list;
  }

  private void append(List list, String classpath) {
    if (classpath.startsWith("\"")) {
      classpath = classpath.substring(1, classpath.length() - 2);
    }

    StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    while (st.hasMoreTokens()) {
      File file = new File(st.nextToken());
      if (!file.exists()) {
        continue;
      }
      PathItemReference ref = new PathItemReference(file);
      if (!list.contains(ref)) {
        list.add(ref);
      }
    }
  }

  private boolean hiddenFilesAllowedForClasspath() {
    return true;
  }

  private boolean canBeUsedForClasspath(FileSystem fs) {
    if (fs.isHidden() && (!hiddenFilesAllowedForClasspath())) {
      return false;
    }

    return fs.isValid() && fs.getCapability().capableOf(FileSystemCapability.COMPILE) && (!fs.isDefault());
    //return fs.isValid() && fs.getCapability().capableOf(ClassPath.COMPILE) &&
    // (!fs.isDefault());
  }

  public synchronized Object getAllPropertiesCloned() {
    return ProjectsManager.getCurrentProject().getAllPropertiesCloned();
  }

  public synchronized void setAllPropertiesFrom(Object newProperties) {
    ProjectsManager.getCurrentProject().setAllPropertiesFrom((Properties)newProperties);
  }

  public synchronized void setAttr(String key, String value) {
    ProjectsManager.getCurrentProject().setProperty(key, value);
  }

  public synchronized String getAttr(String key) {
    return ProjectsManager.getCurrentProject().getProperty(key);
  }

  public DataFolder getCurrentProjectFolder() {
    DataFolder filesFolder = ProjectsManager.getFilesFolder();
    if(filesFolder == null) {
      return null;
    }

    final DataFolder folder = filesFolder.getFolder();
    if (folder == null) {
      AppRegistry.getLogger(Nb3VersionSpecific.class).debug("Failed to get current project folder");
    }
    return folder;
  }

  // NBController

  public Object getCurrentProject() {
    return TopManager.getDefault().getPlaces().nodes().projectDesktop();

    // What follows here is a non-deprecated way of getting similiar results;
    // however, that causes isProjectChangedInIDE() to fail, so we're not using it
    // at the moment. Maybe there is a way to make it work?

    /*Object o = null;

    FileObject fileObject = getProjectFolder();
    if(fileObject != null) {
      DataFolder dataFolder = DataFolder.findFolder(fileObject);
      o = dataFolder.getNodeDelegate();
    }

    return o;*/
  }

  public ProjectId getProjectId() {
    return new ProjectId3(getCurrentProject());
  }

  private static class ProjectId3 implements ProjectId {
    private String name;
    private Node   node;

    ProjectId3(Object nodeObject) {
      this.node = (Node) nodeObject;
      this.name = (node == null) ? "" : node.getName();
    }

    public boolean equals(Object other) {
      return equals((ProjectId)other);
    }

    public boolean equals(ProjectId other) {
      if (!(other instanceof ProjectId3)) {
        return false;
      }

      ProjectId3 otherNodeStore = (ProjectId3) other;

      boolean result = otherNodeStore.node == node &&
          otherNodeStore.name.equals(name);
      return result;
    }

    public String toString() {
      return node + ", " + name;
    }
  }

  public String getLongDisplayName(FileObject fileObject) {
    try {
      File file = FileObjectUtil.getFileOrNull(fileObject);
      if(file == null) {
        return fileObject.getPath();
      } else {
        return file.getAbsolutePath();
      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }

  }

  public List getBootClasspath(Object ideProject) {
    //ideProject;
    if(fakeBootClasspath != null) {
      return fakeBootClasspath;
    }

    List result = new ArrayList(10);

    StringTokenizer classpathElements = new StringTokenizer(ClasspathUtil.
        getDefaultClasspath(), File.pathSeparator);
    while (classpathElements.hasMoreTokens()) {
      String classPath = classpathElements.nextToken();

      File runtimeJar = new File(classPath);
      result.add(new PathItemReference(runtimeJar));
    }

    return result;
  }

  public String getProjectNameFor(Object nbIdeProject) {
    return nbIdeProject.toString();
  }

  public void setFakeBootClasspath(List c) {
    this.fakeBootClasspath = c;
  }

  public PathItemReference getPathItemReference(File file) {
    return new PathItemReference(file);
  }

  public FileObject getArchiveRoot(FileObject object) {
    // is different in NB4
    return object;
  }

  public Object[] getRequiredProjectKeys(Object ideProject) {
    return new Object[] {};
  }

  public Object getUniqueProjectIdentifier(Object ideProject) {
    return ideProject.toString();
  }
  
  public JMenuItem createMenuItem() {
    return new DynamicMenu3();
  }
  
  public boolean isVcsEnabled() {
    return "true".equals(GlobalOptions.getOption("version.control.enabled", "true"));
  }
}
