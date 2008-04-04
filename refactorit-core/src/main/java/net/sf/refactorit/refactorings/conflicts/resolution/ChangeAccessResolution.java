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
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessUtil;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.ModifierEditor;

import java.util.HashMap;
import java.util.List;


/**
 *
 * @author vadim
 */
public class ChangeAccessResolution extends ConflictResolution {
  private int defaultAccess = -1;
  private boolean isMemberMoves = false;
  private List downMembers;
  private HashMap newAccesses = new HashMap();

  public ChangeAccessResolution(BinMember member, int defaultAccess) {
    this(CollectionUtil.singletonArrayList(member));

    this.defaultAccess = defaultAccess;
  }

  public ChangeAccessResolution(BinMember member, boolean isMemberMoves) {
    this(CollectionUtil.singletonArrayList(member));

    this.isMemberMoves = isMemberMoves;
  }

  public ChangeAccessResolution(BinMember member, boolean isMemberMoves,
      HashMap extraImports) {
    this(CollectionUtil.singletonArrayList(member), extraImports);

    this.isMemberMoves = isMemberMoves;
  }

  public ChangeAccessResolution(List downMembers) {
    this(downMembers, null);
  }

  public ChangeAccessResolution(List downMembers, HashMap extraImports) {
    super(extraImports);

    this.downMembers = downMembers;
    addNewAccesses(this.downMembers, -1);
  }

  private void addNewAccesses(List downMembers, int newAccess) {
    for (int i = 0, max = downMembers.size(); i < max; i++) {
      BinMember member = (BinMember) downMembers.get(i);
      if (newAccess == -1) {
        newAccess = member.getAccessModifier();
      }
      addNewAccess(member, newAccess);
    }
  }

  private void addNewAccess(BinMember member, int newAccess) {
    newAccesses.put(member, new Integer(newAccess));
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    if (!isResolved()) {
      return new Editor[0];
    }

    Editor[] editor = new Editor[downMembers.size()];

    for (int i = 0, max = downMembers.size(); i < max; i++) {
      BinMember member = (BinMember) downMembers.get(i);

      int newAccess = getNewAccessModifier(member);
      if (newAccess != member.getAccessModifier()) {
        editor[i] = new ModifierEditor(member,
            BinModifier.setFlags(member.getModifiers(),
            newAccess));
      } else {
        editor = new Editor[0];
        new Exception("access modifier for member hasn't changed").
            printStackTrace();
        break;
      }
    }

    return editor;
  }

  public String getDescription() {
    return "Change access modifier of the following members";
  }

  public void runResolution(ConflictResolver resolver) {
    if (defaultAccess == -1) {
      int newAccess;
      for (int i = 0, max = downMembers.size(); i < max; i++) {
        BinMember member = (BinMember) downMembers.get(i);

        if (isMemberMoves) {
          newAccess = MinimizeAccessUtil.getNewAccessForMember(
              member, resolver.getTargetType(),
              Finder.getInvocations(member));
        } else {
          List invocationData = CollectionUtil.singletonArrayList(new InvocationData(
              member,
              resolver.getTargetType().getTypeRef(), member.getOffsetNode()));
          newAccess = MinimizeAccessUtil.getNewAccessForMember(member,
              member.getOwner().getBinCIType(),
              invocationData);
        }

        addNewAccess(member, newAccess);
      }
    } else {
      addNewAccesses(downMembers, defaultAccess);
    }

    setIsResolved(true);
  }

  public String toString() {
    return "ChangeAccessResolution";
  }

  public int getNewAccessModifier(BinMember member) {
    if (newAccesses.keySet().contains(member)) {
      return ((Integer) newAccesses.get(member)).intValue();
    }

    return -1;
  }

  public List getDownMembers() {
    return downMembers;
  }
}
