/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.encapsulatefield;


import net.sf.refactorit.refactorings.PropertyNameUtil;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateField;

import javax.swing.table.AbstractTableModel;

import java.util.Arrays;

/**
 * @author Kirill Buhhalko
 */

public class EncapsulateTableModel  extends AbstractTableModel {

  private String[] fieldName;
  private String[] setterName;
  private String[] getterName;
  private Boolean[] isEnabled;
  private boolean isFinal[];
  private int size;
  boolean setterEnabled = true;
  boolean getterEnabled = true;


  public EncapsulateTableModel(EncapsulateField[] encapsulatorA) {

    Arrays.sort(encapsulatorA);

    size = encapsulatorA.length;

    fieldName = new String[size];
    setterName = new String[size];
    getterName = new String[size];
    isEnabled = new Boolean[size];
    isFinal = new boolean[size];

    for(int x = 0; x < size; x++) {
      fieldName[x] = encapsulatorA[x].getField().getName();

      setterName[x] = encapsulatorA[x].getField().isFinal() ? 
          "" 
          : PropertyNameUtil.getDefaultSetterName(encapsulatorA[x].getField());

      getterName[x] = PropertyNameUtil.getDefaultGetterName(
          encapsulatorA[x].getField())[0];
      isEnabled[x] = new Boolean(encapsulatorA[x].isEnabled());

      this.isFinal[x] = encapsulatorA[x].getField().isFinal();
    }
  }

  public int getColumnCount() {
    return 4;
  }

  public int getRowCount() {
    return size;
  }

  public Class getColumnClass (int column) {
    if (column == 0) return Boolean.class;
    if ((column >= 1)&&(column <=3)) return String.class;
    else return Object.class;
  }

  public void setValueAt(Object value, int row, int column) {

    switch(column) {
      case 0:
        this.isEnabled[row] = (Boolean) value;
        break;
      case 2:
        this.setterName[row] = (String) value;
        break;
      case 3:
        this.getterName[row] = (String) value;
        break;
      default:
        break;
    }

  }

  public Object getValueAt(int row, int column) {

    switch(column) {
      case 0:
        return this.isEnabled[row];
      case 1:
        return this.fieldName[row];
      case 2:
       /* if (!this.setterEnabled)
          return "     ---";
        if (!this.isEnabled[row].booleanValue())
          return "     ---";
        if (this.isFinal[row]) {
          return "(final field)";
        }*/
        return this.setterName[row];
      case 3:
        /*if (!this.getterEnabled)
          return "     ---";
        if (!this.isEnabled[row].booleanValue())
          return "     ---";*/
        return this.getterName[row];
      default: return null;
    }


  }

  public boolean isCellEditable(int row, int column) {

    switch(column) {
      case 1:
        return false;
      case 2:
        if(setterEnabled && isEnabled[row].booleanValue()) {
          return !this.isFinal[row];
        }
        return false;
      case 3:
        return (getterEnabled && isEnabled[row].booleanValue());
      default:
        return true;
    }
  }



  public String getColumnName(int column) {
  switch (column) {
    case 0:
      return " ";

    case 1:
      return "Field Name";

    case 2:
      return "Setter Name";

    case 3:
      return "Getter Name";

    default:
      return "";
  }
}

  public String getSetterName(int row) {
    return this.setterName[row];
  }

  public String getGetterName(int row) {
    return this.getterName[row];
  }

  public boolean getIsEnabled(int row) {
    return this.isEnabled[row].booleanValue();
  }

  public boolean getIsFinal(int row) {
    return this.isFinal[row];
  }
}
