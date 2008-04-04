/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.SinglePointVisitor;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Use this class to create new snapshots of current code.
 *
 * Main activity: choosing and looking up things to store in Snapshot.
 */
public class SnapshotBuilder {
  private static final int PROJECT_PARSE_TIME_PERCENTAGE = 70;

  public static final ProgressMonitor.Progress PROJECT_PARSE_PROGRESS =
      new ProgressMonitor.Progress(0, PROJECT_PARSE_TIME_PERCENTAGE);
  public static final ProgressMonitor.Progress SNAPSHOT_BUILD_PROGRESS =
      new ProgressMonitor.Progress(PROJECT_PARSE_TIME_PERCENTAGE, 100);

  boolean storeBinItemsInSnapshotItems;

  public SnapshotBuilder() {
    this(false);
  }

  public SnapshotBuilder(boolean storeBinItemsInSnapshotItems) {
    this.storeBinItemsInSnapshotItems = storeBinItemsInSnapshotItems;
  }

  public boolean canCreateSnapshotFrom(Object oneBinItem) {
    return
        (oneBinItem instanceof BinItem
        && SnapshotItem.canCreateSnapshotOf((BinItem) oneBinItem))
        || oneBinItem instanceof Project;
  }

  public boolean canCreateMultiTargetSnapshotFrom(Object[] multipleTargets) {
    for (int i = 0; i < multipleTargets.length; i++) {
      if (canCreateSnapshotFrom(multipleTargets[i])) {
        return true;
      }
    }

    return false;
  }

  /** Requires a ProgressListener to be registered in the CFlowContext */
  public Snapshot createSnapshot(final Object binItem, final String description,
      final Calendar date, final Project project) {
    final Set items = new HashSet();

    AbstractIndexer.runWithProgress(PROJECT_PARSE_PROGRESS, new Runnable() {
      public void run() {
        addSnapshot(binItem, items, project, SNAPSHOT_BUILD_PROGRESS);
      }
    });

    return new Snapshot(description, date, items);
  }

  /** Requires a ProgressListener to be registered in the CFlowContext */
  public Snapshot createMultiTargetSnapshot(final Object[] multipleTargets,
      final String description, final Calendar date, final Project project) {
    final Set items = new HashSet();

    AbstractIndexer.runWithProgress(PROJECT_PARSE_PROGRESS, new Runnable() {
      public void run() {
        for (int i = 0; i < multipleTargets.length; i++) {
          addSnapshot(multipleTargets[i], items, project,
              SNAPSHOT_BUILD_PROGRESS.subdivision(i, multipleTargets.length));
        }
      }
    });

    return new Snapshot(description, date, items);
  }

  void addSnapshot(
      Object binItem, Collection snapshot, Project project,
      ProgressMonitor.Progress progress
      ) {
    if (binItem instanceof BinPackage) {
      addSnapshot((BinPackage) binItem, snapshot, progress);
    } else if (binItem instanceof BinCIType) {
      addSnapshot((BinCIType) binItem, snapshot);
    } else if (binItem instanceof Project) {
      addSnapshot(project, snapshot, progress);
    }
  }

  private BinPackage[] getAllPackagesFromSourcepath(Project project) {
    Set packages = new HashSet();

    List sources = project.getCompilationUnits();
    for (int i = 0; i < sources.size(); i++) {
      packages.add(((CompilationUnit) sources.get(i)).getPackage());
    }

    return (BinPackage[]) packages.toArray(new BinPackage[0]);
  }

  public void addSnapshot(Project project, Collection snapshot,
      ProgressMonitor.Progress progress) {
    BinPackage[] packages = getAllPackagesFromSourcepath(project);

    for (int i = 0; i < packages.length; i++) {
      addSnapshotWithoutChildPackages(packages[i], snapshot,
          progress.subdivision(i, packages.length));
    }
  }

  public void addSnapshotWithoutChildPackages(BinPackage binPackage,
      Collection snapshot,
      ProgressMonitor.Progress progress) {
    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    int doneItems = 0;
    for (Iterator i = binPackage.getAllTypes(); i.hasNext(); ) {
      listener.progressHappened(progress.getPercentage(
          doneItems, binPackage.getTypesNumber()));

      BinCIType type = ((BinTypeRef) i.next()).getBinCIType();
      addSnapshot(type, snapshot);

      doneItems++;
    }
  }

  public void addSnapshot(BinPackage binPackage, Collection snapshot,
      ProgressMonitor.Progress progress) {
    List subPackages = binPackage.getSubPackages();

    addSnapshotWithoutChildPackages(binPackage, snapshot,
        progress.subdivision(0, subPackages.size() + 1));

    for (int i = 0; i < subPackages.size(); i++) {
      BinPackage subPackage = (BinPackage) subPackages.get(i);
      addSnapshotWithoutChildPackages(subPackage, snapshot,
          progress.subdivision(i + 1, subPackages.size() + 1));
    }
  }

  private void addSnapshot(final BinCIType t, final Collection snapshot) {
    BinItemVisitor typeVisitor = new SinglePointVisitor() {
      public void onEnter(Object o) {
        if (!(o instanceof BinItem)) {
          return;
        }

        BinItem item = (BinItem) o;

        if (SnapshotItem.canCreateSnapshotOf(item)) {
          snapshot.add(new SnapshotItem(item,
              SnapshotBuilder.this.storeBinItemsInSnapshotItems));

          // Only packages that have types inside them will go to snapshots.
          // This ensures that referenced packages like "java.lang" stay out of snapshots.
        }
        if (item instanceof BinCIType) {
          snapshot.add(new SnapshotItem(((BinType) item).getPackage(),
              SnapshotBuilder.this.storeBinItemsInSnapshotItems));
        }
      }

      public void onLeave(Object o) {}

      public boolean shouldVisitContentsOf(BinItem item) {
        return SnapshotItem.shouldVisitContentsOf(item);
      }
    };
    t.accept(typeVisitor);
  }
}
