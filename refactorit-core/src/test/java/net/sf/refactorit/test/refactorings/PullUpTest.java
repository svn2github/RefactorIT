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
public class PullUpTest extends RefactoringTestCase {
//  private static final Category cat =
//      Category.getInstance(PullUpTest.class.getName());
//
//  private static final String PROJECTS_PATH = "PullUpTests";
//  private static List dataList = new ArrayList();

//	private Project project;
//	private PullPushConflictsResolver conflictsResolver;
//	private BinMember member;
//	private BinMember conflictMember;
//	private PullPushConflictData data;
//	private boolean isImplementationGlobal = true;

  public PullUpTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "PullUpTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(PullUpTest.class);
    suite.setName("Pull Up Tests");
    return suite;
  }

//	private void pullUpResolve(String memberName, boolean isPullUpMethod,
//														String typeToMoveInto) throws Exception {
//		pullUpResolve(memberName, isPullUpMethod, typeToMoveInto, true);
//	}
//
//	private void pullUpResolve(String memberName, boolean isPullUpMethod,
//														String typeToMoveInto, boolean fromScratch)
//														throws Exception {
//		if (fromScratch) {
//			cat.info("Testing " + getStrippedTestName());
//			project = getMutableProject();
//			PullPushConflictsResolver.resetStaticData();
//			dataList.clear();
//		}
//
//		BinTypeRef pullUpFrom = (BinTypeRef)project.findTypeRefForName("p1.Class2");
//		BinTypeRef pullUpTo = (BinTypeRef)project.findTypeRefForName(typeToMoveInto);
//		if (pullUpTo == null) {
//			throw new RuntimeException("type to move into " + typeToMoveInto + " was not found");
//		}
//
//		member = findMember(pullUpFrom, memberName, isPullUpMethod);
//		if (member == null) {
//			throw new RuntimeException("the member with name " + memberName + " was not found");
//		}
//
//		boolean isImplementation;
//		if (pullUpTo.getBinCIType() instanceof BinClass) {
//			if (pullUpTo.getBinCIType().isAbstract()) {
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
//		conflictsResolver = new PullPushConflictsResolver(pullUpTo.getBinCIType(),
//																											member, true, isImplementation);
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
//	private void pullUpEdit() throws Exception {
//		collectDataForRefactoring();
//
//		final PullPush pullUp = new PullPush(new NullContext(project), null, member);
//		pullUp.setPullPushData((PullPushData[])dataList.toArray(new PullPushData[0]));
//
//		if (!pullUp.performChange().isOk()) {
//			fail("Pull Up of " + member.getName() + " failed." + " Last message to user: "
//					+ ((ImportedTests.KeyStoringDialogManager) DialogManager.getInstance())
//					.getLastMessageKey());
//		}
//
//		final Project expected = getExpectedProject();
//    RwRefactoringTestUtils.assertSameSources("", expected, project);
//	}
//
//	private void pullUpDoubleResolve(String memberName,
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
//		conflictsResolver.resolveDouble(data.getMember(), conflictType,
//																		changeAccessChoice);
//	}
//
//	private void pullUpUnmovableUses(String conflictMemberName, String conflictMemberOwner,
//																		boolean isPullUpMethod,
//																		int conflictCount) throws Exception {
//		pullUpFail(conflictMemberName, conflictMemberOwner, "UNMOVABLE_CANNOT_ACCESS",
//								isPullUpMethod, conflictCount);
//		conflictsResolver.resolveUnmovableUses(conflictMember);
//	}
//
//	private void pullUpNotPublicForInterface(String conflictMemberName,
//																			String conflictMemberOwner,
//																			boolean isPullUpMethod,
//																			int conflictCount) throws Exception {
//		pullUpFail(conflictMemberName, conflictMemberOwner, "NOT_PUBLIC_FOR_INTERFACE",
//								isPullUpMethod, conflictCount);
//		conflictsResolver.resolveNotPublicForInterface(conflictMember);
//	}
//
//	private void pullUpNoStaticInit(String conflictMemberName,
//																	String conflictMemberOwner,
//																	String staticInit,
//																	boolean isPullUpMethod,
//																	int conflictCount) throws Exception {
//		pullUpFail(conflictMemberName, conflictMemberOwner, "NO_STATIC_INIT",
//								isPullUpMethod, conflictCount);
//		conflictsResolver.resolveNoStaticInit(conflictMember, staticInit);
//	}
//
//	private void pullUpOtherImplementersExist(String conflictMemberName,
//																						String conflictMemberOwner,
//																						boolean isPullUpMethod,
//																						int conflictCount) throws Exception {
//		pullUpFail(conflictMemberName, conflictMemberOwner, "IMPLEMENTATION_NEEDED",
//								isPullUpMethod, conflictCount);
//		conflictsResolver.resolveOtherImplementersExist(conflictMember);
//	}
//
//	private void pullUpWeakAccessForAbstract(String conflictMemberName,
//																						String conflictMemberOwner,
//																						boolean isPullUpMethod,
//																						int conflictCount) throws Exception {
//		pullUpFail(conflictMemberName, conflictMemberOwner, "WEAK_ACCESS_FOR_ABSTRACT",
//								isPullUpMethod, conflictCount);
//		conflictsResolver.resolveWeakAccessForAbstract(conflictMember);
//	}
//
//	private void pullUpFail(String conflictMemberName, String conflictMemberOwner,
//													String conflictString, boolean isPullUpMethod,
//													int conflictCount) throws Exception {
//		BinTypeRef conflictOwner = (BinTypeRef)project.findTypeRefForName(conflictMemberOwner);
//		conflictMember = findMember(conflictOwner, conflictMemberName, isPullUpMethod);
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
//																boolean isPullUpMethod) {
//		BinMember[] members;
//		if (isPullUpMethod) {
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
//	public void testPull1() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull2() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull3() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull4() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull5() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull6() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull7() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull8() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull9() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull10() throws Exception {
//		pullUpResolve("tmp3", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull11() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull12() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull13() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull14() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull15() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull16() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull17() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull18() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull19() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull20() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull21() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull22() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull23() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull24() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull25() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull26() throws Exception {
//		pullUpResolve("func1", true, "p1.Class3");
//		pullUpEdit();
//	}
//
//	public void testPull27() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull28() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull29() throws Exception {
//		pullUpResolve("func1", true, "p1.Class3");
//		pullUpEdit();
//	}
//
//	public void testPull30() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull31() throws Exception {
//		pullUpResolve("func1", true, "p1.Class3");
//		pullUpEdit();
//	}
//
//	public void testPull32() throws Exception {
//		pullUpResolve("func1", true, "p1.Class3");
//		pullUpEdit();
//	}
//
//	public void testPull33() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPullFail34() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpFail("func1", "p1.Class2", "OVERRIDE", true, 1);
//	}
//
//	public void testPullFail35() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpFail("func2", "p1.Class2", "OVERRIDE", true, 1);
//	}
//
//	public void testPullFail36() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpFail("func1", "p1.Class2", "OTHER_CLASSES_USE", true, 1);
//	}
//
//	public void testPullFail37() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpFail("func1", "p1.Class2", "OTHER_CLASSES_USE", true, 1);
//	}
//
//	public void testPull38() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPullFail39() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 2);
//		pullUpFail("Class3", "p1.Class3", "WEAK_ACCESS_OF_FOREIGN", false, 2);
//	}
//
////// fails, not implemented
////	public void testPull40() throws Exception {
////		pullUpResolve("tmp1", false, "p1.Class1");
////		pullUpEdit();
////	}
//
//	public void testPull41() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull42() throws Exception {
//		pullUpResolve("tmp1", false, "p2.Class10");
//		pullUpEdit();
//	}
//
////// fails, not implemented
////	public void testPull43() throws Exception {
////		pullUpResolve("tmp1", false, "p1.Class1");
////		pullUpEdit();
////	}
//
//	public void testPullFail44() throws Exception {
//		pullUpResolve("tmp1", false, "p2.Class10");
//		pullUpFail("Inner", "p1.Class2", "IMPORT_NOT_POSSIBLE", false, 1);
//	}
//
//	public void testPullFail45() throws Exception {
//		pullUpResolve("tmp1", false, "p2.Class10");
//		pullUpFail("Inner", "p1.Class2", "IMPORT_NOT_POSSIBLE", false, 1);
//	}
//
//	public void testPull46() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpDoubleResolve("func2", true, false, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pullUpEdit();
//	}
//
//	public void testPull47() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pullUpEdit();
//	}
//
//	public void testPull48() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull49() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull50() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpUnmovableUses("tmp1", "p1.Class2", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPull51() throws Exception {
//		pullUpResolve("tmp1", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull52() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull53() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull54() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull55() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull56() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull57() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull58() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpNotPublicForInterface("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
//	public void testPull59() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpNoStaticInit("a", "p1.Class2", "new String(\"init\")", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPull60() throws Exception {
//		pullUpResolve("a", false, "p2.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPullFail61() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpFail("a", "p1.Class2", "OVERRIDE", false, 1);
//	}
//
//	public void testPullFail62() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpFail("f", "p1.Class2", "OVERRIDE", true, 1);
//	}
//
//	public void testPullFail63() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpFail("a", "p1.Class2", "ASSIGNMENT_FOR_FINAL", false, 1);
//	}
//
//	public void testPullFail64() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpFail("a", "p1.Class2", "ASSIGNMENT_FOR_FINAL", false, 1);
//	}
//
//	public void testPullFail65() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpFail("a", "p1.Class2", "ASSIGNMENT_FOR_FINAL", false, 1);
//	}
//
//	public void testPullFail66() throws Exception {
//		pullUpResolve("a", false, "p2.InterFace1");
//		pullUpFail("a", "p1.Class2", "RESOLVED", false, 1);
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 1);
//	}
//
//	public void testPullFail67() throws Exception {
//		pullUpResolve("a", false, "p2.InterFace1");
//		pullUpFail("a", "p1.Class2", "NO_STATIC_INIT", false, 1);
//		pullUpFail("b", "p1.Class1", "WEAK_ACCESS_OF_FOREIGN", false, 1);
//		pullUpFail("Class1", "p1.Class1", "IMPORT_POSSIBLE", false, 1);
//	}
//
//	public void testPull68() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpOtherImplementersExist("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
//	public void testPullFail69() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpFail("f", "p1.Class2", "STATIC_INTO_INTERFACE", true, 1);
//	}
//
//	public void testPull70() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull71() throws Exception {
//		pullUpResolve("a", false, "p2.Interface10");
//		pullUpFail("b", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 2);
//		pullUpFail("b", "p1.Class3", "WEAK_ACCESS_OF_FOREIGN", false, 2);
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 1);
//		pullUpNoStaticInit("a", "p1.Class2", "0", false, 1);
//		pullUpFail("b", "p1.Class3", "UNKNOWN", false, 1);
//		pullUpFail("Class3", "p1.Class3", "UNKNOWN", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPullFail72() throws Exception {
//		pullUpResolve("a", false, "p2.Interface10");
//		pullUpFail("f", "p1.Class3", "IMPORT_NOT_POSSIBLE", true, 2);
//		pullUpFail("f", "p1.Class3", "WEAK_ACCESS_OF_FOREIGN", true, 2);
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 1);
//		pullUpNoStaticInit("a", "p1.Class2", "null", false, 1);
//		pullUpFail("f", "p1.Class3", "UNKNOWN", true, 1);
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 1);
//	}
//
//	public void testPull73() throws Exception {
//		pullUpResolve("f", true, "p2.Interface10");
//		pullUpNotPublicForInterface("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
//	public void testPull74() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpNotPublicForInterface("f", "p1.Class2", true, 2);
//		pullUpOtherImplementersExist("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
//	public void testPull75() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPullFail76() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpFail("func1", "p1.Class2", "OVERRIDE", true, 1);
//	}
//
///****************************/
//
//	public void testPull77() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull78() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull79() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_2, 1);
//		pullUpEdit();
//	}
//
//	public void testPull80() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull81() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pullUpEdit();
//	}
//
//	public void testPull82() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpDoubleResolve("func2", true, false, ConflictType.DOUBLE_RESOLVE_2, 1);
//		pullUpEdit();
//	}
//
//	public void testPull83() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpDoubleResolve("func2", true, false, ConflictType.DOUBLE_RESOLVE_2, 1);
//		pullUpDoubleResolve("func2", true, true, ConflictType.DOUBLE_RESOLVE_1, 1);
//		pullUpEdit();
//	}
//
//	public void testPull84() throws Exception {
//		pullUpResolve("func1", true, "p2.Class10");
//		pullUpUnmovableUses("func2", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
///****************************/
//
//	public void testPull85() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPullFail86() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpFail("func1", "p1.Class2", "RESOLUTION_IS_CHANGED", true, 1);
//	}
//
//	public void testPull87() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPullFail88() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpFail("func1", "p1.Class2", "RESOLUTION_IS_CHANGED", true, 1);
//	}
//
//	public void testPull89() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPull90() throws Exception {
//		pullUpResolve("a", false, "p1.Class1");
//		pullUpEdit();
//	}
//
//	public void testPullFail91() throws Exception {
//		pullUpResolve("a", false, "p1.Class1");
//		pullUpFail("a", "p1.Class2", "RESOLUTION_IS_CHANGED", false, 1);
//	}
//
//	public void testPull92() throws Exception {
//		pullUpResolve("a", false, "p1.Interface1", true);
//		collectDataForRefactoring();
//
//		pullUpResolve("f1", true, "p1.Class1", false);
//		pullUpFail("f1", "p1.Class2", "OVERRIDE", true, 1);
//
//		pullUpResolve("b", false, "p1.Interface1", false);
//		pullUpEdit();
//	}
//
//	public void testPull93() throws Exception {
//		pullUpResolve("a", false, "p1.InterFace1");
//		pullUpEdit();
//	}
//
//	public void testPull94() throws Exception {
//		pullUpResolve("a", false, "p2.Interface1");
//		pullUpFail("Class3", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 1);
//		pullUpFail("b", "p1.Class3", "IMPORT_NOT_POSSIBLE", false, 2);
//		pullUpFail("b", "p1.Class3", "WEAK_ACCESS_OF_FOREIGN", false, 2);
//		pullUpNoStaticInit("a", "p1.Class2", "0", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPull95() throws Exception {
//		pullUpResolve("f1", true, "p1.Class1");
//		collectDataForRefactoring();
//		pullUpResolve("f3", true, "p1.Class1", false);
//		pullUpEdit();
//	}
//
//	public void testPull96() throws Exception {
//		pullUpResolve("f", true, "p1.InterFace1");
//		pullUpOtherImplementersExist("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
///***********************************/
//	public void testPull97() throws Exception {
//		pullUpResolve("func1", true, "p1.Class1");
//		pullUpEdit();
//	}
//
///***********************************/
//
//	public void testPull98() throws Exception {
//		pullUpResolve("a", false, "p1.Interface1");
//		pullUpEdit();
//	}
//
//	public void testPull99() throws Exception {
//		pullUpResolve("a", false, "p1.Interface1");
//		pullUpNoStaticInit("a", "p1.Class2", "null", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPull100() throws Exception {
//		pullUpResolve("a", false, "p1.Class1");
//		pullUpUnmovableUses("a", "p1.Class2", false, 1);
//		pullUpEdit();
//	}
//
//	public void testPull101() throws Exception {
//		isImplementationGlobal = false;
//		pullUpResolve("f", true, "p1.Class1");
//		pullUpWeakAccessForAbstract("f", "p1.Class2", true, 2);
//    pullUpOtherImplementersExist("f", "p1.Class2", true, 1);
//		pullUpEdit();
//	}
//
//	public void testPull102() throws Exception {
//		pullUpResolve("tmp1", false, "p2.Class10");
//		pullUpEdit();
//	}
//
//	public void testPull103() throws Exception {
//		pullUpResolve("b", false, "p1.Class1");
//		pullUpEdit();
//	}

  protected void setUp() {
  }

  protected void tearDown() {
  }
}
