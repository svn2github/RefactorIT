/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse.vfs;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceUtil;
import net.sf.refactorit.vfs.Source.SourceFilter;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * TODO: Implement required project dependencies
 * TODO: overlook getAllSources and getFileChangeMonitor performance
 *       and improve using eclipse API when needed.
 *
 * @author Tonis Vaga
 */
public class EclipseSourcePath extends AbstractSourcePath {
  private static final Logger log = Logger.getLogger(EclipseSourcePath.class);

  private static final boolean debug = false;

  private IProject project;

  ProjectOptions projectOptions;

  public EclipseSourcePath(IProject project, ProjectOptions options) {
    this.project = project;
    this.projectOptions = options;
  }

  /**
   * Currently we support customized sourcepath only inside
   * autodetected sourcepath
   */
  public Source[] getPossibleRootSources() {
    return getAutodetectedElements();
  }

  public Source[] getRootSources() {
    if (project == null || !project.isOpen()) {
      return Source.NO_SOURCES;
    }

    Assert.must(JavaCore.create(project) != null,
        "Not java project " + project.getName());

    Source[] autoDetectedRoots = getAutodetectedElements();

    if (projectOptions.isAutoDetect()) {
      return autoDetectedRoots;
    }

    List result = new ArrayList();

    PathItem[] items = projectOptions.getSourcePath().getItems();

    for (int i = 0; i < items.length; i++) {
      Source src = SourceUtil.findSource(
          autoDetectedRoots, items[i].getAbsolutePath());

      if (src == null) {
        log.error("source for " + items[i] + " doesn't exist");
        continue;
      }

      result.add(src);
    }

    return (Source[]) result.toArray(new Source[result.size()]);
  }

  public List getIgnoredSources() {
    List li = projectOptions.getIgnoredSourcePath().toPathItems();
    List result = new ArrayList();

    for (int i = 0; i < li.size(); i++) {
      result.add(((PathItem) li.get(i)).getAbsolutePath());
    }

    return result;
  }

  public Source[] getAutodetectedElements() {
    final List tempList = new ArrayList();

    try {
      IClasspathEntry[] sourceEntries = getSourcePathEntries();
      for (int i = 0; i < sourceEntries.length; i++) {
        IClasspathEntry entry = sourceEntries[i];
        IPath path = entry.getPath();

        if (debug) {
          log.debug("folder path = "+path.toOSString());
        }

        IContainer folder = getFolder(path);

        EclipseSource source = EclipseSource.getSource(folder);
        tempList.add(source);

        if (debug) {
          log.debug("folder = " + source.getAbsolutePath());
        }
      }
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

    Source autoDetectedRoots[] = (Source[])
        tempList.toArray(new Source[tempList.size()]);

    return autoDetectedRoots;
  }

  private IContainer getFolder(IPath path) {
    IContainer folder;

    IWorkspaceRoot root = project.getWorkspace().getRoot();

    if (path.segmentCount() == 1) {
      // project folder is also source folder
      folder = root.getProject(path.segment(0));
    } else {
      folder = root.getFolder(path);
    }

    return folder;
  }

  private IClasspathEntry[] getSourcePathEntries() throws JavaModelException {
    IJavaProject jProject = getJavaProject();

    List result = new ArrayList();
    IClasspathEntry[] pathEntries = jProject.getResolvedClasspath(true);
    for (int i = 0; i < pathEntries.length; i++) {
      IClasspathEntry entry = pathEntries[i];
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE
          /*|| entry.getEntryKind() == IClasspathEntry.CPE_PROJECT*/) {
        result.add(entry);
        if (debug) {
          log.debug("exclusion patterns = " +
              Arrays.asList(entry.getExclusionPatterns()));
        }
      }
    }

    return (IClasspathEntry[]) result.toArray(new IClasspathEntry[result.size()]);
  }

  private IJavaProject getJavaProject() {
    return JavaCore.create(project);
  }

  /*
   * @see net.sf.refactorit.vfs.SourcePath#getAllSources()
   */
  public List getAllSources() {
    ArrayList result = new ArrayList(200);
    if (project == null || !project.isOpen()) { // just in case
      return result;
    }

    final IJavaProject javaProject = getJavaProject();

    Source[] rootSources = getRootSources();
    SourceFilter filter = new SourceFilter() {
      private PathItem[] ignoredItems =
          projectOptions.getIgnoredSourcePath().getItems();

      public boolean accept(Source source) {
        IResource resource = ((EclipseSource) source).getResource();
        if (resource.getType() != IResource.FILE) {
          // do not stop early on top level folders because
          // something might be included on deeper level
          return !isIgnored(source);
        }

        return javaProject.isOnClasspath(resource) && !isIgnored(source);
      }

      /**
       * @param source
       * @return true if it is in ignored path
       */
      private boolean isIgnored(final Source source) {
        if (!projectOptions.isAutoDetect()) {
          for (int i = 0; i < ignoredItems.length; i++) {
            String path = source.getAbsolutePath();
            String ignored = ignoredItems[i].getAbsolutePath();
            if (path.indexOf(ignored) == 0) {
              return true;
            }
          }
        }

        return false;
      }
    };

    for (int i = 0; i < rootSources.length; i++) {
      iterateDirectory(rootSources[i], result, filter);
    }

    return result;
  }

  public List getNonJavaSources(WildcardPattern[] patterns) {
    // FIXME: overlook and user Source instead maybe?
    IJavaProject javaProject = getJavaProject();
    List result = new ArrayList();
    try {

      Source[] rootSources = getRootSources();

      for (int i = 0; i < rootSources.length; i++) {
        IResource resource = ((EclipseSource) rootSources[i])
                .getResource();
        IPackageFragmentRoot packageRoot = javaProject
            .getPackageFragmentRoot(resource);
        if (!packageRoot.exists()) {
          continue;
        }

        collectNonJavaResources(result,packageRoot.getNonJavaResources(),patterns);

        // must collect recursively
        collectNonJavaResources(result,packageRoot.getChildren(),patterns);
      }
      return result;
    } catch (JavaModelException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.ECLIPSE_INTERNAL_ERROR,e);
    }
  }

  private void collectNonJavaResources(List result, Object[] nonJavaResources, WildcardPattern[] patterns) {
    for (int i = 0; i < nonJavaResources.length; i++) {
      Object resource = nonJavaResources[i];

      if (resource instanceof IFile) {
        IFile file = ((IFile) resource);
        if (fileAcceptedByPattern(file.getName(), patterns)) {
          result.add(EclipseSource.getSource(file));
        }
      } else if (resource instanceof IPackageFragment) {
        try {
          collectNonJavaResources(result,
              ((IPackageFragment) resource).getNonJavaResources(), patterns);
        } catch (JavaModelException e) {
          AppRegistry.getExceptionLogger().error(e, this);
          throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
        }
      } else {
        log.warn("ignoring non-java resource " + resource);
      }
    }
  }

  public void setOptions(ProjectOptions options) {
    this.projectOptions = options;
  }
}
