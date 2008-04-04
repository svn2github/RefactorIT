/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.inheritance;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author  Igor Malinin
 */
public class HiddenStaticMethodRule extends AuditRule {
  public static final String NAME = "hidden_method";

  public void visit(BinMethod method) {
    if (!method.isStatic()) {
      BinCIType context = method.getParentType();
      if (context.isClass()) {
        final BinTypeRef superclass = context.getTypeRef().getSuperclass();
        if (superclass != null) { // Object doesn't have a super
          BinCIType base = superclass.getBinCIType();

          BinMethod[] accessible = base
              .getAccessibleMethods(method.getName(), context);

          if (accessible != null && accessible.length != 0) {
            BinParameter[] params = method.getParameters();

            BinTypeRef[] types = new BinTypeRef[params.length];
            for (int j = 0; j < types.length; j++) {
              types[j] = params[j].getTypeRef();
            }

            for (int i = 0; i < accessible.length; i++) {
              BinMethod candidate = accessible[i];
              if (!candidate.isStatic()) {
                continue;
              }

              if (MethodInvocationRules.isApplicable(candidate, types)) {
                addViolation(new HiddenStaticMethod(method));
              }
            }
          }
        }
      }
    }

    super.visit(method);
  }
}


class HiddenStaticMethod extends AwkwardMember {
  public HiddenStaticMethod(BinMethod method) {
    super(method, "Method hides another static method from " +
        "superclass: " + method.getName(), "refact.audit.hidden_method");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RenameMethod.instance);
  }
}


class RenameMethod extends CorrectiveAction {
  static final RenameMethod instance = new RenameMethod();

  public String getKey() {
    return "refactorit.audit.action.method.rename";
  }

  public String getName() {
    return "Rename method";
  }

  public Set run(TreeRefactorItContext context, List violations) {
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof HiddenStaticMethod)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();
    BinMember member = violation.getOwnerMember();

    RefactorItAction rename = ModuleManager
        .getAction(member.getClass(), RenameAction.KEY);

    RefactorItActionUtils.run(rename, context, member);

    return Collections.singleton(compilationUnit);
  }
}
