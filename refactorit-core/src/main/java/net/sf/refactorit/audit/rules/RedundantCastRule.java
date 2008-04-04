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
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinSynchronizedStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.MethodInvocationUtils;

import java.util.Collections;
import java.util.List;


/**
 * @author Villu Ruusmann
 * @author Jevgeni Holodkov
 * @author Arseni Grigorjev
 */
public class RedundantCastRule extends AuditRule {
  public static final String NAME = "redundant_cast";

  private boolean skipBitwisePrimitives = false;

  class CastContextAnalyzer extends BinItemVisitor{

    private BinCastExpression castExpr;
    private BinTypeRef sourceType;
    private BinTypeRef resultType;

    public CastContextAnalyzer(BinCastExpression castExpr,
        BinTypeRef sourceType, BinTypeRef resultType){

      this.castExpr = castExpr;
      this.sourceType = sourceType;
      this.resultType = resultType;
    }

    private void analyzeReferenceConversion() {
      if (TypeConversionRules.isSubtypingConversion(sourceType, resultType)){
        // code to avoid removing cast in case of so called 'integer division 
        // in floating point context'
        if (castExpr.getParent() instanceof BinArithmeticalExpression
            && ((BinArithmeticalExpression) castExpr.getParent()).getType() 
            == JavaTokenTypes.DIV
            && sourceType.isPrimitiveType()
            && ((BinPrimitiveType) sourceType.getBinType()).isIntegerType()
            && resultType.isPrimitiveType()
            && ((BinPrimitiveType) resultType.getBinType()).isFloatingPointType()){
          DivisionContextAnalyzer analyzer = new DivisionContextAnalyzer(
              (BinArithmeticalExpression) castExpr.getParent());
          if (analyzer.isFloatingPointContext()){
            addViolation(new RedundantCast(castExpr, sourceType, resultType));
          }
        } else {
          addViolation(new RedundantCast(castExpr, sourceType, resultType));
        }
      }
    }

    public void visit(BinExpressionList x){
      return;
    }

    public void visit(BinMethodInvocationExpression x){
      if (sourceType.isPrimitiveType()){
        // how could that happen?
        return;
      }

      BinMethod method = x.getMethod();
      BinCIType context = this.castExpr.getParentType();

      BinParameter[] params = method.getParameters();
      BinTypeRef[] newTypes = new BinTypeRef[params.length];

      for (int i = 0; i < params.length; i++){
        newTypes[i] = params[i].getTypeRef();
      }

      // find method for same parameters and different source type
      BinMethod newMethod
          = MethodInvocationRules.getMethodDeclaration(context,
          sourceType, method.getName(), newTypes);

      // if methods are equal then cast is redundant
      if (newMethod == method) {
        addViolation(new RedundantCast(castExpr, sourceType, resultType));
      }
    }

    public void visit(BinReturnStatement x){
      analyzeReferenceConversion();
    }

    public void visit(BinStringConcatenationExpression x){
      // means case: System.out.println("aa" + (char) byte + "aa"); -> it`s OK
      if (resultType == null || resultType.equals(BinPrimitiveType.CHAR_REF)){
        return;
      }

      // make sure after cast the same toString() method will be invoked
      if (sourceType.isReferenceType() && resultType.isReferenceType()){
        BinCIType context = this.castExpr.getParentType();

        BinMethod srcMethod
            = MethodInvocationRules.getMethodDeclaration(context,
            sourceType, "toString", BinTypeRef.NO_TYPEREFS);

        BinMethod resMethod
            = MethodInvocationRules.getMethodDeclaration(context,
            resultType, "toString", BinTypeRef.NO_TYPEREFS);

        if (srcMethod == resMethod){
          addViolation(new RedundantCast(castExpr, sourceType, resultType));
        }
      }

    }

    public void visit(BinAssignmentExpression x){
      if (!skipBitwisePrimitives || !isBitwiseOperation(x.getAssignmentType())){
        analyzeReferenceConversion();
      }
    }

    public void visit(BinLocalVariable x){
      analyzeReferenceConversion();
    }

    public void visit(BinLogicalExpression x){
      analyzeReferenceConversion();
    }
    
    public void visit(BinField x){
      analyzeReferenceConversion();
    }

    public void visit(BinArrayUseExpression x){
      // case with primitive types, will be caught by javac
    }

    public void visit(BinArrayInitExpression x){
      // case with primitive types, will be caught by javac
    }

    public void visit(BinArithmeticalExpression x){
      if (!skipBitwisePrimitives || !isBitwiseOperation(x.getType())){
        analyzeReferenceConversion();
      } 
    }

    private boolean isBitwiseOperation(final int type) {
      return type == JavaTokenTypes.BAND 
          || type == JavaTokenTypes.BOR
          || type == JavaTokenTypes.BNOT
          || type == JavaTokenTypes.BXOR
          || type == JavaTokenTypes.BSR
          || type == JavaTokenTypes.SR
          || type == JavaTokenTypes.SL
          || type == JavaTokenTypes.BOR_ASSIGN
          || type == JavaTokenTypes.BAND_ASSIGN
          || type == JavaTokenTypes.BSR_ASSIGN
          || type == JavaTokenTypes.BXOR_ASSIGN
          || type == JavaTokenTypes.SR_ASSIGN
          || type == JavaTokenTypes.SL_ASSIGN;
    }

    public void visit(BinFieldInvocationExpression x){
      // FIXME: this case should be treated too
    }

    public void visit(BinConditionalExpression x){
      // FIXME: this case should be treated too
    }

    public void visit(BinThrowStatement x){
      // FIXME: this case should be treated too
    }

    public void visit(BinCastExpression x) {
      return;
    }

    public void visit(BinExpression x) {
      return;
    }

    public void visit(BinExpressionStatement x) {
      return;
    }

    public void visit(BinForStatement x) {
      return;
    }

    public void visit(BinIfThenElseStatement x) {
      return;
    }

    public void visit(BinIncDecExpression x) {
      return;
    }

    public void visit(BinInstanceofExpression x) {
      return;
    }

    public void visit(BinStatement x) {
      return;
    }

    public void visit(BinSynchronizedStatement x) {
      return;
    }

    public void visit(BinUnaryExpression x) {
      return;
    }

    public void visit(BinWhileStatement x) {
      return;
    }
  }
  
  public void init(){
    super.init();
    this.skipBitwisePrimitives = AuditProfileUtils.getBooleanOption(
        getConfiguration(), "skip", "bitwise_primitives",
        skipBitwisePrimitives);
  }

  public void visit(BinCastExpression expression) {

    BinTypeRef sourceType = expression.getExpression().getReturnType();
    BinTypeRef resultType = expression.getReturnType();

    if (sourceType != null && sourceType.equals(resultType)){
      addViolation(new RedundantCast(expression, sourceType, resultType));
    } else {
      BinItemVisitable parent = expression.getParent();

      if (expression.getParent() instanceof BinExpressionList
          && !MethodInvocationUtils.confusesMethodResolution(expression,
          sourceType)){
        addViolation(new RedundantCast(expression, sourceType, resultType));
      } else {
        CastContextAnalyzer analyzer = new CastContextAnalyzer(
            expression, sourceType, resultType);
        parent.accept(analyzer);
      }
    }

    super.visit(expression);
  }
}

class RedundantCast extends SimpleViolation {

  RedundantCast(BinCastExpression expression, BinTypeRef sourceType,
      BinTypeRef resultType) {
    super(expression.getOwner(), expression.getTypeNode(),
        "Redundant cast from '" + ((sourceType != null)
        ? BinFormatter.format(sourceType) : "null type")
        + "' to '" + BinFormatter.format(resultType) + "'",
        "refact.audit.redundant_cast");
    setTargetItem(expression);
  }

  public BinMember getSpecificOwnerMember() {
    return ((BinSourceConstruct) getTargetItem()).getParentMember();
  }

  public BinCastExpression getCastExpression(){
    return (BinCastExpression) getTargetItem();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveRedundantCast.instance);
  }
}
