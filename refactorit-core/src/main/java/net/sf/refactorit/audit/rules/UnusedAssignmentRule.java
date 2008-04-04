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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.DelegatingVisitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



/**
 *
 *
 * @author Arseni Grigorjev
 */
public class UnusedAssignmentRule extends AuditRule {
  public static final String NAME = "unused_assignment";

  private static final int AVG_ENTRIES_SIZE = 50;

  private List varEntries = new ArrayList(AVG_ENTRIES_SIZE);
  private FastStack scopes = new FastStack();
  private Object currentScope;

  private boolean useOnlyMode = false;

  private int inTry = 0;

  private DelegatingVisitor revisitingSupervisor = new DelegatingVisitor(true);

  /**
   * Class to store information about variable usage.
   */
  class VariableUsageEntry{
    private BinVariable var;      // the variable itself
    private Object scope;         // the scope, where the variable was declared
    private Object assign;        // contains assign expression
    private Object assignScope;   // the scope, where the variable was assigned

    // if the assignment is expired (when a break or continue statement ocures
    //  after an assignment, the assignment is 'expired' and when a new one
    //  comes, no violation will occur.
    private boolean expired = false;

    /**
     *  Sets assignment to null by default.
     *
     *  @param scope Variable declaration scope
     *  @param var variable
     */
    public VariableUsageEntry(Object scope, BinVariable var) {
      this.scope = scope;
      this.var = var;
      this.assign = null;
    }

    /**
     * Is called when visitor comes upon variable usage (clears assignment, that
     *   means that next assignment will not create a violation)
     */
    public final void use(){
      assign = null;
      expired = false;
    }

    /**
     * Is called when break/continue statement comes after current assignment
     *   in assignment scope or in any child scopes.
     */
    public final void expire(){
      expired = true;
    }

    /**
     * Is called whenever the variable is assigned (var = 5 / var++ / var +=5).
     * If previous assignment record was not null -> violation.
     */
    public final void assign(Object assign, Object scope){
      if (this.assign != null
          && this.assignScope == scope
          && !expired
          && inTry == 0){

        addViolationFor(this.assign, this.var);
      }
      this.assign = assign;
      this.assignScope = scope;
    }

    public final Object getAssign(){
      return this.assign;
    }

    public final Object getAssignScope() {
      return this.assignScope;
    }

    public final BinVariable getVar() {
      return this.var;
    }

    public final Object getScope() {
      return this.scope;
    }
  }

  public UnusedAssignmentRule() {
    revisitingSupervisor.registerDelegate(this);
  }

  // Enter scope: push previous scope into stack, make scope entered -> current
  private final void enterScope(final Object scope){
    if (!useOnlyMode){
      scopes.push(currentScope);
      currentScope = scope;
    }
  }

  // Leaving scope: remove all variables that were declared in this scope, and
  // get currentScope from the scopes-stack.
  private final void leaveScope(){
    if (!useOnlyMode){
      removeAllVarsOf(currentScope);
      currentScope = scopes.pop();
    }
  }

  /**
   * @return VariableUsageEntry record for specified var
   */
  private final VariableUsageEntry getVarEntryFor(BinVariable var){
    VariableUsageEntry result = null;
    for (Iterator it = varEntries.iterator(); it.hasNext(); ){
      result = (VariableUsageEntry) it.next();
      if (result.getVar() == var){
        return result;
      }
    }
    return result;
  }

  // creates VariableUsageEntries for parameters
  private final void registerParameters(BinParameter[] array){
    if (!useOnlyMode){
      for (int i = array.length - 1; i >= 0; i--){
        registerVariable(array[i]);
      }
    }
  }

  // creates VariableUsageEntry for specified var
  private final VariableUsageEntry registerVariable(final BinVariable var){
    final VariableUsageEntry newEntry
        = new VariableUsageEntry(currentScope, var);
    varEntries.add(newEntry);
    return newEntry;
  }

  // removes all variables of specified scope
  private final void removeAllVarsOf(Object scope){
    for (Iterator it = varEntries.iterator(); it.hasNext(); ){
      VariableUsageEntry entry = (VariableUsageEntry) it.next();
      if (entry.getScope() == scope){
        if (entry.getAssign() != null){
          addViolationFor(entry.getAssign(), entry.getVar());
        }
        it.remove();
      }
    }
  }

  // 'expires' assignments for all scopes goind upwards until scope
  private final void expireAssignsUpTo(Object argScope){
    Object scope = argScope;
    if (!(scope instanceof Scope)){
      if (scope instanceof BinWhileStatement){
        scope = ((BinWhileStatement) scope).getStatementList();
        return;
      } else if (scope instanceof BinLabeledStatement){
        scope = ((BinLabeledStatement) scope).getLabelStatementList();
        return;
      } else if (scope instanceof BinSwitchStatement){
        // FIXME: what to do?
        return;
      } else {
        // FIXME: are there more statements, that can be broken with
        // brake statement and are not scopes?
        return;
      }
    }

    final List expireScopes = new LinkedList();
    Object cur_scope = currentScope;
    while (true){
      expireScopes.add(cur_scope);
      if (cur_scope == scope){
        break;
      }
      cur_scope = scopes.pop();
    }

    VariableUsageEntry entry;
    for (Iterator it = varEntries.iterator(); it.hasNext(); ){
      entry = (VariableUsageEntry) it.next();
      for (int i = expireScopes.size() - 1; i >= 0; --i){
        if (entry.getAssignScope() == expireScopes.get(i)){
          entry.expire();
          break;
        }
      }
    }

    for (int i = expireScopes.size() - 1; i > 0; --i){
      scopes.push(expireScopes.get(i));
    }
  }

  // entering scope: method
  public final void visit(BinMethod method) {
    enterScope(method);
    registerParameters(method.getParameters());
    super.visit(method);
  }

  public final void leave(BinMethod method) {
    leaveScope();
    super.leave(method);
  }

  // entering scope: catch clause
  public final void visit(BinTryStatement.CatchClause catchclause) {
    enterScope(catchclause);
    super.visit(catchclause);
  }

  public final void leave(BinTryStatement.CatchClause catchclause) {
    leaveScope();
    super.leave(catchclause);
  }

  // entering scope: constructor
  public final void visit(BinConstructor constructor) {
    enterScope(constructor);
    registerParameters(constructor.getParameters());
  }

  public final void leave(BinConstructor method) {
    leaveScope();
  }

  // entering scope: statement list
  public final void visit(BinStatementList list) {
    enterScope(list);
  }

  public final void leave(BinStatementList method) {
    leaveScope();
  }

  // entering scope: for statement
  public final void visit(BinForStatement statement) {
    enterScope(statement);
  }

  public final void leave(BinForStatement statement) {
    // now revisit condition and statementList in 'find usages only'-mode
    final BinExpression condition = statement.getCondition();
    final BinStatementList statementList = statement.getStatementList();

    final boolean tmp = useOnlyMode;
    useOnlyMode = true;
    if (condition != null){
      condition.accept(revisitingSupervisor);
    }

    if (statementList != null){
      statementList.accept(revisitingSupervisor);
    }
    useOnlyMode = tmp;

    leaveScope();
  }

  public final void visit(BinWhileStatement stmt){
  }

  public final void leave(BinWhileStatement stmt) {
    // now revisit condition and statementList in 'find usages only'-mode
    final BinItemVisitable condition = stmt.getCondition();
    final BinStatementList statementList = stmt.getStatementList();

    final boolean tmp = useOnlyMode;
    useOnlyMode = true;

    if (!stmt.isDoWhile() && condition != null) {
      condition.accept(revisitingSupervisor);
    }
    if (statementList != null){
      statementList.accept(revisitingSupervisor);
    }
    useOnlyMode = tmp;
  }

  // entering scope: initializer
  public final void visit(BinInitializer initializer) {
    enterScope(initializer);
  }

  public final void leave(BinInitializer method) {
    leaveScope();
  }

  // variable usage (clear assignment)
  public final void visit(BinVariableUseExpression expression) {
  }

  public final void leave(BinVariableUseExpression expression) {
    // don`t clear assignment, if variable use expression is left part of
    //   variable assignment expression.
    if (!(expression.getParent() instanceof BinAssignmentExpression) ||
        ((BinAssignmentExpression) expression.getParent()).getLeftExpression()
        != expression){

      VariableUsageEntry varEntry = getVarEntryFor(expression.getVariable());
      if (varEntry != null){
        varEntry.use();
      }
    }
  }

  // variable value assignment
  public final void visit(BinAssignmentExpression expression) {
  }

  public final void leave(BinAssignmentExpression expression) {
    if (!useOnlyMode
        && expression.getLeftExpression() instanceof BinVariableUseExpression){
      VariableUsageEntry varEntry = getVarEntryFor(((BinVariableUseExpression) expression
          .getLeftExpression()).getVariable());
      if (varEntry != null){
        // if it is a 'a += 5' style assignment, use() and only then assign()
        if (expression.getAssignmentType() != JavaTokenTypes.ASSIGN){
          varEntry.use();
        }
        varEntry.assign(expression, currentScope);
      }
    }
  }

  // variable assignment using inc/dec expression
  public final void visit(BinIncDecExpression expr){
  }

  public final void leave(BinIncDecExpression expr) {
    final BinExpression incrementedExpr = expr.getExpression();

    if (!useOnlyMode && incrementedExpr instanceof BinVariableUseExpression){
      VariableUsageEntry varEntry = getVarEntryFor(
          ((BinVariableUseExpression) incrementedExpr).getVariable());
      if (varEntry != null){
        varEntry.use();
        varEntry.assign(expr, currentScope);
      }
    }
  }

  // variable declaration
  public final void visit(BinLocalVariableDeclaration decl){
    if (!useOnlyMode){
      // add variables to the list and make assignments, if needed
      // NOTE: we do not count default assignments as assignments.
      //  ( "int a;" is not an assignment, "int a = 5;" is an assignment. )
      BinVariable[] declvars = decl.getVariables();
      VariableUsageEntry newEntry;
      for (int i = 0; i < declvars.length; i++){
        newEntry = registerVariable(declvars[i]);
        if (declvars[i].getExpression() != null){
          newEntry.assign(declvars[i].getExpression(), currentScope);
        }
      }
    }
  }

  public final void leave(BinBreakStatement stmt){
    if (!useOnlyMode){
      expireAssignsUpTo(stmt.getBreakTarget());
    }
  }

  public final void visit(BinTryStatement stmt){
    ++inTry;
  }

  public final void leave(BinTryStatement stmt) {
    --inTry;
  }

  public final void addViolationFor(Object assign, BinVariable var){
    if (assign instanceof BinParameter){
      addViolation(new UnusedAssignment((BinParameter) assign));
    } else {
      addViolation(new UnusedAssignment((BinExpression) assign, var));
    }
  }
}

class UnusedAssignment extends SimpleViolation {

  private UnusedAssignment(BinTypeRef type, ASTImpl ast, BinVariable variable,
      String msg){
    super(type, ast, msg, "refact.audit.unused_assignment");
  }

  UnusedAssignment(BinExpression expression, BinVariable variable){
    this(expression.getOwner(), expression.getCompoundAst(), variable,
        "Unused assignment on variable '" + variable.getName() + "'");
    setTargetItem(variable);
  }

  UnusedAssignment(BinParameter param){
    this(param.getOwner(), param.getNameAstOrNull(), param,
        "Unused assignment on parameter '" + param.getName() + "'");
    setTargetItem(param);
  }

  public final BinMember getSpecificOwnerMember() {
    BinVariable variable = (BinVariable) getTargetItem();
    if (variable instanceof BinParameter){
      return ((BinParameter) variable).getMethod();
    }
    return variable.getParentMember();
  }
}
