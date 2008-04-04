/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.javadoc.Javadoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author vadim
 */
public final class JavadocComment extends Comment {
  public static final int SEE_TAG = 1;
  public static final int LINK_TAG = 2;
  public static final int THROWS_TAG1 = 4;
  public static final int THROWS_TAG2 = 8;
  public static final int PARAM_TAG = 16;
  public static final int RETURN_TAG = 32;
  public static final int DEPRECATED_TAG = 64;
  public static final int AUDIT_TAG = 128; // TEMP: For internal use only!

  private transient Javadoc javadoc = null;

  // for serialization
  public JavadocComment() {
    super();
  }

  public void readExternal(java.io.ObjectInput s) throws ClassNotFoundException,
      IOException {
    super.readExternal(s);
  }

  public void writeExternal(java.io.ObjectOutput s) throws IOException {
    super.writeExternal(s);
  }

  public JavadocComment(String body, int line, int column) {
    super(body, line, column);
  }

  public JavadocComment(String body, int line, int column,
      int endLine, int endColumn) {
    super(body, line, column, endLine, endColumn);
  }

  public void visit(BinItemVisitor visitor) {
    if (!(visitor instanceof AbstractIndexer)) {
      return;
    }

    BinTypeRef currentType = null;

    List types = getCompilationUnit().getIndependentDefinedTypes();
    currentType = findTypeForComment(types);
    if (currentType == null) {
      return;
    }

    ((AbstractIndexer) visitor).setCurrentType(currentType);
    if (javadoc == null) {
      javadoc = Javadoc.parseIntoFakeClassmodel(this, currentType,
          getCompilationUnit(), getStartColumn() - 1);
    }
    if (javadoc != null) {
      javadoc.accept(visitor);
    }
    ((AbstractIndexer) visitor).setCurrentType(null);
    ((AbstractIndexer) visitor).setCurrentLocation(null);
  }

  public void invalidateCache() {
    this.javadoc = null;
  }

  private BinTypeRef findTypeForComment(List types) {
    BinTypeRef ref;
    BinTypeRef[] inners;
    BinTypeRef currentType = null;

    int closest = -1;

    for (int i = 0, max = types.size(); i < max; i++) {
      ref = (BinTypeRef) types.get(i);

      if (commentIsBeforeType(ref.getBinCIType(), this)) {
        if (closest == -1) {
          closest = ref.getBinCIType().getStartLine();
          currentType = ref;
        } else {
          int newClosest = ref.getBinCIType().getStartLine();

          if (newClosest < closest) {
            closest = newClosest;
            currentType = ref;
          }
        }
      } else if (commentIsInsideType(ref.getBinCIType(), this)) {
        inners = ref.getBinCIType().getDeclaredTypes();

        if (inners.length == 0) {
          currentType = ref;
        } else {
          currentType = findTypeForComment(Arrays.asList(inners));

          if (currentType == null || notOnlyEmptyLines(ref, currentType)) {
            currentType = ref;
          }
        }
        break;
      }
    }

    return currentType;
  }

  private boolean notOnlyEmptyLines(BinTypeRef owner, BinTypeRef ref) {
    int commentLine = getEndLine();
    int typeLine = ref.getBinCIType().getStartLine();
    int memberLine;

    List members = getAllMembers(owner);

    for (int i = 0, max = members.size(); i < max; i++) {
      memberLine = ((BinMember) members.get(i)).getStartLine();
      if (memberLine >= commentLine && memberLine <= typeLine) {
        return true;
      }
    }

    return false;
  }

  private List getAllMembers(BinTypeRef owner) {
    List members = new ArrayList();
    BinCIType type = owner.getBinCIType();

    members.addAll(Arrays.asList(type.getDeclaredFields()));
    members.addAll(Arrays.asList(type.getDeclaredMethods()));

    if (type.isClass()) {
      members.addAll(Arrays.asList(((BinClass) type).getConstructors()));
    }

    return members;
  }

  private boolean commentIsInsideType(LocationAware type, Comment comment) {
    if ((comment.getStartLine() >= type.getStartLine()) &&
        comment.getEndLine() <= type.getEndLine()) {
      return true;
    }
    return false;
  }

  private boolean commentIsBeforeType(LocationAware type, Comment comment) {
    if (comment.getEndLine() <= type.getStartLine()) {
      return true;
    }

    return false;
  }

}
