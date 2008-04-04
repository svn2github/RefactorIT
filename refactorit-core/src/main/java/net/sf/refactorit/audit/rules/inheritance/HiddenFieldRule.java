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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
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
public class HiddenFieldRule extends AuditRule {
  public static final String NAME = "hidden_field";

  public void visit(BinField field) {
    BinCIType context = field.getParentType();
    if (context.isClass()) {
      BinCIType base = context.getTypeRef().getSuperclass().getBinCIType();

      BinField accessible = base.getAccessibleField(field.getName(), context);
      if (accessible != null) {
        addViolation(new HiddenField(field));
      }
    }

    super.visit(field);
  }
}


class HiddenField extends AwkwardMember {
  public HiddenField(BinField field) {
    super(field, "Field hides another field from superclass: " 
        + field.getName(), "refact.audit.hidden_field");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RenameField.instance);
  }
}


class RenameField extends CorrectiveAction {
  static final RenameField instance = new RenameField();

  public String getKey() {
    return "refactorit.audit.action.field.rename";
  }

  public String getName() {
    return "Rename field";
  }

  public Set run(TreeRefactorItContext context, List violations) {
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof HiddenField)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();
    BinMember member = violation.getOwnerMember();

    RefactorItAction rename = ModuleManager
        .getAction(member.getClass(), RenameAction.KEY);

    rename.run(context, member);

    return Collections.singleton(compilationUnit);
  }
}
