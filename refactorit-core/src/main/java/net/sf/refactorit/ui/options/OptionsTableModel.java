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
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;

import javax.swing.table.AbstractTableModel;

import java.awt.Color;
import java.awt.Font;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * @author Igor Malinin
 */
public class OptionsTableModel extends AbstractTableModel {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(OptionsTableModel.class);

  private OptionsTab optionsTab;
  private Properties properties;
  private ResourceBundle bundle;


  public OptionsTableModel(OptionsTab optionsTab, Properties properties,
      ResourceBundle bundle) {
    this.optionsTab = optionsTab;
    this.properties = properties;
    this.bundle = bundle;
  }


  /**
   * Returns the number of columns managed by the data source object. A
   * <B>JTable</B> uses this method to determine how many columns it
   * should create and display on initialization.
   *
   * @return the number or columns in the model
   * @see #getRowCount
   */
  public int getColumnCount() {
    return 2;
  }

  /**
   * Returns the name of the column at <i>index</i>.  This is used
   * to initialize the table's column header name.  Note, this name does
   * not need to be unique.  Two columns on a table can have the same name.
   *
   * @param	index	the index of column
   * @return  the name of the column
   */
  public String getColumnName(int index) {
    switch (index) {
      case 0:
        return resLocalizedStrings.getString("table.header.name");
      case 1:
        return resLocalizedStrings.getString("table.header.value");

      default:
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }
  }

  /**
   * Returns the number of records managed by the data source object. A
   * <B>JTable</B> uses this method to determine how many rows it
   * should create and display.  This method should be quick, as it
   * is call by <B>JTable</B> quite frequently.
   *
   * @return the number or rows in the model
   * @see #getColumnCount
   */
  public int getRowCount() {
    return optionsTab.getVisibleOptionsCount();
  }

  /**
   * Returns an attribute value for the cell at <I>column</I>
   * and <I>row</I>.
   *
   * @param	row		the row whose value is to be looked up
   * @param	column 	the column whose value is to be looked up
   * @return	the value Object at the specified cell
   */
  public Object getValueAt(int row, int column) {

    Option option = optionsTab.getVisibleOption(row);

    if (column == 0) {
      String value = option.getValue();
      if (value != null) {
        return value;
      }

      if (bundle == null) {
        return option.getKey();
      }

      String local = bundle.getString(option.getKey());
      return (local == null) ? option.getKey() : local;
    }

    if (column == 1) {
      String key = option.getKey();
      String value = properties.getProperty(key);
      if (value == null) {
        if (option instanceof WarningOption || option instanceof PreviewOption) {
          value = "true";
        } else {
          value = "";
        }
      }

      Class type = option.getType();

      if (type.equals(String.class)) {
        return value;
      }

      if (type.equals(Integer.class)) {
        if (value.length() == 0) {
          // TODO constant!!!
          return new Integer(0);
        }
        try {
          return new Integer(value);
        } catch (Exception e) {
          // TODO constant!!!
          return new Integer(0);
        }
      }

      if (type.equals(Boolean.class)) {
        if (value.length() == 0) {
          // TODO constant!!!
          return new Boolean(false);
        }
        try {
          return new Boolean(value);

          // Jikes can't stand 'catch(Exception ignore){}' here..
        } catch (RuntimeException e) {
          // TODO constant!!!
          return new Boolean(false);
        }
      }

      if (type.equals(Separator.class)) {
        return new Separator(value);
      }

      if (type.equals(Shortcut.class)) {
        return ((CustomOptionsTab) optionsTab).getValue(option.getKey());
      }

      if (type.equals(Color.class)) {
        if (value.length() == 0) {
          return Color.gray;
        }
        try {
          return Color.decode(value);
        } catch (Exception e) {
          return Color.gray;
        }
      }

      if (type.equals(Font.class)) {
        if (value.length() == 0) {
          // TODO constant!!!
          return new Font("SansSerif", Font.PLAIN, 12);
        }
        try {
          return Font.decode(value);

          // Jikes can't stand 'catch(Exception ignore){}' here..
        } catch (RuntimeException e) {
          // TODO constant!!!
          return new Font("SansSerif", Font.PLAIN, 12);
        }
      }

      if (type.equals(ClassPath.class)) {
        return new ClassPath(value);
      }

      if (type.equals(SourcePath.class)) {
        return new SourcePath(value);
      }

      if (type.equals(JavadocPath.class)) {
        return new JavadocPath(value);
      }

      if (type.equals(UIResources.CharacterEncoding.class)) {
        return new UIResources.CharacterEncoding(value.toString());
      }
    }

    new Exception("unknown option: " + option.getClass()
        + ", key: " + option.getKey() + ", type: " + option.getType()
        + ", value: " + option.getValue()).printStackTrace();
    return "!!! unknown option !!!";
  }

  private java.util.HashMap propertiesToEditorComponents = new java.util.
      HashMap();

  javax.swing.JComponent getEditorComponentForProperty(ProjectProperty property) {
    if (propertiesToEditorComponents.get(property) != null) {
      return (javax.swing.JComponent) propertiesToEditorComponents.get(property);
    } else {
      javax.swing.JComponent result = property.getEditor();
      propertiesToEditorComponents.put(property, result);
      return result;
    }
  }

  /**
   * Returns true if the cell at <I>row</I> and <I>column</I>
   * is editable.  Otherwise, setValueAt() on the cell will not change
   * the value of that cell.
   *
   * @param	row		the row whose value is to be looked up
   * @param	column	the column whose value is to be looked up
   * @return	true if the cell is editable.
   * @see #setValueAt
   */
  public boolean isCellEditable(int row, int column) {
    return true;
  }

  /**
   * Sets an attribute value for the record in the cell at
   * <I>column</I> and <I>row</I>.  <I>value</I> is
   * the new value.
   *
   * @param	value	 the new value
   * @param	row		 the row whose value is to be changed
   * @param	column 	 the column whose value is to be changed
   * @see #getValueAt
   * @see #isCellEditable
   */
  public void setValueAt(Object value, int row, int column) {
    if (column == 1) {
      String key = optionsTab.getVisibleOption(row).getKey();
      if (value instanceof Color) {
        Color c = (Color) value;
        properties.setProperty(key,
            "#" + Integer.toHexString(c.getRGB()).substring(2));
      } else if (value instanceof Font) {
        Font f = (Font) value;
        StringBuffer buf = new StringBuffer(f.getName());
        if (f.isBold()) {
          buf.append("-bold");
        }
        if (f.isItalic()) {
          buf.append("-italic");
        }
        buf.append('-').append(f.getSize());

        properties.setProperty(key, buf.toString());
      } else if (value instanceof Shortcut) {
        ((CustomOptionsTab) optionsTab).setValue(key, value);
      } else if (value instanceof String) {
        Object node = getValueAt(row, column);
        if (node instanceof Separator) {
          String s = (((String) value).equals("none")) ? "none"
              : ((String) value).substring(0, 1);
          properties.setProperty(key, s);
        } else {
          properties.setProperty(key, (String) value);
        }
      } else {
        properties.setProperty(key, value.toString());
      }

      fireTableCellUpdated(row, column);
    }
  }

  public OptionsTab getOptionsTab() {
    return optionsTab;
  }
}
