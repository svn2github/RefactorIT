/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.utils;


import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.audit.AuditTreeTableNode;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 * @author Arseni Grigorjev
 */
public final class AuditTablePopupUtils {
  public static final Logger log = AppRegistry.getLogger(AuditTablePopupUtils.class);
  
  /** @skip empblock */
  private AuditTablePopupUtils() {
  }
  
  public static BinMember findOwnerForUnskip(final BinTreeTableNode node, 
      final RuleViolation violation,
      final BinMember skipedOn) {
    
    Object nodeBin = node.getBin();
          
    if (node instanceof AuditTreeTableNode 
        && skipedOn == violation.getOwnerMember()){
      return skipedOn;
    } else if (nodeBin instanceof BinTypeRef
        && skipedOn == ((BinTypeRef) nodeBin).getBinCIType()){
      return skipedOn;
    } else {
      List children = node.getChildren();
      for (int i = 0; i < children.size(); i++){
        BinTreeTableNode curNode = (BinTreeTableNode) children.get(i);
        BinMember owner 
            = findOwnerForUnskip(curNode, violation, skipedOn);
        if (owner != null){
          return owner;
        }
      }
      return null;
    }
  }
     
  public static List groupViolations(List violations){
        
    Map workingMap = new TreeMap();
    
    Iterator allViolations = violations.iterator();
    while (allViolations.hasNext()){
      SimpleViolation violation = (SimpleViolation) allViolations.next();
      String branch = violation.getCategoryName();
      String audit = violation.getAuditName();
      
      Map group = (Map) workingMap.get(branch);
      if (group == null){
        group = new TreeMap();
        workingMap.put(branch, group);
      }
      
      List subGroup = (List) group.get(audit);
      if (subGroup == null){
        subGroup = new ArrayList();
        group.put(audit, subGroup);
      }
      
      subGroup.add(violation);
    }
    
    return new ArrayList(workingMap.values());
  }
  
  public static boolean nodeHasChildrenSelected(BinTreeTableNode node, 
      UITreeNode[] selected){
    boolean found = false;
    for (Iterator chldrn = node.getChildren().iterator(); chldrn.hasNext();){
      BinTreeTableNode child = (BinTreeTableNode) chldrn.next();
      
      for (int i = 0; i < selected.length; i++){
        if (selected[i] == child){
          found = true;
          break;
        }
      }
      if (!found) {
        found = nodeHasChildrenSelected(child, selected);
      }
    }
    return found;
  }

  public static List getViolations(BinTreeTableNode topNode, 
      BinTreeTableNode node, List violations){
    if (node instanceof AuditTreeTableNode) {
      RuleViolation violation = ((AuditTreeTableNode) node).getRuleViolation();
      violation.setSelectionNode(topNode);
      violations.add(violation);
    } else {
      List children = node.getChildren();
      for (Iterator i = children.iterator(); i.hasNext(); ) {
        getViolations(topNode, (BinTreeTableNode) i.next(), violations);
      }
    }

    return violations;
  }

  public static void collectSkipUnskipActions(Map skipNames, Map unskipNames, 
      List violations){
    
    for (int k = 0; k < violations.size(); k++){
      SimpleViolation violation = (SimpleViolation) violations.get(k);
      String shortName = violation.getAuditRule().getKey();
      BinMember skipedOn = violation.getSkipedOn();
      if (violation.getTypeShortName() != null
          && violation.getOwnerMember() != null
          && skipedOn == null){

        List owners = (List) skipNames.get(shortName);
        if (owners == null){
          owners = new ArrayList();
          skipNames.put(shortName, owners);
        }

        BinMember owner = null;
        BinTreeTableNode selectionNode = violation.getSelectionNode();

        if (selectionNode instanceof AuditTreeTableNode){
          owner = violation.getOwnerMember();
        } else if (selectionNode.getBin() instanceof BinTypeRef){
          owner = ((BinTypeRef) selectionNode.getBin()).getBinCIType();
        } else {
          owner = violation.getOwnerMember().getTopLevelEnclosingType();
        }

        if (owner != null && !owners.contains(owner)){
          owners.add(owner);
        }  

      } else if (skipedOn != null){
        List owners = (List) unskipNames.get(shortName);
        if (owners == null){
          owners = new ArrayList();
          unskipNames.put(shortName, owners);
        }

        BinMember owner = AuditTablePopupUtils.findOwnerForUnskip(
            violation.getSelectionNode(), violation, skipedOn);

        if (owner != null && !owners.contains(owner)){
          owners.add(owner);
        }
      }
    }
  }

  public static Set getCorrectiveActions(List violations) {
    Set actions = new HashSet();

    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation violation = (RuleViolation) i.next();
      Iterator j = violation.getCorrectiveActions().iterator();
      while (j.hasNext()) {
        CorrectiveAction action = (CorrectiveAction) j.next();
        if (action == null){
          log.warn("Violation " + violation.getClass().getName()
              + " returns 'null' corrective action!");
        } else {
          //if (action.isMultiTargetsSupported()) {
            actions.add(action);
          //}
        }
      }
    }

    return actions;
  }
}
