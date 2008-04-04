/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Shows modal dialog with 'Memory info'
 *
 * @author Jaanek Oja
 */
public class JMemoryInfo {
  private static final Icon warningIcon =
      ResourceUtil.getIcon(UIResources.class, "Warn.gif");

  final RitDialog dialog;

  public JCheckBox dontShowAgain =
      new JCheckBox("Do not show this message again.");

  JButton buttonOk = new JButton("Ok");

  private String message;

  public JMemoryInfo(IdeWindowContext context, String message) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Not Enough Memory!");

    init(message);
  }

//  private static Frame getActivationFrame() {
//    Frame f = new Frame();
//    f.setVisible(false);
//    return f;
//  }

  private void init(String message) {
    this.message = message;
    dialog.setSize(480, 350);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            buttonOk.requestFocus();
          }
        });
      }
    });

    dialog.setContentPane(createMainPanel());

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk);
  }

  private JPanel createMainPanel() {
    JLabel icon = new JLabel(warningIcon);
    icon.setHorizontalAlignment(JLabel.LEFT);

    String text = this.message;
    JEditorPane textPane = new JEditorPane();
    textPane.setBackground(icon.getBackground());
    textPane.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
    textPane.setText(text);
    textPane.setEditable(false);

    textPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
          HelpViewer.displayTopic(dialog.getContext(), "faq.memory");
        }
      }
    });

    JPanel mainPanel = new JPanel(new GridBagLayout());
    JPanel center = new JPanel(new GridBagLayout());
    center.setBorder(BorderFactory.createLoweredBevelBorder());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 1;
    icon.setVerticalAlignment(JLabel.CENTER);
    center.add(icon, constraints);

    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.fill = GridBagConstraints.BOTH; //HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridx = 2;
    constraints.gridy = 1;
    center.add(new JScrollPane(textPane), constraints);

    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.gridwidth = 2;
    mainPanel.add(center, constraints);

    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = new Insets(5, 5, 4, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridx = 2;
    constraints.gridy = 2;
    mainPanel.add(createCheckBoxPanel(), constraints);

    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = new Insets(5, 5, 4, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridx = 2;
    constraints.gridy = 3;
    mainPanel.add(createOkButtonPanel(), constraints);

    buttonOk.setDefaultCapable(true);
    dialog.getRootPane().setDefaultButton(buttonOk);

    return mainPanel;
  }

  private JPanel createOkButtonPanel() {
    JPanel buttonPanel = new JPanel(new GridBagLayout());

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });

    buttonOk.setMinimumSize(buttonOk.getPreferredSize());
    // FIXME: is it a good way?
    buttonOk.setPreferredSize(new Dimension(
        70, (int) buttonOk.getPreferredSize().getHeight()));

    buttonPanel.add(buttonOk);

    return buttonPanel;
  }

  private JPanel createCheckBoxPanel() {
    JPanel panel = new JPanel(new GridBagLayout());

    dontShowAgain.setMinimumSize(dontShowAgain.getPreferredSize());
    panel.add(dontShowAgain);

    return panel;
  }

  public void show() {
    dialog.show();
  }
}
