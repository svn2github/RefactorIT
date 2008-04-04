/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.query.BinItemVisitor;


/**
 *
 * @author Arseni Grigorjev
 */
public class BinLocalTypeReference extends CacheableReference {
  
  protected final BinItemReference ownerReference;
  protected final String name;
  protected final boolean containedInNamedLocal;
  
  public BinLocalTypeReference(final BinType binType) {
    super(binType, binType.getProject());
    name = binType.getName();
    final BinClass binClass = (BinClass) binType;
    if (containedInNamedLocalClass(binClass)) {
      containedInNamedLocal = true;
      ownerReference = binType.getOwner().createReference();
    } else {
      containedInNamedLocal = false;
      ownerReference = binClass.getParentMember().createReference();
    }
  }
  
  public Object findItem(Project project){
    if (!containedInNamedLocal){
      BinMethod method = (BinMethod) ownerReference.restore(project);
      return getLocalTypeFromMethod(method, name);
    } else {
      BinTypeRef owner = (BinTypeRef) ownerReference.restore(project);
      return owner.getBinCIType().getDeclaredType(name).getBinType();
    }
  }
    
  private boolean containedInNamedLocalClass(final BinClass aClass) {
    final boolean[] result = new boolean[] {false};

    BinItemVisitor visitor = new BinItemVisitor() {
      private final FastStack currentMethod = new FastStack();

      public void visit(BinCIType t) {
        if (t == aClass) {
          result[0] = currentMethod.isEmpty();
        }

        super.visit(t);
      }

      public void visit(BinMethod m) {
        currentMethod.push(m);
        try {
          super.visit(m);
        } finally {
          currentMethod.pop();
        }
      }

      public void visit(BinConstructor m) {
        currentMethod.push(m);
        try {
          super.visit(m);
        } finally {
          currentMethod.pop();
        }
      }
    };

    aClass.getOwner().getBinCIType().accept(visitor);

    return result[0];
  }
  
  private BinCIType getLocalTypeFromMethod(final BinMethod method,
      final String typeName) {
    final BinCIType result[] = new BinCIType[] {null};

    BinItemVisitor anonymousTypeFinder = new BinItemVisitor() {
      public void visit(BinCIType t) {
        if (t.getName().equals(typeName)) {
          result[0] = t;
        }

        super.visit(t);
      }
    };
    method.accept(anonymousTypeFinder);

    return result[0];
  }
}
