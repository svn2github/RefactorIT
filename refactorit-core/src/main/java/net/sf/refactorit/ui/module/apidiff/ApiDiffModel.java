/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.apisnapshot.Snapshot;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotBuilder;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotDiff;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotItem;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


public class ApiDiffModel extends BinTreeTableModel {
  static final ResourceBundle bundle =
      ResourceUtil.getBundle(ApiDiffModule.class);

  private HashMap packageNodes = new HashMap();
  private HashMap classNodes = new HashMap();
  private HashMap methodNodes = new HashMap();
  private HashMap constructorNodes = new HashMap();
  private HashMap fieldNodes = new HashMap();

  private static final int ADDED_ITEM = 1;
  private static final int REMOVED_ITEM = 2;
  private static final int NORMAL_ITEM = 3;

  // The values also indicate display sorting order
  private static final int FIELD = 3;
  private static final int CONSTRUCTOR = 2;
  private static final int METHOD = 1;
  private static final int OTHER = 0;

  private Snapshot binItemsInCurrentCode;
  private SnapshotDiff diff;

  public ApiDiffModel(SnapshotDiff diff, Project project) {
    super(new DiffNode(NORMAL_ITEM,
        getNameForRootNode(diff), getNameForRootNode(diff), null,
        getNameForRootNode(diff), diff.getRemoved(), OTHER));

    this.binItemsInCurrentCode = new SnapshotBuilder(true)
        .createSnapshot(new Object(), "", Calendar.getInstance(), project);

    this.diff = diff;

    createNodes(this.diff.getRemoved(), REMOVED_ITEM);
    createNodes(this.diff.getAdded(), ADDED_ITEM);

    final DiffNode root = (DiffNode) getRoot();

    root.mergeChildren();
    root.reflectLeafAddedRemovedNumberToParentName();
    root.sortAllChildren(DiffNodeComparator.instance);
  }

  private static String getNameForRootNode(SnapshotDiff diff) {
    if (diff.getAdded().size() != 0 || diff.getRemoved().size() != 0) {
      return bundle.getString("action.name");
    }

    return bundle.getString("action.name") +
        " (" + bundle.getString("nothing.changed") + ")";
  }

  /**
   * @param  nodeType is ADDED_ITEM, REMOVED_ITEM or NORMAL_ITEM.
   *                  Currently this is also used as a prefix for the title that's displayed
   *                  to user, but that will change.
   */
  private void createNodes(List diffItems, int nodeType) {
    for (int i = 0; i < diffItems.size(); i++) {
      SnapshotItem item = (SnapshotItem) diffItems.get(i);

      if (item.isPackage()) {
        getNodeForPackage(nodeType, item.getDescription());
      } else if (item.isMethod()) {
        getNodeForMethod(nodeType, item.getDescription(), item.getOwner(),
            item.getOwnerOfOwner());
      } else if (item.isConstructor()) {
        getNodeForConstructor(nodeType, item.getDescription(), item.getOwner(),
            item.getOwnerOfOwner());
      } else if (item.isField()) {
        getNodeForField(nodeType, item.getDescription(), item.getOwner(),
            item.getOwnerOfOwner());
      } else if (item.isClass()) {
        getNodeForClass(nodeType, item.getDescription(), item.getOwner());
      }
    }
  }

  private DiffNode getNodeForPackage(int nodeType, String title) {
    if ("".equals(title)) {
      title = bundle.getString("default.package");

    }
    return getNode(nodeType, title, packageNodes, (DiffNode) getRoot(), OTHER);
  }

  private DiffNode getNodeForMethod(int nodeType, String title, String owner,
      String owner2) {
    return getNode(nodeType, title, methodNodes, getNodeForClass(NORMAL_ITEM,
        owner, owner2), METHOD);
  }

  private DiffNode getNodeForConstructor(int nodeType, String title,
      String owner, String owner2) {
    return getNode(nodeType, title, constructorNodes,
        getNodeForClass(NORMAL_ITEM, owner, owner2), CONSTRUCTOR);
  }

  private DiffNode getNodeForField(int nodeType, String title, String owner,
      String owner2) {
    return getNode(nodeType, title, fieldNodes, getNodeForClass(NORMAL_ITEM,
        owner, owner2), FIELD);
  }

  private DiffNode getNodeForClass(int nodeType, String title, String owner) {
    return getNode(nodeType, title, classNodes, getNodeForPackage(NORMAL_ITEM,
        owner), OTHER);
  }

  private DiffNode getNode(int nodeType, String title, HashMap nodeList,
      DiffNode parentNode, int binMemberType) {
    if (!nodeList.containsKey(nodeType + title)) {
      nodeList.put(nodeType + title, new DiffNode(nodeType,
          SnapshotItem.createTitle(title), title, parentNode,
          binItemsInCurrentCode.getBinItemForDescription(title),
          diff.getRemoved(), binMemberType));
    }

    return (DiffNode) nodeList.get(nodeType + title);
  }

  static class DiffNode extends BinTreeTableNode {
    private final int diffType;
    private final String snapshotItemDescription;
    private final List diffItems;

    private final int binMemberType;

    /**
     * @param diffType  ADDED_ITEM, REMOVED_ITEM or NORMAL_ITEM. Currently this is
     *                  also displayed to the user, but this will change.
     * @param title     displayed to user
     */
    public DiffNode(
        int diffType, String title,
        String snapshotItemDescription,
        DiffNode parentNode, Object binObject,
        List diffItems, int binMemberType
        ) {
      super(binObject, false);

      setDisplayName(title);

      this.diffType = diffType;
      this.snapshotItemDescription = snapshotItemDescription;
      this.diffItems = diffItems;
      this.binMemberType = binMemberType;

      if (parentNode != null) {
        parentNode.addChild(this);
      }
    }

    public int getType() {
      int type = super.getType();
      if (type == UITreeNode.NODE_UNKNOWN) {
        type = getTypeOfItem();
      }
      return type;
    }

    private int getTypeOfItem() {
      for (int i = 0; i < diffItems.size(); i++) {
        SnapshotItem item = (SnapshotItem) diffItems.get(i);
        if (snapshotItemDescription.equals(item.getDescription())) {
          return item.getType().intValue();
        }
      }
      return UITreeNode.NODE_UNKNOWN;
    }

    boolean isNormalNode() {
      return this.diffType == NORMAL_ITEM;
    }

    boolean isRemovedNode() {
      return this.diffType == REMOVED_ITEM;
    }

    boolean isAddedNode() {
      return this.diffType == ADDED_ITEM;
    }

    int getBinMemberType() {
      return this.binMemberType;
    }

    private DiffNode getAddedOrRemovedNode(DiffNode normalChild) {
      for (Iterator i = getChildren().iterator(); i.hasNext(); ) {
        DiffNode childNode = (DiffNode) i.next();
        if (childNode.snapshotItemDescription.equals(normalChild.
            snapshotItemDescription) &&
            childNode.diffType != normalChild.diffType) {
          return childNode;
        }
      }

      return null;
    }

    private boolean existsAddedOrRemovedNode(DiffNode child) {
      return null != getAddedOrRemovedNode(child);
    }

    private HashMap getChildrenToMerge() {
      HashMap result = new HashMap();

      for (Iterator i = getChildren().iterator(); i.hasNext(); ) {
        DiffNode child = (DiffNode) i.next();
        if (child.isNormalNode() && existsAddedOrRemovedNode(child)) {
          result.put(child, getAddedOrRemovedNode(child));
        }
      }

      return result;
    }

    /**
     * For example if there is a NORMAL_NODE and an ADDED_NODE for the same package, then
     * the NORMAL_NODE is deleted (because it is redundant), and its chlidren are moved
     * to the ADDED_NODE of the same package. The same goes for REMOVED_NODE.
     *
     * (This is recursive, also works for classes, etc.)
     */
    void mergeChildren() {
      HashMap toMerge = getChildrenToMerge();

      for (Iterator i = toMerge.keySet().iterator(); i.hasNext(); ) {
        DiffNode normalNode = (DiffNode) i.next();
        DiffNode otherNode = (DiffNode) toMerge.get(normalNode);

        moveChildren(normalNode, otherNode);
        this.removeChild(normalNode);
      }

      for (Iterator i = getChildren().iterator(); i.hasNext(); ) {
        ((DiffNode) i.next()).mergeChildren();
      }
    }

    private void moveChildren(DiffNode from, DiffNode to) {
      ArrayList children = new ArrayList(from.getChildren());

      for (Iterator i = children.iterator(); i.hasNext(); ) {
        DiffNode child = (DiffNode) i.next();
        from.removeChild(child);
        to.addChild(child);
      }
    }

    public AddedRemovedCounter reflectLeafAddedRemovedNumberToParentName() {
      AddedRemovedCounter result = new AddedRemovedCounter();

      for (int i = 0, max = this.getChildCount(); i < max; i++) {
        DiffNode child = (DiffNode)this.getChildAt(i);

        result.add(child.reflectLeafAddedRemovedNumberToParentName());
      }

      if (getChildCount() > 0) {
        setSecondaryText("(" +
            bundle.getString("added") + " " + result.added + ", " +
            bundle.getString("removed") + " " + result.removed + ")");
      }

      if (isRemovedNode()) {
        result.removed++;
      } else if (isAddedNode()) {
        result.added++;
      }

      return result;
    }

    private static class AddedRemovedCounter {
      public int added = 0;
      public int removed = 0;

      public void add(AddedRemovedCounter b) {
        added += b.added;
        removed += b.removed;
      }
    }
  }


  private static class DiffNodeComparator implements Comparator {
    private static final Comparator instance = new DiffNodeComparator();

    private DiffNodeComparator() {}

    public int compare(Object a, Object b) {
      DiffNode node1 = (DiffNode) a;
      DiffNode node2 = (DiffNode) b;

      if (node1.getBinMemberType() > node2.getBinMemberType()) {
        return -1;
      }

      if (node1.getBinMemberType() < node2.getBinMemberType()) {
        return 1;
      }

      String node1Caption = StringUtil.removeHtml(node1.toString()).trim();
      String node2Caption = StringUtil.removeHtml(node2.toString()).trim();

      return node1Caption.compareTo(node2Caption);
    }
  }
}
