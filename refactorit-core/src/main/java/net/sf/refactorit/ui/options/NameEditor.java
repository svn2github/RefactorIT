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
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * @author Anton Safonov
 */
public class NameEditor extends DefaultCellEditor {
  private static final JCheckBox dummyCheckBox = new JCheckBox();

  public NameEditor() {
    super(dummyCheckBox); // we have no constructor for JButton

    // setup real editorComponent
    editorComponent = new JButton();
    editorComponent.setBackground(Color.white);
    ((JButton) editorComponent).setBorderPainted(false);
    ((JButton) editorComponent).setDefaultCapable(false);
    editorComponent.setFont(new Font("SansSerif", Font.PLAIN, 12));
    ((JButton) editorComponent).setHorizontalAlignment(JButton.LEFT);
    ((JButton) editorComponent).setMargin(new Insets(0, 0, 0, 0));

    setClickCountToStart(2);
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected,
      int row, int column) {
    TableCellEditor oldEditor = table.getCellEditor();
    if (oldEditor != null) {
      oldEditor.stopCellEditing();
    }

    final Rectangle rect = table.getCellRect(row, 1, false);
    table.dispatchEvent(
        new MouseEvent(table, MouseEvent.MOUSE_PRESSED,
        System.currentTimeMillis(), MouseEvent.BUTTON1_MASK,
        (int) rect.getCenterX(), (int) rect.getCenterY(), 2, false));
    table.dispatchEvent(
        new MouseEvent(table, MouseEvent.MOUSE_RELEASED,
        System.currentTimeMillis(), MouseEvent.BUTTON1_MASK,
        (int) rect.getCenterX(), (int) rect.getCenterY(), 2, false));
    table.dispatchEvent(
        new MouseEvent(table, MouseEvent.MOUSE_CLICKED,
        System.currentTimeMillis(), MouseEvent.BUTTON1_MASK,
        (int) rect.getCenterX(), (int) rect.getCenterY(), 2, false));

    try {
      TableCellEditor newEditor = table.getCellEditor(row, 1);
//System.err.println("Editor: " + newEditor);
      final Component c = newEditor.getTableCellEditorComponent(
          table, table.getModel().getValueAt(row, 1), true, row, 1);
//System.err.println("Component: " + c);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          c.requestFocus();
        }
      });

//System.err.println("hasFocus: " + c.hasFocus() );
    } catch (NullPointerException e) {
      e.printStackTrace(System.err);
    } catch (ClassCastException e) {
      e.printStackTrace(System.err);
    }

    return null;
  }
}
