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
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.BinItemVisitor;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinAnonymousTypeReference extends CacheableReference {
  
  protected final BinItemReference superClass;
  protected final BinItemReference ownerReference;
  protected final int place;
  protected final boolean ownerIsMethod;
  
  public BinAnonymousTypeReference(final BinType binType) {
    super(binType, binType.getProject());
    BinClass binClass = (BinClass) binType;
    superClass = getSuperclass(binClass).createReference();

    if (getMethodForAnonymousClass(binClass) != null) {
      ownerIsMethod = true;
      ownerReference = getMethodForAnonymousClass(binClass).createReference();
      place = getPlaceInMethod(binClass, getMethodForAnonymousClass(binClass));
    } else { // This is for anon types in *fields* and *initializers*.
      ownerIsMethod = false;
      ownerReference = binClass.getOwner().getBinCIType().createReference();
      place = getPlaceInClass(binClass);
    }
  }
  
  public Object findItem(Project project){
    final BinClass superclass = (BinClass) superClass.restore(project);
    final Object result;
    if (ownerIsMethod) {
      result = getAnonymousTypeFromMethod((BinMethod) ownerReference
          .restore(project), superclass, place);
    } else {
      result = getAnonymousTypeFromClass((BinCIType) ownerReference
          .restore(project), superclass, place);
    }
    return result;
  }
  
  final BinClass getSuperclass(BinCIType aClass) {
    if (aClass.getTypeRef().getSuperclass() == null) {
      return null;
    }

    return (BinClass) aClass.getTypeRef().getSuperclass().getBinType();
  }
  
  private BinMethod getMethodForAnonymousClass(final BinCIType anonymousClass) {
    return (anonymousClass.getParentMember() instanceof BinMethod)
        ? (BinMethod) anonymousClass.getParentMember()
        : null;
  }

  private int getPlaceInMethod(final BinClass anonymousClass,
      final BinMethod binMethod) {
    final int result[] = new int[] { -1};

    BinItemVisitor methodFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinCIType t) {
        // FIXME, HACK: t.getOwner().getBinCIType().isEnum()
        if (t.isAnonymous() || t.getOwner().getBinCIType().isEnum()){
          currentPlace++;

          if (t == anonymousClass) {
            result[0] = currentPlace;
          }
        }
        super.visit(t);
      }
    };
    binMethod.accept(methodFinder);

    return result[0];
  }

  private int getPlaceInClass(final BinClass anonymousClass) {
    final int result[] = new int[] { -1};

    BinItemVisitor methodFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinCIType t) {
        // FIXME, HACK: t.getOwner().getBinCIType().isEnum()
        if (t.isAnonymous() || t.getOwner().getBinCIType().isEnum()) {
          currentPlace++;

          if (t == anonymousClass) {
            result[0] = currentPlace;
          }
        }

        // Don't traverse contents
      }

      public void visit(BinMethod m) {
        // Don't traverse contents
      }

      public void visit(BinConstructor m) {
        // Don't traverse contents
      }
    };
    anonymousClass.getOwner().getBinCIType().defaultTraverse(methodFinder);

    return result[0];
  }
  
  private BinCIType getAnonymousTypeFromMethod(final BinMethod method,
      final BinClass superclass, final int place) {
    final BinCIType result[] = new BinCIType[] {null};

    BinItemVisitor anonymousTypeFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinCIType t) {
        // FIXME, HACK: t.getOwner().getBinCIType().isEnum()
        if (t.isAnonymous() || t.getOwner().getBinCIType().isEnum()){
          currentPlace++;

          if (currentPlace == place) {
            result[0] = t;
          }
        }

        super.visit(t);
      }
    };
    method.accept(anonymousTypeFinder);

    return result[0];
  }

  private BinCIType getAnonymousTypeFromClass(BinCIType owner,
      final BinClass superclass, final int place) {
    final BinCIType result[] = new BinCIType[] {null};

    BinItemVisitor anonymousTypeFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinCIType t) {
        // FIXME, HACK: t.getOwner().getBinCIType().isEnum()
        if (t.isAnonymous() || t.getOwner().getBinCIType().isEnum()) {
          currentPlace++;

          if (currentPlace == place) {
            result[0] = t;
          }
        }

        // Don't traverse contents
      }

      public void visit(BinMethod m) {
        // Don't traverse contents
      }

      public void visit(BinConstructor m) {
        // Don't traverse contents
      }
    };
    owner.defaultTraverse(anonymousTypeFinder);

    return result[0];
  }
}
