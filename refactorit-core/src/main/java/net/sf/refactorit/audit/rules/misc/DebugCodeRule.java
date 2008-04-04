/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Villu Ruusmann
 */
public class DebugCodeRule extends AuditRule {
  public static final String NAME = "debug_code";

  /* Cache */
  private BinField systemOut;
  private BinField systemErr;

  /* Cache */
  private BinTypeRef throwableRef;

  public void visit(BinMethodInvocationExpression expression) {
    BinMethod method = expression.getMethod();

    // XXX:' What if the printStackTrace() is overriden?
    if (method.getName().equals("printStackTrace") &&
        expression.getInvokedOn().isDerivedFrom(getThrowableRef())) {
      BinExpression[] params = expression.getExpressionList().getExpressions();

      if (params.length == 0) {
        // Throwable#printStackTrace
        addViolation(new StackDumpToConsole(expression));
      } else if (params.length == 1 && isSystemPrintStreamUse(params[0])) {
        // Throwable#printStackTrace(PrintStream)
        addViolation(new StackDumpToConsole(expression));
      }
    }

    BinExpression left = expression.getExpression();

    if (left instanceof BinFieldInvocationExpression) {
      BinField field = ((BinFieldInvocationExpression) left).getField();

      if (field == getSystemOut()) {
        addViolation(new SystemOutInvocation(expression));
      } else if (field == getSystemErr()) {
        addViolation(new SystemErrInvocation(expression));
      }
    }

    super.visit(expression);
  }

  private boolean isSystemPrintStreamUse(BinExpression expression) {
    if (expression instanceof BinFieldInvocationExpression) {
      BinField field = ((BinFieldInvocationExpression) expression).getField();

      return (field == getSystemOut() || field == getSystemErr());
    }

    return false;
  }

  private BinTypeRef getSystemRef() {
    return getBinTypeRef("java.lang.System");
  }

  private BinField getSystemOut() {
    if (this.systemOut == null) {
      this.systemOut = getSystemRef().getBinCIType().getDeclaredField("out");
    }

    return this.systemOut;
  }

  private BinField getSystemErr() {
    if (this.systemErr == null) {
      this.systemErr = getSystemRef().getBinCIType().getDeclaredField("err");
    }

    return this.systemErr;
  }

  private BinTypeRef getThrowableRef() {
    if (this.throwableRef == null) {
      this.throwableRef = getBinTypeRef("java.lang.Throwable");
    }

    return this.throwableRef;
  }
}


class DebugCode extends AwkwardExpression {
  DebugCode(BinMethodInvocationExpression expression, String message) {
    super(expression, message, "refact.audit.debug_code");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  public BinMethodInvocationExpression getExpression() {
    return (BinMethodInvocationExpression) getSourceConstruct();
  }

  public List getCorrectiveActions() {
    List result = new ArrayList(2);
    result.add(CommentDebugCodeAction.instance);
    result.add(RemoveDebugCodeAction.instance);
    return result;
  }
}


class SystemOutInvocation extends DebugCode {
  SystemOutInvocation(BinMethodInvocationExpression expression) {
    super(expression, "Method " + expression.getMethod().getName()
        + " invoked on System.out");
  }
}


class SystemErrInvocation extends DebugCode {
  SystemErrInvocation(BinMethodInvocationExpression expression) {
    super(expression, "Method " + expression.getMethod().getName()
        + " invoked on System.err");
  }
}


class StackDumpToConsole extends DebugCode {
  StackDumpToConsole(BinMethodInvocationExpression expression) {
    super(expression, "Stack trace is dumped to console");
  }
}


class CommentDebugCodeAction extends MultiTargetCorrectiveAction {
  static final CommentDebugCodeAction instance = new CommentDebugCodeAction();

  public String getKey() {
    return "refactorit.audit.action.debug.comment";
  }

  public String getName() {
    return "Comment out debug code";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof DebugCode)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

//    ASTImpl ast = violation.getAst().getParent();


    BinMethodInvocationExpression invExprss = ((DebugCode)violation).getExpression();

    // statement
    BinStatement binSt = invExprss.getEnclosingStatement();

    // statement list
    BinStatementList stList = (BinStatementList) binSt.getParent();


    BinStatement temp;

    // comment every line, except first & last
    for (int x = binSt.getStartLine() + 1; x < binSt.getEndLine(); x++) {
      manager.add(new StringInserter(compilationUnit, x, 0, "//"));
    }


    // looking for statements on the same line after this Sys.out
    // if there is no, so temp will be = null
    temp = neighborAfter(binSt, stList.getStatements());
    if (temp != null) {
      if (binSt.getEndLine() != binSt.getStartLine()) {
        manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
            0, "//"));
      }
      manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
          binSt.getEndColumn(), FormatSettings.LINEBREAK));
    } else {
      if (binSt.getEndLine() == stList.getEndLine()) {
        if (binSt.getEndColumn() == stList.getEndColumn()) {
          if (binSt.getEndLine() != binSt.getStartLine()) {
            manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
                0, "//"));
          }
        } else {
          if (binSt.getEndLine() != binSt.getStartLine()) {
            manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
                0, "//"));
          }
          manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
              binSt.getEndColumn(), FormatSettings.LINEBREAK));
        }
      } else {
        if (binSt.getEndLine() != binSt.getStartLine()) {
          manager.add(new StringInserter(compilationUnit, binSt.getEndLine(),
              0, "//"));
        }
      }
    }

    temp = neighborBefore(binSt, stList.getStatements());

    if (temp != null) {
      manager.add(new StringInserter(compilationUnit, temp.getEndLine(),
          temp.getEndColumn(), FormatSettings.LINEBREAK + "//"));
    } else {
      if (binSt.getStartLine() == stList.getStartLine()) {
        if (binSt.getStartColumn() == stList.getStartColumn()) {
          manager.add(new StringInserter(compilationUnit, binSt.getStartLine(),
              binSt.getStartColumn()-1, ";" + FormatSettings.LINEBREAK +"//"));
        } else {
          manager.add(new StringInserter(compilationUnit, stList.getStartLine(),
              stList.getStartColumn(), FormatSettings.LINEBREAK + "//"));
        }
      } else {
        manager.add(new StringInserter(compilationUnit, binSt.getStartLine(),
            0, "//"));
      }
    }


//    int start = ast.getStartLine();

    return Collections.singleton(compilationUnit);
  }

  public static BinStatement neighborBefore(BinStatement bs, BinStatement[] bsl){
    BinStatement child;
    for(int x=0; x < bsl.length; x++) {
      child  = bsl[x];
      if(child!=bs) {
        if(bs.getStartLine()==child.getEndLine()){
          return child;
        }
      }
    }

    return null;
  }

  public static BinStatement neighborAfter(BinStatement bs, BinStatement[] bsl){
    BinStatement child;
    for(int x=0; x < bsl.length; x++) {
      child  = bsl[x];
      if(child!=bs) {
        if(bs.getEndLine()==child.getStartLine()){
          return child;
        }
      }
    }
    return null;
  }


}


class RemoveDebugCodeAction extends MultiTargetCorrectiveAction {
  static final RemoveDebugCodeAction instance = new RemoveDebugCodeAction();

  public String getKey() {
    return "refactorit.audit.action.debug.remove";
  }

  public String getName() {
    return "Remove debug code";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof DebugCode)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    ASTImpl ast = violation.getAst().getParent();
    StringEraser eraser = new StringEraser(compilationUnit, ast, true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    return Collections.singleton(compilationUnit);
  }
}
