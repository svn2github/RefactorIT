/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.errors;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Shows warning dialog with ShowNextTime checkbox.
 * Save checkbox state and return result in Main.propreties.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class JWarningDialog {
  //cannot use JWarningDialog.class, as properties files are in sibling folder
  public static final ResourceBundle MESSAGES_BUNDLE =
      ResourceUtil.getBundle(JProgressDialog.class, "Warnings");

  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JProgressDialog.class);

  public static final int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;
  public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
  public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
  public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;

  public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;

  private static final Icon warningIcon
      = ResourceUtil.getIcon(JProgressDialog.class, "Warn.gif");
  private static final Icon questionIcon
      = ResourceUtil.getIcon(JProgressDialog.class, "Question.gif");
  private static final Icon errorIcon
      = ResourceUtil.getIcon(JProgressDialog.class, "Error.gif");
  private static final Icon informationIcon
      = ResourceUtil.getIcon(JProgressDialog.class, "Inform.gif");

  final RitDialog dialog;

  JButton buttonOk = new JButton(resLocalizedStrings.getString("button.ok"));
  JButton buttonYes = new JButton(resLocalizedStrings.getString("button.yes"));
  JButton buttonNo = new JButton(resLocalizedStrings.getString("button.no"));
  JButton buttonCancel = new JButton(resLocalizedStrings.getString
      ("button.cancel"));
  JButton buttonHelp = new JButton(resLocalizedStrings.getString
      ("button.help"));

  JPanel buttonPanel;

  int result = DialogManager.CANCEL_BUTTON;
  boolean isYesButton = true;

  private String key;

  private JLabel icon;
  JTextArea message;
  JCheckBox box = new JCheckBox(resLocalizedStrings.getString(
      "checkbox.show.next"), true);

  private int messageType = INFORMATION_MESSAGE;
  private boolean isShowCheckBox = true;
  private String helpButtonKey;

  private boolean attachHelp = false;

  private static String getTitle(int messageType) {
    String title;
    switch (messageType) {
      case INFORMATION_MESSAGE:
        title = "Information";
        break;
      case QUESTION_MESSAGE:
        title = "Question";
        break;
      case ERROR_MESSAGE:
        title = "Error";
        break;
      case WARNING_MESSAGE:
        title = "Warning";
        break;
      default:
        title = "Information";
    }
    return title;
  }

  public JWarningDialog(IdeWindowContext context, String key, int messageType) {
    this(context, key, null, messageType, "");
  }

  public JWarningDialog(
      IdeWindowContext context, String text, String helpButtonKey
  ) {
    this.helpButtonKey = helpButtonKey;
    this.isShowCheckBox = false;

    icon = new JLabel(warningIcon);

    Font font = icon.getFont();
    icon.setFont(font.deriveFont(Font.BOLD));
    icon.setHorizontalAlignment(JLabel.LEFT);
    message = new JTextArea();
    message.setBackground(icon.getBackground());
    message.setEditable(false);
    message.setLineWrap(true);
    message.setWrapStyleWord(true);
    message.setText(text);

    messageType = QUESTION_MESSAGE;

    JPanel contentPane = createMainPanel();
    contentPane.setPreferredSize(new Dimension(495, 180));

    dialog = RitDialog.create(context);
    dialog.setTitle(getTitle(messageType));
    dialog.setContentPane(contentPane);

    if (attachHelp) {
      HelpViewer.attachHelpToDialog(dialog, buttonHelp, helpButtonKey);
    }

    focus();

    final String keyName = "closeActionOfWarningDialog";
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final Action act = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        result = DialogManager.NO_BUTTON;
        dialog.dispose();
      }
    };

    dialog.getRootPane().getInputMap().put(stroke, keyName);
    dialog.getRootPane().getActionMap().put(keyName, act);
  }

  public JWarningDialog(
      IdeWindowContext context, String key, String text,
      final int messageType, String helpKey
  ) {
    this.key = key;
    this.messageType = messageType;
    if (helpKey != null && helpKey.length() > 0) {
      attachHelp = true;
      helpButtonKey = helpKey;
    }

    icon = getMessageIcon(this.messageType);
    Font font = icon.getFont();
    icon.setFont(font.deriveFont(Font.BOLD));
    icon.setHorizontalAlignment(JLabel.LEFT);
    message = new JTextArea();
    message.setBackground(icon.getBackground());
    message.setEditable(false);
    message.setLineWrap(true);
    message.setWrapStyleWord(true);
    try {
      if (text == null) {
        String m = MESSAGES_BUNDLE.getString(key);

        message.setText(m);
      } else {
        message.setText(text);
      }
    } catch (MissingResourceException e) {
      message.setText("Internal Program Error: Cant find message for the '"
          + key + "' key ");
    }

    if (this.messageType == QUESTION_MESSAGE) {
      int defaultValue = getLastTimeValue(key);
      if(defaultValue != DialogManager.CANCEL_BUTTON) {
        result = defaultValue;
      }
    }

    JPanel contentPane = createMainPanel();

    if (key != null && key.length() > 0) {
      // FIXME: here is quick hack
      if ("info.beta.license".equals(key)) {
        contentPane.setPreferredSize(new Dimension(495, 430));
      } else if ("license.import.success".equals(key)) {
        contentPane.setPreferredSize(new Dimension(495, 300));
      } else if ("license.agreement".equals(key)) {
        contentPane.setPreferredSize(new Dimension(620, 430));
      } else {
        contentPane.setPreferredSize(new Dimension(495, 180));
      }
    }

    dialog = RitDialog.create(context);
    dialog.setTitle(getTitle(messageType));
    dialog.setContentPane(contentPane);

    if (attachHelp) {
      this.helpButtonKey = helpKey;
      HelpViewer.attachHelpToDialog(dialog, buttonHelp, helpButtonKey);
    }

    focus();

    if (this.messageType == ERROR_MESSAGE
        || this.messageType == PLAIN_MESSAGE) {
      box.setSelected(true);
      box.setVisible(false);
    } else {
      if ( ! shouldAskAgain(key)) {
        box.setSelected(false);
        return;
      }
    }

    dialog.addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        JWarningDialog.this.message.scrollRectToVisible(new Rectangle(0, 0));
      }

      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (messageType == QUESTION_MESSAGE) {
              if (isYesButton) {
                buttonYes.requestFocus();
              } else {
                buttonNo.requestFocus();
              }
            } else {
              buttonOk.requestFocus();
            }
          }
        });
      }

      public void windowClosing(WindowEvent windowEvent) {
        dialog.removeWindowListener(this);
      }
    });

    final ActionListener escapeActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (buttonCancel.isVisible()) {
          result = DialogManager.CANCEL_BUTTON;
        } else {
          result = DialogManager.NO_BUTTON;
        }

        dialog.dispose();
      }
    };
    SwingUtil.addEscapeListener(dialog, escapeActionListener);
  }

  public static boolean shouldAskAgain(final String key) {
    String res = GlobalOptions.getOption(key);
    return res == null || ( ! res.equals("false"));
  }

  /**
   * @return  DialogManager.CANCEL_BUTTON by default
   */
  public static int getLastTimeValue(final String key) {
    String res = GlobalOptions.getOption(key + ".result");
    if (res != null && res.equals("" + DialogManager.YES_BUTTON)) {
      return DialogManager.YES_BUTTON;
    } else if (res != null && res.equals("" + DialogManager.NO_BUTTON)) {
      return DialogManager.NO_BUTTON;
    } else {
      return DialogManager.CANCEL_BUTTON;
    }
  }

  public static void saveLastTimeValue(final String settingKey, final int value, final boolean askAgain, final int typeOfMessage) {
    GlobalOptions.setOption(settingKey, "" + askAgain);
    if (typeOfMessage == QUESTION_MESSAGE) {
      GlobalOptions.setOption(settingKey + ".result", "" + value);
    }

    GlobalOptions.save();
  }

  public void setTitle(String title) {
    dialog.setTitle(title);
  }

  private static JLabel getMessageIcon(int messageType) {
    switch (messageType) {
      case INFORMATION_MESSAGE:
        return new JLabel(informationIcon);
      case QUESTION_MESSAGE:
        return new JLabel(questionIcon);
      case WARNING_MESSAGE:
        return new JLabel(warningIcon);
      case ERROR_MESSAGE:
        return new JLabel(errorIcon);
      case PLAIN_MESSAGE:
      default:
        return new JLabel();
    }
  }

  private JPanel createMainPanel() {
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
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridx = 2;
    constraints.gridy = 1;
    if (this.messageType == PLAIN_MESSAGE) {
      constraints.fill = GridBagConstraints.BOTH;
      center.add(new JScrollPane(message), constraints);
    } else {
      center.add(message, constraints);
    }

    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.gridwidth = 2;
    mainPanel.add(center, constraints);

    if (isShowCheckBox) {
      constraints.insets = new Insets(5, 5, 5, 5);
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.weightx = 1.0;
      constraints.weighty = 0.0;
      constraints.gridx = 1;
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      mainPanel.add(box, constraints);
    }

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridx = 2;
    constraints.gridy = 2;

    if (this.messageType == QUESTION_MESSAGE) {
      mainPanel.add(createYESNOButtonPanel(helpButtonKey), constraints);

      message.setNextFocusableComponent(buttonYes);
      box.setNextFocusableComponent(buttonYes);
      buttonCancel.setNextFocusableComponent(box);
      buttonYes.setDefaultCapable(true);
    } else {
      mainPanel.add(createOkButtonPanel(), constraints);

      message.setNextFocusableComponent(buttonOk);
      box.setNextFocusableComponent(buttonOk);
      buttonOk.setNextFocusableComponent(box);
      buttonOk.setDefaultCapable(true);
    }

    return mainPanel;
  }

  private void focus() {
    if (this.messageType == QUESTION_MESSAGE) {
      dialog.getRootPane().setDefaultButton(buttonYes);
    } else {
      dialog.getRootPane().setDefaultButton(buttonOk);
    }
  }

  private JPanel createOkButtonPanel() {
    buttonPanel = new JPanel(new GridBagLayout());

    buttonOk.setMnemonic(KeyEvent.VK_O);
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

    if (attachHelp) {
      buttonPanel.add(buttonHelp);
    }

    return buttonPanel;
  }

  private JPanel createYESNOButtonPanel(String helpButtonKey) {
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(0, 3, 5, 0));

    buttonYes.setMnemonic(KeyEvent.VK_Y);
    buttonYes.setSelected(true);
    buttonYes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        result = DialogManager.YES_BUTTON;
        dialog.dispose();
      }
    });
    buttonYes.setNextFocusableComponent(buttonNo);
    buttonYes.setMinimumSize(buttonYes.getPreferredSize());
    buttonPanel.add(buttonYes);

    buttonNo.setMnemonic(KeyEvent.VK_N);
    buttonNo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        result = DialogManager.NO_BUTTON;
        dialog.dispose();
      }
    });
    buttonNo.setNextFocusableComponent(buttonCancel);
    buttonNo.setMinimumSize(buttonNo.getPreferredSize());
    buttonPanel.add(buttonNo);

    JButton lastButton =
        ((helpButtonKey == null) || (helpButtonKey.length() == 0))
        ? buttonCancel
        : buttonHelp;

    lastButton.setDefaultCapable(false);
    lastButton.setMnemonic(KeyEvent.VK_C);
    if (lastButton == buttonCancel) {
      lastButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          result = DialogManager.CANCEL_BUTTON;
          dialog.dispose();
        }
      });
      attachHelp = false;
    } else {
      attachHelp = true;
    }
    lastButton.setMinimumSize(lastButton.getPreferredSize());
    buttonPanel.add(lastButton);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 0, 0 /*3*/, 0 /*20*/);
    downPanel.add(buttonPanel, constraints);

    return downPanel;
  }

  public int displayAlways() {
    dialog.show();
    return result;
  }

  public int display() {
    if (!box.isSelected()) {
      return result;
    }

    dialog.show();

    saveLastTimeValue(key, result, box.isSelected(), this.messageType);

    return result;
  }

  /*
   * It can be only 'Yes' or 'No' button
   */
  public void setDefaultButton(boolean isYesButton) {
    this.isYesButton = isYesButton;

    if (!isYesButton && this.messageType == QUESTION_MESSAGE) {
      dialog.getRootPane().setDefaultButton(buttonNo);
      buttonNo.requestFocus();
    }
  }

  public void setCancelEnabled(boolean enabled) {
    if (!enabled && this.messageType == QUESTION_MESSAGE) {
      buttonCancel.setVisible(false);
    }
  }
}
