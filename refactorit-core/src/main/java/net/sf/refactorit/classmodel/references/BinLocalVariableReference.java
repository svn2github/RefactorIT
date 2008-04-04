/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.BinItemVisitor;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinLocalVariableReference extends CacheableReference {
  private String name;
  private BinItemReference owner;
  private BinItemReference type;
  private int place;

  public BinLocalVariableReference(final BinLocalVariable var) {
    super(var, var.getProject());
    name = var.getName();
    owner = var.getParentMember().createReference();
    type = var.getTypeRef().getBinType().createReference();
    place = getPlaceForLocalVariable(var);
  }

  public Object findItem(Project project) {
    final BinMember ownerMember = (BinMember) owner.restore(project);
    final BinType varType = (BinType) type.restore(project);
    return getLocalVariableForPlace(place, varType, ownerMember);
  }

  private BinLocalVariable getLocalVariableForPlace(final int place,
      final BinType varType, BinMember ownerMember){
    final BinLocalVariable result[] = new BinLocalVariable[] {null};

    BinItemVisitor visitor = new BinItemVisitor() {

    private int currentPlace = -1;

    public void visit(BinLocalVariable var) {
      if (var.getName() == null) {
        return; // This can happen when var is instanceof BinXXXParameter.
      }

      if (var.getName().equals(name) &&
        varType.getQualifiedName().equals(var.getTypeRef().
           getQualifiedName())) {

            currentPlace++;

            if (currentPlace == place) {
              result[0] = var;
            }
          }

          super.visit(var);
        }
      };
      ownerMember.accept(visitor);

      return result[0];
  }

  private int getPlaceForLocalVariable(final BinLocalVariable var) {
    final int[] result = new int[] { -1 };

    final String name = var.getName();
    final BinType varType = var.getTypeRef().getBinType();

    BinItemVisitor visitor = new BinItemVisitor() {
      private int currentPlace = -1;

      public void visit(BinLocalVariable lv) {
        if (lv.getName() == null) {
          return; // This can happen when var is instanceof BinXXXParameter.
        }

        if (name.equals(lv.getName()) &&
            varType.getQualifiedName().equals(lv.getTypeRef().getQualifiedName())) {
          currentPlace++;

          if (lv == var) {
            result[0] = currentPlace;
          }
        }

        super.visit(lv);
      }
    };
    var.getParentMember().accept(visitor);

    return result[0];
  }
}
