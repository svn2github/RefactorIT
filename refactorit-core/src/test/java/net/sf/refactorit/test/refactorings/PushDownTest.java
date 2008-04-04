/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author vadim
 */
public class PushDownTest extends RefactoringTestCase {
//  private static final Category cat =
//      Category.getInstance(PushDownTest.class.getName());
//
//  private static final String PROJECTS_PATH = "PushDownTests";
//  private static List dataList = new ArrayList();

//	private Project project;
//	private PullPushConflictsResolver conflictsResolver;
//	private BinMember member;
//	private BinMember conflictMember;
//	private PullPushConflictData data;
//	private boolean isImplementationGlobal = true;

  public PushDownTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "PushDownTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(PushDownTest.class);
    suite.setName("Push Down Tests");
    return suite;
  }

//	private void pushDownResolve(String memberName, boolean isPushDownMethod,
//														String typeToMoveInto) throws Exception {
//		cat.info("Testing " + getStrippedTestName());
//		project = getMutableProject();
//		dataList.clear();
//
//		BinTypeRef pushDownFrom = (BinTypeRef)project.findTypeRefForName("p1.Class2");
//		BinTypeRef pushDownTo = (BinTypeRef)project.findTypeRefForName(typeToMoveInto);
//
//		member = findMember(pushDownFrom, memberName, isPushDownMethod);
//		if (member == null) {
//			throw new RuntimeException("the member with name " + memberName + " was not found");
//		}
//
//		boolean isImplementation;
//		if (pushDownTo.getBinCIType() instanceof BinClass) {
//			if (pushDownTo.getBinCIType().isAbstract()) {
//				isImplementation = isImplementationGlobal;
//			}
//			else {
//				isImplementation = true;
//			}
//		}
//		else {
//			isImplementation = false;
//		}
//
//		PullPushConflictsResolver.resetStaticData();
//		conflictsResolver = new PullPushConflictsResolver(pushDownTo.getBinCIType(),
//																											member, false, isImplementation);
//
//		data = ((PullPushConflictData)conflictsResolver.getConflictsDataMap().get(member));
//
//		if (conflictsResolver.isMemberConflict(data, "DOUBLE_RESOLVE_1",
//																						data.getConflictTypes().size())) {
//			conflictsResolver.resolveDouble(data.getMember(), ConflictType.DOUBLE_RESOLVE_1,
//																			true);
//		}
//	}
//
//	private void collectDataForRefactoring() {
//		conflictsResolver.collectDataForRefactoring();
//		dataList.addAll(conflictsResolver.getPullPushData());
//	}
//
//	private void pushDownEdit() throws Exception {
////		collectDataForRefactoring();
////
////		final PullPush pushDown = new PullPush(new NullContext(project), null, member);
////		pushDown.setPullPushData((PullPushData[])dataList.toArray(new PullPushData[0]));
////
////		if (!pushDown.performChange().isOk()) {
////			fail("Pull Up of " + member.getName() + " failed." + " Last message to user: "
////					+ ((ImportedTests.KeyStoringDialogManager) DialogManager.getInstance())
////					.getLastMessageKey());
////		}
////
////		final Project expected = getExpectedProject();
////    RwRefactoringTestUtils.assertSameSources("", expected, project);
//	}
//
//	private void pushDownDoubleResolve(String memberName,
//																		boolean isPullUpMethod,
//																		boolean changeAccessChoice,
//																		ConflictType conflictType,
//																		int conflictCount) {
//		BinTypeRef pullUpFrom = (BinTypeRef)project.findTypeRefForName("p1.Class2");
//		if (pullUpFrom == null) {
//			throw new RuntimeException("the type with name p1.Class2 was not found");
//		}
//
//		BinMember member = findMember(pullUpFrom, memberName, isPullUpMethod);
//		if (member == null) {
//			throw new RuntimeException("the member with name " + memberName + " was not found");
//		}
//
//		PullPushConflictData data = (PullPushConflictData)conflictsResolver.
//																	getConflictsDataMap().get(member);
//
//		String conflictString;
//		if (conflictType == ConflictType.DOUBLE_RESOLVE_1) {
//			conflictString = "DOUBLE_RESOLVE_1";
//		}
//		else {
//			conflictString = "DOUBLE_RESOLVE_2";
//		}
//
//		assertTrue(data.getMember().getQualifiedName() +
//							" is not conflict member as it must be",
//							conflictsResolver.isMemberConflict(data, conflictString, conflictCount));
//
//		conflictsResolver.resolveDouble(data.getMember(), conflictType, changeAccessChoice);
//	}
//
//	private void pushDownFail(String conflictMemberName, String conflictMemberOwner,
//													String conflictString, boolean isPushDownMethod,
//													int conflictCount) throws Exception {
//		BinTypeRef conflictOwner = (BinTypeRef)project.findTypeRefForName(conflictMemberOwner);
//		conflictMember = findMember(conflictOwner, conflictMemberName, isPushDownMethod);
//		if (conflictMember == null) {
//			throw new RuntimeException("the conflict member with name " +
//																	conflictMemberName + " was not found");
//		}
//
//		data = (PullPushConflictData)conflictsResolver.getConflictsDataMap().get(conflictMember);
//
//		assertTrue(conflictMember.getQualifiedName() +
//								" is not conflict member as it must be",
//								conflictsResolver.isMemberConflict(data, conflictString, conflictCount));
//	}
//
//	private BinMember findMember(BinTypeRef owner, String memberName,
//																boolean isPushDownMethod) {
//		BinMember[] members;
//		if (isPushDownMethod) {
//			members = owner.getBinCIType().getDeclaredMethods();
//		}
//		else {
//			members = owner.getBinCIType().getDeclaredFields();
//		}
//
//		for (int i = 0; i < members.length; i++) {
//			if (memberName.equals(members[i].getName())) {
//				return members[i];
//			}
//		}
//
//		BinConstructor[] constructors = ((BinClass)owner.getBinCIType()).getDeclaredConstructors();
//		for (int i = 0; i < constructors.length; i++) {
//			if (memberName.equals(constructors[i].getName())) {
//				return constructors[i];
//			}
//		}
//
//		BinTypeRef[] inners = owner.getBinCIType().getDeclaredTypes();
//		for (int i = 0; i < inners.length; i++) {
//			if (memberName.equals(inners[i].getName())) {
//				return inners[i].getBinCIType();
//			}
//		}
//
//		if (owner.getName().equals(memberName)) {
//			return owner.getBinCIType();
//		}
//
//		return null;
//	}
//
//	public void testPush1() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPushFail2() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "SUB_USES_PUSHED_DOWN", true, 1);
//	}
//
//	public void testPushFail3() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "SUB_USES_PUSHED_DOWN", true, 2);
//		pushDownFail("func1", "p1.Class2", "OTHER_CLASSES_USE", true, 2);
//	}
//
//	public void testPush4() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPushFail5() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "SUB_USES_PUSHED_DOWN", true, 1);
//	}
//
//	public void testPushFail6() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "OVERRIDE", true, 1);
//	}
//
//	public void testPush7() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPushFail8() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "UNMOVABLE_CANNOT_ACCESS", true, 1);
//	}
//
//	public void testPush9() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pushDownEdit();
//	}
//
//	public void testPush10() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownDoubleResolve("func2", true, false, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pushDownEdit();
//	}
//
//	public void testPushFail11() throws Exception {
//		pushDownResolve("func1", true, "p2.Class10");
//		pushDownFail("f3", "p1.Class3", "WEAK_ACCESS_OF_FOREIGN", true, 1);
//	}
//
//	public void testPush12() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPush13() throws Exception {
//		pushDownResolve("a", false, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPushFail14() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownFail("func1", "p1.Class2", "SUB_USES_PUSHED_DOWN", true, 1);
//	}
//
//	public void testPush15() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownEdit();
//	}
//
//	public void testPush16() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pushDownEdit();
//	}
//
//	public void testPush17() throws Exception {
//		pushDownResolve("func1", true, "p1.Class3");
//		pushDownDoubleResolve("func2", true, false, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pushDownEdit();
//	}

  protected void setUp() {
  }

  protected void tearDown() {
  }
}
