/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;


import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.JOptionPane;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class GenericsRule extends J2Se5AuditRule {
  public static final String NAME = "generics";
  
  public void visit(final BinField var){
    if (isGenericWithoutArgumentsUsage(var)){
      addViolation(new GenericViolation(var));
    }
  }
  
  public void visit(final BinLocalVariable var){
    if (isGenericWithoutArgumentsUsage(var)){
      addViolation(new GenericViolation(var));
    }
  }

  /**
   * @param var variable to check
   * @return <b>true</b>, if a generic type is used without type arguments; else 
   *    <b>false</b>
   */
  private boolean isGenericWithoutArgumentsUsage(final BinVariable var) {
    // primitive types can`t be parametrized
    if(var.getTypeRef().isPrimitiveType()){
      return false;
    }
    
    if ((var.getTypeRef().getTypeArguments() == null
        || var.getTypeRef().getTypeArguments().length == 0)
        && var.getTypeRef().getBinCIType().getTypeParameters() != null
        && var.getTypeRef().getBinCIType().getTypeParameters().length > 0) {
      return true;
    }
    return false;
  }
}

class GenericViolation extends AwkwardMember {

  GenericViolation(BinVariable var){
    super(var, "Variable of generic type is used without type arguments",
        "refact.audit.generics");
  }
  
  public BinVariable getVariable(){
    return (BinVariable) getTargetItem();
  }
  
  public List getCorrectiveActions(){
    return Collections.singletonList(IntroduceTypeArgumentsAction.INSTANCE);
  }
}

class IntroduceTypeArgumentsAction extends J2Se5CorrectiveAction {
  public static final IntroduceTypeArgumentsAction INSTANCE
      = new IntroduceTypeArgumentsAction();
  
  public boolean isMultiTargetsSupported(){
    return false;
  }
  
  public String getName() {
    return "Find and introduce suitable type arguments";
  }
  
  public String getKey() {
    return "refactorit.audit.action.generics.insert_arguments";
  }

  protected Set process(TreeRefactorItContext context,
      TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof GenericViolation)){
      return Collections.EMPTY_SET;
    }
try {
    GenericsArgumentsAnalyzer analyzer = new GenericsArgumentsAnalyzer(
        ((GenericViolation) violation).getVariable());
    final RefactoringStatus status = analyzer.run();
    
    if(!status.isOk()){
      if (status.isQuestion()) {
        int res = RitDialog.showConfirmDialog(context, status.getAllMessages(),
            "RefactorIT: corrective action warning",
            JOptionPane.YES_NO_OPTION,
            status.getJOptionMessageType());
        if (res != JOptionPane.YES_OPTION) {
          return Collections.EMPTY_SET;
        }
      } else {
        RitDialog.showMessageDialog(context, status.getAllMessages(),
            "RefactorIT: corrective action failed",
            status.getJOptionMessageType());
        return Collections.EMPTY_SET;
      }
    }

    analyzer.createEditors(manager);

    Set changedSources = Collections.singleton(violation.getCompilationUnit()); // FIXME
    return changedSources;
} catch (Exception e){
  AppRegistry.getLogger(IntroduceTypeArgumentsAction.class)
      .error("Exception during Generics corrective action: ", e);
}
    return Collections.EMPTY_SET;
  }
  
}
