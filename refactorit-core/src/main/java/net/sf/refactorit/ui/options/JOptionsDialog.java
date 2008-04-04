/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * Dialog for editing options.
 *
 * @author Igor Malinin
 * @author Anton Safonov
 */
public class JOptionsDialog {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JOptionsDialog.class);

  final RitDialog dialog;

  boolean okPressed;

  private Properties properties;

  Options opts;
  private ResourceBundle names;
  JTabbedPane center;
  //private boolean wasReset = false;
  private JButton resetButton = new JButton();
  JPanel help;

  private String helpId;


  List tableList = new ArrayList();

  public JOptionsDialog(
      IdeWindowContext context, Properties props, Options opts,
      ResourceBundle names, String helpTopicId, String dialogTitle
  ) {
    dialog = RitDialog.create(context);

    this.opts = opts;
    this.names = names;
    this.helpId = helpTopicId;

    properties = (Properties) props.clone();

    init(dialogTitle);
  }

  private JTabbedPane getTab() {
    JTabbedPane centerPane = new JTabbedPane();

    int tabCount = opts.getTabCount();
    for (int i = 0; i < tabCount; i++) {
      OptionsTab tab = opts.getTab(i);

      OptionsTableModel model = new OptionsTableModel(tab, properties, names);


      JTable table = new JOptionsTable(dialog.getRootPane(), model);
      tableList.add(table);

      String title = tab.getName();
      if (names != null) {
        String local = names.getString(title);
        if (local != null) {
          title = local;
        }
      }

      centerPane.addTab(title, new JScrollPane(table));
    }

    return centerPane;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 4, 4, 0));

    JButton okButton = new JButton(resLocalizedStrings.getString("button.ok"));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;

        // it fix bug: need to move focus from edited cell
        // for stopEditing  activation
        for(Iterator x = tableList.iterator(); x.hasNext();) {
          ((BinTable) x.next()).stopEditing();
        }

        int tabCount = opts.getTabCount();
        for (int i = 0; i < tabCount; i++) {
          OptionsTab tab = opts.getTab(i);
          tab.save();
        }

        dialog.dispose();
      }
    });

    resetButton.setText(resLocalizedStrings.getString("button.default"));
    resetButton.setMnemonic(KeyEvent.VK_D);
    resetButton.setDefaultCapable(false);
    resetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reset();
      }
    });

    JButton cancelButton = new JButton(
        resLocalizedStrings.getString("button.cancel"));
    ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int tabCount = opts.getTabCount();
        for (int i = 0; i < tabCount; i++) {
          OptionsTab tab = opts.getTab(i);
          tab.cancel();
        }

        dialog.dispose();
      }
    };
    cancelButton.addActionListener(cancelActionListener);

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, this.helpId);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton,
        buttonHelp, cancelActionListener);

    buttonPanel.add(okButton);
    buttonPanel.add(resetButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(buttonHelp);
    /*
      JPanel downPanel = new JPanel( new GridBagLayout() );
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 1;
      constraints.gridy = 1;
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.EAST;
      constraints.weightx = 1.0;
      constraints.weighty = 0.0;
      constraints.insets = new Insets( 3, 0, 3, 20);
      downPanel.add( buttonPanel, constraints);

      constraints.gridx = 2;
      constraints.gridy = 1;
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.EAST;
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.insets = new Insets( 3, 0, 3, 20);
      downPanel.add( cancelButton, constraints);
     */
    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

    return downPanel;
  }

  private void init(String title) {
    dialog.setSize(640, 430);
    dialog.setTitle(title);

    center = getTab();
    center.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    final JPanel main = new JPanel(new BorderLayout());
    main.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    String text = "RefactorIT Options ... ";
    text += center.getTitleAt(center.getSelectedIndex());
    help = DialogManager.getHelpPanel(text);

    main.add(help, BorderLayout.NORTH);
    main.add(center, BorderLayout.CENTER);

    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(main, BorderLayout.CENTER);
    dialog.getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

    center.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String text2 = "RefactorIT Options ... ";
        text2 += center.getTitleAt(center.getSelectedIndex());
        help = DialogManager.getHelpPanel(text2);

        main.removeAll();
        main.add(help, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        dialog.getContentPane().validate();
        dialog.getContentPane().repaint();
      }
    });

    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (okPressed) {
          return;
        }

        int tabCount = opts.getTabCount();
        for (int i = 0; i < tabCount; i++) {
          OptionsTab tab = opts.getTab(i);
          tab.cancel();
        }
      }
    });
  }

  public Properties getProperties() {
    return okPressed ? properties : null;
  }

  void reset() {
    int selected = center.getSelectedIndex();
    OptionsTab tab = opts.getTab(selected);
    tab.setDefault();

    for (int i = 0; i < tab.getVisibleOptionsCount(); i++) {
      Option opt = tab.getVisibleOption(i);

      String key = opt.getKey();
      String value = GlobalOptions.getDefaultOption(key);
      if (value == null) {
        if (properties.getProperty(key) != null) {
          // it is neccessary for instance in warning options
          properties.remove(key);
        }
      } else {
        properties.setProperty(key, value);
      }
    }

    dialog.getContentPane().validate();
    dialog.getContentPane().repaint();

    center.setSelectedIndex(selected);
  }

  public void setResetEnabled(boolean enabled) {
    resetButton.setEnabled(enabled);
  }

  public void show() {
    dialog.show();
  }
}
