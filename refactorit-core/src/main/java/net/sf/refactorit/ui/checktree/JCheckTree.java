/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.checktree;


import net.sf.refactorit.ui.options.profile.ProfilePanel;
import net.sf.refactorit.ui.tree.BinTree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A tree that displays it's nodes with checkboxes. Based on <a
 * href="http://www.fawcette.com/archives/premier/mgznarch/javapro/2001/01jan01/vc0101/vc0101.asp">
 * code</a> by Claude Duguay.
 */
public class JCheckTree extends BinTree {
	public JCheckTree(TreeModel newModel) {
		super(newModel);
		setRenderers();
	}

	public JCheckTree(TreeNode root) {
		super(root);
		setRenderers();
	}

	private void setRenderers() {
		super.setCellRenderer(new CheckTreeCellRenderer(this));

		addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent ke) {
				final int kc = ke.getKeyCode();
				if (kc == KeyEvent.VK_SPACE) {
					ke.consume();

					int[] rows = getSelectionRows();
					if (rows == null || rows.length != 1) {
						return;
					}
					int row = rows[0];
					TreePath path = getPathForRow(row);
					if (path == null) {
						return;
					}

					toggle(path);
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(final MouseEvent e) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}

				Rectangle rect = getPathBounds(path);
				rect.width = 16;
				if (rect.contains(e.getPoint())) {
					toggle(path);
				}
			}
		});
	}

	void toggle(TreePath path) {
		CheckTreeNode node = (CheckTreeNode) path.getLastPathComponent();
		if (node.isShowCheckBox()) {
			node.setSelected(!node.isSelected());
		}
    //if(!(node instanceof ProfilePanel.TreeNode))
		updateSelections((CheckTreeNode) path.getPathComponent(0));
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.nodeChanged((TreeNode) path.getLastPathComponent());
	}
 
	public static int updateSelections(CheckTreeNode a){
    if(a instanceof ProfilePanel.TreeNode)
      return a.isSelected() ? 1 : -1;
    //-1: not sel; 0: grey; 1: fully sel.
		int result = -1;
		int total = a.getChildCount();
		int children = 0;
		boolean fullySelected = true, selected = false;
		if (a.isLeaf())
			return a.isSelected() ? 1 : -1;
		else{
			for (int i = 0; i < total; i++){
				int current = updateSelections((CheckTreeNode) a.getChildAt(i));
				if(current > -1)
					selected = true;
				if(current < 1)
					fullySelected = false;
      }
    }
//    if(!(a instanceof ProfilePanel.TreeNode)){
		if(selected){
			a.setOnlyParentSelected(true);
			if(!fullySelected){
				result = 0;
				a.setFullySelected(false);
			}else{
				a.setFullySelected(true);
				result = 1;
			}
		}else{
			result = -1;
			a.setFullySelected(true);
			a.setSelected(false);
    }
//    }else
//      a.setOnlyParentSelected(!selected);
		/*if(!result)
			a.setSelected(false);
		else
			a.setOnlyParentSelected(true);*/
		return result;
	}

	public void setCellRenderer(TreeCellRenderer renderer) {
		super.setCellRenderer(new CheckTreeCellRenderer(this, renderer));
	}
}
