/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.SinglePointVisitor;


/**
 * @author Arseni Grigorjev
 */
public class BinSourceConstructReference extends CacheableReference {

  protected final BinItemReference parentMemberReference;
  protected final Class constructClass;
  protected final int place;

  public BinSourceConstructReference(final BinSourceConstruct construct) {
    super(construct, construct.getParentMember().getProject());
    final BinMember parentMember = construct.getParentMember();
    parentMemberReference = parentMember.createReference();
    constructClass = construct.getClass();
    place = findPlaceForItem(construct, constructClass, parentMember);
  }

  public Object findItem(Project project) {
    final BinMember parentMember = (BinMember) parentMemberReference.restore(
        project);
    return findItemForPlace(place, constructClass, parentMember);
  }

  public static int findPlaceForItem(final BinItem item, final Class itemClass,
      final BinItem where){
    final int[] result = new int[] { -1 };

    where.accept(new SinglePointVisitor(){
      private int count = -1;

      public void onEnter(Object visitedItem){
        if (item.getClass().equals(visitedItem.getClass())){
          count++;
          if (item == visitedItem){
            result[0] = count;
          }
        }
      }

      public void onLeave(Object visitedItem){
        // do nothing
      }
    });

    return result[0];
  }

  public static Object findItemForPlace(final int place, final Class itemClass,
      final BinItem where){
    final Object[] result = new Object[1];

    where.accept(new SinglePointVisitor(){
      private int count = -1;

      public void onEnter(Object visitedItem){
        if (visitedItem.getClass().equals(itemClass)){
          count++;
          if (count == place){
            result[0] = visitedItem;
          }
        }
      }

      public void onLeave(Object visitedItem){
        // do nothing
      }
    });

    return result[0];
  }
}
