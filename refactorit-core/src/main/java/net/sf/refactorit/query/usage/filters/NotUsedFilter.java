/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;


import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.refactorings.EjbUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author vadim
 * @author Anton Safonov
 */
public final class NotUsedFilter {
  private final boolean publicAndProtectedMembersExcluded;
  private final boolean notUsedAsInterface;
  private final List excludes = new ArrayList();

  public NotUsedFilter(final boolean isPublicAndProtectedExcluded,
      final boolean isNotUsedAsInterface) {

    this.publicAndProtectedMembersExcluded = isPublicAndProtectedExcluded;
    this.notUsedAsInterface = isNotUsedAsInterface;
  }

  public final boolean isPublicAndProtectedMembersExcluded() {
    return publicAndProtectedMembersExcluded;
  }

  public final boolean isNotUsedAsInterface() {
    return notUsedAsInterface;
  }

  public final List getExcludeNames() {
    return this.excludes;
  }

  public final void setExcludeNames(final List newExcludes) {
    this.excludes.clear();
    this.excludes.addAll(newExcludes);
  }

  /**
   * Determines whether the member should be excluded from "not used" list
   *
   * @param a class member a member (method or field)
   * @return true if the member should be excluded
   **/
  public final boolean isToBeExcluded(final BinMember member) {
    if (this.publicAndProtectedMembersExcluded &&
        (member.isPublic() || member.isProtected())) {
      return true;
    }

    if (member instanceof BinConstructor) {
      if (((BinConstructor) member).isSynthetic()) {
        return true;
      }
    } else if (member instanceof BinMethod
        && ((BinMethod) member).isMain()) {
      return true;
    }

    return this.excludes.contains(member.getName());
  }

  public static final boolean isMarkedAsUsed(final BinMember member) {
//    if (member.isPrivate()) {
//      return false;
//    }

    final JavadocComment jdoc = Comment.findJavadocFor(member);
    if (jdoc != null && jdoc.getText().toLowerCase().indexOf("@used") != -1) {
      return true;
    }

    if (member instanceof BinMethod) {
      final BinMethod method = (BinMethod) member;
      if (EjbUtil.isEjbMethod(method) || EjbUtil.isServletMethod(method)) {
        return true;
      }
    }

    return false;
  }
}
