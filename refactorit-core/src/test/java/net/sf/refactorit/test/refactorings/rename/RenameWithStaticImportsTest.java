/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author tanel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RenameWithStaticImportsTest extends RefactoringTestCase {

	/**
	 * @param name
	 */
	public RenameWithStaticImportsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(RenameWithStaticImportsTest.class);
	}

	protected void tearDown() {
		DialogManager.setInstance(new NullDialogManager());
	}

	/*
	 * @see net.sf.refactorit.test.refactorings.RefactoringTestCase#getTemplate()
	 */
	public String getTemplate() {
		return "RenameWithStaticImports/<stripped_test_name>/<in_out>";
	}

	private void renameFieldMustWork(String typeName, String fieldName, String newFieldName,
			boolean updateReferences) throws Exception {
		final Project project =
			RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();
		BinTypeRef aRef = project.findTypeRefForName(typeName);
		final BinField renamable = aRef.getBinCIType().getDeclaredField(fieldName);

		final RenameField renamer
		= new RenameField(new NullContext(project), renamable);
		renamer.setNewName(newFieldName);

		RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(renamer);

		if (status != null) {
			assertTrue(
					"Renaming " + renamable.getQualifiedName() + " -> " + newFieldName
					+ " succeeded: " + status.getAllMessages(),
					status.isOk());
		}

		RwRefactoringTestUtils.assertSameSources(
				"Renamed " + renamable.getQualifiedName() + " -> " + newFieldName,
				getExpectedProject(),
				project);
	}

	private void renameMustWork(String typeName, String fieldName,
			String newFieldName) throws Exception {
		renameFieldMustWork(typeName, fieldName, newFieldName, true);
	}

	/**
	 * @param oldName
	 * @param newName
	 * @throws Exception
	 */
	private void renameTypeMustWork(String oldName, String newName) throws Exception {
		final Project project =
			RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef = project.findTypeRefForName(oldName);

		final RenameType renamer
		= new RenameType(new NullContext(project), aRef.getBinCIType());

		renamer.setNewName(newName);

		RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(renamer);

		if (status != null) {
			assertTrue(
					"Renaming " + aRef.getQualifiedName() + " -> " + newName
					+ " succeeded: " + status.getAllMessages(),
					status.isOk());
		}
		RwRefactoringTestUtils.assertSameSources(
				"Renamed " + aRef.getQualifiedName() + " -> " + newName,
				getExpectedProject(),
				project);
	}


	public void testRenameField() throws Exception {
    renameMustWork("A", "FOO", "FOO2");
	}

	public void testRenameClass() throws Exception {
		String oldName = "A";
		String newName = "A2";

		renameTypeMustWork(oldName, newName);
	}




	public void testRenamePackage() throws Exception {
		final Project project =
			RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();
		BinPackage aPackage = project.getPackageForName("test");

		final RenamePackage renamer
		= new RenamePackage(new NullContext(project), aPackage);
		String newName = "test2";
		renamer.setNewName(newName);

		RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(renamer);

		if (status != null) {
			assertTrue(
					"Renaming " + aPackage.getQualifiedName() + " -> " + newName
					+ " succeeded: " + status.getAllMessages(),
					status.isOk());
		}
		RwRefactoringTestUtils.assertSameSources(
				"Renamed " + aPackage.getQualifiedName() + " -> " + newName,
				getExpectedProject(),
				project);
	}

	public void testRenameImportedType() throws Exception {
    renameTypeMustWork("A$Foo", "Foo2");
	}

	public void renameMethodMustWork(String typeName, String methodName, String[] signature, String newMethodName, boolean mustWork) throws Exception {
		final Project project =
			RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();
		BinTypeRef aRef = project.findTypeRefForName(typeName);

		final BinMethod renamable = aRef.getBinCIType().getDeclaredMethod(methodName,
				RenameMethodTest.convertSingature(project, signature));

		final RenameMethod renamer
		= new RenameMethod(new NullContext(project), renamable);
		renamer.setNewName(newMethodName);

		RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(renamer);
		if (mustWork) {
			if (status != null) {
				assertTrue(
						"Renaming " + renamable.getQualifiedName() + " -> " + newMethodName
						+ " failed: " + status.getAllMessages(),
						status.isOk());
			}

			RwRefactoringTestUtils.assertSameSources(
					"Renamed " + renamable.getQualifiedName() + " -> " + newMethodName,
					getExpectedProject(),
					project);
		} else {
			assertFalse(
					"Renaming " + renamable.getQualifiedName() + " -> " + newMethodName
					+ " succeeded: " + status.getAllMessages(),
					status.isOk());

		}

	}

	public void testRenameMethod() throws Exception {
		renameMethodMustWork("A", "out", new String[] {"QString;"}, "print", true);
	}


	public void testShadesStaticImport() throws Exception {
		renameMethodMustWork("A", "out", new String[] {"QString;"}, "load", false);
	}
}
