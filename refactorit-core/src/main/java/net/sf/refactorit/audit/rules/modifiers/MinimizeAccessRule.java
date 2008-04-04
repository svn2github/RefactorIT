/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.modifiers;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.transformations.DeclarationSplitTransformation;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.errors.JWarningDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MinimizeAccessRule extends AuditRule {
  public static final String NAME = "minimize_access";

  private Map memberAccess = new HashMap();
  private Set hierarchyTopMethods = new HashSet();
  private List violative = new ArrayList();
  
  private boolean projectVisited = false;
  private boolean isReflectionUsed = false;
  private boolean isViolationsDetected = false;
  private boolean skipConstructors = false;

  private int innerDepth = 0;

  public void init() {
    skipConstructors = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "constructors", false);
  }

  public void visit(final CompilationUnit cu) {
    if (!projectVisited) {
      // collect usageinfo on first visit
      MemberUsageCollector visitor = new MemberUsageCollector(memberAccess,
          hierarchyTopMethods, cu.getProject().getCompilationUnits().size());
      cu.getProject().accept(visitor);
      visitor.postProcess();
      isReflectionUsed = visitor.isReflectionUsed();
      projectVisited = true;
    }
    super.visit(cu);
  }

  public void visit(BinField field) {
    checkMember(field);
    super.visit(field);
  }

  public void visit(BinMethod meth) {
    checkMember(meth);
    super.visit(meth);
  }

  public void visit(BinConstructor meth) {
    if (!skipConstructors) {
      checkMember(meth);
    }
    super.visit(meth);
  }

  public void visit(BinCIType type) {
    if (type.isInnerType()) { // for inners only
      checkMember(type);
    }
    if (type.isInnerType() || type.isAnonymous() || type.isLocal()) {
      innerDepth++;
    }
    super.visit(type);
  }

  public void leave(BinCIType type) {
    if (type.isInnerType() || type.isAnonymous() || type.isLocal()) {
      innerDepth--;
    }
  }

  public boolean shouldExcludeType(BinTypeRef typeRef) {
    if (typeRef == null) {
      return false;
    }
    BinCIType ciType = typeRef.getBinCIType();
    return ciType != null
        && (ciType.isAnonymous() || ciType.isLocal() || ciType.isInterface() || typeRef
            .getTypeParameters().length > 0);
  }

  public void checkMember(BinMember member) {
    if (shouldExcludeType(member.getOwner()) || innerDepth > 0) {
      return;
    }

    if (member instanceof BinCIType) {
      BinCIType ciType = (BinCIType) member;
      if (shouldExcludeType(((BinCIType) member).getTypeRef())) {
        return;
      }
    }

    Integer access = (Integer) memberAccess.get(member);
    if (access == null) {
      System.err.println("Access is null for " + member.getQualifiedName());
    } else if (BinModifier.compareAccesses(access.intValue(), member
        .getAccessModifier()) < 0) {
      isViolationsDetected = true;
      if (!(member instanceof BinMethod)) {
        addViolation(new MinimizeAccessViolation(member, access.intValue(),
            false));
      } else {
        violative.add(member);
      }
    }
  }

  public void postProcess() {
    while (!violative.isEmpty()) {
      BinMethod method = (BinMethod) violative.get(0);
      BinMethod top = method.getTopSuperclassMethod();
      Integer access = (Integer) memberAccess.get(method);

      List overrides = method.findOverrides();
      boolean canBeMinimized = true;
      for(int i=0; i<overrides.size() && canBeMinimized; i++)
        if(((BinMethod) overrides.get(i)).getOwner().getBinCIType().isInterface())
          canBeMinimized = false;
      
      if(canBeMinimized)
        if (hierarchyTopMethods.contains(top)) {
          List hierarchy = method.findAllOverrides();
          hierarchy.add(method);
  
          Map m = new HashMap();
          for (Iterator it = hierarchy.iterator(); it.hasNext();) {
            BinMethod meth = (BinMethod) it.next();
            BinTypeRef owner = meth.getOwner();
            Integer acc = (Integer) memberAccess.get(meth);
            if (acc != null && owner != null &&
                owner.getCompilationUnit() != null) {
              BinCIType ciType = owner.getBinCIType();
              if(!(ciType.isInterface() || ciType.isLocal() 
                  || ciType.isAbstract() || ciType.isAnonymous())
                  && BinModifier.compareAccesses(acc.intValue(), meth
                    .getAccessModifier()) < 0) {
                m.put(meth, acc);
              }
            }
          }
          hierarchy.clear();
          
          MinimizeAccessViolation violation;
          if(m.size() > 1) { 
            violation = new MinimizeAccessViolation(method,
              access.intValue(), true);
            violation.setHierarchy(new HashMap(m));
          } else {
            violation = new MinimizeAccessViolation(method, access.intValue(),
                false);
          }
          addViolation(violation);
        } else {
          addViolation(new MinimizeAccessViolation(method, access.intValue(),
              false));
        }
      violative.remove(method);
    }
    hierarchyTopMethods.clear();
  }

  public void finishedRun() {
    if (isReflectionUsed && isViolationsDetected && !isTestRun()) {
      new JWarningDialog(
          IDEController.getInstance().createProjectContext(),
          "show.minimize.access.warning",
          "The usage of Reflection API was detected in your project."
              + "It is highly recommended to make sure proposed Minimize Access "
              + "changes do not cause runtime exceptions.",
          JWarningDialog.WARNING_MESSAGE, "").display();
    }
  }
}

class MinimizeAccessViolation extends AwkwardMember {
  private int access;

  private Map hierarchy = null;

  public MinimizeAccessViolation(BinMember member, int access,
      boolean isInHierarchy) {
    super(member, "Can minimize " + member.getMemberType() + " '"
        + member.getName() + "' access " + 
        ((isInHierarchy) ? "within hierarchy " : "")+"to '"
        + new BinModifierFormatter(access, true).print() + "'", null);
    this.access = access;
  }

  public int getAccess() {
    return access;
  }

  public boolean isHierarchical() {
    return (hierarchy != null);
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(ApplyMinimalAccessAction.INSTANCE);
  }

  public void setHierarchy(Map hierarchy) {
    this.hierarchy = hierarchy;
  }

  public Map getHierarchy() {
    return hierarchy;
  }

  public boolean equals(Object o) {
    return o instanceof MinimizeAccessViolation
        && ((MinimizeAccessViolation) o).getOwnerMember().equals(
            getOwnerMember());
  }
}

class ApplyMinimalAccessAction extends MultiTargetGroupingAction {
  static final ApplyMinimalAccessAction INSTANCE = new ApplyMinimalAccessAction();

  public String getKey() {
    return "refactorit.audit.action.minimize_access.apply_minimal";
  }

  public String getName() {
    return "Apply minimal access for member";
  }

  public String getMultiTargetName() {
    return "Apply minimal access for member(s)";
  }
  
  
  public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
    Set sources = new HashSet(violations.size());
    Map memberAccesses = new HashMap();

    for (int i = 0; i < violations.size(); i++) {
      RuleViolation violation = (RuleViolation) violations.get(i);
      if (violation instanceof MinimizeAccessViolation) {
        MinimizeAccessViolation maViolation = (MinimizeAccessViolation) violation;
        if (maViolation.isHierarchical()) {
          memberAccesses.putAll(maViolation.getHierarchy());
        } else {
          memberAccesses.put(maViolation.getOwnerMember(), new Integer(
              maViolation.getAccess()));
        }
      }
    }

    MultiValueMap fieldDeclarations = new MultiValueMap();
    // group fields by declaration
    for (Iterator it = memberAccesses.keySet().iterator(); it.hasNext();) {
      BinMember member = (BinMember) it.next();
      sources.add(member.getCompilationUnit());
      if (member instanceof BinField) {
        BinFieldDeclaration decl = (BinFieldDeclaration) (((BinField) member)
            .getParent());
        fieldDeclarations.put(decl, member);
      } else if (member instanceof BinMethod || member instanceof BinCIType) {
        Integer access = (Integer) memberAccesses.get(member);
        int newModifier = BinModifier.setFlags(member.getModifiers(), access
            .intValue());
        manager.add(new ModifierEditor(member, newModifier));
      }
    }
    
    for (Iterator it = fieldDeclarations.keySet().iterator(); it.hasNext();) {
      BinFieldDeclaration decl = (BinFieldDeclaration) it.next();
      List declMembers = fieldDeclarations.get(decl);

      // no need to execute splitter if single field declaration
      if (decl.getVariables().length == 1) {
        BinMember member = (BinMember) declMembers.get(0);
        Integer access = (Integer) memberAccesses.get(member);
        int newModifier = BinModifier.setFlags(member.getModifiers(), access
            .intValue());
        manager.add(new ModifierEditor(member, newModifier));
      } else {
        HashMap accesses = new HashMap();
        for (int i = 0; i < declMembers.size(); i++) {
          BinMember member = (BinMember) declMembers.get(i);
          Integer access = (Integer) memberAccesses.get(member);
          accesses.put(member, access);
        }
        manager.add(new DeclarationSplitTransformation(decl
            .getCompilationUnit(), decl, accesses));
      }
    }
    memberAccesses.clear();
    fieldDeclarations.clear();
    return sources;
  }
}
