/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.usage.filters.ApiDiffFilter;
import net.sf.refactorit.refactorings.apisnapshot.Snapshot;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotBuilder;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotDiff;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotIO;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;

import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.ResourceBundle;


public class ApiDiffAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.ApiDiffAction";
  public static final String NAME = "API Diff";

  static final ResourceBundle bundle =
    ResourceUtil.getBundle(ApiDiffModule.class);

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.isAssignableFrom(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)) {
      return true;
    }
    return false;
  }

  public boolean isReadonly() {
    return true;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean run(final RefactorItContext context, Object object) {
    ApiDiffDialog dialog = new ApiDiffDialog(context);
    dialog.show();
    if (!dialog.getOkWasPressed()) {
      return false;
    }

    final Object target;
    if (object instanceof BinConstructor) {
      target = ((BinConstructor) object).getOwner().getBinCIType();
    } else {
      target = object;
    }

    ApiDiffFilter filter = new ApiDiffFilter();
    context.setState(filter);

    ApiDiffFilterDialog filterDialog = new ApiDiffFilterDialog(context);
    filterDialog.show();
    if (!filterDialog.getOkWasPressed()) {
      return false;
    }

    final SnapshotsToCompare stc = new SnapshotsToCompare(dialog, bundle, target, context);

    try {
      final SnapshotDiff[] diff = new SnapshotDiff[1];
      final ApiDiffModel[] createApiDiffModel = new ApiDiffModel[1];

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          if (!stc.createFirstSnapshot() || !stc.createSecondSnapshot()) {
            return;
          }

          diff[0] = stc.createSnapshotDiff();
          createApiDiffModel[0] = createApiDiffModel(diff[0], context);
          if (diff[0] == null) {
            return;
          }
        }
      }, bundle.getString("comparing.snapshots"), true);

      BinTreeTable table
          = new BinTreeTable(createApiDiffModel[0], context);
      table.getTree().setCellRenderer(new ApiDiffCellRenderer());

      ResultArea results = ResultArea.create(table, context,
          ApiDiffAction.this);
      results.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(
          context, bundle.getString("tab.title"), results);

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.apids");
      initFilterButtons(panel, context, target, stc);
    } catch (SearchingInterruptedException ex) {
    }

    return false;
  }

  void initFilterButtons(final BinPanel panel, final RefactorItContext context,
      Object anObject, final SnapshotsToCompare stc) {
    final BinItemReference objectReference = BinItemReference.create(anObject);

    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Object object = objectReference.restore(context.getProject());

        ApiDiffFilterDialog filterDialog = new ApiDiffFilterDialog(context);
        filterDialog.show();
        if (!filterDialog.getOkWasPressed()) {
          return;
        }

        reload(panel, context, object, stc);
      }
    };

    panel.setFilterActionListener(listener);
  }

  void reload(final BinPanel panel,
      final RefactorItContext context,
      final Object object,
      final SnapshotsToCompare stc
  ) {
    try {
      final SnapshotDiff[] diff = new SnapshotDiff[1];
      final ApiDiffModel[] createApiDiffModel = new ApiDiffModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          diff[0] = stc.createSnapshotDiff();
          createApiDiffModel[0] = createApiDiffModel(diff[0], context);
          if (diff[0] == null) {
            return;
          }
        }
      }, bundle.getString("comparing.snapshots"), true);

      BinTreeTable table = new BinTreeTable(createApiDiffModel[0], context);
      table.getTree().setCellRenderer(new ApiDiffCellRenderer());

      panel.reload(table);
      table.smartExpand();
      ErrorsTab.addNew(context);
      context.showTab(panel.getIDEComponent());
    } catch (SearchingInterruptedException ex) {
      //
    }
  }

  public String getName() {
    return NAME;
  }

  ApiDiffModel createApiDiffModel(final SnapshotDiff diff,
      RefactorItContext context) {
    return new ApiDiffModel(diff, context.getProject());
  }
}


class SnapshotsToCompare {
  private ApiDiffDialog dialog;
  private ResourceBundle bundle;
  private Object object;
  private RefactorItContext context;
  private Snapshot firstSnapshot;
  private Snapshot secondSnapshot;

  SnapshotsToCompare(ApiDiffDialog dialog, ResourceBundle bundle,
      Object object, RefactorItContext context) {
    this.dialog = dialog;
    this.bundle = bundle;
    this.object = object;
    this.context = context;
  }

  public boolean createFirstSnapshot() {
    try {
      firstSnapshot = SnapshotIO.getSnapshotFromFile(dialog.
          getFirstSnapshotFileName());
    } catch (IOException e) {
      RitDialog.showMessageDialog(context,
          bundle.getString("file.open.error") + dialog.getFirstSnapshotFileName(),
          "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  public boolean createSecondSnapshot() {
    secondSnapshot = getSnapshotToCompareAgainst();
    if (secondSnapshot == null) {
      return false;
    }
    return true;
  }

  private Snapshot getSnapshotToCompareAgainst() {
    if (dialog.getShouldCompareAgainstCurrentCode()) {
      if (object instanceof Object[]) {
        return new SnapshotBuilder().createMultiTargetSnapshot((Object[])
            object, "", Calendar.getInstance(), context.getProject());
      } else {
        return new SnapshotBuilder().createSnapshot(object, "",
            Calendar.getInstance(), context.getProject());
      }
    } else {
      try {
        return SnapshotIO.getSnapshotFromFile(dialog.getAnotherSnapshotFileName());
      } catch (IOException e) {
        RitDialog.showMessageDialog(context,
            bundle.getString("file.open.error")
            + dialog.getAnotherSnapshotFileName());
        return null;
      }
    }
  }

  public SnapshotDiff createSnapshotDiff() {
    final SnapshotDiff diff;
    try {
      diff = new SnapshotDiff(firstSnapshot, secondSnapshot,
          (ApiDiffFilter) context.getState());
    } catch (Exception e) {
      System.out.println("EXCEPTION -- please report");
      e.printStackTrace();
      RitDialog.showMessageDialog(context,
          bundle.getString("file.parsing.error"), "Error",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }

    return diff;
  }

  public Snapshot getFirstSnapshot() {
    return firstSnapshot;
  }

  public Snapshot getSecondSnapshot() {
    return secondSnapshot;
  }
}
