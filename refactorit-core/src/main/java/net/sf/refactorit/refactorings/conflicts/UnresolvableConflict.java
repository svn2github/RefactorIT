/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.List;


/**
 *
 * @author vadim
 */
public class UnresolvableConflict extends UpDownMemberConflict {
  private ConflictType conflictType;
  private ConflictResolver resolver;
  private boolean isObsolete;

  public UnresolvableConflict(BinMember upMember, List downMembers) {
    super(upMember, downMembers);
  }

  public UnresolvableConflict(BinMember upMember, BinMember downMember) {
    this(upMember, CollectionUtil.singletonArrayList(downMember));
  }

  public UnresolvableConflict(ConflictResolver resolver,
      ConflictType conflictType,
      BinMember upMember, List downMembers) {
    super(upMember, downMembers);

    this.conflictType = conflictType;
    this.resolver = resolver;
  }

  public ConflictType getType() {
    return conflictType;
  }

  public int getSeverity() {
    return RefactoringStatus.ERROR;
  }

  public String getDescription() {
    if (conflictType == ConflictType.OVERRIDES) {
      return "Cannot move \'" + BinFormatter.format(getUpMember()) +
          "\' since it overrides or implements the following methods";
    }

    if (conflictType == ConflictType.OVERRIDEN) {
      return "Cannot move \'" + BinFormatter.format(getUpMember()) +
          "\' since it is overridden by following methods";
    }

    if (conflictType == ConflictType.ALREADY_DEFINED) {
      return "\'" + BinFormatter.format(getUpMember())
          + "\' is already defined in \'"
          + BinFormatter.formatQualified(resolver.getTargetType()) + "\'";
    }

    if (conflictType == ConflictType.USED_INSTANCE_FIELD) {
      return "Can not move an instance field \'"
          + BinFormatter.format(getUpMember())
          + "\' which is used in";
    }

    if (conflictType == ConflictType.MOVE_NOT_POSSIBLE) {
      return "Due to unresolvable conflicts member cannot be moved";
    }

    if (conflictType == ConflictType.ASSIGNMENT_FOR_FINAL) {
      return "Cannot move \'" + BinFormatter.format(getUpMember()) +
          "\' into interface since there are classes which change its value";
    }

    if (conflictType == ConflictType.STATIC_METHOD_INTO_INTERFACE) {
      return "Cannot move static method \'" + BinFormatter.format(getUpMember()) +
          "\' into interface";
    }

    if (conflictType == ConflictType.NOT_STATIC_FIELD_INTO_INTERFACE) {
      return "Cannot move non-static field \'"
          + BinFormatter.format(getUpMember()) +
          "\' into interface";
    }

    if (conflictType == ConflictType.FIELD_INIT_IS_COMPLEX_EXPR) {
      return
          "Field, which is initialized with complex expression, cannot be moved";
    }

    if (conflictType == ConflictType.MAIN_INTO_WRONG_CLASS) {
      return "Method " + BinFormatter.format(getUpMember())
          + " must be in the class" +
          " with the same name as source file";
    }

    if (conflictType == ConflictType.USED_ON_COMPLEX) {
      return StringUtil.capitalizeFirstLetter(getUpMember().getMemberType())
          + " " + BinFormatter.format(getUpMember())
          + " is used on complex expression"
          + (getDownMembers().size() <= 1 ? "" : "s");
    }

    if (conflictType == ConflictType.USES_FOREIGN_LOCAL_VARIABLES) {
      return StringUtil.capitalizeFirstLetter(getUpMember().getMemberType())
          + " " +
          BinFormatter.format(getUpMember()) + " from local " +
          BinFormatter.formatQualified(getUpMember().getOwner()) +
          " uses foreign local variables";
    }

    if (conflictType == ConflictType.ABSTRACT_METHOD_TO_CLASS) {
      return "Cannot move abstract method to none-abstract class";
    }

    if (conflictType == ConflictType.UNMOVABLE_CANNOT_ACCES_TARGET) {
      String msg = "Target class ";
      if (getDownMembers().size() == 1) {
        BinMember member = (BinMember) getDownMembers().get(0);
        msg += member.getName() + " ";

      }
      msg += "isn't accessible for all usages";
      return msg;
    }

    if (conflictType == ConflictType.METHD_TO_FOREIGN_TARGET) {
      return "Cannot move: target doesn't belong to the project.";
    }

    return "Unknown conflict";
  }

  public boolean isResolvable() {
    return false;
  }

  public boolean isObsolete() {
    return isObsolete;
  }

  public void resolve() {
  }

  public Editor[] getEditors() {
    return new Editor[0];
  }
}
