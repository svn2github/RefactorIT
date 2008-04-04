/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;



import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.ui.options.profile.Profile;

import java.io.File;
import java.io.IOException;


public class ProfileTestUtil {
  public static File createCorruptedProfile() throws IOException {
    File result = TempFileCreator.getInstance().createRootFile().getFileOrNull();
    FileCopier.writeStringToFile(result, "<xml");
    return result;
  }

  public static File createOkProfile() throws IOException {
    File result = TempFileCreator.getInstance().createRootFile().getFileOrNull();
    Profile.createDefaultAudit().serialize(result);
    return result;
  }
}
