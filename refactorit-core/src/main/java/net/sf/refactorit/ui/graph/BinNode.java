/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.graph;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.ui.tree.NodeIcons;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGPanel;

import javax.swing.Icon;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;


/**
 * @author Anton Safonov
 */
public abstract class BinNode extends Node {
  private Object bin;
  private boolean visited = false;
//  private BinItemReference projectReference;

  static {
    setNodeBorderInactiveColor(Color.gray);
    setNodeBorderMouseOverColor(Color.black);
    setNodeBorderDragColor(Color.darkGray);

    setNodeTextColor(Color.black);
  }

  public static final BinNode createFor(TGPanel panel,
      BinItem bin, String id, String name) throws TGException {
    BinNode node = null;
    switch (BinClassificator.getItemType(bin, true)) {
      case BinClassificator.PACKAGE:
        node = new PackageNode(bin, id, name);
        break;
      case BinClassificator.TYPE:
        node = new TypeNode(bin, id, name);
        break;
      default:
        node = new MemberNode(bin, id, name);
        break;
    }

    panel.addNode(node);
    return node;
  }

  protected BinNode(BinItem bin, String id, String name) {
    super(id, name);
    this.bin = bin;
  }

  public BinItem getBin() {
    if (this.bin instanceof BinItemReference) {
      return null;
//      Project proj = (Project) this.projectReference.restore(null);
//      return (BinItem) ((BinItemReference)this.bin).restore(proj);
    }
    return (BinItem)this.bin;
  }

  public Icon getIcon() {
    return null;
  }

  public int getExtraHeaderSpace() {
    Icon icon = getIcon();
    if (icon != null) {
      return icon.getIconWidth() + 3;
    }
    return 0;
  }

  public void paintNodeBody(Graphics g, TGPanel tgPanel) {
    
    boolean isOver = tgPanel.getMouseOverN() == this;
    if(isOver) {
      Iterator it = this.getEdges();
      if(it!=null){
        while(it.hasNext()) {
          BinEdge edge = (BinEdge)it.next();
          edge.setEdgeDefaultColor(Color.GRAY);
          edge.paint(g, tgPanel);
          Node node = edge.from.equals(this)? edge.to: edge.from;
          node.paint(g, tgPanel);
        }
      }
    }
    
    super.paintNodeBody(g, tgPanel);
    Icon icon = getIcon();
    if (icon != null) {
      icon.paintIcon(tgPanel, g, (int) (drawx - getWidth() / 2 + 3),
          (int) drawy - icon.getIconHeight() / 2);
    }
  }

  public Object detouchFromClassmodel() {
    this.bin = getBinItemReference();
    return this.bin;
  }

  public BinItemReference getBinItemReference() {
    if (!(this.bin instanceof BinItemReference)) {
//      try {
//        this.projectReference = Project.getProjectFor(this.bin).createReference();
//      } catch (NullPointerException e) {
//        System.err.println("bin: " + this.bin + " - " + this.bin.getClass());
//      }
      return BinItemReference.create(this.bin);
    }

    return (BinItemReference) this.bin;
  }


  public abstract int getItemType();

  public final BinEdge fastFindEdge(final BinNode toNode) {
    /*for (int i = 0, max = this.edgeCount(); i < max; i++) {
      BinEdge e = (BinEdge)this.edgeAt(i);
      if (e.getTo() == toNode) {
        return e;
      }
    }*/
    Edge e = super.fastFindEdge(toNode);
    if(e instanceof BinEdge) {
      return (BinEdge)e;
    }

    return null;
  }
  
  public boolean isVisited() {
    return visited;
  }

  public void setVisited(boolean visited) {
    this.visited = visited;
  }
}


class PackageNode extends BinNode {
  private static final Color PACKAGE_BACK_COLOR = new Color(108, 217, 170);
  private static final Color BINARY_PACKAGE_BACK_COLOR = new Color(0, 170, 170);
  private static final Icon packageIcon
      = NodeIcons.getNodeIcons().getPackageIcon(true);

  protected PackageNode(BinItem bin, String id, String name) {
    super(bin, id, name);
    setType(Node.TYPE_ROUNDRECT);
    if (((BinPackage) bin).hasTypesWithSources()) {
      setBackColor(PACKAGE_BACK_COLOR);
    } else {
      setBackColor(BINARY_PACKAGE_BACK_COLOR);
    }
  }

  public int getItemType() {
    return BinClassificator.PACKAGE;
  }

  public Icon getIcon() {
    return packageIcon;
  }
}


class TypeNode extends BinNode {
  private static final Color TYPE_BACK_COLOR = new Color(192, 192, 236);
  private Icon icon = null;

  protected TypeNode(BinItem bin, String id, String name) {
    super(bin, id, name);
    setBackColor(TYPE_BACK_COLOR);
  }

  public int getItemType() {
    return BinClassificator.TYPE;
  }

  public Icon getIcon() {
    if (icon == null) {
      if (getBin() instanceof BinClass) {
        icon = NodeIcons.getNodeIcons().getClassIcon(
            ((BinMember) getBin()).getModifiers());
      } else {
        icon = NodeIcons.getNodeIcons().getInterfaceIcon(
            ((BinMember) getBin()).getModifiers());
      }
    }
    return icon;
  }
}


class MemberNode extends BinNode {
  private static final Color METHOD_BACK_COLOR = new Color(255, 192, 128);
  private static final Color FIELD_BACK_COLOR = new Color(225, 162, 98);
  private Icon icon;

  protected MemberNode(BinItem bin, String id, String name) {
    super(bin, id, name);
    if (bin instanceof BinField) {
      setBackColor(FIELD_BACK_COLOR);
    } else {
      setBackColor(METHOD_BACK_COLOR);
    }
  }

  public int getItemType() {
    return BinClassificator.MEMBER;
  }

  public Icon getIcon() {
    if (icon == null) {
      BinMember bin = (BinMember) getBin();
      if (bin instanceof BinField) {
        icon = NodeIcons.getNodeIcons().getFieldIcon(bin.getModifiers());
      } else if (bin instanceof BinConstructor || bin instanceof BinInitializer) {
        icon = NodeIcons.getNodeIcons().getConstructorIcon(bin.getModifiers());
      } else {
        icon = NodeIcons.getNodeIcons().getMethodIcon(bin.getModifiers());
      }
    }
    return icon;
  }
}
