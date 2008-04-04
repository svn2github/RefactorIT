/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinGenericTypeRef;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinParentFinder;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinEmptyExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinAssertStatement;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinEmptyStatement;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinSynchronizedStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.LocationlessSourceParsingException;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.ArrayList;
import java.util.List;


/**
 * This class for extracting method bodies.
 */
public final class MethodBodyLoader implements JavaTokenTypes {

  MethodBodyLoader() {
  }

  private static final boolean isLazyLoading() {
    // N.B! If you need to test without lazyloading comment in one line in Project.java
    // search for comment lazyloading
    return true;
  }

  private final void buildExpressionsForFields(final BinTypeRef curTypeRef,
      final BodyContext bodyContext) {
    final BinField[] fields = curTypeRef.getBinCIType().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      try {
        buildExpressionForField(fields[i], bodyContext);
        BinParentFinder.findParentsFor(fields[i]);
      } catch (SourceParsingException se) {
        (bodyContext.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(se.getUserFriendlyError());
      }
    }
  }

  private final BinExpression buildVariableInitExpression(final ASTImpl varNode,
      final BodyContext bodyContext) throws SourceParsingException {
    final ASTImpl assignNode
        = (ASTImpl) varNode.getFirstChild().getNextSibling().getNextSibling()
        .getNextSibling(); // modifiers node, type node, ident node must exist...
    BinExpression expression = null;

    if (assignNode != null) {
      if (Assert.enabled && assignNode.getType() != ASSIGN) {
        Assert.must(false, " Assigment expected");
      }
      final ASTImpl expressionNode = (ASTImpl) assignNode.getFirstChild();
      if (expressionNode.getType() == ARRAY_INIT) {
        expression = buildArrayInitExpression(expressionNode, bodyContext);
      } else {
        expression = buildExpressionForExpr(expressionNode, bodyContext);
      }
    }

    return expression;
  }

  public final BinExpression buildAnnotationFieldExpression(
      ASTImpl mainNode, BodyContext bodyContext) throws SourceParsingException {
    // mainNode is a Annotation->(Child)ident->(sibling)mainNode
    if(mainNode.getType() == JavaTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {

      final ASTImpl leftExpressionNode = (ASTImpl) mainNode.getFirstChild();
      final ASTImpl rightExpressionNode = (ASTImpl) leftExpressionNode.
          getNextSibling();

      BinExpression leftExpression = buildIdentExpression(leftExpressionNode,
          bodyContext, true);

      BinExpression rightExpression = buildExpression(rightExpressionNode, bodyContext);
      if(rightExpression == null) {
        return null;
      }

      ASTImpl rootNode = mainNode;
      BinAssignmentExpression expression = new BinAssignmentExpression(leftExpression, rightExpression, rootNode);

      return expression;
    } else {
      BinExpression expression = buildExpression(mainNode, bodyContext);
      return expression;
    }
  }

  public final void buildExpressionForField(final BinField field,
      final BodyContext bodyContext) throws SourceParsingException {

    if (field.hasExpression()) {
      return;
    }

    final ASTImpl fieldNode = field.getOffsetNode();

    // modifiers node, type node, ident node must exist...
    try {
      final ASTImpl identNode
          = (ASTImpl) fieldNode.getFirstChild().getNextSibling().getNextSibling();
      final ASTImpl assignNode = (ASTImpl) identNode.getNextSibling();
      // check in buildField()
      if (assignNode != null) {
        if (Assert.enabled && assignNode.getType() != ASSIGN) {
    //        if (assignNode.getType() != ASSIGN) {
    //          new rantlr.debug.misc.ASTFrame(" Assignment expected: " + assignNode
    //              + ", for field: "
    //              + field.getQualifiedName(), fieldNode).setVisible(true);
    //        }
          Assert.must(false,
              " Assignment expected: " + assignNode + ", for field: "
              + field.getQualifiedName());
        }
        final ASTImpl expressionNode = (ASTImpl) assignNode.getFirstChild();
        if (expressionNode != null) {
          BinExpression expression = null;
          switch (expressionNode.getType()) {
            case ARRAY_INIT:
            case ANNOTATION_ARRAY_INIT:
              expression = buildArrayInitExpression(expressionNode, bodyContext);
              break;
            case EXPR:
              expression = buildExpressionForExpr(expressionNode, bodyContext);
              break;
            case ANNOTATION:
              expression = buildExpression(expressionNode, bodyContext);
              break;
            default:
              break;
          }
          field.setExpression(expression);
        } else {
          // FIXME the handler might be more generic one
          userFriendlyError("Failed to parse init expression of: " + field,
              bodyContext, fieldNode);
        }
      }
    } catch (NullPointerException e) {
      userFriendlyError("Failed to parse init expression of: " + field,
          bodyContext, fieldNode);
    }
  }

  public final void buildExpressionForEnumConstant(
      final BinEnumConstant enumConstant,
      final BodyContext bodyContext) throws SourceParsingException {

    if (enumConstant.hasExpression()) {
      return;
    }

    final ASTImpl node = enumConstant.getOffsetNode();

    final ASTImpl annotation = (ASTImpl) node.getFirstChild();

    BinExpressionList annotationsList = CompilationUnitsLoader
        .buildAnnotationExpressionList(annotation, bodyContext);

    final ASTImpl ident = (ASTImpl) annotation.getNextSibling();
    final ASTImpl exprListNode = (ASTImpl) ident.getNextSibling();

    final BinExpressionList expressionList;
    if (exprListNode != null && exprListNode.getType() == ELIST) {
      expressionList = buildExpressionList(exprListNode, bodyContext);
    } else {
      expressionList = BinExpressionList.NO_EXPRESSIONLIST;
    }
    bodyContext.setExpressionList(expressionList);

    BinTypeRef enumConstantType = null;
    BinCITypesDefStatement def = null;
    try {
      enumConstantType
          = buildAnonymousType(node, enumConstant.getOwner(),
          ident.getText(), bodyContext);
    } catch (Exception e) {
      e.printStackTrace();
    }
    def = new BinCITypesDefStatement(enumConstantType, node);

    final BinExpression expression
        = new BinNewExpression(enumConstantType, expressionList, null, null,
        def, node);
    enumConstant.setExpression(expression);

    // add dependency for constructor type / enum constant types owner
    ((DependencyParticipant) enumConstant.getOwner()).addDependable(bodyContext.getTypeRef());

    if (annotationsList != null) {
      enumConstant.setAnnotations(annotationsList);
      annotationsList.setParent(expression);
    }

//      } else {
//        // FIXME the handler might be more generic one
//        userFriendlyError("Failed to parse init expression of: " + enumConstant,
//            bodyContext, fieldNode);
//      }
//    }

  }

  public final void buildFieldsAndMethodBodys(final BinTypeRef curTypeRef,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 40) {
      System.out.println("Building fields for: " + curTypeRef.getQualifiedName());
    }

    if (curTypeRef.getBinCIType().isLocal() || !isLazyLoading()) {
      buildExpressionsForFields(curTypeRef, bodyContext);
    }

    if (Settings.debugLevel > 40) {
      System.out.println("Building Constructors for: "
          + curTypeRef.getQualifiedName());
    }

    if (curTypeRef.getBinCIType().isClass()) {
      if (curTypeRef.getBinCIType().isLocal() || !isLazyLoading()) {
        final BinConstructor[] constructors

            = ((BinClass) curTypeRef.getBinCIType()).getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
          buildMethodBody(constructors[i], bodyContext);
        }

        final BinInitializer[] initializers
            = ((BinClass) curTypeRef.getBinCIType()).getInitializers();
        for (int i = 0; i < initializers.length; ++i) {
          buildInitializerBody(initializers[i], bodyContext);
        }
      }
    }

    if (Settings.debugLevel > 40) {
      System.out.println("Building method bodys for: "
          + curTypeRef.getQualifiedName());
    }

    if (curTypeRef.getBinCIType().isLocal() || !isLazyLoading()) {
      final BinMethod[] methods = curTypeRef.getBinCIType().getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        buildMethodBody(methods[i], bodyContext);
      }
    }
  }

  public final void buildFieldsAndMethodBodys(
      final ProgressListener listener,
      final List builtCompilationUnits) throws SourceParsingException {
    final int fileCount = builtCompilationUnits.size();
    for (int i = 0; i < fileCount; i++) { // all source files

      CancelSupport.checkThreadInterrupted();

      final CompilationUnit aCompilationUnit
          = (CompilationUnit) builtCompilationUnits.get(i);
      if (!LoadingASTUtil.optimized) {
        listener.showMessage(aCompilationUnit.getDisplayPath());
      }
      buildFieldsAndMethodBodys(aCompilationUnit);
      listener.progressHappened(ProjectLoader.PASS1_TIME +
          ProjectLoader.PASS2_TIME +
          ProjectLoader.PASS3_TIME * (i + 1) / fileCount);

    }
    if (!LoadingASTUtil.optimized) {
      listener.showMessage("Done");
    }
  }

  private final void buildFieldsAndMethodBodys(final CompilationUnit aCompilationUnit)
      throws SourceParsingException {
    final List definedTypes = aCompilationUnit.getDefinedTypes();
    final BodyContext context = new BodyContext(aCompilationUnit);

    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      final BinTypeRef curTypeRef = (BinTypeRef) definedTypes.get(i);

      aCompilationUnit.getProject().getProjectLoader().startProfilingTimer("building fields and method bodys for a defined type");
      context.startType(curTypeRef);
      try {
        if (!curTypeRef.getBinCIType().hasBuildErrors()) {
          this.buildFieldsAndMethodBodys(curTypeRef, context);
        }
      } finally {
        context.endType();
        aCompilationUnit.getProject().getProjectLoader().stopProfilingTimer();
      }
    }
  }

  public final BinStatementList buildMethodBody(final BinMethod method,
      final BodyContext bodyContext) throws SourceParsingException {
    BinStatementList body = null;
    if (method.isBodyEnsured()) {
      body = method.getBodyWithoutEnsure();
    }
    if (body != null) {
      return body;
    }

    final ASTImpl node = method.getOffsetNode();
    if (node != null) { // on copied methods it might be null? or no more?
      final ASTImpl statementListNode
          = LoadingASTUtil.getStatementNode((ASTImpl) node.getFirstChild());
      body = buildMethodStatementList(method, statementListNode, bodyContext);
      method.setBody(body);
    }

    BinParentFinder.findParentsFor(method);

    return body;
  }

  public final void buildInitializerBody(final BinInitializer initializer,
      final BodyContext bodyContext) throws SourceParsingException {
    if (initializer.hasStatementList()) {
      return;
    }

    final ASTImpl node = initializer.getOffsetNode();
    final ASTImpl statementListNode = (ASTImpl) node.getFirstChild();

    // FIXME not sure if it is needed
    bodyContext.beginScope(initializer);

    BinStatementList statementList = null;
    try {
      statementList = buildStatementList(statementListNode, bodyContext);
    } finally {
      bodyContext.endScope();
    }

    initializer.setStatementList(statementList);

    BinParentFinder.findParentsFor(initializer);
  }

  private static final void userFriendlyError(final String description,
      final BodyContext bodyContext,
      final ASTImpl ast) throws SourceParsingException {
    SourceParsingException.throwWithUserFriendlyError(
        description,
        bodyContext.getCompilationUnit(),
        ast
        );
  }

  private static final void methodNotFoundError(final BinTypeRef owner,
      final BinTypeRef returnType, final String methodName,
      final BinExpressionList arguments, final BodyContext bodyContext,
      final ASTImpl ast) throws SourceParsingException {
    final MethodNotFoundError methodNotFoundError
        = new MethodNotFoundError(owner, returnType, methodName,
        arguments, bodyContext, ast);
    SourceParsingException.throwWithUserFriendlyError("Method not found",
        methodNotFoundError);
  }

  private final BinStatementList buildMethodStatementList(final BinMethod method,
      final ASTImpl statementListNode,
      final BodyContext bodyContext) throws SourceParsingException {
    BinStatementList statementList = null;

    if (method.isAbstract() || method.isNative()) {
      if (statementListNode != null) {
        userFriendlyError("Abstract and native methods can't have a body",
            bodyContext, statementListNode);
      }
    } else {
      if (statementListNode == null) {
        userFriendlyError("Method does not have a body: "
            + method.getQualifiedName(),
            bodyContext, statementListNode);
      } else if (statementListNode.getType() != SLIST) {
        userFriendlyError("Method body is invalid: "
            + method.getQualifiedName() + ", body node: " + statementListNode,
            bodyContext, statementListNode);
      }
      bodyContext.beginScope(method);
      try {
        final BinParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) { // add method parameters into local variable list
          bodyContext.addVariable(parameters[i]);
        }
        statementList = buildStatementList(statementListNode, bodyContext);
      } finally {
        bodyContext.endScope();
      }
    }

    return statementList;
  }

  private final BinStatementList buildStatementList(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final List statementList = new ArrayList();
    boolean isNewScopeBegin = false;
    BinStatementList retVal = null;

    try {
      if (node.getType() == SLIST) {

        if ("{".equals(node.getText())) { // only exception when scope doesn't begin is switch/case statement)
          bodyContext.beginScope();
          isNewScopeBegin = true;
        }

        ASTImpl curNode = (ASTImpl) node.getFirstChild();
        while (curNode != null) {
          // NOTE: catching exceptions here refines error granularity to one statement
          try {
            if (curNode.getType() == VARIABLE_DEF) {
              statementList.add(buildVariableDef(
                  LoadingASTUtil.findDefNodesOfOneDeclaration(curNode),
                  bodyContext));
              curNode = LoadingASTUtil.findLastDefNodeOfDeclaration(
                  curNode);
            } else {
              statementList.add(buildStatement(curNode, bodyContext));
            }
          } catch (SourceParsingException e) {
            if (!e.isUserFriendlyErrorReported()) {
              (bodyContext.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(new
                            UserFriendlyError(
                            e.getMessage(),
                            bodyContext.getCompilationUnit(),
                            bodyContext.getBlock().getStartLine(),
                            bodyContext.getBlock().getStartColumn()
                            ));
            }
          }
          curNode = (ASTImpl) curNode.getNextSibling();
        }
      } else {
        statementList.add(buildStatement(node, bodyContext));
      }

      retVal = new BinStatementList((BinStatement[]) statementList.toArray(
          new BinStatement[statementList.size()]), node);
    } finally {
      if (isNewScopeBegin) {
        if (retVal != null) {
          bodyContext.attachScopeReceiver(retVal);
        }
        bodyContext.endScope();
      }
    }

    return retVal;
  }

  private final List buildDimensionExpressionList(final ASTImpl arrayNode,
      final List dimensionExpressionList,
      final BodyContext bodyContext) throws SourceParsingException {
    ASTImpl child = (ASTImpl) arrayNode.getFirstChild();

    if (child != null) {
      if (child.getType() == ARRAY_DECLARATOR) {
        buildDimensionExpressionList(child, dimensionExpressionList,
            bodyContext);
        child = (ASTImpl) child.getNextSibling();
      }
      if (child != null && child.getType() == EXPR) {
        dimensionExpressionList.add(buildExpressionForExpr(child,
            bodyContext));
        return dimensionExpressionList;
      }
    }
    final BinEmptyExpression emptyExpression = new BinEmptyExpression();
    dimensionExpressionList.add(emptyExpression);

    return dimensionExpressionList;
  }

  private final BinExpression[] buildDimensionsExpressionArray(final ASTImpl
      arrayNode, final BodyContext bodyContext) throws SourceParsingException {

    final List dimensionList = buildDimensionExpressionList(arrayNode,
        new ArrayList(), bodyContext);
    return (BinExpression[]) dimensionList.toArray(new BinExpression[
        dimensionList.size()]);
  }

  private final BinExpression[] buildArrayInitExpressionArray(final ASTImpl initNode,
      final BodyContext bodyContext) throws SourceParsingException {

    final List initExpressionList = new ArrayList();
    ASTImpl child = (ASTImpl) initNode.getFirstChild();

    while (child != null) {
      if (child.getType() == ARRAY_INIT) {
        initExpressionList.add(buildArrayInitExpression(child, bodyContext));
      } else if(child.getType() == ANNOTATION) {
        initExpressionList.add(buildExpression(child, bodyContext));
      } else {
        initExpressionList.add(buildExpressionForExpr(child, bodyContext));
      }
      child = (ASTImpl) child.getNextSibling();
    }
    return (BinExpression[]) initExpressionList.toArray(new BinExpression[
        initExpressionList.size()]);
  }

  private final BinArrayInitExpression buildArrayInitExpression(
      final ASTImpl initNode,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinArrayInitExpression expression
        = new BinArrayInitExpression(buildArrayInitExpressionArray(initNode,
        bodyContext), initNode);

    return expression;
  }

  final BinExpressionList buildExpressionList(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    if (Settings.debugLevel > 50) {
      System.out.println("             ---- build expression list ----");
    }

    // JAVA5: shouldn't get here
    if (node.getType() != ELIST) {
      SourceParsingException.throwWithUserFriendlyError(
          "Expected node ELIST, got: " + node, bodyContext.getCompilationUnit(), node);
    }

    final List expressionList = new ArrayList();
    if (Assert.enabled
        && (node == null || node.getType() != ELIST)) {
      Assert.must(false,
          "Bad expression list type was ", node);
    }

    ASTImpl curNode = (ASTImpl) node.getFirstChild();
    while (curNode != null) {
      final BinExpression expression = buildExpressionForExpr(curNode,
          bodyContext);

      if (expression == null) {
        userFriendlyError("Invalid expression found", bodyContext, node);
      }

      expressionList.add(expression);
      curNode = (ASTImpl) curNode.getNextSibling();
    }
    return new BinExpressionList((BinExpression[]) expressionList.toArray(new
        BinExpression[expressionList.size()]), node);
  }

  private final BinExpression buildExpressionForExpr(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    if (Assert.enabled && node.getType() != EXPR) {
      Assert.must(false,
          "Expression expected! found(" + String.valueOf(node.getType()) + ")");
    }

    final ASTImpl curNode = (ASTImpl) node.getFirstChild();
    final BinExpression expression = buildExpression(curNode, bodyContext, true);

    if (Assert.enabled) {
      if (expression == null) {
        (new rantlr.debug.misc.ASTFrame("expr=null", curNode)).show();
        Assert.must(expression != null, "expression must not be null: "
            + bodyContext.getCompilationUnit().getDisplayPath() + " "
            + node.getLine()
            + ":" + node.getColumn());
      }
    }

    return expression;
  }

  private final BinExpression buildExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    return buildExpression(node, bodyContext, true);
  }

  /**
   * FIXME: check the callers
   */
  private final BinExpression buildExpression(final ASTImpl node,
      final BodyContext bodyContext,
      final boolean isTopLevelExpression) throws SourceParsingException {
//System.err.println("buildExpression - node: " + node
//        + ", bodyContext: " + bodyContext
//        + ", isTopLevelExpression: " + isTopLevelExpression);
    BinExpression expression = null;
    if (node == null) {
      SourceParsingException.throwWithUserFriendlyError(
          "Failed to build some expression", bodyContext.getCompilationUnit());
    }
    final int nodeType = node.getType();

    switch (nodeType) {

      case QUESTION:
        expression = buildConditionalExpression(node, bodyContext);
        break;

      case LITERAL_instanceof:
        expression = buildInstanceofExpression(node, bodyContext);
        break;

      case PLUS_ASSIGN:
      case MINUS_ASSIGN:
      case STAR_ASSIGN:
      case DIV_ASSIGN:
      case MOD_ASSIGN:
      case SL_ASSIGN:
      case SR_ASSIGN:
      case BSR_ASSIGN:
      case BAND_ASSIGN:
      case BXOR_ASSIGN:
      case BOR_ASSIGN:
      case ASSIGN:
        expression = buildAssignmentExpression(node, bodyContext);
        break;

      case LOR:
      case LAND:
      case EQUAL:
      case NOT_EQUAL:
      case LT:
      case GT:
      case LE:
      case GE:
        expression = buildLogicalExpression(node, bodyContext);
        break;

      case POST_INC:
      case POST_DEC:
      case INC:
      case DEC:
        expression = buildIncDecExpression(node, bodyContext);
        break;

      case LNOT:
      case BNOT:
      case UNARY_MINUS:
      case UNARY_PLUS:
        expression = buildUnaryExpression(node, bodyContext);
        break;

      case PLUS:
        expression = buildPlusExpression(node, bodyContext);
        break;

      case SL:
      case SR:
      case BSR:
      case MINUS:
      case STAR:
      case DIV:
      case MOD:
      case BOR:
      case BXOR:
      case BAND:
        expression = buildArithmeticalExpression(node, bodyContext);
        break;

      case TYPECAST:
        expression = buildCastExpression(node, bodyContext);
        break;

      case LITERAL_new:
        expression = buildNewExpression(node, null, bodyContext);
        break;

      case NUM_INT:
      case NUM_LONG:
      case NUM_FLOAT:
      case NUM_DOUBLE:
      case CHAR_LITERAL:
      case STRING_LITERAL:
      case LITERAL_this:
      case LITERAL_super:
      case LITERAL_true:
      case LITERAL_false:
      case LITERAL_null:
        expression = buildLiteralExpression(node, bodyContext);
        break;

      case IDENT:
        expression = buildIdentExpression(node, bodyContext,
            isTopLevelExpression);
        break;

      case DOT:
        expression = buildDotExpression(node, bodyContext,
            isTopLevelExpression);
        break;

      case METHOD_CALL:
        expression = buildMethodInvocationExpression(node, bodyContext);
        break;

      case SUPER_CTOR_CALL:
        expression = buildSuperConstructorInvocation(node, bodyContext);
        break;

      case INDEX_OP:
        expression = buildArrayUseExpression(node, bodyContext);
        break;

      case LITERAL_void:
      case LITERAL_boolean:
      case LITERAL_byte:
      case LITERAL_char:
      case LITERAL_short:
      case LITERAL_int:
      case LITERAL_long:
      case LITERAL_float:
      case LITERAL_double:

        // this is for boolean.class
        break;

      case ARRAY_DECLARATOR:
        // JAVA5: ?
        // this is for Object[].class
        break;

      case LPAREN:
        expression = buildExpression(
            (ASTImpl) node.getFirstChild(), bodyContext, isTopLevelExpression);
        break;

      case ANNOTATION_ARRAY_INIT:
        expression = buildArrayInitExpression(node, bodyContext);
        break;

      case ANNOTATION:
        expression = bodyContext.getProject().getProjectLoader()
          .getSourceLoader().buildAnnotationExpression(node, bodyContext);
      break;

      default:
        if (Assert.enabled) {
          Assert.must(false, "Unknown expression type: " + nodeType);
        }

    }

    /*if(expression == null && fromDOT==0) {
      System.err.println(fromDOT + " with null");
      ASTDebugOn(node);
      new Exception().printStackTrace(System.out);
         }*/

    /*if (expression == null) {
      System.out.println("Expression == null");
         } else {
      System.out.println("Expression: " + expression);
         }*/

    return expression;
  }

  private final BinExpression buildArrayUseExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    BinExpression expression = null;
    if (Settings.debugLevel > 50) {
      System.out.println("             index expression");
    }
    final ASTImpl arrayExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl dimensionExpressionNode
        = (ASTImpl) arrayExpressionNode.getNextSibling();

    final BinExpression arrayExpression = buildExpression(
        arrayExpressionNode, bodyContext);

    if (Assert.enabled && dimensionExpressionNode == null) {
      Assert.must(false, "index espression missing.");
    }
    final BinExpression dimensionExpression = buildExpressionForExpr(
        dimensionExpressionNode, bodyContext);

    try {
      expression = new BinArrayUseExpression(arrayExpression,
          dimensionExpression,
          node);
    } catch (Exception e) {
      new rantlr.debug.misc.ASTFrame("strange array", node).setVisible(true);
    }

    return expression;
  }

  private static final BinExpression buildLiteralExpression(final ASTImpl node,
      final BodyContext bodyContext) {
    BinExpression expression = null;
    BinTypeRef numberRef = null;
    final String literal = node.getText();
    if (Assert.enabled && literal == null) {
      Assert.must(false, "No literal !?", node);
    }

    switch (node.getType()) {
      case NUM_INT:
      case NUM_LONG:
        if (Settings.debugLevel > 50) {
          System.out.println("             numeric expression");
        }

        // Resolve type
        switch (Character.toUpperCase(literal.charAt(literal.length() - 1))) {
          case 'L':
            numberRef = BinPrimitiveType.LONG_REF;
            break;
          default:
            numberRef = BinPrimitiveType.INT_REF;
            break;
        }

        expression = new BinLiteralExpression(literal, numberRef, node);
        break;

      case CHAR_LITERAL:
        if (Settings.debugLevel > 50) {
          System.out.println("             char literal expression");
        }
        expression = new BinLiteralExpression(literal,
            BinPrimitiveType.CHAR_REF, node);
        break;

      case STRING_LITERAL:
        if (Settings.debugLevel > 50) {
          System.out.println("             string literal expression: "
              + literal);
        }
        final BinTypeRef typeRef = bodyContext.getProject()
            .getTypeRefForName("java.lang.String");

//        if (typeRef == null) {
//          System.out.println("             No type for string.. hmm weird...");
//        }

        expression = new BinLiteralExpression(literal, typeRef, node);
        break;

      case NUM_FLOAT:
        if (Settings.debugLevel > 50) {
          System.out.println("             float expression");
        }

//        switch (Character.toUpperCase(literal.charAt(literal.length() - 1))) {
//          case 'F':
            numberRef = BinPrimitiveType.FLOAT_REF;
//            break;
//          default:
//            numberRef = BinPrimitiveType.DOUBLE_REF;
//            break;
//        }

        expression = new BinLiteralExpression(literal, numberRef, node);
        break;

      case NUM_DOUBLE:
        if (Settings.debugLevel > 50) {
          System.out.println("             double expression");
        }

//        switch (Character.toUpperCase(literal.charAt(literal.length() - 1))) {
//          case 'D':
            numberRef = BinPrimitiveType.DOUBLE_REF;
//            break;
//          default:
//            numberRef = BinPrimitiveType.FLOAT_REF; // :)
//            break;
//        }

        expression = new BinLiteralExpression(literal, numberRef, node);
        break;

      case LITERAL_this:
        if (Settings.debugLevel > 50) {
          System.out.println("             this expression");
        }

        expression = new BinLiteralExpression(BinLiteralExpression.THIS,
            bodyContext.getTypeRef(), node);
        break;

      case LITERAL_super:
        if (Settings.debugLevel > 50) {
          System.out.println("             super expression");
        }

        expression = new BinLiteralExpression(BinLiteralExpression.SUPER,
            bodyContext.getTypeRef().getSuperclass(), node);
        break;

      case LITERAL_true:
        if (Settings.debugLevel > 50) {
          System.out.println("             true expression");
        }

        expression = new BinLiteralExpression(BinLiteralExpression.TRUE,
            BinPrimitiveType.BOOLEAN_REF, node);
        break;

      case LITERAL_false:
        if (Settings.debugLevel > 50) {
          System.out.println("             false expression");
        }

        expression = new BinLiteralExpression(BinLiteralExpression.FALSE,
            BinPrimitiveType.BOOLEAN_REF, node);
        break;

      case LITERAL_null:
        if (Settings.debugLevel > 50) {
          System.out.println("             null expression");
        }

        expression = new BinLiteralExpression(BinLiteralExpression.NULL,
            null, node);
        break;

      default:
        break;
    }

    return expression;
  }

  private final BinExpression buildIdentExpression(final ASTImpl node,
      final BodyContext bodyContext,
      final boolean isTopLevelExpression) throws SourceParsingException {

    BinExpression expression = null;
    final String identifierName = node.getText();
    if (Settings.debugLevel > 50) {
      System.out.println("             ident expression: " + identifierName);
    }

    BinTypeRef curTypeRef = bodyContext.getTypeRef();
    while (curTypeRef != null && expression == null) {
      BinVariable variable
          = bodyContext.getLocalVariableForName(identifierName, curTypeRef);
      if (variable != null) {
        expression = new BinVariableUseExpression(
            (BinLocalVariable) variable, node);
      } else {
        /* Gets compile-time type declaration the field is invoked on from the
           specified context. See JLS 6.5.6.1 for more details.
           First checked own fields, then owners.
           This algorithm applies to the following type of field invocation:
           FieldInvocation:
             FieldName
         */
        final BinTypeRef switchEnumType = bodyContext.getSwitchType();
        if (switchEnumType != null) {
          variable = switchEnumType.getBinCIType()
              .getAccessibleField(identifierName, curTypeRef.getBinCIType());
          if (variable != null) {
            curTypeRef = switchEnumType;
          }
        }

        if (variable == null) {
          variable = curTypeRef.getBinCIType().getAccessibleField(
              identifierName, curTypeRef.getBinCIType());
        }

        if (variable != null) {
          // self fields is also field invocation expression not variable use.
          expression = new BinFieldInvocationExpression(
              (BinField) variable, null, curTypeRef, node);

          ((BinFieldInvocationExpression) expression).setNameAst(node);

          // DEPENDENCIE: no dependencie needed for dotless field invocations
          // because these dependecies will be covered anyway by subtypes
        } else {
          curTypeRef = curTypeRef.getBinCIType().getOwner();
        }
      }
    }

    //FIXME: ok, this is what Sander thinks
    //this is allowed to return null then and only then if it
    //is called from DOT clause from the same method, right?
    if (expression == null) {
      if (isTopLevelExpression || fromDOT == 0) {
        //we are top level expression and our left side was unknown (null)
        userFriendlyError("Unknown field or variable: "
            + identifierName, bodyContext, node);
      }
    }

    if (Settings.debugLevel > 50) {
      if (expression == null) {
        System.out.println(" Ident " + identifierName + " not found..");
      } else {
        System.out.println(" Ident " + identifierName + " ok.");
      }
    }

    return expression;
  }

  private final BinExpression buildNewExpression(final ASTImpl node,
      final BinTypeRef superRef,
      final BodyContext bodyContext) throws SourceParsingException {
    BinNewExpression expression = null;
    BinTypeRef[] typeArguments = null;
    if (Settings.debugLevel > 50) {
      System.out.println("             new expression");
    }

    ASTImpl typeNode = (ASTImpl) node.getFirstChild();

    if (typeNode.getType() == TYPE_ARGUMENTS) {
      // Case: new <String>X()
      typeArguments = CompilationUnitsLoader
          .buildTypeArguments(typeNode, null, bodyContext);

      typeNode = (ASTImpl) typeNode.getNextSibling();
    }

    final ASTImpl extensionNode = (ASTImpl) typeNode.getNextSibling();

    final BinTypeRef typeRef = CompilationUnitsLoader.buildSpecificTypeRef(
        typeNode, bodyContext, superRef);

    if (extensionNode.getType() == ARRAY_DECLARATOR) {
      final BinExpression[] dimensionExpressions
          = buildDimensionsExpressionArray(extensionNode, bodyContext);
      final ASTImpl arrayInitNode = (ASTImpl) extensionNode.getNextSibling();
      BinArrayInitExpression arrayInitExpression = null;
      if (arrayInitNode != null) {
        arrayInitExpression
            = buildArrayInitExpression(arrayInitNode, bodyContext);
      }

      expression = new BinNewExpression(
          typeRef, null, dimensionExpressions, arrayInitExpression, null, node);

      if (superRef == null) {
        // Dependencies - new array - no dependencies needed
      } else {
        // Add incremental rebuild dependencie
        ((DependencyParticipant) typeRef).addDependable(bodyContext.getTypeRef());
      }
    } else {
      final BinExpressionList expressionList = buildExpressionList(
          extensionNode, bodyContext);
      final ASTImpl objectBlockNode = (ASTImpl) extensionNode.getNextSibling();

      BinCITypesDefStatement def = null;
      final DependencyParticipant originalTypeRef = (DependencyParticipant) typeRef;
      if (objectBlockNode != null) {
        bodyContext.setExpressionList(expressionList);
        final BinTypeRef anonTypeRef = buildAnonymousType(
            node, typeRef.getTypeRef(), null, bodyContext);
        def = new BinCITypesDefStatement(anonTypeRef, node);
        ((BinSpecificTypeRef) typeRef).setTypeRef(anonTypeRef);
      }
      expression = new BinNewExpression(
          typeRef, expressionList, null, null, def, node);

      // add dependency for constructor type / anon types owner
      // FIXME: must check someday if there is a difference here!
      if (superRef == null) {
        originalTypeRef.addDependable(bodyContext.getTypeRef());
      } else {
        ((DependencyParticipant) typeRef).addDependable(bodyContext.getTypeRef());
      }

      if (typeRef.isReferenceType() && typeRef.getBinCIType().isInterface()) {
        // Fix for bug #1770
        userFriendlyError(
            "Interfaces can't be instantiated with the 'new' keyword",
            bodyContext, node);
      }
    }

    if (typeArguments != null) {
      expression.setTypeArguments(typeArguments);
      BinConstructor constructor = expression.getConstructor(); // it will resolve constructor right away :(
      if (constructor != null) {
        for (int i = 0, max = typeArguments.length; i < max; i++) {
          ((BinSpecificTypeRef) typeArguments[i])
              .setTypeParameterResolver(constructor, i);
        }
      }
    }

    return expression;
  }

  private final BinExpression buildCastExpression(
      final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println("             cast expression");
    }
    final ASTImpl typeNode = (ASTImpl) node.getFirstChild();
    final ASTImpl expressionNode = (ASTImpl) typeNode.getNextSibling();

    final BinExpression rightExpression
        = buildExpression(expressionNode, bodyContext);

    final BinTypeRef typeUsage = CompilationUnitsLoader.buildSpecificTypeRef((ASTImpl) typeNode.getFirstChild(), bodyContext, null);

    return new BinCastExpression(rightExpression, typeUsage, node);
  }

  private final BinExpression buildArithmeticalExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    if (Settings.debugLevel > 50) {
      System.out.println("             arithmetical expression");
    }
    final ASTImpl leftExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl rightExpressionNode = (ASTImpl) leftExpressionNode.
        getNextSibling();

    final BinExpression leftExpression = buildExpression(leftExpressionNode,
        bodyContext);
    final BinExpression rightExpression = buildExpression(
        rightExpressionNode, bodyContext);

    return new BinArithmeticalExpression(leftExpression, rightExpression, node);
  }

  private final BinExpression buildPlusExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinExpression expression;
    final ASTImpl leftExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl rightExpressionNode
        = (ASTImpl) leftExpressionNode.getNextSibling();

    final BinExpression leftExpression =
        buildExpression(leftExpressionNode, bodyContext);
    final BinExpression rightExpression =
        buildExpression(rightExpressionNode, bodyContext);
    if (((leftExpression.getReturnType() != null)
        && (leftExpression.getReturnType().isString()))
        || ((rightExpression.getReturnType() != null)
        && (rightExpression.getReturnType().isString()))) {

      if (Settings.debugLevel > 50) {
        System.out.println(
            "             String concatenation expression");
      }
      expression = new BinStringConcatenationExpression(leftExpression,
          rightExpression, node);

    } else {
      if (Settings.debugLevel > 50) {
        System.out.println(
            "             arithmetical expression");
      }
      expression = new BinArithmeticalExpression(leftExpression,
          rightExpression, node);
    }
    return expression;
  }

  private final BinExpression buildUnaryExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinExpression expression;
    if (Settings.debugLevel > 50) {
      System.out.println("             unary expression");
    }
    final ASTImpl expressionNode = (ASTImpl) node.getFirstChild();
    final BinExpression rightExpression = buildExpression(expressionNode,
        bodyContext);
    expression = new BinUnaryExpression(rightExpression, node);
    return expression;
  }

  private final BinExpression buildIncDecExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinExpression expression;
    if (Settings.debugLevel > 50) {
      System.out.println("             post/pre-fix expression");
    }
    final ASTImpl expressionNode = (ASTImpl) node.getFirstChild();
    final BinExpression postPreExpression = buildExpression(expressionNode,
        bodyContext);
    expression = new BinIncDecExpression(postPreExpression, node.getType(),
        node);
    return expression;
  }

  private final BinExpression buildLogicalExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println("             logical expression");
    }
    final ASTImpl leftExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl rightExpressionNode = (ASTImpl) leftExpressionNode.
        getNextSibling();

    final BinExpression leftExpression
        = buildExpression(leftExpressionNode, bodyContext);
    final BinExpression rightExpression
        = buildExpression(rightExpressionNode, bodyContext);

    return new BinLogicalExpression(leftExpression, rightExpression, node);
  }

  private final BinExpression buildAssignmentExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println("             assign expression");
    }
    final ASTImpl leftExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl rightExpressionNode = (ASTImpl) leftExpressionNode.
        getNextSibling();

    final BinExpression leftExpression
        = buildExpression(leftExpressionNode, bodyContext);
    final BinExpression rightExpression
        = buildExpression(rightExpressionNode, bodyContext);

    // FIXME: what if leftExpression is null
    // this happens in a bug when trying to use nonexistant field or something
    return new BinAssignmentExpression(leftExpression, rightExpression, node);
  }

  private final BinExpression buildInstanceofExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    if (Settings.debugLevel > 50) {
      System.out.println("             instanceof expression");
    }

    final ASTImpl leftExpressionNode = (ASTImpl) node.getFirstChild();

    final BinExpression leftExpression
        = buildExpression(leftExpressionNode, bodyContext);

    // FIXME not nice, should be more generic
    // FIXME after all, shouldn't work correctly for Outer.Inner[], just looses those []
    final ASTImpl rightExpressionNode
        = (ASTImpl) leftExpressionNode.getNextSibling().getFirstChild();
    BinExpression rightExpression = null;
    int dimension = 0;
    ASTImpl notArrayNode = rightExpressionNode;
    while (notArrayNode.getType() == ARRAY_DECLARATOR) {
      notArrayNode = (ASTImpl) notArrayNode.getFirstChild();
      dimension++;
    }

    if (notArrayNode.getType() == DOT) {
      rightExpression = buildDotExpression(notArrayNode, bodyContext, false);
      /*      if (Assert.enabled) {
              if (rightExpression == null) {
                (new rantlr.debug.misc.ASTFrame(
                    "Expression == null", notArrayNode)).show();
              }
              Assert.must(rightExpression != null, "Expression == null",
                  notArrayNode);
            }*/
    }

    if (rightExpression == null) {
      final BinTypeRef returnType
          = CompilationUnitsLoader.buildSpecificTypeRef(
              rightExpressionNode, bodyContext, null);
      rightExpression = new BinCITypeExpression(returnType, null, notArrayNode);
    }

    return new BinInstanceofExpression(leftExpression, rightExpression, node);
  }

  private final BinExpression buildConditionalExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinExpression expression;
    if (Settings.debugLevel > 50) {
      System.out.println("             Question expression");
    }
    final ASTImpl conditionExpressionNode = (ASTImpl) node.getFirstChild();
    final ASTImpl trueExpressionNode
        = (ASTImpl) conditionExpressionNode.getNextSibling();
    final ASTImpl falseExpressionNode
        = (ASTImpl) trueExpressionNode.getNextSibling();

    final BinExpression condition
        = buildExpression(conditionExpressionNode, bodyContext);

    final BinExpression trueExpression
        = buildExpression(trueExpressionNode, bodyContext);
    final BinExpression falseExpression
        = buildExpression(falseExpressionNode, bodyContext);

    expression = new BinConditionalExpression(condition, trueExpression,
        falseExpression, node);

    return expression;
  }

  private final BinExpression buildDotExpression(final ASTImpl node,
      final BodyContext bodyContext,
      final boolean isTopLevelExpression) throws SourceParsingException {
//System.err.println("buildDotExpression - node: " + node
//    + ", bodyContext: " + bodyContext
//    + ", isTopLevelExpression: " + isTopLevelExpression);

    BinExpression expression = null;

    fromDOT++;
    if (Settings.debugLevel > 50) {
      System.out.println("             dot expression");
    }
    ASTImpl leftNode = (ASTImpl) node.getFirstChild();
    final ASTImpl rightNode = (ASTImpl) leftNode.getNextSibling();

    // build not toplevel expression
    BinExpression leftExpression
        = buildExpression(leftNode, bodyContext, false);

    if (Settings.debugLevel > 50) {
      System.out.println("              left expression = " + leftExpression);
    }

    if (leftExpression == null || rightNode.getType() == LITERAL_class) {
      // probably in leftside is static or qualified name

      //SourceUtil.test( leftNode.getType() == this.ARRAY_DECLARATOR
      // || leftNode.getType() == DOT || leftNode.getType() == IDENT,
      // "Bad field or variable use!" );

      int dimension = 0;
      while (leftNode.getType() == ARRAY_DECLARATOR) {
        leftNode = (ASTImpl) leftNode.getFirstChild();
        dimension++;
      }
      BinTypeRef typeRef = null;
      String identifierName = null;
      final int leftType = leftNode.getType();

      // for boolean.class
      switch (leftType) {
        case LITERAL_void:
          typeRef = BinPrimitiveType.VOID_REF;
          break;
        case LITERAL_boolean:
          typeRef = BinPrimitiveType.BOOLEAN_REF;
          break;
        case LITERAL_byte:
          typeRef = BinPrimitiveType.BYTE_REF;
          break;
        case LITERAL_char:
          typeRef = BinPrimitiveType.CHAR_REF;
          break;
        case LITERAL_short:
          typeRef = BinPrimitiveType.SHORT_REF;
          break;
        case LITERAL_int:
          typeRef = BinPrimitiveType.INT_REF;
          break;
        case LITERAL_long:
          typeRef = BinPrimitiveType.LONG_REF;
          break;
        case LITERAL_float:
          typeRef = BinPrimitiveType.FLOAT_REF;
          break;
        case LITERAL_double:
          typeRef = BinPrimitiveType.DOUBLE_REF;
          break;
        default:
          identifierName = LoadingASTUtil.combineIdentsAndDots(leftNode);

          if (identifierName == null) {
            userFriendlyError("Bad field or variable use", bodyContext,
                leftNode);
          }

          try {
            // get left type reference
            typeRef = bodyContext.getTypeRefForName(identifierName);

            if (typeRef != null) {
              if (dimension > 0) {
                typeRef = bodyContext.getProject().createArrayTypeForType(
                    typeRef, dimension);
                if (Assert.enabled && typeRef == null) {
                  Assert.must(false, "Array type can't be null!");
                }
              }
              final BinTypeRef returnType = BinSpecificTypeRef.create(
                  bodyContext.getCompilationUnit(), leftNode, typeRef, true);
              leftExpression = new BinCITypeExpression(returnType, null, leftNode);
            }
          } catch (LocationlessSourceParsingException e) {
            SourceParsingException.rethrowWithUserFriendlyError(e, node);
          }
        }

        if (typeRef == null) {
          if (isTopLevelExpression || fromDOT == 0) {
            //we are top level expression and our left side was unknown (null)
            if (rightNode.getType() == LITERAL_class
                || rightNode.getType() == LITERAL_this
                || rightNode.getType() == LITERAL_super) {
              userFriendlyError("Unknown type: "
                  + identifierName, bodyContext, node);
            } else {
              userFriendlyError("Unknown field, variable or inner type: "
                  + identifierName, bodyContext, node);
            }
          } else {
            // we are'nt top level expression and our left side was unknown, probably it's part of package name
            // our master must know what to do.
          }
        } else {

          // if it was an array
          if (typeRef != null && dimension != 0) {
            typeRef = bodyContext.getProject()
                .createArrayTypeForType(typeRef, dimension);
          }

          if (!(typeRef.isReferenceType() || typeRef.isPrimitiveType()
              && rightNode.getType() == LITERAL_class)) {
            userFriendlyError("Primitive type must not have fields",
                bodyContext, leftNode);
          }

          switch (rightNode.getType()) {

            case LITERAL_this:
              expression = new BinLiteralExpression(BinLiteralExpression.THIS,
                  leftExpression, typeRef, node);
              ((BinLiteralExpression) expression).setNameAst(rightNode);
              break;

            case LITERAL_super:
              expression = new BinLiteralExpression(BinLiteralExpression.SUPER,
                  leftExpression, typeRef.getSuperclass(), node);
              ((BinLiteralExpression) expression).setNameAst(rightNode);
              break;

            case LITERAL_class:
              BinTypeRef classRef
                  = bodyContext.getProject().getTypeRefForName("java.lang.Class");
              if (leftExpression != null) { // when?
                BinTypeRef arg = leftExpression.getReturnType();
                if (arg != null) {
                  classRef = new BinGenericTypeRef(classRef);
                  if (!arg.isSpecific()) {
                    arg = BinSpecificTypeRef.create(arg);
                  }
                  classRef.setTypeArguments(new BinTypeRef[] {arg});
                  ((BinSpecificTypeRef) arg).setTypeParameterResolver(classRef, 0);
                }
              }

              expression = new BinLiteralExpression(
                  BinLiteralExpression.CLASS, leftExpression, classRef, node);
              ((BinLiteralExpression) expression).setNameAst(rightNode);
              break;

            case IDENT: //static field or inner
              final String fieldName = rightNode.getText();
              final BinField field = typeRef.getBinCIType()
                  .getAccessibleField(fieldName,
                  bodyContext.getTypeRef().getBinCIType());
              if (field == null) {
                if (isTopLevelExpression) {
                  userFriendlyError("Field not found, type: "
                      + typeRef.getQualifiedName() + " field: " + fieldName,
                      bodyContext, node);
                } else {
                  final BinTypeRef inner = typeRef.getBinCIType()
                      .getDeclaredType(fieldName); // FIXME or getAccessible?
                  if (inner != null) {
                    final BinTypeRef returnType = BinSpecificTypeRef.create(
                        bodyContext.getCompilationUnit(), rightNode, inner, true);
                    expression = new BinCITypeExpression(
                        returnType, leftExpression, rightNode);
                  }
                }
              } else {
                expression = new BinFieldInvocationExpression(field,
                    leftExpression, typeRef, node);

                ((BinFieldInvocationExpression) expression).setNameAst(
                    rightNode);

                // add dependency for incremental rebuild
                ((DependencyParticipant) typeRef).addDependable(bodyContext.getTypeRef());
              }
              break;
            default:
              userFriendlyError("Invalid right-side of dot expression: "
                  + rightNode.toString(),
                  bodyContext, rightNode);
          }
        }
    } else {
      BinTypeRef typeRef = leftExpression.getReturnType();
      if(leftExpression instanceof BinConditionalExpression) {
        if (rightNode.getType() == IDENT) {
          typeRef = ((BinConditionalExpression) leftExpression).
              ensureReturnType(bodyContext.getTypeRef().getBinCIType(),
              rightNode.getText());
        }
      }
      switch (rightNode.getType()) {

        case IDENT:
          final String identifierName = rightNode.getText();
          if (Assert.enabled && identifierName == null) {
            Assert.must(false, "Identifier doesn't have a name!?");
          }

          if (Assert.enabled && typeRef == null) {
            Assert.must(false,
                "Bad expression before field: " + identifierName + ", expr: "
                + leftExpression);
          }
          if (typeRef.isPrimitiveType()) {
            userFriendlyError("Primitive type doesn't have fields",
                bodyContext, node);
          }
          final BinField field = typeRef.getBinCIType()
              .getAccessibleField(identifierName,
              bodyContext.getTypeRef().getBinCIType());

          if (field != null) {
            expression = new BinFieldInvocationExpression(field,
                leftExpression, typeRef, node);

            ((BinFieldInvocationExpression) expression).setNameAst(rightNode);

            // add dependency for incremental rebuild
            ((DependencyParticipant) typeRef).addDependable(bodyContext.getTypeRef());

          } else {
            final BinTypeRef inner = typeRef.getBinCIType()
                .getDeclaredType(identifierName); // FIXME or getAccessible?
            if (inner != null) {
              final BinTypeRef returnType = BinSpecificTypeRef.create(
                  bodyContext.getCompilationUnit(), rightNode, inner, true);
              expression = new BinCITypeExpression(
                  returnType, leftExpression, rightNode);
            }
          }

          if (expression == null) {
            userFriendlyError("Field or inner not found - context: "
                + bodyContext.getTypeRef().getBinCIType().getQualifiedName()
                + "; type: " + typeRef.getQualifiedName()
                + "; name: " + identifierName, bodyContext, node);
          }
          break;
        case LITERAL_new:
          expression = buildNewExpression(rightNode,
              leftExpression.getReturnType(), bodyContext);
          break;

        case LITERAL_this:
          if (typeRef != null) {
            expression = new BinLiteralExpression(BinLiteralExpression.THIS,
                leftExpression, typeRef, node);
            ((BinLiteralExpression) expression).setNameAst(rightNode);
          } else {
            AppRegistry.getLogger(this.getClass()).debug(" typeRef == null for expression"
            + leftExpression);
          }
          break;

        case LITERAL_super:
          // JAVA5: copy-pasted, structure must be checked and corrected
          expression = leftExpression; // this way it looks the same as with old 1.3 grammar
//          if (typeRef != null) {
//            expression = new BinLiteralExpression(
//                BinLiteralExpression.SUPER,
//                leftExpression, ((BinTypeRef) typeRef).getSuperclass(), node);
//            ((BinLiteralExpression) expression).setNameAst(rightNode);
//          } else {
//            DebugInfo.trace(" typeRef == null for expression"
//                + leftExpression);
//          }
          break;

        default:
          userFriendlyError("Invalid right-side of dot expression: "
              + rightNode.toString(),
              bodyContext, rightNode);
      }
    }

    fromDOT--;

    return expression;
  }

  private final BinExpression buildMethodInvocationExpression(
      final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    BinExpression expression = null;

    if (Settings.debugLevel > 50) {
      System.out.println("             method call expression");
    }
    ASTImpl methodNode = (ASTImpl) node.getFirstChild();

    BinTypeRef[] typeArguments = null;

    ASTImpl expressionListNode = (ASTImpl) methodNode.getNextSibling();

    if (expressionListNode.getType() != ELIST) {
      // Static method type parameter: X.<String>method("str");
      typeArguments = CompilationUnitsLoader
          .buildTypeArguments(expressionListNode, null, bodyContext);

//      bodyContext.getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(
//          new UserFriendlyError(
//          "Parameterized method invocations are not yet supported: " + node,
//          bodyContext.getCompilationUnit(), expressionListNode));

      expressionListNode = (ASTImpl) expressionListNode.getNextSibling();
    }

    final BinExpressionList expressionList = buildExpressionList(
        expressionListNode, bodyContext);

    switch (methodNode.getType()) {
      case DOT:
        fromDOT++;
        {
          if (Settings.debugLevel > 50) {
            System.out.println("      found DOT method call");
          }
          final ASTImpl leftChildNode = (ASTImpl) methodNode.getFirstChild();
          ASTImpl methodNameNode = (ASTImpl) leftChildNode.getNextSibling();

          if (methodNameNode.getType() == TYPE_ARGUMENTS) {
            // Type argument case: new X().<String>method("Str");
            if (Assert.enabled && typeArguments != null) {
              Assert.must(false, "Type arguments already set: ", methodNameNode);
            }
//            bodyContext.getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(
//                new UserFriendlyError(
//                "Parameterized method invocations are not yet supported: " + node,
//                bodyContext.getCompilationUnit(), expressionListNode));
            typeArguments = CompilationUnitsLoader
                .buildTypeArguments(methodNameNode, null, bodyContext);

            methodNameNode = (ASTImpl) methodNameNode.getNextSibling();
          }

          BinExpression leftChildExpression
              = buildExpression(leftChildNode, bodyContext, false);

          if (Settings.debugLevel > 50) {
            if (leftChildExpression != null) {
              System.out.println(
                  "        found valid expression leftside DOT");
            } else {
              System.out.println(
                  "        no expression leftside DOT, probably some static method");
            }
          }

          BinTypeRef leftTypeRef = null;
          if (leftChildExpression != null) {
            // there is somekind expression in left side
            leftTypeRef = leftChildExpression.getReturnType();

            if (leftChildExpression instanceof BinConditionalExpression) {
              leftTypeRef = ((BinConditionalExpression) leftChildExpression).
                  ensureReturnType(bodyContext.getTypeRef().getBinCIType(),
                  methodNameNode.getText(), expressionList.getExpressionTypes());
            }

          } else if (leftChildNode.getType() == DOT
              || leftChildNode.getType() == IDENT) {

            // probably this is a static method
            final String identifierName
                = LoadingASTUtil.combineIdentsAndDots(leftChildNode);
            if (identifierName != null) {
              try {
                leftTypeRef = bodyContext.getTypeRefForName(identifierName); // lets find that class

                if (leftTypeRef != null) {
                  final BinTypeRef returnType = BinSpecificTypeRef.create(
                      bodyContext.getCompilationUnit(), leftChildNode,
                      leftTypeRef, true);
                  leftChildExpression = new BinCITypeExpression(
                      returnType, null, leftChildNode);
                }
              } catch (LocationlessSourceParsingException e) {
                SourceParsingException.rethrowWithUserFriendlyError(e, node);
              }
            }
          }
          if (leftTypeRef == null) {
            userFriendlyError(
                "Type, member or variable not found to call method on: "
                + leftChildNode.getText(), bodyContext, node);
          }
          if (leftTypeRef.isPrimitiveType()) {
            userFriendlyError("Primitive type doesn't have methods",
                bodyContext, node);
          }
          final String methodName = methodNameNode.getText();
          if (Settings.debugLevel > 50) {
            System.out.println("get method for name - type: "
                + leftTypeRef.getQualifiedName() + " method: " + methodName);
          }

          if (Assert.enabled && leftTypeRef.getBinCIType() == null) {
            Assert.must(false,
                "Type is null for ref: " + leftTypeRef.getQualifiedName());
          }
          BinTypeRef resolverRef = leftTypeRef; // Mark up the resolver - We can use it to resolve methods again in the future

          boolean localNotAnonymous = leftTypeRef.getBinCIType().isLocal()
              && !leftTypeRef.getBinCIType().isAnonymous();
          final BinTypeRef context
              = localNotAnonymous ? leftTypeRef : bodyContext.getTypeRef();

            BinMethod method = MethodInvocationRules.getMethodDeclaration(context
            .getBinCIType(), leftTypeRef, methodName, expressionList
            .getExpressionTypes());

          if (method == null && leftTypeRef.getBinCIType().isInterface()) {
            // all types extends lava.lang.Objects even those what are defined via interface.
            final Project pro = bodyContext.getProject();
            final BinTypeRef objectRef = pro.getObjectRef();

            method = MethodInvocationRules.getMethodDeclaration(
                context.getBinCIType(),
                objectRef, methodName, expressionList.getExpressionTypes());

            if (method != null) {
              // Update resolver reference to "java.lang.Object"
              resolverRef = objectRef;
            }
          }

          // this is done, since method invocations on Annotation type
          // is actually field invocation
          BinField field = null;
          if (method == null && leftTypeRef.getBinCIType().isAnnotation()) {
            field = leftTypeRef.getBinCIType().getAccessibleField(methodName,
                leftTypeRef.getBinCIType());
          }

          if (method != null) {
            expression = new BinMethodInvocationExpression(method,
                leftChildExpression, expressionList, leftTypeRef, // or it should
                // be resolver?
                node);

            // add dependency for incremental rebuild
            ((DependencyParticipant) method.getOwner()).addDependable(
                bodyContext
                .getTypeRef());

            // Set resolver type
            //          ((BinMethodInvocationExpression) expression)
            //              .setResolverType(resolverRef);

            // FIXME doesn't get here ever?
            //            if (leftChildExpression == null) {
            //              ((BinMethodInvocationExpression) expression).addTypeUsageInfos(
            //                  CollectionUtil.singletonArrayList(
            //                  LoaderTypeNodeHelper.createTypeUsageInfo(
            //                  bodyContext.getCompilationUnit(), leftChildNode, typeRef)));
            //            }

            ((BinMethodInvocationExpression) expression)
                .setNameAst(methodNameNode);

            if (typeArguments != null) {
              ((BinMethodInvocationExpression) expression)
                  .setTypeArguments(typeArguments);
              for (int i = 0, max = typeArguments.length; i < max; i++) {
                ((BinSpecificTypeRef) typeArguments[i]).
                    setTypeParameterResolver(
                    method, i);
              }
            }
          } else if (field != null) {
            expression = new BinFieldInvocationExpression(field,
                leftChildExpression, leftTypeRef, node);

            ((BinFieldInvocationExpression) expression)
                .setNameAst(methodNameNode);

            // add dependency for incremental rebuild
            ((DependencyParticipant) field.getOwner()).addDependable(
                bodyContext
                .getTypeRef());
          } else {
            methodNotFoundError(resolverRef, guessReturnType(node, bodyContext),
                methodName, expressionList, bodyContext, node);
          }
        }

        fromDOT--;
        break;

      case IDENT: {
        final String identifierName = methodNode.getText();
        // Find innermost enclosing type containing method with the name
        final BinCIType type =
            MethodInvocationRules.getTypeForDotlessInvocation(
            bodyContext.getTypeRef().getBinCIType(),
            identifierName);
        if (type == null) {
          methodNotFoundError(bodyContext.getTypeRef(),
              guessReturnType(node, bodyContext),
              identifierName,
              expressionList,
              bodyContext,
              node);
        }

        final BinMethod method =
            MethodInvocationRules.getMethodDeclaration(
            bodyContext.getTypeRef().getBinCIType(),
            type.getTypeRef(),
            identifierName,
            expressionList.getExpressionTypes());

        if (method == null) {
          methodNotFoundError(type.getTypeRef(),
              guessReturnType(node, bodyContext),
              identifierName,
              expressionList,
              bodyContext,
              node);
        }
        expression = new BinMethodInvocationExpression(method,
            null, expressionList,
            type.getTypeRef() /*bodyContext.getTypeRef()*/,
            node);

        // add dependency for incremental rebuild
        ((DependencyParticipant) method.getOwner()).addDependable(
            bodyContext.getTypeRef());

        // Set resolver type
//        ((BinMethodInvocationExpression) expression)
//            .setResolverType(bodyContext.getTypeRef());

        // Attach BinSpecificTypeRef
        // Not implemented - we do not care about not-fully-qualified static
        // methods

        ((BinMethodInvocationExpression) expression).setNameAst(methodNode);

        ((BinMethodInvocationExpression) expression)
            .setTypeArguments(typeArguments);
      }
      break;

  //      case LITERAL_super:
  //        // FIXME: does this ever get's called with new java.g?
  //        if(1 == 1) throw new RuntimeException("This got called");
  //        expression = new BinConstructorInvocationExpression(
  //            bodyContext.getTypeRef(),
  //            bodyContext.getTypeRef().getSuperclass(), expressionList, true,
  //            node);
  //        ((BinConstructorInvocationExpression) expression).setNameAst(methodNode);
  //
  //        break;
  //
  //      case LITERAL_this:
  //        // FIXME: does this ever get's called with new java.g?
  //        if(1 == 1) throw new RuntimeException("This got called");
  //        expression = new BinConstructorInvocationExpression(
  //            bodyContext.getTypeRef(),
  //            bodyContext.getTypeRef(), expressionList, false,
  //            node);
  //        ((BinConstructorInvocationExpression) expression).setNameAst(methodNode);
  //        break;

      default:
        if (Assert.enabled) {
          Assert.must(false, "Weird method call type: " + methodNode.getType());
        }

    }
    return expression;
  }

  /**
   * Tries to guess method return type from method invocation node.
   *
   * @param methodInvocationNode method invocation node
   * @return return type, null if unknown
   */
  private static final BinTypeRef guessReturnType(final ASTImpl methodInvocationNode,
      final BodyContext bodyContext) throws SourceParsingException {
  //    {
  //      //System.out.println("getParentTypeRef:" + bodyContext.getParentTypeRef());
  //      System.out.println("" + bodyContext.getExpressionList() );
  //    }

    final ASTImpl node = methodInvocationNode;
    //final ASTImpl parent = node.getParent();

    ASTImpl parent = node.getParent();
    ASTImpl significantBranch = null;
    while(parent.getType()==LPAREN){
      significantBranch = parent;
      parent = parent.getParent();
    }
    if (parent.getType() == LNOT || parent.getType() == LOR
        || parent.getType() == LAND) {
      return BinPrimitiveType.BOOLEAN_REF;
    } else
    if (parent.getType() == EXPR) {
      final ASTImpl parentParent = parent.getParent();
      switch (parentParent.getType()) {
        case SLIST:
          return BinPrimitiveType.VOID_REF;
        case LITERAL_if:
          return BinPrimitiveType.BOOLEAN_REF;
        case LITERAL_switch:
          return BinPrimitiveType.INT_REF;
        case LITERAL_return:
          if(bodyContext.getBlock() instanceof BinMethod) {
            return ((BinMethod) bodyContext.getBlock()).getReturnType();
          } else {
            return null;
          }
        default:
          if (LoadingASTUtil.isAssignmentNode(parentParent)) {
            final ASTImpl defNode = parentParent.getParent();
            final ASTImpl typeDefNode = ASTUtil
                .getFirstChildOfType(defNode, TYPE);
            final ASTImpl typeNode = (ASTImpl) typeDefNode.getFirstChild();

            return CompilationUnitsLoader.buildSpecificTypeRef(typeNode, bodyContext, null);
          } else {
            return null;
          }
      }
    } else if (LoadingASTUtil.isAssignmentNode(parent)) {
      final ASTImpl assignTo = ASTUtil.getFirstChildOfType(parent, IDENT);
      if (assignTo == null) {
        // This happens in the case if "this.field", for example -- then the DOT (.) is the only child
        return null;
      }
      final BinVariable variable = bodyContext.getLocalVariableForName(
          assignTo.getText());
      if (variable != null) {
        return variable.getTypeRef();
      } else {
        return null;
      }
    } else if (LoadingASTUtil.isComparisonNode(parent)
        || LoadingASTUtil.isMathematicalOperationNode(parent)) {
      // the compared nodes should be of same type (although not necessarily)

      ASTImpl comparedTo = (ASTImpl)parent.getFirstChild();

      while(comparedTo.equals(node) || comparedTo.equals(significantBranch)){
        comparedTo = (ASTImpl)comparedTo.getNextSibling();
      }

      if (comparedTo != null) {
        if (comparedTo.getType() == IDENT) {
          final BinVariable variable = bodyContext.getLocalVariableForName(
              comparedTo.getText());
          if (variable != null) {
            return variable.getTypeRef();
          } else {
            return null;
          }
        } else if (LoadingASTUtil.isExplicitValue(comparedTo)) {
          try {
            return getTypeRefForExplicitValueNode(comparedTo, bodyContext);
          } catch (Exception e) {
            return null;
          }
        } else if (comparedTo.getType() == METHOD_CALL) {
          return getReturnTypeForMethodNode(comparedTo, bodyContext);
        }
      }
    } else if (parent.getType() == TYPECAST) {
      ASTImpl typeNameAst =(ASTImpl) parent.getFirstChild().getFirstChild();;
      try {
        BinTypeRef type = bodyContext.getTypeRefForName(typeNameAst.getText());
        return type;
      } catch (LocationlessSourceParsingException e) {}
    }
    return null;
  }

  private static final BinTypeRef getReturnTypeForMethodNode(final ASTImpl node,
      final BodyContext bodyContext) {
    // TODO:
    return null;
  }

  private static final BinTypeRef getTypeRefForExplicitValueNode(
      final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException,
      LocationlessSourceParsingException {
    switch (node.getType()) {
      case LITERAL_true:
      case LITERAL_false:
        return BinPrimitiveType.BOOLEAN_REF;
      case NUM_INT:
        return BinPrimitiveType.INT_REF;
      case CHAR_LITERAL:
        return BinPrimitiveType.CHAR_REF;
      case STRING_LITERAL:
        final BinTypeRef ref = bodyContext.getTypeRef().getResolver().resolve(
            "java.lang.String");
        return ref;
      case NUM_FLOAT:
        return BinPrimitiveType.FLOAT_REF;
      case NUM_LONG:
        return BinPrimitiveType.LONG_REF;
      case NUM_DOUBLE:
        return BinPrimitiveType.DOUBLE_REF;
      default:
        return null;
    }
  }

  //  private static BinTypeRef getBinTypeRefFromAst(final ASTImpl typeNode, final BodyContext bodyContext) {
  //    switch (typeNode.getType()) {
  //      //case ARRAY_DECLARATOR: return new BinArrayType();
  //      case LITERAL_boolean: return Project.BOOLEAN_REF;
  //      case LITERAL_byte: return Project.BYTE_REF;
  //      case LITERAL_char: return Project.CHAR_REF;
  //      case LITERAL_double: return Project.DOUBLE_REF;
  //      case LITERAL_float: return Project.FLOAT_REF;
  //      case LITERAL_int: return Project.INT_REF;
  //      case LITERAL_long: return Project.LONG_REF;
  //      case LITERAL_short: return Project.SHORT_REF;
  //      case IDENT:
  //        try {
  //          return bodyContext.getTypeRefForName(typeNode.getText());
  //        } catch (Exception e) {
  //          return null;
  //        }
  //      default: return null;
  //    }
  //  }

  // FIXME: we need to guard this against exceptions
  private int fromDOT = 0;

  private final BinExpression buildStatementExpression(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    try {
      return buildExpressionForExpr(node, bodyContext);
    } catch (NullPointerException e) {
      // JAVA5: falls here on assert, must be fixed in a proper way
      e.printStackTrace();
      bodyContext.getProject().getProjectLoader().getErrorCollector()
          .addNonCriticalUserFriendlyError(new UserFriendlyError(
          "Fall on unknown expression: " + node,
          bodyContext.getCompilationUnit(), node));
      return new BinEmptyExpression();
    }
  }

  private final BinTryStatement.Finally extractFinally(ASTImpl curNode,
      final BodyContext bodyContext) throws SourceParsingException {
    BinTryStatement.Finally result = null;

    while (curNode != null) {
      if (curNode.getType() == LITERAL_finally) {
        final ASTImpl statementListNode = (ASTImpl) curNode.getFirstChild();
        final BinStatementList statementList
            = buildStatementList(statementListNode, bodyContext);

        result = new BinTryStatement.Finally(statementList, curNode);
        break;
      }
      curNode = (ASTImpl) curNode.getNextSibling();
    }

    return result;
  }

  private final BinTryStatement.CatchClause[] buildCatchList(ASTImpl curNode,
      final BodyContext bodyContext) throws SourceParsingException {

    if (Settings.debugLevel > 50) {
      System.out.println("    CatchClause statement");
    }

    final List catchList = new ArrayList();

    while (curNode != null) {
      if (Assert.enabled
          && curNode.getType() != LITERAL_catch
          && curNode.getType() != LITERAL_finally) {
        Assert.must(false, "CatchClause or Finally statement expected !");
      }
      if (curNode.getType() == LITERAL_finally) {
        curNode = (ASTImpl) curNode.getNextSibling();
        continue; // we handle this in extractFinally
      }
      final ASTImpl parameterNode = (ASTImpl) curNode.getFirstChild();
      final ASTImpl statementListNode = (ASTImpl) parameterNode.
          getNextSibling();

      // NOTE: The BinSpecificTypeRef is *ALREADY* bound to BinVariable
      final BinCatchParameter param = buildCatchParameter(parameterNode, bodyContext);
      param.setOwner(bodyContext.getBlock().getOwner());

      bodyContext.beginScope();
      BinStatementList statementList = null;
      try {
        bodyContext.addVariable(param);
        statementList
            = buildStatementList(statementListNode, bodyContext);
        bodyContext.attachScopeReceiver(statementList);
      } finally {
        bodyContext.endScope();
      }

      catchList.add(new BinTryStatement.CatchClause(param, statementList,
          curNode));

      curNode = (ASTImpl) curNode.getNextSibling();
    }

    return (BinTryStatement.CatchClause[]) catchList.toArray(
        new BinTryStatement.CatchClause[catchList.size()]);
  }

  private final BinSwitchStatement.CaseGroup[] buildCaseGroup(ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    final List caseGroupList = new ArrayList();

    while (node != null) {
      if (Assert.enabled && node.getType() != CASE_GROUP) {
        Assert.must(false, "Case group expected !");
      }
      ASTImpl groupNode = (ASTImpl) node.getFirstChild();
      if (Assert.enabled && groupNode == null) {
        Assert.must(false, "Bad case group !");
      }

      final List caseList = new ArrayList();
      BinStatementList statementList = null;
      while (groupNode != null) {
        switch (groupNode.getType()) {
          case LITERAL_case:
            final ASTImpl expressionNode = (ASTImpl) groupNode.getFirstChild();
            final BinExpression expression
                = buildExpressionForExpr(expressionNode, bodyContext);
            caseList.add(new BinSwitchStatement.Case(expression, groupNode));
            break;
          case LITERAL_default:
            caseList.add(new BinSwitchStatement.Case(null, groupNode));
            break;
          case SLIST:
            if (Assert.enabled && statementList != null) {
              Assert.must(false,
                  "Statement list already exists !? weird...");
            }
            statementList = buildStatementList(groupNode, bodyContext);

            // FIXME: looks like it is assumed, that SLIST is always the last sibling
            // possible bug?
            break;
          default:
            if (Assert.enabled) {
              Assert.must(false, "Bad case group !");
            }
        }
        groupNode = (ASTImpl) groupNode.getNextSibling();
      }
      final BinSwitchStatement.Case[] caseArray = (BinSwitchStatement.Case[])
          caseList.toArray(new BinSwitchStatement.Case[caseList.size()]);
      caseGroupList.add(new BinSwitchStatement.CaseGroup(caseArray,
          statementList, groupNode));
      node = (ASTImpl) node.getNextSibling();
    }

    return (BinSwitchStatement.CaseGroup[]) caseGroupList.toArray(new
        BinSwitchStatement.CaseGroup[caseGroupList.size()]);
  }

  private final BinStatement buildStatement(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    final int nodeType = node.getType();
    BinStatement statement = null;

    switch (nodeType) {
      case EMPTY_STAT: {
        if (Settings.debugLevel > 50) {
          System.out.println("    empty statement");
        }
        statement = new BinEmptyStatement(node);
        break;
      }
      case LITERAL_if: {
        if (Settings.debugLevel > 50) {
          System.out.println("    if statement");
        }
        if(node.getNumberOfChildren() < 2) {
          userFriendlyError("Invalid if statement : " + nodeType, bodyContext, node);
        }
        final ASTImpl conditionNode = (ASTImpl) node.getFirstChild();
        final ASTImpl trueNode = (ASTImpl) conditionNode.getNextSibling();
        final ASTImpl falseNode = (ASTImpl) trueNode.getNextSibling();

        final BinExpression condition = buildExpressionForExpr(
            conditionNode, bodyContext);

        final BinStatementList trueStatementList
            = buildStatementList(trueNode, bodyContext);
        BinStatementList falseStatementList = null;
        if (falseNode != null) {
          falseStatementList
              = buildStatementList(falseNode, bodyContext);
        }
        statement = new BinIfThenElseStatement(condition, trueStatementList,
            falseStatementList, node);
      }
      break;
      case LITERAL_while: {
        if (Settings.debugLevel > 50) {
          System.out.println("    while statement");
        }
        final ASTImpl conditionNode = (ASTImpl) node.getFirstChild();
        final ASTImpl statementListNode = (ASTImpl) conditionNode.
            getNextSibling();

        final BinExpression condition = buildExpressionForExpr(
            conditionNode, bodyContext);

        statement = new BinWhileStatement(condition, false, node);

        bodyContext.startBreakTarget(statement);
        try {
          final BinStatementList statementList = buildStatementList(
              statementListNode, bodyContext);
          ((BinWhileStatement) statement).setStatementList(statementList);
        } finally {
          bodyContext.endBreakTarget();
        }
      }
      break;

      case LITERAL_assert:
        final ASTImpl testExprNode = (ASTImpl) node.getFirstChild();
        final ASTImpl messageExprNode = (ASTImpl) testExprNode.getNextSibling();
        final BinExpression testExpression
            = buildExpressionForExpr(testExprNode, bodyContext);
        BinExpression messageExpression = null;
        if (messageExprNode != null) {
          messageExpression
              = buildExpressionForExpr(messageExprNode, bodyContext);
        }
        statement
            = new BinAssertStatement(testExpression, messageExpression, node);
        break;

      case LITERAL_do: {
        if (Settings.debugLevel > 50) {
          System.out.println("    do statement");
        }
        final ASTImpl statementListNode = (ASTImpl) node.getFirstChild();
        final ASTImpl conditionNode = (ASTImpl) statementListNode.
            getNextSibling();

        final BinExpression condition = buildExpressionForExpr(
            conditionNode, bodyContext);

        statement = new BinWhileStatement(condition, true, node);

        bodyContext.startBreakTarget(statement);
        try {
          final BinStatementList statementList = buildStatementList(
              statementListNode, bodyContext);
          ((BinWhileStatement) statement).setStatementList(statementList);
        } finally {
          bodyContext.endBreakTarget();
        }
      }
      break;
      case LITERAL_for: {
        bodyContext.beginScope();
        try {
          if (Settings.debugLevel > 50) {
            System.out.println("    for statement");
          }
          final ASTImpl initNode = (ASTImpl) node.getFirstChild();
          final ASTImpl statementListNode;
          BinSourceConstruct init = null;
          BinExpression condition = null;
          BinExpressionList iteratorExpressionList = null;

          if (initNode.getType() == FOR_EACH_CLAUSE) {
            statementListNode = (ASTImpl) initNode.getNextSibling();

            final ASTImpl varNode = (ASTImpl) initNode.getFirstChild();
            init = buildVariableDef(
                LoadingASTUtil.findDefNodesOfOneDeclaration(varNode),
                bodyContext);
            ASTImpl exprNode = (ASTImpl) varNode.getNextSibling();
            if (init != null && exprNode != null) {
              exprNode = (ASTImpl) exprNode.getFirstChild();
              final BinLocalVariable var
                  = (BinLocalVariable) ((BinLocalVariableDeclaration) init)
                  .getVariables()[0];
              var.setExpression(buildExpressionForExpr(exprNode, bodyContext));
            }
          } else {
            final ASTImpl conditionNode = (ASTImpl) initNode.getNextSibling();
            final ASTImpl iteratorNode = (ASTImpl) conditionNode.getNextSibling();
            statementListNode = (ASTImpl) iteratorNode.getNextSibling();

            final ASTImpl initExpressionNode = (ASTImpl) initNode.getFirstChild();
            if (initExpressionNode != null) {
              if (initExpressionNode.getType() == VARIABLE_DEF) {
                init = buildVariableDef(LoadingASTUtil.
                    findDefNodesOfOneDeclaration(initExpressionNode),
                    bodyContext);
              } else {
                init = buildExpressionList(initExpressionNode, bodyContext);
              }
            }

            if (conditionNode.getFirstChild() != null) {
              condition = buildExpressionForExpr(
                  (ASTImpl) conditionNode.getFirstChild(), bodyContext);
            }

            if (iteratorNode.getFirstChild() != null) {
              iteratorExpressionList = buildExpressionList(
                  (ASTImpl) iteratorNode.getFirstChild(), bodyContext);
            }
          }

          statement = new BinForStatement(init, condition,
              iteratorExpressionList, node);

          bodyContext.startBreakTarget(statement);
          try {
            final BinStatementList statementList = buildStatementList(
                statementListNode, bodyContext);
            bodyContext.attachScopeReceiver((BinForStatement) statement);
            ((BinForStatement) statement).setStatementList(statementList);
          } finally {
            bodyContext.endBreakTarget();
          }
        } finally {
          bodyContext.endScope();
        }

      }
      break;

      case CTOR_CALL:
        statement = buildConstructorInvocation(node, bodyContext);
        break;

      case SUPER_CTOR_CALL:
        statement = new BinExpressionStatement(
            buildSuperConstructorInvocation(node, bodyContext), node);
        break;

      case EXPR: {
        if (Settings.debugLevel > 50) {
          System.out.println("    expression statement");
        }
        final BinExpression expression = buildStatementExpression(node,
            bodyContext);
        statement = new BinExpressionStatement(expression, node);
      }
      break;

      case LITERAL_switch: {
        if (Settings.debugLevel > 50) {
          System.out.println("    switch statement");
        }
        final ASTImpl conditionNode = (ASTImpl) node.getFirstChild();
        final ASTImpl caseGroupListNode
            = (ASTImpl) conditionNode.getNextSibling();

        final BinExpression condition = buildExpressionForExpr(
            conditionNode, bodyContext);
        statement = new BinSwitchStatement(condition, node);

        bodyContext.startBreakTarget(statement);
        bodyContext.startSwitch(condition.getReturnType());
        try {
          final BinSwitchStatement.CaseGroup[] caseGroupList
              = buildCaseGroup(caseGroupListNode, bodyContext);
          ((BinSwitchStatement) statement).setCaseGroupList(caseGroupList);
        } finally {
          bodyContext.endSwitch();
          bodyContext.endBreakTarget();
        }
      }
      break;
      case LABELED_STAT: {
        if (Settings.debugLevel > 50) {
          System.out.println("    labeled statement");
        }
        final ASTImpl identNode = (ASTImpl) node.getFirstChild();
        final ASTImpl statementListNode = (ASTImpl) identNode.getNextSibling();

        final String identifier = LoadingASTUtil.getIdent(identNode);
        statement = new BinLabeledStatement(identifier, node);

        bodyContext.startLabel(identifier, statement);
        try {
          final BinStatementList statementList = buildStatementList(
              statementListNode, bodyContext);
          ((BinLabeledStatement) statement).setLabelStatementList(
              statementList);
        } finally {
          bodyContext.stopLabel();
        }
      }
      break;
      case LITERAL_break: {
        if (Settings.debugLevel > 50) {
          System.out.println("    break statement");
        }
        final ASTImpl identNode = (ASTImpl) node.getFirstChild();
        String identifier = null;
        if (identNode != null) {
          identifier = LoadingASTUtil.getIdent(identNode);
        }
        BinStatement breakTo = null;
        if (identifier != null) {
          breakTo = bodyContext.getStatementForLabel(identifier);
        } else {
          breakTo = bodyContext.getBreakTarget();
        }
        statement = new BinBreakStatement(identifier, true, breakTo, node);
      }
      break;
      case LITERAL_continue: {
        if (Settings.debugLevel > 50) {
          System.out.println("    continue statement");
        }
        final ASTImpl identNode = (ASTImpl) node.getFirstChild();
        String identifier = null;
        if (identNode != null) {
          identifier = LoadingASTUtil.getIdent(identNode);
        }
        BinStatement breakTo = null;
        if (identifier != null) {
          breakTo = bodyContext.getStatementForLabel(identifier);
        } else {
          breakTo = bodyContext.getBreakTarget();
        }
        statement = new BinBreakStatement(identifier, false, breakTo, node);
      }
      break;
      case LITERAL_return: {
        if (Settings.debugLevel > 50) {
          System.out.println("    return statement");
        }
        final ASTImpl returnExpressionNode = (ASTImpl) node.getFirstChild();
        BinExpression returnExpression = null;

        if (returnExpressionNode != null) {
          returnExpression = buildExpressionForExpr(returnExpressionNode,
              bodyContext);
        }
        statement = new BinReturnStatement(returnExpression, node);
      }
      break;
      case LITERAL_synchronized: {
        if (Settings.debugLevel > 50) {
          System.out.println("    synchronized statement");
        }
        final ASTImpl expressionNode = (ASTImpl) node.getFirstChild();
        final ASTImpl statementListNode
            = (ASTImpl) expressionNode.getNextSibling();

        final BinExpression expression = buildExpressionForExpr(
            expressionNode, bodyContext);
        final BinStatementList statementList = buildStatementList(
            statementListNode, bodyContext);

        statement = new BinSynchronizedStatement(expression, statementList,
            node);
      }
      break;
      case LITERAL_throw: {
        if (Settings.debugLevel > 50) {
          System.out.println("    throw statement: " + node.getText());
        }
        final ASTImpl expressionNode = (ASTImpl) node.getFirstChild();
        final BinExpression expression = buildExpressionForExpr(
            expressionNode, bodyContext);
        statement = new BinThrowStatement(expression, node);
      }
      break;
      case LITERAL_try: {
        if (Settings.debugLevel > 50) {
          System.out.println("    try statement");
        }
        final ASTImpl statementListNode = (ASTImpl) node.getFirstChild();
        final ASTImpl catchListNode = (ASTImpl) statementListNode.
            getNextSibling();

        final BinStatementList statementList
            = buildStatementList(statementListNode, bodyContext);
        final BinTryStatement.TryBlock aTry
            = new BinTryStatement.TryBlock(statementList,
            statementList.getRootAst(bodyContext.getCompilationUnit()));
        final BinTryStatement.CatchClause[] catchList
            = buildCatchList(catchListNode, bodyContext);
        final BinTryStatement.Finally aFinally
            = extractFinally(catchListNode, bodyContext);

        statement = new BinTryStatement(aTry, catchList, aFinally, node);
      }
      break;
      case SLIST: {
        if (Settings.debugLevel > 50) {
          System.out.println("           statement list");
        }
        statement = buildStatementList(node, bodyContext);
      }
      break;
      case VARIABLE_DEF: {
        throw new IllegalArgumentException("VARIABLE_DEFs are now built in another method because VARIABLE_DEF nodes can come in groups");
      }
      //break;

      case INTERFACE_DEF:
      case ENUM_DEF:
      case CLASS_DEF:
      case ANNOTATION_DEF:
        if (Settings.debugLevel > 50) {
          System.out.println("    class or interface definition statement");
        }
        statement = new BinCITypesDefStatement(
            buildLocalType(node, bodyContext), node);
        break;

      default:
        userFriendlyError(
            "Unknown statement : " + nodeType,
            bodyContext,
            node
            );
    }

    // if(Assert.enabled){
    //	{Assert.must( source != null, "source must not be null");}
    // }
    // statement.setSource(source);

    return statement;
  }

  private final BinStatement buildConstructorInvocation(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println(" ctor_call:");
    }

    assertNotInterfaceForBug1733(node, bodyContext, 0);

    ASTImpl nameAST = (ASTImpl) node.getFirstChild();
    ASTImpl expressionListAST = (ASTImpl) nameAST.getNextSibling();

    BinTypeRef[] typeArguments = null;

    if (expressionListAST.getType() != ELIST) {
      // Type arguments case: <String>this("str");
      typeArguments = CompilationUnitsLoader
          .buildTypeArguments(expressionListAST, null, bodyContext);
//      bodyContext.getProject().addNonCriticalUserFriendlyError(
//          new UserFriendlyError(
//          "Parameterized constructors are not yet supported: " + node,
//          bodyContext.getCompilationUnit(), node));

      expressionListAST = (ASTImpl) expressionListAST.getNextSibling();
    }

    final BinExpressionList elist = buildExpressionList(
        expressionListAST, bodyContext);
    final BinConstructorInvocationExpression expression
        = new BinConstructorInvocationExpression(
        bodyContext.getTypeRef(),
        bodyContext.getTypeRef(),
        elist,
        false, node);
    expression.setNameAst(nameAST);
    expression.setTypeArguments(typeArguments);

    final BinStatement statement = new BinExpressionStatement(expression, node);

    final BinConstructor ctor = expression.getConstructor();
    if (ctor == null) {
      userFriendlyError("Wrong constructor call: "
          + bodyContext.getTypeRef().getQualifiedName() + "("
          + displayableListOfReturnTypes(elist) + ")",
          bodyContext, node);
    }

    if (typeArguments != null) {
      for (int i = 0, max = typeArguments.length; i < max; i++) {
        ((BinSpecificTypeRef) typeArguments[i])
            .setTypeParameterResolver(ctor, i);
      }
    }

    return statement;
  }

  private final BinExpression buildSuperConstructorInvocation(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println(" super_ctor_call:");
    }

    ASTImpl nameAST = (ASTImpl) node.getFirstChild();
    ASTImpl expressionListAST = (ASTImpl) nameAST.getNextSibling();

    /** JLS 8.8.5.1
     Qualified superclass constructor invocations begin with a Primary
     expression. They allow a subclass constructor to explicitly specify
     the newly created object's immediately enclosing instance with respect
     to the direct superclass (�8.1.2). This may be necessary when the
     superclass is an inner class. Here is an example of a qualified
     superclass constructor invocation:
      class Outer {
        class Inner{}
      }
      class ChildOfInner extends Outer.Inner {
        ChildOfInner(){
          (new Outer()).super();
        }
      }
     */
    BinExpression primaryExpr = null;
    BinTypeRef[] typeArguments = null;
    if (expressionListAST.getType() != ELIST) {
      /*System.err.println(bodyContext.getCompilationUnit().getRelativePath());
                 ASTDebugOn(node);*/

      if (expressionListAST.getType() != TYPE_ARGUMENTS) {
        primaryExpr = buildExpression(expressionListAST, bodyContext, true);
      } else {
        // Type arguments case: <String>super("str");
        typeArguments = CompilationUnitsLoader
            .buildTypeArguments(expressionListAST, null, bodyContext);

//        bodyContext.getProject().addNonCriticalUserFriendlyError(
//            new UserFriendlyError(
//            "Parameterized constructors are not yet supported: " + node,
//            bodyContext.getCompilationUnit(), node));
      }
      expressionListAST = (ASTImpl) expressionListAST.getNextSibling();
    }

    final BinExpressionList elist
        = buildExpressionList(expressionListAST, bodyContext);

    assertNotInterfaceForBug1733(node, bodyContext, 1);

    final BinConstructorInvocationExpression expression
        = new BinConstructorInvocationExpression(
        bodyContext.getTypeRef(),
        bodyContext.getTypeRef().getSuperclass(),
        elist,
        true, node);
    expression.setPrimaryExpression(primaryExpr);
    expression.setNameAst(nameAST);
    expression.setTypeArguments(typeArguments);

    final BinConstructor ctor = expression.getConstructor();
    if (ctor == null) {
      userFriendlyError("Wrong constructor call: "
          + bodyContext.getTypeRef().getSuperclass().getQualifiedName()
          + "(" + displayableListOfReturnTypes(elist) + ")",
          bodyContext, node);
    }

    if (typeArguments != null) {
      for (int i = 0, max = typeArguments.length; i < max; i++) {
        ((BinSpecificTypeRef) typeArguments[i])
            .setTypeParameterResolver(ctor, i);
      }
    }

    return expression;
  }

  private final BinLocalVariableDeclaration buildVariableDef(final ArrayList nodes,
      final BodyContext bodyContext) throws SourceParsingException {
    if (Settings.debugLevel > 50) {
      System.out.println("    variable definition statement");
    }

    final BinLocalVariable[] variables = new BinLocalVariable[nodes.size()];
    for (int i = 0; i < variables.length; i++) {
      final ASTImpl node = (ASTImpl) nodes.get(i);
  //new rantlr.debug.misc.ASTFrame("var", node).setVisible(true);
      // The BinSpecificTypeRef is *already* bound to this object
      variables[i] = buildLocalVariable(node, bodyContext);
      bodyContext.addVariable(variables[i]);

      final BinExpression initExpression
          = buildVariableInitExpression(node, bodyContext);
      variables[i].setExpression(initExpression);
    }

    return new BinLocalVariableDeclaration(variables, (ASTImpl) nodes.get(0));
  }

  /** %%% Debug method, remove this method later ??? */
  private static final void assertNotInterfaceForBug1733(final ASTImpl node,
      final BodyContext bodyContext, final int code) {
    final BinCIType type = bodyContext.getTypeRef().getBinCIType();
    if (type instanceof BinInterface) {
      final StringBuffer s = new StringBuffer(
          "%%% REFACTORIT ERROR, PLEASE REPORT: CODE " +
          code + ", AST NODES: ");
      ASTImpl n = node;
      do {
        s.append(n.getType());
        s.append(" ");
        n = n.getParent();
      } while (n != null);
      System.err.println(s.toString());
    }
  }

  public final BinTypeRef buildAnonymousType(final ASTImpl node,
      final BinTypeRef superclass, final String className, final BodyContext bodyContext)
      throws SourceParsingException {
  //System.err.println("buildAnonymousType - node: " + node
  //    + ", superclass: " + superclass + ", context: " + bodyContext);
//new rantlr.debug.misc.ASTFrame("super: " + superclass, node).setVisible(true);
    final BinTypeRef retVal
        = buildLoadingAnonymousType(node, superclass, className, bodyContext);
//System.err.println("retVal: " + retVal);
    try {
      resolversForLocalTypes(bodyContext, retVal);
      resolveLocalTypeSupersAndInterfaces(bodyContext, retVal);
      buildLocalBinTypes(bodyContext, retVal);
      createDefaultConstructorForAnonymous(bodyContext, retVal);
      buildLocalBinTypesFieldsAndMethodBodys(bodyContext, retVal);
    } finally {
      bodyContext.endType();
    }

    return retVal;
  }

  private static final void createDefaultConstructorForAnonymous(final BodyContext
      bodyContext, final BinTypeRef retVal) {
  //          if (Assert.enabled) {
  //            Assert.must(bodyContext.getExpressionList() != null,
  //                "Expression list for anonymous constructor is null: " + bc,
  //                typeRootNode);
  //          }

    final BinExpression[] exprs
        = bodyContext.getExpressionList().getExpressions();
    final BinTypeRef[] paramTypes = new BinTypeRef[exprs.length];
    for (int i = 0; i < exprs.length; i++) {
      paramTypes[i] = exprs[i].getReturnType();
    }

    final BinClass bc = (BinClass) retVal.getBinCIType();
    if (!bc.hasDefaultConstructor()) {
      // anonymous did not have this constructor
//System.err.println("superclass: " + bc.getTypeRef().getSuperclass().getBinCIType());
      final BinCIType superClass = bc.getTypeRef().getSuperclass().getBinCIType();
      // it *has* to have it
      if (superClass instanceof BinClass) {
        final BinConstructor existing
            = ((BinClass) superClass).getAccessibleConstructor(bc, paramTypes);
        bc.createDefaultConstructor(
            bodyContext.getExpressionList(), existing);
      } else {
        bc.createDefaultConstructor(bodyContext.getExpressionList());
      }
      bodyContext.setExpressionList(null);
    }
  }

  private final BinTypeRef buildLocalType(final ASTImpl node,
      final BodyContext bodyContext) throws SourceParsingException {

    final BinTypeRef retVal = buildTypeInLocalContext(node, bodyContext);
    try {
      resolversForLocalTypes(bodyContext, retVal);
      resolveLocalTypeSupersAndInterfaces(bodyContext, retVal);
      buildLocalBinTypes(bodyContext, retVal);
      buildLocalBinTypesFieldsAndMethodBodys(bodyContext, retVal);
    } finally {
      bodyContext.endType();
    }

    return retVal;
  }

  private final void resolversForLocalTypes(final BodyContext bodyContext,
      final BinTypeRef localRef) {

    final BinTypeRef typeRef = localRef;
    typeRef.setResolver(
        Resolver.getForLocalType(typeRef,
        bodyContext.getParentTypeRef(),
        bodyContext.getParentLocalTypeRefs())
        );

    resolversForLocalTypeInners(bodyContext, typeRef.getBinCIType());

  }

  private final void resolversForLocalTypeInners(final BodyContext bodyContext,
      final BinCIType aType) {
    final BinTypeRef[] inners = aType.getDeclaredTypes();
    for (int l = 0; l < inners.length; ++l) {
      inners[l].setResolver(
          Resolver.getForSourceType(inners[l], bodyContext.getCompilationUnit())
          );
      resolversForLocalTypeInners(bodyContext, inners[l].getBinCIType());
    }
  }

  private final void resolveLocalTypeSupersAndInterfaces(final BodyContext
      bodyContext, final BinTypeRef typeRef) throws SourceParsingException {

    typeRef.getResolver().resolveSuperTypes();

    deepResolveSupersAndInterfaces(typeRef.getBinCIType());

  }

  private final void deepResolveSupersAndInterfaces(final BinCIType aType) throws
      SourceParsingException {
    final BinTypeRef[] inners = aType.getDeclaredTypes();
    for (int l = 0; l < inners.length; ++l) {
      inners[l].getResolver().resolveSuperTypes();
      deepResolveSupersAndInterfaces(inners[l].getBinCIType());
    }
  }

  private final void buildLocalBinTypes(final BodyContext bodyContext,
      final BinTypeRef typeRef) throws SourceParsingException {
    if (!typeRef.isBuilt()) {
      bodyContext.getProject().getProjectLoader().getSourceLoader()
          .buildType(typeRef, bodyContext);
    }
    deepBuildBinTypes(bodyContext, typeRef);
  }

  private final void deepBuildBinTypes(final BodyContext bodyContext,
      final BinTypeRef typeRef) throws SourceParsingException {
    final BinTypeRef[] inners = typeRef.getBinCIType().getDeclaredTypes();
    for (int l = 0; l < inners.length; ++l) {
      bodyContext.startType(inners[l]);
      try {
        if (!inners[l].isBuilt()) { // useless? it is not built yet usually...
          bodyContext.getProject().getProjectLoader().getSourceLoader()
              .buildType(inners[l], bodyContext);
        }
        deepBuildBinTypes(bodyContext, inners[l]);
      } finally {
        bodyContext.endType();
      }
    }
  }

  private final void buildLocalBinTypesFieldsAndMethodBodys(
      final BodyContext bodyContext,
      final BinTypeRef typeRef) throws SourceParsingException {

    buildFieldsAndMethodBodys(typeRef, bodyContext);

    deepBuildFieldsAndMethodBodies(bodyContext, typeRef);

  }

  private final void deepBuildFieldsAndMethodBodies(final BodyContext bodyContext,
      final BinTypeRef typeRef) throws SourceParsingException {
    final BinTypeRef[] inners = typeRef.getBinCIType().getDeclaredTypes();
    for (int l = 0; l < inners.length; ++l) {
      bodyContext.startType(inners[l]);
      try {
        buildFieldsAndMethodBodys(inners[l], bodyContext);
        deepBuildFieldsAndMethodBodies(bodyContext, inners[l]);
      } finally {
        bodyContext.endType();
      }
    }
  }

  //
  // Accessor methods
  //

  /** Sample return value: "String, int". */
  public static final String displayableListOfReturnTypes(final BinExpressionList
      expressionList) {
    final StringBuffer result = new StringBuffer();

    final BinExpression[] expressions = expressionList.getExpressions();
    for (int i = 0; i < expressions.length; i++) {
      result.append(i > 0 ? ", " : "");
      result.append(expressions[i].getReturnType() != null
          ? BinFormatter.formatNotQualified(expressions[i].getReturnType())
          : "null");
    }

    return result.toString();
  }

  private static final BinCatchParameter buildCatchParameter(
      final ASTImpl parameterNode, final BodyContext bodyContext)
      throws SourceParsingException {

    if (parameterNode.getType() != JavaTokenTypes.PARAMETER_DEF) {
      SourceParsingException.throwWithUserFriendlyError(
          "invalid ASTImpl node type for parameterNode: " + parameterNode,
          bodyContext.getCompilationUnit(),
          parameterNode
          );
    }

    final ASTImpl modifierNode = (ASTImpl) parameterNode.getFirstChild();
    final ASTImpl typeNode = (ASTImpl) modifierNode.getNextSibling();
    final ASTImpl identNode = (ASTImpl) typeNode.getNextSibling();

    final BinTypeRef typeRef
        = CompilationUnitsLoader.buildSpecificTypeRef((ASTImpl) typeNode.getFirstChild(), bodyContext, null);

    final int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);
    final BinCatchParameter param = new BinCatchParameter(
        identNode.getText(), typeRef, modifiers);

  //    param.setCompilationUnit(getCompilationUnit((ASTImpl) parameterNode, bodyContext));
    param.setOffsetNode(parameterNode);
    param.setNameAst(identNode);

    return param;
  }

  // FIXME: mostly copy-pasted
  private static final BinLocalVariable buildLocalVariable(
      final ASTImpl varDefNode,
      final BodyContext bodyContext) throws SourceParsingException {
    ASTImpl modifiersNode = ASTUtil.getFirstChildOfType(varDefNode,
        JavaTokenTypes.MODIFIERS);
    BinExpressionList annotationsList = CompilationUnitsLoader
        .buildAnnotationExpressionList(modifiersNode, bodyContext);

    final ASTImpl modifierNode = (ASTImpl) varDefNode.getFirstChild();
    final int modifiers = LoadingASTUtil.getModifiersForAST(modifierNode);

    final ASTImpl typeNode = (ASTImpl) modifierNode.getNextSibling();
    if (Assert.enabled && typeNode.getType() != JavaTokenTypes.TYPE) {
      Assert.must(false,
        " Type expected! found(" + String.valueOf(typeNode.getType()) + ")");
    }

    final BinTypeRef typeRef = CompilationUnitsLoader.buildSpecificTypeRef((ASTImpl) typeNode.getFirstChild(), bodyContext, null);

    final ASTImpl nameNode = (ASTImpl) typeNode.getNextSibling();
    if (Assert.enabled && nameNode.getType() != JavaTokenTypes.IDENT) {
      Assert.must(false,
        " Identifier expected! found(" + String.valueOf(typeNode.getType())
        + ")");
    }

    final ASTImpl nameDeclaration = nameNode;

    final BinLocalVariable variable
        = new BinLocalVariable(nameNode.getText(), typeRef, modifiers);

    variable.setOwner(bodyContext.getBlock().getOwner());
    variable.setOffsetNode(varDefNode);
    variable.setNameAst(nameDeclaration);

    if (annotationsList != null) {
      variable.setAnnotations(annotationsList);
      annotationsList.setParent(variable);
    }

    return variable;
  }

  static final BinEnumConstant buildEnumConstant(final ASTImpl node, final BinTypeRef owner, BodyContext bodyContext) throws SourceParsingException {
    // NOTE: building annotations on buildExpressionForEnumConstant phase. Skipping it here
    final ASTImpl annotation = (ASTImpl) node.getFirstChild();

    final ASTImpl ident = (ASTImpl) annotation.getNextSibling();
    final BinEnumConstant enumConstant
        = new BinEnumConstant(ident.getText(), owner,
        BinModifier.ENUM | BinModifier.PUBLIC | BinModifier.STATIC
        | BinModifier.FINAL, false);
    enumConstant.setOffsetNode(node);
    enumConstant.setNameAst(ident);

    return enumConstant;
  }

  /**
   * starts type in body context - does not end it
   */
  private static final BinTypeRef buildTypeInLocalContext(final ASTImpl aNode,
      final BodyContext bodyContext) {

    final FastStack classStack = new FastStack(2);
    // NOTE: local type has owner!
    classStack.push(bodyContext.getTypeRef().getBinCIType());

    final LoadingSourceBinCIType aType
        = LoadingSourceBinCIType.build(aNode,
        bodyContext.getTypeRef().getBinCIType(), bodyContext.getCompilationUnit());
    final BinTypeRef typeRef = bodyContext.getProject().createLocalTypeRefForType(aType);

//    typeRef = BinSpecificTypeRef.create(bodyContext.getCompilationUnit(),
//        LoadingASTUtil.getTypeNodeFromDef(aNode), typeRef, false);
//    aType.setTypeRef(typeRef);

    aType.setLocal(true);
    bodyContext.addTypeRef(typeRef);
    bodyContext.setHasLocalTypes(true);
    bodyContext.startType(typeRef);

    final ASTImpl child = (ASTImpl) aNode.getFirstChild();
    if (child != null) {
      classStack.push(aType);
      buildLocalTypesInners(child, bodyContext, classStack);
    }

    return typeRef;
  }

  /**
   * starts type in body context - does not end it
   */
  private static final BinTypeRef buildLoadingAnonymousType(
      final ASTImpl aNode,
      final BinTypeRef superType,
      String className,
      final BodyContext bodyContext) throws SourceParsingException {
    final FastStack classStack = new FastStack(2);
    final int modifiers = 0; // FIXME: ?

    final String superclassName;
    final String[] interfaces;

    if (!superType.getBinType().isInterface()) {
      superclassName = superType.getQualifiedName();
      interfaces = StringUtil.NO_STRINGS;
    } else {
      superclassName = Project.OBJECT;
      interfaces = new String[] {superType.getQualifiedName()};
    }

    final BinTypeRef ownerRef = bodyContext.getTypeRef();
    if (className == null) {
      className = new Integer(
          ownerRef.getBinCIType().getNextAnonymousNumber()).toString();
    }
    final LoadingSourceBinCIType aType = new LoadingSourceBinCIType(
        bodyContext.getCompilationUnit(), aNode,
        ownerRef,
        className,
        modifiers, superclassName, interfaces,
        bodyContext.getProject());

    if (!superType.getBinType().isAnnotation()) {
      aType.setAnonymous(true);
    }

    // FIXME: JLS 14.3 "A local class is a nested class (?8) that is not a member
    // of any class and that has a name."
    // This way anonymous class is inner, but not local!
    aType.setLocal(true);

    final BinTypeRef typeRef
        = bodyContext.getProject().createLocalTypeRefForType(aType);
    bodyContext.addTypeRef(typeRef);
    bodyContext.setHasLocalTypes(true);
    classStack.push(aType);

    bodyContext.startType(typeRef);

    final ASTImpl child = (ASTImpl) aNode.getFirstChild();
    if (child != null) {
      buildLocalTypesInners(child, bodyContext, classStack);
    }

    return typeRef;
  }

  private static final void buildLocalTypesInners(ASTImpl aNode,
      final BodyContext bodyContext, final FastStack classStack) {

    for (; aNode != null; aNode = (ASTImpl) aNode.getNextSibling()) {
      final int type = aNode.getType();
      boolean typeFound = false;
      BinTypeRef typeRef = null;

      if (type == JavaTokenTypes.CLASS_DEF || type == JavaTokenTypes.INTERFACE_DEF
          || type == JavaTokenTypes.ENUM_DEF || type == JavaTokenTypes.ANNOTATION_DEF) {
        LoadingSourceBinCIType owner = null;

        if (classStack.size() > 0) {
          owner = (LoadingSourceBinCIType) classStack.peek();
  //          BinTypeRef ownerRef = owner.getTypeRef();
        }

        final LoadingSourceBinCIType aType
            = LoadingSourceBinCIType.build(aNode, owner,
            bodyContext.getCompilationUnit());
        typeRef = bodyContext.getProject().createCITypeRefForType(aType);
        //typeRef = project.createLocalTypeRefForType(aType);

//        typeRef = BinSpecificTypeRef.create(bodyContext.getCompilationUnit(),
//            LoadingASTUtil.getTypeNodeFromDef(aNode), typeRef, false);
//        aType.setTypeRef(typeRef);

        aType.setLocal(true);

        if (owner != null) {
          owner.addDeclaredType(typeRef);
        }

        classStack.push(aType);
        typeFound = true;
        bodyContext.startType(typeRef);
      }

      try {
        if (type != JavaTokenTypes.METHOD_DEF && type != JavaTokenTypes.CTOR_DEF
            && type != JavaTokenTypes.STATIC_INIT
            && type != JavaTokenTypes.INSTANCE_INIT) {
          final ASTImpl child = (ASTImpl) aNode.getFirstChild();
          if (child != null) {
            buildLocalTypesInners(child, bodyContext, classStack);
          }
        }
      } finally {
        if (typeFound) {
          bodyContext.endType();
          classStack.pop();
        }
      }
    }
  }

}
