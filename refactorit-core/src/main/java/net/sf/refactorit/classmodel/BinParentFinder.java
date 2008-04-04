/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.query.DirectSinglePointVisitor;


/**
 * You should call only methods defined in BinParentFinder and not in superclasses though
 *
 * FIXME: need to think and check the try-finally situation. PArents should be found anyway?
 */
public final class BinParentFinder extends DirectSinglePointVisitor {
  private BinItem currentParent = null;

  private BinParentFinder() {}
  
  private BinParentFinder(BinItem currentParent) {
    this.currentParent = currentParent;
  }

  /**
   * Use this one for methods and constructors
   */
  public static void findParentsFor(BinMember x) {
    new BinParentFinder().findParentsImpl(x);
  }
  
  public static void findParentsFor(BinSourceConstruct x) {
    new BinParentFinder((BinItem)x.getParent()).doVisit(x);
  }

  private void findParentsImpl(BinMember x) {
    currentParent = x;
    x.defaultTraverse(this);
    currentParent = null;
  }

//  private void findParentsImpl(BinMember x) {
//    if (x.getOwner() != null) {
//      currentParent = x.getOwner().getBinCIType();
//    }
//    doVisit(x);
//    currentParent = null;
//  }

  public final void doVisit(BinItem o) {
    o.setParent(currentParent);

    BinItem oldParent = currentParent;
    currentParent = o;
    o.defaultTraverse(this);
    currentParent = oldParent;
  }

}
