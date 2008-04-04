/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CharacterEncodingEditor extends DefaultCellEditor {
  private static final JCheckBox dummyCheckBox = new JCheckBox();

  String encoding;
  JButton b;

  public CharacterEncodingEditor(final IdeWindowContext context) {
    // we have no condtructor for JButton
    super(dummyCheckBox);

    // setup real editorComponent
    this.b = new JButton();
    b.setHorizontalAlignment(SwingConstants.LEFT);
    b.setOpaque(false);
    b.setBorderPainted(false);
    b.setDefaultCapable(false);
    b.setMargin(new Insets(0, 0, 0, 0));
    editorComponent = b;

    setClickCountToStart(2);

    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EncodingDialog dialog = new EncodingDialog(context, encoding);
        dialog.show();
        if (dialog.okPressed()) {
          encoding = dialog.getEncoding();
          b.setText(encoding);
          stopCellEditing();
        }
      }
    });
  }

  public Object getCellEditorValue() {
    return encoding;
  }

  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column
  ) {
    encoding = value.toString();
    b.setText(encoding);

    return editorComponent;
  }
}
