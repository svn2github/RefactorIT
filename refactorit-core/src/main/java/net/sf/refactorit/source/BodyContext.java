/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.source;


import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.loader.Settings;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public final class BodyContext {
  private final FastStack variableMapStack = new FastStack();
  private final FastStack typeMapStack = new FastStack();
  private final FastStack scopeAttachmentStack = new FastStack();
  private final FastStack currentTypeStack = new FastStack();
  private final FastStack switchEnumTypeStack = new FastStack();

  private final FastStack labelStatements = new FastStack();
  private final FastStack breakTargets = new FastStack();
  /** allows to form correct default constructor for anonymous types */
  private BinExpressionList expressionList;

  private boolean hasLocalTypes = false;

  private final CompilationUnit compilationUnit;

  private static final Scope NULL_ATTACHMENT = new Scope() {
    public void initScope(final HashMap vars, final HashMap b) {
      //Assert.must(false, "Tryng to initialize null scope with " + vars  );
      if (Assert.enabled) {
        System.err.println("Closing error scope");
      }
    }

//    public Scope.ScopeRules getScopeRules() {
//      //Assert.must(false, "Asking rules from invalid scope");
//      return null;
//    }

    public boolean contains(Scope other) {
      return false;
    }
  };

  /** For testing */
  private static final List allCreatedInstances = new ArrayList();

  /** For testing */
  public static void startCollectingCreatedInstances() {
    allCreatedInstances.clear();
  }

  /** For testing */
  public static void assertAllCreatedInstancesAreEmptied() {
    for (int i = 0; i < allCreatedInstances.size(); i++) {
      ((BodyContext) allCreatedInstances.get(i)).assertEmpty();
    }
  }

  /** For testing, only works when Assert.enabled */
  private void assertEmpty() {
    if (Assert.enabled) {
      Assert.must(this.typeMapStack.isEmpty(),
          "typeMapStack has to be empty when BodyContext is no longer used");
      Assert.must(this.scopeAttachmentStack.isEmpty(),
          "scopeAttachmentStack has to be empty when BodyContext is no longer used");
      Assert.must(this.currentTypeStack.isEmpty(),
          "currentTypeStack has to be empty when BodyContext is no longer used");
      Assert.must(this.labelStatements.isEmpty(),
          "labelStatements has to be empty when BodyContext is no longer used");
      Assert.must(this.breakTargets.isEmpty(),
          "breakTargets has to be empty when BodyContext is no longer used");
    }
  }

  public BodyContext(final CompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;

    if (Assert.enabled) { // For testing
      allCreatedInstances.add(this);
    }
  }

  /** Starts a scope that must be closed later (endType() does that, for example) */
  public final void startType(final BinTypeRef typeRef) {
//System.err.println("startType: " + typeRef);
    currentTypeStack.push(typeRef);
    variableMapStack.push(new FastStack());
    beginScope(typeRef);
  }

  /** Ends a scope as well */
  public final void endType() {
    endScope();
    variableMapStack.pop(); // pop whole member scopes stack
    currentTypeStack.pop();
    /*    if(!scopeAttachmentStack.isEmpty()) {
          System.err.println("WARNING - HANGING SCOPE COUNT " + scopeAttachmentStack.size());
        }*/
//System.err.println("endType: " + object);
  }

  public final BinTypeRef getParentTypeRef() {
    return (BinTypeRef) currentTypeStack.get(currentTypeStack.size() - 2);
  }

  public final BinTypeRef getTypeRef() {
    return (BinTypeRef) currentTypeStack.peek();
  }

  public final void startSwitch(final BinTypeRef typeRef) {
    if (typeRef.isPrimitiveType() || typeRef.isArray()) {
      switchEnumTypeStack.push(null);
    } else {
      switchEnumTypeStack.push(typeRef);
    }
  }

  public final void endSwitch() {
    switchEnumTypeStack.pop();
  }

  public final BinTypeRef getSwitchType() {
    if (!switchEnumTypeStack.isEmpty()) {
      return (BinTypeRef) switchEnumTypeStack.peek();
    }

    return null;
  }

  public final BinMember getBlock() {
    for (int i = scopeAttachmentStack.size(); --i >= 0; ) {
      if (scopeAttachmentStack.get(i) instanceof BinMember) {
        return (BinMember) scopeAttachmentStack.get(i);
      }
    }

    return null;
  }

  public final void beginScope(final Scope aScope) {
    if (Assert.enabled) {
      Assert.must(aScope != null, "Scope pushed can not be null");
    }
    scopeAttachmentStack.push(aScope);
    beginScopeImpl();
  }

  public final void attachScopeReceiver(final Scope aScope) {
    final Object last = scopeAttachmentStack.pop(); // pop NULL_ATTACHMENT attached by default
    if (Assert.enabled && last != NULL_ATTACHMENT) {
      Assert.must(false,
          "Attaching scope " + aScope + " to already defined " + last);
    }
    scopeAttachmentStack.push(aScope);
  }

  public final void beginScope() {
    scopeAttachmentStack.push(NULL_ATTACHMENT);
    beginScopeImpl();
  }

  private void beginScopeImpl() {
//System.err.println("beginScope (" + this + ")");
    getCurrentVariableMapStack().push(new HashMap(3));
    typeMapStack.push(new HashMap(1));
  }

  public final void endScope() {
//System.err.println("endScope (" + this + ")");
    final HashMap variableMap = (HashMap) getCurrentVariableMapStack().pop();
    final HashMap typeMap = (HashMap) typeMapStack.pop();
    final Scope attachable = (Scope) scopeAttachmentStack.pop();
    attachable.initScope(variableMap, typeMap);
  }

  public final void startLabel(final String label, final BinStatement statement) {
    labelStatements.push(label);
    labelStatements.push(statement);
  }

  public final void stopLabel() {
    labelStatements.pop();
    labelStatements.pop();
  }

  public final BinStatement getStatementForLabel(final String label) {
    BinLabeledStatement result = null;
    final int size = labelStatements.size();

    for (int i = 0; i < size / 2; ++i) {
      final String comparable = (String) labelStatements.get(size - i * 2 - 2);
      if (comparable.equals(label)) {
        result = (BinLabeledStatement) labelStatements.get(size - i * 2 - 1);
        break;
      }
    }

    return result;
  }

  public final void startBreakTarget(final BinStatement statement) {
    breakTargets.push(statement);
  }

  public final void endBreakTarget() {
    breakTargets.pop();
  }

  public final BinStatement getBreakTarget() {
    if (breakTargets.isEmpty()) {
      return null;
    }
    return (BinStatement) breakTargets.peek();
  }

  public final void addVariable(final BinVariable variable) {
    if (Assert.enabled) {
      Assert.must(!(variable instanceof BinField),
          "Called addVariable with field " + variable.getName());
    }

    getCurrentVariableMap().put(variable.getName(), variable);
  }

  private HashMap getCurrentVariableMap() {
    return (HashMap) getCurrentVariableMapStack().peek();
  }

  private FastStack getCurrentVariableMapStack() {
    return (FastStack) variableMapStack.peek();
  }

  public Iterator getCurrentVariables() {
    return getCurrentVariableMap().values().iterator();
  }

  public final void addTypeRef(final BinTypeRef typeRef) {
    final HashMap typeMap = (HashMap) typeMapStack.peek();
    typeMap.put(typeRef.getName(), typeRef);
  }

  public final void addTypeRefs(final BinTypeRef[] typeRefs) {
    if (typeRefs == null) { return; }
    final HashMap typeMap = (HashMap) typeMapStack.peek();
    for (int i = typeRefs.length; --i >= 0; ) {
      typeMap.put(typeRefs[i].getName(), typeRefs[i].getTypeRefAsIs()); // JAVA5: should we unwrap here?
    }
  }

  public final BinTypeRef[] getParentLocalTypeRefs() {
    final HashMap typeMap = (HashMap) typeMapStack.get(typeMapStack.size() - 2);
    return (BinTypeRef[]) typeMap.values().toArray(
        new BinTypeRef[typeMap.size()]);
  }

  public final BinVariable getLocalVariableForName(final String name,
      final BinTypeRef context) {
    BinTypeRef curType = getTypeRef();

    for (int k = variableMapStack.size(); --k >= 0 && curType != null; ) {
      if (context == curType || context.equals(curType)) {
        // traversing inners scopes
        final FastStack curVariableMapStack = (FastStack) variableMapStack.get(k);

        for (int i = curVariableMapStack.size(); --i >= 0; ) {
          final HashMap variableMap = (HashMap) curVariableMapStack.get(i);

          final BinVariable retVal = (BinVariable) variableMap.get(name);

          if (retVal != null) {
            return retVal;
          }
        }

        break;
      } else {
        curType = curType.getBinCIType().getOwner();
      }
    }

    return null;
  }

  /** Searches all contexts starting from current which is deepest */
  public final BinVariable getLocalVariableForName(final String name) {

    for (int k = variableMapStack.size() - 1; k >= 0; k--) {
      // traversing inners scopes
      final FastStack curVariableMapStack = (FastStack) variableMapStack.get(k);

      for (int i = curVariableMapStack.size() - 1; i >= 0; i--) {
        final HashMap variableMap = (HashMap) curVariableMapStack.get(i);

        final BinVariable retVal = (BinVariable) variableMap.get(name);

        if (retVal != null) {
          return retVal;
        }
      }
    }

    return null;
  }

  public final BinTypeRef getLocalTypeForName(final String name) {
    //System.err.println("Searching for local type : " + name);
    if (Settings.debugLevel > 50) {
      System.out.println("-- local types -- find: " + name);

      for (int i = typeMapStack.size(); --i >= 0;) {
        final HashMap typeMap = (HashMap) typeMapStack.get(i);
        final Iterator it = typeMap.values().iterator();

        for (; it.hasNext(); ) {
          System.out.println(((BinTypeRef) it.next()).getName());
        }
      }
      System.out.println("-- -------------- --");
    }

    for (int i = typeMapStack.size(); --i >= 0; ) {
      final HashMap typeMap = (HashMap) typeMapStack.get(i);
      final BinTypeRef retVal = (BinTypeRef) typeMap.get(name);
//System.err.println("search: " + name + " - " + typeMap + " - " + retVal);

      if (retVal != null) {
        return retVal;
      }
    }

    if (Settings.debugLevel > 50) {
      System.out.println("have not found in " + this);
    }

    //System.err.println("have not found in " + this);
    return null;
  }

  public final BinTypeRef getTypeRefForName(final String name)
      throws SourceParsingException, LocationlessSourceParsingException {
//System.err.println("name: " + name);
    BinTypeRef retVal = getLocalTypeForName(name);
//System.err.println("no: " + retVal);
//if (Assert.enabled) {
//  Assert.must(retVal == null || !retVal.isSpecific(), "Got specific typeRef: " + retVal);
//}

    if (retVal == null) {
//System.err.println("resolver: " + getTypeRef().getResolver());
      try {
        retVal = getTypeRef().getResolver().resolve(name);
      } catch (RuntimeException e) {
        String mess = "Failed to resolve: " + name + " in context: "
            + BinFormatter.formatQualified(getTypeRef());
        AppRegistry.getExceptionLogger().error(e, mess, this.getClass());
        SourceParsingException.throwWithUserFriendlyError(mess, getCompilationUnit());
      }
//      if (Assert.enabled) {
//        Assert.must(retVal == null || !retVal.isSpecific(), "Got specific typeRef: " + retVal);
//      }
    }
//    if (retVal == null) {
//      System.err.println("not resolved: " + name + " -- " + getTypeRef().getResolver());
//    }

    return retVal;
  }

  public final Project getProject() {
    return getTypeRef().getProject();
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final void setExpressionList(final BinExpressionList expressionList) {
    this.expressionList = expressionList;
  }

  public final BinExpressionList getExpressionList() {
    return this.expressionList;
  }

  public final String toString() {
    String name = this.getClass().getName();
    name = name.substring(name.lastIndexOf('.') + 1);
    return name + " - cur type: " + getTypeRef().getQualifiedName()
        + ", block: " + getBlock();
  }


  public boolean isHasLocalTypes() {
    return this.hasLocalTypes;
  }

  public void setHasLocalTypes(final boolean hasLocalTypes) {
    this.hasLocalTypes = hasLocalTypes;
  }
}
