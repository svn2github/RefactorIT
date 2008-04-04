/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts.resolution;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author vadim
 */
public class CreateDefinitionResolution extends ConflictResolution {
  private BinMember upMember;

  public CreateDefinitionResolution(BinMember upMember) {
    this.upMember = upMember;
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    return new Editor[0];
  }

  public String getDescription() {
    return "Move definition of " + BinFormatter.format(upMember)
        + " into target type";
  }

  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
  }

  public String toString() {
    return "CreateDefinitionResolution";
  }

  public List getDownMembers() {
    return new ArrayList();
  }

}
