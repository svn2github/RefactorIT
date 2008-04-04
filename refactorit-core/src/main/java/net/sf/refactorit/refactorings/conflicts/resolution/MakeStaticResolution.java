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
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.refactorings.conflicts.ConflictData;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.HashMap;
import java.util.List;


/**
 *
 * @author vadim
 */
public class MakeStaticResolution extends ConflictResolution {
  private BinMember upMember;
  private List downMembers;

  public MakeStaticResolution(BinMember upMember, List downMembers) {
    this(upMember, downMembers, new HashMap());
  }

  public MakeStaticResolution(BinMember upMember, List downMembers,
      HashMap imports) {
    super(imports);

    this.upMember = upMember;
    this.downMembers = downMembers;
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    if (!isResolved()) {
      return new Editor[0];
    }

    Editor[] editor = new Editor[1];
    int newAccess = resolver.targetBelongsToNative()
        ? upMember.getAccessModifier()
        : BinModifier.PUBLIC;

    editor[0] = new ModifierEditor(upMember,
        BinModifier.setFlags(upMember.getModifiers(),
        newAccess | BinModifier.STATIC));

    return editor;
  }

  public String getDescription() {
    return "Make " + BinFormatter.format(upMember) +
        " static because is used by the following members";
  }

  public String toString() {
    return "MakeStaticResolution";
  }

  public void runResolution(ConflictResolver resolver) {
    List usesList = resolver.getConflictData(upMember).getUsesList();
    boolean isMethodHasInstanceOfTargetType =
        resolver.isMethodHasInstanceOfType(upMember, resolver.getTargetType());
    for (int i = 0, max = usesList.size(); i < max; i++) {
      ConflictData usesData = resolver.getConflictData(usesList.get(i));

      if ((usesData != null) && usesData.isSelectedToMove() &&
          !resolver.willBeStatic(usesData) &&
          !isMethodHasInstanceOfTargetType) {
        resolver.makeStaticSinceUsedByStatic(usesData);
      }
    }

    setIsResolved(true);
  }

  public List getDownMembers() {
    return downMembers;
  }
}
