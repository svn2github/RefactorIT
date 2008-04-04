/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.subtypes;

import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 *
 *
 * @author Anton Safonov
 */
public class SubtypesNode extends BinTreeTableNode {
  private String name = null;

  public SubtypesNode(Object bin) {
    super(bin, false);
  }

  public String getDisplayName() {
    if (this.name == null) {
      if (getBin() instanceof BinType) {
        this.name = BinFormatter.formatQualified(((BinType) getBin()).getTypeRef());
      } else if (getBin() instanceof BinTypeRef) {
        this.name = BinFormatter.formatQualified((BinTypeRef) getBin());
      } else {
        this.name = super.getDisplayName();
      }
    }

    return this.name;
  }
}
