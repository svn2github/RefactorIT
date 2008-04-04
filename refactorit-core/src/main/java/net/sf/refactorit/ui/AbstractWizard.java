/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;


/**
 *
 *
 * @author Igor Malinin
 */
public class AbstractWizard {
  protected final RitDialog dialog;

  protected final CardLayout layout = new CardLayout();
  protected final JPanel cards = new JPanel(layout);
  {
    cards.setBorder(BorderFactory.createEtchedBorder());
  }

  protected final JButton backButton = new JButton(
      new AbstractAction("Back") {
        public void actionPerformed(ActionEvent e) {
          onBack();
        }
      });

  protected final JButton nextButton = new JButton(
      new AbstractAction("Next") {
        public void actionPerformed(ActionEvent e) {
          onNext();
        }
      });

  protected final JButton finishButton = new JButton(
      new AbstractAction("Finish") {
        public void actionPerformed(ActionEvent e) {
          onFinish();
        }
      });

  protected final JButton cancelButton = new JButton(
      new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
          onCancel();
        }
      });
  protected boolean cancelled = false;

  protected final JButton helpButton;

  public AbstractWizard(IdeWindowContext context, String title) {
    this(context, title, null);
  }

  public AbstractWizard(IdeWindowContext context, String title, String helpTopicId) {
    dialog = RitDialog.create(context);
    dialog.setTitle(title);

    if (helpTopicId != null) {
      helpButton = new JButton("Help");
      HelpViewer.attachHelpToDialog(dialog, helpButton, helpTopicId);
    } else {
      helpButton = null;
    }

    Container pane = dialog.getContentPane();
    pane.add(cards);
    pane.add(createButtonPanel(), BorderLayout.SOUTH);
  }

  protected JPanel createButtonPanel() {
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 1;

    gbc.gridx = 1;
    gbc.insets = new Insets(5, 5, 5, 0);

    panel.add(backButton, gbc);

    gbc.gridx = 2;
    gbc.insets = new Insets(5, 0, 5, 5);

    panel.add(nextButton, gbc);

    gbc.gridx = 3;
    gbc.insets = new Insets(5, 5, 5, 5);

    panel.add(finishButton, gbc);

    gbc.gridx = 4;
    gbc.insets = new Insets(5, 5, 5, 5);

    panel.add(cancelButton, gbc);

    if (helpButton != null) {
      gbc.gridx = 5;
      gbc.insets = new Insets(5, 5, 5, 5);

      panel.add(helpButton, gbc);
    }

    return panel;
  }

  protected final Component getVisibleCard() {
    int size = cards.getComponentCount();
    for (int i = 0 ; i < size; i++) {
      Component comp = cards.getComponent(i);
      if (comp.isVisible()) {
        return comp;
      }
    }

    return null;
  }

  public void setEnabledButtons() {
    boolean focus = false;

    boolean back = checkBackEnabled();
    if (!back && backButton.isFocusOwner()) {
      focus = true;
    }

    backButton.setEnabled(back);

    boolean next = checkNextEnabled();
    if (!next && nextButton.isFocusOwner()) {
      focus = true;
    }

    nextButton.setEnabled(next);

    boolean finish = checkFinishEnabled();
    if (!finish && finishButton.isFocusOwner()) {
      focus = true;
    }

    finishButton.setEnabled(finish);

    boolean cancel = checkCancelEnabled();
    if (!cancel && cancelButton.isFocusOwner()) {
      focus = true;
    }

    cancelButton.setEnabled(cancel);

    JRootPane root = SwingUtilities.getRootPane(cards);

    JButton button = null;

    if (next) {
      button = nextButton;
    } else if (finish) {
      button = finishButton;
    } else if (back) {
      button = backButton;
    } else if (cancel){
      button = cancelButton;
    }

    root.setDefaultButton(button);

    if (focus) {
      if (button != null) {
        button.requestFocus();
      } else {
        resetFocus();
      }
    }
  }

  public void resetFocus() {
    Container ancestor = getVisibleCard().getFocusCycleRootAncestor();
    FocusTraversalPolicy policy = ancestor.getFocusTraversalPolicy();
    Component component = policy.getDefaultComponent(ancestor);
    if (component != null) {
      component.requestFocus();
    } else {
      ancestor.requestFocus();
    }
  }

  protected boolean checkBackEnabled() {
    int size = cards.getComponentCount();
    if (size <= 1) {
      return false;
    }

    return !cards.getComponent(0).isVisible();
  }

  protected boolean checkNextEnabled() {
    int size = cards.getComponentCount();

    if (size <= 1) {
      return false;
    }

    Component lastComp = cards.getComponent(size-1);
    return !lastComp.isVisible();
  }

  protected boolean checkFinishEnabled() {
    return true;
  }

  protected boolean checkCancelEnabled() {
    return true;
  }

  protected void onBack() {
    layout.previous(cards);

    setEnabledButtons();
    resetFocus();
  }

  protected void onNext() {
    layout.next(cards);

    setEnabledButtons();
    resetFocus();
  }

  protected void onFinish() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  protected void onCancel() {
    if (dialog != null) {
      dialog.dispose();
    }
    cancelled = true;
  }

  public boolean isOk() {
    return !cancelled;
  }

  /**
   * Test look
   */
  public AbstractWizard(JFrame frame) {
    dialog = null;
    helpButton = null;

    Container pane = frame.getContentPane();
    pane.add(cards);
    pane.add(createButtonPanel(), BorderLayout.SOUTH);
  }

  /**
   * Test look
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    new AbstractWizard(frame);

    frame.setSize(400, 400);
    frame.show();
  }
}
