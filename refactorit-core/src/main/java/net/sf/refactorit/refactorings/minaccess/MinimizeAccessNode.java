/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.minaccess;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.List;


/**
 * @author vadim
 */
public class MinimizeAccessNode extends BinTreeTableNode {
  private BinMember member;
  private int[] stricterAccess;
  private String currentAccess;
  private String selectedAccess;
  private MinimizeAccessNode nextNode;
  private MinimizeAccessNode previousNode;
  MinimizeAccessTableModel model;

  public MinimizeAccessNode(BinMember member, int[] stricterAccess,
      MinimizeAccessTableModel model) {
    super(member, false);

    this.member = member;
    this.currentAccess = 
      new BinModifierFormatter(member.getAccessModifier(), true).print();

    int strictest = stricterAccess[stricterAccess.length - 1];

    this.selectedAccess = new BinModifierFormatter(strictest, true).print();
    this.stricterAccess = stricterAccess;
    this.model = model;

    setSelected(false);
  }

  public String getCurrentAccessName() {
    return currentAccess;
  }

  public String getMinimalAccessName() {
    int strictest = stricterAccess[stricterAccess.length - 1];
    return new BinModifierFormatter(strictest, true).print();
  }

  public Object[] getStricterAccessesAsStrings() {
    List names = new ArrayList();

    for (int i = 0; i < stricterAccess.length; i++) {
      names.add(new BinModifierFormatter(stricterAccess[i], true).print());
    }

    return names.toArray();
  }

  public int[] getStricterAccesses() {
    return stricterAccess;
  }

  public void setStricterAccesses(int[] stricterAccess) {
    this.stricterAccess = stricterAccess;
    int strictest = stricterAccess[stricterAccess.length - 1];
    this.selectedAccess = new BinModifierFormatter(strictest, true).print();
  }

  public String getSelectedAccess() {
    return selectedAccess;
  }

  public void setSelectedAccess(Object value) {
    if (selectedAccess.equals(value)) {
      return;
    }

    selectedAccess = (String) value;

    if (nextNode != null) {
      nextNode.setSelectedAccess(value);
    }

    if (previousNode != null) {
      previousNode.setSelectedAccess(value);
    }

    model.fireTableStructureChanged();
  }

  public void setSelected(boolean selected) {
    boolean isSelected = selected;
    if (isSelected == isSelected()) {
      return;
    }

    super.setSelected(isSelected);

    if (nextNode != null) {
      nextNode.setSelected(selected);
    }

    if (previousNode != null) {
      previousNode.setSelected(selected);
    }

    model.fireTableStructureChanged();
  }

  public void setNextNode(MinimizeAccessNode nextNode) {
    this.nextNode = nextNode;

    int[] stricterAccessOfNext = nextNode.getStricterAccesses();
    if (stricterAccessOfNext.length > stricterAccess.length) {
      nextNode.setStricterAccesses(stricterAccess);
    } else if (stricterAccessOfNext.length < stricterAccess.length) {
      MinimizeAccessNode node = this;
      do {
        node.setStricterAccesses(stricterAccessOfNext);
        node = node.getPreviousNode();
      } while (node != null);
    }
  }

  public void setPreviousNode(MinimizeAccessNode previousNode) {
    this.previousNode = previousNode;
  }

  public MinimizeAccessNode getNextNode() {
    return nextNode;
  }

  public MinimizeAccessNode getPreviousNode() {
    return previousNode;
  }

  public String toString() {
    return member.getName();
  }

// for tests only
  public BinMember getMember() {
    return member;
  }
}
