/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.modifiers;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMemberModifiers;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.edit.ModifierOrderer;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class ModifierOrderRule extends AuditRule{
  public static final String NAME = "modifier_order";

  /*
   * If there is such type of declaration:
   *   final public int a = 2, b = 4;
   * then audit must show only one violation; these fields are here to
   * catch such cases. It will create list of declaration-asts for each
   * compilation unit, and search through them if each new field has same
   * declaration=ast, as one of previous.
   */
  private List fieldOffsetAsts = new ArrayList();
  private CompilationUnit curUnit = null;

  public void checkOrder(BinMember member){
    // if it is a field, special treatment for complex declarations:
    if (member instanceof BinField){
      // now visiting a new compilation unit -> clear list
      if (curUnit != member.getCompilationUnit()){
        curUnit = member.getCompilationUnit();
        fieldOffsetAsts.clear();
      } else if (offsetFound(member.getOffsetNode())) {
        // this modifier group is already present -> do nothing
        return;
      }

      fieldOffsetAsts.add(member.getOffsetNode());
    }

    // get modifier asts
    Iterator modifiers = member.getModifierNodes().iterator();
    // check order comparing given ast positions with positions in correctOrder
    int prev = -1;
    while (modifiers.hasNext()){
      ASTImpl node = (ASTImpl) modifiers.next();
      int modifier = ASTUtil.getModifierForAST(node);
      
      if(modifier == BinModifier.ANNOTATION) { // skip annotations
        continue;
      }
      
      if (modifier < prev){
        // mixed order -> throw violation
        addViolation(new ModifierOrder(member));
        break;
      }
      prev = modifier;
    }
  }

  private boolean offsetFound(ASTImpl offsetNode){
    for (int i = fieldOffsetAsts.size() - 1; i >= 0; i--){
      if (((ASTImpl) fieldOffsetAsts.get(i)).equals(offsetNode)){
        return true;
      }
    }
    return false;
  }

  public void visit(BinCIType citype){
    checkOrder(citype);
    super.visit(citype);
  }

  public void visit(BinMethod method){
    checkOrder(method);
    super.visit(method);
  }

  public void visit(BinField field){
    checkOrder(field);
    super.visit(field);
  }
}

class ModifierOrder extends AwkwardMemberModifiers {
  public ModifierOrder(BinMember type) {
    super(type, "Modifiers order is mixed up", "refact.audit.modifier_order");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(CorrectModifiersOrder.instance);
  }
}

class CorrectModifiersOrder extends MultiTargetCorrectiveAction{
  public static final CorrectModifiersOrder instance
      = new CorrectModifiersOrder();

  public String getKey() {
    return "refactorit.audit.action.modifier_order.correct_order";
  }

  public String getName() {
    return "Order modifiers correctly";
  }

  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof ModifierOrder)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    BinMember member = ((AwkwardMemberModifiers) violation).getOwnerMember();
    CompilationUnit cUnit = member.getCompilationUnit();

    ModifierOrderer orderer = new ModifierOrderer(member);
    manager.add(orderer);

    return Collections.singleton(cUnit);
  }
}

