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
import net.sf.refactorit.ui.Shortcuts;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;
import net.sf.refactorit.ui.table.BinTable;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;


/**
 * Extends JTable in order to have different
 * kinds of input in one column.
 *
 * @author Igor Malinin
 */
public class JOptionsTable extends BinTable {
  private TableCellEditor stringEditor;

  public JOptionsTable(Component parent, OptionsTableModel model) {
    super(model);

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    setDefaultRenderer(Color.class, new ColorRenderer());
    setDefaultEditor(Color.class, new ColorEditor(parent));

    setDefaultRenderer(Font.class, new FontRenderer());
    setDefaultEditor(Font.class, new FontEditor(parent));

    setDefaultEditor(ClassPath.class, new PathEditor(parent,
        JPathChooser.CLASSPATH));
    setDefaultEditor(SourcePath.class, new PathEditor(parent,
        JPathChooser.SOURCEPATH));
    setDefaultEditor(JavadocPath.class, new PathEditor(parent,
        JPathChooser.JAVADOCPATH));

    this.stringEditor = getDefaultEditor(String.class);
    setDefaultEditor(String.class, new NameEditor());

    // separator
    setDefaultEditor(Separator.class,
        new SeparatorEditor(new JComboBox(
        new Object[] {", (comma)", ". (dot)", "  (space)", "none"})));
    setDefaultRenderer(Separator.class, new ComboRenderer());

    setDefaultEditor(UIResources.CharacterEncoding.class,
        new CharacterEncodingEditor(
            IDEController.getInstance().createProjectContext()));
    setDefaultRenderer(UIResources.CharacterEncoding.class, new ComboRenderer());

    // shortcuts
    TableCellEditor editor;
    if (model.getOptionsTab() instanceof Shortcuts) {
      editor = new ShortcutsEditor(model.getOptionsTab());
    } else {
      editor = new DefaultCellEditor(new JTextField());
    }

    setDefaultEditor(Shortcut.class, editor);
    setDefaultRenderer(Shortcut.class, new ShortcutsRenderer());

    setDefaultEditor(ProjectProperty.class, new ProjectPropertyEditor());
    setDefaultRenderer(ProjectProperty.class, new ProjectPropertyRenderer());
  }

  public TableCellEditor getCellEditor(int row, int column) {
    Object value = getModel().getValueAt(row, column);
    TableCellEditor editor = getDefaultEditor(value.getClass());

    if (editor instanceof NameEditor) {
      if (column == 1) {
        editor = this.stringEditor;
      } else {
        final TableCellEditor oldEditor = this.getCellEditor();
        if (oldEditor != null) {
          oldEditor.stopCellEditing();
        }
      }
    }

    return editor;
  }

  public TableCellRenderer getCellRenderer(int row, int column) {
    if (column == 1) {
      Object value = getModel().getValueAt(row, column);
      return getDefaultRenderer(value.getClass());
    }

    return super.getCellRenderer(row, column);
  }

  public boolean isManagingFocus() {
    return false;
  }
}
