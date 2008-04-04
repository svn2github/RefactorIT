/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinLocalVariableReference;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinLocalVariableFormatter;


/**
 * Local variable defined within method body or {@link BinParameter}
 */
public class BinLocalVariable extends BinVariable {

  public BinLocalVariable(final String name,
      final BinTypeRef typeRef, final int modifiers) {
    super(name, typeRef, modifiers);
  }

  public final String getQualifiedName() {
    return getName();
  }

// FIXME: strange method - should go!!!
//  /**
//   * @deprecated use getParentType() instead!
//   */
  public final BinTypeRef getOwner() {
    BinTypeRef ownerType = super.getOwner();
    if (ownerType == null) {
      ownerType = getParentType().getTypeRef();
    }

    return ownerType;
  }

  public final String getDetails() {
    try {
      return getParentMember().getNameWithAllOwners()
          + " - " + getName();
    } catch (NullPointerException e) {
      e.printStackTrace(System.err);
      return getName();
    }
  }

  /**
   *
   * @return true if variable is assigned somewhere in code(doesn't include declaration point)
   */
  public final boolean isAssignedTo() {
    final boolean result[] = new boolean[] {false};

    getParentMember().accept(new BinItemVisitor() {
      public void visit(BinAssignmentExpression x) {
        if (x.getLeftExpression() instanceof BinVariableUseExpression &&
            ((BinVariableUseExpression) x.getLeftExpression()).getVariable()
            == BinLocalVariable.this) {
          result[0] = true;
        }

        super.visit(x);
      }
    });

    return result[0];
  }

  public final boolean isLocalVariable() {
    return true;
  }

  public void accept(final net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public String getMemberType() {
    return memberType;
  }

  public final BinItemFormatter getFormatter() {
    return new BinLocalVariableFormatter(this);
  }
  
  public BinItemReference createReference() {
    return new BinLocalVariableReference(this);
  }

  private static final String memberType = "local variable";
}
