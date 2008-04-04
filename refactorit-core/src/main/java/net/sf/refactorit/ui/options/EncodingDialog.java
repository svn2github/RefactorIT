/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


class EncodingDialog {
    final RitDialog dialog;

    JComboBox encodingEditor;

    boolean okPressed;
    String encoding;

    EncodingDialog(IdeWindowContext context, String encoding) {
      this.encoding = encoding;

      dialog = RitDialog.create(context);
      dialog.setTitle("Source Encoding");
      dialog.setSize(300, 150);

      JPanel contentPane = new JPanel();

      contentPane.setLayout(new BorderLayout());

      contentPane.add(createEncodingPanel(), BorderLayout.CENTER);
      contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

      //contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      dialog.setContentPane(contentPane);
    }

    public void show() {
      dialog.show();
    }

    private JPanel createEncodingPanel() {
      JPanel result = new JPanel(new BorderLayout());
      result.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 3, 3, 3),
          BorderFactory.createEtchedBorder())
          );

      JPanel p = new JPanel(new GridBagLayout());
      this.encodingEditor = createEncodingEditor();
      p.add(encodingEditor);
      p.setBorder(BorderFactory.createEmptyBorder(10, 3, 10, 3));
      result.add(p, BorderLayout.CENTER);
      result.add(DialogManager.getHelpPanel(
          "Choose desired source text encoding"), BorderLayout.NORTH);

      return result;
    }

    private JComboBox createEncodingEditor() {
      JComboBox result = new JComboBox(GlobalOptions.getKnownSupportedEncodings());

      if (!alreadyListed(this.encoding, result)) {
        result.addItem(this.encoding);
      }

      result.setSelectedIndex(itemIndex(this.encoding, result));
      result.setEditable(true);

      return result;
    }

    private boolean alreadyListed(Object encoding, JComboBox items) {
      return itemIndex(encoding, items) >= 0;
    }

    private int itemIndex(Object item, JComboBox items) {
      if (item == null) {
        return -1;
      }

      for (int i = 0; i < items.getItemCount(); i++) {
        if (items.getItemAt(i).toString().equals(encoding.toString())) {
          return i;
        }
      }

      return -1;
    }

    private JPanel createButtonPanel() {
      JButton okButton = new JButton("Ok");

      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String result = (String) encodingEditor.getSelectedItem();

          if (!GlobalOptions.encodingSupported(result.toString())) {
            RitDialog.showMessageDialog(dialog.getContext(),
                "Unsupported encoding", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          okPressed = true;
          encoding = result;
          dialog.dispose();
        }
      });

      final ActionListener cancelActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(cancelActionListener);

      JButton buttonHelp = new JButton("Help");
      HelpViewer.attachHelpToDialog(dialog,
          buttonHelp, "getStart.refactoritOptions");

      JPanel result = new JPanel(new GridLayout(1, 3, 4, 0));
      result.add(okButton);
      result.add(cancelButton);
      result.add(buttonHelp);
      
      SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
          buttonHelp, cancelActionListener);

      JPanel p = new JPanel(new GridBagLayout());
      p.add(result);

      return p;
    }

    public String getEncoding() {
      return encoding;
    }

    public boolean okPressed() {
      return okPressed;
    }
  }
