/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;



import net.sf.refactorit.cli.Cli;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;
import net.sf.refactorit.ui.options.profile.ProfileUtil;

import java.io.IOException;


/**
 * @author Risto
 */
public class CliTest extends ConsoleTestCase {
  public void testUnknownParameter() throws IOException {
    new Cli().run(new String[] {"-asd", // this one is an unknown parameter
        "-sourcepath", ".", "-classpath", ".", "-metrics"});
    assertEquals("ERROR: Unknown parameter(s): [-asd]", getOut().trim());
  }

  public void testProfileIsValid() throws IOException {
    assertFalse(ProfileUtil.isValidProfile(ProfileTestUtil.
        createCorruptedProfile()));
    assertTrue(ProfileUtil.isValidProfile(ProfileTestUtil.createOkProfile()));
  }
}
