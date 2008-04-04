/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.checktree;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Enumeration;


/**
 * A tree node for @link{JCheckTree}.
 * Based on <a href="http://www.fawcette.com/archives/premier/mgznarch/javapro/2001/01jan01/vc0101/vc0101.asp">
 * code</a> by Claude Duguay.
 */
public class CheckTreeNode extends DefaultMutableTreeNode {
  protected boolean selected, propagate;
  protected boolean showCheckBox = true;
	private boolean full = true;
	
	public boolean isFullySelected(){
		return full;
	}
	
	public void setFullySelected(boolean full){
		this.full = full;
	}

  public CheckTreeNode(Object data) {
    this(data, false, true);
  }

  public CheckTreeNode(Object data, boolean selected) {
    this(data, selected, true);
  }

  public CheckTreeNode(Object data, boolean selected, boolean propagate) {
    super(data);

    this.selected = selected;
    this.propagate = propagate;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
    if (propagate) {
      propagateSelected(selected);
    }
  }
	
	public void setOnlyParentSelected(boolean selected){
		this.selected = selected;
	}

  public void propagateSelected(boolean selected) {
    Enumeration enumer = children();
    while (enumer.hasMoreElements()) {
      CheckTreeNode node = (CheckTreeNode) enumer.nextElement();
      node.setSelected(selected);
    }
  }

  public void setUserObject(Object obj) {
    if (obj == this) {
      return;
    }
    super.setUserObject(obj);
  }

  public boolean isShowCheckBox() {
    return this.showCheckBox;
  }

  public void setShowCheckBox(final boolean showCheckBox) {
    this.showCheckBox = showCheckBox;
  }

}
