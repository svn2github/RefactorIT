/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit;


import net.sf.refactorit.audit.AuditReconciler;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.ExplanationAction;
import net.sf.refactorit.audit.MultiTargetSkipUnskipAction;
import net.sf.refactorit.audit.ReconcileActionDecorator;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.ResultsTreeDisplayState;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.AuditTablePopupUtils;

import javax.swing.table.TableColumn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 *
 * @author Risto Alas
 * @author Igor Malinin
 * @author Arseni Grigorjev
 */
public class AuditTreeTable extends BinTreeTable {
  final AuditReconciler reconciler;

  public AuditTreeTable(AuditTreeTableModel model, RefactorItContext context,
      AuditReconciler reconciler) {
    super(model, context);

    this.reconciler = reconciler;

    //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    AuditTreeTableColumn[] columns = model.getColumns();

    for (int i = 0; i < columns.length; i++) {
      TableColumn column = getColumnModel().getColumn(i);
      column.setMinWidth(columns[i].getMinWidth());
      column.setPreferredWidth(columns[i].getPreferredWidth());
      column.setMaxWidth(columns[i].getMaxWidth());
    }

    getTableHeader().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int column = getColumnModel().getColumnIndexAtX(e.getX());
        if (column != -1) {
          clickColumn(column);
        }
      }
    });
  }

  public void clickColumn(final int column) {
    AuditTreeTableModel model = (AuditTreeTableModel) getBinTreeTableModel();

    ResultsTreeDisplayState state;

    state = model.getActiveColumn().getResultsTreeDisplayState();
    state.saveExpansionAndScrollState(getTree());
        
    model.sort(column);

    Project project = context.getProject();

    state = model.getActiveColumn().getResultsTreeDisplayState();
    state.restoreExpansionAndScrollState(this, project);
  }

  public List getApplicableActions(
      final UITreeNode[] treeNodes, final IdeWindowContext context) {

    List result = new ArrayList();
    List allViolations = new ArrayList();

    boolean includeNonMultitarget = false;

    // now we will collect violations from selected nodes
    BinTreeTableNode node = (BinTreeTableNode) treeNodes[0];
    BinTypeRef ownerType = null;
    if (treeNodes.length == 1 && node.getBin() instanceof BinTypeRef){
      ownerType = (BinTypeRef) node.getBin();
      AuditTablePopupUtils.getViolations(node, node, allViolations);
    } else if(treeNodes.length == 1 && node instanceof AuditTreeTableNode){
      RuleViolation violation = ((AuditTreeTableNode) node).getRuleViolation();
      violation.setSelectionNode(node);
      allViolations.add(violation);
      includeNonMultitarget = true;
    } else {
      for (int i = 0; i < treeNodes.length; i++){
        node = (BinTreeTableNode) treeNodes[i];

        if (node instanceof AuditTreeTableNode
            || !AuditTablePopupUtils.nodeHasChildrenSelected(node, treeNodes)){
          AuditTablePopupUtils.getViolations(node, node, allViolations);
        }
      }
    }

    // group violations by category and auditrule type (two level grouping)
    List branchGroups = AuditTablePopupUtils.groupViolations(allViolations);

    // now walk through the groups and collect corrective actions
    for (int brgr = 0; brgr < branchGroups.size(); brgr++){
      List violationGroups
          = new ArrayList(((Map) branchGroups.get(brgr)).values());
      List curGroup = new ArrayList();

      for (int i = 0; i < violationGroups.size(); i++){
        List violations = (List) violationGroups.get(i);

        Set correctiveActions
            = AuditTablePopupUtils.getCorrectiveActions(violations);

        SimpleViolation anyViolation = ((SimpleViolation) violations.get(0));

        if (i == 0){
          // put name of category (first element in list is always a string)
          curGroup.add(anyViolation.getCategoryName());
          // put bold state (second element in list is always a boolean)
          curGroup.add(new Boolean(false));
        }

        List ra = new ArrayList();
        // put name of audittype
        ra.add(anyViolation.getAuditName());
        if (correctiveActions.size() > 0){
          // put bold state
          ra.add(new Boolean(true));
          // change parent-menu bold state
          if (!((Boolean)curGroup.get(1)).booleanValue()){
            curGroup.set(1, new Boolean(true));
          }
        } else {
          ra.add(new Boolean(false));
        }

        // encapsulate correctiveActions for display
        ra.addAll(getReconcileActions(correctiveActions, violations,
            includeNonMultitarget));

        // maps for storing skip-names and skip-owners
        Map unskipNames = new TreeMap();
        Map skipNames = new TreeMap();

        // walk the violations and collect skip/unskip names and owners
        AuditTablePopupUtils.collectSkipUnskipActions(skipNames, unskipNames,
            violations);

        // if there were some corrective actions present -- add separator
        //  before skip/unskip actions
        if (ra.size() > 2 && (skipNames.size() > 0 || unskipNames.size() > 0)){
          // adding to current group
          ra.add(null);
        }

        // create and add reconcile skip unskip actions to current group
        createSkipUnskipReconcileActions(ra, skipNames, true);
        createSkipUnskipReconcileActions(ra, unskipNames, false);
        
        if (anyViolation.getHelpTopicId() != null){
          ra.add(null);
          ra.add(new ExplanationAction(anyViolation.getHelpTopicId()));
        }

        // the literal 2 means that first two elements are String name of
        // group and Boolean for bold/plain display of the name
        if (ra != null && ra.size() > 2) {
          curGroup.add(ra);
        }
      }

      if (curGroup.size() > 2){
        result.add(curGroup);
      }
    }

    simplifyMenu(result);

    return result;
  }

  /**
   * If there is only one branch in the results, we can list the audit rules
   * directly into the root popup.<br>
   * If there is only one branch in the results, and this branch contains only
   * one audit rule, we can list the corrective actions for this audit directly
   * into root popup. This covers the case, when the user sorts the audit
   * results by type and the right click on violation group shows redundant
   * multi-level popup.
   */
  private static void simplifyMenu(List result){
    if (result.size() == 1){
      List branch = (List) result.get(0);
      result.remove(0);
      if (branch.size() == 3){
        List group = (List) branch.get(2);
        group.remove(0);
        group.remove(0);
        result.addAll(group);
      } else {
        branch.remove(0);
        branch.remove(0);
        result.addAll(branch);
      }
    }
  }

  public void createSkipUnskipReconcileActions(final List ra, final Map skipUnskip,
      boolean skipAction) {
    for (Iterator it = skipUnskip.keySet().iterator(); it.hasNext(); ){
      String name = (String) it.next();
      MultiTargetSkipUnskipAction action
          = new MultiTargetSkipUnskipAction(name, skipAction);
      List list = (List) skipUnskip.get(name);
      for (int h = 0; h < list.size(); h++){
        action.addTarget(list.get(h));
      }
      ra.add(new ReconcileActionDecorator(action, null,
          action.isMultiTargetsSupported(), reconciler));
    }
  }

//  old variant for single selection
// -------------------------------------
//  public List getApplicableActions(
//      final UITreeNode treeNode, final RefactorItContext context) {
//    if (treeNode instanceof AuditTreeTableNode) {
//      AuditTreeTableNode node = (AuditTreeTableNode) treeNode;
//      RuleViolation violation = node.getRuleViolation();
//
//      List violations = Collections.singletonList(violation);
//      Collection correctiveActions = violation.getCorrectiveActions();
//
//      List result = new ArrayList(correctiveActions.size() + 4);
//      result.add(new ExplanationAction(violation.getHelpTopicId()));
//      List ra = getReconcileActions(correctiveActions, violations, false);
//      if (ra.size() > 0) {
//        result.add(null); // separator before the actions
//        result.addAll(ra);
//      }
//
//      BinMember owner = violation.getOwnerMember();
//      if (owner != null) {
//        result.add(null); // menu separator
//
//        String name = violation.getTypeShortName();
//        SkipUnskipAction action = new SkipUnskipAction(name, owner);
//        result.add(new ReconcileActionDecorator(action, violations, false));
//      }
//
//      return result;
//    }
//
//    if (treeNode instanceof BinTreeTableNode) {
//      BinTreeTableNode node = (BinTreeTableNode) treeNode;
//      List violations  = AuditTablePopupUtils.getViolations(node, node, new ArrayList());
//      Set correctiveActions = AuditTablePopupUtils.getMultitargetCorrectiveActions(violations);
//
//      List result = new ArrayList(correctiveActions.size() + 4);
//      List ra = getReconcileActions(correctiveActions, violations, true);
//      result.addAll(ra);
//
//      Object bin = node.getBin();
//      if (bin instanceof BinTypeRef) {
//        Set names = new TreeSet(); // alphabetically ordered
//        for (Iterator i = violations.iterator(); i.hasNext(); ) {
//          RuleViolation violation = (RuleViolation) i.next();
//          if (violation.getOwnerMember() != null) {
//            names.add(violation.getTypeShortName());
//          }
//        }
//
//        if (!names.isEmpty()) {
//          if (!result.isEmpty()) {
//            result.add(null); // menu separator
//          }
//
//          BinCIType owner = ((BinTypeRef) bin).getBinCIType();
//
//          for (Iterator i = names.iterator(); i.hasNext(); ) {
//            String name = (String) i.next();
//            SkipUnskipAction action = new SkipUnskipAction(name, owner);
//            result.add(new ReconcileActionDecorator(action, violations, false));
//          }
//        }
//      }
//
//      return result;
//    }
//
//    return super.getApplicableActions(treeNode, context);
//  }

  private List getReconcileActions(Collection actions, List violations,
      boolean enableSingletarget) {
    int length = actions.size();
    if (length == 0) {
      return Collections.EMPTY_LIST;
    }

    ArrayList result = new ArrayList(length);

    for (Iterator i = actions.iterator(); i.hasNext(); ) {
      CorrectiveAction action = (CorrectiveAction) i.next();
      result.add(new ReconcileActionDecorator(action, violations,
          action.isMultiTargetsSupported(), reconciler, enableSingletarget));
    }

    Collections.sort(result, new Comparator() {
      public int compare(Object o1, Object o2) {
        ReconcileActionDecorator d1 = (ReconcileActionDecorator) o1;
        ReconcileActionDecorator d2 = (ReconcileActionDecorator) o2;
        int res = d1.getName().compareTo(d2.getName());
        if (res != 0) {
          return res;
        }

        return d1.getKey().compareTo(d2.getKey());
      }
    });

    return result;
  }
}
