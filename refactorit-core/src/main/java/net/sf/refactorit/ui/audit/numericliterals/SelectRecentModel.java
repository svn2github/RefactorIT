/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class SelectRecentModel extends BinTreeTableModel {
  
  private boolean empty = true;
  
  public SelectRecentModel(Map usedConstants) {
    super(new BinTreeTableNode("Recent"));
    collectRecent(usedConstants);
  }
  
  private final void collectRecent(final Map usedConstants){ 
    Set used = new HashSet();
    Set created = new HashSet();
    for (Iterator it = usedConstants.keySet().iterator(); it.hasNext(); ){
      BinField field = (BinField) it.next();
      if (usedConstants.get(field) == null){
        used.add(field);
      } else {
        created.add(field);
      }
    }

    BinTreeTableNode usedNode = new BinTreeTableNode("Recently used fields");
    if (used.size() > 0){
      this.empty = false;
      for (Iterator it = used.iterator(); it.hasNext(); ){
        BinTreeTableNode newNode = new BinTreeTableNode(it.next());
        usedNode.addChild(newNode);
      }
    } else {
      usedNode.addChild(new BinTreeTableNode("<no recently used fields yet>"));
    }
        
    BinTreeTableNode createdNode 
        = new BinTreeTableNode("Recently created fields");
    if (created.size() > 0){
      this.empty = false;
      for (Iterator it = created.iterator(); it.hasNext(); ){
        BinTreeTableNode newNode = new BinTreeTableNode(it.next());
        createdNode.addChild(newNode);
      }
    } else {
      createdNode.addChild(
          new BinTreeTableNode("<no recently created fields yet>"));
    }
    
    ((BinTreeTableNode) this.getRoot()).addChild(createdNode);
    ((BinTreeTableNode) this.getRoot()).addChild(usedNode);
  }
  
  public Class getColumnClass(int column) {
    switch (column){
      case 0: return TreeTableModel.class;
      case 1: return String.class;
      default: return String.class;
    }
  }
  
  public int getColumnCount() {
    return 2;
  }
  
  public String getColumnName(final int column) {
    switch (column){
      case 0: return "Field";
      case 1: return "Field value";
      default: return StringUtil.EMPTY_STRING;
    }
  }
  
  public Object getValueAt(Object node, int column){
    if (node instanceof BinTreeTableNode){
      switch (column){
        case 0: 
          return node;
        case 1:
          if (((BinTreeTableNode) node).getBin() instanceof BinField){
            BinField field = (BinField) ((BinTreeTableNode) node).getBin();
            if (field.hasExpression()){
              return field.getExpression().getText();
            } else {
              return field.getExpression();
            }
          }
        default: 
          return StringUtil.EMPTY_STRING;
      }
    }
    
    return StringUtil.EMPTY_STRING;
  }
  
  public final boolean isEmpty(){
    return this.empty;
  }
}
