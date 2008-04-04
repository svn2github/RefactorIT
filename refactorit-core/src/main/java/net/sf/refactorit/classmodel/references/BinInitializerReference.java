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
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.BinItemVisitor;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinInitializerReference extends CacheableReference {
  
  private final boolean isStatic;
  private final BinItemReference ownerReference;
  private final int place;
  
  public BinInitializerReference(final BinInitializer initializer) {
    super(initializer, initializer.getProject());
    place = getPlaceForInitializer(initializer);
    ownerReference = initializer.getOwner().getBinCIType().createReference();
    isStatic = initializer.isStatic();
  }
  
  public Object findItem(Project project) {
    final BinCIType aClass = (BinCIType) ownerReference.restore(project);
    return getInitializerForPlace(aClass, place, isStatic);
  }
  
  private int getPlaceForInitializer(final BinInitializer initializer) {
    final int[] result = new int[] { -1};

    BinItemVisitor initializerFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinInitializer visitedInitializer) {
        if (visitedInitializer.isStatic() == initializer.isStatic()) {
          currentPlace++;

          if (visitedInitializer == initializer) {
            result[0] = currentPlace;
          }
        }

        super.visit(visitedInitializer);
      }
    };
    initializer.getOwner().getBinCIType().accept(initializerFinder);

    return result[0];
  }
  
  private BinInitializer getInitializerForPlace(final BinCIType aClass,
      final int place, final boolean isStatic) {
    final BinInitializer result[] = new BinInitializer[] {null};

    BinItemVisitor initializerFinder = new BinItemVisitor() {
      private int currentPlace = 0;

      public void visit(BinInitializer visitedInitializer) {
        if (visitedInitializer.isStatic() == isStatic) {
          currentPlace++;

          if (currentPlace == place) {
            result[0] = visitedInitializer;
          }
        }

        super.visit(visitedInitializer);
      }
    };
    aClass.accept(initializerFinder);

    return result[0];
  }
}
