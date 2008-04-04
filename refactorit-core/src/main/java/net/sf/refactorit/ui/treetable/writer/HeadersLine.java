/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

import net.sf.refactorit.ui.treetable.BinTreeTableModel;


public class HeadersLine implements LineContentsProvider {
  private BinTreeTableModel model;

  public HeadersLine(BinTreeTableModel model) {
    this.model = model;
  }

  public String getTypeColumn() {
    return "Type";
  }

  public String getNameColumn() {
    return getColumn(0);
  }

  public String getColumn(int i) {
    return model.getColumnName(i);
  }

  public String getPackageColumn() {
    return "Package";
  }

  public String getClassColumn() {
    return "Class";
  }
}
