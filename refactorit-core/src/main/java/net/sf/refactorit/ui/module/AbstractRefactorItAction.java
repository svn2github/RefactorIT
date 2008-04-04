/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.ShortcutKeyStrokes;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;

import javax.swing.KeyStroke;


/**
 *
 *
 * @author Anton Safonov
 */
public abstract class AbstractRefactorItAction implements RefactorItAction {

  private static final Logger log = net.sf.refactorit.common.util.AppRegistry
      .getLogger(AbstractRefactorItAction.class);

  public boolean isAvailableForTarget(Object[] target) {
    if (target == null || target.length == 0) {
      log.debug("action target was null");
      return false;
    }
    if (target.length > 1 && !this.isMultiTargetsSupported()) {
      return false;
    }
    for (int i = 0; i < target.length; i++) {
      if (target[i] == null) {
        log.error("action " + getName() + " target was null");
        return false;
      }
      if (!this.isAvailableForType(target[i].getClass())) {
        log.debug(getName() + " action is not applicable for "
            + target[i].getClass());
        return false;
      }
    }

    return true;

  }

  /**
   * KeyStroke for the accelerator purpose
   *
   * @return  name
   */
  public KeyStroke getKeyStroke() {
    return ShortcutKeyStrokes.getByKey(getKey());
  }

  public boolean run(RefactorItContext context, Object object) {
    if (!isAvailableForType(object.getClass())) {
      return false;
    }

    object = RefactorItActionUtils.unwrapTargetIfNotConstructor(object);
    final Refactoring refactoring = createRefactoring(context, object);
    if (refactoring == null) {
      return false;
    }

    RefactoringStatus status = refactoring.checkPreconditions();

    if (!processStatus(context, status)) {
      return false;
    }

    if (!readUserInput(refactoring)) {
      return false;
    }

    try {
      final RefactoringStatus[] arrStatus = new RefactoringStatus[1];

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          arrStatus[0] = refactoring.checkUserInput();
        }
      }


      , true);

      status = arrStatus[0];
    } catch (SearchingInterruptedException ex) {
      return false;
    }

    if (!processStatus(context, status)) {
      return false;
    }

    status = refactoring.apply();

    // FIXME: same as something below, need to choose one
//    if (!status.isOk()) {
//      DialogManager.getInstance().showCustomError(
//          context, status.getAllMessages());
//    }

    return processStatus(context, status);
  }

  /**
   * Override this
   * @param refactoring
   * @return true if continue, false if cancel action
   */
  public boolean readUserInput(Refactoring refactoring) {
    return true;
  }

  /**
   * @param context
   * @param status
   * @return true if can continue
   */
  protected boolean processStatus(
      IdeWindowContext context, RefactoringStatus status) {
    if (!status.isOk() && status.hasSomethingToShow() && !status.isQuestion()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), getName(), status.getJOptionMessageType());
    }

    return !status.isErrorOrFatal() && !status.isCancel();
  }

  public boolean isPreprocessedSourcesSupported(BinItem item) {
    return isPreprocessedSourcesSupported(item.getClass());
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class[] cl) {
    for (int i = 0; i < cl.length; ++i) {
      if (!isPreprocessedSourcesSupported(cl[i])) {
        return false;
      }
    }

    return true;
  }

  public void updateEnvironment(final RefactorItContext context) {
    context.rebuildAndUpdateEnvironment();
    Finder.clearInvocationMap();
  }

  public void raiseResultsPane(final RefactorItContext context) {
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(
        new Runnable() {
      public void run() {
        ErrorsTab.addNew(context);

        if(enableRaisingResultsPane()) {
          BinPanel panel = BinPanel.getBinPanelManager()
              .getLastPanelInGroup(context, getName());
          if (panel != null) {
            context.showTab(panel.getIDEComponent());
          }
        }
      }
    });

    // some elements might be used during lazy-loading of bodies
    context.getProject().getPaths().getClassPath().release();
    Finder.clearInvocationMap();
  }

  protected boolean enableRaisingResultsPane() {
    return true;
  }

  /**
   * Override this!
   * @return null if failed to create refactoring
   */
  public Refactoring createRefactoring(
      RefactorItContext context, Object object
      ) {
    if (Assert.enabled) {
      throw new UnsupportedOperationException(
          "createRefactoring() not implemented");
    }

    return null;
  }

  public boolean equals(Object o) {
    if (o instanceof AbstractRefactorItAction) {
      AbstractRefactorItAction other = (AbstractRefactorItAction) o;
      return other.getKey().equals(getKey())
          && other.getName().equals(getName())
          && other.isMultiTargetsSupported() == isMultiTargetsSupported()
          && other.isReadonly() == isReadonly();
    }

    return false;
  }

  public char getMnemonic() {
    return '\0';
  }

  public int hashCode() {
    return getKey().hashCode();
  }
}
