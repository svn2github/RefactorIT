/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts;

/**
 *
 * @author vadim
 */
public class ConflictType {
  public static final ConflictType OVERRIDES = new ConflictType(1);
  public static final ConflictType OVERRIDEN = new ConflictType(2);
  public static final ConflictType ALREADY_DEFINED = new ConflictType(3);
  public static final ConflictType UNMOVABLE_CANNOT_ACCESS = new ConflictType(4);
  public static final ConflictType UNMOVABLE_CANNOT_BE_ACCESSED = new
      ConflictType(5);
  public static final ConflictType WEAK_ACCESS_FOR_ABSTRACT = new ConflictType(
      6);
  public static final ConflictType NOT_PUBLIC_FOR_INTERFACE = new ConflictType(
      7);
  public static final ConflictType USES = new ConflictType(8);
  public static final ConflictType USED_BY = new ConflictType(9);
  public static final ConflictType MOVE_NOT_POSSIBLE = new ConflictType(10);
  public static final ConflictType IMPLEMENTATION_NEEDED = new ConflictType(11);
  public static final ConflictType DECLARATION_OR_DEFINITION = new ConflictType(
      12);
  public static final ConflictType STATIC_METHOD_INTO_INTERFACE = new
      ConflictType(13);
  public static final ConflictType NOT_STATIC_FIELD_INTO_INTERFACE = new
      ConflictType(14);
  public static final ConflictType CREATE_ONLY_DECLARATION = new ConflictType(
      15);
  public static final ConflictType IMPORT_NOT_POSSIBLE = new ConflictType(16);
  public static final ConflictType ASSIGNMENT_FOR_FINAL = new ConflictType(17);
  public static final ConflictType USED_INSTANCE_FIELD = new ConflictType(18);
  public static final ConflictType MAKE_STATIC = new ConflictType(19);
  public static final ConflictType FIELD_INIT_IS_COMPLEX_EXPR = new
      ConflictType(20);
  public static final ConflictType MOVE_USE_ALSO = new ConflictType(21);
  public static final ConflictType MOVE_USEDBY_ALSO = new ConflictType(22);
  public static final ConflictType MAIN_INTO_WRONG_CLASS = new ConflictType(23);
  public static final ConflictType CHANGED_FUNCTIONALITY = new ConflictType(24);
  public static final ConflictType USED_ON_COMPLEX = new ConflictType(25);
  public static final ConflictType USES_FOREIGN_LOCAL_VARIABLES = new
      ConflictType(26);
  public static final ConflictType ABSTRACT_METHOD_TO_CLASS = new ConflictType(
      27);
  public static final ConflictType UNMOVABLE_CANNOT_ACCES_TARGET = new
      ConflictType(28);
  public static final ConflictType INSTANCE_NOT_ACCESSIBLE = new ConflictType(
      29);
  public static final ConflictType DELETE_IMPLEMENTATIONS_IN_SUBCLASSES = new ConflictType(30);
  public static final ConflictType METHD_TO_FOREIGN_TARGET = new ConflictType(31);


  private final int conflictType;

  public ConflictType(int conflictType) {
    this.conflictType = conflictType;
  }

  public boolean equals(final Object object) {
    if (object instanceof ConflictType) {
      return ((ConflictType) object).conflictType == this.conflictType;
    }
    return false;
  }

  public int hashCode() {
    return conflictType;
  }

  public String toString() {
    switch (conflictType) {
      case 1:
        return "OVERRIDES";
      case 2:
        return "OVERRIDEN";
      case 3:
        return "ALREADY_DEFINED";
      case 4:
        return "UNMOVABLE_CANNOT_ACCESS";
      case 5:
        return "UNMOVABLE_CANNOT_BE_ACCESSED";
      case 6:
        return "WEAK_ACCESS_FOR_ABSTRACT";
      case 7:
        return "NOT_PUBLIC_FOR_INTERFACE";
      case 8:
        return "USES";
      case 9:
        return "USED_BY";
      case 10:
        return "MOVE_NOT_POSSIBLE";
      case 11:
        return "IMPLEMENTATION_NEEDED";
      case 12:
        return "DECLARATION_OR_DEFINITION";
      case 13:
        return "STATIC_METHOD_INTO_INTERFACE";
      case 14:
        return "NOT_STATIC_FIELD_INTO_INTERFACE";
      case 15:
        return "CREATE_ONLY_DECLARATION";
      case 16:
        return "IMPORT_NOT_POSSIBLE";
      case 17:
        return "ASSIGNMENT_FOR_FINAL";
      case 18:
        return "USED_INSTANCE_FIELD";
      case 19:
        return "MAKE_STATIC";
      case 20:
        return "FIELD_INIT_IS_COMPLEX_EXPR";
      case 21:
        return "MOVE_USE_ALSO";
      case 22:
        return "MOVE_USEDBY_ALSO";
      case 23:
        return "MAIN_INTO_WRONG_CLASS";
      case 24:
        return "CHANGED_FUNCTIONALITY";
      case 25:
        return "USED_ON_COMPLEX";
      case 26:
        return "USES_FOREIGN_LOCAL_VARIABLES";
      case 27:
        return "ABSTRACT_METHOD_TO_CLASS";
      case 28:
        return "UNMOVABLE_CANNOT_ACCES_TARGET";
      case 29:
        return "INSTANCE_NOT_ACCESSIBLE";
      case 30:
        return "DELETE_IMPLEMENTATIONS_IN_SUBCLASSES";
      case 31:
        return "METHD_TO_FOREIGN_TARGET";
      default:
        return "" + conflictType;
    }
  }
}
