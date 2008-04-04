/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteral;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;



/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitTreeTableModel extends BinTreeTableModel{
    
  public NumLitTreeTableModel() {
    super(new BinTreeTableNode("Violations: "));
  }
  
  public int getColumnCount() {
    return 3;
  }
  
  public Class getColumnClass(int column) {
    switch (column){
      case 0: return Integer.class;
      case 1: return String.class;
      case 3: return String.class;
      default: return String.class;
    }
  }
  
  public String getColumnName(final int column) {
    switch (column){
      case 0: return "Line";
      case 1: return "Source preview";
      case 2: return "SFC";
      default: return StringUtil.EMPTY_STRING;
    }
  }
  
  public Object getValueAt(Object node, int column){
    if (node instanceof NumLitTreeTableNode){
      switch (column){
        case 0: 
          return new Integer(((NumLitTreeTableNode) node).getRuleViolation()
              .getLine());
        case 1: 
          return getPreview((NumLitTreeTableNode) node);
        case 2:
          return ((NumLitTreeTableNode) node).getNumericViolation()
              .isConstantalizable() ? "yes" : "no";
        default: 
          return StringUtil.EMPTY_STRING;
      }
    }
    
    return StringUtil.EMPTY_STRING;
  }
  
  public static String getPreview(NumLitTreeTableNode node){
    StringBuffer result = new StringBuffer();
    if (!node.getNumericViolation().hasFix()){
      return asHTML(node.getLineSource());
    } else {
      NumericLiteral violation = node.getNumericViolation();
      return asHTML(violation.getFix().getLinePreview());
    }
  }
  
  private static String asHTML(String s){
    StringBuffer result = new StringBuffer(s);
    return new String(result.insert(0, "<html>&nbsp;&nbsp;").append("</html>"));
  }
  
  public NumLitTreeTableNode getNodeForRow(int row){
    return (NumLitTreeTableNode) this.getChild(getRoot(), row);
  }
}


