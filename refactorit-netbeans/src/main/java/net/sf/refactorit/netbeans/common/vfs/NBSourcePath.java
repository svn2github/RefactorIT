/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;


import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.loader.MultiFileChangeMonitor;
import net.sf.refactorit.netbeans.common.projectoptions.NBFileUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.FileChangeMonitor;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePathFilter;
import net.sf.refactorit.vfs.local.LocalFileChangeMonitor;

import org.openide.filesystems.FileObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of SourcePath VFS for netbeans/forte
 *
 * @author Igor Malinin
 */
public class NBSourcePath extends AbstractSourcePath {
  private static SourcePathFilter filter = new SourcePathFilter();
  private WeakReference ideProjectRef;

  public NBSourcePath(Object ideProject) {
    this.ideProjectRef = new WeakReference(ideProject);
  }

  public WeakReference getIdeProjectReference() {
    return ideProjectRef;
  }

//  public Object getIdeProject() {
//    return null;
//  }

  public Source[] getAutodetectedElements() {
    final Object ideProject = ideProjectRef.get();
    if(ideProject == null) {
      return new Source[]{};
    } else {
      return getSourcesFromFileObjects(PathUtil.getInstance()
          .getAutodetectedSourcepath(ideProject, false));
    }
  }

  public void setValidExtensions(String[] extensions) {
    super.setValidExtensions(extensions);
    NBFileUtil.setValidExtensions(extensions); // set valid extensions for
    // static NB filters also
  }

  public FileChangeMonitor getFileChangeMonitor() {
    if (fileChangeMonitor == null) {
      final PathItemReference[] paths;
      final Object ideProject = ideProjectRef.get();

      if(ideProject != null) {
        paths = PathUtil.getInstance().getSourcepath(ideProject);
      } else {
        paths = new PathItemReference[]{};
      }

      List nbPaths = new ArrayList();

      boolean localFound = false;

      for (int i = 0; i < paths.length; i++) {
        if (paths[i].isFileObject()) {
          nbPaths.add(paths[i].getFileObject());
        } else {
          localFound = true;
        }
      }

      NBFileChangeMonitor m1 = new NBFileChangeMonitor(this,
              (FileObject[]) nbPaths.toArray(new FileObject[0]));

      if (localFound) {
        HashSet currentLocalSources = new HashSet(200);
        collectLocalSources(currentLocalSources);

        LocalFileChangeMonitor m2 = new LocalFileChangeMonitor(
                currentLocalSources) {
          protected void collectSources(Collection result) {
            collectLocalSources(result);
          }
        };

        fileChangeMonitor = new MultiFileChangeMonitor(new FileChangeMonitor[]{
            m1, m2});
      } else {
        fileChangeMonitor = m1;
      }
    }

    return fileChangeMonitor;
  }

  void collectLocalSources(Collection result) {
    final Object ideProject = ideProjectRef.get();
    if (ideProject == null) {
      return;
    }
      PathItemReference[] paths = PathUtil.getInstance().getSourcepath(
          ideProject);

      PathUtil.IgnoreListFilter filter = PathUtil.getInstance()
          .getIgnoreListFilter(ideProject);
      for (int i = 0; i < paths.length; i++) {
        if (paths[i].isValid() && paths[i].isLocalFile()) {
          iterateDirectory(paths[i].getSource(), result, filter, null);
        }
      }
  }

  public Source[] getRootSources() {
    final Object ideProject = ideProjectRef.get();

    if(ideProject == null) {
      return new Source[] {};
    }

    PathItemReference[] items = PathUtil.getInstance().getSourcepath(ideProject);
    return getSourcesFromFileObjects(items);
  }

  public List getIgnoredSources() {
    final Object ideProject = ideProjectRef.get();

    if(ideProject == null) {
      return new ArrayList(0);
    }

    PathItemReference[] items = PathUtil.getInstance().getIgnoredSourceDirectories(ideProject);
    List result = new ArrayList();

    for (int i = 0; i < items.length; i++) {
      result.add(items[i].getAbsolutePath());
    }
    return result;
  }

  private Source[] getSourcesFromFileObjects(
          PathItemReference[] pathItemReferences) {
    final Object ideProject = ideProjectRef.get();
    if(ideProject == null) {
      return new Source[] {};
    }

    PathUtil.IgnoreListFilter filter = PathUtil.getInstance()
            .getIgnoreListFilter(ideProject);
    List sources = new ArrayList(pathItemReferences.length);
    for (int i = 0; i < pathItemReferences.length; i++) {
      if (pathItemReferences[i].isValid()) {
        Source source = pathItemReferences[i].getSource();

        // Checking entire filesystem at a time here
        if (!filter.inIgnoreList(source)
                && (source.isFile() || source.isDirectory())) {
          sources.add(source);
        }
      }
    }

    return (Source[]) sources.toArray(new Source[sources.size()]);
  }

  public List getAllSources() {
    return getAllSources(null);
  }

  public List getAllSources(WildcardPattern[] patterns) {
    final Object ideProject = ideProjectRef.get();

    if(ideProject == null) {
      return new ArrayList(0);
    }
    PathUtil.IgnoreListFilter ignoreListFilter = PathUtil.getInstance()
            .getIgnoreListFilter(ideProject);

    filter.initialize();
    ArrayList result = new ArrayList(200);

    Source[] sources = getRootSources();
    for (int i = 0; i < sources.length; ++i) {
      iterateDirectory(sources[i], result, ignoreListFilter, patterns);
    }

    // FIXME: it should be slow, better filter out original paths
    final Set uniqueSourcesSet = new HashSet();
    uniqueSourcesSet.addAll(result);

    final List uniqueSources = new ArrayList(uniqueSourcesSet.size());
    uniqueSources.addAll(uniqueSourcesSet);

    return uniqueSources;
  }

  void iterateDirectory(Source parent, Collection result,
          PathUtil.IgnoreListFilter filter, WildcardPattern[] patterns) {
    if (!parent.isDirectory()) {
      return;
    }

    Source children[] = parent.getChildren();
    if (children == null) {
      return;
    }

    for (int i = 0; i < children.length; ++i) {
      Source curSource = children[i];

      if (curSource.isFile() && fileInSourcepath(curSource, patterns)) {
        result.add(curSource);
      } else if (curSource.isDirectory()
              && shouldIterateInto(curSource, filter)) {
        iterateDirectory(curSource, result, filter, patterns);
      }
    }
  }

  void collectDirectories(FileObject parent, Set result,
          PathUtil.IgnoreListFilter filter) {
    if (shouldIterateInto(NBSource.getSource(parent), filter)) {
      result.add(parent);

      Enumeration e = parent.getFolders(true);

      while (e.hasMoreElements()) {
        FileObject dir = (FileObject) e.nextElement();

        if (shouldIterateInto(NBSource.getSource(dir), filter)) {
          result.add(dir);
        }
      }
    }
  }

  /** Note: does not check parent folders */
  boolean fileInSourcepath(Source file, WildcardPattern[] patterns) {
    if (patterns == null) {
      return fileAcceptedByName(file);
    } else {
      return fileAcceptedByPattern(file.getName(), patterns);
    }
  }

  /** Note: does not check parent folders */
  boolean shouldIterateInto(Source folder, PathUtil.IgnoreListFilter filter) {
    boolean result = directoryAcceptedByName(folder)
            && (!filter.inIgnoreList(folder));

    return result;
  }

  boolean fileAcceptedByName(Source file) {
    if (file == null) {
      return false;
    }
    return isValidSource(file.getName());
  }

  private boolean directoryAcceptedByName(Source folder) {
    return filter.acceptDirectoryByName(folder.getName().toLowerCase());
  }

  public boolean sourceAcceptedIfNotIgnored(Source source) {
    if (source.isFile()) {
      return fileAcceptedByName(source);
    } else {
      return directoryAcceptedByName(source);
    }
  }

  public List getNonJavaSources(WildcardPattern[] patterns) {
    return getAllSources(patterns);
  }
}
