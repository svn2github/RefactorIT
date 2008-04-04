/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;



import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.JWarningDialog;
import net.sf.refactorit.ui.module.ChangesPreviewConfirmationDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.JConfirmationDialog;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.SettingsDialog;
import net.sf.refactorit.ui.resolutiondialog.ChooseResolutionDialog;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import javax.swing.JOptionPane;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Impl of DialogManager interface.<br>
 *
 * @author Vlad Vislogubov
 * @author Anton Safonov
 */
public class RealDialogManager extends DialogManager {
  public RealDialogManager() {
  }

  public BinTreeTableModel showSettings(String title,
      RefactorItContext context, BinTreeTableModel model, String description,
      String helpTopicId) {
    JConfirmationDialog cd = new SettingsDialog(title,
        "The following items will be affected by this refactoring: ",
        model,
        context, description, helpTopicId);
    cd.show();

    if (cd.isOkPressed()) {
      return cd.getModel();
    }

    return null;
  }

  /*
   * @see DialogManager#showConfirmations(IdeWindowContext, Component, BinTreeTableModel)
   */
  public BinTreeTableModel showConfirmations(
      RefactorItContext context, BinTreeTableModel model,
      String helpTopicId
  ) {
    return showConfirmations(context, model, null, helpTopicId);
  }


  public BinTreeTableModel showConfirmations(
      RefactorItContext context, BinTreeTableModel model,
      String description, String helpTopicId
      ) {
    return showConfirmations("Refactoring", context, model, description,
        helpTopicId);
  }

  public BinTreeTableModel showConfirmations(String title,
      RefactorItContext context, BinTreeTableModel model,
      String description, String helpTopicId
  ) {
    JConfirmationDialog cd = new JConfirmationDialog(title,
        "The following changes are necessary to perform the refactoring",
        model,
        context, description, helpTopicId);
    cd.show();

    if (cd.isOkPressed()) {
      return cd.getModel();
    }

    return null;
  }

  public boolean showConfirmations(String caption,
      String helpMessage, ChangesPreviewModel changesPreviewModel,
      RefactorItContext context, String descriptionStr,
      String helpId) {

    //Change preview dialog, method used only in EditorManager
    JConfirmationDialog cd = new ChangesPreviewConfirmationDialog(
        caption,
        helpMessage,
        changesPreviewModel,
        context,
        descriptionStr,
        helpId);
    cd.show();

    return cd.isOkPressed();
  }

  /*
   * @see DialogManager#showInformation(Component, String)
   */
  public void showInformation(IdeWindowContext context, String key) {
    new JWarningDialog(context, key,
        JWarningDialog.INFORMATION_MESSAGE).display();
  }

  public void showInformation(
      IdeWindowContext context, String key, String message
  ) {
    new JWarningDialog(context, key, message,
        JWarningDialog.INFORMATION_MESSAGE, "").display();
  }

  public void showErrorWithHelp(
      IdeWindowContext context, String key, String message, String helpButtonKey
  ) {
    new JWarningDialog(context, key, message,
        JWarningDialog.ERROR_MESSAGE, helpButtonKey).displayAlways();
  }

  /*
   * @see DialogManager#showWarning(Component, String)
   */
  public void showWarning(IdeWindowContext context, String key) {
    new JWarningDialog(context, key, JWarningDialog.WARNING_MESSAGE).display();
  }

  public void showWarning(IdeWindowContext context, String key, String message) {
    new JWarningDialog(context, key, message,
        JWarningDialog.WARNING_MESSAGE, "").display();
  }

  /*
   * @see DialogManager#showError(Component, String)
   */
  public void showError(IdeWindowContext context, String key) {
    new JWarningDialog(context, key, JWarningDialog.ERROR_MESSAGE).display();
  }

  /*
   * @see DialogManager#showYesNoCancelQuestion(Component, String)
   */
  public int showYesNoCancelQuestion(IdeWindowContext context, String key) {
    return new JWarningDialog(context, key,
        JWarningDialog.QUESTION_MESSAGE).display();
  }

  public int showYesNoCancelQuestion(
      IdeWindowContext context, String key, String message,
      int defaultSelectedButton
  ) {
    JWarningDialog d = new JWarningDialog(context, key, message,
        JWarningDialog.QUESTION_MESSAGE, "");

    if (defaultSelectedButton != YES_BUTTON) {
      d.setDefaultButton(false);
    }

    return d.display();
  }

  /*
   * @see DialogManager#showYesNoQuestion(Component, String)
   */
  public int showYesNoQuestion(IdeWindowContext context, String key) {
    JWarningDialog d = new JWarningDialog(context, key,
        JWarningDialog.QUESTION_MESSAGE);
    d.setCancelEnabled(false);

    return d.display();
  }

  public int showYesNoQuestion(
      IdeWindowContext context, String key, String message,
      int defaultSelectedButton
  ) {
    JWarningDialog d = new JWarningDialog(context, key, message,
        JWarningDialog.QUESTION_MESSAGE, "");

    if (defaultSelectedButton != YES_BUTTON) {
      d.setDefaultButton(false);
    }

    d.setCancelEnabled(false);

    return d.display();
  }

  public int showYesNoHelpQuestion(
      IdeWindowContext context, String text, String helpButtonKey
  ) {
    return new JWarningDialog(context, text, helpButtonKey).displayAlways();
  }

  /**
   * Shows modal Warning dialog with Yes/No buttons
   *
   * @param context
   * @param title title of the dialog
   * @param text text of the dialog
   * @return constant whether Yes or No was pressed
   */
  public int showCustomYesNoQuestion(
      IdeWindowContext context, String title, String text
  ) {
    final int result = RitDialog.showConfirmDialog(
        context, cutMessage(text), title,
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.YES_OPTION) {
      return YES_BUTTON;
    }

    return NO_BUTTON;
  }

  public int showCustomYesNoQuestion(
      IdeWindowContext context, String title, String text, int defaultSelection
  ) {
    return showCustomYesNoQuestion(context, title, text);
  }

  /*
   * @see DialogManager#showError(Component, String, Throwable)
   */
  public void showError(IdeWindowContext context, String title, Throwable t) {
    JErrorDialog err = new JErrorDialog(context, title);
    err.setException(t);
    err.show();
  }

  /**
   * Shows modal Error dialog
   *
   * @param context
   * @param title title of the dialog
   * @param text text of the dialog
   */
  public void showCustomError(
      IdeWindowContext context, String title, String text
  ) {
//    final String error = cutMessage(errorString);
    JErrorDialog ed = new JErrorDialog(context, title);
    ed.setText(cutMessage(text));
    ed.show();
//    RitDialog.showMessageDialog(
//        context, cutMessage(text), title, JOptionPane.ERROR_MESSAGE);
  }

  //
  // Specific dialogs
  //

  public void showCriticalError(IdeWindowContext context, Project project) {
    final String msg =
        "The project has critical errors and can not be used by RefactorIT!\n"
        +
        "Choose help to read more or check the errors list in the Errors tab.";

    showErrorWithHelp(context, "RefactorItDialogManager.has_critical_errors",
        msg, "faq.errors");
  }

  /** Might *not* show the warning if disabled from RefactorIT options */
  public void showJavaVersionWarning(IdeWindowContext context) {
    String message =
        "You should check the Java Version Supported from the Project Options.\n"
        + "That could remove some of the current parsing errors.";

    new JWarningDialog(context, "jvm.mode.warning", message,
        JWarningDialog.INFORMATION_MESSAGE, "").display();
  }

  public void showNonSourcePathItemInfo(
      IdeWindowContext context, String actionName, Object triedToRunUpon
  ) {
    final String key = "info.not.sourcepath.item";
    final ResourceBundle MESSAGES_BUNDLE
        = ResourceUtil.getBundle(JWarningDialog.class, "Warnings");

    String message = MESSAGES_BUNDLE.getString(key);

    message = StringUtil.replace(message, "<Action>", actionName)
        + " " + BinFormatter.format(triedToRunUpon);

    new JWarningDialog(context, key, message,
        JWarningDialog.INFORMATION_MESSAGE, "").display();
  }

  public void showCustomError(
      final IdeWindowContext context, String errorString
  ) {
//    final String error = cutMessage(errorString);
    JErrorDialog ed = new JErrorDialog(context, "Error");
    ed.setText(cutMessage(errorString));
    ed.show();
//    try {
//      SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
//        public void run() {
//          RitDialog.showMessageDialog(
//              context, error, "Error", JOptionPane.ERROR_MESSAGE);
//        }
//      });
//    } catch (Exception e) {
//      e.printStackTrace(System.err);
//    }
  }

  public static String cutMessage(String message) {
    if (message == null) {
      return message;
    }

    LineNumberReader reader = new LineNumberReader(new StringReader(message));
    StringBuffer buf = new StringBuffer();

    String line;
    int count = 0;
    try {
      while ((line = reader.readLine()) != null && count++ < LINE_COUNT_LIMIT) {
        buf.append(line);
        buf.append("\n");
      }
    } catch (IOException ignore) {}

    return buf.toString();
  }

  public ConflictResolution getResultFromResolutionDialog(List resolutions) {
    ChooseResolutionDialog dialog = new ChooseResolutionDialog(resolutions);
    dialog.show();

    return dialog.getChosenResolution();
  }

  public int getResultFromAuxiliaryDialog(String title, String[] strings) {
    AuxiliaryDialog dialog = new AuxiliaryDialog(
        IDEController.getInstance().createProjectContext(), title, strings);

    dialog.show();

    return dialog.getResult();
  }

  public int getResultFromQuestionDialog(String title, String message) {
    return RitDialog.showConfirmDialog(
        IDEController.getInstance().createProjectContext(),
        message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
  }

  public static String getDisplayablePackageListWithSizes(List list) {
    String result = "<ul>";

    for (int i = 0; i < list.size(); i++) {
      BinPackage p = (BinPackage) list.get(i);
      result += "<li>" + StringUtil.tagsIntoHTML(p.getQualifiedDisplayName())
          + ": " +
          StringUtil.countToString(p.getCompilationUnitList().size(), "source");
    }

    result += "</ul>";
    return result;
  }

  public int showYesNoHelpQuestion(
      IdeWindowContext context, String key, String text, String helpButtonKey
  ) {
    final JWarningDialog dialog = new JWarningDialog(
        context, key, text, JWarningDialog.QUESTION_MESSAGE, helpButtonKey);
    dialog.setCancelEnabled(false);
    return dialog.display();
  }

}
