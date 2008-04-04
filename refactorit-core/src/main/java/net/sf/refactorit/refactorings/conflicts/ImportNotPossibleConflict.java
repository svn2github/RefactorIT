/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.source.format.BinFormatter;


/**
 * @author vadim
 */
public class ImportNotPossibleConflict extends UnresolvableConflict {
  private static final ConflictType conflictType = ConflictType.
      IMPORT_NOT_POSSIBLE;

  private BinTypeRef typeToImport;

  public ImportNotPossibleConflict(BinMember upMember, BinCIType targetType,
      BinTypeRef typeToImport) {
    super(upMember, targetType);

    this.typeToImport = typeToImport;
  }

  public String getDescription() {
    return "Cannot import " + BinFormatter.formatQualified(getTypeToImport()) +
        " into the following types";
  }

  public ConflictType getType() {
    return ImportNotPossibleConflict.conflictType;
  }

  public BinTypeRef getTypeToImport() {
    return this.typeToImport;
  }
}
