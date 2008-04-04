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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.graph.BinClassificator;
import net.sf.refactorit.ui.graph.BinEdge;
import net.sf.refactorit.ui.graph.BinNode;

import com.touchgraph.graphlayout.GLPanel;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Anton Safonov
 */
class DependencyGraphBuilder extends AbstractIndexer {
  private int focusLevel;
  private int focusRadius;
  private GLPanel panel;
  private List target;
  private Map nodes = new HashMap();

  public DependencyGraphBuilder(GLPanel panel, List target, Project project,
      boolean moreDetails) {
    this.panel = panel;
    this.target = target;
    if (this.target.size() == 0) {
      return;
    }
    calculateFocusLevel();
    Object focusable = target.get(0);
    for (Iterator it = this.target.iterator(); it.hasNext(); ) {
      BinItemVisitable item = (BinItemVisitable) it.next();
      if (BinClassificator.getItemType(item, true) == this.focusLevel) {
        focusable = item;
        break;
      }
    }
    if (moreDetails) {
      if (this.focusRadius < 1) {
        ++this.focusRadius;
      }
    }

    addNodes();
    createDependencyEdges(project);
//    setUpLocale();

    this.panel.getTGPanel().setSelect((Node)this.nodes.get(focusable));

//    detouchFromClassmodel();

    this.nodes.clear();
  }

//  private void detouchFromClassmodel() {
//    for (Iterator visitables = this.nodes.values().iterator();
//        visitables.hasNext(); ) {
//      BinNode node = (BinNode) visitables.next();
//      node.detouchFromClassmodel();
//    }
//  }

  private void addNodes() {
    for (Iterator visitables = this.target.iterator(); visitables.hasNext(); ) {
      BinItemVisitable visitable = (BinItemVisitable) visitables.next();
      visitable.accept(this);
    }
  }

  private final void calculateFocusLevel() {
    this.focusLevel = Integer.MAX_VALUE;
    this.focusRadius = 0;
    for (Iterator it = this.target.iterator(); it.hasNext(); ) {
      BinItemVisitable item = (BinItemVisitable) it.next();
      int type = BinClassificator.getItemType(item, true);
      if (type == BinClassificator.PROJECT) {
        type = BinClassificator.PACKAGE;
      }
      if (this.focusLevel != Integer.MAX_VALUE) { // inited already
        this.focusRadius = Math.max(this.focusRadius,
            Math.abs(this.focusLevel - type));
      }
      if (type < this.focusLevel) {
        this.focusLevel = type;
      }
    }
    if (this.focusLevel == Integer.MAX_VALUE) { // strange, actually
      this.focusLevel = BinClassificator.TYPE;
      this.focusRadius = 1;
    }
  }

  private boolean isMustAdd(BinItem item) {
    if (item instanceof BinCIType) {
      BinCIType ciType = (BinCIType)item;
        if(!ciType.isFromCompilationUnit() 
            || ciType.isTypeParameter()) {
          return false;
        }
    }

    int diff = this.focusLevel - BinClassificator.getItemType(item, true);
    return Math.abs(diff) <= this.focusRadius;
  }

  private void createDependencyEdges(final Project project) {
    Set addedDeps = new HashSet();

    final Set keys = new HashSet(this.nodes.keySet());
    for (Iterator visitables = keys.iterator(); visitables.hasNext(); ) {
      BinItemVisitable dependable = (BinItemVisitable) visitables.next();
      List deps = DependenciesModel.collectDependencies(dependable, project);
      for (int i = 0, max = deps.size(); i < max; i++) {
        InvocationData data = (InvocationData) deps.get(i);
        if (addedDeps.contains(data)) {
          continue;
        }
        addedDeps.add(data);
        BinNode dependsOnNode = findNodeIncludingParents(data.getWhat());
        if (dependsOnNode != null) {
//          if (dependsOnNode.getID().equals("")) {
//              System.err.println("data: " + data);
//          }

          BinNode dependableNode
              = findNodeIncludingParents(data.getWhereMember());
          BinEdge.createFor(
              this.panel.getTGPanel(), dependableNode, dependsOnNode, false);
        }
      }
    }
  }

  private BinNode findNodeIncludingParents(final BinItemVisitable bin) {
    BinItemVisitable cur = bin;
    while (cur != null && !(cur instanceof Project)) {
      BinNode node = (BinNode)this.nodes.get(cur);
      if (node != null) {
        return node;
      }
      if (cur instanceof BinPackage
          && isAllowedPackage((BinPackage) cur)
          && (focusLevel == BinClassificator.PROJECT
          || (focusLevel == BinClassificator.PACKAGE
          && isInTargetScope((BinPackage) cur)))) {
        return getNode((BinPackage) cur, ((BinPackage) cur).getQualifiedName(),
            ((BinPackage) cur).getQualifiedDisplayName());
      }
      cur = BinClassificator.getParent(cur);
    }

    return null;
  }

  public static boolean isAllowedPackage(final BinPackage pack) {
    if (pack == null || pack.hasTypesWithSources()) {
      return true; // shortcut
    }

    if (GlobalOptions.getOptionAsBoolean("dependencies-ignore-binary-packages", true)) {
      return false;
    }

    if (GlobalOptions.getOptionAsBoolean("dependencies-ignore-jdk-packages", true)
        && (pack.getQualifiedName().startsWith("java.")
        || pack.getQualifiedName().startsWith("javax."))) {
      return false;
    }

    return true;
  }

  private boolean isInTargetScope(final Scope pack) {
    for (Iterator targs = this.target.listIterator(); targs.hasNext(); ) {
      Object targ = targs.next();
      if (targ instanceof Scope && ((Scope) targ).contains(pack)) {
        return true;
      }
    }

    return false;
  }

  private void setUpLocale() {
    for (Iterator visitables = this.target.iterator(); visitables.hasNext(); ) {
      BinItemVisitable visitable = (BinItemVisitable) visitables.next();
      BinNode node = (BinNode)this.nodes.get(visitable);
      if (node.getItemType() == this.focusLevel) {
        try {
          this.panel.getTGPanel().setLocale(node, 3, 1000, 10, false);
        } catch (TGException e) {
        }
      }
    }
  }

  public void visit(CompilationUnit x) {
    getNode(x.getPackage(), x.getPackage().getQualifiedName(),
        x.getPackage().getQualifiedDisplayName());
    super.visit(x);
  }

  public void visit(BinPackage x) {
    getNode(x, x.getQualifiedName(), x.getQualifiedDisplayName());
    x.defaultTraverseWithSubpackages(this);
  }

  public void visit(BinCIType x) {
    if (isMustAdd(x)) {
      String name = (x.isLocal())?
          x.getNameWithLocals(false):x.getNameWithAllOwners();
      addChildNode(x, getIdFor(x), name);
      super.visit(x);
    }
  }

  public void visit(BinMethod x) {
    if (isMustAdd(x)) {
      addChildNode(x, getIdFor(x)
          + BinFormatter.formatMethodParameters(x),
          BinFormatter.formatWithoutReturn(x));
    }
    super.visit(x);
  }

  public void visit(BinConstructor x) {
    if (!x.isSynthetic() && isMustAdd(x)) {
      addChildNode(x, getIdFor(x)
          + BinFormatter.formatMethodParameters(x),
          BinFormatter.formatWithoutReturn(x));
    }
    super.visit(x);
  }

  public void visit(BinField x) {
    if (isMustAdd(x)) {
      addChildNode(x, getIdFor(x), x.getName());
    }
    super.visit(x);
  }

  public void visit(BinInitializer x) {
    if (isMustAdd(x)) {
      addChildNode(x, getIdFor(x), x.getName());
    }
    super.visit(x);
  }

  private String getIdFor(BinMember member) {
    BinType ownerType = (member instanceof BinType)? (BinType)member :
        member.getOwner().getBinType();
    String id = ownerType.isLocal()? ownerType.getNameWithLocals(true):
      ownerType.getQualifiedName();
    if(!(member instanceof BinType)) {
      id += ("." + member.getName());
    }
    return id;
  }
 
  
  private BinNode getNode(BinItem x, String id, String name) {
    try {
      BinNode node = (BinNode)this.nodes.get(x);
      if (node == null) {
        node = BinNode.createFor(this.panel.getTGPanel(), x, id, name);
        this.nodes.put(x, node);
      }
      return node;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private BinNode addChildNode(BinMember x, String id, String name) {
    BinNode child = getNode(x, id, name);
    BinItemVisitable parentOf = BinClassificator.getParent(x);
    if (parentOf != null) {
      BinNode parent = (BinNode)this.nodes.get(parentOf);
      BinEdge.createFor(this.panel.getTGPanel(), child, parent, true);
    }

    return child;
  }
}
