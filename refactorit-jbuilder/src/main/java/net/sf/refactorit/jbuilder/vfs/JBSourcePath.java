/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.vfs;

import com.borland.jbuilder.node.JBProject;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.vfs.Url;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.jbuilder.RefactorItPropGroup;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePathFilter;
import net.sf.refactorit.vfs.Source.SourceFilter;


/**
 * Implementation of SourcePath VFS for JBuilder.
 *
 * @author Igor Malinin
 */
public class JBSourcePath extends AbstractSourcePath {
  protected JBProject project;
  //private Project riProject;
  private SourcePathFilter sourcepathFilter = new SourcePathFilter();

  /**
   * carefully used in <code>getActiveInstance</code>
   * method which uses it only if underlying project is still the same
   */
  private static JBSourcePath activeInstance; // added by juri

  public JBSourcePath(JBProject p) {
    project = p;
    //IDEController.getInstance().getActiveProject();
    //riProject = IDEController.getInstance().getWorkspace().getProject(p);

    //
    activeInstance = this;
  }

  /**
   * returns <code>JBSourcePath</code> instance that is associated with current
   * <code>JBProject</code> that is provided by
   * <code>JBController.getInstance().getActiveProjectFromIDE()</code>
   */
  public static JBSourcePath getActiveJBSourcePath() {
    JBProject project = (JBProject) IDEController.getInstance().
        getActiveProjectFromIDE();
    if (activeInstance != null && activeInstance.project.equals(project)) {
      return activeInstance;
    } else {
      return new JBSourcePath(project);
    }
  }

  public List getAllSources() {
    Browser.getActiveBrowser().getActiveProject().check();
    Browser.getActiveBrowser().getActiveProject().refresh();

    return getAllSources(new SourceFilter() {
      public boolean accept(Source source) {
        return isValidSource(source.getName());
      }
    });
  }

  public List getAllSources(SourceFilter acceptor) {
    sourcepathFilter.initialize();
    ArrayList result = new ArrayList(200);
    Source[] roots = getRootSources();
    for (int i = 0; i < roots.length; ++i) {
      if (isIgnoredPath(roots[i].getAbsolutePath())) {
        continue;
      }
      iterateDirectory(roots[i], acceptor, result);
    }
    return result;
  }

  public List getNonJavaSources(final WildcardPattern[] patterns) {
    return getAllSources(new SourceFilter() {
      public boolean accept(Source source) {
        return fileAcceptedByPattern(source.getName(), patterns);
      }
    });
  }

  public Source[] getAutodetectedElements() {
    return getAutodetectedRootSources();
  }

  private void iterateDirectory(Source parent, SourceFilter acceptor,
      Collection result) {
    //if (riProject == null) {
      // this happens when RIT compiled with JDK 1.4,
      // but an old JBuilder runs with 1.3
    //  return;
    //}
    //check

    //riProject.startProfilingTimer("iterating a directory");
    //riProject.startProfilingTimer("getting list of children for a directory");
    Source[] list = parent.getChildren();
    //riProject.stopProfilingTimer();
    // fix for bug #75 from public bugzilla
    if (list == null) {
      return;
    }
    for (int i = 0; i < list.length; ++i) {
      Source curSource = list[i];
      if (isIgnoredPath(curSource.getAbsolutePath())) {
        continue;
      }
      if (curSource.isFile()) {
        if (acceptor.accept(curSource)) {
          result.add(curSource);
        }
      } else {
        if (sourcepathFilter.acceptDirectoryByName(curSource.getName().toLowerCase())) {
          iterateDirectory(curSource, acceptor, result);
        }
      }
    }
    //riProject.stopProfilingTimer();
  }

  public Source[] getRootSources() {
    if (RefactorItPropGroup.getProjectPropertyBoolean(
        RefactorItPropGroup.AUTODETECT_PATHS, true)) {
      return getAutodetectedRootSources();
    } else {
      return getUserSpecifiedRootSources();
    }
  }

  public List getIgnoredSources() {
    return RefactorItPropGroup.getIgnoredSourcePath();
  }

  public Source[] getUserSpecifiedRootSources() {
    String specifiedSourcepath = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_SOURCEPATH, "");
    if (isOldStylePath(specifiedSourcepath)) {
      return Source.NO_SOURCES;
    }
    Set specifiedSourcepathSet = new HashSet(pathStrAsList(specifiedSourcepath));
    Set result = new HashSet(specifiedSourcepathSet.size());
    Iterator i = specifiedSourcepathSet.iterator();
    while (i.hasNext()) {
      String pathItem = (String) i.next();
      if (!isIgnoredPath(pathItem)) {
        result.add(JBSource.getSource(new File(pathItem)));
      }
    }
    return (Source[]) result.toArray(Source.NO_SOURCES);
  }

  public Source[] getAutodetectedRootSources() {
    Url[] srcs = project.getPaths().getSourcePath();
    int length = srcs.length;
    Set sourceSet = new HashSet(length);
    for (int i = 0; i < length; i++) {
      String filePath = srcs[i].getFileObject().getAbsolutePath();
      if (!isIgnoredPath(filePath)) {
        sourceSet.add(JBSource.getSource(srcs[i]));
      }
    }
    Source[] sources = (Source[]) sourceSet
        .toArray(new Source[sourceSet.size()]);
    return sources;
  }


  /**
   * checks if the path identifier from the parameter is contained in cached set
   * of ignored paths. The path identifier could be either the
   * absolute path in case the reffered resource is local, or <code>Url</code>
   * specifier as specified by <code>com.borland.primetime.vfs.Url</code>
   *
   * @param pathIdentifier
   *          the path identifier could be either the absolute path in case the
   *          reffered resource is local, or <code>Url</code> specifier as
   *          returned by <code>getFullName()</code> in
   *          <code>com.borland.primetime.vfs.Url</code>
   */
  public boolean isIgnoredPath(String pathIdentifier) {
    return RefactorItPropGroup.getIgnoredSourcePath().contains(pathIdentifier);
  }

//  public void setProject(Project project) {
//    this.riProject = project;
//  }

  /**
   * backwards compatibility conversion function.
   * In previous versions (up to 1.1 in cvs) paths were saved in persistance
   * in a string where elements were separated by '|' instead of default
   * File.pathSeparator and prefixed with 'L' (meaning 'local') or 'U' (meaning
   * 'url'). Now both of the default File.pathSeparator is used and no difference
   * is made between sources made from local files and those made from urls.
   *
   *
   * @param sourcePathStr
   * @return new style string (File.pathSeparator as path separator and absolute
   * paths as pathelements)
   */
  public static boolean isOldStylePath(String sourcePathStr) {
    if (sourcePathStr.indexOf('|') != -1) { //old style
      return true;
      //      StringBuffer sb=new StringBuffer();
      //      List list=StringUtil.deserializeStringList(sourcePathStr);
      //      Iterator i=list.iterator();
      //      String pathElement;
      //      if(i.hasNext()){
      //        sb.append(isOldStylePathElement((String)i.next()));
      //      }
      //      while(i.hasNext()){
      //        sb.append(File.pathSeparator);
      //        sb.append(isOldStylePathElement((String)i.next()));
      //      }
      //      return sb.toString();
    }
    return isOldStylePathElement(sourcePathStr);
  }

  private static boolean isOldStylePathElement(String pathElement) {
    if ("".equals(pathElement)) {
      return false;
    }
    char firstCharInPath = pathElement.charAt(0);
    if ((firstCharInPath == 'L'
        || firstCharInPath == 'U') && (pathElement.length() > 1
        && pathElement.charAt(1) != ':')) {
      return true;
    }
    return false;
  }

  /**
   * Returns a list of path strings,
   *
   * @param pathStr paths delimeted with a <code>File.pathSeparator</code>
   * @return
   */
  public static List pathStrAsList(String pathStr) {
  	return Arrays.asList(StringUtil.split(pathStr, File.pathSeparator));
  }
}
