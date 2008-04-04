/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.complexity;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.utils.AuditProfileUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class LawOfDemeterRule extends AuditRule {
  private class BinCITypeVisitor extends BinItemVisitor {
    private Set violations = new HashSet();

    private BinTypeRef currentType;

    public BinCITypeVisitor() {
      super();
    }

    public void visit(BinCIType x) {
      BinTypeRef oldCurrentType = currentType;
      currentType = x.getTypeRef();
      
      super.visit(x);
      currentType = oldCurrentType;
    }

    public void visit(BinMethodInvocationExpression x) {
      if (isViolative(x))
        violations.add(getTopExpression(x));
      
      super.visit(x);
    }

    public void visit(BinFieldInvocationExpression x) {
      if (isViolative(x))
        violations.add(getTopExpression(x));
      
      super.visit(x);
    }

    private BinExpression getTopExpression(BinMemberInvocationExpression x) {
      BinItemVisitable parent = x;
      while (((BinMemberInvocationExpression) parent).getParent() instanceof BinMemberInvocationExpression)
        parent = ((BinMemberInvocationExpression) parent).getParent();

      return (BinExpression) parent;
    }

    public Set getViolations() {
      return violations;
    }

    //Tries to find expressions, that must be skipped according to the user's selection
    //i. e. singleton, System.out.println() and HashMap.keySet().iterator()
    private boolean mustBeSkipped(BinMemberInvocationExpression x) {
      if (skipSystem && x instanceof BinMethodInvocationExpression) {
        BinMethodInvocationExpression bmie = (BinMethodInvocationExpression) x;
        if (bmie.getMethod().getName().equals("print")
            || bmie.getMethod().getName().equals("println")) {
          if (bmie.getExpression() instanceof BinFieldInvocationExpression) {
            BinFieldInvocationExpression bfie = (BinFieldInvocationExpression) bmie
                .getExpression();
            if (bfie.getField().getName().equals("out")
                || bfie.getField().getName().equals("err"))
              if (bfie.getField().getOwner() == x.getParentType().getProject()
                  .getTypeRefForName("java.lang.System"))
                return true;
          }
        }
      }

      if (skipHashMap && x instanceof BinMethodInvocationExpression) {
        BinMethodInvocationExpression bmie1 = (BinMethodInvocationExpression) x;
        BinTypeRef mapRef = x.getParentType().getProject().getTypeRefForName(
            "java.util.Map");
        if (bmie1.getMethod().getName().equals("iterator"))
          if (bmie1.getExpression() instanceof BinMethodInvocationExpression) {
            BinMethodInvocationExpression bmie2 = (BinMethodInvocationExpression) bmie1
                .getExpression();
            if (bmie2.getMethod().getName().equals("keySet")) {
              if (bmie2.getInvokedOn().equals(mapRef))
                return true;
              BinTypeRef[] interfaces = bmie2.getInvokedOn().getInterfaces();
              for (int i = 0; i < interfaces.length; i++)
                if (interfaces[i].equals(mapRef))
                  return true;

            }
          }
      }

      if (skipSingleton)
        if (x.getExpression() instanceof BinMethodInvocationExpression) {
          if (((BinMethodInvocationExpression) x.getExpression()).getMethod()
              .getName().equals("getInstance"))
            return true;
        } else if (x.getExpression() instanceof BinFieldInvocationExpression) {
          if (((BinFieldInvocationExpression) x.getExpression()).getField()
              .getName().equalsIgnoreCase("instance"))
            return true;
        }

      return false;
    }

    private boolean isViolative(BinMemberInvocationExpression x){
      if (mustBeSkipped(x))
        return false;

      BinExpression parent = x.getExpression();
      while (parent instanceof BinMemberInvocationExpression) {
        BinTypeRef owner = getTopOwner(((BinMemberInvocationExpression) parent)
            .getMember());
        BinTypeRef curType = getTopOwner(currentType);
        if (curType != owner
            && !curType.getBinCIType().contains(
                (LocationAware) owner.getBinCIType())
            && !curType.getAllSupertypes().contains(owner))
          return true;
        parent = ((BinMemberInvocationExpression) parent).getExpression();
      }

      return false;
    }

    private BinTypeRef getTopOwner(BinMember x) {
      BinTypeRef result = x.getOwner();
      while (result.getBinCIType().getOwner() != null)
        result = result.getBinCIType().getOwner();

      return result;
    }

    private BinTypeRef getTopOwner(BinTypeRef x) {
      if (x.getBinCIType().getOwner() == null)
        return x;

      BinTypeRef result = x.getBinCIType().getOwner();
      while (result.getBinCIType().getOwner() != null)
        result = result.getBinCIType().getOwner();

      return result;
    }
  }

  public static final String NAME = "LawOfDemeter";

  private boolean skipSingleton, skipHashMap, skipSystem;

  MultiValueMap localvars = new MultiValueMap();

  public void init() {
    skipSingleton = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "skip_singleton", false);
    skipHashMap = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "skip_hashmap_keyset", false);
    skipSystem = AuditProfileUtils.getBooleanOption(getConfiguration(), "skip",
        "skip_system_out", false);
  }

  public void visit(BinCIType x) {
    if (!x.isLocal() && !x.isInnerType()) {
      BinCITypeVisitor visitor = new BinCITypeVisitor();
      x.accept(visitor);

      for (Iterator a = visitor.getViolations().iterator(); a.hasNext();) {
        BinExpression ex = (BinExpression) a.next();
        addViolation(new LawOfDemeterViolation(ex,
            "Expression violates Law Of Demeter", ""));
      }
    }
    
    super.visit(x);
  }
}

class LawOfDemeterViolation extends AwkwardSourceConstruct {
  public LawOfDemeterViolation(BinSourceConstruct construct, String message,
      String helpID) {
    super(construct, message, null/*helpID*/);
  }
}
