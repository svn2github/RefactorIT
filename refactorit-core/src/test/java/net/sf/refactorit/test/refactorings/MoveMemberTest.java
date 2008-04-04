/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.refactorings.conflicts.ConflictData;
import net.sf.refactorit.refactorings.conflicts.ConflictType;
import net.sf.refactorit.refactorings.conflicts.MultipleResolveConflict;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.movemember.MoveMember;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Category;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test driver for {@link net.sf.refactorit.refactorings.movemember.MoveMember}.
 */
public final class MoveMemberTest extends RefactoringTestCase {
  private Project project;
  private MoveMember mover;
  private BinTypeRef nativeRef;
  private BinTypeRef targetRef;

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(MoveMemberTest.class.getName());

  public MoveMemberTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(MoveMemberTest.class);
    suite.setName("MoveMember tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public String getTemplate() {
    return "MoveMember/<stripped_test_name>/<in_out>";
  }

  public void moveResolve(String typeToMoveFrom, String memberName,
      String typeToMoveInto) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    List binMembers = new ArrayList();
    project = getMutableProject();

    nativeRef = project.findTypeRefForName(typeToMoveFrom);

    if (nativeRef == null) {
      nativeRef = findTypeRefForLocalType(typeToMoveFrom);
    }

    if (typeToMoveInto.length() > 0) {
      targetRef = project.findTypeRefForName(typeToMoveInto);
    }

    if (nativeRef == null) {
      throw new RuntimeException("type to move from (" + typeToMoveFrom
          + ") was not found");
    }

    if ((typeToMoveInto.length() > 0) && (targetRef == null)) {
      throw new RuntimeException("type to move into (" + typeToMoveInto
          + ") was not found");
    }

    List notFoundMembers = new ArrayList();
    StringTokenizer tokens = new StringTokenizer(memberName, ",");
    while (tokens.hasMoreTokens()) {
      String nextToken = tokens.nextToken().trim();
      BinMember foundMember = findMember(nativeRef, nextToken);

      if (foundMember == null) {
        notFoundMembers.add(nextToken + " ");
      } else {
        binMembers.add(foundMember);
      }
    }

    if (notFoundMembers.size() > 0) {
      binMembers.clear();
    }

    assertTrue("the following members were not found:" + notFoundMembers,
        (binMembers.size() > 0));

    mover = new MoveMember(new NullContext(project),
        nativeRef.getBinCIType(), binMembers);
    RefactoringStatus status = mover.checkPreconditions();
    assertTrue("preconditions: " + status.getAllMessages(), status.isOk());
    status = mover.checkUserInput();
    assertTrue("user input: " + status.getAllMessages(), status.isOk());

    if (targetRef != null) {
      mover.getResolver().setTargetType(targetRef.getBinCIType());
    }
  }

  private BinTypeRef findTypeRefForLocalType(String typeToMoveFrom) throws
      Exception {
    List localTypes = new ArrayList();
    List definedTypes = project.getDefinedTypes();
    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      BinTypeRef typeRef = (BinTypeRef) definedTypes.get(i);
      BinMethod[] methods = typeRef.getBinCIType().getDeclaredMethods();
      for (int j = 0; j < methods.length; j++) {
        localTypes.addAll(methods[j].getDeclaredTypes());
      }
    }

    for (int i = 0, max = localTypes.size(); i < max; i++) {
      BinCIType localType = (BinCIType) localTypes.get(i);
      if (localType.getQualifiedName().equals(typeToMoveFrom)) {
        return localType.getTypeRef();
      }
    }

    return null;
  }

  private void createFakeResolutionDialog(final Conflict conflict,
      final int resolutionNumber) {
    DialogManager.setInstance(new NullDialogManager() {
      public ConflictResolution getResultFromResolutionDialog(List resolutions) {
        if (conflict instanceof MultipleResolveConflict) {
          return (ConflictResolution) ((MultipleResolveConflict) conflict).
              getPossibleResolutions().get(resolutionNumber);
        } else {
          return conflict.getResolution();
        }
      }

      public int showYesNoHelpQuestion(
          IdeWindowContext context, String text, String helpButtonKey
      ) {
        return showYesNoHelpQuestion(context, null, text, helpButtonKey);
      }

      public int showYesNoHelpQuestion(
          IdeWindowContext context, String key, String text, String helpButtonKey
      ) {
        return JOptionPane.YES_OPTION;
      }

    });
  }

  public void moveResolveConflict(final ConflictType conflictType) {
    moveResolveConflict(conflictType, -1);
  }

  public void moveResolveConflict(final ConflictType conflictType,
      final boolean isMakeStaticConflictWillBeAdded) {
    moveResolveConflict(conflictType, -1, isMakeStaticConflictWillBeAdded);
  }

  public void moveResolveConflict(final ConflictType conflictType,
      final int resolutionNumber) {
    moveResolveConflict(conflictType, resolutionNumber, false);
  }

  public void moveResolveConflict(final ConflictType conflictType,
      final int resolutionNumber,
      final boolean isMakeStaticConflictWillBeAdded) {
    List extraConflictsAdded = new ArrayList();
    List alreadyResolved = new ArrayList();
    HashMap conflictsOfType = new HashMap();
    List noConflictsOfType = new ArrayList();
    List membersToMove = mover.getResolver().getBinMembersToMove();

    upper:
        for (int i = 0, max = membersToMove.size(); i < max; i++) {
      BinMember member = (BinMember) membersToMove.get(i);
      ConflictData data = mover.getResolver().getConflictData(member);
      List conflicts = data.getConflicts();

      boolean isNoConflictOfType = true;
      for (int j = 0, maxJ = conflicts.size(); j < maxJ; j++) {
        Conflict conflict = (Conflict) conflicts.get(j);
        if (conflict.getType() == conflictType) {
          conflictsOfType.put(conflict, data);
          isNoConflictOfType = false;

          if (conflict.isResolved()) {
            alreadyResolved.add(member);
            continue upper;
          }
        }
      }

      if (isNoConflictOfType) {
        noConflictsOfType.add(member);
      }
    }

    Iterator iter = conflictsOfType.keySet().iterator();
    while (iter.hasNext()) {
      Conflict conflict = (Conflict) iter.next();
      ConflictData data = (ConflictData) conflictsOfType.get(conflict);
      int initialUpSize = data.getConflicts().size();
      HashMap initialDownSize = createInitialDownSize(data);

      createFakeResolutionDialog(conflict, resolutionNumber);
      conflict.resolve();

      if (isMakeStaticConflictWillBeAdded) {
      } else {
        if (initialUpSize != data.getConflicts().size()) {
          extraConflictsAdded.add(data.getMember());
        }

        List downMembers = conflict.getResolution().getDownMembers();
        for (int j = 0, maxJ = downMembers.size(); j < maxJ; j++) {
          Object o = downMembers.get(j);
          ConflictData downData = mover.getResolver().getConflictData(o);

          if ((downData == null) ||
              (initialDownSize.get(o) == null)) {
            continue;
          }

          if (((Integer) initialDownSize.get(o)).intValue() !=
              downData.getConflicts().size()) {
            extraConflictsAdded.add(o);
          }
        }
      }
    }

    assertTrue("all " + conflictType + " already resolved",
        conflictsOfType.keySet().size() != alreadyResolved.size());

    assertTrue("the conflict of type " + conflictType.toString() +
        " was not found for any of " + noConflictsOfType,
        ((noConflictsOfType.size() + alreadyResolved.size())
        != membersToMove.size()));

    assertTrue("in the process of resolving new conflicts were added for " +
        extraConflictsAdded, (extraConflictsAdded.size() == 0));

    mover.getResolver().resolveConflicts();
  }

  private HashMap createInitialDownSize(ConflictData data) {
    HashMap result = new HashMap();

    List usages = new ArrayList();
    usages.addAll(data.getUsesList());
    usages.addAll(data.getUsedByList());

    for (int i = 0, max = usages.size(); i < max; i++) {
      Object o = usages.get(i);
      ConflictData usageData = mover.getResolver().getConflictData(o);
      int size = 0;
      if (usageData != null) {
        size = usageData.getConflicts().size();
      }

      result.put(o, new Integer(size));
    }

    return result;
  }

  public void moveEdit() throws Exception {

    RefactoringStatus status = mover.apply();

    assertTrue("perform change: " + status.getAllMessages(), status.isOk());

    final Project expected = getExpectedProject();
    RwRefactoringTestUtils.assertSameSources("", expected, project);
  }

  private void moveCheckConflictCheckResolvable(String conflictMemberName,
      String conflictMemberOwner,
      ConflictType conflict,
      int conflictCount,
      boolean[] isResolvable) throws
      Exception {

    moveCheckConflict(conflictMemberName, conflictMemberOwner, conflict,
        conflictCount);

    BinTypeRef conflictOwner
        = project.findTypeRefForName(conflictMemberOwner);

    if (conflictOwner == null) {
      conflictOwner = findTypeRefForLocalType(conflictMemberOwner);
    }

    if (conflictOwner == null) {
      throw new RuntimeException("type of conflict member ("
          + conflictMemberOwner
          + ") was not found");
    }

    BinMember conflictMember = findMember(conflictOwner, conflictMemberName);
    ConflictData data = mover.getResolver().getConflictData(conflictMember);

    int index = 0;

    for (Iterator i = data.getConflicts().iterator(); i.hasNext(); ++index) {
      Conflict item = (Conflict) i.next();
      assertTrue("Expected isResolvable()=" + isResolvable[index] + " but was "
          + !isResolvable[index], item.isResolvable() == isResolvable[index]);
    }

  }

  private void moveCheckConflict(String conflictMemberName,
      String conflictMemberOwner,
      ConflictType conflict, int conflictCount) throws Exception {
    BinTypeRef conflictOwner
        = project.findTypeRefForName(conflictMemberOwner);

    if (conflictOwner == null) {
      conflictOwner = findTypeRefForLocalType(conflictMemberOwner);
    }

    if (conflictOwner == null) {
      throw new RuntimeException("type of conflict member ("
          + conflictMemberOwner
          + ") was not found");
    }

    BinMember conflictMember = findMember(conflictOwner, conflictMemberName);
    ConflictData data = mover.getResolver().getConflictData(conflictMember);

    assertTrue("member: " + conflictMember +
        "; data.getConflicts().size() is: " + data.getConflicts().size() +
        ", but expected conflictCount is: " + conflictCount
        + "; created conflicts: " + data.getConflicts(),
        conflictCount == data.getConflicts().size());

    if (conflictCount > 0) {
//      assertTrue("conflictStatus error",
//                 hasConflict(data, conflict,
//                             mover.getResolver().
//                             getConflictStatus(data.getMember()).getConflicts(),conflictCount));
      assertTrue(conflictMember.getQualifiedName() +
          " must have conflict " + conflict
          + ", got: " + data.getConflicts(),
          hasConflict(data.getConflicts(), conflict, conflictCount));
    }
  }

  private BinMember findMember(BinTypeRef owner, String memberName) {
    BinMember[] members;

    members = owner.getBinCIType().getDeclaredMethods();
    for (int i = 0; i < members.length; i++) {
      if (memberName.equals(members[i].getName())) {
        return members[i];
      }
    }

    members = owner.getBinCIType().getDeclaredFields();
    for (int i = 0; i < members.length; i++) {
      if (memberName.equals(members[i].getName())) {
        return members[i];
      }
    }

    BinConstructor[] constructors = ((BinClass) owner.getBinCIType()).
        getDeclaredConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (memberName.equals(constructors[i].getName())) {
        return constructors[i];
      }
    }

    BinTypeRef[] inners = owner.getBinCIType().getDeclaredTypes();
    for (int i = 0; i < inners.length; i++) {
      if (memberName.equals(inners[i].getName())) {
        return inners[i].getBinCIType();
      }
    }

    if (owner.getName().equals(memberName)) {
      return owner.getBinCIType();
    }

    return null;
  }

  public boolean hasConflict(List conflicts, ConflictType conflictType,
      int conflictCount) {

    if (conflicts.size() != conflictCount) {
      return false;
    }

    for (int i = 0, max = conflicts.size(); i < max; i++) {
      Conflict conflict = (Conflict) conflicts.get(i);

      if (conflictType.equals(conflict.getType())) {
        return true;
      }
    }

    return false;
  }

  public void testArrayTypeParameters() throws Exception {
    moveResolve("p1.A", "method", "p1.B");
    moveEdit();
  }

  /** Bug 2009 */
  public void testFieldIntoInterface() throws Exception {
    moveResolve("a.From", "field", "a.Interface");
    moveEdit();
  }

  public void testGeneralImports() throws Exception {
    moveResolve("x.Test", "method", "target.Target");
    moveEdit();
  }

  public void testImportsOfStatic() throws Exception {
    moveResolve("p1.A", "method", "p1.B");
    moveEdit();
  }

  public void testAlreadyDefinedConflict() throws Exception {
    moveResolve("p1.A", "f", "p1.B");
    moveCheckConflict("f", "p1.A", ConflictType.ALREADY_DEFINED, 1);
  }

  public void testOverridesAndOverridenConflict() throws Exception {
    moveResolve("A", "f", "");
    moveCheckConflict("f", "A", ConflictType.OVERRIDES, 2);
    moveCheckConflict("f", "A", ConflictType.OVERRIDEN, 2);
  }

  public void testUpdateStaticUsage() throws Exception {
    moveResolve("p1.A", "method", "p1.B");
    moveEdit();
  }

  public void testCallOnParameterType() throws Exception {
    moveResolve("A", "method", "B");
    moveEdit();
  }

  public void testCallOnParameterType2() throws Exception {
    moveResolve("A", "method, method1, method2", "B");
    moveEdit();
  }

  public void testCallOnRandomInstance() throws Exception {
    moveResolve("p1.A", "method", "p1.B");
    moveEdit();
  }

  public void testAddNativeTypeParam() throws Exception {
    moveResolve("A", "method, movingMethod", "B");
    moveEdit();
  }

  public void testAddNativeTypeParam2() throws Exception {
    moveResolve("p1.A", "method", "C");
    moveEdit();
  }

  public void testAddNativeTypeParam3() throws Exception {
    moveResolve("p1.A", "method, method2", "B");
    moveEdit();
  }

  public void testAddNativeTypeParam4() throws Exception {
    moveResolve("p1.A", "paintBuffer", "p2.B");
    moveEdit();
  }

  public void testMoveNotPossibleConflict1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_NOT_POSSIBLE, 1);
  }

  public void testMoveNotPossibleConflict2() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_NOT_POSSIBLE, 2);
  }

  public void testMoveNotPossibleConflict3() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_NOT_POSSIBLE, 2);
  }

  public void testImportNotPossibleConflict() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.IMPORT_NOT_POSSIBLE, 2);
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        2);
  }

  public void testImportNotPossibleConflict2() throws Exception {
    moveResolve("p1.A", "b", "p2.X");
    moveCheckConflict("b", "p1.A", ConflictType.IMPORT_NOT_POSSIBLE, 1);
  }

  /************* DRUsedByConflict ********/

  public void testDRUsedByThroughRefConflict1_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict1_Resolve2() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 1);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict2_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict2_Resolve2() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 1);
    moveEdit();
  }

  public void testDRUsedByThroughRef_AlwaysMove() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_USEDBY_ALSO, 2);
    moveResolveConflict(ConflictType.MOVE_USEDBY_ALSO);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict3_ResolveChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict4_ResolveChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict5_ResolveMakeStatic() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 2);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict_AlwaysMakeStatic1() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testDRUsedByThroughRef_AlwaysChangeAccess2() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_ACCESS, 1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_ACCESS);
    moveEdit();
  }

  public void testDRUsedByThroughRef_AlwaysChangeAccess4() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_ACCESS, 1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_ACCESS);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict6_ResolveMove() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testDRUsedByThroughRefConflict7_NoConflict1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveEdit();
  }

  /********** DRUsedByThroughClassNameConflict ********/

  public void testDRUsedByThroughClassNameConflict1_ResolveChangeAccess() throws
      Exception {
    moveResolve("p1.A", "a", "p2.X");
    moveCheckConflict("a", "p1.A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 1);
    moveEdit();
  }

  public void testDRUsedByThroughClassNameConflict2_AlwaysChangeAccess() throws
      Exception {
    moveResolve("p1.A", "a", "p2.X");
    moveCheckConflict("a", "p1.A", ConflictType.UNMOVABLE_CANNOT_ACCESS, 1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_ACCESS);
    moveEdit();
  }

  /************ DRUseThroughRefConflict *********/

  public void testDRUseThroughRefConflict1_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testDRUseThroughRefConflict1_Resolve2() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testDRUseThroughRefConflict2_NoConflict1() throws Exception {
    moveResolve("p1.A", "f1", "p3.Y");
    moveEdit();
  }

  public void testDRUseThroughRefConflict3_ResolveChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  /************ DRUseThroughThisConflict *********/

  public void testDRUseThroughThisConflict1_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testDRUseThroughThisConflict1_Resolve2() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testDRUseThroughThisConflict2_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testDRUseThroughThisConflict2_Resolve2() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testDRUseThroughThisConflict_AlwaysChangeAccess1() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testDRUseThroughThisConflict_AlwaysChangeAccess2() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testDRUseThroughThisConflict_AlwaysMove1() throws Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_USE_ALSO, 1);
    moveResolveConflict(ConflictType.MOVE_USE_ALSO);
    moveEdit();
  }

  public void testDRUseThroughThisConflict_AlwaysMove2() throws Exception {
    moveResolve("A", "f1", "B");
    moveCheckConflict("f1", "A", ConflictType.MOVE_USE_ALSO, 1);
    moveResolveConflict(ConflictType.MOVE_USE_ALSO);
    moveEdit();
  }

  /************ DRUseThroughClassName *********/

  public void testDRUseThroughClassNameConflict1_Resolve1() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testDRUseThroughClassNameConflict1_Resolve2() throws Exception {
    moveResolve("A", "f1", "B");
    moveCheckConflict("f1", "A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testDRUseThroughClassNameConflict2_AlwaysChangeAccess1() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  /*********************/

  public void testInstanceMethodCallWithinStaticTargetContext() throws
      Exception {
    moveResolve("B", "method", "A");
    moveEdit();
  }

  public void testReuseFirstOfTwoParameters() throws Exception {
    moveResolve("A", "compare, getName", "B");
    moveEdit();
  }

  public void testNotRemoveParamUsedForStayingMember() throws Exception {
    moveResolve("A", "method", "B");
    moveEdit();
  }

  public void testMoveMethodCalledOnComplexExprWhenPullUp() throws Exception {
    moveResolve("B", "method", "A");
    moveEdit();
  }

  public void testRemoveParamUsedForRecursiveCall() throws Exception {
    moveResolve("A", "method", "B");
    moveEdit();
  }

  /***************************************/

  public void testDeclarationOrDefinitionConflict1_Declaration1() throws
      Exception {
    moveResolve("A", "f1", "B");
    moveCheckConflict("f1", "A", ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 0);
    moveCheckConflict("f1", "A", ConflictType.IMPLEMENTATION_NEEDED, 2);
    moveResolveConflict(ConflictType.IMPLEMENTATION_NEEDED);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict1_Definition1() throws
      Exception {
    moveResolve("A", "f1", "B");
    moveCheckConflict("f1", "A", ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict2_Declaration1() throws
      Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 0);
    moveCheckConflict("f1", "p1.A", ConflictType.IMPLEMENTATION_NEEDED, 3);
    moveResolveConflict(ConflictType.IMPLEMENTATION_NEEDED);
    moveCheckConflict("f1", "p1.A", ConflictType.WEAK_ACCESS_FOR_ABSTRACT, 3);
    moveResolveConflict(ConflictType.WEAK_ACCESS_FOR_ABSTRACT);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict3_Declaration1() throws
      Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 0);
    moveCheckConflict("f1", "p1.A", ConflictType.IMPLEMENTATION_NEEDED, 2);
    moveResolveConflict(ConflictType.IMPLEMENTATION_NEEDED);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict4_Declaration1() throws
      Exception {
    moveResolve("A", "f", "B");
    moveCheckConflict("f", "A", ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 0);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict_AlwaysDefinition1() throws
      Exception {
    moveResolve("A", "f", "B");
    moveResolveConflict(ConflictType.DECLARATION_OR_DEFINITION, 1);
    moveEdit();
  }

  public void testDeclarationOrDefinitionConflict_AlwaysDefinition2() throws
      Exception {
    moveResolve("A", "a", "B");
    moveCheckConflict("a", "A", ConflictType.CHANGED_FUNCTIONALITY, 2);
    moveResolveConflict(ConflictType.CHANGED_FUNCTIONALITY, 0);
    moveCheckConflict("a", "A", ConflictType.MOVE_USEDBY_ALSO, 2);
    moveResolveConflict(ConflictType.MOVE_USEDBY_ALSO);
    moveEdit();
  }

  /***************************************/
  public void testStaticFieldIntoInterface1() throws Exception {
    moveResolve("A", "a", "Inter1");
    moveCheckConflict("a", "A", ConflictType.CREATE_ONLY_DECLARATION, 1);
    moveEdit();
  }

  public void testMethodIntoInterface1() throws Exception {
    moveResolve("A", "f1", "Inter");
    moveCheckConflict("f1", "A", ConflictType.CREATE_ONLY_DECLARATION, 3);
    moveCheckConflict("f1", "A", ConflictType.NOT_PUBLIC_FOR_INTERFACE, 3);
    moveCheckConflict("f1", "A", ConflictType.IMPLEMENTATION_NEEDED, 3);
    moveResolveConflict(ConflictType.NOT_PUBLIC_FOR_INTERFACE);
    moveResolveConflict(ConflictType.IMPLEMENTATION_NEEDED);
    moveEdit();
  }

  public void testNotStaticFieldIntoInterface() throws Exception {
    moveResolve("A", "a", "Inter");
    moveCheckConflict("a", "A", ConflictType.NOT_STATIC_FIELD_INTO_INTERFACE, 1);
  }

  public void testFieldForInterfaceIsAssignedWhenUsed() throws Exception {
    moveResolve("A", "a", "Inter");
    moveCheckConflict("a", "A", ConflictType.ASSIGNMENT_FOR_FINAL, 1);
  }

  public void testStaticMethodIntoInterfaceConflict() throws Exception {
    moveResolve("A", "f1", "Inter");
    moveCheckConflict("f1", "A", ConflictType.STATIC_METHOD_INTO_INTERFACE, 1);
  }

  public void testFieldWithExpressionIntoInterface1() throws Exception {
    moveResolve("A", "a", "Inter");
    moveCheckConflict("a", "A", ConflictType.CREATE_ONLY_DECLARATION, 1);
    moveEdit();
  }

  public void testFieldNeedsImport1() throws Exception {
    moveResolve("p1.A", "ref", "p2.X");
    moveEdit();
  }

  /*********use of inner**************/

  public void testUseQualifiedNameOfInner_AddOwnerOfInner1() throws Exception {
    moveResolve("A", "a,f1", "B");
    moveEdit();
  }

  public void testUseQualifiedNameOfInner_RemoveOwnerOfInner1() throws
      Exception {
    moveResolve("A", "b,f1", "B");
    moveEdit();
  }

  public void testUseQualifiedNameOfInner_ChangeAccessOfInner1() throws
      Exception {
    moveResolve("p1.A", "a", "p2.X");
    moveCheckConflict("a", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testNoConflictForInnerLocalClass() throws Exception {
    moveResolve("p1.A", "f", "p2.X");
    moveEdit();
  }

  /***********************/
  public void testMakeStaticForUsedChangeAccessForUses() throws Exception {
    moveResolve("A", "f", "B");
    moveCheckConflict("f", "A", ConflictType.MAKE_STATIC, 2);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("a", "A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED, 1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  /***********************/
  public void testIfUsesToStringSkipIt() throws Exception {
    moveResolve("A", "f", "B");
    moveEdit();
  }

  /***********************/
  public void testbug1850_1() throws Exception {
    moveResolve("A", "doSomethingWithVal", "C");
    moveCheckConflict("doSomethingWithVal", "A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 0);
    moveEdit();
  }

  public void testbug1850_2() throws Exception {
    moveResolve("A", "doSomethingWithVal", "B");
    moveCheckConflict("doSomethingWithVal", "A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testbug1863() throws Exception {
    moveResolve("A", "f", "A$InnerClass1");
    moveEdit();
  }

  public void testInstanceFinderBug1() throws Exception {
    moveResolve("A", "f", "B");
    moveCheckConflict("f", "A", null, 0);
    moveEdit();
  }

  public void testWrongParameterNameBug() throws Exception {
    moveResolve("Native", "f1", "B");
    moveEdit();
  }

  public void testbug1873() throws Exception {
    moveResolve("p2.X", "f", "p1.A");
    moveEdit();
  }

  public void testbug2051() throws Exception {
    moveResolve("a.Test", "method", "a.Destination");
    moveEdit();
  }

  /***********************/
  public void testMainIntoWrongClassConflict() throws Exception {
    moveResolve("A", "main", "B");
    moveCheckConflict("main", "A", ConflictType.MAIN_INTO_WRONG_CLASS, 1);
  }

  /***********************/
  public void testUsesThroughThisIntoNonStaticInner1() throws Exception {
    moveResolve("A", "f", "A$Inner");
    moveEdit();
  }

  public void testUsesThroughThisIntoStaticInner1() throws Exception {
    moveResolve("A", "f", "A$Inner");
    moveEdit();
  }

  public void testUsedByIntoNonStaticInner1() throws Exception {
    moveResolve("A", "f", "A$Inner");
    moveEdit();
  }

  public void testUsedByIntoStaticInner1_ResolveMakeStatic() throws Exception {
    moveResolve("A", "f", "A$Inner");
    moveCheckConflict("f", "A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testUsesThroughThisIntoNonStaticInner_MoveNotPossible1() throws
      Exception {
    // FIXME: case needs overlooking, why can't we move?[tonis]
    moveResolve("A", "f", "A$Inner");
    moveCheckConflict("f", "A", ConflictType.INSTANCE_NOT_ACCESSIBLE, 1);
  }

  public void testUsedOnComplexExpression() throws Exception {
    moveResolve("A", "method", "C");
    moveCheckConflict("method", "A", ConflictType.USED_ON_COMPLEX, 1);
  }

  public void testMoveMethodAtWwwRefactoringCom1() throws Exception {
    moveResolve("Person", "participate", "Project");
    moveEdit();
  }

  public void testMoveMethodAtWwwRefactoringCom1_2() throws Exception {
    moveResolve("Person", "participate", "Project");
    moveEdit();
  }

//  public void testAccessThroughFieldsSequence() throws Exception {
//    moveResolve("a.A", "method", "c.C");
//    moveCheckConflict("b", "a.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED, 1);
//    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
//    moveCheckConflict("method", "a.A", ConflictType.MAKE_STATIC, 1);
//    moveResolveConflict(ConflictType.MAKE_STATIC);
//    moveEdit();
//  }

  public void testAmbiguousImport1() throws Exception {
    moveResolve("Test", "method", "Target");
    moveEdit();
  }

  public void testAmbiguousImport2() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testAmbiguousImport3() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testAmbiguousImport4() throws Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testAmbiguousImport5() throws Exception {
    moveResolve("p1.B", "f", "p2.Collections");
    moveCheckConflict("f", "p1.B", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  /**********************************************/
  public void testMakeAllUsesAlsoStatic1() throws Exception {
    moveResolve("A", "method, otherMethod", "C");
    moveCheckConflict("method", "A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("otherMethod", "A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testIfMakeStaticAndUsesField_OnlyChangeAccessOfField() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 2);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        2);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testIfMakeStaticAndUsesMethod_LeaveAndChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 2);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 2);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testIfMakeStaticAndUsesMethod_MoveAndMakeStaticAlso() throws
      Exception {
    moveResolve("p1.A", "f1", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 2);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 2);
    moveResolveConflict(ConflictType.USES, 1, true);
    moveCheckConflict("f2", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testIfMakeStaticAndUsesMethod_MoveAndMakeStaticAlso2() throws
      Exception {
    moveResolve("p1.A", "f1", "p1.B");
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 2);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 2);
    moveResolveConflict(ConflictType.USES, 1, true);
    moveCheckConflict("f2", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  /***********************/
  public void testRightParameterName1() throws Exception {
    moveResolve("A", "f1", "B");
    moveEdit();
  }

  public void testRemoveParenthesesBug1() throws Exception {
    moveResolve("A", "f", "B");
    moveCheckConflict("f", "A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  /***********NonStaticUsedByStatic************/

  public void testNonStaticUsedByStatic_ResolveMakeStatic1() throws Exception {
    moveResolve("A", "f1", "B");
    moveCheckConflict("f1", "A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 1);
    moveEdit();
  }

  public void testNonStaticUsedByStatic_ResolveMove1() throws Exception {
    moveResolve("p1.A", "f1", "p2.B");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_USEDBY_ALSO, 2);
    moveResolveConflict(ConflictType.MOVE_USEDBY_ALSO, true);
    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
    moveResolveConflict(ConflictType.MAKE_STATIC);
    moveEdit();
  }

  public void testNonStaticUsedByStatic_ResolveMove2() throws Exception {
  	//if (RefactorItConstants.runNotImplementedTests) {
	    moveResolve("p1.A", "f1, f2", "p2.B");
	    moveCheckConflict("f1", "p1.A", ConflictType.MAKE_STATIC, 1);
	    moveResolveConflict(ConflictType.MAKE_STATIC);
	    moveEdit();
  	//}
  }


  /********Move from anonymous and local***************/
  public void testMoveFromAnonymous_MoveNotPossible1() throws Exception {
    moveResolve("A$1", "anonym1", "B");
    moveCheckConflict("anonym1", "A$1", ConflictType.OVERRIDES, 1);
  }

  public void testMoveFromLocalUsesForeignLocalVar_MoveNotPossible1() throws
      Exception {
    moveResolve("A$Local", "local1", "B");
    moveCheckConflict("local1", "A$Local",
        ConflictType.USES_FOREIGN_LOCAL_VARIABLES, 1);
  }

  public void testMoveFromLocalUsesNative_MoveNotPossible1() throws Exception {
    moveResolve("A$Local", "local1", "B");
    moveCheckConflict("local1", "A$Local", ConflictType.MOVE_NOT_POSSIBLE, 1);
  }

  public void testMoveFromLocalUsesNative_MoveNotPossible2() throws Exception {
    moveResolve("A$Local", "local1", "B");
    moveCheckConflict("local1", "A$Local", ConflictType.IMPORT_NOT_POSSIBLE, 2);
    moveCheckConflict("local1", "A$Local", ConflictType.MOVE_USE_ALSO, 2);
  }

  /***********************/
  public void testCastBug1() throws Exception {
    moveResolve("A", "isMain", "BinMethod");
    moveEdit();
  }

  /***********************/
  public void testReuseOfParameter1() throws Exception {
    moveResolve("A", "Main1,Main2", "BinMethod");
    moveEdit();
  }

  /***********************/
// fails - adds unnecessary space
  public void testReuseSecondOfTwoParameters() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      moveResolve("A", "compare, getName", "B");
      moveEdit();
    }
  }

  /********* USES **************/

  public void testUsesNativeSubTargetDoubleResolution_ChangeAccess() throws
      Exception {
    moveResolve("A", "f1,f4,f6,f8", "B");
    moveCheckConflict("f1", "A", ConflictType.USES, 1);
    moveCheckConflict("f4", "A", ConflictType.USES, 1);
    moveCheckConflict("f6", "A", ConflictType.USES, 1);
    moveCheckConflict("f8", "A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testUsesNativeSubTargetDoubleResolution_MoveNative() throws
      Exception {
    moveResolve("A", "f1,f4,f6,f8", "B");
    moveCheckConflict("f1", "A", ConflictType.USES, 1);
    moveCheckConflict("f4", "A", ConflictType.USES, 1);
    moveCheckConflict("f6", "A", ConflictType.USES, 1);
    moveCheckConflict("f8", "A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testUsesNativeSubTargetNoConflict() throws Exception {
    moveResolve("A", "f1,f4,f6,f8", "B");
    moveEdit();
  }

  public void testUsesNativeNotSubTargetDoubleResolution_ChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1,f4,f6,f8", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f4", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f6", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f8", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 0);
    moveEdit();
  }

  public void testUsesNativeNotSubTargetDoubleResolution_MoveNative() throws
      Exception {
    moveResolve("p1.A", "f1,f4,f6,f8", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f4", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f6", "p1.A", ConflictType.USES, 1);
    moveCheckConflict("f8", "p1.A", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }

  public void testUsesNativeNotSubTargetSingleResolution_MoveNative() throws
      Exception {
    moveResolve("p1.A", "f1,f4,f6", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.MOVE_USE_ALSO, 1);
    moveCheckConflict("f4", "p1.A", ConflictType.MOVE_USE_ALSO, 1);
    moveCheckConflict("f6", "p1.A", ConflictType.MOVE_USE_ALSO, 1);
    moveResolveConflict(ConflictType.MOVE_USE_ALSO);
    moveEdit();
  }

  public void testUsesNativeNotSubTargetSingleResolution_ChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1,f4,f6,f8", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f4", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f6", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f8", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testUsesNotNativeNotSubTargetSingleResolution_ChangeAccess() throws
      Exception {
    moveResolve("p1.A", "f1,f4,f6,f8", "p2.X");
    moveCheckConflict("f1", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f4", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f6", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveCheckConflict("f8", "p1.A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
        1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testUsesNativePrivateConstructorSingleResolution_ChangeAccess() throws
      Exception {
    moveResolve("A", "f", "B");
    moveCheckConflict("f", "A", ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED, 1);
    moveResolveConflict(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED);
    moveEdit();
  }

  public void testbug1977() throws Exception {
    moveResolve("A", "f1", "Super");
    moveEdit();
  }

  public void testmoveOverridesToSuper() throws Exception {
    moveResolve("moveOverridesToSuper.A", "equals",
        "moveOverridesToSuper.C");
    moveEdit();
  }

  public void testmoveOverridesAbstract() throws Exception {
    moveResolve("moveOverridesAbstract.A", "isTrue",
        "moveOverridesAbstract.C");
    moveCheckConflict("isTrue", "moveOverridesAbstract.A",
        ConflictType.OVERRIDES, 1);

    moveResolve("moveOverridesAbstract.A", "isTrue",
        "moveOverridesAbstract.D");
    moveCheckConflictCheckResolvable("isTrue", "moveOverridesAbstract.A",
        ConflictType.OVERRIDES, 1, new boolean[] {false});

    moveResolve("moveOverridesAbstract.A", "isTrue",
        "moveOverridesAbstract.E");

    moveCheckConflictCheckResolvable("isTrue", "moveOverridesAbstract.A",
        ConflictType.OVERRIDES, 1, new boolean[] {false});

    moveResolve("moveOverridesAbstract.A2", "isTrue",
        "moveOverridesAbstract.D2");

    moveCheckConflictCheckResolvable("isTrue", "moveOverridesAbstract.A2",
        ConflictType.OVERRIDES, 1, new boolean[] {true});

  }

  public void testmoveOverridden() throws Exception {
    moveResolve("moveOverridden.A", "test1",
        "moveOverridden.D");
    moveCheckConflictCheckResolvable("test1", "moveOverridden.A",
        ConflictType.OVERRIDEN, 1, new boolean[] {true});

  }

  public void testwrongEdit() throws Exception {
    moveResolve("B", "setOwner", "ARef");
  }

  public void testMoveFromInterface1() throws Exception {
    moveResolve("A", "TEST", "B");
    moveEdit();
  }

  public void testMoveFromInterface2() throws Exception {
    moveResolve("A", "f", "p2.B");
//   moveCheckConflictCheckResolvable("f","A",ConflictType.IMPLEMENTATION_NEEDED,1,new boolean[]{true,false});
    moveCheckConflictCheckResolvable("f", "A",
        ConflictType.ABSTRACT_METHOD_TO_CLASS, 1, new boolean[] {false});

    //moveEdit();
  }

  public void testMoveToNotAccessedClass() throws Exception {
    moveResolve("Source", "CONST", "p2.Target");
    moveCheckConflict("CONST", "Source",
        ConflictType.UNMOVABLE_CANNOT_ACCES_TARGET, 2);
  }

  public void testDontAddParameter() throws Exception {
    moveResolve("B", "f1", "A");
//   moveResolveConflict( ConflictType.DECLARATION_OR_DEFINITION,1);
    moveEdit();

  }

  public void testDontAddParameter2() throws Exception {

    moveResolve("B", "f1",
        "A");
    moveEdit();
  }

  public void testDontAddParameter3() throws Exception {

    //FIXME: super using case needs correct resolving

    if (RefactorItConstants.runNotImplementedTests) {
      AppRegistry.getLogger(this.getClass()).debug("Correct super using not implemented");

    }
  }

  public void testStaticImports1() throws Exception {
    moveResolve("p1.A", "foo", "p2.C");
    moveEdit();
  }

  public void testStaticImports2() throws Exception {
    moveResolve("A", "FOO", "B");
    moveEdit();
  }

  public void testStaticImports3() throws Exception {
    moveResolve("A", "TWO_PI", "B");
    moveEdit();
  }

  public void testMoveInner_ResolveToStatic() throws Exception {
  	moveResolve("p1.A", "Inner", "p2.B");
  	moveCheckConflict("Inner", "p1.A", ConflictType.USED_BY, 1);
  	moveResolveConflict(ConflictType.USED_BY, 1);
  	moveEdit();
  }

  public void testMoveInner_AlsoMoveUsedBy() throws Exception {
  	moveResolve("p1.A", "Inner", "p2.B");
  	moveCheckConflict("Inner", "p1.A", ConflictType.USED_BY, 1);
  	moveResolveConflict(ConflictType.USED_BY, 0);
  	moveEdit();
  }

  public void testMoveInner() throws Exception {
  	moveResolve("p1.A", "Inner", "p2.B");
  	moveCheckConflict("Inner", "p1.A", null, 0);
  	moveEdit();
  }

  public void testMoveInner_AlsoMoveUse() throws Exception {
  	moveResolve("p1.A", "Inner", "p2.B");
    moveCheckConflict("Inner", "p1.A", ConflictType.MOVE_USE_ALSO, 2);
  	moveCheckConflict("Inner", "p1.A", ConflictType.USED_BY, 2);
  	moveResolveConflict(ConflictType.MOVE_USE_ALSO, 0);
  	moveResolveConflict(ConflictType.USED_BY, 0);
  	moveEdit();
  }

  public void testIssue246() throws Exception {
    moveResolve("A", "foo", "B");
    moveCheckConflict("foo", "A", ConflictType.OVERRIDEN, 1);
    moveResolveConflict(ConflictType.OVERRIDEN, 0);
    moveEdit();
  }


  public void testIssue616() throws Exception {
    moveResolve("issue616.Bar", "bar", "issue616.p1.Foo");
    moveEdit();
  }

  public void testIssue631() throws Exception {
    moveResolve("A", "processInfo,foo", "B");
    moveCheckConflict("foo", "A", ConflictType.CHANGED_FUNCTIONALITY, 1);
    moveResolveConflict(ConflictType.CHANGED_FUNCTIONALITY, 0);
    moveCheckConflict("processInfo", "A", ConflictType.USED_BY, 1);
    moveResolveConflict(ConflictType.USED_BY, 2);
    moveCheckConflict("processInfo", "A", ConflictType.IMPLEMENTATION_NEEDED, 2);
    moveResolveConflict(ConflictType.IMPLEMENTATION_NEEDED, 1);
    moveEdit();
  }

  public void testIssue632() throws Exception {
    moveResolve("B", "processInfo,foo", "A");
    moveCheckConflict("foo", "B", ConflictType.CHANGED_FUNCTIONALITY, 1);
    moveResolveConflict(ConflictType.CHANGED_FUNCTIONALITY, 0);
    moveCheckConflict("processInfo", "B", ConflictType.ALREADY_DEFINED, 2);
    moveResolveConflict(ConflictType.ALREADY_DEFINED, 0);
    moveCheckConflict("processInfo", "B", ConflictType.OVERRIDES, 2);
    moveResolveConflict(ConflictType.OVERRIDES, 0);

    moveEdit();
  }


  public void testMultiDeclarationMove() throws Exception {
    moveResolve("multiDeclarationMove.A", "ONE,TWO,THREE,FOUR", "multiDeclarationMove.B");
    moveEdit();
  }

  public void testMoveMethodThatUsesLocal_REF1120() throws Exception {
    moveResolve("zju.Claass", "moveable", "zju.MoveTo");
    moveCheckConflict("moveable", "zju.Claass", null, 0);
    moveEdit();
  }

  public void testRedundantImports() throws Exception {
    moveResolve("Test", "RABBIT", "TestInterface");
    moveEdit();
  }

  public void testNameDuplicating() throws Exception {
    moveResolve("Test", "doPrintRabbit", "Test2");
    moveCheckConflict("printRabbit", "Test", ConflictType.USES, 1);
    moveResolveConflict(ConflictType.USES, 1);
    moveEdit();
  }


  /**
   * For debugging; a sample line that could be run in this method:
   * new MoveMemberTest("").testXXX();
   */
  public static void main(String args[]) throws Exception {
    //System.err.println("sleeping 5 sec -- attach debugger now");
    //Thread.sleep(5000);

    DialogManager.setInstance(new NullDialogManager());

//    MoveMemberTest test = new MoveMemberTest("testStaticImports1");
//    test.testAddNativeTypeParam();
  }

}
