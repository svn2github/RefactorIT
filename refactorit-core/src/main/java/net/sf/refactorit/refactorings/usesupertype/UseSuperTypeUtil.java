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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.Transformation;

import rantlr.collections.AST;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * @author Tonis Vaga
 */
public final class UseSuperTypeUtil {
  public static boolean checkAllOverridenInSourcePath(final BinMethod method) {
    List allMethods = method.findAllOverridesOverriddenInHierarchy();
    allMethods.add(method);

    for (int index = 0; index < allMethods.size(); ++index) {
      BinMethod item = (BinMethod) allMethods.get(index);

      if (item == null) {
        continue;
      }

      if (!item.getOwner().getBinCIType().isFromCompilationUnit()) {
        return false;
      }
    }

    return true;
  }

  /**
   *
   * @param method
   * @return all overrides && overloades list this method has, method isn't
   * included into list.
   */
  public static List getAllOverrides(BinMethod method) {
    return method.findAllOverridesOverriddenInHierarchy();

//    if (method.isStatic()) {
//      return (new ArrayList(0));
//    }
//
//    ManagingIndexer supervisor = new ManagingIndexer(ProgressMonitor.Progress.DONT_SHOW);
//    BinMethodSearchFilter filter = new BinMethodSearchFilter(false, true,true,true,false,false,false,false);
//    new MethodIndexer(supervisor, method, filter);
//    supervisor.visit(method.getProject());
//    List invocations = supervisor.getInvocations();
//    List result = (new ArrayList(invocations.size()));
//    for (int index = 0; index < invocations.size(); index++) {
//      InvocationData item = (InvocationData)invocations.get(index);
//      result.add(item.getWhere());
//    }
//
//    return result;
  }

  /**
   * Creates editors which change cast expression type. NB! If expression type
   * is array then only array type is changed
   * @param castExpression
   * @param newType
   */

  public static Transformation createCastTypeEditor(BinCastExpression
      castExpression, BinCIType newType, boolean useFqn) {
    AST firstChild = castExpression.getRootAst().getFirstChild();
    if (firstChild == null) {
      return null;
    } else {
      final ASTImpl rootAst = CompoundASTImpl.compoundTypeAST((ASTImpl)
          firstChild.getFirstChild());
      return (new RenameTransformation(castExpression.getCompilationUnit(), rootAst,
          useFqn ? newType.getQualifiedName() : newType.getName()));
    }
  }

  public static BinTypeRef getOwnerType(SourceConstruct par) {
    return par.getOwner();
  }

  public static List getMemberInvocations(final BinMember member) {
    SearchFilter filter = null;

    if (member instanceof BinMethod) {
      filter = new BinMethodSearchFilter(true, true, true, true, false, false, true, false, false);
    } else {
      filter = new BinClassSearchFilter(false, false);
    }

    return Finder.getInvocations(member.getProject(), member, filter);
  }

  public static BinTypeRef getTargetType(BinMember item) {
    BinTypeRef targetType = null;

    if (item instanceof BinVariable) {
      BinVariable var = (BinVariable) item;
      targetType = var.getTypeRef();
    } else if (item instanceof BinMethod) {
      targetType = ((BinMethod) item).getReturnType();
    } else if (item instanceof BinType) {
      targetType = ((BinType) item).getTypeRef();
    }
    return targetType.getNonArrayType();
  }

  public static Set getAllSubtypes(BinTypeRef supertype) {
    if ( supertype == null ) {
      return new HashSet(0);
    }
    final Project project = supertype.getProject();
    project.discoverAllUsedTypes();


    List allSubclasses = supertype.getAllSubclasses();
    HashSet result = new HashSet(allSubclasses.size());
    for (int i = 0, max = allSubclasses.size(); i < max; i++) {
      BinTypeRef sub = ((BinTypeRef) allSubclasses.get(i)).getNonArrayType();
      if (sub.getBinType().isWildcard()
          || sub.getBinType().isTypeParameter()
          || sub.isPrimitiveType() || sub.getBinCIType().isPreprocessedSource() ) {
        continue;
      }
      result.add(sub);
    }
    // String type not supported in usesupertype
    result.remove(project.getTypeRefForName("java.lang.String"));

    return result;
  }

  /**
   * @param superType
   * @param useFqn
   * @param existingTypeRef
   * @return
   */
  protected static String formatWithTypeArguments(BinCIType superType, boolean useFqn, BinTypeRef existingTypeRef) {
    StringBuffer newTypeDesc = new StringBuffer(useFqn ? BinFormatter.formatQualified(superType) : BinFormatter.formatNotQualified(superType.getTypeRef()));


    if ((existingTypeRef.getTypeParameters() != null) &&
        (existingTypeRef.getTypeParameters().length > 0) &&
        (existingTypeRef.getTypeArguments() != null) &&
        (existingTypeRef.getTypeArguments().length > 0) &&
        (superType.getTypeParameters() != null) &&
        (superType.getTypeParameters().length > 0)) {
      BinTypeRef[] superTypeArguments = new BinTypeRef[superType.getTypeParameters().length];
      // remap type arguments
      List memberTypeParameterList = Arrays.asList(existingTypeRef.getTypeParameters());
      for (int i = 0; i < superType.getTypeParameters().length; i++) {
        BinTypeRef parameterRef = superType.getTypeParameters()[i];
        superTypeArguments[i] = existingTypeRef.getTypeArguments()
        	[memberTypeParameterList.indexOf(existingTypeRef.getTypeParameter(parameterRef.getName()))];

      }
      BinFormatter.formatTypeArguments(newTypeDesc, superTypeArguments, true);
    }
    return newTypeDesc.toString();
  }
}
