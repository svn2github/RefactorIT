/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classfile.ClassData;
import net.sf.refactorit.classmodel.BinAnnotation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinEnum;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeParameterManager;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariableArityParameter;
import net.sf.refactorit.classmodel.BinWildcardTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.JspCompilationUnit;
import net.sf.refactorit.classmodel.MissingBinClass;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinAnnotationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.LocationlessSourceParsingException;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Defines class for managing source file loading and binary types building
 */
public final class CompilationUnitsLoader implements JavaTokenTypes {

  private final ProjectLoader projectLoader;

  public CompilationUnitsLoader(final ProjectLoader projectLoader) {
    this.projectLoader = projectLoader;
  }

  public final void buildTypes(final ProgressListener listener,
      final List builtCompilationUnits) throws SourceParsingException {
    final int fileCount = builtCompilationUnits.size();
    for (int i = 0; i < fileCount; ++i) { // all source files
      final CompilationUnit aCompilationUnit = (CompilationUnit) builtCompilationUnits.get(i);
      if (!LoadingASTUtil.optimized) {
        listener.showMessage(aCompilationUnit.getDisplayPath());
      }
      CancelSupport.checkThreadInterrupted();

      buildTypes(aCompilationUnit);

      listener.progressHappened(ProjectLoader.PASS1_TIME
          + ProjectLoader.PASS2_TIME * (i + 1) / fileCount);
    }
    CancelSupport.checkThreadInterrupted();

    if (!LoadingASTUtil.optimized) {
      listener.showMessage("");
    }

  }

  private final void buildTypes(final CompilationUnit aCompilationUnit) throws
      SourceParsingException {

    final List definedTypes = aCompilationUnit.getDefinedTypes();

    final BodyContext bodyContext = new BodyContext(aCompilationUnit);

    for (int q = 0, max = definedTypes.size(); q < max; ++q) {
      final BinTypeRef typeRef = (BinTypeRef) definedTypes.get(q);
      if (typeRef.isBuilt()) {
        continue;
      }

if (Assert.enabled && typeRef.getBinType().getCompilationUnit()
      != aCompilationUnit) {
    Assert.must(false, "type from wrong CU: "
        + typeRef.getBinType().getCompilationUnit() + " != " + aCompilationUnit);
}
if (Assert.enabled && typeRef.getBinType().getCompilationUnit().getSource()
      != aCompilationUnit.getSource()) {
    Assert.must(false, "type from wrong CU2: "
        + typeRef.getBinType().getCompilationUnit().getSource() + " != "
        + aCompilationUnit.getSource());
}

      this.projectLoader.startProfilingTimer("build bin type");
      bodyContext.startType(typeRef);
      try {
        buildType(typeRef, bodyContext);
      } catch (SourceParsingException e) {
        if (!e.isUserFriendlyErrorReported()) {
          throw e;
        }

        try {
          ((BinTypeRef) definedTypes.get(q)).getBinCIType().setHasBuildErrors(true);
        } catch (NullPointerException ex) {
          // type was not built completely
          ex.printStackTrace();
        }
      } finally {
        bodyContext.endType();
        this.projectLoader.stopProfilingTimer();
      }
    }
  }

  static final void ensureImpliedMembers(final List builtCompilationUnits) {
    final int fileCount = builtCompilationUnits.size();
    CancelSupport.checkThreadInterrupted();
    for (int i = 0; i < fileCount; i++) { // all source files
      final CompilationUnit aCompilationUnit = (CompilationUnit) builtCompilationUnits.get(i);
      final List definedTypes = aCompilationUnit.getDefinedTypes();
      for (int t = 0; t < definedTypes.size(); ++t) {
        final BinTypeRef curTypeRef = (BinTypeRef) definedTypes.get(t);
        final BinCIType curType = curTypeRef.getBinCIType();
        if (curType instanceof BinClass) {
          ((BinClass) curType).ensureDefaultConstructor();
          ((BinClass) curType).ensureCopiedMethods();
        }
      }
    }
  }

  final void buildType(final BinTypeRef curTypeRef,
      final BodyContext bodyContext) throws SourceParsingException {
    final LoadingSourceBinCIType curType
        = (LoadingSourceBinCIType) curTypeRef.getBinType();
    if (Settings.debugLevel > 60) {
      System.out.println("DD source building " + curType.getQualifiedName());
    }
    ASTImpl typeRootNode = curType.getOffsetNode();

    if (Assert.enabled && curType.getCompilationUnit().getSource()
        != typeRootNode.getSource()) {
      Assert.must(false, "root node from wrong source1: " +
          bodyContext.getCompilationUnit().getSource()
          + " != " + typeRootNode.getSource());
    }

    if (Assert.enabled && bodyContext.getCompilationUnit().getSource()
        != typeRootNode.getSource()) {
      Assert.must(false, "root node from wrong source2: " +
          bodyContext.getCompilationUnit().getSource()
          + " != " + typeRootNode.getSource());
    }

    // Debug
    // if(counter++ < 5){
    //	(new rantlr.debug.misc.ASTFrame(curTypeRef.getQualifiedName(), typeRootNode)).setVisible(true);
    // }

//    String b_qualifiedname = curType.getQualifiedName();
    final int b_modifiers = curType.getModifiers();
    BinTypeRef b_superclass = null;
    BinTypeRef[] b_interfaces = null;
    BinTypeRef[] b_typeParams = null;
    final List constructors = new ArrayList(2);
    final List fields = new ArrayList();
    final List fieldDeclarations = new ArrayList();
    final List methods = new ArrayList();
    final List inits = new ArrayList(1);
    BinExpressionList annotationsList = null;
    String className = curType.getName();

    SourceParsingException criticalException = null;
    try {
      for (ASTImpl curNode = (ASTImpl) typeRootNode.getFirstChild();
          curNode != null; curNode = (ASTImpl) curNode.getNextSibling()) {
        final int nodeType = curNode.getType();
        switch (nodeType) {
          case MODIFIERS:
            ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(typeRootNode,
                JavaTokenTypes.MODIFIERS);
            annotationsList = buildAnnotationExpressionList(modifiersNode,
                bodyContext);
            break;

          case EXTENDS_CLAUSE:
            if (curNode.getFirstChild() == null) {
              break;
            }
            if (curType.isClass()) {
              b_superclass = buildSuperClassTypeRef(curNode, bodyContext);
              // TODO: resolving ways must be refactored, too complex and duplicated
              if (curTypeRef != null && curTypeRef.equals(b_superclass)) {
                bodyContext.getProject().getProjectLoader().getErrorCollector()
                    .addNonCriticalUserFriendlyError(new UserFriendlyError(
                    "Cyclic inheritance for: "
                    + curTypeRef.getQualifiedName()
                    + ", " + bodyContext.getCompilationUnit(),
                    bodyContext.getCompilationUnit(), curNode));
                b_superclass = this.projectLoader.getProject().getObjectRef();
              }
            } else {
              b_interfaces = buildManyCITypeRefs(curNode, bodyContext);
            }
            break;

          case IMPLEMENTS_CLAUSE:
            b_interfaces = buildManyCITypeRefs(curNode, bodyContext);
            break;

          case TYPE_PARAMETERS:
            b_typeParams = buildTypeParameters(curNode, bodyContext);
            curType.setTypeParameters(b_typeParams); // let resolver work
            break;

          case OBJBLOCK:
            for (ASTImpl obNode = (ASTImpl) curNode.getFirstChild();
                obNode != null; obNode = (ASTImpl) obNode.getNextSibling()) {
              bodyContext.getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();
              final int obType = obNode.getType();
              try {
                switch (obType) {
                  case ANNOTATION_FIELD_DEF:
//              	  final ArrayList annotationNodes = LoadingASTUtil.findDefNodesOfOneDeclaration(obNode);
//              	  obNode = LoadingASTUtil.findLastDefNodeOfDeclaration(obNode);
//
//                  final BinFieldDeclaration annotationFieldDeclaration = buildAnnotationDeclaration(
//                      annotationNodes, bodyContext, curType.isAnnotation());
//                  fieldDeclarations.add(annotationFieldDeclaration);
//                  fields.addAll(Arrays.asList(annotationFieldDeclaration.getVariables()));
//              	  break;
                  case VARIABLE_DEF: // actually field
                    final ArrayList defNodes
                        = LoadingASTUtil.findDefNodesOfOneDeclaration(obNode);
                    obNode = LoadingASTUtil.findLastDefNodeOfDeclaration(obNode);
                    final BinFieldDeclaration fieldDeclaration =
                        buildFieldDeclaration(
                        defNodes, bodyContext, curType.isInterface());
                    fieldDeclarations.add(fieldDeclaration);
                    fields.addAll(Arrays.asList(fieldDeclaration.getVariables()));
                    break;

                  case ENUM_CONSTANT_DEF:
                    final BinEnumConstant constant = MethodBodyLoader
                        .buildEnumConstant(obNode, curTypeRef, bodyContext);
                    fields.add(constant);
                    fieldDeclarations.add(
                        new BinFieldDeclaration(new BinField[] {constant},
                        obNode));
                    break;

                  case STATIC_INIT:
                  case INSTANCE_INIT:
                    inits.add(buildInitializer(obNode, bodyContext));
                    break;
                  case CTOR_DEF:
                    constructors.add(buildConstructor(obNode, bodyContext,
                        curType.getCompilationUnit()));
                    break;
                  case METHOD_DEF:
                    methods.add(buildMethod(obNode, bodyContext,
                        curType.isInterface()));
                    break;
                  default:
                    break;
                }
              } catch (RuntimeException e) {
                AppRegistry.getExceptionLogger().error(e, this.getClass());
                bodyContext.getProject().getProjectLoader().getErrorCollector()
                    .addUserFriendlyError(
                    new UserFriendlyError("Failed to build item with: " + e.toString(),
                    bodyContext.getCompilationUnit(), obNode));
              }
              bodyContext.getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();
            }
            break;

            default:
              break;
        }
      }

    } catch (SourceParsingException e) {
      // let's save it and try to build the type and then rethrow it at the end
      criticalException = e;
    }

    final BinField[] b_fields
        = (BinField[]) fields.toArray(new BinField[fields.size()]);
    final BinFieldDeclaration[] b_fieldDeclarations
        = (BinFieldDeclaration[]) fieldDeclarations.toArray(
        new BinFieldDeclaration[fieldDeclarations.size()]);
    final BinConstructor[] b_constructors
        = (BinConstructor[]) constructors.toArray(
        new BinConstructor[constructors.size()]);
    final BinMethod[] b_methods
        = (BinMethod[]) methods.toArray(new BinMethod[methods.size()]);
    final BinInitializer[] b_inits
        = (BinInitializer[]) inits.toArray(new BinInitializer[inits.size()]);

    final BinTypeRef[] b_inners = curType.getDeclaredTypes();
    final BinTypeRef b_owner = curType.getOwner();

    final BinPackage aPackage = curType.getPackage();

    // XXX: ugly hack!!!!!!!!!!!!!!!!!!! Fix ASAP!
    final int ind = className.lastIndexOf('$');
    if (ind > 0) {
      className = className.substring(ind + 1);
    }

    if (curType.isAnonymous()) {
      b_superclass = curTypeRef.getSuperclass();
      b_interfaces = curTypeRef.getInterfaces();
    }

    // FIXME: actually, resolveSuperTypes should have resolved it already, must be a bug?
    if (b_superclass == null && curType.getSuperclassName() != null) {
      b_superclass = bodyContext.getProject().createCITypeRefForName(
          curType.getSuperclassName(),
          bodyContext.getProject().getProjectLoader().getClassLoader());
    }

    // FIXME: resolver must set it itself if there was no interface names
    if (b_interfaces == null) {
      b_interfaces = BinTypeRef.NO_TYPEREFS;
    }

    if (b_superclass == null && b_interfaces.length == 0 && !curType.isInterface()) {
      b_superclass = bodyContext.getProject().getObjectRef(); // signifies missing super
    }

    BinCIType bc;
    if (curType.isEnum()) {
      bc = new BinEnum(aPackage, className,
          b_methods, b_fields, b_fieldDeclarations,
          b_constructors, b_inits,
          b_inners, b_owner,
          b_modifiers, bodyContext.getProject());
    } else if (curType.isInterface()) {
      bc = new BinInterface(aPackage, className,
          b_methods, b_fields, b_fieldDeclarations, b_inners, b_owner,
          b_modifiers, bodyContext.getProject());
    } else if(curType.isAnnotation())  {
      bc = new BinAnnotation(aPackage, className, b_methods, b_fields,
          b_fieldDeclarations, b_constructors, b_inits, b_inners, b_owner,
          b_modifiers, bodyContext.getProject());
    } else {
      bc = new BinClass(aPackage, className,
          b_methods, b_fields, b_fieldDeclarations, b_constructors, b_inits,
          b_inners, b_owner,
          b_modifiers, bodyContext.getProject());
      bc.setAnonymous(curType.isAnonymous());
    }

    bc.setLocal(curType.isLocal());
    bc.setCompilationUnit(curType.getCompilationUnit());
    bc.setOwners(curTypeRef);
    bc.setTypeParameters(b_typeParams);

    if (bc.isLocal() && !bc.isAnonymous()) {
      byte localTypePrefix = 1;
      Set locals = b_owner.getBinCIType().getLocalTypeNames();
      while (locals.contains(localTypePrefix + className)) {
        ++localTypePrefix;
      }
      locals.add(localTypePrefix + className);

      bc.setLocalPrefix(localTypePrefix);
    }

    // force resolving; it's needed for rebuildLogic.fixSubtypes
    if (b_superclass != null) {
      curTypeRef.setSuperclass(b_superclass);
      b_superclass.getBinType();
    }
    if (b_interfaces != null) {
      curTypeRef.setInterfaces(b_interfaces);
      for (int z = 0, zMax = b_interfaces.length; z < zMax; ++z) {
        b_interfaces[z].getBinType();
      }
    }

    bc.setOffsetNode(typeRootNode);

    if (curType.isAnonymous()) {
      // typeRootNode = new or DOT
      typeRootNode = (ASTImpl) typeRootNode.getFirstChild(); // skip NEW
      while (typeRootNode.getType() == DOT) {
        typeRootNode = (ASTImpl) typeRootNode.getFirstChild().getNextSibling();
      }

      final BinTypeRef explicitSupertype;
      if (b_interfaces.length > 0) {
        explicitSupertype = b_interfaces[0];
      } else {
        explicitSupertype = b_superclass;
      }
      final BinTypeRef usageRef = BinSpecificTypeRef.create(
          bodyContext.getCompilationUnit(),
          typeRootNode, explicitSupertype, true);
      if (b_interfaces.length > 0) {
        curTypeRef.setInterfaces(new BinTypeRef[] {usageRef});
      } else {
        curTypeRef.setSuperclass(usageRef);
      }

      // there is no self usage, since anonymous has no name
    } else {
      bc.setSelfUsageInfo(
          BinSpecificTypeRef.create(
          bodyContext.getCompilationUnit(),
          LoadingASTUtil.getTypeNodeFromDef(typeRootNode), curTypeRef, false));
    }

    if (curType.isEnum()) {
      ((BinEnum) bc).generateValueMethods();
    }

    if (annotationsList != null) {
      bc.setAnnotations(annotationsList);
      annotationsList.setParent(bc);
    }

    if (criticalException != null) {
      throw criticalException;
    }
  }

  public static final BinExpressionList buildAnnotationExpressionList(ASTImpl modifiersNode,
      BodyContext bodyContext) throws SourceParsingException {
    ArrayList expressionList = new ArrayList(1);
    for(ASTImpl modifierNode = (ASTImpl) modifiersNode.getFirstChild();
        modifierNode != null;
        modifierNode = (ASTImpl) modifierNode.getNextSibling()) {
      if (modifierNode.getType() == ANNOTATION) {
        BinAnnotationExpression expression = buildAnnotationExpression(modifierNode, bodyContext);
        if(expression != null) {
          expressionList.add(expression);
        }
      }
    }

    if(expressionList.size() > 0) {
      BinExpression[] expArray = (BinExpression[]) expressionList
          .toArray(new BinExpression[expressionList.size()]);
      BinExpressionList list = new BinExpressionList(expArray) { // hack ;(
        public BinMember getParentMember() {
          return (BinMember) this.getParent();
        }
      };

      // setting parents for binExpressions:
      for(int i = 0; i < expArray.length; i++) {
        expArray[i].setParent(list);
      }

      return list;
    }
    return null;
  }

  public static final BinAnnotationExpression buildAnnotationExpression(
      ASTImpl annotationNode, BodyContext bodyContext) {
    ASTImpl identNode = (ASTImpl)annotationNode.getFirstChild();

    BinTypeRef returnTypeRef = null;
    try {
      returnTypeRef = buildSpecificTypeRef(identNode, bodyContext, null);
    } catch (SourceParsingException e) {
      e.printStackTrace();
    }

    if(returnTypeRef == null) {
      return null;
    }

    BinAnnotationExpression annotationExpression =
      new BinAnnotationExpression(returnTypeRef, annotationNode);

    return annotationExpression;
  }

  private final BinFieldDeclaration buildFieldDeclaration(final ArrayList defNodes,
      final BodyContext bodyContext,
      final boolean isInterface) throws SourceParsingException {
    final BinField[] fields = new BinField[defNodes.size()];

    for (int i = 0; i < defNodes.size(); i++) {
      final ASTImpl defNode = (ASTImpl) defNodes.get(i);
      fields[i] = buildField(defNode, bodyContext, isInterface);
    }

    return new BinFieldDeclaration(fields, (ASTImpl) defNodes.get(0));
  }

  private final BinField buildField(final ASTImpl varDefNode, final BodyContext bodyContext,
      final boolean isInterface) throws SourceParsingException {
    ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(varDefNode,
        JavaTokenTypes.MODIFIERS);
    BinExpressionList annotationsList
        = buildAnnotationExpressionList(modifiersNode, bodyContext);

    final ASTImpl modifierNode = (ASTImpl) varDefNode.getFirstChild();
    int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);
    if (isInterface) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.PUBLIC | BinModifier.STATIC | BinModifier.FINAL);
    }

    final ASTImpl typeNode = (ASTImpl) modifierNode.getNextSibling();
    if (Assert.enabled && typeNode.getType() != TYPE) {
      Assert.must(false,
          " Type expected! found(" + String.valueOf(typeNode.getType()) + ")");
    }

    final BinTypeRef typeRef = buildSpecificTypeRef((ASTImpl) typeNode.getFirstChild(), bodyContext, null);

    final ASTImpl nameNode = (ASTImpl) typeNode.getNextSibling();
    if (Assert.enabled && nameNode.getType() != IDENT) {
      Assert.must(false,
          " Identifier expected! found(" + String.valueOf(typeNode.getType())
          + ")");
    }

    final ASTImpl nameDeclaration = nameNode;

    final BinField field = BinField.createByPrototype(
        nameNode.getText(), typeRef, modifiers,
        false, bodyContext.getTypeRef());

//    field.setCompilationUnit(getCompilationUnit((ASTImpl) varDefNode, bodyContext));
    field.setOffsetNode(varDefNode);
    field.setNameAst(nameDeclaration);

    if (annotationsList != null) {
      field.setAnnotations(annotationsList);
      annotationsList.setParent(field);
    }

    return field;
  }

  private static final BinInitializer buildInitializer(final ASTImpl initNode,
      final BodyContext bodyContext) throws SourceParsingException {
    final int type = initNode.getType();
    int modifiers = 0;
    String name;
    if (type == STATIC_INIT) {
      modifiers |= BinModifier.STATIC;
      name = ClassData.STATIC_INIT_NAME;
    } else {
      name = ClassData.CONSTRUCTOR_NAME;
    }

    final BinInitializer retVal = new BinInitializer(name, modifiers);

    retVal.setOwner(bodyContext.getTypeRef());
//    retVal.setCompilationUnit(getCompilationUnit(initNode, bodyContext));
    retVal.setOffsetNode(initNode);

    return retVal;
  }

  private final BinConstructor buildConstructor(final ASTImpl ctorNode,
      final BodyContext bodyContext,
      final CompilationUnit compilationUnit) throws SourceParsingException {
    ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(ctorNode,
        JavaTokenTypes.MODIFIERS);
    BinExpressionList annotationsList = buildAnnotationExpressionList(modifiersNode, bodyContext);

    final ASTImpl modifierNode = (ASTImpl) ctorNode.getFirstChild();
    ASTImpl identNode = (ASTImpl) modifierNode.getNextSibling();
    ASTImpl typeParamsNode = null;
    if (identNode.getType() == TYPE_PARAMETERS) {
      typeParamsNode = identNode;
      identNode = (ASTImpl) typeParamsNode.getNextSibling();
    }
    final ASTImpl parametersNode = (ASTImpl) identNode.getNextSibling();
    final ASTImpl throwsNode = (ASTImpl) parametersNode.getNextSibling();

    final int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);

    final BinTypeRef[] typeParameters = buildTypeParameters(typeParamsNode, bodyContext);

    final BinParameter[] params = buildParameters(parametersNode, bodyContext);

    final BinMethod.Throws[] exceptions;
    if (throwsNode != null && throwsNode.getType() == LITERAL_throws) {
      exceptions = buildThrowsClause(throwsNode, bodyContext);
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    // Resolve constructor's type
    final BinTypeRef typeRef = bodyContext.getTypeRef();
    final String constructorName = identNode.getText();
    final BinTypeRef returnType = BinSpecificTypeRef.create(
        compilationUnit, identNode, typeRef, false);

    final BinConstructor ctor = BinConstructor.createByPrototype(
        returnType, params, modifiers, exceptions, false, typeRef);

    // Set offset Node
//    ctor.setCompilationUnit(getCompilationUnit(ctorNode, bodyContext));
    ctor.setOffsetNode(ctorNode);
    ctor.setTypeParameters(typeParameters);

    if (!typeRef.getName().equals(constructorName)) {
      SourceParsingException.throwWithUserFriendlyError(
          "bad constructor name " + identNode.getText(),
          compilationUnit,
          ctorNode
          );
    }

    if (annotationsList != null) {
      ctor.setAnnotations(annotationsList);
      annotationsList.setParent(ctor);
    }

    return ctor;
  }

//  private static boolean nodeInsideBinType(
//      final ASTImpl node, final CompilationUnit ctorCompilationUnit, final BinType binType
//  ) {
//    if (binType.getCompilationUnit() != ctorCompilationUnit) {
//      return false;
//    }
//
//    return
//      node.getLine() >= binType.getStartLine() &&
//      node.getLine() <= binType.getEndLine();
//  }

  private final BinMethod buildMethod(
      final ASTImpl methodNode, final BodyContext bodyContext,
      final boolean isInterface
      ) throws SourceParsingException {
    final ASTImpl modifierNode = (ASTImpl) methodNode.getFirstChild();
    ASTImpl returnNode = (ASTImpl) modifierNode.getNextSibling();
    ASTImpl typeParamsNode = null;
    if (returnNode.getType() == TYPE_PARAMETERS) {
      typeParamsNode = returnNode;
      returnNode = (ASTImpl) typeParamsNode.getNextSibling();
    }
    final ASTImpl identNode = (ASTImpl) returnNode.getNextSibling();
    final ASTImpl parametersNode = (ASTImpl) identNode.getNextSibling();
    final ASTImpl throwsNode = (ASTImpl) parametersNode.getNextSibling();
//    final ASTImpl statementNode = SourceUtil.getStatementNode(modifierNode);

    ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(methodNode,
        JavaTokenTypes.MODIFIERS);
    BinExpressionList annotationsList = buildAnnotationExpressionList(modifiersNode, bodyContext);

    int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);
    if (isInterface) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.PUBLIC | BinModifier.ABSTRACT);
    }

    final BinTypeRef[] typeParameters = buildTypeParameters(typeParamsNode, bodyContext);

    final BinTypeRef returnType = buildSpecificTypeRef((ASTImpl) returnNode.getFirstChild(), bodyContext, null);


    final BinParameter[] params = buildParameters(parametersNode, bodyContext);

    BinMethod.Throws[] exceptions;
    if (throwsNode != null && throwsNode.getType() == LITERAL_throws) {
      exceptions = buildThrowsClause(throwsNode, bodyContext);
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    final BinMethod aMethod
        = BinMethod.createByPrototype(identNode.getText(), params,
        returnType, modifiers, exceptions, bodyContext.getTypeRef());

//    aMethod.setCompilationUnit(getCompilationUnit(methodNode, bodyContext));
    aMethod.setOffsetNode(methodNode);
    aMethod.setNameAst(identNode);
    aMethod.setTypeParameters(typeParameters);

    if (annotationsList != null) {
      aMethod.setAnnotations(annotationsList);
      annotationsList.setParent(aMethod);
    }

    return aMethod;
  }

  private final BinParameter[] buildParameters(final ASTImpl parametersNode,
      final BodyContext bodyContext) throws SourceParsingException {

    final List parameters = new ArrayList(5);
    for (ASTImpl parameterNode = (ASTImpl) parametersNode.getFirstChild();
        parameterNode != null;
        parameterNode = (ASTImpl) parameterNode.getNextSibling()) {
      final BinParameter param = buildParameter(parameterNode, bodyContext);
      parameters.add(param);
    }

    return (BinParameter[]) parameters.toArray(new BinParameter[parameters.size()]);
  }

  private final BinParameter buildParameter(final ASTImpl parameterNode,
      final BodyContext bodyContext) throws SourceParsingException {
    ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(parameterNode,
        JavaTokenTypes.MODIFIERS);
    BinExpressionList annotationsList = buildAnnotationExpressionList(modifiersNode, bodyContext);

    if (parameterNode.getType() != PARAMETER_DEF
        && parameterNode.getType() != VARIABLE_PARAMETER_DEF) {
      SourceParsingException.throwWithUserFriendlyError(
          "invalid ASTImpl node type for parameterNode: " + parameterNode,
          bodyContext.getCompilationUnit(),
          parameterNode
          );
    }

    final ASTImpl modifierNode = (ASTImpl) parameterNode.getFirstChild();
    final ASTImpl typeNode = (ASTImpl) modifierNode.getNextSibling();
    final ASTImpl identNode = (ASTImpl) typeNode.getNextSibling();

    final BinTypeRef typeRef = buildSpecificTypeRef((ASTImpl) typeNode.getFirstChild(), bodyContext, null);

    final int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);

    final BinParameter param;
    if(parameterNode.getType() == VARIABLE_PARAMETER_DEF) {
      param = new BinVariableArityParameter(identNode.getText(), typeRef,
          modifiers);
    } else {
      param = new BinParameter(identNode.getText(), typeRef, modifiers);
    }

//    param.setCompilationUnit(getCompilationUnit(parameterNode, bodyContext));
    param.setOffsetNode(parameterNode);
    param.setNameAst(identNode);

    if (annotationsList != null) {
      param.setAnnotations(annotationsList);
      annotationsList.setParent(param);
    }

    return param;
  }

  private static final BinTypeRef[] buildTypeParameters(
      final ASTImpl typeParametersNode,
      final BodyContext bodyContext) throws SourceParsingException {
    if (typeParametersNode == null) {
      return null;
    }

    bodyContext.getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();

    final ArrayList typeParameters = new ArrayList(1);
    for (ASTImpl typeParameterNode = (ASTImpl) typeParametersNode.getFirstChild();
        typeParameterNode != null;
        typeParameterNode = (ASTImpl) typeParameterNode.getNextSibling()) {
      final BinTypeRef typeRef
          = buildTypeParameterHeader(typeParameterNode, bodyContext);
      typeParameters.add(typeRef);
    }

    int i = 0;
    for (ASTImpl typeParameterNode = (ASTImpl) typeParametersNode.getFirstChild();
        typeParameterNode != null;
        typeParameterNode = (ASTImpl) typeParameterNode.getNextSibling()) {
      final BinTypeRef typeRef = (BinTypeRef) typeParameters.get(i++);
      buildTypeParameterBody(typeParameterNode, typeRef, bodyContext);
    }

    bodyContext.getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();

    return (BinTypeRef[]) typeParameters.toArray(new BinTypeRef[typeParameters.size()]);
  }

  private static BinTypeRef buildTypeParameterHeader(final ASTImpl typeParameterNode, final BodyContext bodyContext) {
    ASTImpl identNode = (ASTImpl) typeParameterNode.getFirstChild();
    if (identNode.getType() == TYPE) {
      identNode = (ASTImpl) identNode.getFirstChild();
    }

    final BinCIType stubType = new BinCIType(
        null, identNode.getText(), null, 0, bodyContext.getProject()) {
      public final boolean isInterface() {
        return false;
      }
      public final boolean isClass() {
        return false;
      }
      public final boolean isEnum() {
        return false;
      }
      public boolean isAnnotation() {
        return false;
      }
      public final String getMemberType() {
        return "type parameter";
      }
      public final boolean isTypeParameter() {
        return true;
      }
    };

    final BinTypeRef typeRef = new BinCITypeRef(stubType);
    bodyContext.addTypeRef(typeRef);

    return typeRef;
  }

  private static final BinTypeRef buildTypeParameterBody(
      final ASTImpl typeParameterNode,
      final BinTypeRef typeRef,
      final BodyContext bodyContext) throws SourceParsingException {
    ASTImpl identNode = (ASTImpl) typeParameterNode.getFirstChild();
    if (identNode.getType() == TYPE) {
      identNode = (ASTImpl) identNode.getFirstChild();
    }

    final ASTImpl boundNode = (ASTImpl) identNode.getNextSibling();

    BinTypeRef superClass = null;
    BinTypeRef[] superInterfaces = null;
    if (boundNode != null) {
//new rantlr.debug.misc.ASTFrame("bounds", boundNode).setVisible(true);
      final BinTypeRef[] supers = buildManyCITypeRefs(boundNode, bodyContext);
//System.err.println("supers: " + Arrays.asList(supers) + " - " + typeRef);
      if (supers != null) {
        final List interfaces = new ArrayList(1);
        for (int i = 0, max = supers.length; i < max; i++) {
          if (supers[i].getBinCIType().isClass()) {
            superClass = supers[i];
          } else {
            CollectionUtil.addNew(interfaces, supers[i]);
          }
        }
        superInterfaces
            = (BinTypeRef[]) interfaces.toArray(new BinTypeRef[interfaces.size()]);
      }
    }
    if (superClass == null && (superInterfaces == null
        || superInterfaces.length == 0)) {
      superClass = bodyContext.getProject().getObjectRef();
    }
    if (superInterfaces == null) {
      superInterfaces = BinTypeRef.NO_TYPEREFS;
    }

    // JAVA5: this is a hack, must be reimplemented later
    BinCIType type;
    if (superClass == null) {
      type = new BinInterface(bodyContext.getCompilationUnit().getPackage(),
          identNode.getText(), BinMethod.NO_METHODS, BinField.NO_FIELDS,
          BinFieldDeclaration.NO_FIELDDECLARATIONS,
          BinTypeRef.NO_TYPEREFS,
          bodyContext.getTypeRef(), BinModifier.PUBLIC, bodyContext.getProject()) {
        public final String getMemberType() {
          return "type parameter";
        }

        public final boolean isTypeParameter() {
          return true;
        }
      };
    } else {
      type = new BinClass(bodyContext.getCompilationUnit().getPackage(),
          identNode.getText(), BinMethod.NO_METHODS, BinField.NO_FIELDS,
          BinFieldDeclaration.NO_FIELDDECLARATIONS,
          BinConstructor.NO_CONSTRUCTORS, BinInitializer.NO_INITIALIZERS,
          BinTypeRef.NO_TYPEREFS,
          bodyContext.getTypeRef(), BinModifier.PUBLIC, bodyContext.getProject()) {
        public final String getMemberType() {
          return "type parameter";
        }

        public final boolean isTypeParameter() {
          return true;
        }
      };
    }
    type.setCompilationUnit(bodyContext.getCompilationUnit());
    type.setOffsetNode(typeParameterNode);
    type.setTypeRef(typeRef);
    typeRef.setBinType(type);
    typeRef.setSuperclass(superClass);
    typeRef.setInterfaces(superInterfaces);
    typeRef.setResolver(
        Resolver.getForSourceType(typeRef, bodyContext.getCompilationUnit()));

    final BinTypeRef specTypeRef = BinSpecificTypeRef.create(
            bodyContext.getCompilationUnit(), identNode, typeRef, false);
    type.setSelfUsageInfo(specTypeRef);

    if (type.isClass()) {
      ((BinClass) type).ensureCopiedMethods();
    }

    return typeRef;
  }

  public static final BinTypeRef[] buildTypeArguments(
      final ASTImpl typeArgumentsNode,
      final BinTypeParameterManager argumentOwner,
      final BodyContext bodyContext) throws SourceParsingException {
    if (typeArgumentsNode == null) {
      return null;
    }

    bodyContext.getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();

    final ArrayList result = new ArrayList(1);
    for (ASTImpl typeArgumentNode = (ASTImpl) typeArgumentsNode.getFirstChild();
        typeArgumentNode != null;
        typeArgumentNode = (ASTImpl) typeArgumentNode.getNextSibling()) {
      BinTypeRef typeRef
          = buildTypeArgument(typeArgumentNode, argumentOwner,
              result.size(), bodyContext);
      result.add(typeRef);
    }

    bodyContext.getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();

    return (BinTypeRef[]) result.toArray(new BinTypeRef[result.size()]);
  }

  private static final BinTypeRef buildTypeArgument(final ASTImpl typeArgumentNode,
      final BinTypeParameterManager argumentOwner,
      final int argumentPosition, final BodyContext bodyContext)
      throws SourceParsingException {
    ASTImpl identNode = (ASTImpl) typeArgumentNode.getFirstChild();
    BinTypeRef argumentRef = null;

    if (identNode.getType() == WILDCARD_TYPE) {
//      = new BinWildcardTypeRef(bodyContext.getProject().getObjectRef().getBinCIType());
      argumentRef = new BinWildcardTypeRef(
          bodyContext.getTypeRef().getPackage(), bodyContext.getTypeRef(),
          bodyContext.getProject());

      ASTImpl boundNode = (ASTImpl) identNode.getFirstChild();
      while (boundNode != null) {
        switch (boundNode.getType()) {
          case TYPE_UPPER_BOUNDS: {
            ASTImpl boundIdent = (ASTImpl) boundNode.getFirstChild();
            BinTypeRef boundRef = buildSpecificTypeRef(boundIdent, bodyContext, null);
//            if (Assert.enabled) {
//            Assert.must(argumentRef == null,
//                "has wildcardRef already: " + argumentRef, identNode);
//            }
            argumentRef.setUpperBound(boundRef);
          }
            break;

          case TYPE_LOWER_BOUNDS: // well, small hack
          {
            ASTImpl boundIdent = (ASTImpl) boundNode.getFirstChild();
            BinTypeRef boundRef = buildSpecificTypeRef(boundIdent, bodyContext, null);
          //            if (Assert.enabled) {
//            Assert.must(argumentRef == null,
//                "has wildcardRef already: " + argumentRef, identNode);
          //            }
            argumentRef.setLowerBound(boundRef);
          }
            break;

          default:
            if (Assert.enabled) {
              Assert.must(false, "Expected BOUNDS: " + boundNode);
            }
            break;
        }

        boundNode = (ASTImpl) boundNode.getNextSibling();
      }
//      if (argumentRef.getUpperBound() == null && argumentRef.getLowerBound() == null) {
////        argumentRef = BinSpecificTypeRef.create(
////            bodyContext.getProject().getObjectRef());
//        argumentRef.setUpperBound(bodyContext.getProject().getObjectRef());
//      }
    } else {
      identNode = (ASTImpl) identNode.getFirstChild();
      argumentRef = buildSpecificTypeRef(identNode, bodyContext, null);
    }

    if (argumentOwner != null) { // will be set later otherwise
      if (argumentRef instanceof BinWildcardTypeRef) {
        ((BinWildcardTypeRef) argumentRef).setTypeParameterResolver(
            argumentOwner, argumentPosition);
      }
    }

    if (argumentOwner != null) { // will be set later otherwise
      if (!argumentRef.isSpecific()) { // hmm?
        argumentRef = BinSpecificTypeRef.create(argumentRef);
      }
      ((BinSpecificTypeRef) argumentRef).setTypeParameterResolver(
          argumentOwner, argumentPosition);
    }

    return argumentRef;
  }

  /** Used usually to build implemented interfaces refs */
  private static final BinTypeRef[] buildManyCITypeRefs(
      final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final ArrayList result = new ArrayList(3);
    for (ASTImpl refNode = (ASTImpl) node.getFirstChild();
        refNode != null; refNode = (ASTImpl) refNode.getNextSibling()) {

      final String typeName = LoadingASTUtil.combineIdentsAndDots(refNode);
//System.err.println("typeName: " + typeName);
      BinTypeRef typeRef = null;
      try {
        typeRef = bodyContext.getTypeRefForName(typeName);
      } catch (LocationlessSourceParsingException e) {
        SourceParsingException.rethrowWithUserFriendlyError(e, node);
      }
      if (typeRef != null && typeRef.getBinCIType() != null) {
        final BinTypeRef specificTypeRef = BinSpecificTypeRef.create(
            bodyContext.getCompilationUnit(), refNode, typeRef, true);

        final ASTImpl argumentsNode = LoadingASTUtil.findTypeArgumentsNode(refNode);
        if (argumentsNode != null) {
          specificTypeRef.setTypeArguments(
              buildTypeArguments(argumentsNode, specificTypeRef, bodyContext));
        }

        result.add(specificTypeRef);
      } else {
        bodyContext.getProject().getProjectLoader().getErrorCollector()
            .addNonCriticalUserFriendlyError(
            new UserFriendlyError("Type not found: " + typeName,
                bodyContext.getCompilationUnit(), refNode));
      }
    }

    return (BinTypeRef[]) result.toArray(new BinTypeRef[result.size()]);
  }

  private final BinMethod.Throws[] buildThrowsClause(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final List result = new ArrayList(2);
    for (ASTImpl refNode = (ASTImpl) node.getFirstChild(); refNode != null;
        refNode = (ASTImpl) refNode.getNextSibling()) {

      final String typeName = LoadingASTUtil.combineIdentsAndDots(refNode);
      BinTypeRef typeRef = null;
      try {
        typeRef = bodyContext.getTypeRefForName(typeName);
      } catch (LocationlessSourceParsingException e) {
        SourceParsingException.rethrowWithUserFriendlyError(e, node);
      }
      if (typeRef != null) {
        final BinMethod.Throws throwz
            = new BinMethod.Throws(BinSpecificTypeRef.create(
            bodyContext.getCompilationUnit(), refNode, typeRef, true));
        result.add(throwz);
      } else {
        this.projectLoader.getErrorCollector()
            .addNonCriticalUserFriendlyError(
            new UserFriendlyError("Type not found: " + typeName,
                bodyContext.getCompilationUnit(), refNode));
      }

    }

    return (BinMethod.Throws[]) result.toArray(
        new BinMethod.Throws[result.size()]);
  }

  private final BinTypeRef buildSuperClassTypeRef(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    final ASTImpl typeRefIdentifier = (ASTImpl) node.getFirstChild();

    final String typeName = LoadingASTUtil.combineIdentsAndDots(typeRefIdentifier);

    BinTypeRef typeRef = null;
    try {
      typeRef = bodyContext.getTypeRefForName(typeName);
    } catch (LocationlessSourceParsingException e) {
      SourceParsingException.rethrowWithUserFriendlyError(e, node);
    }

    if (typeRef != null && typeRef.getBinType() != null) {
      if (typeRef.getBinCIType().isClass()) {
        final BinTypeRef specificTypeRef = BinSpecificTypeRef.create(
            bodyContext.getCompilationUnit(),
            typeRefIdentifier, typeRef, true);

        final ASTImpl argumentsNode
            = LoadingASTUtil.findTypeArgumentsNode(typeRefIdentifier);
        if (argumentsNode != null) {
          specificTypeRef.setTypeArguments(
              buildTypeArguments(argumentsNode, specificTypeRef, bodyContext));
        }

        typeRef = specificTypeRef;
      } else {
        typeRef = this.projectLoader.getProject().getObjectRef();
        this.projectLoader.getErrorCollector()
            .addNonCriticalUserFriendlyError(
            new UserFriendlyError("Identifier used in extends clause : "
            + typeRefIdentifier, bodyContext.getCompilationUnit(),
            typeRefIdentifier));
      }
    } else {
      this.projectLoader.getErrorCollector()
          .addNonCriticalUserFriendlyError(new UserFriendlyError(
            "Class not found: " + typeRefIdentifier, bodyContext.getCompilationUnit(),
            typeRefIdentifier));
    }

    return typeRef;
  }

  final void addJspDependencies(final List buildedList) {
    // FIXME: remove if not needed
    final Iterator it = buildedList.iterator();
    CompilationUnit compilationUnit = null;
    final ASTTreeCache cache = this.projectLoader.getAstTreeCache();

    while (it.hasNext()) {
      compilationUnit = (CompilationUnit) it.next();

      if (compilationUnit instanceof JspCompilationUnit) {

        final FileParsingData data = cache.checkJSPCacheFor(compilationUnit.getSource());
        if (data == null) {
          Assert.must(false);
          continue;
        }
        Source includedSource = null;
        final Iterator includedIterator = data.jpi.getIncludedPages().iterator();

        while (includedIterator.hasNext()) {
          includedSource = (Source) includedIterator.next();
          final String includedJspClassName = JspUtil.getClassName(includedSource.
              getName());

          final BinTypeRef includedJspRef
              = this.projectLoader.getProject().findTypeRefForName(includedJspClassName);
          if (includedJspRef == null) {
            AppRegistry.getLogger(this.getClass()).debug(includedJspClassName + " not found in project");
            continue;
          }

          final Iterator currentSourceTypes = compilationUnit.getDefinedTypes().iterator();

          while (currentSourceTypes.hasNext()) {
            final BinTypeRef dependent = (BinTypeRef) currentSourceTypes.next();
            AppRegistry.getLogger(this.getClass()).debug("marked " + dependent + " dependent from "
            + includedJspRef.getName());
            ((DependencyParticipant) includedJspRef).addDependable(dependent);
          }
        }

      }

    }

  }

  // FIXME: doesn't it belong to ASTTreeCache class?
  public final void clearJspCache(final Collection buildList) {
    final ASTTreeCache cache = this.projectLoader.getAstTreeCache();

    if (cache.isJspCacheEmpty()) {
      return;
    }

    final Iterator it = buildList.iterator();
    Source source = null;

    while (it.hasNext()) {
      source = (Source) it.next();

      if (FileUtil.isJspFile(source)) {
        cache.removeJspSource(source);
      }

    }
  }

  public static final CompilationUnit getCompilationUnit(final ASTImpl node,
      final BodyContext context) {
    if (Assert.enabled) {
      if (node == null) {
        Assert.must(false, "node is null");
      }
    }

    CompilationUnit source;

// Causes serious problems and doesn't solve multi-source problem of JSP
//    source = context.getProject().getCompilationUnitForName(
//        ((ASTImpl) node).getSource().getRelativePath());

//    if (source == null) {
//      final BinMember block = context.getBlock();
//      if (block != null) {
//        source = block.getCompilationUnit();
//      } else {
    source = context.getCompilationUnit();
//      }
//    }

    return source;
  }

  /**
   * @return BinTypeRef for primitive types and arrays of primitives
   * or BinSpecificTypeRef for any object type
   */
  static final BinTypeRef buildSpecificTypeRef(final ASTImpl typeNode,
      final BodyContext bodyContext, final BinTypeRef superRef)
      throws SourceParsingException {
    // FIXME this is temporary to get less critical errors on missing types
    bodyContext.getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();

    ASTImpl typeDeclNode = typeNode;//.getFirstChild();
//if (Assert.enabled) {
//  Assert.must(typeDeclNode.getType() != TYPE, "Wrong node: " + typeDeclNode);
//}
    int dimensions = 0;

    while (typeDeclNode.getType() == ARRAY_DECLARATOR) {
      dimensions++;
      typeDeclNode = (ASTImpl) typeDeclNode.getFirstChild();
      if (typeDeclNode == null) {
        SourceParsingException.throwWithUserFriendlyError(
            "Missing array type", bodyContext.getCompilationUnit(), typeDeclNode);
      }
    }
    if (typeDeclNode.getParent().getParent().getType() == VARIABLE_PARAMETER_DEF) {
      dimensions++;
    }

    // this one for new Object[0][0]
    ASTImpl arrayDimensions = (ASTImpl) typeDeclNode.getNextSibling();
    while (arrayDimensions != null && arrayDimensions.getType() == ARRAY_DECLARATOR) {
      dimensions++;
      arrayDimensions = (ASTImpl) arrayDimensions.getFirstChild();
    }

    String typeName;
    if (typeDeclNode.getType() == DOT) {
      typeName = LoadingASTUtil.combineIdentsAndDots(typeDeclNode);
    } else {
      typeName = typeDeclNode.getText();
    }
    BinTypeRef typeRef = Project.findPrimitiveTypeForName(typeName);

    if (typeRef == null) {
      try {
        if (superRef == null) {
          typeRef = bodyContext.getTypeRefForName(typeName);
//if (Assert.enabled) {
//  Assert.must(!typeRef.isSpecific(), "Got specific typeRef: " + typeRef);
//}
        } else {
          String testTypeName = null;
          final BinTypeRef[] hierarchyList = getHierarchyListForTypeRef(superRef);

          for (int i = 0; i < hierarchyList.length; ++i) {
            final String superTypeName = hierarchyList[i].getQualifiedName();
            testTypeName = superTypeName + '$' + typeName;
            // NOTE: it is already context there owner defined, so doesn't find local of this owner
            typeRef = bodyContext.getTypeRefForName(testTypeName);

            if (typeRef != null) {
              break;
            }
          } // end for that tests all possible names
        }
      } catch (LocationlessSourceParsingException e) {
        SourceParsingException.rethrowWithUserFriendlyError(e, typeDeclNode);
      }
    }

    if (typeRef == null) {
      // FIXME error message is not adequate
      bodyContext.getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(new UserFriendlyError(
            "Type not found: " + typeName,
            bodyContext.getCompilationUnit(), typeDeclNode));
      final BinCIType missing = MissingBinClass.createMissingBinClass(typeName,
          bodyContext.getProject());
      typeRef = bodyContext.getProject().createLocalTypeRefForType(missing);
    }

    if (dimensions > 0) {
      typeRef = bodyContext.getProject().createArrayTypeForType(typeRef, dimensions);
    }

    final BinTypeRef specificTypeRef = BinSpecificTypeRef.create(
        bodyContext.getCompilationUnit(), typeDeclNode, typeRef, true);

    final ASTImpl argumentsNode = LoadingASTUtil.findTypeArgumentsNode(typeDeclNode);
    if (argumentsNode != null) {
      specificTypeRef.setTypeArguments(
          buildTypeArguments(argumentsNode, specificTypeRef, bodyContext));
    }

    // FIXME this is temporary to get less critical errors on missing types
    bodyContext.getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();

    return specificTypeRef;
  }

  private static final BinTypeRef[] getHierarchyListForTypeRef(final BinTypeRef aType) {
    final ArrayList foundTypes = new ArrayList(5);
    getHierarchyListForTypeRef(aType, foundTypes);
    return (BinTypeRef[]) foundTypes.toArray(new BinTypeRef[foundTypes.size()]);
  }

  private static final void getHierarchyListForTypeRef(final BinTypeRef aType,
      final ArrayList foundTypes) {
    if (foundTypes.contains(aType)) {
      return;
    }
    foundTypes.add(aType);

    final BinTypeRef superClass = aType.getSuperclass();
    if (superClass != null) {
      getHierarchyListForTypeRef(superClass, foundTypes);
    }

    final BinTypeRef[] interfaces = aType.getInterfaces();
    for (int i = 0; i < interfaces.length; ++i) {
      getHierarchyListForTypeRef(interfaces[i], foundTypes);
    }
  }
}
