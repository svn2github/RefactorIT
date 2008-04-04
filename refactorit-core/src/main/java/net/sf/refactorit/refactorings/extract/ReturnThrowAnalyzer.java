/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



public class ReturnThrowAnalyzer extends FlowAnalyzer {

  private static final Object infoObject = new Object() {
    public String toString() {
      return "info";
    }
  };

  private ReturnThrowInfo topInfo = null;
  private List thrownExceptions = null;
  private List fqnExceptions;

  public static class ReturnThrowInfo extends FlowInfo {

    public static final int UNDEFINED = 0;
    public static final int PART_MIX = 1;
    public static final int PART_RET = 2;
    public static final int PART_THR = 3;
    public static final int MIXED = 4;
    public static final int RETURN = 5;
    public static final int THROWS = 6;

    private static final int[][] CONDITIONAL_MERGE = {
        /*               UNDEFINED, PART_MIX,  PART_RET,  PART_THR,  MIXED,     RETURN,    THROWS */
        /* UNDEFINED */ {UNDEFINED, PART_MIX, PART_RET, PART_THR, PART_MIX,
        PART_RET, PART_THR}
        ,
        /* PART_MIX  */{PART_MIX, PART_MIX, PART_MIX, PART_MIX, PART_MIX,
        PART_MIX, PART_MIX}
        ,
        /* PART_RET  */{PART_RET, PART_MIX, PART_RET, PART_MIX, PART_MIX,
        PART_RET, PART_MIX}
        ,
        /* PART_THR  */{PART_THR, PART_MIX, PART_MIX, PART_THR, PART_MIX,
        PART_MIX, PART_THR}
        ,
        /* MIXED     */{PART_MIX, PART_MIX, PART_MIX, PART_MIX, MIXED, MIXED,
        MIXED}
        ,
        /* RETURN    */{PART_RET, PART_MIX, PART_RET, PART_MIX, MIXED, RETURN,
        MIXED}
        ,
        /* THROWS    */{PART_THR, PART_MIX, PART_MIX, PART_THR, MIXED, MIXED,
        THROWS}
    };

    private static final int[][] SEQUENTIAL_MERGE = {
        /*               UNDEFINED, PART_MIX,  PART_RET,  PART_THR,  MIXED,     RETURN,    THROWS */
        /* UNDEFINED */ {UNDEFINED, PART_MIX, PART_RET, PART_THR, MIXED, RETURN,
        THROWS}
        ,
        /* PART_MIX  */{UNDEFINED, PART_MIX, PART_MIX, PART_MIX, MIXED, RETURN,
        THROWS}
        ,
        /* PART_RET  */{UNDEFINED, PART_MIX, PART_RET, PART_MIX, MIXED, RETURN,
        MIXED}
        ,
        /* PART_THR  */{UNDEFINED, PART_MIX, PART_MIX, PART_THR, MIXED, RETURN,
        THROWS}
        ,
        /* MIXED     */{UNDEFINED, PART_MIX, PART_MIX, PART_MIX, MIXED, MIXED,
        MIXED}
        ,
        /* RETURN    */{RETURN, PART_MIX, PART_RET, PART_MIX, MIXED, RETURN,
        MIXED}
        ,
        /* THROWS    */{THROWS, PART_MIX, PART_MIX, PART_THR, MIXED, MIXED,
        THROWS}
    };

    public int status = UNDEFINED;

    public BinTypeRef returnType = null;
    private List thrownExceptions = null;
    public BinTypeRef caughtException = null;

    public ReturnThrowInfo() {}

    public void addException(BinTypeRef exception, BinMember rangeMember) {
      boolean init = false;
      if (this.thrownExceptions == null) {
        this.thrownExceptions = new ArrayList(1);
        if (this.status == UNDEFINED) {
          init = true;
        }
      }

      if (init) {
        this.status = THROWS;
      } else {
        this.status = SEQUENTIAL_MERGE[this.status][THROWS];
      }

      if (isRuntime(exception)
          && !isRangeMemberThrows(rangeMember, exception)) {
        return;
      }

      CollectionUtil.addNew(thrownExceptions, exception);
    }

    private boolean isRuntime(BinTypeRef exception) {
      final BinTypeRef runtimeRef = exception.getProject()
          .getTypeRefForName("java.lang.RuntimeException");
      return exception.equals(runtimeRef) || exception.isDerivedFrom(runtimeRef);
    }

    private boolean isRangeMemberThrows(BinMember rangeMember,
        BinTypeRef exception) {
      BinMethod.Throws[] throwses = null;
      if (rangeMember instanceof BinMethod) {
        throwses = ((BinMethod) rangeMember).getThrows();
      } else if (rangeMember instanceof BinConstructor) {
        throwses = ((BinConstructor) rangeMember).getThrows();
      }

      if (throwses == null) {
        return true;
      }

      for (int i = 0; i < throwses.length; i++) {
        if (exception.equals(throwses[i].getException())
            || exception.isDerivedFrom(throwses[i].getException())) {
          return true;
        }
      }

      return false;
    }

    public List getThrownExceptions() {
      return thrownExceptions;
    }

    public void mergeConditional(FlowInfo f, boolean exclusive) {
      ReturnThrowInfo other = (ReturnThrowInfo) f;

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Conditional: " + this);
//System.err.println("YYYYYY: " + this.status);
        System.err.println("With: " + other);
      }

      if (this.isInside() && other.isInside()) {
        int oldExceptionsNum = this.thrownExceptions == null ? 0
            : this.thrownExceptions.size();

        if (other.caughtException != null && this.thrownExceptions != null) {
          Iterator it = this.thrownExceptions.iterator();
          while (it.hasNext()) {
            final BinTypeRef exception = (BinTypeRef) it.next();
            if (exception.equals(other.caughtException)
                || exception.isDerivedFrom(other.caughtException)) {
              it.remove();
            }
          }
        }

        if (other.thrownExceptions != null) {
          int preserveStatus = this.status;

          for (int i = 0, max = other.thrownExceptions.size(); i < max; i++) {
            final BinTypeRef exception
                = (BinTypeRef) other.thrownExceptions.get(i);
            addException(exception, null);
          }

          this.status = preserveStatus;
        }

//System.err.println("ZZZZ: " + oldExceptionsNum + " - "
//            + (this.thrownExceptions == null ? 0 : this.thrownExceptions.size())
//            + " - " + this.status);
        // caught all exceptions, so they are not affecting return
        if (oldExceptionsNum > 0 && this.thrownExceptions.size() == 0) {
          switch (this.status) {
            case MIXED:
              this.status = RETURN;
              break;
            case PART_MIX:
              this.status = PART_RET;
              break;
            case PART_THR:
            case THROWS:
              this.status = UNDEFINED;
              break;
            default:
              break;
          }
        }

//System.err.println("XXXXXX: " + this.status + " - " + other.status + " = " + CONDITIONAL_MERGE[this.status][other.status]);
        this.status = CONDITIONAL_MERGE[this.status][other.status];

        if (this.returnType == null) {
          this.returnType = other.returnType;
        }
      } else if (!this.isInside() && other.isInside()) {
        copy(other);
      }

      this.caughtException = null;

      this.setInside(this.isInside() | other.isInside());
      this.setAfter(this.isAfter() | other.isAfter());

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("After: " + this);
      }
    }

    public void mergeSequential(FlowInfo f) {
      ReturnThrowInfo other = (ReturnThrowInfo) f;
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Sequential: " + this);
        System.err.println("With: " + other);
      }

      if (this.isInside() && other.isInside()) {
        this.status = SEQUENTIAL_MERGE[this.status][other.status];

        if (other.thrownExceptions != null) {
          for (int i = 0, max = other.thrownExceptions.size(); i < max; i++) {
            final BinTypeRef exception
                = (BinTypeRef) other.thrownExceptions.get(i);
            addException(exception, null);
          }
        }

        if (this.returnType == null) {
          this.returnType = other.returnType;
        }
      } else if (!this.isInside() && other.isInside()) {
        copy(other);
      }

      if (this.caughtException == null) {
        this.caughtException = other.caughtException;
      }

      this.setInside(this.isInside() | other.isInside());
      this.setAfter(this.isAfter() | other.isAfter());

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("After: " + this);
      }
    }

    private void copy(FlowInfo f) {
      ReturnThrowInfo other = (ReturnThrowInfo) f;
      status = other.status;
      returnType = other.returnType;
      if (other.thrownExceptions != null) {
        thrownExceptions = (List) ((ArrayList) other.thrownExceptions).clone();
      } else {
        thrownExceptions = null;
      }
      caughtException = other.caughtException;
    }

    public Object clone() {
      try {
        // BUG: ??? WhereUsed for Object.clone() (interface) doesn't find this call
        ReturnThrowInfo newInfo = (ReturnThrowInfo)super.clone();
        if (this.thrownExceptions != null) {
          newInfo.thrownExceptions
              = (List) ((ArrayList)this.thrownExceptions).clone();
        }
        return newInfo;
      } catch (Exception e) {
        e.printStackTrace(System.err);
        return this;
      }
    }

    private String toString(int status) {
      switch (status) {
        case UNDEFINED:
          return "ud";
        case PART_MIX:
          return "part_mix";
        case PART_RET:
          return "part_ret";
        case PART_THR:
          return "part_thr";
        case MIXED:
          return "mix";
        case RETURN:
          return "ret";
        case THROWS:
          return "thr";
        default:
          return "crazy";
      }
    }

    public String toString() {
      return (this.isInside() ? "I" : "") + (this.isAfter() ? "A" : "")
          + " - " + toString(this.status)
          + ", return: " + (returnType == null ? "null" : returnType.getName())
          + ", throws: " + thrownExceptions
          + ", caught: "
          + (caughtException == null ? "null" : caughtException.getName());
    }

    public FlowInfo getEmptyInfo(Flow flow) {
      ReturnThrowInfo emptyInfo = new ReturnThrowInfo();
      emptyInfo.setInside(flow.inside);
      emptyInfo.setAfter(flow.after);

      return emptyInfo;
    }
  }


  public ReturnThrowAnalyzer(IdeWindowContext context, BinMember rangeMember, List constructs) {
    super(context, rangeMember, constructs);

    this.topInfo = getReturnInfo();
    if (topInfo != null) {
      this.thrownExceptions = topInfo.getThrownExceptions();
      if (this.thrownExceptions != null) {
        this.thrownExceptions = optimizeExceptions(this.thrownExceptions);
        Collections.sort(this.thrownExceptions, BinTypeRef.NameSorter.getInstance());
      }
    }

    if (this.thrownExceptions == null) {
      this.thrownExceptions = CollectionUtil.EMPTY_ARRAY_LIST;
    }
  }

  public void initVars() {
    super.initVars();

    this.fqnExceptions = new ArrayList();
  }

  public List getFqnTypes() {
    return this.fqnExceptions;
  }

  public ReturnThrowInfo getReturnInfo() {
    if (getTopBlock() != null) {
      return (ReturnThrowInfo) getTopBlock().getInfo(infoObject, null);
    }
    return null;
  }

  public BinTypeRef[] getThrownExceptions() {
    return (BinTypeRef[])this.thrownExceptions
        .toArray(new BinTypeRef[this.thrownExceptions.size()]);
  }

  private static List optimizeExceptions(List exceptions) {
    List exceptionsToRemove = new ArrayList();

    Iterator it = exceptions.iterator();
    while (it.hasNext()) {
      final BinTypeRef potentialCoarseException = (BinTypeRef) it.next();

      Iterator itInner = exceptions.iterator();
      while (itInner.hasNext()) {
        final BinTypeRef finerException = (BinTypeRef) itInner.next();
        if (!finerException.equals(potentialCoarseException) &&
            finerException.isDerivedFrom(potentialCoarseException)) {
          CollectionUtil.addNew(exceptionsToRemove, finerException);
        }
      }
    }

    for (int i = 0; i < exceptionsToRemove.size(); i++) {
      exceptions.remove(exceptionsToRemove.get(i));
    }

    return exceptions;
  }

  public void visit(BinThrowStatement throwStatement) {
    onEnter(throwStatement);

    if (isInside()) {
      final BinExpression expression = throwStatement.getExpression();
      final BinTypeRef exception = expression.getReturnType();

      ReturnThrowInfo info = getCurrentInfo();
      info.addException(exception, getRangeMember());
    }

    if (shouldVisitContentsOf(throwStatement)) {
      throwStatement.defaultTraverse(this);
    }

    onLeave(throwStatement);
  }

  public void visit(BinReturnStatement returnStatement) {
    onEnter(returnStatement);

    if (isInside()) {
      ReturnThrowInfo info = getCurrentInfo();
      info.status = ReturnThrowInfo.RETURN;
      if (returnStatement.getReturnExpression() != null) {
        info.returnType = returnStatement.getReturnExpression().getReturnType();
      } else {
        info.returnType = BinPrimitiveType.VOID_REF;
      }
    }

    if (shouldVisitContentsOf(returnStatement)) {
      returnStatement.defaultTraverse(this);
    }

    onLeave(returnStatement);
  }

  public void visit(BinMethodInvocationExpression expr) {
    if (isInside()) {
      checkThrows(expr.getMethod().getThrows());
    }

    super.visit(expr);
  }

  public void visit(BinConstructorInvocationExpression expr) {
    if (isInside()) {
      checkThrows(expr.getConstructor().getThrows());
    }

    super.visit(expr);
  }

  public void visit(BinNewExpression expr) {
    if (isInside()) {
      BinConstructor constructor = expr.getConstructor();
      if (constructor != null) {
        checkThrows(constructor.getThrows());

        final BinTypeRef throwable
            = getRangeMember().getCompilationUnit().getProject()
            .getTypeRefForName("java.lang.Throwable");
        if (expr.getReturnType().isReferenceType()
            && expr.getReturnType().isDerivedFrom(throwable)) {
          BinTypeRef data = expr.getTypeRef();
          if (ImportUtils.isFqnUsage(
              getRangeMember().getCompilationUnit(), data.getNode())) {
            CollectionUtil.addNew(this.fqnExceptions, data.getTypeRef());
          }
        }
      }
    }

    super.visit(expr);
  }

  private void checkThrows(final BinMethod.Throws[] throwses) {
    ReturnThrowInfo info = null;

    for (int i = 0; i < throwses.length; i++) {
      final BinTypeRef exception = throwses[i].getException();
      if (info == null) {
        info = getCurrentInfo();
      }
      info.addException(exception, getRangeMember());
    }
  }

  public void visit(BinTryStatement.CatchClause catchClause) {
    onEnter(catchClause);

    if (isInside()) {
      ReturnThrowInfo info = getCurrentInfo();
      info.caughtException
          = catchClause.getParameter().getTypeRef();
    }

    if (shouldVisitContentsOf(catchClause)) {
      catchClause.defaultTraverse(this);
    }

    onLeave(catchClause);
  }

  public void visit(BinCITypesDefStatement x) {
    // skip inner, it will never break outside code
  }

  private ReturnThrowInfo getCurrentInfo() {
    final Flow currentFlow = getCurrentFlow();
    ReturnThrowInfo info = (ReturnThrowInfo) currentFlow
        .getInfo(infoObject, ReturnThrowInfo.class);
    return info;
  }
}
