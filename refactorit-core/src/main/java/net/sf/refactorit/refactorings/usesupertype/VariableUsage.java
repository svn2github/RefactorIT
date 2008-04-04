/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.Transformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.RuntimePlatform;

import java.util.List;




/**
 * @author Tonis Vaga
 */
class VariableUsage extends TypeUsage {
  private BinVariable var;
  private boolean restricted;

  VariableUsage(BinVariable var) {
    super(var);
    this.var = var;
  }

  BinVariable getVariable() {
    return var;
  }
  
  public void addTypeEditors(BinCIType superType,
      final TransformationList transList, ImportManager importManager) {

    try {
    	importManager.addExtraImports(superType.getTypeRef(), var.getOwner());
    	addVariableTypeEditors(var, superType, transList, false);
    } catch (AmbiguousImportImportException e) {
    	addVariableTypeEditors(var, superType, transList, true);
    }
  }

  protected static void addVariableTypeEditors(BinVariable var,
      BinCIType superType, final TransformationList transList, boolean useFqn) {
    //    ASTImpl typeNode = null;
    SourceConstruct member = var;
    //if (!(var.getParent() instanceof BinVariableDeclaration));

    if (member instanceof BinVariable) {
      Transformation rTransformation = createVarRenameTransformation((BinVariable)member,
          superType, useFqn);
      if (rTransformation != null) {
        transList.add(rTransformation);
      }
      //      BinItemVisitable expr = ((BinVariable) member).getExpression();
      //
      //      // cast type editing is happening elsewhere
      //      if (expr instanceof BinCastExpression) {
      //        Editor castEditor =
      // UseSuperTypeUtil.createCastTypeEditor((BinCastExpression)expr,
      // superType);
      //        if (castEditor != null) {
      //          editor.addEditor(castEditor);
      //          DebugInfo.trace("changed cast expression " + expr);
      //        } else {
      //          DebugInfo.trace("castExpression editor returned== null");
      //        }
      //      }
    } else if (member instanceof BinVariableDeclaration) {
      BinVariable vars[] = ((BinVariableDeclaration) member).getVariables();
      for (int i = 0; i < vars.length; i++) {
        if (i == 0) {
          Transformation varTransformation = createVarRenameTransformation(vars[i], superType, useFqn);
          if (varTransformation != null) {
            transList.add(varTransformation);
          }
        }
        //        if (vars[i].getExpression() instanceof BinCastExpression) {
        //          Editor castEditor =
        // UseSuperTypeUtil.createCastTypeEditor((BinCastExpression)vars[i].getExpression(),
        // superType);
        //          if (castEditor != null) {
        //            editor.addEditor(castEditor);
        //          }
        //        }
      }

    } else if (member instanceof BinCastExpression) {
      Assert.must(false);
      //      Editor castEditor =
      // UseSuperTypeUtil.createCastTypeEditor((BinCastExpression)member,
      // superType);
      //      if (castEditor != null) {
      //        editor.addEditor(castEditor);
      //        DebugInfo.trace("changed cast expression " + member);
      //      } else {
      //        DebugInfo.trace("castExpression editor returned== null");
      //      }
    } else {
      AppRegistry.getLogger(VariableUsage.class).debug("item type " + member.getClass() + " not supported");
    }
  }

  private static Transformation createVarRenameTransformation(BinVariable
      member,
      BinCIType superType,
			boolean useFqn) {
    ASTImpl typeNode = member.getTypeAst();
    
    if(member.getTypeRef().isArray()){
      while(typeNode.getType() == JavaTokenTypes.ARRAY_DECLARATOR){
        typeNode = (ASTImpl)typeNode.getFirstChild();
      }
    }
    
    if (typeNode != null) {
      typeNode = CompoundASTImpl.compoundTypeAST(typeNode);
    }

    if (typeNode == null) {
      // FIXME: this happened when I run it on CompilationUnit->SourceHolder 200411121030
      RuntimePlatform.console.println("Wrong AST for "
          + member.getQualifiedName() + ": " + member.getOwner());
      return null;
    }
    
    BinTypeRef existingTypeRef = member.getTypeRef();
    
    String newTypeDesc = UseSuperTypeUtil.formatWithTypeArguments(superType, useFqn, existingTypeRef);
    
    Transformation rTransformation = (new RenameTransformation(member
        .getCompilationUnit(), typeNode, newTypeDesc));
    return rTransformation;
  }


  public static VariableUsage create(
      net.sf.refactorit.classmodel.BinVariable var) {
    if ( var instanceof BinCatchParameter ) {
      return new CatchParameterUsage((BinCatchParameter) var);
    } else if ( var instanceof BinParameter ) {
      return new ParameterUsage((BinParameter) var);
    } else {
      return new VariableUsage(var);
    }
  }
  
  public boolean checkCanUseSuper(SuperClassInfo superInf,
      List resolvedMembers, List failureReasons) {
    if(restricted) {
      addFailureReason(failureReasons,"Variable is used in non java5 conditional");
      return false;
    }
    return super.checkCanUseSuper(superInf, resolvedMembers, failureReasons);
  }

  public void setRestricted(boolean restricted) {
    this.restricted = restricted;
  }
}
