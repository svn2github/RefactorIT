/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.source.edit.CompoundASTImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



/**
 * Member variables or methods. Classes and interfaces are also members, e.g.
 * they can be inners.
 */
public abstract class BinMember extends AbstractLocationAware {
  public static final class QualifiedNameSorter implements Comparator {
    public final int compare(final Object o1, final Object o2) {
      return ((BinMember) o1).getQualifiedName()
          .compareTo(((BinMember) o2).getQualifiedName());
    }
  }


//  private CompilationUnit compilationUnit;

  private String name;
  private int modifiers;

  private BinTypeRef owner;

  /* The member's declarator */
  private int offsetNode = -1;

  protected BinMember() {
  }

  protected BinMember(
      final String name, final int modifiers, final BinTypeRef owner) {
    this.name = name;
    this.modifiers = modifiers;
    this.owner = owner;
  }

  /**
   * @return the modifiers of this field
   */
  public final int getModifiers() {
    return modifiers;
  }

  public final void setModifiers(final int modifiers) {
    this.modifiers = modifiers;
  }

  public final int getAccessModifier() {
    return getModifiers() & BinModifier.PRIVILEGE_MASK;
  }

  /**
   * Returns the owner of this member
   */
  public BinTypeRef getOwner() {
//    BinCIType ownerType = getParentType();
//    if (ownerType == null) {
//      Assert.must(false, "Asked getOwner before parent is set: " + this.name);
//    }
//    return ownerType.getTypeRef();
    return this.owner;
  }

  public final void setOwner(final BinTypeRef owner) {
    this.owner = owner;
  }

  /**
   * @return the fully qualified name of this member
   */
  public String getQualifiedName() {
    if (getOwner() != null) {
      return getOwner().getQualifiedName() + '.' + getName();
    } else {
      return "<unknown owner>." + getName();
    }
  }

  /**
   * FIXME: could be better make separate type detector along with other pretty
   * namers?
   *
   * @return e.g. "field", "method", "class" etc.
   */
  public abstract String getMemberType();

  public String getName() {
    return name;
  }

  public final String getNameWithAllOwners() {
    BinTypeRef owner = getOwner();
    if (owner == null) {
      return getName();
    }

    char delim = (this instanceof BinType) ? '$' : '.';
    String ownerName = owner.getBinCIType().getNameWithAllOwners();
    return ownerName + delim + getName();
  }

  protected void setName(final String name) {
    this.name = name;
  }

  /**
   * @return	true if this member is static
   */
  public final boolean isStatic() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.STATIC);
  }

  /**
   * @return	true if this member is public
   */
  public final boolean isPublic() {
    return (modifiers & BinModifier.PUBLIC) == BinModifier.PUBLIC;
  }

  /**
   * @return	true if this member is package private
   */
  public final boolean isPackagePrivate() {
    return!BinModifier.hasFlag(getModifiers(), BinModifier.PUBLIC)
        && !BinModifier.hasFlag(getModifiers(), BinModifier.PROTECTED)
        && !BinModifier.hasFlag(getModifiers(), BinModifier.PRIVATE);
  }

  public final boolean isNative() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.NATIVE);
  }

  /**
   * @return	true if this member is protected
   */
  public final boolean isProtected() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.PROTECTED);
  }

  /**
   * @return	true if this member is private
   */
  public final boolean isPrivate() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.PRIVATE);
  }

  /**
   * @return	true if this member is final
   */
  public final boolean isFinal() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.FINAL);
  }

  /**
   * @return	true if this member is abstract
   */
  public final boolean isAbstract() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.ABSTRACT);
  }

  public void accept(final net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return null if the name is implied, i.e. the name is
   * not actually present in the source, happens in case of binary members
   * or copied methods.
   */
  public abstract ASTImpl getNameAstOrNull();

  /**
   * Store member's offset
   */
  public final ASTImpl getOffsetNode() {
    final CompilationUnit src = getCompilationUnit();
    if (src == null || src.getSource() == null) {
      return null;
    }
    return src.getSource().getASTByIndex(this.offsetNode);
  }

  public final void setOffsetNode(final ASTImpl declarator) {
    this.offsetNode = ASTUtil.indexFor(declarator);
  }

  public final boolean hasCoordinates() {
    return getCompilationUnit() != null && this.offsetNode != -1;
  }

  public final ASTImpl getModifiersNode() {
    ASTImpl node
        = ASTUtil.getFirstChildOfType(getOffsetNode(), JavaTokenTypes.MODIFIERS);

    if (node != null && node.getFirstChild() != null) {
      node = new CompoundASTImpl(node);
      node.setText(getCompilationUnit().getSource().getText(node));
      return node;
    }

    final ASTImpl place = ASTUtil.getFirstNodeOnLine(getOffsetNode());
    // let's make virtual node
    final ASTImpl packagePrivateNode = new SimpleASTImpl(0, "");
    packagePrivateNode.setStartLine(place.getStartLine());
    packagePrivateNode.setStartColumn(place.getStartColumn());
    packagePrivateNode.setEndLine(place.getStartLine());
    packagePrivateNode.setEndColumn(place.getStartColumn());
    return packagePrivateNode;
  }


  public final List getModifierNodes() {
    final List modifierNodes = new ArrayList(3);

    ASTImpl node
        = ASTUtil.getFirstChildOfType(getOffsetNode(), JavaTokenTypes.MODIFIERS);

    if (node != null) {
      node = (ASTImpl) node.getFirstChild();
    }

    while (node != null) {
      modifierNodes.add(node);
      node = (ASTImpl) node.getNextSibling();
    }

    return modifierNodes;
  }

  public final ASTImpl getVisibilityNode() {
    final List nodes = getModifierNodes();
    for (int i = 0, max = nodes.size(); i < max; i++) {
      final ASTImpl node = (ASTImpl) nodes.get(i);
      switch (node.getType()) {
        case JavaTokenTypes.LITERAL_public:
        case JavaTokenTypes.LITERAL_protected:
        case JavaTokenTypes.LITERAL_private:
          return node;
        default:
      }
    }

    final ASTImpl place = ASTUtil.getFirstNodeOnLine(getOffsetNode());
    // let's make virtual node
    final ASTImpl packagePrivateNode = new SimpleASTImpl(0, "");
    packagePrivateNode.setStartLine(place.getStartLine());
    packagePrivateNode.setStartColumn(place.getStartColumn());
    packagePrivateNode.setEndLine(place.getStartLine());
    packagePrivateNode.setEndColumn(place.getStartColumn());
    return packagePrivateNode;
  }

  /**
   * Gets top-level type (JLS 7.6) in which this member is enclosed.
   *
   * @return top-level enclosing type or <code>null</code> if this member
   * is not enclosed in a top-level type (for example, primitive types are not).
   */
  public BinCIType getTopLevelEnclosingType() {
    if (getOwner() == null) {
      if (this instanceof BinCIType) {
        return (BinCIType) this;
      } else {
        return null;
      }
    }

    BinCIType topLevelEnclosingType = getOwner().getBinCIType();
    while (topLevelEnclosingType.isInnerType()) {
      topLevelEnclosingType =
          topLevelEnclosingType.getOwner().getBinCIType();
    }

    return topLevelEnclosingType;
  }

  /**
   * Gets string representation of this member.
   *
   * @return string representation.
   */
  public String toString() {
    final String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": \""
        + getName() + "\", "
        + getCompilationUnit() + ", "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + ", "
        + Integer.toHexString(hashCode());

  }

  /**
   * Gets source file where this item is located.
   *
   * @return source file or <code>null</code> if it is not known.
   */
  public CompilationUnit getCompilationUnit() {
    BinCIType parentType = getParentType();
    if (Assert.enabled && parentType == null) {
      Assert.must(false,
          "Member has no parent type: " + this.getQualifiedName());
    }
    return parentType.getCompilationUnit();
  }

  public BinPackage getPackage() {
    BinTypeRef ownerType = getOwner();
    return (ownerType != null) ? ownerType.getPackage() : null;
  }

  public Project getProject() {
    try {
      return getPackage().getProject();
    } catch (NullPointerException e) {
      try {
        return getOwner().getProject(); // seems package can be null, but when?
      } catch (NullPointerException e2) {
        return null; // totally disconnected member? strange
      }
    }
  }

  /**
   * Gets first line of this item in the source file.
   *
   * @return line or <code>0</code> if line not known.
   */
  public final int getStartLine() {
    final ASTImpl node = getOffsetNode();
    return (node == null) ? 0 : node.getLine();
  }

  /**
   * Gets last line of this item in the source file.
   *
   * @return line or <code>0</code> if line not known.
   */
  public final int getEndLine() {
    final ASTImpl node = getOffsetNode();
    return (node == null) ? 0 : node.getEndLine();
  }

  /**
   * Gets first column of the first line taken by this item in the source file.
   *
   * @return column or <code>0</code> if column not known.
   */
  public final int getStartColumn() {
    final ASTImpl node = getOffsetNode();
    return (node == null) ? 0 : node.getColumn();
  }

  /**
   * Gets last column of the last line taken by this item in the source file.
   *
   * @return column or <code>0</code> if column not known.
   */
  public final int getEndColumn() {
    final ASTImpl node = getOffsetNode();
    return (node == null) ? 0 : node.getEndColumn();
  }

  // *** END LocationAware interface implementation ***


  // FIXME: shouldn't the parameter "invokedOn" be a BinType?
  // For example: new String[5].clone();
  // I disagree -- string arrays are objects, too, for example. You can't invoke
  // a member that is not (owned by) a class or interface, right?
  /**
   * Checks whether member of a type is accessible from from the specified type.
   * See JLS 6.6.
   *
   * NB! doesn't work correctly with protected access. For that use
   * {@link isProtectedAccessible}
   *
   * @param invokedOn compile-time type on which the method is invoked.
   * @param context type from which the access is checked.
   *
   * @return <code>true</code> if and only if the member is accessible,
   *         <code>false</code> otherwise.
   */
  public final boolean isAccessible(final BinCIType invokedOn,
      final BinCIType context) {
    // JLS 6.6.1:
    // A member (class, interface, field, or method) of a reference
    // (class, interface, or array) type or a constructor of a class type is
    // accessible only if the type is accessible and the member or constructor
    // is declared to permit access:
    // * If the member or constructor is declared public, then access is
    //   permitted. All members of interfaces are implicitly public.
    // * Otherwise, if the member or constructor is declared protected, then
    //   access is permitted only when one of the following is true:
    //   * Access to the member or constructor occurs from within the package
    //     containing the class in which the protected member or constructor
    //     is declared.
    //   * Access is correct as described in §6.6.2.
    // * Otherwise, if the member or constructor is declared private, then
    //   access is permitted if and only if it occurs within the body of the
    //   top level class (§7.6) that encloses the declaration of the member.
    // * Otherwise, we say there is default access, which is permitted only when
    //   the access occurs from within the package in which the type is
    //   declared.
    return isAccessible(invokedOn, context, getOwner() == null
        ? null : getOwner().getBinCIType());
  }

  /**
   * This method used when member is being moved to a new owner,
   * but the member itself contains an old owner still.
   *
   * @param invokedOn
   * @param context
   * @param givenOwner a new owner, accessibility will be checked as if the
   *   member belongs to this type already
   * @return true when accessible
   */
  private boolean isAccessible(final BinCIType invokedOn,
      final BinCIType context,
      final BinCIType givenOwner) {
    if (invokedOn == context && context == givenOwner) {
      return true; // same type same context
    }

    if (!invokedOn.isAccessible(context)) {
      return false; // Type not accessible
    }

    if ((this.isPublic())
        || (invokedOn.isInterface())
        || (this.getOwner().getBinCIType().isAnnotation()
        && this.getOwner().getBinCIType().isAccessible(context)) ) {
      return true; // public method
    } else if (this.isProtected()) {
      if (givenOwner.getPackage().isIdentical(context.getPackage())) {
        // Access from same package
        return true;
      }

      // Check for correct access (JLS 6.6.2).

      // JLS 6.6.2.1:
      // Let C be the class in which a protected member m is declared. Access
      // is permitted only within the body of a subclass S of C.

      final BinCIType contextCI = context.getTypeRef().getBinCIType();
      if (context.getTypeRef().isDerivedFrom(givenOwner.getTypeRef())) {

        // FIXME: wrong but hard to fix because of super case

        return true;
      }

      if (contextCI.isInnerType()) {
//        BinTypeRef contextOwner = contextCI.getTypeRef();
        BinTypeRef contextOwner = contextCI.getOwner();
        while (contextOwner != null) {
          if (contextOwner.isDerivedFrom(givenOwner.getTypeRef())) {
            // Access within inner in body of subtype.
            return true;
          }
          contextOwner = contextOwner.getBinCIType().getOwner();
        }
      }

      // JLS 6.6.2.1:
      // In addition, if Id denotes an instance field or instance method, then:
      // * If the access is by a qualified name Q.Id, where Q is an
      //   ExpressionName, then the access is permitted if and only if the type
      //   of the expression Q is S or a subclass of S.
      // * If the access is by a field access expression E.Id, where E is a
      //   Primary expression, or by a method invocation expression E.Id(. . .),
      //   where E is a Primary expression, then the access is permitted if and
      //   only if the type of E is S or a subclass of S.

      // if ()
      // FIXME: Are these checks necessary? Does parser guarantee that these
      //        checks are redundant?

      return false;
    } else if (this.isPrivate()) {
      // Accessible if access occurs from within the body of member's
      // enclosing top level class.

      final BinCIType memberTopLevelEnclosingType =
          givenOwner.getTopLevelEnclosingType();
      final BinCIType contextTopLevelEnclosingType =
          context.getTopLevelEnclosingType();
      final BinCIType invokedOnTopLevelEnclosingType =
          invokedOn.getTopLevelEnclosingType();

      if ((memberTopLevelEnclosingType == contextTopLevelEnclosingType)
          && (contextTopLevelEnclosingType == invokedOnTopLevelEnclosingType)) {

        return true; // member's top-level enclosing type and context's
        // top-level enclosing type are equal.
      } else {
        return false;
      }
    } else {
      // default-access
      // Accessible only from the package it is declared in.

      try {
        if (givenOwner.getPackage().isIdentical(context.getPackage())
            && invokedOn.getPackage().isIdentical(context.getPackage())) {
          // Access from same package
          return true;
        } else {
          return false;
        }
      } catch (NullPointerException e) {
        // Debug code here for bug 1666 -- this bug was impossible to reproduce, so
        // if it ever happens again we'll at least know which of the variables is causing it.

        final int code;
        if (givenOwner == null) {
          code = 1;
        } else if (givenOwner.getPackage() == null) {
          code = 3;
        } else if (context == null) {
          code = 4;
        } else if (context.getPackage() == null) {
          code = 5;
        } else if (invokedOn == null) {
          code = 6;
        } else if (invokedOn.getPackage() == null) {
          code = 7;
        } else {
          code = 0;
        }
        final RuntimeException exx = new ChainableRuntimeException(
            "PLEASE REPORT RefactorIT Exception: CODE " + code, e);
        exx.printStackTrace();
        e.printStackTrace();
        throw exx;
      }
    }
  }

  public final boolean isOwnedByOuterTypeOf(BinCIType type) {
    BinCIType owner = getOwner().getBinCIType();
    if (owner == type) {
      return false;
    } else {
      return BinItemVisitableUtil.contains(owner, type);
    }
  }

  public final boolean locationOverlapsWithAstLocation(final ASTImpl ast) {
    return
        ast.getStartLine() >= getStartLine() &&
        ast.getEndLine() <= getEndLine() &&
        (!(ast.getStartLine() == getStartLine()
        && ast.getEndColumn() < getStartColumn())) &&
        (!(ast.getEndLine() == getEndLine()
        && ast.getStartColumn() > getEndColumn()));
  }

  protected void cleanForPrototype() {
    // name is kept
    owner = null;
    modifiers = 0;
    offsetNode = -1;
//    compilationUnit = null;
  }

  public final boolean isProtectedAccessible(
      BinCIType invokedOn, BinCIType context, boolean superCall
      ) {
    if (!isProtected()) {
      return true;
    }

    if (context.getPackage() == getOwner().getPackage()) {
      return true; // access in same package is always allowed
    }

    if (!context.getTypeRef().isDerivedFrom(getOwner())) {
      return false;
    }

    if (!invokedOn.getTypeRef().isDerivedFrom(context.getTypeRef())
        && !superCall) {
      return false;
    }

    return true;
  }

  public final boolean isSame(BinItem other) {
    if (other instanceof BinMember) {
      // should be enough for the beginning to make it a bit strict
      return (this == other);
    }

    return false;
  }
}
