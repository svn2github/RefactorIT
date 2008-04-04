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

import java.util.ArrayList;


/**
 *
 *
 * @author Tonis Vaga
 */
public class InstanceNotAccessibleConflict extends UnresolvableConflict {
  static final ConflictType conflictType = ConflictType.INSTANCE_NOT_ACCESSIBLE;

  public InstanceNotAccessibleConflict(
      ConflictResolver resolver, BinMember upMember
      ) {
    super(resolver,
        InstanceNotAccessibleConflict.conflictType, upMember, new ArrayList());
  }

  public ConflictType getType() {
    return conflictType;
  }

  public String getDescription() {
    return "Target instance isn't accessible for all usages";
  }
}
