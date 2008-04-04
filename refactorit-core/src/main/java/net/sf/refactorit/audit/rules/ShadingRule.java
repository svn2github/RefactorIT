/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.GetterSetterUtils;

import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author sander
 */
public class ShadingRule extends AuditRule {
  public static final String NAME = "shading";

  private boolean skip_constructors = true;
  private boolean skip_setters = true;
  
  public void init(){
    super.init();
    final Element configuration = getConfiguration();
    skip_constructors = AuditProfileUtils.getBooleanOption(configuration,
        "skip", "constructors", skip_constructors);
    skip_setters = AuditProfileUtils.getBooleanOption(configuration,
        "skip", "setters", skip_setters);
  }
  
  public void visit(BinTryStatement.CatchClause catchClause) {
    testVariableForFieldShading(catchClause.getParameter());

    super.visit(catchClause);
  }

  public void visit(BinLocalVariable x) {
    testVariableForFieldShading(x);
    super.visit(x);
  }

  private void testVariableForFieldShading(BinLocalVariable var) {
    if (var.isImplied()) {
      return;
    }

    String name = var.getName();
    BinField shadableField = getCurrentType().getBinCIType()
        .getAccessibleField(name, getCurrentType().getBinCIType());

    if (shadableField != null) {
      boolean varStatic = var.getParentMember().isStatic();
      boolean fieldStatic = shadableField.isStatic();

      // if variable is not static or both variable and field are static
      if (!varStatic || fieldStatic) {
        BinMember member = var.getParentMember();
        if (member instanceof BinConstructor) {
          if (!skip_constructors) {
            addViolation(new Shading(var));
          }
        } else if (member instanceof BinMethod) {
          if (GetterSetterUtils.isSetterMethod((BinMethod) member, false)) {
            if (!skip_setters) {
              addViolation(new Shading(var));
            }
          } else {
            addViolation(new Shading(var));
          }
        } else {
          addViolation(new Shading(var));
        }
      }
    }
  }
}

class Shading extends SimpleViolation {

  public Shading(BinLocalVariable var) {
    super(var.getOwner(), var.getNameAstOrNull(), "Variable shades a field: "
        + var.getName(), "refact.audit.vars_shading_fields");
    setTargetItem(var);
  }

  public BinMember getSpecificOwnerMember() {
    return getVar().getParentMember();
  }

  BinLocalVariable getVar() {
    return (BinLocalVariable) getTargetItem();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RenameLocalVariable.instance);
  }
}


class RenameLocalVariable extends CorrectiveAction {
  static final RenameLocalVariable instance = new RenameLocalVariable();

  public String getKey() {
    return "refactorit.audit.action.shading.rename";
  }

  public String getName() {
    return "Rename variable";
  }

  public Set run(TreeRefactorItContext context, List violations) {
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof Shading)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    BinLocalVariable variable = ((Shading) violation).getVar();
    CompilationUnit compilationUnit = variable.getCompilationUnit();

    RefactorItAction rename = ModuleManager
        .getAction(variable.getClass(), RenameAction.KEY);

    rename.run(context, variable);

    return Collections.singleton(compilationUnit);
  }
}
