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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class StaticMethodProposalRule extends AuditRule {
  public static final String NAME = "static_candidate";

  static class StateAccessFinder extends BinItemVisitor {
    boolean success = true;

    public void visit(BinFieldInvocationExpression expression) {
      if (!isSafe(expression)) {
        this.success = false;

        // Bail out
        return;
      }

      super.visit(expression);
    }

    public void visit(BinMethodInvocationExpression expression) {
      if (!isSafe(expression)) {
        this.success = false;

        // Bail out
        return;
      }

      super.visit(expression);
    }

    public void visit(BinLiteralExpression expression) {
      if (expression.isThis() || expression.isSuper()) {
        this.success = false;

        // Bail out
        return;
      }

      super.visit(expression);
    }

    private static boolean isSafe(BinMemberInvocationExpression expression) {
      BinMember member = expression.getMember();

      // XXX: Assumes that static members are never invoked via object reference
      if (!member.isStatic()) {
        return expression.isOutsideMemberInvocation();
      }

      return true;
    }
  }


  public void visit(BinMethod method) {
    boolean concrete = !(method.isAbstract() || method.isNative());

    if (!method.isStatic() && concrete) {
      StateAccessFinder finder = new StateAccessFinder();

      method.accept(finder);

      BinCIType owner = (method.getOwner()).getBinCIType();

      // Don't break dependencies
      if (finder.success && method.findOverrides().isEmpty()
          && owner.getSubMethods(method).isEmpty()) {
        boolean unique = true;

        BinMethod[] methods = owner.getDeclaredMethods();

        // Do not staticalize overloaded methods
        for (int i = 0; (i < methods.length) && unique; i++) {
          BinMethod test = methods[i];

          if (test != method
              && (test.isApplicable(method) || method.isApplicable(test))) {
            unique = false;
          }
        }

        // XXX: Is this overloading check necessary IRL?
        if (unique) {
          addViolation(new StaticMethodProposal(method));
        }
      }
    }

    super.visit(method);
  }
}


class StaticMethodProposal extends AwkwardMemberModifiers {
  StaticMethodProposal(BinMethod method) {
    super(method, method.getName()
        + "() does not use enclosing state - should be static", "refact.audit.staticalizable_methods");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(AddStaticModifier.instance);
  }
}


class AddStaticModifier extends MultiTargetCorrectiveAction {
  static final AddStaticModifier instance = new AddStaticModifier();

  public String getKey() {
    return "refactorit.audit.action.static.add";
  }

  public String getName() {
    return "Accept suggested static modifier";
  }

  public String getMultiTargetName() {
    return "Accept suggested static modifiers";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof StaticMethodProposal)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinMember method = violation.getOwnerMember();
    int modifiers = method.getModifiers();
    modifiers = BinModifier.setFlags(modifiers, BinModifier.STATIC);

    manager.add(new ModifierEditor(method, modifiers));

    return Collections.singleton(compilationUnit);
  }
}
