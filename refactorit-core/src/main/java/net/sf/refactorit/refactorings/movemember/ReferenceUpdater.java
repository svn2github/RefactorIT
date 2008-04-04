/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.common.InnersToQualifySeeker;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * @author Anton Safonov
 */
public class ReferenceUpdater {
  BinCIType targetType;

  List members;
  private HashSet membersToBeStatic = new HashSet();

  private final Map paramsToReuse = new HashMap();
  private final MultiValueMap extraTypesToImport = new MultiValueMap();
  final MultiValueMap invokedMethodsMap = new MultiValueMap();
  private final MultiValueMap invocationGotParameter = new MultiValueMap();
  private final List replaceWithThis = new ArrayList();

  // FIXME: remove this ?
  //private TransformationManager fakeManager = new TransformationManager(null);
  private List membersNeedToReceiveParam = new ArrayList();
  private HashMap parametersName = new HashMap();

  //contais all members visited in method addNativeTypeAsParam
  private List visitedMembersGlobal = new ArrayList();

  public ReferenceUpdater() {
  }

  public void createEditors(final TransformationList transList) {
    // 0
    updateUsages(transList);
  }

  public void analyze(final List members, final List membersToBeStatic,
      final BinCIType targetType) {
    this.membersToBeStatic = new HashSet(membersToBeStatic);

    //System.err.println("analyze - members: " + members + ", target: " +
    // targetType);
    this.members = members;
    this.targetType = targetType;

    paramsToReuse.clear();
    extraTypesToImport.clear();
    replaceWithThis.clear();

    for (int i = 0; i < members.size(); i++) {
      BinMember member = (BinMember) members.get(i);
      //System.err.println("parameter usage: " + member);
      findParamToUseAndRemove(member);
    }
  }

  /** @deprecated not needed - ConflictResolver handles it by its own means */
  public MultiValueMap getExtraTypesToImport() {
    return this.extraTypesToImport;
  }

  public List getParamsToSkip() {
    return new ArrayList(this.paramsToReuse.values());
  }

  // 1
  private void updateUsages(final TransformationList transList) {
    for (int i = 0, max = members.size(); i < max; i++) {
      BinMember member = (BinMember) members.get(i);
      //      System.out.println("updateUsages for:" + member.getName()); //innnnn

      if (targetType.isInnerType()
          //&& !targetType.isInterface()
          && !targetType.isStatic()
          && (member.getOwner().getBinCIType().getDeclaredType(
          targetType.getName()) != null)) {
        // move into native's nonstatic inner
        updateUsageForInner(member, transList);
      } else {
        /* boolean mustAddNativeTypeAsParam = */
        if(!targetType.isInterface() ) {
          addNativeTypeAsParam(member, transList, new ArrayList());
        }
      }

      if (member.isStatic() && !member.isPrivate()) {
      	ImportUtils.removeInvalidSingleStaticImports(transList, member);
      }
      //      System.out.println("isAddParameter for " + member.getName() + " = " +
      // mustAddNativeTypeAsParam); //innnnn
    }

    for (int i = 0, max = members.size(); i < max; i++) {
      BinMember member = (BinMember) members.get(i);
      //System.err.println("update usages: " + member + " - mustadd: " +
      // mustAddNativeTypeAsParam);
      List invocations = Finder.getInvocations(member);
      for (int j = 0, maxJ = invocations.size(); j < maxJ; j++) {
        InvocationData data = (InvocationData) invocations.get(j);

        //        System.out.println(member.getName() + " invoked in " +
        // data.getLocation()); //innnnn

        if (member.isStatic() || (member instanceof BinCIType)) {
          transList.getStatus().merge(updateStaticUsage(member, data, transList));

        } else {
          transList.getStatus().merge(updateInstanceUsage(member, data, transList,
              membersNeedToReceiveParam.contains(member)));
        }
      }

      if (member instanceof BinMethod) {
        removeParameter((BinMethod) member, transList);
      }

      addOwnerForInnerIfNecessary(member, transList);
    }
  }

  /**
   * adds ClassName.this. in front of every member which is invoked by moving
   * member
   */
  private void updateUsageForInner(final BinMember member,
      final TransformationList transList) {
    class Visitor extends AbstractIndexer {
      public void visit(BinMethodInvocationExpression x) {
        updateInvocation(x);
        super.visit(x);
      }

      public void visit(BinFieldInvocationExpression x) {
        updateInvocation(x);
        super.visit(x);
      }

      private void updateInvocation(BinMemberInvocationExpression x) {
        BinExpression thisExpr = x.getExpression();
        if (thisExpr == null) {
          ASTImpl invocAST = x.getNameAst();
          transList.add(new StringInserter(member.getCompilationUnit(), invocAST
              .getStartLine(), invocAST.getStartColumn() - 1, member.getOwner()
              .getName()
              + '.' + "this."));
        } else if (thisExpr instanceof BinLiteralExpression) {
          if (((BinLiteralExpression) thisExpr).getExpression() == null) {
            ASTImpl thisAST = ((BinLiteralExpression) thisExpr).getNameAst();
            transList.add(new StringInserter(member.getCompilationUnit(), thisAST
                .getStartLine(), thisAST.getStartColumn() - 1, member
                .getOwner().getName() + '.'));
          }
        }
      }
    }


    Visitor visitor = new Visitor();
    member.accept(visitor);
  }

  private void addOwnerForInnerIfNecessary(final BinMember member,
      final TransformationList transList) {

    InnersToQualifySeeker visitor = new InnersToQualifySeeker(this.targetType) {

    	protected boolean isInnerTypeOfTarget(BinCIType type) {
    		return (super.isInnerTypeOfTarget(type) || members.contains(type));
    	}
    };
    member.accept(visitor);
    visitor.generateEditors(transList, member.getCompilationUnit());
  }

  private RefactoringStatus updateStaticUsage(BinMember member,
      InvocationData data, final TransformationList transList) {
    SourceConstruct inConstruct = data.getInConstruct();
    if (Assert.enabled) {
      Assert.must(inConstruct != null, "Usage expression is null for: " + data);
    }
//    BinExpression inExpression = null;
//    if (inConstruct instanceof BinMemberInvocationExpression) {
//    	inExpression = ((BinMemberInvocationExpression)inConstruct).getExpression();
//
//    }

    if ((member instanceof BinCIType) && (members.contains(data.getWhereMember()))) {
    	// this is updated already
    	return null;
    }

    if ((member instanceof BinCIType) && (inConstruct instanceof BinMemberInvocationExpression)) {
    	// no need to update member invocation to inner type
    	return null;
    }


    if ((data.getWhereAst().getParent().getType() != JavaTokenTypes.DOT)
        || (data.getWhereAst().getParent().getFirstChild() == data.getWhereAst())) {

    //}
    //if (inExpression == null) { // implicit this
      if (!isLocationInFutureTarget(data.getWhereMember(), members, targetType)) {
        // add class name + DOT
        transList.add(new StringInserter(data.getCompilationUnit(),
        		data.getWhereAst().getStartLine(), data.getWhereAst().getStartColumn() - 1, targetType.getName() + '.'));
        this.extraTypesToImport.putNew(data.getWhereType().getBinCIType(),
            targetType);
      }
    } else {
      // rename
      ASTImpl node = data.getWhereAst();
      node = (ASTImpl) node.getParent().getFirstChild();
      node = new CompoundASTImpl(node);
      String newName;

      // NOTE may fail here a bit on inners or may not
      if (node.getText().indexOf('.') != -1) {
        newName = targetType.getQualifiedName();
      } else {
        newName =
          ImportUtils.isAmbiguousImport(targetType, data.getWhereMember().getParentType())
          			? targetType.getQualifiedName()
          	    :  targetType.getName();
      }

      transList.add(new RenameTransformation(data.getCompilationUnit(), node, newName));
      if (!isLocationInFutureTarget(data.getWhereMember(), members, targetType)) {
      	this.extraTypesToImport.putNew(data.getWhereType().getBinCIType(),
      			targetType);
      }
    }

    return null;
  }

  // 3
  private RefactoringStatus updateInstanceUsage(BinMember member,
      InvocationData data, final TransformationList transList,
      boolean mustAddNativeTypeAsParam) {
     SourceConstruct inConstruct = data.getInConstruct();
    if (Assert.enabled) {
      Assert.must(inConstruct != null, "Usage expression is null for: " + data);
    }

    BinMember instance = null;
    String instanceName = null;

    if (membersToBeStatic.contains(member)) {
      if (!members.contains(data.getWhereMember())) {
        instanceName = ImportUtils.isAmbiguousImport(targetType, data
            .getWhereType().getBinCIType())
            ? targetType.getQualifiedName()
            : targetType.getName();
        this.extraTypesToImport.putNew(data.getWhereType().getBinCIType(),
            targetType);
      } else {
        instanceName = "";
      }
    } else {
      // let's reuse param expression
      int paramToReuse = getParameterNumToReuse(member);
      //System.err.println("paramToReuse: " + paramToReuse);
      if (paramToReuse >= 0) {
        BinExpression instanceUseExpression = ((BinMethodInvocationExpression)
        		inConstruct)
            .getExpressionList().getExpressions()[paramToReuse];
        instance = checkExpressionForInstance(instanceUseExpression, targetType);
  //      if (Assert.enabled && transList != fakeManager) {
  //        Assert.must(instance != null, "Got no instance on param reuse: "
  //            + instanceUseExpression);
  //      }

        if (instance != null) {
          String cast = "";
          ASTImpl instanceAST = instanceUseExpression.getRootAst();
          instanceAST = new CompoundASTImpl(instanceAST);

          if (instanceUseExpression instanceof BinCastExpression) {
            cast = ((BinCastExpression) instanceUseExpression)
                .getReturnType().getName();
          }

          transList.add(new StringEraser(data.getCompilationUnit(), instanceAST,
              true));

          String instancePreName = (instanceUseExpression instanceof BinArrayUseExpression) ?
              ((BinArrayUseExpression) instanceUseExpression).getText()
              : instance.getName();

          if (!(instance instanceof BinParameter)
              || !isReusable((BinParameter) instance)) {
            instanceName = (cast.length() > 0) ? "((" + cast + ")"
                + instancePreName + ")" : instancePreName;
          }

        }
      } else {
        if (!isLocationInFutureTarget(data.getWhereMember(), members,
            targetType)
            || data.getWhereMember().isStatic()) {
        	if (inConstruct instanceof BinMemberInvocationExpression) {
        		instance = new InstanceFinder().findInstance(data.getWhereMember(),
              (BinMemberInvocationExpression)inConstruct, targetType);
  //        if (Assert.enabled && manager != fakeManager) {
  //          Assert.must(instance != null, "Couldn't find an instance to call: "
  //              + expression + " on type: " + targetType);
  //        }
          // FIXME instance shouldn't be reusable
	          if (instance != null) {
	            instanceName = instance.getName();
	          }
        	}
        }
      }
      if (instanceName == null) {
        if (member instanceof BinField) {
          instanceName = "this";
        } else {
          instanceName = "";
        }
      }
    }

    //System.err.println("instance for member: " + member + ", instance: " +
    // instance
    //    + ", instanceName: " + instanceName);

    BinExpression inExpression = null;
    if (inConstruct instanceof BinMemberInvocationExpression) {
    	inExpression = ((BinMemberInvocationExpression)inConstruct).getExpression();

    }

    if (inExpression == null) { // was implicit this
      if (!"this".equals(instanceName) && instanceName != null
          && instanceName.length() > 0) {
        transList.add(new StringInserter(data.getCompilationUnit(),
        		data.getWhereAst().getStartLine(), data.getWhereAst().getStartColumn() - 1, instanceName + '.'));
      }

      if (mustAddNativeTypeAsParam) {
        String newParam;
        if (membersNeedToReceiveParam.contains(member)
            && members.contains(data.getWhereMember())) {
          newParam = (String) parametersName.get(data.getWhere());
        } else {
          newParam = "this";
        }

        transList.add(getEditorToInsertParameter(
            (BinMethodInvocationExpression) inConstruct, newParam, data
            .getCompilationUnit()));
      }

    } else if (!inExpression.getReturnType()
        .isDerivedFrom(targetType.getTypeRef())) {
      // rename
    	ASTImpl nameNode = ((BinMemberInvocationExpression)inConstruct).getNameAst();
      ASTImpl parentNode = nameNode.getParent();
      ASTImpl childOfParentNode = (ASTImpl) parentNode.getFirstChild();
      ASTImpl compoundNode = new CompoundASTImpl(childOfParentNode);

      if (childOfParentNode.getType() == JavaTokenTypes.TYPECAST) {
        Parentheses parentheses = new Parentheses(data, transList);
        parentheses.removeRightParentheses(compoundNode, parentNode);
        parentheses.removeLeftParentheses(childOfParentNode);
      }

      if (mustAddNativeTypeAsParam) {
        String content = data.getCompilationUnit().getContent();
        LineIndexer indexer = data.getCompilationUnit().getLineIndexer();

        String invokedOnExpression = content.substring(indexer.lineColToPos(
            compoundNode.getStartLine(), compoundNode.getStartColumn()),
            indexer.lineColToPos(compoundNode.getEndLine(), compoundNode
            .getEndColumn()));
        if ("super".equals(invokedOnExpression)) {
          invokedOnExpression = "this";
        }
        ASTImpl begin = ((BinMethodInvocationExpression) inConstruct)
            .getExpressionList().getRootAst();
        int existing = ((BinMethodInvocationExpression) inConstruct)
            .getExpressionList().getExpressions().length;
        if (getParameterNumToReuse(((BinMethodInvocationExpression) inConstruct)
            .getMethod()) >= 0) {
          --existing;
        }
        if (existing > 0) {
          invokedOnExpression += ", ";
        }
        transList.add(new StringInserter(data.getCompilationUnit(), begin
            .getStartLine(), begin.getStartColumn() - 1, invokedOnExpression));
        invocationGotParameter.putAll(data.getWhereMember(), member);
      }

      transList.add(new StringEraser(data.getCompilationUnit(), compoundNode,
          instanceName.length() == 0));
      if (instanceName.length() > 0) {
        transList.add(new StringInserter(data.getCompilationUnit(), compoundNode
            .getStartLine(), compoundNode.getStartColumn() - 1, instanceName));
      }
    } else {
      if (mustAddNativeTypeAsParam) {
        String newParam;
        if (membersNeedToReceiveParam.contains(member)
            && members.contains(data.getWhereMember())) {
          newParam = (String) parametersName.get(data.getWhere());
        } else {
          newParam = instanceName;
        }

        transList.add(getEditorToInsertParameter(
            (BinMethodInvocationExpression) inConstruct, newParam, data
            .getCompilationUnit()));
      }
    }

    return null;
  }


  private Editor getEditorToInsertParameter(
      BinMethodInvocationExpression expression, String parameter,
      CompilationUnit compilationUnit) {
    ASTImpl begin = expression.getExpressionList().getRootAst();
    int existing = expression.getExpressionList().getExpressions().length;

    if (getParameterNumToReuse(expression.getMethod()) >= 0) {
      --existing;
    }

    if (existing > 0) {
      parameter += ", ";
    }

    return new StringInserter(compilationUnit, begin.getStartLine(), begin
        .getStartColumn() - 1, parameter);
  }

  private class Parentheses {
    private int rightParenthesis = 0;
    private String content;
    private LineIndexer indexer;
    private CompilationUnit compilationUnit;
    private TransformationList transList;

    public Parentheses(InvocationData data, final TransformationList transList) {
      this.content = data.getCompilationUnit().getContent();
      this.indexer = data.getCompilationUnit().getLineIndexer();
      this.compilationUnit = data.getCompilationUnit();
      this.transList = transList;
    }

    public void removeRightParentheses(ASTImpl countFrom, ASTImpl countTo) {
      int startPos = indexer.lineColToPos(countFrom.getEndLine(), countFrom
          .getEndColumn() - 1);
      int endPos = indexer.lineColToPos(countTo.getStartLine(), countTo
          .getStartColumn());

      int index = startPos;
      boolean comment = false;
      while (index < endPos) {
        if (comment) {
          if (content.startsWith("*/", index)) {
            comment = false;
          }

          index++;
          continue;
        }

        if (content.startsWith("/*", index)) {
          comment = true;
          index++;
          continue;
        }

        if (content.startsWith(")", index)) {
          rightParenthesis++;
          transList.add(new StringEraser(compilationUnit, index, index + 1));
        }

        index++;
      }
    }

    public void removeLeftParentheses(ASTImpl countTo) {
      int startPos = indexer.lineColToPos(1, 1);
      int endPos = indexer.lineColToPos(countTo.getStartLine(), countTo
          .getStartColumn());
      int index = (endPos - 1);
      boolean comment = false;
      while ((rightParenthesis > 0) && (index > startPos)) {
        if (comment) {
          if (content.startsWith("/*", index)) {
            comment = false;
          }

          index--;
          continue;
        }

        if (content.startsWith("*/", index)) {
          comment = true;
          index--;
          continue;
        }

        if (content.startsWith("(", index)) {
          rightParenthesis--;
          transList.add(new StringEraser(compilationUnit, index, index + 1));
        }

        index--;
      }
    }
  }


  public static BinMember checkExpressionForInstance(
      final BinExpression expression, final BinCIType instanceOfType) {

    BinMember instance = getVariableFromExpression(expression);
    if (instance == null) {
      return null;
    }

    BinTypeRef instanceType = (expression instanceof BinCastExpression ||
        expression instanceof BinArrayUseExpression) ?
        expression.getReturnType():
        ((BinVariable) instance).getTypeRef();

    if (instanceType.isPrimitiveType()
        || !instanceType.isDerivedFrom(
        instanceOfType.getTypeRef())) {
      instance = null;
    }

    return instance;
  }

  private static BinMember getVariableFromExpression(BinExpression expression) {

      while (expression instanceof BinArrayUseExpression) {
        expression = ((BinArrayUseExpression) expression).getArrayExpression();
      }

    if (expression instanceof BinFieldInvocationExpression) {
      return ((BinFieldInvocationExpression) expression).getField();
    } else if (expression instanceof BinVariableUseExpression) {
      return ((BinVariableUseExpression) expression).getVariable();
    } else if (expression instanceof BinCastExpression) {
      return getVariableFromExpression(((BinCastExpression) expression)
          .getExpression());
    } else {
      return null;
    }
  }

  private void removeParameter(final BinMethod method,
      final TransformationList transList) {
    //System.err.println("removeParameter: " + method);
    int paramToRemove = getParameterNumToReuse(method);
    if (paramToRemove < 0) {
      return;
    }
    //System.err.println("num: " + paramToRemove);

    // parameter
    BinParameter param = method.getParameters()[paramToRemove];
    transList.add(new StringEraser(method.getCompilationUnit(), param
        .getRootAst(), true));

    // usages
    List usages = Finder.getInvocations(param);
    //System.err.println("usages: " + usages);
    for (int i = 0, max = usages.size(); i < max; i++) {
      InvocationData usage = (InvocationData) usages.get(i);
      BinVariableUseExpression expression = (BinVariableUseExpression) usage
          .getInConstruct();
      //System.err.println("param use: " + expression);

      boolean usageWillGoItself = false;
      if (expression.getParent() instanceof BinExpressionList) {
        BinExpressionList list = (BinExpressionList) expression.getParent();
        int usagePosition = 0;
        for (int m = 0; m < list.getExpressions().length; m++) {
          if (list.getExpressions()[m] == expression) {
            usagePosition = m;
            break;
          }
        }
        BinMethod inMethod = ((BinMethodInvocationExpression) list.getParent())
            .getMethod();
        if (isReusable(inMethod.getParameters()[usagePosition])) {
          usageWillGoItself = true;
        }
      }

      if (!usageWillGoItself) {
        boolean addThis = ((expression.getParent() instanceof
            BinFieldInvocationExpression) || replaceWithThis
            .contains(usage.getWhereAst()));
        transList.add(new StringEraser(method.getCompilationUnit(), usage
            .getWhereAst(), !addThis));
        if (addThis) {
          transList.add(new StringInserter(method.getCompilationUnit(), usage
              .getWhereAst().getStartLine(), usage.getWhereAst()
              .getStartColumn() - 1, "this"));
        }
      }
    }
  }

  private String checkIfParameterNameUnique(String parameterName,
      final BinMethod method, final BinCIType nativeType) {
    int count = 1;
    String resultName = parameterName;

    final List names = new ArrayList();
    class Visitor extends AbstractIndexer {
      public List localVars = new ArrayList();

      public void visit(BinLocalVariable x) {
        names.add(x.getName());
      }
    }


    Visitor visitor = new Visitor();
    method.accept(visitor);

    BinField fields[] = targetType.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      names.add(fields[i].getName());
    }

    if (!NameUtil.isValidIdentifier(resultName)) {
      resultName = (parameterName + Integer.toString(count++));
    }

    upper:while (true) {
      for (int i = 0, max = names.size(); i < max; i++) {
        String name = (String) names.get(i);
        if (resultName.equals(name)) {
          resultName = (parameterName + Integer.toString(count++));
          continue upper; // restart the whole process
        }
      }
      break;
    }

    return resultName;
  }

  private void addParameter(final BinMethod method,
      final TransformationList transList, final List usagesToUpdate) {
    final BinCIType nativeType = method.getOwner().getBinCIType();
    String parameterName = nativeType.getName();
    parameterName = StringUtil.decapitalizeFirstLetter(parameterName);
    parameterName = StringUtil.replace(parameterName, "$", "");
    parameterName = StringUtil.replace(parameterName, ".", ""); // just in case

    parameterName = checkIfParameterNameUnique(parameterName, method,
        nativeType);
    parametersName.put(method, parameterName);

    String parameterDef = nativeType.getName() + ' ' + parameterName;
    // TODO write complete test!!!
    BinParameter[] existingParams = method.getParameters();
    int existing = existingParams.length;
    if (getParameterNumToReuse(method) >= 0) {
      --existing;
    }
    if (existing > 0) {
      parameterDef += ", ";
    }

    String content = method.getCompilationUnit().getContent();
    LineIndexer indexer = method.getCompilationUnit().getLineIndexer();
    int pos = indexer.lineColToPos(method.getNameAstOrNull().getEndLine(),
        method.getNameAstOrNull().getEndColumn());
    while (Character.isWhitespace(content.charAt(pos))) {
      ++pos;
    }

    ++pos; // FIXME fails if there is a comment between method name and the
    // opening brace

    // parameter
    transList.add(new StringInserter(method.getCompilationUnit(), indexer
        .posToLineCol(pos - 1), parameterDef));

    // adds or renames reference in front of usage invocation
    for (int i = 0; i < usagesToUpdate.size(); i++) {
      BinMemberInvocationExpression expression = (BinMemberInvocationExpression)
          usagesToUpdate
          .get(i);
      if (expression.getExpression() == null) {
        transList.add(new StringInserter(method.getCompilationUnit(), expression
            .getNameAst().getStartLine(), expression.getNameAst()
            .getStartColumn() - 1, parameterName + '.'));
      } else {
        // rename
        ASTImpl node = expression.getNameAst();
        node = (ASTImpl) node.getParent().getFirstChild(); // infront of DOT
        node = new CompoundASTImpl(node);

        transList.add(new StringEraser(method.getCompilationUnit(), node, false));
        transList.add(new StringInserter(method.getCompilationUnit(), node
            .getStartLine(), node.getStartColumn(), parameterName));
      }
    }

    renameThisReference(method, parameterName, transList);
    this.extraTypesToImport.putNew(targetType, nativeType);
  }

  private void renameThisReference(BinMethod method, String parameterName,
      final TransformationList transList) {
    class Visitor extends AbstractIndexer {
      List thisAsts = new ArrayList();

      public void visit(BinLiteralExpression x) {
        if (x.isThis()
            && !(x.getParent() instanceof BinMethodInvocationExpression)
            && !(x.getParent() instanceof BinFieldInvocationExpression)) {
          thisAsts.add(x.getNameAst());
        }
        super.visit(x);
      }
    }


    Visitor visitor = new Visitor();
    method.accept(visitor);

    for (int i = 0, max = visitor.thisAsts.size(); i < max; i++) {
      ASTImpl thisAST = (ASTImpl) visitor.thisAsts.get(i);

      transList.add(new StringEraser(method.getCompilationUnit(), thisAST, false));
      transList.add(new StringInserter(method.getCompilationUnit(), thisAST
          .getStartLine(), thisAST.getStartColumn() - 1, parameterName));
    }
  }

  private int findParamToUseAndRemove(BinMember member) {
    //System.err.println("findParamToUseAndRemove: " + member);
    if (member instanceof BinField) {
      return -1;
    }

    if (member instanceof BinCIType) {
      return -1;
    }

    if (membersToBeStatic.contains(member)) {
      // we can't reuse any of the parameters
      return -1;
    }

    if (((BinMethod) member).findOverrides().size() > 0
        || member.getOwner().getBinCIType().getSubMethods((BinMethod) member)
        .size() > 0) {
      return -1;
    }

    BinParameter[] params = ((BinMethod) member).getParameters();
    boolean[] paramIndexesToReuse = new boolean[params.length];
    for (int i = 0; i < paramIndexesToReuse.length; i++) {
      if (params[i].getTypeRef().isPrimitiveType()) {
        paramIndexesToReuse[i] = false;
      } else {
        paramIndexesToReuse[i] = params[i].getTypeRef()
            .isDerivedFrom(targetType.getTypeRef());
      }
    }
    //System.err.println("params1: " +
    // StringUtil.toString(paramIndexesToReuse));

    List invocations = Finder.getInvocations(member);

    for (int i = 0, max = invocations.size(); i < max; i++) {
      if (!isThereUnmarkedParams(paramIndexesToReuse)) {
        break; // no more to check
      }

      InvocationData data = (InvocationData) invocations.get(i);
      BinMethodInvocationExpression expression = (BinMethodInvocationExpression)
          data
          .getInConstruct();

      BinExpression[] expressions = expression.getExpressionList()
          .getExpressions();
      for (int k = 0; k < expressions.length; k++) {
        if (!paramIndexesToReuse[k]) {
          continue;
        }

        if (expressions[k].isChangingAnything()) {
          // complex expression passed in as a parameter -
          // we couldn't predict results, so let's not reuse it
          paramIndexesToReuse[k] = false;
        }
      }
    }
    //System.err.println("params2: " +
    // StringUtil.toString(paramIndexesToReuse));

    // now analyze internal usage of the parameter
    for (int i = 0; i < params.length; i++) {
      if (!paramIndexesToReuse[i]) {
        continue; // already unmarked
      }

      invocations = Finder.getInvocations(params[i]);
      for (int k = 0, max = invocations.size(); k < max; k++) {
        InvocationData data = (InvocationData) invocations.get(k);
        BinItemVisitable parent = data.getInConstruct().getParent();
        if (parent instanceof BinMemberInvocationExpression) {
          BinMember memberCalled = ((BinMemberInvocationExpression) parent)
              .getMember();
          // can it be called with "this"?
          if (members.contains(memberCalled) // moving also
              || targetType.getTypeRef().isDerivedFrom(memberCalled.getOwner())) {
            continue;
          }
        } else if ((parent instanceof BinExpressionList)
            || (parent instanceof BinStringConcatenationExpression)
            || (parent instanceof BinLocalVariable)) {
          replaceWithThis.add(data.getWhereAst());
          continue; // it is just ok to either remove it or replace with "this"
        } else if (parent instanceof BinAssignmentExpression) {
          BinExpression leftExpression = ((BinAssignmentExpression) parent)
              .getLeftExpression();
          if (leftExpression instanceof BinVariableUseExpression) {
            if (((BinVariableUseExpression) leftExpression).getVariable()
                != params[i]) {
              replaceWithThis.add(data.getWhereAst());
              continue;
            }
          }
        }

        // called in expression in which can't be removed
        paramIndexesToReuse[i] = false;
      }
    }
    //System.err.println("params3: " +
    // StringUtil.toString(paramIndexesToReuse));

    int paramNum = -1;

    // most specific
    for (int i = 0; i < params.length; i++) {
      if (!paramIndexesToReuse[i]) {
        continue; // already unmarked
      }

      BinTypeRef targetTypeRef = targetType.getTypeRef();
      if (targetTypeRef != null && targetTypeRef.equals(params[i].getTypeRef())) {
        paramNum = i;
        break;
      }
    }

    //System.err.println("params4: " +
    // StringUtil.toString(paramIndexesToReuse));
    if (paramNum < 0) {
      // less specific - we can call given method on any subclass of target
      // class also
      for (int i = 0; i < params.length; i++) {
        if (!paramIndexesToReuse[i]) {
          continue; // already unmarked
        }

        if (params[i].getTypeRef().isDerivedFrom(targetType
            .getTypeRef())) {
          paramNum = i;
          break;
        }
      }
    }

    if (paramNum >= 0) {
      this.paramsToReuse.put(member, params[paramNum]);
    } else {
      this.paramsToReuse.remove(member);
    }

    return paramNum;
  }

  private boolean isThereUnmarkedParams(boolean[] paramIndexes) {
    for (int k = 0; k < paramIndexes.length; k++) {
      if (paramIndexes[k]) {
        return true;
      }
    }

    return false;
  }

  private BinParameter getParameterToReuse(BinMember member) {
    return (BinParameter)this.paramsToReuse.get(member);
  }

  private int getParameterNumToReuse(BinMember member) {
    BinParameter param = (BinParameter)this.paramsToReuse.get(member);
    if (param != null && member instanceof BinMethod) {
      BinParameter[] params = ((BinMethod) member).getParameters();
      for (int i = 0; i < params.length; i++) {
        if (params[i] == param) {
          return i;
        }
      }
    }

    return -1;
  }

  private boolean isReusable(BinParameter parameter) {
    if (parameter == null) {
      return false;
    }
    return getParameterToReuse(parameter.getParentMember()) == parameter;
  }

  public static final boolean isLocationInFutureTarget(
      final BinMember location, final List membersToMove,
      final BinCIType targetType) {

    // in target class?
    if (targetType == location.getOwner().getBinCIType()) {
      return true;
    }

    // in subclass of target?
    //System.err.println("loc: " + location + ", type: " +
    // InvocationData.getLocationType(location));
    if (location.getOwner().isDerivedFrom(targetType.getTypeRef())) {
      return true;
    }

    // in future members of target?
    for (int k = 0; k < membersToMove.size(); k++) {
      BinMember movedMember = (BinMember) membersToMove.get(k);
      if (movedMember.contains(location)) {
        return true;
      }
    }

    return false;
  }

  // 2
  boolean addNativeTypeAsParam(final BinMember member,
      final TransformationList transList, final List visitedMembers) {
    //    System.out.println("member in addNativeTypeAsParam1:" +
    // member.getName()); //innnnn
    if (membersNeedToReceiveParam.contains(member)) {
      return true;
    }


    //    System.out.println("member in addNativeTypeAsParam2:" +
    // member.getName()); //innnnn

    final BinCIType nativeType = member.getOwner().getBinCIType();

    // usages
    class Visitor extends AbstractIndexer {
      boolean isAddParameter = false;
      List thisUsagesToUpdate = new ArrayList();
      List superUsagesToUpdate = new ArrayList();
      List staticUsagesToUpdate = new ArrayList();

      public void visit(BinMethodInvocationExpression x) {
        checkMember(x);
        super.visit(x);
      }

      public void visit(BinFieldInvocationExpression x) {
        checkMember(x);
        super.visit(x);
      }

      private void checkMember(BinMemberInvocationExpression x) {
        //        List invokedMethods = invokedMethodsMap.get(member);
        //        if ((invokedMethods == null) ||
        // !invokedMethods.contains(x.getMember())) {
        if (x.invokedViaStaticImport()) {
        	staticUsagesToUpdate.add(x);
        }
      	if (visitedMembers.contains(x.getMember())) {
          return;
        } else {
          if (x.getMember() instanceof BinMethod) {
            invokedMethodsMap.putAll(member, x.getMember());
          }
        }

        //        System.out.println(member.getName() + " invokes1:" +
        // x.getMember().getName()); //innnnn

        if (targetType.isInnerType() && !targetType.isStatic()
            && (nativeType.getDeclaredType(targetType.getName()) != null)) {
          return;
        }

        if (nativeType.getTypeRef().isDerivedFrom(x.getMember().getOwner())
            && !targetType.getTypeRef().isDerivedFrom(nativeType.getTypeRef())
            && !members.contains(x.getMember()) && member != x.getMember()) {
          BinExpression parent = x.getExpression();

          //          System.out.println(member.getName() + " invokes2:" +
          // x.getMember().getName()); //innnnn

          if (x.getMember().isStatic()) {
            if (parent == null) {
              staticUsagesToUpdate.add(x);
            }
          } else {
            if (parent == null || parent instanceof BinLiteralExpression) {
              if (parent == null || ((BinLiteralExpression) parent).isThis()) {
                thisUsagesToUpdate.add(x);
              } else { // super invocation may cause troubles if member was
                // overriden in this
                if (x.getMember() instanceof BinField) {
                  if (nativeType.getDeclaredField(x.getMember().getName()) != null) {
                    superUsagesToUpdate.add(x);
                  } else { // no conflict, so we can safely call it through this
                    thisUsagesToUpdate.add(x);
                  }
                } else {
                  if (nativeType.getDeclaredMethod(x.getMember().getName(),
                      ((BinMethod) x.getMember()).getParameters()) != null) {
                    superUsagesToUpdate.add(x);
                  } else { // no conflict, so we can safely call it through this
                    thisUsagesToUpdate.add(x);
                  }
                }
              }
            }
          }
        } else if (members.contains(x.getMember())
            && ((member instanceof BinField) || !((member instanceof BinMethod) && ((BinMethod) member).isMain()))) {
          //          System.out.println(member.getName() + " invokes3:" +
          // x.getMember().getName()); //innnnn
          if (!isAddParameter) {
            isAddParameter = addNativeTypeAsParam(x.getMember(), transList,
                visitedMembers);
            //            System.out.println("is add parameter for "+x.getMember()+" ==
            // "+isAddParameter);
          }
        }
      }
    }


    visitedMembers.add(member);
    Visitor visitor = new Visitor();
    member.accept(visitor);

    List usages = new ArrayList(visitor.thisUsagesToUpdate);
    // NOTE here we assume that resolver checked for unresolvable conflicts with
    // override
    // Anyway, we can add only one expression as a parameter
    usages.addAll(visitor.superUsagesToUpdate);

    for (int i = 0; i < visitor.staticUsagesToUpdate.size(); i++) {
      BinMemberInvocationExpression expression = (BinMemberInvocationExpression)
          visitor.staticUsagesToUpdate
          .get(i);

      //If member was visited for the first time,
      //otherwise class name + DOT are already added
      if (!visitedMembersGlobal.contains(member)) {
        // add class name + DOT
        transList.add(new StringInserter(member.getCompilationUnit(),
            expression
            .getNameAst().getStartLine(), expression.getNameAst()
            .getStartColumn() - 1,
            expression.getMember().getOwner().getName() + '.'));
        this.extraTypesToImport.putNew(targetType, expression.getMember()
            .getOwner().getBinCIType());
      }
    }

    //must remember all visited members in order not to add class name + DOT
    //several times into one place
    visitedMembersGlobal.add(member);

    if (member instanceof BinMethod) {
    	if (!visitor.isAddParameter) {
    		visitor.isAddParameter = (usages.size() > 0)
				&& !canUseTargetRefFor((BinMethod) member,
						visitor.thisUsagesToUpdate, visitor.superUsagesToUpdate);
    	}

    	if (visitor.isAddParameter) {
    		membersNeedToReceiveParam.add(member);
    		addParameter((BinMethod) member, transList, usages);
    	} else {
    		replaceSuperReferenceWithThis((BinMethod) member, transList,
    				visitor.superUsagesToUpdate);
    		// todo: super usages updating
    	}

    	return visitor.isAddParameter;
    	//    return usages.size() > 0;
    } else {
    	return false;
    }
  }

  private void replaceSuperReferenceWithThis(BinMethod method,
      final TransformationList transList, List superUsages) {

    for (int index = 0; index < superUsages.size(); ++index) {
      BinMethodInvocationExpression x = (BinMethodInvocationExpression)
          superUsages
          .get(index);
      if (!(x.getExpression() instanceof BinLiteralExpression)) {
        Assert.must(false, "" + x.getExpression());
        continue;
      }

      final BinLiteralExpression expression = (BinLiteralExpression) x
          .getExpression();

      ASTImpl node = expression.getNameAst();
      //      node=(ASTImpl)node.getParent().getFirstChild(); // infront of DOT
      //      node=new CompoundASTImpl(node);

      transList.add(new StringEraser(method.getCompilationUnit(), node, true));

    }
  }

  private boolean canUseTargetRefFor(BinMethod meth, List thisUsg,
      List superUsg) {

    BinTypeRef targetRef = this.targetType.getTypeRef();

    if (thisUsg.isEmpty() && superUsg.isEmpty()) {
      return true;
    }

    // FIXME: hacks

    BinMemberInvocationExpression expr; //=(BinMemberInvocationExpression)thisAndSuperUsg.get(0);

    List thisAndSuperUsg = new ArrayList(thisUsg.size() + superUsg.size());
    thisAndSuperUsg.addAll(thisUsg);

    //System.out.println("method="+meth+", usg="+superUsg);

    thisAndSuperUsg.addAll(superUsg);

    for (int index = 0; index < thisAndSuperUsg.size(); ++index) {
      expr = (BinMemberInvocationExpression) thisAndSuperUsg.get(index);

      if (!expr.getMember().getOwner().isDerivedFrom(targetRef)) {
        return false;
      }

      // invariant;: targetRef is supertype for member

      if (expr.getMember() instanceof BinMethod) {
        BinMethod method = (BinMethod) expr.getMember();

        if (targetType.hasMemberWithSignature(method) == null) {
          return false;
        }

      } else {
        AppRegistry.getLogger(this.getClass()).debug("member invoced on BinField, case not supported:"
        + expr.getMember());
        return false;
      }

    }

    return true;
  }
}
