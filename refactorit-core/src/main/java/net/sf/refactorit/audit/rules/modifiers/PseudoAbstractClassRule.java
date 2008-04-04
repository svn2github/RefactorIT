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
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Igor Malinin
 */
public class PseudoAbstractClassRule extends AuditRule {
  public static final String NAME = "pseudo_abstract";

  public void visit(BinCIType type) {
    if (type.isClass() && type.isAbstract()) {
      boolean found = false;
      BinMethod[] methods = type.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].isAbstract()) {
          found = true;
          break;
        }
      }

      if (!found) {
        addViolation(new PseudoAbstractClass((BinClass) type));
      }
    }

    super.visit(type);
  }
}


class PseudoAbstractClass extends AwkwardMemberModifiers {

  PseudoAbstractClass(BinClass type) {
    super(type, "Redundant abstract modifier", "refact.audit.pseudo_abstract");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveAbstractModifier.instance);
  }
}


class RemoveAbstractModifier extends MultiTargetCorrectiveAction {
  static final RemoveAbstractModifier instance = new RemoveAbstractModifier();

  public String getKey() {
    return "refactorit.audit.action.abstract.remove";
  }

  public String getName() {
    return "Remove abstract modifier";
  }

  public String getMultiTargetName() {
    return "Remove abstract modifiers from pseudo-abstract classes";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof PseudoAbstractClass)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinMember member = ((PseudoAbstractClass) violation).getOwnerMember();
    int modifiers = BinModifier.clearFlags(
        member.getModifiers(), BinModifier.ABSTRACT);

    manager.add(new ModifierEditor(member, modifiers));

    return Collections.singleton(compilationUnit);
  }
}
