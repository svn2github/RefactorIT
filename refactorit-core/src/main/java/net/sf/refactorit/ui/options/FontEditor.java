/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.commonIDE.IDEController;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Insert the type's description here.
 *
 * @author Igor Malinin
 */
public class FontEditor extends DefaultCellEditor {
  private static final JCheckBox dummyCheckBox = new JCheckBox();

  Font font;

  private JButton button;

  public FontEditor(Component parent) {
    // we have no condtructor for JButton
    super(dummyCheckBox);

    // setup real editorComponent
    button = new JButton();
    button.setBackground(Color.white);
    button.setBorderPainted(false);
    button.setDefaultCapable(false);
    button.setFont(new Font("SansSerif", Font.PLAIN, 12));
    button.setHorizontalAlignment(JButton.LEFT);
    button.setMargin(new Insets(0, 0, 0, 0));
    editorComponent = button;

    setClickCountToStart(2);

    final JFontChooser chooser = new JFontChooser();

    final ActionListener okListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        font = chooser.getFont();
        stopCellEditing();
      }
    };

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooser.setFont(font);
        // TODO: XXX Doublecheck null is OK
        JFontChooserDialog dialog = new JFontChooserDialog(
            IDEController.getInstance().createProjectContext(),
            "Pick a Font", chooser, okListener, null);
        dialog.show();
      }
    });
  }

  public Object getCellEditorValue() {
    return font;
  }

  public Component getTableCellEditorComponent(JTable table,
      Object value, boolean isSelected, int row, int column
      ) {
    font = (Font) value;

    button.setFont(font);

    StringBuffer buf = new StringBuffer(font.getName());
    if (font.isBold()) {
      buf.append("-bold");
    }
    if (font.isItalic()) {
      buf.append("-italic");
    }
    buf.append('-').append(font.getSize());

    button.setText(buf.toString());

    return editorComponent;
  }
}
