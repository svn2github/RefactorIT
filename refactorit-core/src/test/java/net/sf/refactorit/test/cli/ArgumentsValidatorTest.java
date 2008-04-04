/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.cli.ArgumentsValidator;
import net.sf.refactorit.cli.ProjectInitException;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;



public class ArgumentsValidatorTest extends ConsoleTestCase {
  private ArgumentsValidator v;

  public void setUp() throws Exception {
    super.setUp();

    v = new ArgumentsValidator();
  }

  public void testCheckProfileMethod_invalidProfile() throws Exception {
    try {
      v.checkProfile(ProfileTestUtil.createCorruptedProfile().
          getAbsolutePath());
      fail("Wanted an exception");
    } catch (ProjectInitException ex) {
      assertTrue(ex.getMessage(), ex.getMessage().trim().startsWith(
          "ERROR: Corrupted profile: "));
    }
  }

  public void testCheckProfileMethod_missingProfile() throws Exception {
    try {
      v.checkProfile("some-missing-file");
      fail("Wanted an exception");
    } catch (ProjectInitException ex) {
      assertTrue(ex.getMessage(), ex.getMessage().trim().startsWith(
          "ERROR: Profile does not exist: "));
    }
  }

  public void testEmptyNameProfileIsValid() throws ProjectInitException {
    v.checkProfile("");
  }

  public void testCheckActionPresent() throws Exception {
    v.checkActionPresent(new StringArrayArguments("-notused"));

    try {
      v.checkActionPresent(new StringArrayArguments(""));
      fail("Must have thrown an exception");
    } catch (ProjectInitException pass) {
      assertEquals("ERROR: Action name missing",
          pass.getMessage());
    }
  }

  public void testCheckProfileSupport_yes() {
    v.checkProfileSupport(new StringArrayArguments("-audit -profile x"));
    assertEquals("", getOut());
  }

  public void testCheckProfileSupport_yes2() {
    v.checkProfileSupport(new StringArrayArguments("-metrics -profile x"));
    assertEquals("", getOut());
  }

  public void testCheckProfileSupport_no() {
    v.checkProfileSupport(new StringArrayArguments("-notused -profile x"));
    assertEquals(1, v.getWarnings().size());
    assertEquals(
        "WARNING: This action does not support the -profile switch, ignoring",
        v.getWarnings().get(0));
  }

  public void testCheckProfileSupport_noProfileArgument() {
    v.checkProfileSupport(new StringArrayArguments("-notused"));
    assertEquals("", getOut().trim());
  }

}
