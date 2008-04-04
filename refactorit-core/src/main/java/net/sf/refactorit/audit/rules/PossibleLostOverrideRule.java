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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PossibleLostOverrideRule extends AuditRule {

  // The strategy is next:
  // for each method find list of methods from superclass of current method
  // owner
  // class, that are of the same name as current method (possible override
  // methods).
  // If current method does not override any of found methods,
  // and some found method is not already overridden in owner class of current
  // method, then the pair of current method and some found method is possible
  // lost override.

  public static final String NAME = "lost_override";

  public void visit(BinMethod method) {

    List suspiciousMethods = findPossiblyOverriddenMethods(method);

    for (int i = 0; i < suspiciousMethods.size(); i++) {
      final BinMethod suspMeth = (BinMethod) suspiciousMethods.get(i);
      if (!isDerivedFromAnyOf(suspiciousMethods, method)
          && !isAlreadyOverridden(method.getOwner(), suspMeth)) {
        addViolation(new LostOverrideViolation(method, suspMeth));
      }
    }
    super.visit(method);
  }

  private boolean isDerivedFromAnyOf(List suspMethods, BinMethod meth) {
    for (int i = 0; i < suspMethods.size(); i++) {
      BinMethod suspMeth = (BinMethod) suspMethods.get(i);
      if (suspMeth.getCompilationUnit() != null
          && !suspMeth.isPrivate()
          && meth.sameSignature(suspMeth)
          && suspMeth.isAccessible(suspMeth.getOwner().getBinCIType(), meth
              .getOwner().getBinCIType())) {
        return true;
      }
    }
    return false;
  }

  private List findPossiblyOverriddenMethods(BinMethod meth) {

    List foundMethods = new ArrayList();
    BinTypeRef parentClassTypeRef = meth.getOwner().getSuperclass();

    if ((meth == null) || (parentClassTypeRef == null)) {
      return foundMethods;
    }

    BinCIType parentCIType = parentClassTypeRef.getBinCIType();
    String currentName = meth.getName();

    foundMethods.addAll(Arrays.asList(parentCIType.getAccessibleMethods(
        currentName, parentCIType)));

    // foundMethods.removeAll(parentClassTypeRef.getTypeRef());

    return foundMethods;
  }

  private boolean isAlreadyOverridden(BinTypeRef currentClass,
      BinMethod suspMethod) {
    if ((currentClass == null) || (suspMethod == null)) {
      return false;
    }

    BinCIType classCIType = currentClass.getBinCIType();
    List currentClassMethods = Arrays.asList(classCIType.getDeclaredMethods());

    for (int i = 0; i < currentClassMethods.size(); i++) {
      BinMethod currentClassMethod = (BinMethod) currentClassMethods.get(i);
      if (currentClassMethod.sameSignature(suspMethod)) {
        return true;
      }
    }

    return false;
  }
}

class LostOverrideViolation extends AwkwardMember {
  public LostOverrideViolation(BinMethod clss, BinMethod viol) {
    super(clss, "Possible lost override of "
        + viol.getQualifiedNameWithParamTypes() + " by "
        + clss.getQualifiedNameWithParamTypes(), null);
  }
}
