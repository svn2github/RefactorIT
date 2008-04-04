/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Insert the type's description here.
 *
 * @author Igor Malinin
 */
public class ColorEditor extends DefaultCellEditor {
  private static final JCheckBox dummyCheckBox = new JCheckBox();

  Color color;

  public ColorEditor(Component parent) {
    // we have no condtructor for JButton
    super(dummyCheckBox);

    // setup real editorComponent
    final JButton b = new JButton();
    b.setBackground(Color.white);
    b.setBorderPainted(false);
    b.setDefaultCapable(false);
    b.setMargin(new Insets(0, 0, 0, 0));
    editorComponent = b;

    setClickCountToStart(2);

    final JColorChooser chooser = new JColorChooser();

    ActionListener okListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        color = chooser.getColor();
        stopCellEditing();
      }
    };

    final JDialog dialog = JColorChooser.createDialog(parent,
        "Pick a Color", true, chooser, okListener, null);
    // TODO: XXXDoublecheck null is OK

    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        b.setBackground(color);
        chooser.setColor(color);
        dialog.show();
      }
    });
  }

  public Object getCellEditorValue() {
    return color;
  }

  public Component getTableCellEditorComponent(JTable table,
      Object value, boolean isSelected, int row, int column
      ) {
    color = (Color) value;
    return editorComponent;
  }
}
