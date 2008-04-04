/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v5;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.NBController;
import net.sf.refactorit.netbeans.common.ProjectId;
import net.sf.refactorit.netbeans.common.ProjectNotFoundException;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.ProjectsManager;
import net.sf.refactorit.netbeans.common.standalone.FileBasedOptions;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author risto
 */
public class Nb5VersionSpecific extends VersionSpecific {
  private static final String NAMESPACE = "http://www.refactorit.com/ns/netbeans_50_project";
  private static final String ELEMENT = "RefactorIT";

  private List fakeBootClasspath;

  // Project propeties

  public synchronized void setAttr(String key, String value) {
    if(useAuxiliaryConfiguration()) {
      Element configNode = getConfigNode();
      configNode.setAttribute(key, value);
      getAuxiliaryConfiguration().putConfigurationFragment(configNode, false);
    } else {
      try {
        FileBasedOptions fileBasedOptions = getFileBasedOptions();
        if(fileBasedOptions == null) {
          return;
        }
        fileBasedOptions.setProperty(key, value);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public synchronized String getAttr(String key) {
    if(useAuxiliaryConfiguration()) {
      return getConfigNode().getAttribute(key);
    } else {
      final FileBasedOptions fileBasedOptions = getFileBasedOptions();
      if(fileBasedOptions == null) {
        return null;
      }
      return fileBasedOptions.getProperty(key);
    }
  }

  public synchronized Object getAllPropertiesCloned() {
    if(useAuxiliaryConfiguration()) {
      return getConfigNode().cloneNode(true);
    } else {
      final FileBasedOptions fileBasedOptions = getFileBasedOptions();
      if(fileBasedOptions == null) {
        return null;
      }
      return fileBasedOptions.getSnapshot();
    }
  }

  public synchronized void setAllPropertiesFrom(Object snapshot) {
    if(snapshot == null) {
      return;
    }
    if(useAuxiliaryConfiguration()) {
      getAuxiliaryConfiguration().putConfigurationFragment(
          (Element) ((Element)snapshot).cloneNode(true),
          false);
    } else {
      try {
        final FileBasedOptions fileBasedOptions = getFileBasedOptions();
        if(fileBasedOptions == null) {
          return;
        }
        fileBasedOptions.restoreFromSnapshot(snapshot);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Element getConfigNode() {
    Element result = getAuxiliaryConfiguration().getConfigurationFragment(
            ELEMENT, NAMESPACE, false);
    if (result == null) {
      result = createNewConfigNode();
      getAuxiliaryConfiguration().putConfigurationFragment(result, false);
    }
    return result;
  }

  private AuxiliaryConfiguration getAuxiliaryConfiguration() {
    Project project = getActiveOrOpenedProject();
    AuxiliaryConfiguration a = (AuxiliaryConfiguration) project.getLookup()
            .lookup(AuxiliaryConfiguration.class);

    return a;
  }

  private boolean useAuxiliaryConfiguration() {
    final FileBasedOptions fileBasedOptions = getFileBasedOptions();
    return getAuxiliaryConfiguration() != null && fileBasedOptions != null &&
        ( !fileBasedOptions.optionsExist());
  }

  private FileBasedOptions getFileBasedOptions() {
    try {
      Project currentProject = (Project)getCurrentProject();
      if(currentProject == null) {
        return null;
      }
      File projectFolder = FileObjectUtil.getFileOrNull(currentProject.getProjectDirectory());
      return new FileBasedOptions(projectFolder);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  // End of project properties

  /**
   * @return
   */
  private Project getActiveOrOpenedProject() throws ProjectNotFoundException {
    Project project = (Project) IDEController.getInstance()
            .getActiveProjectFromIDE();
    if (project != null) {
      return project;
    }else {
      Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
      if (openProjects.length != 0) {
        return openProjects[0];
      }
    }
    if(project == null) {
      throw new ProjectNotFoundException();
    }
    return null;
  }

  private Element createNewConfigNode() {
    try {
      Document document = DocumentBuilderFactory.newInstance()
              .newDocumentBuilder().newDocument();
      return document.createElementNS(NAMESPACE, ELEMENT);
    } catch (ParserConfigurationException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    } catch (FactoryConfigurationError e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }
  }

  public Object getCurrentProject() {
    final Object activeProjectFromIDE = NBController.getVersionState().getActiveProjectFromIDE();
//    if(activeProjectFromIDE == null) {
//      throw new ProjectNotFoundException();
//    }
    return activeProjectFromIDE;

    //    Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
    //    if (openProjects.length == 0) {
    //      return null;
    //    } else {
    //      return openProjects[0];
    //    }
  }

  public FileObject[] getIdeSourcepath(Object idePrj) {
    Project ideProject = (Project)idePrj;
    return (FileObject[]) getSourceRoots(ideProject).toArray(new FileObject[0]);
  }

  private Set getSourceRoots(Project ideProject) {
//    Project project = (Project) getCurrentProject();
    if(ideProject == null) {
    	return CollectionUtil.EMPTY_SET;
    }
    try {
	    Sources s = ProjectUtils.getSources(ideProject);

	    SourceGroup[] javaSources = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
	    //  hack! (use webProjectConstants.DOC_ROOT from web module)
	    SourceGroup[] jspDocRoot = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

	    SourceGroup[] joinedGroup = new SourceGroup[javaSources.length + jspDocRoot.length];
	    System.arraycopy(javaSources, 0, joinedGroup, 0, javaSources.length);
	    System.arraycopy(jspDocRoot, 0, joinedGroup, javaSources.length, jspDocRoot.length);

	    Set sourceRoots = new LinkedHashSet();
	    for (int j = 0; j < joinedGroup.length; j++) {
	      sourceRoots.add(joinedGroup[j].getRootFolder());
	    }
	    return sourceRoots;
    } catch (NullPointerException e) {
      AppRegistry.getExceptionLogger().error(e, "Failed to get source roots for: " + ideProject);
      return CollectionUtil.EMPTY_SET;
    }
  }

  public List getClasspath(Object ideProject) {
    return getClasspath((Project)ideProject, ClassPath.COMPILE);
  }

  public List getBootClasspath(Object ideProject) {
    if(fakeBootClasspath != null) {
      return fakeBootClasspath;
    }

    return getClasspath((Project)ideProject, ClassPath.BOOT);
  }

  public List getClasspath(Project ideProject, String classpathType) {
    List result = new ArrayList();
//    Project project = (Project) getCurrentProject();
    if(ideProject == null) {
      return result;
    }
    ClassPathProvider cp = (ClassPathProvider) (ideProject)
            .getLookup().lookup(ClassPathProvider.class);
    if (cp == null) {
      throw new SystemException("No classpath available for current project");
    }

    for (Iterator iter = getSourceRoots(ideProject).iterator(); iter.hasNext();) {
      FileObject root = (FileObject) iter.next();
      ClassPath path = cp.findClassPath(root, classpathType);
      if(path == null) {
        continue;
      }
      List list = path.entries();
      for(Iterator it = list.iterator(); it.hasNext(); ) {
        ClassPath.Entry entry = (ClassPath.Entry)it.next();
        FileObject entryRoot = entry.getRoot();
        if(entryRoot == null) {
          continue;
        }

//        FileSystem system;
//        try {
//          system = entryRoot.getFileSystem();
//        } catch (FileStateInvalidException e) {
//          AppRegistry.getExceptionLogger().error(e, this);
//          throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
//        }
//        if(entryRoot.isFolder() && !(system instanceof JarFileSystem)) {
//         List directoryJars = getJarsFromDirectory(entryRoot);
//         CollectionUtil.addAllNew(result, directoryJars);
//        }
        File file = FileUtil.toFile(entryRoot);
        if(file != null && file.isDirectory()) {
          ArrayList toFill = new ArrayList();
          addJarsFrom(file, new HashSet(), toFill);
          CollectionUtil.addAllNew(result, toFill);
        }

        if(entryRoot.isValid())
          CollectionUtil.addNew(result, new PathItemReference(entryRoot));
        }
    }

    return result;
  }

  private void addJarsFrom(final File dir, final HashSet added,
      ArrayList results) {
    added.add(dir);
    File[] allList = dir.listFiles();
    for (int i = 0; i < allList.length; ++i) {
      File cur = allList[i];
      if (cur.isDirectory()) {
        if (added.contains(cur)) {
          continue;
        } else {
          addJarsFrom(cur, added, results);
        }
      }
      String curName = cur.getName().toLowerCase();
      if (!curName.endsWith(".jar") && !curName.endsWith(".zip")) {
        continue;
      }

      PathItemReference ref = new PathItemReference(cur);
      results.add(ref);
    }
  }
//    Enumeration children = directoryRoot.getChildren(true);
//    while (children.hasMoreElements()) {
//      FileObject child = (FileObject) children.nextElement();
//      if (child.isFolder() && !child.isData()) {
//        continue;
//      }
//
//      String curName = child.getExt().toLowerCase();
//      if (!curName.endsWith("jar") && !curName.endsWith("zip")) {
//        continue;
//      }
//
//      if (child.isValid()) {
//        CollectionUtil.addNew(jars, new PathItemReference(child));
//      }
//    }
//    return jars;
//  }

  public DataFolder getCurrentProjectFolder() {
    final Project project = (Project) getCurrentProject();
    if(project == null) {
      return null;
    }
    if(useAuxiliaryConfiguration()) {
      CacheDirectoryProvider cp = (CacheDirectoryProvider) project
              .getLookup().lookup(CacheDirectoryProvider.class);
      try {
        return (DataFolder) DataObject.find(cp.getCacheDirectory());
      } catch (IOException e) {
        AppRegistry.getExceptionLogger().error(e, ProjectsManager.class);
        throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
      }
    } else {
      FileObject fo = project.getProjectDirectory();
      return DataFolder.findFolder(fo);
    }
  }

  public ProjectId getProjectId() {
    final Object currentProject = getCurrentProject();
    if(currentProject == null) {
      return null;
    }
    return new ProjectId4(currentProject);
  }

  private static class ProjectId4 implements ProjectId {
    private Object project;

    ProjectId4(Object nodeObject) {
      this.project = nodeObject;
    }

    public boolean equals(Object other) {
      return equals((ProjectId) other);
    }

    public boolean equals(ProjectId otherObject) {
      if (otherObject == null || (!(otherObject instanceof ProjectId4))) {
        return false;
      }

      ProjectId4 other = (ProjectId4) otherObject;

      if (this.project == null) {
        return other.project == null;
      } else if (other.project == null) {
        return false;
      }

      return other.project.equals(project);
    }
  }

  public String getLongDisplayName(FileObject fileObject) {
    return FileUtil.getFileDisplayName(fileObject);
  }

  public String getProjectNameFor(Object nbIdeProject) {
    return getLongDisplayName(((org.netbeans.api.project.Project) nbIdeProject)
        .getProjectDirectory());
  }

  public void setFakeBootClasspath(List c) {
    this.fakeBootClasspath = c;
  }

  public PathItemReference getPathItemReference(File file) {
    FileObject object = FileUtil.toFileObject(file);
    if(object == null) {
      return new PathItemReference(file);
    } else {
      object = getArchiveRoot(object);
      return new PathItemReference(object);
    }
  }

  public FileObject getArchiveRoot(FileObject object) {
    try {
      if (FileUtil.isArchiveFile(object)) {
        object = FileUtil.getArchiveRoot(object);
      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger()
          .warn(e, "Failed to convert to archive fileobject: " + object);
    }

    return object;
  }


  public Object[] getRequiredProjectKeys(Object idePrj) {
    assert idePrj != null;
    Project ideProject = (Project)idePrj;
    SubprojectProvider provider = (SubprojectProvider) ideProject.getLookup()
        .lookup(SubprojectProvider.class);
    Set subProjects = provider.getSubprojects();
    List projectKeys = new ArrayList(3);
    for(Iterator it = subProjects.iterator(); it.hasNext();) {
      Project subProject = (Project)it.next();
      Object projectId = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(subProject);
      projectKeys.add(projectId);
    }
    return projectKeys.toArray();
  }

  public Object getUniqueProjectIdentifier(Object idePrj) {
    Project ideProject = (Project)idePrj;
    return ideProject.getProjectDirectory().toString();
  }

  public JMenuItem createMenuItem() {
    return new DynamicMenu5();
  }
  
  public boolean isVcsEnabled() {
    return false;
  }
}
