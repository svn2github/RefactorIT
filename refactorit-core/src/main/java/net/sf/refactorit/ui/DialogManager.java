/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;


/**
 * User interaction layer for refactoring algorithms requiring some
 * confirmations from the user or providing warning/error messages.
 *
 * @author Vlad Vislogubov
 * @author Anton Safonov
 * @author Igor Malinin
 */
public abstract class DialogManager {
  public static final int LINE_COUNT_LIMIT = 40;

  public static final int YES_BUTTON = 0;
  public static final int NO_BUTTON = 1;
  public static final int CANCEL_BUTTON = 2;

  //public static Color HELP_PANEL_COLOR = new Color( 253, 120, 19 );
  //public static Color HELP_PANEL_COLOR = new Color( 254, 227, 206 );
  public static Color DEFAULT_HELP_PANEL_COLOR = new Color(255, 245, 231);

  private static DialogManager instance = new RealDialogManager();

  public static DialogManager getInstance() {
    return instance;
  }

  public static JPanel getHelpPanel(String text) {
    return getHelpPanel(text, DEFAULT_HELP_PANEL_COLOR);
  }

  /**
   * Actually, we need some style object for the customization purpose
   */
  public static JPanel getHelpPanel(String text, Color panelColor) {
    JPanel panel = new JPanel(new GridBagLayout());
    //panel.setBackground( Color.lightGray );
    panel.setBackground(panelColor);
    panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, Color.white));

    JTextArea area = new JTextArea();
    if (text == null || text.length() == 0) {
      (new Throwable()).printStackTrace();
      text = "ERROR: HELP MESSAGE IS NOT FOUND\nPlease send error stack trace to support@refactorit.com";
    }
    area.setEnabled(false);
    area.setText(text);
    area.setForeground(Color.black);
    area.setDisabledTextColor(Color.black);
    area.setBackground(panelColor);
    JLabel temp = new JLabel("temp"); // don't delete this hack
    area.setFont(temp.getFont());
    /*
         area.setFont( new Font(temp.getFont().getName(),
        Font.BOLD, temp.getFont().getSize() ) );
     */
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setEditable(false);
    area.transferFocus();

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(10, 5, 10, 0);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    panel.add(new JLabel(
        getRefactoritIconSmall()),
        constraints);

    constraints.insets = new Insets(10, 5, 10, 5);
    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    panel.add(area, constraints);

    return panel;
  }

  /**
   * <b>For tests only! Don't use it!!!</b><br>
   * Don't forget to restore it back to default manager (NullDialogManager)
   * on <code>tearDown</code> of a testSuite.
   *
   * @param instance
   */
  public static void setInstance(DialogManager instance) {
    DialogManager.instance = instance;
  }

  /**
   * Finds top level Dialog or Frame in component hierarhy.
   *
   * @param component any component in required Dialog or Frame
   * @return <code>null</code> when component is not contained in any Dialog or
   * Frame
   */
  public static Window findOwnerWindow(Component component) {
    while (component != null) {
      if (component instanceof Window) {
        for (Window window = (Window) component; window != null;
            window.getOwner()) {
          if (window instanceof Dialog || window instanceof Frame) {
            return window;
          }
        }
      }

      component = component.getParent();
    }

    return null;
  }

  public abstract BinTreeTableModel showSettings(String title,
      RefactorItContext context,
      BinTreeTableModel model,
      String description, String helpTopicId);

  /**
   * Shows modal dialog with CHECKBOX_STYLE TreeTable.
   *
   * @param context the originating context containing display details etc
   *
   * @return BinTreeTableModel with all nodes.
   */
  public abstract BinTreeTableModel showConfirmations(
      RefactorItContext context,
      BinTreeTableModel model, String helpTopicId);

  /**
   * Shows modal dialog with CHECKBOX_STYLE TreeTable.
   *
   * @param context the originating context containing display details etc
   *
   * @return BinTreeTableModel with all nodes.
   */
  public abstract BinTreeTableModel showConfirmations(
      RefactorItContext context,
      BinTreeTableModel model, String desription, String helpTopicId);

  public abstract BinTreeTableModel showConfirmations(String title,
      RefactorItContext context, BinTreeTableModel model,
      String description, String helpTopicId
  );

  public abstract boolean showConfirmations(String caption,
      String helpMessage, ChangesPreviewModel changesPreviewModel,
      RefactorItContext context, String descriptionStr,
      String helpId);

  /**
   * Shows modal Information dialog with showNextTime checkbox
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   */
  public abstract void showInformation(
      IdeWindowContext context, String key);

  /**
   * Shows modal Information dialog with showNextTime checkbox
   * and possibility to define Message area text
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @param message  Any string for the Message Area
   */
  public abstract void showInformation(
      IdeWindowContext context, String key, String message);

  /**
   * Shows modal Warning dialog with showNextTime checkbox
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   */
  public abstract void showWarning(IdeWindowContext context, String key);

  /**
   * Shows modal Warning dialog with showNextTime checkbox
   * and possibility to define Message area text
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @param message  Any string for the Message Area
   */
  public abstract void showWarning(
      IdeWindowContext context, String key, String message);

  /**
   * Shows modal Warning dialog with showNextTime checkbox and Yes/No/Cancel buttons
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @return constant whether Yes or No was pressed
   */
  public abstract int showYesNoCancelQuestion(
      IdeWindowContext context, String key);

  /**
   * Shows modal Warning dialog with showNextTime checkbox and Yes/No/Cancel buttons
   * with possibility to define Message area text
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @param message  Any string for the Message Area
   * @return constant whether Yes or No was pressed
   */
  public abstract int showYesNoCancelQuestion(
      IdeWindowContext context, String key,
      String message, int defaultSelectedButton);

  /**
   * Shows modal Warning dialog with showNextTime checkbox and Yes/No buttons
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @return constant whether Yes or No was pressed
   */
  public abstract int showYesNoQuestion(IdeWindowContext context, String key);

  /**
   * Shows modal Warning dialog with showNextTime checkbox and Yes/No buttons
   * with possibility to define Message area text
   *
   * @param key Key for the finding message text from
   *            net/sf/refactorit/ui/resources/Warnings.properties
   * @param context the originating context containing display details etc
   * @param message  Any string for the Message Area
   * @return constant whether Yes or No was pressed
   */
  public abstract int showYesNoQuestion(
      IdeWindowContext context, String key, String message,
      int defaultSelectedButton);

  public abstract int showYesNoHelpQuestion(
      IdeWindowContext context, String key, String text, String helpButtonKey);

  public abstract int showYesNoHelpQuestion(
      IdeWindowContext context, String text, String helpButtonKey);

  /**
   * Shows modal Warning dialog with Yes/No buttons
   *
   * @param context the originating context containing display details etc
   * @param title title of the dialog
   * @param text text of the dialog
   * @return constant whether Yes or No was pressed
   */
  public abstract int showCustomYesNoQuestion(
      IdeWindowContext context, String title, String text,
      int defaultSelection);

  /**
   * Shows modal Warning dialog with Yes/No buttons
   *
   * @param context the originating context containing display details etc
   * @param title title of the dialog
   * @param text text of the dialog
   * @return constant whether Yes or No was pressed
   */
  public abstract int showCustomYesNoQuestion(
      IdeWindowContext context, String title, String text);

  /**
   * Shows modal Error dialog <em>without</em> showNextTime flag
   *
   * @param context the originating context containing display details etc
   * @param key in the Warnings.resource file
   */
  public abstract void showError(IdeWindowContext context, String key);

  /**
   * Shows modal Error dialog
   *
   * @param context the originating context containing display details etc
   * @param title title of the dialog
   * @param text text of the dialog
   */
  public abstract void showCustomError(
      IdeWindowContext context, String title, String text);

  /**
   * Shows modal Error dialog <em>without</em> showNextTime flag
   *
   * @param context the originating context containing display details etc
   * @param title
   * @param t
   */
  public abstract void showError(
      IdeWindowContext context, String title, Throwable t);

  //
  // Specific dialogs
  //

  public abstract void showCriticalError(
      IdeWindowContext context, Project project);

  public abstract void showJavaVersionWarning(IdeWindowContext context);

  public abstract void showNonSourcePathItemInfo(
      IdeWindowContext context, String actionName, Object triedToRunUpon);

  public abstract void showCustomError(
      IdeWindowContext context, String errorString);

  public abstract int getResultFromAuxiliaryDialog(
      String title, String[] strings);

  public abstract int getResultFromQuestionDialog(String title, String message);

  public abstract ConflictResolution getResultFromResolutionDialog(
      List resolutions);


  public static ImageIcon getRefactoritIconSmall() {
    return ResourceUtil.getIcon(DialogManager.class, "RefactorIt.gif");
  }
}
