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
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ResourceBundle;



/**
 * Shows exception trace
 */
public class JErrorDialog {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JErrorDialog.class);

  private static final int WIDTH = 570;
  private static final int HEIGHT_WITH_DETAILS = 300;
  private static final int HEIGHT_WITHOUT_DETAILS = 125;

  private static Icon errorIcon
      = ResourceUtil.getIcon(JErrorDialog.class, "Error.gif");

  final RitDialog dialog;

  JTextArea text;

  JPanel messagePanel;
  JScrollPane details;
  Throwable t;

  private JLabel message;

  private JToggleButton buttonDetail;
  private JButton buttonClose;

  public JErrorDialog(IdeWindowContext context, String title) {
    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setSize(WIDTH, 150);

    message = new JLabel(errorIcon);

    Font font = message.getFont();
    message.setFont(font.deriveFont(Font.BOLD));
    message.setHorizontalAlignment(JLabel.LEFT);

    text = new JTextArea();

    text.setLineWrap(true);
    text.setWrapStyleWord(true);

    //text.setBackground( getBackground() );
    text.setEditable(false);
    text.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showTextPopupMenu(e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showTextPopupMenu(e.getX(), e.getY());
        }
      }

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showTextPopupMenu(e.getX(), e.getY());
        }
      }
    });

    dialog.setContentPane(createMainPanel());
  }

  void showTextPopupMenu(int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    JMenuItem menuItem = new JMenuItem("Copy To Clipboard");
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SystemClipboard.setContents(text.getText());
      }
    });

    menu.add(menuItem);

    menu.show(text, x, y);
  }

  public void setException(Throwable t) {
    this.t = t;
    CharArrayWriter cw = new CharArrayWriter();
    PrintWriter pw = new PrintWriter(cw);
    t.printStackTrace(pw);
    pw.flush();

    text.setText(new String(cw.toCharArray()));
    String msg = t.toString(); //getMessage();
    if (msg == null || msg.length() <= 0) {
      msg = "Error";
    }
    message.setText(msg);
  }

  public void setText(String message) {
    text.setText(message);
    buttonDetail.setEnabled(false);
    showDetails(true);
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new BorderLayout());

    JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
    JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));

    buttonDetail = new JToggleButton(resLocalizedStrings.
        getString("button.detail"));
    buttonDetail.setMnemonic(KeyEvent.VK_D);

    buttonDetail.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDetails(buttonDetail.isSelected());
      }
    });
    westPanel.add(buttonDetail);

    buttonClose = new JButton(resLocalizedStrings.getString("button.close"));
    buttonClose.setMnemonic(KeyEvent.VK_C);
    buttonClose.setDefaultCapable(true);
    dialog.getRootPane().setDefaultButton(buttonClose);

    ActionListener closeActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };
    SwingUtil.addEscapeListener(dialog, closeActionListener);
    buttonClose.addActionListener(closeActionListener);
    eastPanel.add(buttonClose, BorderLayout.EAST);

    buttonPanel.add(westPanel,BorderLayout.WEST);
    buttonPanel.add(eastPanel,BorderLayout.EAST);

    buttonClose.setNextFocusableComponent(buttonDetail);
    buttonDetail.setNextFocusableComponent(buttonClose);
    text.setNextFocusableComponent(buttonClose);

    return buttonPanel;
  }

  private String getExceptionInfo() {
    return (t!=null)?text.getText():"Error";
  }

  void showDetails(boolean enabled) {
    dialog.setSize(WIDTH, enabled ? HEIGHT_WITH_DETAILS : HEIGHT_WITHOUT_DETAILS);
    messagePanel.setVisible(!enabled);
    details.setVisible(enabled);
    SwingUtilities.getRoot(dialog.getRootPane()).validate();
  }

  private JPanel createMessagePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEtchedBorder());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    panel.add(message, constraints);
    return panel;
  }

  private JPanel createMainPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(5, 5, 0, 5);
    messagePanel = createMessagePanel();
    mainPanel.add(messagePanel, constraints);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 2;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weighty = 0.0;
    mainPanel.add(createButtonPanel(), constraints);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weighty = 1.0;
    constraints.weightx = 1.0;
    details = new JScrollPane(text);
    details.setNextFocusableComponent(buttonClose);
    details.setVisible(false);
    constraints.insets = new Insets(5, 5, 0, 5);
    mainPanel.add(details, constraints);
    return mainPanel;
  }

  public void show() {
    dialog.show();
  }
}
