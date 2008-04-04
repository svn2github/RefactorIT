/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MissingBinMember;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinAnnotationExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.SourceCoordinate;

import java.util.Iterator;
import java.util.List;


public final class ItemByCoordinateFinder extends AbstractIndexer {
  // The source file into which to look
  private CompilationUnit source;

  // The BinItem that was discovered while visiting
  private BinItem foundItem;

  // The coordinate, node should be at
  private SourceCoordinate coordinate;

  private TypeRefVisitor refVisitor = new TypeRefVisitor();

  private class TypeRefVisitor extends BinTypeRefVisitor {
    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(true);
      setIncludeNewExpressions(false); // will handle with a hack, check visit(BinNew..)
    }

    public void visit(final BinTypeRef typeRef) {
      if (isMatch(typeRef.getNode())) {
        if (typeRef.getNonArrayType() != null) { // null happens sometimes after parsing warnings
          setFoundItem(typeRef.getNonArrayType().getBinType());
          return; // don't go deeper, already found
        }
      }

      super.visit(typeRef);
    }
  }

  public ItemByCoordinateFinder(CompilationUnit source) {
    super(true);
    this.source = source;
  }

  public BinItem findItemAt(SourceCoordinate coordinate) {
    this.coordinate = coordinate;

    // anything else we need to reset this?
    setFoundItem(null);

    // Launch visitor
    if ((this.source) != null) {
      visit(this.source);
    }

    if (!isFound()) {
      // FIXME: hack to get CreateMissingMethod working in editor
      if (this.source != null && this.source.getProject() != null) {
        final Iterator it = this.source.getProject().getProjectLoader()
            .getErrorCollector().getUserFriendlyErrors();
        while (it.hasNext()) {
          final Object item = it.next();
          if (item instanceof MethodNotFoundError) {
            final MethodNotFoundError error = (MethodNotFoundError) item;
            if (error.getCompilationUnit().equals(this.source)
                && error.getLine() == coordinate.getLine()) {
              return new MissingBinMember(error);
            }
          }
        }
      }
    }

    return this.foundItem;
  }

  private boolean checkPackageUsageInfos(List rpD) {
    if (rpD == null) {
      return false;
    }
    for (int i = 0, max = rpD.size(); i < max; ++i) {
      PackageUsageInfo d = (PackageUsageInfo) rpD.get(i);
      ASTImpl rootAST = d.getNode();
      ASTImpl asts[] = null;

      if (rootAST.getType() == JavaTokenTypes.DOT) {
        asts = LoadingASTUtil.extractIdentNodesFromDot(rootAST);
      } else {
        asts = new ASTImpl[] {rootAST};
      }

      Project project = d.getBinPackage().getProject();
      int packagePartCount = asts.length; //aPackage.getPartCount();

      String name = "";
      for (int p = 0; p < packagePartCount; ++p) {
        if (p > 0) {
          name += ".";
        }
        name += asts[p].getText();

        if (isMatch(asts[p])) {
          BinPackage pack = project.createPackageForName(name);
          if (pack != null) {
            setFoundItem(pack);
          }
          return true;
        }
      }

    }

    return false;
  }

  private boolean checkTypeRefs(BinTypeRefManager manager) {
    manager.accept(refVisitor);
    return isFound();
  }

  public void visit(BinCastExpression cast) {
    if (!checkTypeRefs(cast)) {
      super.visit(cast);
    }
  }

  public void visit(CompilationUnit source) {
    if (!checkTypeRefs(source)
        && !checkPackageUsageInfos(source.getPackageUsageInfos())) {
      super.visit(source);
    }
  }

  private void checkMethodName(BinMethod method) {
    if (isMatch(method.getNameAstOrNull())) {
      setFoundItem(method);
    }
  }

  private boolean isMatch(ASTImpl external) {
    // this get's called sometimes from
    // anonymous.getNameNode() etc
    if (external == null) {
      return false;
    }

    if (external.getSource() != this.source.getSource()) {
      return false;
    }

    // Compare nodes that reside on the same line
    if (coordinate.getLine() == external.getLine()) {
      return (coordinate.getColumn() >= external.getColumn() &&
          coordinate.getColumn()
          < external.getColumn() + external.getTextLength());
    }

    return false;
  }

  public void visit(BinCIType x) {
    if (!checkTypeRefs(x)) {
      super.visit(x);
    }
  }

  public void visit(BinCITypeExpression x) {
    if (!checkTypeRefs(x)) {
      super.visit(x);
    }
  }

  public void visit(BinNewExpression x) {
    if (!checkTypeRefs(x)) {
      ASTImpl ast = x.getRootAst();
      while (ast.getType() == JavaTokenTypes.LITERAL_new) {
        ast = (ASTImpl) ast.getFirstChild();
      }
      while (ast.getType() == JavaTokenTypes.DOT) {
        ast = (ASTImpl) ast.getFirstChild().getNextSibling();
      }

      if (ast.getType() != JavaTokenTypes.ENUM_CONSTANT_DEF) {
        if (isMatch(ast)) {
          if (x.getParent() instanceof BinThrowStatement) {
            // exclusion - too many people click on exception name in such clauses
            setFoundItem((BinItem) x.getParent());
          } else {
            BinTypeRef newType = x.getReturnType();
            // NOTE: clicking on anonymous needed for Override/Implement Method refactoring
            //          if (newType.getBinType().isAnonymous()) {
            //            setFoundItem(
            //                ((BinTypeRef) newType.getSupertypes().get(0)).getBinType());
            //          } else {
            BinItem obj = x.getConstructor();
            if (obj == null || ((BinConstructor) obj).isSynthetic()) {
              obj = newType.getNonArrayType().getBinType();
            }

            setFoundItem(obj);
            //          }
          }
        } else {
          checkTypeRefs(x);
        }
      }
    }

    if (!isFound()) {
      super.visit(x);
    }
  }

  public void visit(BinConstructorInvocationExpression x) {
    if (checkTypeRefs(x)) {
      return;
    }

    if (isMatch(x.getRootAst())) {
      BinItem obj = x.getConstructor();

      if (obj == null || ((BinConstructor) obj).isSynthetic()) {
        obj = x.getReturnType().getBinType();

        // FIXME: or leave it as Array and handle properly in modules?
        if (((BinType) obj).getTypeRef().isArray()) {
          obj = ((BinArrayType) obj).getArrayType().getBinType();
        }
      }

      setFoundItem(obj);
    }

    if (!isFound()) {
      super.visit(x);
    }
  }

  public void visit(BinThrowStatement x) {
    BinExpression expression = x.getExpression();
    if (expression != null) {
      if (isMatch(x.getRootAst())) {
        setFoundItem(x);
      }
    }

    if (!isFound()) {
      super.visit(x);
    }
  }

  public void visit(BinMethod method) {
    checkMethodName(method);

    if (!isFound()) {
      checkTypeRefs(method);
    }

    if (!isFound()) {
      super.visit(method);
    }
  }
  
  /* small hack: BinLabeledStatement is returned for where used model */
  public void visit(BinBreakStatement s) {
    ASTImpl label = (ASTImpl)s.getRootAst().getFirstChild();
    if(isMatch(label) && label.getType() == JavaTokenTypes.IDENT) {
      setFoundItem(s.getBreakTarget());
    }
    if(!isFound()) {
      super.visit(s);
    }
  }
  
  public void visit(BinLabeledStatement s) {
    ASTImpl label = (ASTImpl)s.getRootAst().getFirstChild();
    if(isMatch(label) && label.getType() == JavaTokenTypes.IDENT) {
      setFoundItem(s);
    }
    if(!isFound()) {
      super.visit(s);
    }
  }
  
  public void visit() {
    
  }

  public void visit(BinMethod.Throws x) {
    if (checkTypeRefs(x)) {
      return;
    }

    // FIXME: remove? //////////////
    ASTImpl ast = x.getRootAst();
    while (ast != null && ast.getType() == JavaTokenTypes.DOT) {
      ast = (ASTImpl) ast.getFirstChild().getNextSibling();
    }
    if (isMatch(ast)) {
      setFoundItem(x); // exception itself
    } else {
      checkTypeRefs(x); // prefixes, e.g. owners of inner exception
    }
    ////////////////////////////////

    if (!isFound()) {
      super.visit(x);
    }
  }

  public void visit(BinConstructor constructor) {
    if (isMatch(constructor.getNameAstOrNull())) {
      setFoundItem(constructor);
      return;
    }

    if (!checkTypeRefs(constructor)) {
      super.visit(constructor);
    }
  }

  public void visit(BinField field) {
    if (checkTypeRefs(field)) {
      return;
    }

    ASTImpl node = field.getNameAstOrNull();

    if (isMatch(node)) {
      setFoundItem(field);
      return;
    }

    super.visit(field);
  }

  public void visit(BinLocalVariable variable) {
    if (checkTypeRefs(variable)) {
      return;
    }

    ASTImpl node = variable.getNameAstOrNull();
    if (isMatch(node)) {
      setFoundItem(variable);
    } else {
      super.visit(variable);
    }
  }

  public void visit(BinMethodInvocationExpression expression) {
    if (checkTypeRefs(expression)) {
      return;
    }

    ASTImpl node = expression.getNameAst();

    if (isMatch(node)) {
      setFoundItem(expression);
    } else {
      super.visit(expression);
    }
  }

  public void visit(BinFieldInvocationExpression expression) {
    ASTImpl node = expression.getNameAst();

    if (isMatch(node)) {
      setFoundItem(expression);
    } else {
      super.visit(expression);
    }
  }

  public void visit(BinVariableUseExpression expression) {
    // Check parameter
    if (isMatch(expression.getNameAst())) {
      setFoundItem(expression.getVariable());
    }

    if (!isFound()) {
      super.visit(expression);
    }
  }

  public void visit(BinAnnotationExpression expression) {
    if (!checkTypeRefs(expression)) {
      super.visit(expression);
    }
  }

  private void setFoundItem(BinItem foundItem) {
    this.foundItem = foundItem;
  }

  private boolean isFound() {
    return this.foundItem != null;
  }

}
