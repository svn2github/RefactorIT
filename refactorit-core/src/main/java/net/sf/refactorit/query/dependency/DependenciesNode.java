/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 * @author Anton Safonov
 */
public class DependenciesNode extends BinTreeTableNode {
  public DependenciesNode(Object bin, boolean showSource) {
    super(bin, showSource);
  }

  public String getDisplayName() {
    if (this.name == null) {
      if (getBin() instanceof BinCIType) {
        this.name = BinFormatter.formatWithAllOwners(
            ((BinCIType) getBin()).getTypeRef());
      } else if (getBin() instanceof BinTypeRef) {
        this.name = BinFormatter.formatWithAllOwners((BinTypeRef) getBin());
//      } else if (getBin() instanceof BinMethod) {
//        this.name = BinFormatter.formatWithAllOwners(((BinMethod) getBin()).getOwner()) + "."
//            + BinFormatter.formatWithoutReturn((BinMethod) getBin());
//      } else if (getBin() instanceof BinField) {
//        this.name = BinFormatter.formatWithAllOwners(((BinField) getBin()).getOwner()) + "."
//            + BinFormatter.formatWithoutType((BinField) getBin());
      } else if (getBin() instanceof BinMember) {
        this.name = BinFormatter.formatWithAllOwners(((BinMember) getBin()).getOwner()) + "."
            + super.getDisplayName();
//      } else if (getBin() instanceof CompilationUnit) {
//        this.name = ((CompilationUnit) getBin()).getDisplayPath();
//        int ind = this.name.lastIndexOf(' ');
//        if (ind > 0) {
//          this.name = this.name.substring(ind);
//        }
      } else {
//        if (getBin() instanceof BinMember) {
//          BinTypeRef ownerRef = ((BinMember) getBin()).getOwner();
//          if (ownerRef != null) {
//            this.name = ownerRef.getQualifiedName();
//          }
//        }
//
//        if (this.name != null) {
//          this.name += "." + super.getName();
//        } else {
        this.name = super.getDisplayName();
//        }
      }
    }

    return this.name;
  }
}
