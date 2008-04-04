/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.license;



import net.sf.refactorit.Version;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLEditorKit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;


/**
 *
 *
 * @author Igor Malinin
 */
public class AboutDialog {
  private static final ImageIcon logo
      = ResourceUtil.getIcon(AboutDialog.class, "logo.png");

  private static final Color background = Color.white;

  final RitDialog dialog;

  private JTextPane text;

  final Action closer = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      dialog.dispose();
    }
  };

  public AboutDialog(IdeWindowContext context) {
    dialog = RitDialog.create(context);
    dialog.setTitle("About RefactorIT");
    dialog.setSize(400, 400);

    Container content = dialog.getContentPane();

    content.setBackground(background);

    JLabel logoLabel = new JLabel(logo);

    content.add(logoLabel, BorderLayout.NORTH);
    content.add(createCenterPanel());
    content.add(createButtonPanel(), BorderLayout.SOUTH);

    reload();
  }

  private Container createCenterPanel() {
    text = new JTextPane();
    text.setFont(new Font("SansSerif", Font.PLAIN, 12));
    text.setEditable(false);
    text.setEditorKit(new HTMLEditorKit());
    text.addHyperlinkListener(new ExternalBrowserAdapter());
    text.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
        closer);

    JScrollPane pane = new JScrollPane(text);
    pane.setBorder(BorderFactory.createEtchedBorder());

    return pane;
  }

  private JPanel createButtonPanel() {
    // Buttons
    JPanel buttonPanel = new JPanel(new GridBagLayout());


    GridBagConstraints constraints = new GridBagConstraints();
    constraints.ipadx = 0;
    constraints.ipady = 3;
    constraints.insets = new Insets(5, 5, 5, 3);

    JButton buttonOk = new JButton("Ok");
    buttonOk.addActionListener(closer);

    constraints.gridx = 0; //2;

    buttonPanel.add(buttonOk, constraints);

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, closer);

    return buttonPanel;
  }

  public void reload() {
    Object[] args = {
        Version.getVersion(),
        Version.getBuildId(),
        AboutTemplates.getIDEText(),
    };

    String about = AboutTemplates.getAboutTemplate();
    text.setText(MessageFormat.format(about, args));
    text.select(0, 0);
  }

  public void show() {
    dialog.show();
  }
}
