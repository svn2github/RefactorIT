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
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Igor Malinin
 */
public class RedundantModifiersRule extends AuditRule {
  public static final String NAME = "redundant_modifiers";
  
  public void visit(final BinCIType type) {
    if (type.isInterface()) {
      final int redundant = getModifiersFromAST(type)
          & getImplicitModifiers(type);
      if (redundant != 0) {
        addViolation(new RedundantModifiers(type, redundant));
      }
    } else if (type.isFinal()) { 
      // if BinCIType is final, no methods should be final
      final BinMethod[] methods = type.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++){
        if (methods[i].isFinal()){
          addViolation(new RedundantModifiers(methods[i], BinModifier.FINAL));
        }
      }
    }
  }

  public void visit(final BinFieldDeclaration field) {
    final BinCIType type = field.getParentType();
    if (type.isInterface()) {
      final BinField first = (BinField) field.getVariables()[0];

      final int redundant = getModifiersFromAST(first)
          & getImplicitModifiers(first);
      if (redundant != 0) {
        addViolation(new RedundantModifiers(first, redundant));
      }
    }

  }

  public void visit(final BinMethod method) {
    final BinCIType type = method.getParentType();
    if (type.isInterface()) {
      final int redundant = getModifiersFromAST(method)
          & getImplicitModifiers(method);
      if (redundant != 0) {
        addViolation(new RedundantModifiers(method, redundant));
      }
    }
  }

  static int getModifiersFromAST(final BinMember member) {
    int modifiers = 0;

    final List nodes = member.getModifierNodes();
    for (final Iterator i = nodes.iterator(); i.hasNext(); ) {
      final ASTImpl ast = (ASTImpl) i.next();
      modifiers |= ASTUtil.getModifierForAST(ast);
    }

    return modifiers;
  }

  static int getImplicitModifiers(final BinMember member) {
    if (member instanceof BinInterface) {
      return BinModifier.ABSTRACT;
    }

    if (member instanceof BinField) {
      return BinModifier.PUBLIC | BinModifier.STATIC | BinModifier.FINAL;
    }

    if (member instanceof BinMethod) {
      return BinModifier.PUBLIC | BinModifier.ABSTRACT;
    }

    return 0;
  }
}


class RedundantModifiers extends AwkwardMemberModifiers {
  private int redundantModifiers;
  
  private static final String createMessage(final BinMember type,
      final int modifiers){
    String message = "Redundant ";
    if (type instanceof BinInterface){
      message += "interface";
    } else if (type instanceof BinField){
      message += "field";
    } else if (type instanceof BinMethod){
      message += "method";
    }
    message += " modifiers: " + new BinModifierFormatter(modifiers).print();
    return message;
  }
  
  RedundantModifiers(final BinMember member, final int redundantModifiers){
    super(member, createMessage(member, redundantModifiers), "refact.audit.redundant_modifiers");
    
    this.redundantModifiers = redundantModifiers;
  }

  int getRedundantModifiers() {
    return redundantModifiers;
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveRedundantModifiers.INSTANCE);
  }
}


class RemoveRedundantModifiers extends MultiTargetCorrectiveAction {
  static final RemoveRedundantModifiers INSTANCE
      = new RemoveRedundantModifiers();

  public String getKey() {
    return "refactorit.audit.action.modifiers.remove";
  }

  public String getName() {
    return "Remove redundant modifiers";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, final RuleViolation violation) {
    if (!(violation instanceof RedundantModifiers)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    final CompilationUnit compilationUnit = violation.getCompilationUnit();

    final BinMember member = ((RedundantModifiers) violation).getOwnerMember();

    final int modifiers = BinModifier.clearFlags(
        RedundantModifiersRule.getModifiersFromAST(member),
        ((RedundantModifiers) violation).getRedundantModifiers());

    manager.add(new ModifierEditor(member, modifiers));

    return Collections.singleton(compilationUnit);
  }
}
