/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;

import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class VariableUseAnalyzer extends FlowAnalyzer {

  private List allVariables;
  private List fqnTypes;
  private List notFqnTypes;

  boolean inSameStatement =  false;
  
  public VariableUseAnalyzer(
      IdeWindowContext context, BinMember rangeMember, List constructs
  ) {
    super(context, rangeMember, constructs);
    
  }

  public void initVars() {
    super.initVars();

    this.allVariables = new ArrayList();
    this.fqnTypes = new ArrayList();
    this.notFqnTypes = new ArrayList();
  }

  public BinLocalVariable[] getAllVariables() {
    return (BinLocalVariable[]) allVariables.toArray(
        new BinLocalVariable[allVariables.size()]);
  }

  public BinParameter[] getParameters() {
    List params = new ArrayList();
    for (int i = 0; i < allVariables.size(); i++) {
      BinLocalVariable var = (BinLocalVariable) allVariables.get(i);
      if (var.getClass() == BinParameter.class) {
        params.add(var);
      }
    }

    return (BinParameter[]) params.toArray(
        new BinParameter[params.size()]);
  }

  public BinLocalVariable[] getLocalVariables() {
    List localVars = new ArrayList();
    for (int i = 0; i < allVariables.size(); i++) {
      BinLocalVariable var = (BinLocalVariable) allVariables.get(i);
      if (var.getClass() == BinLocalVariable.class
          || var.getClass() == BinCatchParameter.class) {
        localVars.add(var);
      }
    }

    return (BinLocalVariable[]) localVars.toArray(
        new BinLocalVariable[localVars.size()]);
  }

  public List getFqnTypes() {
//System.err.println("************* NotFQN: " + this.notFqnTypes);
//System.err.println("************* FQN: " + this.fqnTypes);
    for (int i = 0, max = this.notFqnTypes.size(); i < max; i++) {
      final BinTypeRef typeRef = (BinTypeRef)this.notFqnTypes.get(i);
      this.fqnTypes.remove(typeRef);
    }

    this.notFqnTypes.clear();

    return this.fqnTypes;
  }

  public VarInfo getVarInfo(BinLocalVariable variable) {
    return (VarInfo) getTopBlock().getInfo(variable, VarInfo.class);
  }

  private VarInfo getCurrentVarInfo(BinLocalVariable variable) {
    final FlowAnalyzer.Flow flow = getCurrentFlow();
    VarInfo info = (VarInfo) flow.getInfo(variable, VarInfo.class);
    if (info != null) {
      info.variable = variable;
    }

    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Flow: " + flow + ", info: " + info);

    }
    return info;
  }

  protected void markUsage(BinLocalVariable var) {
    CollectionUtil.addNew(allVariables, var);
    final VarInfo info = getCurrentVarInfo(var);

    if (isReenterLoop()) {
      if (!info.declaredInside && !info.declaredInLocal) {
//      if (!isSelected((LocationAware) var.getScope())
//          && !isSelected(var.getWhereDeclared())) {
        info.usedAfter = true;

        if (isInside()) {
          // NOTE: expects that _use_ is marked BEFORE _change_ in, e.g. i++
          if (!info.changedInside) {
            info.changesBeforeUseInside = VarInfo.NO;
          }
        }

        if (!info.changedAfter) {
          info.changesBeforeUseAfter = VarInfo.NO;
        }
      }
    } else if (isBefore()) {
      info.usedBefore = true;
      if (isInSameStatement()) {
      	info.usedBeforeInSameStatement = true;
      }
    } else if (isInside()) {
      info.usedInside = true;

      // NOTE: expects that _use_ is marked BEFORE _change_ in, e.g. i++
      if (!info.changedInside) {
        info.changesBeforeUseInside = VarInfo.NO;
      }

      checkForFqn(var);
    } else if (isAfter()) {
      info.usedAfter = true;

      if (!info.changedAfter) {
        info.changesBeforeUseAfter = VarInfo.NO;
      }
    }

    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Usage: " + info);
    }
  }

  private boolean isInSameStatement() {
  	return inSameStatement;
  }
  
  private boolean isSelected(LocationAware construct) {
    for (Iterator it = this.constructs.iterator(); it.hasNext(); it.next()) {
      LocationAware la = (LocationAware) it.next();
      if (la.contains(construct)) {
        return true;
      }
    }

    return false;
  }

  protected void markChange(BinLocalVariable var) {
    CollectionUtil.addNew(allVariables, var);
    final VarInfo info = getCurrentVarInfo(var);

    if (isReenterLoop()) {
      if (!info.declaredInside && !info.declaredInLocal) {
//      if (!isSelected(var.getWhereDeclared())
//          && !isSelected((LocationAware) var.getScope())) {
        info.changedAfter = true;

        if (isInside()) {
          // NOTE: expects that _use_ is marked BEFORE _change_ in, e.g. i++
          if (!info.usedInside) {
            info.changesBeforeUseInside = VarInfo.YES;
          }
        }

        if (!info.usedAfter) {
          info.changesBeforeUseAfter = VarInfo.YES;
        }
      }
    } else if (isBefore()) {
      info.changedBefore = true;
      if (isInSameStatement()) {
      	info.changedBeforeInSameStatement = true;
      }
    } else if (isInside()) {
      info.changedInside = true;

      // NOTE: expects that _use_ is marked BEFORE _change_ in, e.g. i++
      if (!info.usedInside) {
        info.changesBeforeUseInside = VarInfo.YES;
      }

      checkForFqn(var);
    } else if (isAfter()) {
      info.changedAfter = true;

      if (!info.usedAfter) {
        info.changesBeforeUseAfter = VarInfo.YES;
      }
    }

    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Change: " + info);
//      new Exception(getCurrentFlow().toString()).printStackTrace(System.err);
    }
  }

  /**
   * We only track declaredInside
   */
  protected void markDeclared(BinLocalVariable var) {
    CollectionUtil.addNew(this.allVariables, var);
    VarInfo info = null;
    if (!isInLocal()) {
      info = getCurrentVarInfo(var);
      if (isInside()) {
        info.declaredInside = true;
      }
    } else {
      info = getCurrentVarInfo(var);
      info.declaredInLocal = true;
    }
    info.declaredInOuter = false;
    
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Declared: " + info);
    }
  }

  private void handleVariableUse(BinVariableUseExpression varUse) {
    BinItemVisitable parent = varUse.getParent();
    BinLocalVariable var = varUse.getVariable();

    if (parent instanceof BinIncDecExpression) {
    	markUsage(var);
    	markChange(var);
    } else if (parent instanceof BinAssignmentExpression) {
      if (((BinAssignmentExpression) parent).getLeftExpression() != varUse) {
        markUsage(var);
      } else {
        // caught in upper level
      }
    } else {
      markUsage(var);
    }
  }

  private void checkForFqn(BinVariable var) {
    BinTypeRef typeRef = var.getTypeRef();
    if (typeRef.isSpecific() && typeRef.isReferenceType()) {
      if (ImportUtils.isFqnUsage(typeRef.getCompilationUnit(), typeRef.getNode())) {
        CollectionUtil.addNew(fqnTypes, typeRef.getTypeRef());
      } else {
        CollectionUtil.addNew(notFqnTypes, typeRef.getTypeRef());
      }
    }
  }

  /**
   * onEnter will be called on visiting *before* starting to traverse children

   */
  public void onEnter(Object o) {
    super.onEnter(o);
   
    if (o instanceof BinStatement) {
    	BinStatement stmt = (BinStatement) o;
    	for (Iterator iter = constructs.iterator(); iter.hasNext();) {
    		Object next = iter.next();
    		if (next instanceof BinExpression) {
    			BinExpression element = (BinExpression) next ;
    			if (element.getEnclosingStatement() == stmt) {
    				inSameStatement = true;
    			}
    		}
    	}
    }
    
    
    if (o instanceof BinVariableUseExpression) {
      handleVariableUse((BinVariableUseExpression) o);
    } else if (o instanceof BinParameter) { // method parameters
      final BinParameter param = (BinParameter) o;
      markDeclared(param);
      markChange(param);
    } else if (o instanceof BinLocalVariable) {
      BinLocalVariable var = (BinLocalVariable) o;
      BinExpression initExpression = var.getExpression();
      markDeclared(var);
      if (initExpression != null) {
        markChange(var);
      }
    }

  }

  /**
   * onLeave will be called on visiting *after* starting to traverse children

   */
  public void onLeave(Object o) {
    if (o instanceof BinStatement) {
    	inSameStatement = false;
    }
  	
  	
    if (o instanceof BinAssignmentExpression) {
      BinAssignmentExpression assignExpr = (BinAssignmentExpression) o;
      if (assignExpr.leftIsArray()) {
        final BinArrayUseExpression arrayExp
            = (BinArrayUseExpression) assignExpr.getLeftExpression();
        final BinExpression anExpr = arrayExp.getArrayExpression();

        if (anExpr instanceof BinVariableUseExpression) {
          markUsage(((BinVariableUseExpression) anExpr).getVariable());
        }
      }
      if (assignExpr.leftIsVariable()) {
        final BinVariableUseExpression varUse
            = (BinVariableUseExpression) assignExpr.getLeftExpression();
        if (assignExpr.getAssignmentType() != JavaTokenTypes.ASSIGN) {
          markUsage(varUse.getVariable());
        }
        markChange(varUse.getVariable());
      }
    }
    super.onLeave(o);
  }

  public static class VarInfo extends FlowInfo {

    public static final int UNDEFINED = 0;
    public static final int NO = 1;
    public static final int YES = 2;

    /** To merge changeBeforeUseXXX in conditional statements */
    private static final int[][] CONDITIONAL_MERGE = {
        /*               UNDEFINED, NO, YES */
        /* UNDEFINED */ {UNDEFINED, NO, NO}
        ,
        /* NO        */{NO, NO, NO}
        ,
        /* YES       */{NO, NO, YES}
    };

    public BinLocalVariable variable;

    public boolean changedBefore = false;
    public boolean usedBefore = false;

    public int changesBeforeUseInside = UNDEFINED; // affects parameters
    public boolean changedInside = false;
    public boolean usedInside = false;
    public boolean declaredInside = false;
    public boolean declaredInLocal = false;
    public boolean declaredInOuter = true; // variable is declared in outer scope

    public int changesBeforeUseAfter = UNDEFINED; // affects return
    public boolean changedAfter = false;
    public boolean usedAfter = false;

    public boolean usedBeforeInSameStatement = false; 
    public boolean changedBeforeInSameStatement = false;
    
    
    public void mergeConditional(FlowInfo f, boolean exclusive) {
      VarInfo other = (VarInfo) f;
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.print("Conditional" + (exclusive ? "(exclusive)" : "")
            + ": " + this);
        System.err.print("With: " + other);
      }

      if (exclusive) {
        mergeExclusiveConditional(other);
      } else {
        mergeConcurrentConditional(other);
      }

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.print("After: " + this);
      }
    }

    /**
     * E.g. for, while loops are not exclusive, but concurrent, since they may
     * break at random places.
     */
    private void mergeConcurrentConditional(VarInfo other) {
      changedBefore |= other.changedBefore;
      usedBefore |= other.usedBefore;

      mergeConcurrentConditionalInside(other);

      mergeConcurrentConditionalAfter(other);

      this.setInside(this.isInside() | other.isInside());
      this.setAfter(this.isAfter() | other.isAfter());
    }

    private void mergeConcurrentConditionalInside(VarInfo other) {
      if (this.isInside() && other.isInside()) {
        changesBeforeUseInside
            = CONDITIONAL_MERGE[other.changesBeforeUseInside]
            [changesBeforeUseInside];
        /*        old version of !exclusive
                  if (changesBeforeUseInside == UNDEFINED) {
                    changesBeforeUseInside = other.changesBeforeUseInside;
                  }
                }*/
      } else if (!this.isInside() && other.isInside()) {
        changesBeforeUseInside = other.changesBeforeUseInside;
      }

      changedInside |= other.changedInside;
      usedInside |= other.usedInside;
      declaredInside |= other.declaredInside;
      declaredInLocal |= other.declaredInLocal;
      // FIXME: more complex merging needed?
      usedBeforeInSameStatement |= other.usedBeforeInSameStatement;
      changedBeforeInSameStatement |= other.changedBeforeInSameStatement;
    }

    private void mergeConcurrentConditionalAfter(VarInfo other) {
      if (this.isAfter() && other.isAfter()) {
        if (!this.isInside() && !other.isInside()) {
          changesBeforeUseAfter
              = CONDITIONAL_MERGE[other.changesBeforeUseAfter]
              [changesBeforeUseAfter];
        } else if (!this.isInside()) {
          if (changesBeforeUseAfter == UNDEFINED) {
            changesBeforeUseAfter = other.changesBeforeUseAfter;
          }
        }
      } else {
        changesBeforeUseAfter = UNDEFINED;
      }

      if (((this.declaredInside && other.declaredInside)
          || (this.declaredInLocal && other.declaredInLocal))
          && this.isInside() && other.isAfter() && other.isInside()
          && !this.isAfter()) {
        changedAfter = false;
        usedAfter = false;
      } else {
        changedAfter |= other.changedAfter;
        usedAfter |= other.usedAfter;
      }
    }

    private void mergeExclusiveConditional(VarInfo other) {
      if (this.isBefore() && !other.isBefore()) {
        changedBefore = false;
        usedBefore = false;
      }

      mergeExclusiveConditionalInside(other);

      mergeExclusiveConditionalAfter(other);

      this.setInside(this.isInside() | other.isInside());
      if (!other.isAfter() && !this.declaredInside && !this.declaredInLocal) {
        this.setAfter(false);
      }
    }

    private void mergeExclusiveConditionalInside(VarInfo other) {
      if (this.isInside() && other.isInside()) {
        changesBeforeUseInside
            = CONDITIONAL_MERGE[other.changesBeforeUseInside]
            [changesBeforeUseInside];
      } else if (!this.isInside() && other.isInside()) {
        changesBeforeUseInside = other.changesBeforeUseInside;
      }

      changedInside |= other.changedInside;
      usedInside |= other.usedInside;
      declaredInside |= other.declaredInside;
      declaredInLocal |= other.declaredInLocal;
      // FIXME: more complex merging needed?
      usedBeforeInSameStatement |= other.usedBeforeInSameStatement;
      changedBeforeInSameStatement |= other.changedBeforeInSameStatement;
      
    }

    private void mergeExclusiveConditionalAfter(VarInfo other) {
      if (this.isAfter() && other.isAfter()) {
        if (!this.isInside() && !other.isInside()) {
          changesBeforeUseAfter
              = CONDITIONAL_MERGE[other.changesBeforeUseAfter]
              [changesBeforeUseAfter];
        } else if (!this.isInside()) {
          if (changesBeforeUseAfter == UNDEFINED) {
            changesBeforeUseAfter = other.changesBeforeUseAfter;
          }
        }
      } else if (this.isAfter() && this.isInside() && !other.isAfter()) {
        // leave as is
      } else {
        changesBeforeUseAfter = UNDEFINED;
      }

      /*  this\other A+ A-
                A+   4  2
                A-   1  3 */
      if (!this.isAfter() && other.isAfter()) { // 1
        changedAfter = other.changedAfter;
        usedAfter = other.usedAfter;
      } else if (this.isAfter() && !other.isAfter()) { // 2
        // leave as is
      } else if (!this.isAfter() || !other.isAfter()) { // 3
        changedAfter = false;
        usedAfter = false;
      } else if (this.isAfter() && other.isAfter()) { // 4
        changedAfter |= other.changedAfter;
        usedAfter |= other.usedAfter;
      } else {
        // well, shouldn't get here
      }
    }

    public void mergeSequential(FlowInfo f) {
      VarInfo other = (VarInfo) f;
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.print("Sequential - Before: " + this);
        System.err.print("With: " + other);
      }

      changedBefore |= other.changedBefore;
      usedBefore |= other.usedBefore;

      if (changesBeforeUseInside == UNDEFINED) {
        changesBeforeUseInside = other.changesBeforeUseInside;
      }
      changedInside |= other.changedInside;
      usedInside |= other.usedInside;
      declaredInside |= other.declaredInside;
      declaredInLocal |= other.declaredInLocal;
      // FIXME: more complex merging needed?
      usedBeforeInSameStatement |= other.usedBeforeInSameStatement;
      changedBeforeInSameStatement |= other.changedBeforeInSameStatement;

      if (changesBeforeUseAfter == UNDEFINED) {
        changesBeforeUseAfter = other.changesBeforeUseAfter;
      }
      changedAfter |= other.changedAfter;
      usedAfter |= other.usedAfter;

      this.setInside(this.isInside() | other.isInside());
      this.setAfter(this.isAfter() | other.isAfter());

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.print("After: " + this);
      }
    }

    public FlowInfo getEmptyInfo(Flow flow) {
      VarInfo emptyInfo = new VarInfo();
      emptyInfo.variable = variable;
      emptyInfo.setInside(flow.inside);
      emptyInfo.setAfter(flow.after);

      return emptyInfo;
    }

    private String toString(int a) {
      switch (a) {
        case UNDEFINED:
          return "ud";
        case NO:
          return "no";
        case YES:
          return "yes";
        default:
          return "crazy";
      }
    }

    public String toString() {
      return (variable == null ? "null" : variable.getName()) + " " +
          (this.isInside() ? "I" : "") + (this.isAfter() ? "A" : "") + " [" +
          "before: " + (usedBefore ? "R" : "") + (changedBefore ? "W"
          : "") + ", " +
          "cbui: " + toString(changesBeforeUseInside) + ", " +
          "inside: " + (usedInside ? "R" : "") + (changedInside ? "W" : "")
          + (declaredInside ? "D" : "") + (declaredInLocal ? "L" : "") + ", " +
          "cbua: " + toString(changesBeforeUseAfter) + ", " +
          "after: " + (usedAfter ? "R" : "") + (changedAfter ? "W" : "") +
          "] \n";
    }
  }

}
