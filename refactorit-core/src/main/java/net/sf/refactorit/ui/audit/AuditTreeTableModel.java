/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * WARNING: Because of cacheing this is sensitive to notifications:
 *
 *   1. To clear the entire model, use clearModel()
 *   2. If you remove a node from the model, call
 *      notifyNodeRemoved(Object)
 *
 * @author Risto Alas
 * @author Igor Malinin
 */
public class AuditTreeTableModel extends BinTreeTableModel {
  static final DecimalFormat densityParser = new DecimalFormat();

  static final DecimalFormat[] densityFormatters = new DecimalFormat[] {
      new DecimalFormat("0.000000"),
      new DecimalFormat("0.00000"),
      new DecimalFormat("0.0000"),
      new DecimalFormat("0.000"),
      new DecimalFormat("0.00"),
      new DecimalFormat("00.0"),
      new DecimalFormat("#########'00'"),
  };

  private final Map cachedNodePriorities = new HashMap();
  private final Map cachedNodeDensities = new HashMap();
  private final Map cachedNodeLineCounts = new HashMap();

  private AuditRule[] auditRules = new AuditRule[] {};

  private final AuditTreeTableColumn[] columns =
      new AuditTreeTableColumn[] {
      new LocationColumn(),
      new LineColumn(),
      new SourceColumn(),
      new PriorityColumn(),
      new DensityColumn(),
      new TypeColumn(),
  };

  private Map cachedBinTypeRefNodes = new HashMap();

  private int activeColumn;

  private static String formatDensity(float f) {
    return getDensityFormatter(f).format(f);
  }

  private static DecimalFormat getDensityFormatter(final float f) {
    DecimalFormat result;

    if (f < 0.001) { // 0.0009999...
      result = densityFormatters[0];
    } else if (f < 0.01) { // 0.009999...
      result = densityFormatters[1];
    } else if (f < 0.1) { // 0.09999...
      result = densityFormatters[2];
    } else if (f < 1.0) { // 0.9999...
      result = densityFormatters[3];
    } else if (f < 10) { // 9.999...
      result = densityFormatters[4];
    } else if (f < 100) { // 99.99...
      result = densityFormatters[5];
    } else {
      result = densityFormatters[6];
    }

    result.setDecimalFormatSymbols(UIResources.createDecimalFormatSymbols());

    return result;
  }

  public AuditTreeTableModel() {
    super(new BinTreeTableNode(new AuditRootNode()));
  }

  public AuditTreeTableModel(String header) {
    super(new BinTreeTableNode(header));
  }

  public int getColumnCount() {
    return columns.length;
  }

  public AuditTreeTableColumn[] getColumns() {
    return columns;
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Location";
      case 1:
        return "Line";
      case 2:
        return "Source";
      case 3:
        return "Priority";
      case 4:
        return "Density";
      case 5:
        return "Type";
      default:
        return null;
    }
  }

  public Class getColumnClass(int col) {
    switch (col) {
      case 1:
        return Integer.class;
      case 2:
        return String.class;
      default:
        return super.getColumnClass(col);
    }
  }

  public Object getValueAt(Object node, int column) {
    try {
      return columns[column].getValue(this, node);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,
          "Failed to get value for: " + node + "[" + column + "]", this);
      return null;
    }
  }

  public void resort() {
    sort(activeColumn);
  }

  public void sort(int column) {
    ArrayList nodes = new ArrayList(1000);

    getAuditNodes((BinTreeTableNode) getRoot(), nodes);

    clearModel();

    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();

    rootNode.removeSecondaryText();

    activeColumn = column;
    for (int i = 0; i < nodes.size(); i++) {
      addAuditNode((AuditTreeTableNode) nodes.get(i));
    }

    sort();

    notifyNodesAddedOrRemoved();

    rootNode.reflectLeafNumberToParentName();

    fireSomethingChanged();
  }

  private void getAuditNodes(BinTreeTableNode node, List result) {
    if (node instanceof AuditTreeTableNode) {
      result.add(node);
    }

    List children = node.getAllChildren();
    for (int i = 0; i < children.size(); i++) {
      getAuditNodes((BinTreeTableNode) children.get(i), result);
    }
  }

  /** Use only this method to clear the entire model because it works with the cache */
  public void clearModel() {
    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();
    rootNode.removeAllChildren();
    notifyAllNodesRemoved();
  }

  void notifyNodesAddedOrRemoved() {
    cachedNodePriorities.clear();
    cachedNodeDensities.clear();
    cachedNodeLineCounts.clear();
  }

  void notifyAllNodesRemoved() {
    notifyNodesAddedOrRemoved();
    cachedBinTypeRefNodes.clear();
  }

  /**
   * Counts density and converts it to string to display.
   *
   * For speed this method caches its results
   */
  String getDisplayableDensity(Object node) {
    if (node instanceof AuditTreeTableNode) {
      AuditTreeTableNode n = (AuditTreeTableNode) node;
      return formatDensity(n.getRuleViolation().getDensity());
    }

    BinTreeTableNode n = (BinTreeTableNode) node;

    if (n.getBin() instanceof BinTypeRef) {
      if (n.getChildCount() == 0) {
        return "0";
      }

      return getDisplayableDensity(n.getChildAt(0));
    }

    // node is a package node or some string (root node or type node)
    String density = (String) cachedNodeDensities.get(node);
    if (density != null) {
      return density;
    }

    densityParser.setDecimalFormatSymbols(UIResources.createDecimalFormatSymbols());

    float total = 0;
    int totalLines = 0;

    for (int i = 0, max = n.getChildCount(); i < max; i++) {
      try {
        int lineCount = getLineCount((BinTreeTableNode) n.getChildAt(i));
        total += lineCount * densityParser.parse(
            getDisplayableDensity(n.getChildAt(i))).floatValue();

        totalLines += lineCount;
      } catch (ParseException e) {
//        e.printStackTrace();
        throw new ChainableRuntimeException(e);
      }
    }

    if(totalLines == 0) {
      density = "";
    } else {
      density = formatDensity(total / totalLines);
    }

    cachedNodeDensities.put(n, density);

    return density;
  }

  /**
   * Counts lines covered by the node's (and its childrens') CompilationUnits.
   *
   * For speed this method caches its results.
   */
  private int getLineCount(BinTreeTableNode node) {
    Object bin = node.getBin();
    if (bin instanceof BinTypeRef) {
      BinCIType type = ((BinTypeRef) bin).getBinCIType();
      if (!type.isFromCompilationUnit()) {
        return 0;
      }
      return type.getCompilationUnit().getSource().getLineCount();
    }

    Integer lineCount = (Integer) cachedNodeLineCounts.get(node);
    if (lineCount != null) {
      return lineCount.intValue();
    }

    if (bin instanceof BinPackage) {
      // Keeps us from counting a source file twice --
      // one file might contain many BinCITypes.
      List countedCompilationUnits = new ArrayList();

      // The direct here children must all be BinTypeRefs

      int total = 0;
      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        BinTreeTableNode child = (BinTreeTableNode) node.getChildAt(i);
        if (!countedCompilationUnits.contains(child.getSource())) {
          total += getLineCount(child);
          countedCompilationUnits.add(child.getSource());
        }
      }

      lineCount = new Integer(total);
    } else {
      /* node.getBin() is a String or Priority */

      // Direct children here must all be either other Strings or
      // BinPackages; either way, their lines do not overlap with eachother
      // (as is in the case of files of BinTypeRefs).

      int total = 0;
      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        BinTreeTableNode child = (BinTreeTableNode) node.getChildAt(i);
        total += getLineCount(child);
      }

      lineCount = new Integer(total);
    }

    cachedNodeLineCounts.put(node, lineCount);

    return lineCount.intValue();
  }

  /**
   * Counts maximum priority.
   *
   * For speed this method caches its results.
   */
  Priority getMaxPriorityOfChildren(Object node) {
    if (node instanceof AuditTreeTableNode) {
      return ((AuditTreeTableNode) node).getRuleViolation().getPriority();
    }

    Priority priority;

    priority = (Priority) cachedNodePriorities.get(node);
    if (priority != null) {
      return priority;
    }

    priority = Priority.LOW;

    int childCount = getChildCount(node);
    for (int i = 0; i < childCount; i++) {
      Priority max = getMaxPriorityOfChildren(getChild(node, i));
      priority = Priority.highest(priority, max);
    }

    cachedNodePriorities.put(node, priority);

    return priority;
  }

  public AuditTreeTableColumn getActiveColumn() {
    AuditTreeTableColumn column = columns[activeColumn];
    if (column instanceof LineColumn || column instanceof SourceColumn) {
      return columns[0];
    }

    return columns[activeColumn];
  }

  public void sort() {
    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();
    rootNode.sortAllChildren();

    Comparator comparator = getActiveColumn().getNodeComparatorOrNull(this);
    if (comparator != null) {
      rootNode.sortDirectChildren(comparator);
    }
  }

  public void addViolation(RuleViolation violation) {
    addAuditNode(new AuditTreeTableNode(violation, this));

    notifyNodesAddedOrRemoved();
  }

  private void addAuditNode(AuditTreeTableNode node) {
    RuleViolation violation = node.getRuleViolation();

    ParentTreeTableNode rootNode = (BinTreeTableNode) getRoot();

    Object sortObject = getActiveColumn().getSortObjectOrNull(violation);
    if (sortObject != null) {
      rootNode = findParentForCategoryNode(rootNode, sortObject);
    }

    BinTypeRef binTypeRef = violation.getBinTypeRef();
    if (binTypeRef == null){
      return; // could not restore binTypeRef :(
    }

    ParentTreeTableNode typeNode = findParentForTypeNode(rootNode,
        binTypeRef, getActiveColumn().isPackageSort());

    typeNode.addChild(node);
  }

  private ParentTreeTableNode findParentForPackage(
      ParentTreeTableNode rootNode, BinPackage aPackage
      ) {
    ParentTreeTableNode result = null;

    HashMap cachedNodes = (HashMap) cachedBinTypeRefNodes.get(rootNode);
    if (cachedNodes == null) {
      cachedNodes = new HashMap();
      cachedBinTypeRefNodes.put(rootNode, cachedNodes);
    } else {
      result = (ParentTreeTableNode) cachedNodes.get(aPackage);
    }

    if (result == null) {
      result = new BinTreeTableNode(aPackage);
      cachedNodes.put(aPackage, result);

      rootNode.addChild(result);
    }

    return result;
  }

  private ParentTreeTableNode findParentForTypeNode(
      ParentTreeTableNode rootNode, BinTypeRef typeRef,
      boolean usePackageNode
      ) {
    ParentTreeTableNode result = null;

    HashMap cachedNodes = (HashMap) cachedBinTypeRefNodes.get(rootNode);
    if (cachedNodes == null) {
      cachedNodes = new HashMap();
      cachedBinTypeRefNodes.put(rootNode, cachedNodes);
    } else {
      result = (ParentTreeTableNode) cachedNodes.get(typeRef);
    }

    if (result == null) {
      result = new BinTreeTableNode(typeRef);
      cachedNodes.put(typeRef, result);

      BinCIType type = typeRef.getBinCIType();

      if (type.isInnerType()) {
        findParentForTypeNode(rootNode, type.getOwner(), usePackageNode)
            .addChild(result);
      } else if (usePackageNode) {
        findParentForPackage(rootNode, type.getPackage()).addChild(result);
      } else {
        rootNode.addChild(result);
      }
    }

    return result;
  }

  private static ParentTreeTableNode findParentForCategoryNode(
      ParentTreeTableNode node, Object o
      ) {
    String name = o.toString();

    List nodeChildren = node.getAllChildren();
    for (int i = 0, length = nodeChildren.size(); i < length; i++) {
      BinTreeTableNode child = (BinTreeTableNode) nodeChildren.get(i);
      if (child.getDisplayName().equals(name)) {
        return child;
      }
    }

    // Does not exist yet, need to create a new one
    ParentTreeTableNode result = new BinTreeTableNode(o) {
      // Avoids warnings about missing ASTs
      public ArrayList getAsts() {
        return CollectionUtil.EMPTY_ARRAY_LIST;
      }
    };

    result.setDisplayName(name);
    node.addChild(result);

    return result;
  }

  public void saveBinItemReferences(RefactorItContext context) {
    saveBinItemReferencesForCaches(context.getProject());

    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();
    rootNode.saveBinItemReferences(context.getProject());
  }

  public void restoreFromBinItemReferences(RefactorItContext context) {
    restoreCachesFromBinItemReferences(context.getProject());

    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();
    rootNode.restoreFromBinItemReferences(context.getProject());
  }

  private static HashMap getNewMap(HashMap map, Project project) {
    HashMap result = new HashMap();

    Iterator keys = map.keySet().iterator();
    while (keys.hasNext()) {
      Object binObject = keys.next();
      Object value = map.get(binObject);
      result.put(BinItemReference.create(binObject), value);
    }

    return result;
  }

  private void saveBinItemReferencesForCaches(Project project) {
    HashMap newMap = new HashMap();

    Iterator keys = cachedBinTypeRefNodes.keySet().iterator();
    while (keys.hasNext()) {
      BinTreeTableNode rootNode = (BinTreeTableNode) keys.next();
      HashMap value = (HashMap) cachedBinTypeRefNodes.get(rootNode);
      newMap.put(rootNode, getNewMap(value, project));
    }

    cachedBinTypeRefNodes = newMap;
  }

  private static HashMap getOldMap(HashMap map, Project project) {
    HashMap result = new HashMap();

    Iterator keys = map.keySet().iterator();
    while (keys.hasNext()) {
      BinItemReference ref = (BinItemReference) keys.next();
      Object value = map.get(ref);
      result.put(ref.restore(project), value);
    }

    return result;
  }

  private void restoreCachesFromBinItemReferences(Project project) {
    HashMap newMap = new HashMap();

    Iterator keys = cachedBinTypeRefNodes.keySet().iterator();
    while (keys.hasNext()) {
      BinTreeTableNode rootNode = (BinTreeTableNode) keys.next();
      HashMap value = (HashMap) cachedBinTypeRefNodes.get(rootNode);
      newMap.put(rootNode, getOldMap(value, project));
    }

    cachedBinTypeRefNodes = newMap;
  }

  public void setAuditRules(AuditRule[] audits) {
    this.auditRules = audits;
  }

  public AuditRule[] getAuditRules() {
    return this.auditRules;
  }

  public int getActiveColumnIndex() {
    return activeColumn;
  }
}
