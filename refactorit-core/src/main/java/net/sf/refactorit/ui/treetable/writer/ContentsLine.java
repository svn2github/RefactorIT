/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;


public class ContentsLine implements LineContentsProvider {
  private ParentTreeTableNode node;

  private BinTreeTableModel model;

  public ContentsLine(BinTreeTableModel model, ParentTreeTableNode node) {
    this.node = node;
    this.model = model;
  }

  public String getTypeColumn() {
    return node.getNameType(node.getBin());
  }

  public String getNameColumn() {
    return node.getNameForTextOutput();
  }

  public String getColumn(int i) {
    return node.getValue(model, i);
  }

  public String getPackageColumn() {
    Object v = node.getBin();
    if (v instanceof BinTypeRef) {
      v = ((BinTypeRef) v).getBinType();
    }

    if (v instanceof BinPackage) {
      return ((BinPackage) v).getQualifiedDisplayName();
    } else if (v instanceof BinMember) {
      return ((BinMember) v).getPackage().getQualifiedDisplayName();
    } else {
      return "";
    }
  }

  public String getClassColumn() {
    Object v = node.getBin();
    if (v instanceof BinTypeRef) {
      v = ((BinTypeRef) v).getBinType();
    }

    if (v instanceof BinCIType) {
      return ((BinCIType) v).getName();
    } else if (v instanceof BinMember) {
      return ((BinMember) v).getOwner().getBinCIType().getName();
    } else {
      return "";
    }
  }
}
