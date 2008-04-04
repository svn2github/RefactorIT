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
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinEmptyStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class FlowAnalyzer extends SinglePointVisitor {
  static int blockId;

  Flow topBlock;

  /** NOTE: don't use it directly, was open only for debug purposes */
  protected Flow currentFlow;

  protected Set constructs = new HashSet();

  private boolean reenterLoop;

  private static final BinEmptyStatement emptyStmt = new BinEmptyStatement(null);

  private BinMember rangeMember;
  private BinSourceConstruct loopCheckConstruct;

  /** When inside > 0 then we are inside watched section*/
  private int inside = -1;
  private int inLocal = 0;

  private boolean hasBeenInside;

  public abstract static class FlowInfo implements Cloneable {
    private boolean inside;
    private boolean after;

    public abstract void mergeConditional(FlowInfo otherInfo, boolean exclusive);

    public abstract void mergeSequential(FlowInfo otherInfo);

    public abstract FlowInfo getEmptyInfo(Flow flow);

    public Object clone() {
      try {
        return super.clone();
      } catch (Exception e) {
        e.printStackTrace(System.err);
        return this;
      }
    }

    public boolean isBefore() {
      return!inside && !after;
    }

    public boolean isInside() {
      return inside;
    }

    public boolean isAfter() {
      return after;
    }

    public void setInside(boolean inside) {
      this.inside = inside;
    }

    public void setAfter(boolean after) {
      this.after = after;
    }
  }


  public abstract static class Flow {
    /** for debug purposes to see which construct this flow corresponds to */
    protected Object debugItem = null;

    protected Flow parent = null;
    protected int id = -1;
    public List children = null;
    protected Map infos = new HashMap();

    public boolean inside = false;
    public boolean after = false;

    /** <code>true</code> - if, conditional, switch<br>
     *  <code>false</code> - for, do, while, try, labeled */
    public boolean exclusive = false;

    public Flow(Flow parent, Object item) {
      this(parent);
      this.debugItem = item;
    }

    public Flow(Flow parent) {
      this.parent = parent;
      if (this.parent != null) {
        if (this.parent.children == null) {
          this.parent.children = new ArrayList();
        }
        this.parent.children.add(this);
      }
      this.id = blockId++;
    }

    public Flow getParent() {
      return this.parent;
    }

    public void addInfo(Object object, FlowInfo info) {
      updateLocationFlags(info);
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Adding info: " + object + " - " + info);
      }
      Object previous = infos.put(object, info);
      if (Assert.enabled) {
        Assert.must(previous == null,
            "Adding duplicate info in flow " + id + " of " + object);
      }
    }

    public FlowInfo getDirectInfo(Object object) {
      return (FlowInfo) infos.get(object);
    }

    public FlowInfo getInfo(Object object, Class flowInfoType) {
      FlowInfo info = (FlowInfo) infos.get(object);
      if (info == null && flowInfoType != null) {
        try {
          info = (FlowInfo) flowInfoType.newInstance();
          addInfo(object, info);
        } catch (Exception e) {
          System.err.println("Failed to create new info of type: "
              + flowInfoType.getName());
        }
      }
      updateLocationFlags(info);

      return info;
    }

    private void updateLocationFlags(FlowInfo info) {
      if (info != null && this.inside) {
        info.setInside(true);
      }
      if (info != null && this.after) {
        info.setAfter(true);
      }
    }

    public abstract void mergeChildrenInfo();
  }


  protected static class FlowBlock extends Flow {

    public FlowBlock(Flow parent, Object item) {
      super(parent, item);
    }

    public FlowBlock(Flow parent) {
      super(parent);
    }

    public void mergeChildrenInfo() {
      if (children == null) {
        if (ExtractMethodAnalyzer.showDebugMessages) {
          System.err.println("<-> Block merge: " + this +", infos: " + infos);
        }
        return;
      }
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("--> Block merge: " + this +", infos: " + infos);

      }
      for (int i = 0, max = children.size(); i < max; i++) {
        final Flow child = (Flow) children.get(i);
        child.mergeChildrenInfo();

        Iterator it = child.infos.entrySet().iterator();
        while (it.hasNext()) {
          final Map.Entry entry = (Map.Entry) it.next();
          final Object object = entry.getKey();
          final FlowInfo childInfo = (FlowInfo) entry.getValue();

          FlowInfo info = (FlowInfo)this.infos.get(object);
          if (info != null) {
            info.mergeSequential(childInfo);
          } else {
            info = (FlowInfo) childInfo.clone();
            addInfo(object, info);
          }
        }
      }

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("<-- Block merge: " + this +", infos: " + infos);
      }
    }

    public String toString() {
      if (this.debugItem != null) {
        return "B" + this.id
            + (this.inside ? "I" : "") + (this.after ? "A" : "")
            + " (" + this.debugItem + ")";
      } else {
        return "B" + this.id
            + (this.inside ? "I" : "") + (this.after ? "A" : "");
      }
    }
  }


  protected static class FlowTransaction extends Flow {

    public FlowTransaction(Flow parent, boolean exclusive) {
      super(parent);
      this.exclusive = exclusive;
    }

    public FlowTransaction(Flow parent, boolean exclusive, Object item) {
      super(parent, item);
      this.exclusive = exclusive;
    }

    public void mergeChildrenInfo() {
      if (children == null) {
        if (ExtractMethodAnalyzer.showDebugMessages) {
          System.err.println("<-> Transaction merge: " + this +", infos: "
              + infos);
        }
        return;
      }
      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("--> Transaction merge: " + this +", infos: "
            + infos);

      }
      List objects = new ArrayList();

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Step 1");
        // first step: collect info objects
      }
      for (int i = 0, max = children.size(); i < max; i++) {
        final Flow child = (Flow) children.get(i);
        child.mergeChildrenInfo();

        Iterator it = child.infos.keySet().iterator();
        while (it.hasNext()) {
          CollectionUtil.addNew(objects, it.next());
        }
      }

      List needsMergingWithEmpty = new ArrayList();

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Step 2");
        // second step: merge objects infos
      }
      for (int i = 0, max = children.size(); i < max; i++) {
        final Flow child = (Flow) children.get(i);

        for (int k = 0, maxK = objects.size(); k < maxK; k++) {
          final Object object = objects.get(k);
          FlowInfo childInfo = child.getInfo(object, null);

          if (childInfo == null) {
            CollectionUtil.addNew(needsMergingWithEmpty, object);
            continue;
          }

          FlowInfo info = (FlowInfo)this.infos.get(object);
          if (info != null) {
            info.mergeConditional(childInfo, exclusive);
          } else {
            info = (FlowInfo) childInfo.clone();
            addInfo(object, info);
          }
        }
      }

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("Step 3");
        // third step: merge with empty ones when info was missing in some block
      }
      for (int i = 0, max = needsMergingWithEmpty.size(); i < max; i++) {
        final FlowInfo info = (FlowInfo) infos.get(needsMergingWithEmpty.get(i));
        final FlowInfo emptyInfo = info.getEmptyInfo(this);
        info.mergeConditional(emptyInfo, exclusive);
      }

      if (ExtractMethodAnalyzer.showDebugMessages) {
        System.err.println("<-- Transaction merge: " + this +", infos: "
            + infos);
      }
    }

    public String toString() {
      if (this.debugItem != null) {
        return "T" + (this.exclusive ? "E" : "") + this.id
            + (this.inside ? "I" : "") + (this.after ? "A" : "")
            + " (" + this.debugItem + ")";
      } else {
        return "T" + (this.exclusive ? "E" : "") + this.id
            + (this.inside ? "I" : "") + (this.after ? "A" : "");
      }
    }
  }


  /**
   * @param rangeMember rangeMember
   * @param constructs list of {@link BinSourceConstruct constructs} to extract
   */
  public FlowAnalyzer(
      IdeWindowContext context, BinMember rangeMember, List constructs
  ) {
    initVars();

    if (Assert.enabled) {
      Assert.must(rangeMember != null, "Range member is null!");
    }
    if (rangeMember == null) {
      return;
    }

    if (constructs != null) {
      this.constructs.addAll(constructs);
    }

    for (int i = 0; i < constructs.size(); i++) {
      final Object object = constructs.get(i);
      if (object instanceof BinSourceConstruct) {
        this.loopCheckConstruct = (BinSourceConstruct) object;
        break;
      }
    }
    this.rangeMember = rangeMember;

    blockId = 0; // not perfect, but it is for debug only
    this.inside = 0;
    this.hasBeenInside = false;
    this.topBlock = startFlowBlock("Top");
    accept(this.rangeMember, this);
    endFlow();

    FlowDebug debug = null;
    if (ExtractMethodAnalyzer.showDebugMessages
        && this instanceof VariableUseAnalyzer) {
      debug = new FlowDebug(context, (VariableUseAnalyzer) this);
    }

    this.topBlock.mergeChildrenInfo();

    if (ExtractMethodAnalyzer.showDebugMessages
        && this instanceof VariableUseAnalyzer) {
      debug.show();
    }

  }

  /** To be overriden */
  public void initVars() {
  }

  protected BinMember getRangeMember() {
    return this.rangeMember;
  }

  protected boolean isReenterLoop() {
    return reenterLoop;
  }

  protected void setReenterLoop(boolean reenterLoop) {
    this.reenterLoop = reenterLoop;
  }

  protected boolean isBefore() {
    return!isInside() && !isAfter() && !hasBeenInside() && !isInLocal()
        && !isReenterLoop();
  }

  protected boolean isInside() {
    final boolean isinside = this.inside > 0;
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("isInside: " + currentFlow + ", inside: " + isinside);
    }
    return isinside;
  }

  protected boolean isAfter() {
    boolean isafter = hasBeenInside && this.inside == 0;
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("isAfter: " + currentFlow + ", after: " + isafter
          + "(inside: " + this.inside + ", hasBeenInside: " + hasBeenInside
          + ")");
    }
    return isafter;
  }

  protected boolean hasBeenInside() {
    return this.hasBeenInside;
  }

  protected boolean isInLocal() {
    return inLocal > 0;
  }

  protected Flow getCurrentFlow() {
    return currentFlow;
  }

  protected void setCurrentFlow(Flow currentFlow) {
    this.currentFlow = currentFlow;
  }

  /** for debug only */
  public Flow getTopBlock() {
    return this.topBlock;
  }

  public Flow startFlowTransaction(boolean exclusive, Object item) {
    currentFlow = new FlowTransaction(currentFlow, exclusive, item);
    checkInsideAfterFlags();
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Starting transaction: " + currentFlow);
    }
    return currentFlow;
  }

  public Flow startFlowTransaction(boolean exclusive) {
    currentFlow = new FlowTransaction(currentFlow, exclusive);
    checkInsideAfterFlags();
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Starting transaction: " + currentFlow);
    }
    return currentFlow;
  }

  public Flow startFlowBlock() {
    currentFlow = new FlowBlock(currentFlow);
    checkInsideAfterFlags();
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Starting block: " + currentFlow);
    }
    return currentFlow;
  }

  public Flow startFlowBlock(Object item) {
    currentFlow = new FlowBlock(currentFlow, item);
    checkInsideAfterFlags();
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Starting block: " + currentFlow);
    }
    return currentFlow;
  }

  public void onEnter(Object object) {
    if (constructs.contains(object)) {
      if (this.inside < 0) {
        this.inside = 0;
      }
      this.inside++;
//      if (ExtractMethodAnalyzer.showDebugMessages)
//        System.err.println("onEnter " + object + " inside: " + inside);
      hasBeenInside = true;
    }

    if (object instanceof BinStatement
        && !(object instanceof BinStatementList)) {
      startFlowBlock(object);
    }
  }

  public void onLeave(Object object) {
    if (object instanceof BinStatement
        && !(object instanceof BinStatementList)) {
      endFlow();
    }

    if (constructs.contains(object)) {
      this.inside--;
//      if (ExtractMethodAnalyzer.showDebugMessages)
//        System.err.println("onLeave " + object + " inside: " + inside);
    }
  }

  public void visit(BinCITypesDefStatement typeDef) {
    if (isInside()) {
      inLocal++;
    }
    super.visit(typeDef);
    if (isInside()) {
      inLocal--;
    }
  }

  private void checkInsideAfterFlags() {
    if (isInside()) {
      currentFlow.inside = true;
    }
    if (isAfter()) {
      currentFlow.after = true;
    }
  }

  public Flow endFlow() {
    Flow oldFlow = currentFlow;
    if (currentFlow != null) {
      currentFlow = currentFlow.getParent();
    }
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Ending "
          + (oldFlow instanceof FlowTransaction
          ? "transaction" : "block") + ": " + oldFlow);
    }

    return oldFlow;
  }

  public Flow endFlowBlock() {
    return endFlow();
  }

  public Flow endFlowTransaction() {
    return endFlow();
  }

  public void visit(BinConstructor ctr) {
    if (!ctr.isSynthetic()) {
      super.visit(ctr);
    }
  }

  public void visit(BinIfThenElseStatement ifStmt) {
    onEnter(ifStmt);

    accept(ifStmt.getCondition(), this);

    startFlowTransaction(true, ifStmt);
    BinItem child;

    child = ifStmt.getTrueList();
    startFlowBlock((child == null ? emptyStmt : child));
    accept(child, this);
    endFlow();

    child = ifStmt.getFalseList();
    startFlowBlock((child == null ? emptyStmt : child));
    accept(child, this);
    endFlowBlock();

    endFlowTransaction();

    onLeave(ifStmt);
  }

  public void visit(BinConditionalExpression condExpr) {
    onEnter(condExpr);

    startFlowBlock(condExpr.getCondition());
    accept(condExpr.getCondition(), this);
    endFlowBlock();

    startFlowTransaction(true, condExpr);

    startFlowBlock(condExpr.getTrueExpression());
    accept(condExpr.getTrueExpression(), this);
    endFlowBlock();

    startFlowBlock(condExpr.getFalseExpression());
    accept(condExpr.getFalseExpression(), this);
    endFlowBlock();

    endFlowTransaction();
    onLeave(condExpr);
  }

  public void visit(BinSwitchStatement switchStmt) {
    onEnter(switchStmt);

    accept(switchStmt.getCondition(), this);

    boolean hasDefault = false;

    // N.B! Case blocks can contain usages of constant variables, where
    // the actual 'usage' happens at evaluation of condition
    // will visit them 'outside transaction'
    BinSwitchStatement.CaseGroup list[] = switchStmt.getCaseGroupList();
    for (int i = 0; i < list.length; ++i) {
      BinSwitchStatement.Case caseList[] = list[i].getCaseList();
      for (int c = 0; c < caseList.length; ++c) {
        accept(caseList[c], this);
        if (caseList[c].getExpression() == null) {
          hasDefault = true;
        }
      }
    }

    startFlowTransaction(true, switchStmt);

    boolean didBreak = true;
    boolean closedLast = false;
    for (int i = 0; i < list.length; ++i) {
      boolean willBreak = willBreakOutside(list[i].getStatementList())
          || (i == list.length - 1);
      //if (ExtractMethodAnalyzer.showDebugMessages)
      //  System.err.println( "Will Break = " + willBreak + " @ " + list[i].getStatementList().getStartLine());
      if (didBreak) {
        startFlowBlock(list[i]);
      }
      accept(list[i].getStatementList(), this);
      if (willBreak) {
        endFlowBlock();
        closedLast = true;
      } else {
        closedLast = false;
      }
      didBreak = willBreak;
    }
    if (!closedLast) {
      endFlow();

    }
    if (!hasDefault) {
      startFlowBlock(emptyStmt);
      // empty block in case it didn't called any of cases
      endFlowBlock();
    }

    endFlowTransaction();

    onLeave(switchStmt);
  }

  /**
   * returns true if clause has either return or break, that breaks
   * out of it (as opposed to having a break, that breaks from while statement
   * for example
   */
  private boolean willBreakOutside(BinStatementList stmtList) {
    final boolean willBreak[] = new boolean[] {false};

    BinItemVisitor searcher = new BinItemVisitor() {

      Set targets = new HashSet();
      int returnLevel = 0;

      public void visit(BinForStatement forStmt) {
        targets.add(forStmt);
        super.visit(forStmt);
      }

      public void visit(BinWhileStatement whileStmt) {
        targets.add(whileStmt);
        super.visit(whileStmt);
      }

      public void visit(BinLabeledStatement labeledStmt) {
        targets.add(labeledStmt);
        super.visit(labeledStmt);
      }

      public void visit(BinSwitchStatement switchStmt) {
        targets.add(switchStmt);
        super.visit(switchStmt);
      }

      public void visit(BinConstructor ctr) {
        ++returnLevel;
        super.visit(ctr);
        --returnLevel;
      }

      public void visit(BinMethod method) {
        ++returnLevel;
        super.visit(method);
        --returnLevel;
      }

      public void visit(BinReturnStatement returnStatement) {
        if (returnLevel == 0) {
          willBreak[0] = true;
        } else {
          super.visit(returnStatement);
        }
      }

      public void visit(BinBreakStatement brkStmt) {
        if (!targets.contains(brkStmt.getBreakTarget())) {
          willBreak[0] = true;
        } else {
          super.visit(brkStmt);
        }
      }
    };

    accept(stmtList, searcher);

    return willBreak[0];
  }

  public void visit(BinTryStatement tryStmt) {
    onEnter(tryStmt);

    startFlowTransaction(false, tryStmt);

    startFlowBlock(tryStmt.getTryBlock());
    accept(tryStmt.getTryBlock(), this);
    endFlowBlock();

    BinTryStatement.CatchClause catches[] = tryStmt.getCatches();
    for (int i = 0; i < catches.length; ++i) {
      startFlowBlock(catches[i]);
      accept(catches[i], this);
      endFlowBlock();
    }

    if (catches.length == 0) {
      startFlowBlock(emptyStmt);
      // empty block to be merged with main block
      endFlowBlock();
    }

    endFlowTransaction();

    accept(tryStmt.getFinally(), this);

    onLeave(tryStmt);
  }

  public void visit(BinLabeledStatement labeledStmt) {
    onEnter(labeledStmt);

    boolean mayBreak = willBreakOutside(labeledStmt.getLabelStatementList());

    if (mayBreak) {
      startFlowTransaction(false, labeledStmt);
    }

    startFlowBlock(labeledStmt);
    labeledStmt.defaultTraverse(this);
    endFlowBlock();

    if (mayBreak) {
      startFlowBlock(emptyStmt);
      // empty block for the case statement was exited unexpectedly
      endFlowBlock();

      endFlowTransaction();
    }
    onLeave(labeledStmt);
  }

  public void visit(BinLogicalExpression logicalExpression) {
    onEnter(logicalExpression);

    if (logicalExpression.getAssigmentType() == JavaTokenTypes.LAND
        || logicalExpression.getAssigmentType() == JavaTokenTypes.LOR) {

      boolean isParentConditional = getCurrentFlow() instanceof FlowTransaction;

      if (isParentConditional) {
        startFlowTransaction(false, logicalExpression);

        startFlowBlock(logicalExpression.getLeftExpression());
        accept(logicalExpression.getLeftExpression(), this);
        endFlowBlock();

        startFlowBlock(logicalExpression.getRightExpression());
        accept(logicalExpression.getRightExpression(), this);
        endFlowBlock();

        endFlowTransaction();
      } else {
        accept(logicalExpression.getLeftExpression(), this);

        startFlowTransaction(false, logicalExpression.getRightExpression());

        startFlowBlock(logicalExpression.getRightExpression());
        accept(logicalExpression.getRightExpression(), this);
        endFlowBlock();

        startFlowBlock(emptyStmt);
        // empty block in case when left expression evaluation lead to exit
        endFlowBlock();

        endFlowTransaction();
      }
    } else {
      logicalExpression.defaultTraverse(this);
    }

    onLeave(logicalExpression);
  }

  public void visit(BinForStatement forStmt) {
    onEnter(forStmt);

    final boolean mayBreak = willBreakOutside(forStmt.getStatementList());
    final boolean savedReenterFlag = isReenterLoop();
    final boolean reenterable = isLoopReenterable(forStmt);

    accept(forStmt.getInitSourceConstruct(), this);

    if (reenterable && !savedReenterFlag) {
      startFlowTransaction(false, "ForTransaction");
      startFlowBlock(forStmt); // main block
    }

    visitFor(forStmt, mayBreak);

    if (reenterable && !savedReenterFlag) {
      endFlowBlock(); // main block
      startFlowBlock("for reenter"); // reenter block

      setReenterLoop(reenterable);

      visitFor(forStmt, mayBreak);

      setReenterLoop(savedReenterFlag);

      endFlowBlock(); // reenter block
      endFlowTransaction();
    }

    onLeave(forStmt);
  }

  private void visitFor(final BinForStatement forStmt, final boolean mayBreak) {

    accept(forStmt.getCondition(), this); // condition is always visited

    if (mayBreak) {
      startFlowTransaction(false, "ForMayBreak");
    }

    final BinStatementList slist = forStmt.getStatementList();
    startFlowBlock(slist);
    accept(slist, this);
    endFlowBlock();

    startFlowBlock(forStmt.iteratorExpressionList());
    accept(forStmt.iteratorExpressionList(), this);
    endFlowBlock();

    if (mayBreak) {
      endFlowTransaction();
    }
  }

  private void accept(BinItem item, BinItemVisitor visitor) {
    if (item != null) {
      item.accept(visitor);
    }
  }

  private boolean isLoopReenterable(BinSourceConstruct loop) {
    final boolean reenter = isReenterLoop()
        || (!isInside() && loop.contains(loopCheckConstruct));
    return reenter;
  }

  public void visit(BinWhileStatement whileStmt) {
    onEnter(whileStmt);

    final boolean mayBreak = willBreakOutside(whileStmt.getStatementList());
    final boolean savedReenterFlag = isReenterLoop();
    final boolean reenterable = isLoopReenterable(whileStmt);

    if (reenterable && !savedReenterFlag) {
      startFlowTransaction(false, "WhileTransaction");
      startFlowBlock(whileStmt); // main block
    }

    visitDoWhile(whileStmt, mayBreak);

    if (reenterable && !savedReenterFlag) {
      endFlowBlock(); // main block
      startFlowBlock("while reenter"); // reenter block

      setReenterLoop(reenterable);

      visitDoWhile(whileStmt, mayBreak);

      setReenterLoop(savedReenterFlag);

      endFlowBlock();
      endFlowTransaction();
    }

    onLeave(whileStmt);
  }

  private void visitDoWhile(final BinWhileStatement whileStmt,
      final boolean mayBreak) {
    final BinStatementList statementList = whileStmt.getStatementList();
    final BinExpression condition = whileStmt.getCondition();

    if (whileStmt.isDoWhile()) {
      if (mayBreak) {
        startFlowTransaction(false, whileStmt);
      } else {
        startFlowBlock(whileStmt);
      }
      startFlowBlock(statementList);
      accept(statementList, this);
      endFlow();

      startFlowBlock(condition);
      accept(condition, this);
      endFlowBlock();
      endFlowTransaction();
    } else {
      accept(condition, this); // condition is always visited at least once here

      if (mayBreak) {
        startFlowTransaction(false, statementList);
      } else {
        startFlowBlock(statementList);
      }
      startFlowBlock(statementList);
      accept(statementList, this);
      endFlow();
      startFlowBlock(emptyStmt);
      // empty block for false condition
      endFlowBlock();
      endFlowTransaction();
    }
  }

}
