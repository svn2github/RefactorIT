/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.rename.ParentFinder;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.DirCreator;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.FileEraser;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public final class SourceRelocator {
//  private TransformationList transList;// = new TransformationList();
  private final EditorManager manager;

  private SavedChildren snapshot;
  private final Project project;

  private final ParentFinder parentFinder = new ParentFinder();

  private int allSources = 0;
  private int processedSources = 0;

  public SourceRelocator(Project project, EditorManager manager) {
    this.project = project;
    this.manager = manager;
  }

  /** @param sourcesFailedToRelocate will contain failed Source instances */
  public void relocate(List packages, String oldPrefix, String newPrefix,
      List sourcesFailedToRelocate) {
    Set folders = parentFinder.findFolders(oldPrefix, packages);
    sourcesFailedToRelocate.addAll(parentFinder.getSourcesInWrongFolders());

    processedSources = 0;
    allSources = 0;
    for (Iterator i = folders.iterator(); i.hasNext(); ) {
      Source toMove = (Source) i.next();
      allSources += countChildrenFiles(toMove);
    }

    for (Iterator i = folders.iterator(); i.hasNext(); ) {
      Source toMove = (Source) i.next();
      Source root = parentFinder.findRoot(oldPrefix, toMove);

      if (root == null) {
        continue; // renamable is probably higher than mount point
      }

      // Taking snapshot to allow "recursive" moves to subfolders
      snapshot = new SavedChildren(toMove);

      Project project = ((BinPackage) packages.get(0)).getProject();
      SourceHolder sourceDir = new SimpleSourceHolder(root, project);
      SourceHolder destinationDir = new SimpleSourceHolder(project);

      manager.addEditor(new DirCreator(sourceDir, destinationDir, newPrefix, toMove.inVcs()));

      recursiveMove(toMove, destinationDir);
    }
  }

  private int countChildrenFiles(Source source) {
    int result = 0;
    Source[] children = source.getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i].isFile()) {
        ++result;
      } else {
        result += countChildrenFiles(children[i]);
      }
    }

    return result;
  }

  private void relocateChildren(Source parent, SourceHolder parentDir) {
    Source[] children = snapshot.getChildren(parent);

    relocateFolders(children, parentDir);
    relocateFiles(children, parentDir);

    deleteEmptyParents(parent);
  }

  // HACK: this mkdirs-kind of isVcs checking logic should be moved away and encapsulated
  // in NBSource (and Source in general)
  private void relocateFolders(final Source[] sources, SourceHolder parentDir) {
    for (int i = 0; i < sources.length; i++) {
      Source child = sources[i];
      SourceHolder newDir;

      if (child.isDirectory()
          && !AbstractSource.inVersionControlDirList(child.getName())) {
        newDir = new SimpleSourceHolder(parentDir.getProject());

        manager.addEditor(new DirCreator(parentDir, newDir, child.getName(), child.inVcs()));

//        if (newDir == null) {
//          errors.add(child);
//        } else {
          relocateChildren(child, newDir);
//          child.delete();
//        }
      }
    }
  }

  public void relocateFilesTransaction(Source[] sources, SourceHolder transObj) {
    processedSources = 0;
    allSources = 0;
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].isFile()) {
        allSources++;
      }
    }
    relocateFiles(sources, transObj);
  }

  private void relocateFiles(final Source[] sources,
      final SourceHolder parentDir) {
    for (int i = 0; i < sources.length; i++) {
      Source source = sources[i];

      if (source.isFile()) {
        if (shouldDelete(source)) {

          manager.addEditor(new FileEraser(new SimpleSourceHolder(source, project)));
        } else {
          CompilationUnit compilationUnit
              = CompilationUnit.getCompilationUnit(source, project);
          if(compilationUnit !=null){
            manager.addEditor(new FileRenamer(compilationUnit, parentDir, source.getName()));
          } else {
            manager.addEditor(new FileRenamer(project.getNonJavaUnit(source), parentDir, source.getName()));
          }
        }
      }

      notifyOneSourceProcessed();
    }
  }

  private void notifyOneSourceProcessed() {
    ProgressListener listener = (ProgressListener) CFlowContext.get(
        ProgressListener.class.getName());
    if (listener != null) {
      listener.progressHappened(ProgressMonitor.Progress.FULL.
          getPercentage(++processedSources, allSources));
    }
  }

  private void recursiveMove(Source toMove, SourceHolder destination) {
    relocateChildren(toMove, destination);
  }

  static boolean shouldDelete(Source source) {
    return source.getName().endsWith(".class");
  }

  private void deleteEmptyParents(Source source) {
    List sourcepathRoots = getSourcepathRoots();
    
    while (source != null && ( ! sourcepathRoots.contains(source))) {
      manager.addEditor(new FileEraser(new SimpleSourceHolder(source, project)));
      source = source.getParent();
    }
  }

  private List getSourcepathRoots() {
    return Arrays.asList(project.getPaths().getSourcePath().getRootSources());
  }
}
