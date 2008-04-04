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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
*
* @author Arseni Grigorjev
*/
public class FinalMethodProposalRule extends AuditRule{
  public static final String NAME = "finalize_methods";
  
  Stack stack = new Stack();
  ArrayList methodsToFinalize = null;
  
  public void init() {
    super.init();
    // slow, but makes BinTypeRef.getDirectSubclasses() return correct results
    getProject().discoverAllUsedTypes();
  }
  
  public void visit(final BinCIType type){
    stack.push(methodsToFinalize);
    methodsToFinalize = new ArrayList(10);
  }
  
  public void leave(final BinCIType type){
    int alreadyFinalMethods = 0;
    final BinMethod[] declaredMethods = type.getDeclaredMethods();
    for (int i = 0; i < declaredMethods.length; i++){
      if (declaredMethods[i].isFinal()){
        alreadyFinalMethods++;
      }
    }

    final boolean proposeFinalizeClass;
    if (!type.isFinal()
        && !type.isAbstract()
        && !type.isInterface()
        && !type.isAnonymous()
        && !type.isTypeParameter()
        && (type.getOwner() == null || !type.getOwner().getBinCIType().isEnum())
        && type.getTypeRef().getDirectSubclasses().size() == 0
        && declaredMethods.length == (alreadyFinalMethods + methodsToFinalize
        .size())){
      addViolation(new FinalClassProposal(type));
      proposeFinalizeClass = true;
    } else {
      proposeFinalizeClass = false;
    }

    for (int i = 0, max = methodsToFinalize.size(); i < max; i++){
      addViolation(new FinalMethodProposal((BinMethod) methodsToFinalize.get(i),
          proposeFinalizeClass));
    }
    methodsToFinalize.clear();
    methodsToFinalize = (ArrayList) stack.pop();
  }
  
  public void visit(final BinMethod method){
    final BinCIType ownerType = method.getOwner().getBinCIType();
    if (!method.isFinal()
        && !method.isAbstract()
        && !method.isMain()
        && !ownerType.isFinal()
        && !ownerType.isInterface()
        && (ownerType.getOwner() == null
        || !ownerType.getOwner().getBinCIType().isEnum())
        && ownerType.getSubMethods(method).size() == 0){
      methodsToFinalize.add(method);
    }
  }
}

class FinalClassProposal extends AwkwardMember {
  public FinalClassProposal(final BinMember member){
    super(member, "Class " + member.getName() + " should be " +
        "'final': neither it nor its methods are overriden.", "refact.audit.finalize_methods");
  }
  
  public List getCorrectiveActions(){
    return Collections.singletonList(AddFinalModifierForClass.INSTANCE);
  }
}

class FinalMethodProposal extends AwkwardMember {
  final boolean proposeFinalizeClass;
  
  public FinalMethodProposal(final BinMethod method,
      final boolean proposeFinalizeClass){
    super(method, method.getName() + "() is never overriden - "
        + "should be 'final'", "refact.audit.finalize_methods");
    
    this.proposeFinalizeClass = proposeFinalizeClass;
  }
  
  public List getCorrectiveActions(){
    final ArrayList result = new ArrayList(2);
    if (proposeFinalizeClass){
      result.add(AddFinalModifierForClass.INSTANCE);
    }
    result.add(AddFinalModifierForMethod.INSTANCE);
    return result;
  }

  public boolean hasProposeFinalizeClass() {
    return this.proposeFinalizeClass;
  }
}

class AddFinalModifierForClass extends MultiTargetCorrectiveAction {
  static final AddFinalModifierForClass INSTANCE 
      = new AddFinalModifierForClass();

  public String getKey() {
    return "refactorit.audit.action.final.add_to_class";
  }

  public String getName() {
    return "Accept suggested final modifier for class";
  }

  public String getMultiTargetName() {
    return "Accept suggested final modifier(s) for class(es)";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, final RuleViolation violation) {
    if (!(violation instanceof FinalMethodProposal)
        && !(violation instanceof FinalClassProposal)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    
    if (violation instanceof FinalMethodProposal 
        && !((FinalMethodProposal) violation).hasProposeFinalizeClass()){
      return Collections.EMPTY_SET;  
    }

    final CompilationUnit compilationUnit = violation.getCompilationUnit();
    
    final BinMember ownerMember = violation.getOwnerMember();
    final BinCIType type;
    if (ownerMember instanceof BinMethod){
      type = ownerMember.getOwner().getBinCIType();
    } else {
      type = (BinCIType) ownerMember;
    }
    
    // add 'final' modifier to Class
    int modifiers = type.getModifiers();
    modifiers = BinModifier.setFlags(modifiers, BinModifier.FINAL);
    manager.add(new ModifierEditor(type, modifiers));
    
    // remove 'final' modifiers from methods
    final BinMethod[] declaredMethods = type.getDeclaredMethods();
    int newModifiers;
    for (int i = 0; i < declaredMethods.length; i++){
      modifiers = declaredMethods[i].getModifiers();
      newModifiers = BinModifier.clearFlags(modifiers, BinModifier.FINAL);
      if (modifiers != newModifiers){
        manager.add(new ModifierEditor(declaredMethods[i], newModifiers));
      }
    }

    return Collections.singleton(compilationUnit);
  }
}

class AddFinalModifierForMethod extends MultiTargetCorrectiveAction {
  static final AddFinalModifierForMethod INSTANCE 
      = new AddFinalModifierForMethod();

  public String getKey() {
    return "refactorit.audit.action.final.add_to_method";
  }

  public String getName() {
    return "Accept suggested final modifier for method";
  }

  public String getMultiTargetName() {
    return "Accept suggested final modifier(s) for method(s)";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, final RuleViolation violation) {
    if (!(violation instanceof FinalMethodProposal)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    final CompilationUnit compilationUnit = violation.getCompilationUnit();

    final BinMember method = violation.getOwnerMember();
    int modifiers = method.getModifiers();
    modifiers = BinModifier.setFlags(modifiers, BinModifier.FINAL);
    manager.add(new ModifierEditor(method, modifiers));

    return Collections.singleton(compilationUnit);
  }
}
