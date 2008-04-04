/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinItemFormatter;



/**
 * Base class for BinMember, BinStatement and BinExpression.
 */
public abstract class BinItem implements BinItemVisitable, Referable {

  public BinItem() {
    // System.out.println(getClass().getName());
  }

  public void accept(final BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(final BinItemVisitor visitor) {
    // NOTE: it can come here in e.g. MissingBinMember
    //if (Assert.enabled) {
    //  Assert.must(false, "This should never be called on BinItem instance");
    //}
  }

  public String toString() {
    final String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + " "
        + Integer.toHexString(hashCode());
  }

  /**
   * Recursively checks both structure and names.
   * @param other structure
   * @return true if both structure matches and identifiers have the same name
   */
  public boolean isSame(BinItem other) {
    if (Assert.enabled) {
      Assert.must(false, "Illegally called on: "
          + ClassUtil.getShortClassName(this));
    }
    return false;
  }

  /** Helper to compare sometimes missing items.
   * @param first first item, e.g. expression
   * @param second second item, e.g. expression
   * @return <code>true</code> if both are <code>null</code> or both {@link #isSame}
   */
  public static final boolean isBothNullOrSame(
      final BinItem first, final BinItem second) {
    if (first == null) {
      return second == null;
    } else {
      if (second == null) {
        return false;
      }

      return first.isSame(second);
    }
  }

  /** Helper to compare sometimes missing items.
   * @param first first item, e.g. expression
   * @param second second item, e.g. expression
   * @return <code>true</code> if both are <code>null</code> or both {@link #isSame}
   */
  public static final boolean isBothNullOrSame(
      final BinTypeRef first, final BinTypeRef second) {
    if (first == null) {
      return second == null;
    } else {
      if (second == null) {
        return false;
      }

      return first.equals(second);
    }
  }

  /**
   * Recursively checks structure and types, but not names.
   * @param other structure
   * @return true if structure matches
   */
  public final boolean isSimilar(BinItem other) {
    if (Assert.enabled) {
      Assert.must(false, "Illegally called on: "
          + ClassUtil.getShortClassName(this));
    }
    return false;
  }

  public BinItemFormatter getFormatter() {
    throw new RuntimeException("Not implemented for: "
        + ClassUtil.getShortClassName(this));
  }

  public final Scope getScope() {
    if (this instanceof Scope) {
      return (Scope)this;
    }

    BinItemVisitable parent = this.getParent();
    while (parent != null && !(parent instanceof Scope)) {
      parent = parent.getParent();
    }

    return (Scope) parent;
  }

  public final BinItemVisitable getParent() {
    return this.parent;
  }

  public void setParent(BinItemVisitable parent) {
    this.parent = parent;
  }

  public BinMember getParentMember() {
    BinItemVisitable parent = this.getParent();
    if (parent == null) {
      return null;
    }
    
    if (BinItemVisitableUtil.isMember(parent)) {
      return (BinMember) parent;
    } else {
      try {
        return ((BinItem) parent).getParentMember();
      } catch (ClassCastException e) {
        return null;
      }
    }
  }

  public BinCIType getParentType() {
    BinItemVisitable parent = this.getParent();
    while (parent != null && !(parent instanceof BinCIType)) {
      parent = parent.getParent();
    }

    return (BinCIType) parent;
  }

  public BinItemReference createReference(){
    throw new UnsupportedOperationException();
  }

  private BinItemVisitable parent;
}
