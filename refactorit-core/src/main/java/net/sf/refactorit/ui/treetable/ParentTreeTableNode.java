/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.writer.TableFormat;
import net.sf.refactorit.ui.treetable.writer.TableLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;



/**
 * Node with children for the JTreeTable component.
 * Note: child manipulation code does NOT notify model change listeners.
 *
 * @author Anton Safonov
 * @author Risto Alas
 * @author Igor Malinin
 */
public abstract class ParentTreeTableNode implements UITreeNode {
  public static final boolean DEFAULT_SHOW_HIDDEN_CHILDREN = false;

  private final ArrayList children = new ArrayList();

  private boolean hiddenChildrenVisible = DEFAULT_SHOW_HIDDEN_CHILDREN;

  private ParentTreeTableNode parent;
  private boolean hidden;

  /** Root nodes must *not* be hidden */
  public final void setHiddenRecursively(final boolean hidden) {
    setHiddenRecursively(hidden, 0);
  }

  private final void setHiddenRecursively(final boolean hidden, int depth){
    setHidden(hidden);

    for (int i = 0, max = children.size(); i < max; i++) {
      final ParentTreeTableNode child = (ParentTreeTableNode) children.get(i);
      child.setHiddenRecursively(hidden);
    }

    if (depth == 0){
      // now, when recursion is over -- check if any parent nodes should be
      // also marked as hidden (have no visible children anymore)
      checkParentsShouldBeHidden(hidden);

      // update numbers, if there was any before
      ParentTreeTableNode rootNode = (this.getPath())[0];
      if (rootNode.getSecondaryText() != null){
        rootNode.reflectLeafNumberToParentName();
      }
    }
  }

  public final void setHidden(final boolean willBeHidden) {
    this.hidden = willBeHidden;
//    updateColor();

    invalidateCacheOfVisibleChildren();
  }

  /**
   * Checks recursively to the root if nodes have no VISIBLE children.
   * Sets them 'hidden', if true.
   */
  public final void checkParentsShouldBeHidden(final boolean hidden){
    ParentTreeTableNode parent = this.getParent();
    if (parent == null || parent.getParent() == null){
      return;
    }
    if (!parent.hasVisibleChildren()){
      parent.setHidden(true);
      parent.checkParentsShouldBeHidden(true);
    } else {
      parent.setHidden(false);
      if (!hidden){
        parent.checkParentsShouldBeHidden(false);
      }
    }
  }

  public final boolean hasVisibleChildren(){
    List children = this.getAllChildren();
    for (int i = 0; i < children.size(); i++){
      if (children.get(i) instanceof ParentTreeTableNode
          && !((ParentTreeTableNode) children.get(i)).isHidden()){
        return true;
      }
    }
    return false;
  }

  public final boolean isShowHiddenChildren() {
    return hiddenChildrenVisible;
  }

  /** Should only be called on root nodes */
  public final void setShowHiddenChildren(final boolean b) {
    hiddenChildrenVisible = b;

    for (int i = 0, max = children.size(); i < max; i++) {
      final ParentTreeTableNode child = (ParentTreeTableNode) children.get(i);
      child.setShowHiddenChildren(b);
    }
  }

  /**
   * WARNING: can be slow -- can build a new list on every call
   * @return visible children;
   */
  public final ArrayList getChildren() {
    return hiddenChildrenVisible ? getAllChildren() : notHiddenChildren();
  }

  public final void removeAllChildren() {
    children.clear();

    invalidateCacheOfVisibleChildren();
  }

  /** null value means "needs updating" */
  private ArrayList cacheOfVisibleChildren;

  private ArrayList notHiddenChildren() {
    if (cacheOfVisibleChildren == null) {
      ArrayList allChildren = getAllChildren();
      cacheOfVisibleChildren = new ArrayList(allChildren.size());
      for (int i = 0, max = allChildren.size(); i < max; i++) {
        ParentTreeTableNode child = (ParentTreeTableNode) allChildren.get(i);

        if (!child.isHidden()) {
          cacheOfVisibleChildren.add(child);
        }
      }
    }

    return cacheOfVisibleChildren;
  }

  protected void invalidateCacheOfVisibleChildren() {
    cacheOfVisibleChildren = null;
    ParentTreeTableNode curParent = getParent();
    if (curParent != null) {
      curParent.invalidateCacheOfVisibleChildren();
    }
  }

  public final int getChildCount() {
    return getChildren().size(); // getChildren() might not return this.children
  }

  public final Object getChildAt(final int num) {
    return getChildren().get(num); // getChildren() might not return this.children
  }

  public final int getIndex(final Object child) {
    return getChildren().indexOf(child);
  }

  public final void addChild(final Object child) {
    addChild(child, false);
  }

  public final void addChild(final Object child, boolean addAsFirstChild) {
    if (Assert.enabled) {
      //Assert.must( ! children.contains( child ), "Adding a child for the second time: " + child );
    }

    if(children.contains(child)) {
      return;
    }

    if(addAsFirstChild) {
      children.add(0, child);
    } else {
      /*boolean result = */
      children.add(child);
    }

    if (Assert.enabled) {
      //Assert.must(result, "Failed to add child: " + child);
    }

    if (child instanceof ParentTreeTableNode) {
      ParentTreeTableNode node = (ParentTreeTableNode) child;
      node.parent = this;
      node.checkParentsShouldBeHidden(node.isHidden());
    }

    invalidateCacheOfVisibleChildren();

    setSelectedParents();
  }

  public final void removeChild(final Object child) {
    /*boolean result = */children.remove(child);

    if (Assert.enabled) {
      //Assert.must(result, "Failed to remove child: " + child);
    }
    if (child instanceof ParentTreeTableNode){
      ((ParentTreeTableNode) child).checkParentsShouldBeHidden(false);
    }
    invalidateCacheOfVisibleChildren();
  }

  public final ParentTreeTableNode getParent() {
    return this.parent;
  }

  public final boolean isClassmodelType() {
    switch (getType()) {
      case NODE_NON_JAVA:
      case NODE_SOURCE:
      case NODE_UNKNOWN:
        return false;
      default:
        return true;
    }
  }

  public final String getFullName() {
    if (this.parent != null && this.parent.getParent() != null // root is never part of package structure
        && this.parent.isClassmodelType()) {
      return this.parent.getFullName() + '.' + getDisplayName();
    } else {
      return getDisplayName();
    }
  }

  /**
   * Builds the parents of node up to and including the root node,
   * where the original node is the last element in the returned array.
   * The length of the returned array gives the node's depth in the
   * tree.
   */
  public final ParentTreeTableNode[] getPath() {
    final List nodes = new ArrayList();

    // NOTE: of course, this is inefficient, so intended to be used rarely
    if (getParent() != null) {
      nodes.addAll(Arrays.asList(getParent().getPath()));
    }
    nodes.add(this);

    return (ParentTreeTableNode[]) nodes.toArray(
        new ParentTreeTableNode[nodes.size()]);
  }

  protected final ParentTreeTableNode findChild(final String name) {
    return findChild(name, true);
  }

  public final ParentTreeTableNode findChild(
      final String name, final boolean descend
      ) {
    ParentTreeTableNode node;

    final List children = getAllChildren();
    for (int i = 0, max = children.size(); i < max; i++) {
      node = (ParentTreeTableNode) children.get(i);
      if (node.getDisplayName().equals(name)) {
        return node;
      }

      final Object bin = node.getBin();
      if (bin instanceof BinPackage &&
          name.equals(((BinPackage) bin).getQualifiedName())
          ) {
        return node;
      }

      if (bin instanceof BinType
          && name.equals(((BinType)bin).getNameWithLocals(true))) {
        return node;
      }

      if (bin instanceof String && bin.equals(name)) {
        return node;
      }

      if (descend) { // has children
        node = node.findChild(name, descend);
        if (node != null) {
          return node;
        }
      }
    }

    return null;
  }

  public abstract void setDisplayName(String name);

  public abstract void setSecondaryText(String text);

  public final int reflectLeafNumberToParentName() {
    ParentTreeTableNode node;

    int count = 0;

    final ArrayList children = getChildren();
    for (int i = 0, max = children.size(); i < max; i++) {
      node = (ParentTreeTableNode) children.get(i);
      if (node.getChildCount() == 0) {
        count++;
      } else {
        count += node.reflectLeafNumberToParentName();
      }
    }

    setSecondaryText("(" + count + ")");

    return count;
  }

  public final void removeSecondaryText() {
    setSecondaryText(null);

    // Recursive call
    final List children = getAllChildren();
    for (int i = 0; i < children.size(); i++) {
      final ParentTreeTableNode child = (ParentTreeTableNode) children.get(i);
      child.removeSecondaryText();
    }
  }

  /** To create unique TreePathReference */
  public String getIdentifier() {
    String result = getDisplayName() + getLineNumber()
        + StringUtil.removeHtml(getLineSource());
    return result;
  }

  public String toString() {
    String result = getDisplayName();
    String text = getSecondaryText();
    if (text != null) {
      result += " " + text;
//    result += (isHidden() ? "(hidden)" : "")
//          + " - " + getLineSource();
    }
    return result;
  }

  /** @return visible and hidden children */
  public final ArrayList getAllChildren() {
    return this.children;
  }

  /** @return amount of visible and hidden children */
  final int getAllChildrenCount() {
    return this.children.size();
  }

  public final boolean isHidden() {
    return hidden;
  }

  public abstract String getLineNumber();

  public abstract String getLineSource();

  // Formatting code

  //FIXME: preserve FONT tags to get pretty HTML
  public final String toClipboardFormat(TableFormat writer,
      final BinTreeTableModel model) {

    final StringBuffer result = new StringBuffer();

    writer.startNewLine(result);
    writer.addColumns(TableLayout.createColumnContents(model, this), result, false);
    writer.endLine(result);

    return result.toString();
  }

  public final String getValue(final BinTreeTableModel model, final int column) {
    final Object value = model.getValueAt(this, column);

    if (value == null) {
      return "";
    } else {
      return StringUtil.removeHtml(value.toString());
    }
  }

  public final String getNameForTextOutput() {
    // if we dont have a parent or it isnt a BinItem then we have to show more
    // information such as the whole package name or the class name including
    // package.
    if (getParent() == null || !(getParent().getBin() instanceof BinItem)) {
      final Object bin = getBin();
      if (bin instanceof BinPackage) {
        return ((BinPackage) bin).getQualifiedDisplayName();
      }

      if (bin instanceof BinMember) {
        return ((BinMember) bin).getQualifiedName();
      }

      if (bin instanceof CompilationUnit) {
        return ((CompilationUnit) bin).getName();
      }
    }

    String result = getDisplayName();

    String text = getSecondaryText();
    if (text != null) {
      result += " " + text;
    }

    return result;
  }

  public final String getNameType(Object o) {
    if (o instanceof BinTypeRef) {
      o = ((BinTypeRef) o).getBinType();
    }

    if (o instanceof BinPackage) {
      return "Package";
    } else if (o instanceof BinMember) {
      return StringUtil.capitalizeFirstLetter(((BinMember) o).getMemberType());
    } else {
      return "";
    }
  }

  public final StringBuffer collectClipboardTextRecursively(
      TableFormat writer, final BinTreeTableModel model,
      final Set processedNodes
      ) {
    if (processedNodes.contains(this)) {
      return null;
    }

    processedNodes.add(this);

    final StringBuffer buf = new StringBuffer(100);
    buf.append(toClipboardFormat(writer, model));

    final ArrayList children = getChildren();
    for (int i = 0; i < children.size(); i++) {
      final Object node = children.get(i);

      TableLayout.collectClipboardText(writer, model, buf, processedNodes, node);
    }

    return buf;
  }

  protected boolean greyed;

  public final boolean isGreyed() {
    return greyed;
  }

  protected boolean selected = true;

  public final void toggle() {
    setSelected(!selected);
  }

  public void setSelected(boolean selected) {
    setSelectedChildren(selected);
    if (parent != null) {
      parent.setSelectedParents();
    }
  }

  void setSelectedChildren(boolean selected) {
    this.greyed = false;
    this.selected = selected;

    for (int i = 0, max = getChildCount(); i < max; i++) {
      ((ParentTreeTableNode) getChildAt(i)).setSelectedChildren(selected);
    }
  }

  void setSelectedParents() {
    boolean off = false, on = false;

    for (int i = 0, max = getChildCount(); i < max; i++) {
      ParentTreeTableNode node = ((ParentTreeTableNode) getChildAt(i));

      if (node.selected) {
        on = true;
        if (node.greyed) {
          off = true;
        }
      } else {
        off = true;
      }
    }

    this.greyed = on && off;
    this.selected = on;

    if (parent != null) {
      parent.setSelectedParents();
    }
  }

  public final boolean isSelected() {
    return selected;
  }

  private boolean isCheckBoxNeeded = true;

  public final void setShowCheckBox(final boolean isCheckBoxNeeded) {
    this.isCheckBoxNeeded = isCheckBoxNeeded;
  }

  public final boolean isCheckBoxNeeded() {
    return isCheckBoxNeeded;
  }
}
