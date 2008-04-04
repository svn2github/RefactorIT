/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.help;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.InitializationException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * Main RefactorIT help display class, for all platforms.
 *
 * @author  jaanek
 * @author  Risto
 */
public class HelpViewer {
  private static TopicDisplayer instance;

  public static void attachHelpToDialog(
      RitDialog dialog, AbstractButton helpButton, String topicId
  ) {
    IdeWindowContext context = dialog.getContext();

    addHelpShowActionListener(context, helpButton, topicId);

    final String key = "helpAction";
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);

    JRootPane root = dialog.getRootPane();
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, key);
    root.getActionMap().put(key, getHelpShowAction(context, topicId));
  }

  public static void addHelpShowActionListener(
      IdeWindowContext context, AbstractButton button, String topicId
  ) {
    button.addActionListener(getHelpShowAction(context, topicId));
  }

  private static Action getHelpShowAction(
      final IdeWindowContext context, final String topicId
  ) {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        displayTopic(context, topicId);
      }
    };
  }

  public static void displayDefaultTopic() {
    displayTopic(IDEController.getInstance().createProjectContext(), ".top");
  }

  public static void displayTopic(IdeWindowContext context, String topicId) {
    try {
      getTopicDisplayer().displayTopic(context, topicId);
    } catch (InitializationException e) {
      AppRegistry.getExceptionLogger().error(e, HelpViewer.class);
      DialogManager.getInstance().showCustomError(
          IDEController.getInstance().createProjectContext(),
          "RefactorIT help viewer failed to initialize");
      return;
    }
  }

  public static void setTopicDisplayer(TopicDisplayer h) {
    instance = h;
  }

  private static TopicDisplayer getTopicDisplayer()
  throws InitializationException {
    if (instance == null) {
      // Must init lazily -- JavaHelpTopicDisplayer conflicts
      // with NB's JavaHelp instance (tested under NB 3.4.1)
      setTopicDisplayer(new JavaHelpTopicDisplayer());
    }

    return instance;
  }
}
