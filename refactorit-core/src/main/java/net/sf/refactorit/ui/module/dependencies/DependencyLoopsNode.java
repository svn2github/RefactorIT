/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.query.CallTreeIndexer.Invocation;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 */
public class DependencyLoopsNode extends BinTreeTableNode {
  private String name = null;

  public DependencyLoopsNode(final Object bin) {
    super(bin);
    if(bin instanceof BinMember){
      setName(((BinMember)bin).getQualifiedName());
    }
  }

  public DependencyLoopsNode(final Invocation invocation) {
    super(invocation.getWhere());

    if (invocation.getUsages() > 1) {
      setSecondaryText("(" + invocation.getUsages() + " usages)");
    }

    setLine(invocation.getFirstOccurence().getLine());
  }

  public String getDisplayName() {
    if (this.name == null) {
      if (getBin() instanceof BinMethod) {
        this.name = ((BinMethod) getBin()).getOwner().getQualifiedName() + '.'
            + BinFormatter.format((BinMethod) getBin());
      } else if (getBin() instanceof BinField) {
        this.name = BinFormatter.formatNotQualified(
            ((BinField) getBin()).getTypeRef()) + " "
            + ((BinField) getBin()).getQualifiedName();
      } else if (getBin() instanceof BinInitializer) {
        this.name = ((BinInitializer) getBin()).getQualifiedName();
      } else {
        this.name = super.getDisplayName();
      }
    }

    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }
  
  /*public boolean equals(Object o) {
    if(o instanceof DependencyLoopsNode){
      Object bin = ((DependencyLoopsNode)o).getBin();
      if(this.getBin()!=null && this.getBin().equals(bin)) {
        return true;
      }
    }
    return false;
  }*/
}
