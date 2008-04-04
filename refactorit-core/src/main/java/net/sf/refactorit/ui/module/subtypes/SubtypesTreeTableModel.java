/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.subtypes;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class SubtypesTreeTableModel extends BinTreeTableModel {
  private BinCIType target = null;

  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(SubtypesTreeTableModel.class);

  public SubtypesTreeTableModel(Project project, Object target) {
    super(new BinTreeTableNode(target));

    this.target = (BinCIType) target;

    String name = MessageFormat.format(
        resLocalizedStrings.getString("tree.root"),
        new Object[] {this.target.getQualifiedName()}
        );

    ((BinTreeTableNode) getRoot()).setDisplayName(name);

    this.target.getProject().discoverAllUsedTypes();

    populateTree((BinTreeTableNode) getRoot(), this.target);

    ((BinTreeTableNode) getRoot()).sortAllChildren();

    if (Assert.enabled && false) {
      BinTreeTableNode depends = new BinTreeTableNode("Dependables");
      ((BinTreeTableNode) getRoot()).addChild(depends);
      Iterator dependables = ((DependencyParticipant) this.target.getTypeRef()).getDependables().iterator();
      while (dependables.hasNext()) {
        depends.addChild(new BinTreeTableNode(dependables.next()));
      }
      depends.sortAllChildren();
    }
  }

  private Set visited = new HashSet();

  private void populateTree(BinTreeTableNode parent, BinCIType type) {
    if (type instanceof BinArrayType) {
      return;
    }
    //System.err.println("-->Type: "+type.getQualifiedName());
    BinTreeTableNode node = null;
    if (type.equals(target)) {
      node = parent;
    } else {
      node = parent.findChildByType(type);
      if (node != null) {
        return;
      }

      node = new SubtypesNode(type);
      parent.addChild(node);
    }

    visited.add(type);

    Iterator subs = type.getTypeRef().getDirectSubclasses().iterator();
    while (subs.hasNext()) {
      final BinCIType nextType = ((BinTypeRef) subs.next()).getBinCIType();
      if (!visited.contains(nextType)) {
        populateTree(node, nextType);
      }
    }

    visited.remove(type);
  }
}
