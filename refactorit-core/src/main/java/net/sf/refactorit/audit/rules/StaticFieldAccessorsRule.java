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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class StaticFieldAccessorsRule extends AuditRule {
  public static final String NAME = "static_accessors";

  private MultiValueMap varAccessors = new MultiValueMap();

  private List variables = new ArrayList();

  private List visited = new ArrayList();

  private MultiValueMap trashMethods = new MultiValueMap();

  public void visit(BinFieldDeclaration decl) {
    BinVariable[] vars = decl.getVariables();

    for (int i = 0; i < vars.length; i++) {
      if (vars[i].isStatic()) {
        variables.add(vars[i]);
      }
    }
    super.visit(decl);
  }

  public void visit(BinFieldInvocationExpression expr) {
    final BinField var = expr.getField();
    final BinMember parent = expr.getParentMember();
    if (var instanceof BinVariable && parent instanceof BinMethod) {
      varAccessors.put(var, parent);
    }
    super.visit(expr);
  }

  public void visit(BinCIType c) {
    visited.add(c.getTypeRef());
    super.visit(c);
  }

  public void postProcess() {

    while (variables.size() > 0) {
      BinVariable var = (BinVariable) variables.get(0);
      if (var.getOwner() != null) {
        List ownerHierarchy = var.getOwner().getAllSubclasses();
        ownerHierarchy.addAll(var.getOwner().getAllSupertypes());

        List varHierarchy = new ArrayList();
        for (int k = 0; k < ownerHierarchy.size(); k++) {
          BinTypeRef ref = (BinTypeRef) ownerHierarchy.get(k);
          BinField fld = ref.getBinCIType().getDeclaredField(var.getName());
          if (fld instanceof BinVariable) {
            varHierarchy.add(fld);
          }
        }
        varHierarchy.add(var);
        ownerHierarchy.clear();

        for (int k = 0; k < varHierarchy.size(); k++) {
          BinVariable tmpVar = (BinVariable) varHierarchy.get(k);
          if (varAccessors.containsKey(tmpVar)) {
            for (Iterator it = varAccessors.get(tmpVar).iterator(); it
                .hasNext();) {
              BinMethod tmpMeth = (BinMethod) it.next();
              for (int j = 0; j < varHierarchy.size(); j++) {
                if (k != j) {
                  BinVariable tempVar = (BinVariable) varHierarchy.get(j);
                  if (tempVar.getOwner() != null
                      && tempVar.getOwner().getCompilationUnit() != null
                      && visited.contains(tempVar.getOwner())) {
                    if (!hasOverridingAccessor(tempVar, tmpMeth)) {
                      BinMethod ovrMeth = getMethodOverride(tempVar.getOwner(),
                          tmpMeth);
                      if (ovrMeth == null) {
                        addViolation(new MissingAccessorMethodViolation(tempVar
                            .getOwner(), tmpMeth));
                      } else {
                        addViolation(new MissingFieldAccessViolation(ovrMeth,
                            tempVar));
                      }
                    }
                  }
                }
              }
              trashMethods.put(tmpVar, tmpMeth);
            }
            varAccessors.removeAll(trashMethods);
            trashMethods.clear();
          }

        }
        variables.removeAll(varHierarchy);
      }
      variables.remove(var);
    }

    varAccessors.clear();
    visited.clear();
  }

  private boolean hasOverridingAccessor(BinVariable var, BinMethod meth) {
    boolean result = false;
    if (var != null && meth != null && varAccessors.containsKey(var)) {
      List hierarchy = getMethodPseudoHierarchy(meth);

      for (Iterator it = varAccessors.get(var).iterator(); it.hasNext();) {
        BinMethod accessMeth = (BinMethod) it.next();
        if (hierarchy.contains(accessMeth)) {
          result = true;
          trashMethods.put(var, accessMeth);
        }
      }
    }
    return result;
  }

  private BinMethod getMethodOverride(BinTypeRef ref, BinMethod meth) {
    BinMethod ovrMethod = null;
    if (ref != null && meth != null && ref.getBinCIType() != null) {
      ovrMethod = ref.getBinCIType().getDeclaredMethod(meth.getName(),
          meth.getParameters());
    }
    return ovrMethod;
  }

  private List getMethodPseudoHierarchy(BinMethod meth) {
    List hierarchy = new ArrayList();
    final BinTypeRef owner = meth.getOwner();
    if (owner != null) {
      final String name = meth.getName();
      final BinParameter[] params = meth.getParameters();

      List classHierarchy = owner.getAllSubclasses();
      classHierarchy.addAll(owner.getAllSupertypes());
      for (int i = 0; i < classHierarchy.size(); i++) {
        BinTypeRef tmpRef = (BinTypeRef) classHierarchy.get(i);
        BinMethod tmpMeth = tmpRef.getBinCIType().getDeclaredMethod(name,
            params);
        if (tmpMeth != null) {
          hierarchy.add(tmpMeth);
        }
      }
    }
    return hierarchy;
  }
}

class MissingAccessorMethodViolation extends SimpleViolation {

  public MissingAccessorMethodViolation(BinTypeRef member, BinMethod method) {
    super(member, member.getBinCIType().getNameAstOrNull(), " Class '"
        + member.getName() + "' should probably override '" + method.getName()
        + "()' method", "refact.audit.static_accessors");
  }

}

class MissingFieldAccessViolation extends SimpleViolation {

  public MissingFieldAccessViolation(BinMethod method, BinVariable var) {
    super(method.getOwner(), method.getRootAst(), " Method '"
        + method.getName() + "()' should probably access '" + var.getName()
        + "' variable", "refact.audit.static_accessors");
  }

}
