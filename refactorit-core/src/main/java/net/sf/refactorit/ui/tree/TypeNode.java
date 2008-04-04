/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinEnum;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract node for class/interface.
 * @author Igor Malinin
 * @author Anton Safonov
 */
public class TypeNode extends BranchNode implements SourceNode {
  public static final int ACCESS = 0x03;

  public static final int SHOW_ALL = 0x00;

  public static final int HIDE_PRIVATE = 0x01;

  public static final int HIDE_PACKAGE = 0x02;

  public static final int SHOW_PUBLIC = 0x03;

  public static final int HIDE_FIELDS = 0x04;

  public static final int HIDE_METHODS = 0x08;

  public static final int HIDE_STATIC = 0x10;

  private int filter;

  protected final BinCIType bin;

  protected List members;

  /*
   * Constructor.
   */
  public TypeNode(UITreeNode parent, BinCIType bin) {
    super(parent);
    this.bin = bin;
  }

  public final int getType() {
    if (bin instanceof BinClass) {
      return UITreeNode.NODE_CLASS;
    } else if (bin instanceof BinEnum) {
      return UITreeNode.NODE_ENUM;
    } else {
      return UITreeNode.NODE_INTERFACE;
    }
  }

  public final Object getBin() {
    return bin;
  }

  public final String getDisplayName() {
    return bin.getName();
  }

  public final String getSecondaryText() {
    return null;
  }

  public final boolean matchesFor(String str) {
    String binName = bin.getQualifiedName().toLowerCase();
    return (binName.startsWith(str.toLowerCase()));
  }

  public final BinCIType getBinCIType() {
    return bin;
  }

  public final CompilationUnit getCompilationUnit() {
    return getBinCIType().getCompilationUnit();
  }

  public final UITreeNode getChildAt(int index) {
    return (UITreeNode) getMembers().get(index);
  }

  public final int getChildCount() {
    return getMembers().size();
  }

  public final int getIndexOf(UITreeNode child) {
    return getMembers().indexOf(child);
  }

  public final SourceCoordinate getStart() {
    ASTImpl node = getBinCIType().getOffsetNode();
    if (node == null) {
      return new SourceCoordinate(1, 1);
    }
    return SourceCoordinate.getForAST(ASTUtil.getFirstNodeOnLine(node));

    // return null; // FIXME: Make it work or simply return new
    // SourceCoordinate(1, 1)
  }

  public final SourceCoordinate getEnd() {
    // No highlighting needed
    return null;
  }

  private void initInners(List list) {
    BinTypeRef[] types = bin.getDeclaredTypes();
    if (types == null) {
      return;
    }

    Collections.sort(Arrays.asList(types), MemberComparator.TYPE);

    for (int i = 0, len = types.length; i < len; i++) {
      TypeNode node = new TypeNode(this, types[i].getBinCIType());
      node.filter(filter);
      list.add(node);
    }
  }

  final void initFields(List list) {
    if ((filter & HIDE_FIELDS) != 0) {
      return;
    }

    BinField[] fields = bin.getDeclaredFields();
    if (fields == null) {
      return;
    }

    Collections.sort(Arrays.asList(fields), MemberComparator.FIELD);

    boolean showStatic = (filter & HIDE_STATIC) == 0;
    for (int i = 0, len = fields.length; i < len; i++) {
      BinField field = fields[i];
      if (field.isPreprocessedSource()) {
        continue;
      }

      if (showStatic || !field.isStatic()) {
        if (filter(field, filter & ACCESS)) {
          list.add(new Field(this, field));
        }
      }
    }
  }

  final void initConstructors(List list) {
    if ((filter & HIDE_METHODS) != 0) {
      return;
    }

    if (!(bin instanceof BinClass)) {
      return;
    }

    BinConstructor[] constructors = ((BinClass) bin).getConstructors();
    if (constructors == null) {
      return;
    }

    Collections.sort(Arrays.asList(constructors), MemberComparator.METHOD);

    for (int i = 0, len = constructors.length; i < len; i++) {
      BinConstructor constructor = constructors[i];

      if (filter(constructor, filter & ACCESS)
          && !constructor.isPreprocessedSource()) {
        list.add(new Constructor(this, constructor));
      }
    }
  }

  final void initMethods(List list) {
    if ((filter & HIDE_METHODS) != 0) {
      return;
    }

    // FIXME: here we want methods that have bodies
    BinMethod[] methods = bin.getDeclaredMethods();
    if (methods == null) {
      return;
    }

    Collections.sort(Arrays.asList(methods), MemberComparator.METHOD);

    boolean showStatic = (filter & HIDE_STATIC) == 0;
    for (int i = 0, len = methods.length; i < len; i++) {
      BinMethod method = methods[i];
      if (method.isPreprocessedSource()) {
        // // show JSP methods which are defined in same file
        // if( method.getCompilationUnit()!=getCompilationUnit() ) {
        // continue;
        // }
        continue;
      }

      if (showStatic || !method.isStatic()) {
        if (filter(method, filter & ACCESS)) {
          list.add(new Method(this, method));
        }
      }
    }
  }

  protected List getMembers() {
    // Init lazily
    if (members == null) {
      members = new ArrayList(16);

      initInners(members);
      initFields(members);
      initConstructors(members);
      initMethods(members);
    }

    // Return result
    return members;
  }

  public final void filter(int filter) {
    this.filter = filter;
  }

  public static boolean filter(BinMember member, int access) {
    if (access > 0 && !member.isPublic()) {
      boolean priv = member.isPrivate();
      if (priv) {
        return false;
      }

      boolean prot = member.isProtected();
      if (access >= 3 && prot) {
        return false;
      }

      if (access >= 2 && !(priv || prot)) {
        return false;
      }
    }

    return true;
  }

  public final String toString() {
    return "TypeNode: " + getDisplayName() + " "
        + Integer.toHexString(this.hashCode());
  }

  //
  // Inner classes
  //

  public static final class Field extends Member {
    public Field(UITreeNode parent, BinField binField) {
      super(parent, binField);
    }

    public final int getType() {
      return UITreeNode.NODE_TYPE_FIELD;
    }

    public final String getDisplayName() {
      return BinFormatter.format((BinField) getBinMember());
    }

    public final boolean matchesFor(String str) {
      String binName = getBinMember().getQualifiedName().toLowerCase();
      int lastDot = binName.lastIndexOf('.');

      return (binName.substring(lastDot + 1).startsWith(str.toLowerCase()));
    }
  }

  public static final class Constructor extends Method {
    public Constructor(UITreeNode parent, BinConstructor constructor) {
      super(parent, constructor);
    }

    public final int getType() {
      return UITreeNode.NODE_TYPE_CNSTOR;
    }
  }

  public static class Method extends Member {
    public Method(UITreeNode parent, BinMethod binMethod) {
      super(parent, binMethod);
    }

    public int getType() {
      return UITreeNode.NODE_TYPE_METHOD;
    }

    public final String getDisplayName() {
      return BinFormatter.format((BinMethod) getBinMember());
    }

    public final boolean matchesFor(String str) {
      String binName = getBinMember().getQualifiedName().toLowerCase();
      int lastDot = binName.lastIndexOf('.');

      return (binName.substring(lastDot + 1).startsWith(str.toLowerCase()));
    }
  }

  public abstract static class Member extends AbstractNode implements
      SourceNode {
    private BinMember member = null;

    public Member(UITreeNode parent, BinMember member) {
      super(parent);

      this.member = member;
    }

    public final Object getBin() {
      return getBinMember();
    }

    public final String getSecondaryText() {
      return null;
    }

    public final CompilationUnit getCompilationUnit() {
      return getBinMember().getOwner().getBinCIType().getCompilationUnit();
    }

    public final SourceCoordinate getStart() {
      ASTImpl node = getBinMember().getOffsetNode();

      // Compose result
      return (node != null ? SourceCoordinate.getForAST(ASTUtil
          .getFirstNodeOnLine(node)) : null);
    }

    public final SourceCoordinate getEnd() {
      return null; // TODO
    }

    public final BinMember getBinMember() {
      return member;
    }

    public final String toString() {
      return "Member node: " + this.getDisplayName() + " "
          + Integer.toHexString(this.hashCode());
    }

    public final boolean equals(Object o) {
      return (super.equals(o) && getBinMember().equals(
          ((Member) o).getBinMember()));
    }
  }
}
