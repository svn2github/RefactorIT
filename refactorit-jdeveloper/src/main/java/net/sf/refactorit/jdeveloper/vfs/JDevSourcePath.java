/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.vfs;


import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.jdeveloper.projectoptions.ProjectConfiguration;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePathFilter;
import net.sf.refactorit.vfs.Source.SourceFilter;
import oracle.ide.model.DeployableTextNode;
import oracle.ide.model.Node;
import oracle.ide.net.URLPath;
import oracle.jdeveloper.model.JProject;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Tanel
 */
public class JDevSourcePath extends AbstractSourcePath {

  JProject jproject;

  private SourcePathFilter filter = new SourcePathFilter();

  private String cachedIgnoredSourcePathStr="";
  private Set cachedIgnoredSourcePaths=new HashSet();

  /** Creates new JDevSourcePath */
  public JDevSourcePath(JProject jproject) {
    this.jproject = jproject;
  }


  /**
   * List of root Sources.
   *
   * @return list of root Source directories
   */
  public Source[] getRootSources() {
    Source[] ret= getAutodetectedRootSources();
    return ret;
  }

  public List getIgnoredSources() {
    ProjectConfiguration projectConfig = ProjectConfiguration.getActiveInstance();
    String ignoredSourcepathStr = projectConfig
        .get(ProjectConfiguration.PROP_IGNORED_SOURCEPATH);

    if(ignoredSourcepathStr==null)
      ignoredSourcepathStr="";

    if(!this.cachedIgnoredSourcePathStr.equals(ignoredSourcepathStr)){
      cachedIgnoredSourcePathStr = ignoredSourcepathStr;
      StringTokenizer st=new StringTokenizer(ignoredSourcepathStr,File.pathSeparator);
      cachedIgnoredSourcePaths = new HashSet(st.countTokens());
      while(st.hasMoreElements()) {
        cachedIgnoredSourcePaths.add(st.nextElement());
      }
    }

    ArrayList result = new ArrayList();
    result.addAll(cachedIgnoredSourcePaths);
    return result;
  }

  /**
   * checks if the absolute path from the parameter is
   * contained in cached set of ignored paths. (If the property labled
   * ProjectConfiguration.PROP_IGNORED_SOURCEPATH has changed, then the cached
   * set is updated prior to the check)
   */
  public boolean isIgnoredPath(String absolutePath){
    ProjectConfiguration projectConfig = ProjectConfiguration.getActiveInstance();
    String ignoredSourcepathStr = projectConfig
        .get(ProjectConfiguration.PROP_IGNORED_SOURCEPATH);
    if(ignoredSourcepathStr==null)
      ignoredSourcepathStr="";
    if(!this.cachedIgnoredSourcePathStr.equals(ignoredSourcepathStr)){
      cachedIgnoredSourcePathStr = ignoredSourcepathStr;
      StringTokenizer st=new StringTokenizer(ignoredSourcepathStr,File.pathSeparator);
      cachedIgnoredSourcePaths = new HashSet(st.countTokens());
      while(st.hasMoreElements()) {
        cachedIgnoredSourcePaths.add(st.nextElement());
      }
    }
    return cachedIgnoredSourcePaths.contains(absolutePath);
  }

  /**
   * @return source path directories as provided by IDE
   */
  public Source[] getAutodetectedRootSources() {
    URLPath urlPath = jproject.getSourcePath();

    URL[] sourcePathEntries = urlPath.getEntries();
    List sourceList = new java.util.ArrayList(sourcePathEntries.length);
    for (int i = 0; i < sourcePathEntries.length; i++) {
      String fileName = sourcePathEntries[i].getFile();
      fileName = URLDecoder.decode(fileName);
      File file=new File(fileName);
      //to hide some wierd sources of type
      //C:\JDeveloper\jdev\bin\file:\C:\JDeveloper\jdk\src.zip!
      if(!file.exists())continue;
      JDevSourceDir Source = JDevSourceDir.getSource(new File(fileName), "");
      sourceList.add(Source);

    }

    return (Source[]) sourceList.toArray(new Source[sourceList.size()]);
  }

  public List getAllSources() {
    ArrayList result = new java.util.ArrayList(200);
    collectValidSources(result);
    return result;
  }

  /**
   * @param result
   */
  private void collectValidSources(Collection result) {
    Source[] roots = getRootSources();
    SourceFilter sourceFilter=new SourceFilter() {

      public boolean accept(Source source) {
        if ( source.isDirectory() ) {
          return filter.acceptDirectoryByName(source.getName().toLowerCase());
        }
        return true;
      }

    };
    for (int i = 0; i < roots.length; i++) {
      if(isIgnoredPath(roots[i].getAbsolutePath())) {
        continue;
      }
      iterateDirectory(roots[i], result, sourceFilter);
    }
  }


  public List getNonJavaSources(WildcardPattern[] patterns) {
    ArrayList result = new java.util.ArrayList(10);
    Node[] nodes = oracle.jdeveloper.runner.Source.getProjectFileList(jproject);
    for (int i = 0; i < nodes.length; i++) {
      if ((DeployableTextNode.class).isAssignableFrom(nodes[i].getClass())) {
        DeployableTextNode textNode = (DeployableTextNode) nodes[i];
        if (fileAcceptedByPattern(textNode.getShortLabel(), patterns)) {
          result.add(JDevSource.getSource(textNode));
        }
      }
    }
    return result;
  }


  public Source[] getAutodetectedElements() {

    return getRootSources();
  }
}
