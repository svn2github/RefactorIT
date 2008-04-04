/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vcs;



import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.netbeans.common.testmodule.NBTestRunnerModule;
import net.sf.refactorit.test.netbeans.vcs.testutil.CvsCheckOut;
import net.sf.refactorit.test.netbeans.vcs.testutil.TestRepository;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;


public class CrLfTest extends TestCase {

  public static final String CR_LF_FILE_CONTENTS = "\r\n \r \r \n \n";

  public static void setUp(CvsCheckOut checkout) throws IOException,
      InterruptedException, NBTestRunnerModule.CancelledException {
    File crLf = new File(new File(checkout.dir, "a"), "CrLfBinary.java");
    createMixedCrLfFile(crLf);
    checkout.addBinary(crLf);

    crLf = new File(new File(checkout.dir, "a"), "CrLfText.java");
    createMixedCrLfFile(crLf);
    checkout.add(crLf);
  }

  private static void createMixedCrLfFile(File f) throws IOException {
    if (!f.exists()) {
      f.createNewFile();
    }

    FileCopier.writeStringToFile(f, CR_LF_FILE_CONTENTS);
  }

  public static void testCrLfBinary_prepare() throws Exception {
    CompilationUnit compilationUnit = TestRepository.nbProject.getCompilationUnitForName(
        "a/CrLfBinary.java");
    String content = compilationUnit.getContent();
    assertEquals(CR_LF_FILE_CONTENTS, content);

    Source source = compilationUnit.getSource();
    Source result = source.renameTo(source.getParent(), "CrLfBinary2.java");
    assertEquals(CR_LF_FILE_CONTENTS, new String(result.getContent()));
  }

  public static void testCrLfText_prepare() throws Exception {
    CompilationUnit compilationUnit = TestRepository.nbProject.getCompilationUnitForName(
        "a/CrLfText.java");
    String content = compilationUnit.getContent();
    assertTrue(!CR_LF_FILE_CONTENTS.equals(content));

    Source source = compilationUnit.getSource();
    Source result = source.renameTo(source.getParent(), "CrLfText2.java");
    assertEquals(content, new String(result.getContent()));

    OutputStream s = result.getOutputStream();
    s.write(CR_LF_FILE_CONTENTS.getBytes());
    s.close();
  }

  public static void testCrLfBinary_assert() throws Exception {
    Source source = TestRepository.localCheckoutDir.getChild("a").getChild(
        "CrLfBinary2.java");

    String expected = CR_LF_FILE_CONTENTS;
    String actual = new String(source.getContent());
    assertEquals(asHex(expected) + " vs " + asHex(actual), expected, actual);
  }

  public static void testCrLfText_assert() throws Exception {
    Source source = TestRepository.localCheckoutDir.getChild("a").getChild(
        "CrLfText2.java");

    String actual = new String(source.getContent());
    assertTrue(!CR_LF_FILE_CONTENTS.equals(actual));
  }

  private static String asHex(String s) {
    byte[] bytes = s.getBytes();
    String result = "";
    for (int i = 0; i < bytes.length; i++) {
      result += Integer.toHexString(bytes[i]) + " ";
    }
    return result;
  }
}
