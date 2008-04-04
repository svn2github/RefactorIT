/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apisnapshot;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.apisnapshot.Snapshot;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotBuilder;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotIO;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JOptionPane;

import java.io.File;
import java.util.Calendar;
import java.util.ResourceBundle;


public class ApiSnapshotAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.ApiSnapshotAction";
  public static final String NAME = "API Snapshot";

  private static ResourceBundle bundle = ResourceUtil.getBundle(
      ApiSnapshotModule.class);

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.isAssignableFrom(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)) {
      return true;
    } else {
      return false;
    }
  }

  public String getKey() {
    return KEY;
  }

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean run(final RefactorItContext context, final Object object) {
    final Object target;
    if (object instanceof BinConstructor) {
      target = ((BinConstructor) object).getOwner().getBinCIType();
    } else {
      target = object;
    }

    if (target instanceof Object[]) {
      if (!new SnapshotBuilder().canCreateMultiTargetSnapshotFrom((Object[])
          target)) {
        return false; // Silent ignore
      }
    } else {
      if (!new SnapshotBuilder().canCreateSnapshotFrom(target)) {
        return false; // Silent ignore
      }
    }

    ApiSnapshotDialog dialog = new ApiSnapshotDialog(context);
    dialog.show();

    final String fileName = dialog.getChosenFileName();
    if (dialog.okPressed()) {
      if (new File(fileName).exists()) {
        if (!confirmOverwrite(context, fileName)) {
          return false;
        }
      }

      final ApiSnapshotDialog fDialog = dialog;
      try {
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            saveSnapshot(context, target, fileName,
                fDialog.getChosenTitle(), context.getProject());
          }
        }, true);
      } catch (SearchingInterruptedException ex) {
      }
    }

    return false;
  }

  void saveSnapshot(
      IdeWindowContext context, final Object binObject,
      final String fileName, final String title, final Project project
  ) {
    Snapshot snapshot;
    if (binObject instanceof Object[]) {
      snapshot = new SnapshotBuilder().createMultiTargetSnapshot((Object[])
          binObject, title, Calendar.getInstance(), project);
    } else {
      snapshot = new SnapshotBuilder().createSnapshot(binObject, title,
          Calendar.getInstance(), project);
    }

    try {
      SnapshotIO.writeSnapshotToFile(snapshot, fileName);
    } catch (Exception e) {
      DialogManager.getInstance().showCustomError(context,
          bundle.getString("cannot.write.to.file") + ": " + fileName);
    }
  }

  private boolean confirmOverwrite(IdeWindowContext context, String fileName) {
    int res = RitDialog.showConfirmDialog(context,
        bundle.getString("confirm.overwrite") + " " + fileName + "?");
    return (res == JOptionPane.YES_OPTION);
  }

  public String getName() {
    return NAME;
  }
}
