/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.JWordDialog;
import net.sf.refactorit.ui.module.fixmescanner.Timestamper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FixmeTimestampTest extends TestCase {
  private Project testProject;
  private String tmpFileName = null;
  private Calendar timestamp;
  private DateFormat format = DateFormat.getDateInstance(
      DateFormat.SHORT, Locale.FRENCH);

  public FixmeTimestampTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(FixmeTimestampTest.class);
    suite.setName("FIXME Timestamp");
    return suite;
  }

  private CompilationUnit getCompilationUnit(String name) {
    return testProject.getCompilationUnitForName(name);
  }

  private String copyFile(String fromFile, String toFile,
      byte toSkip) throws Exception {
    InputStream in = getCompilationUnit(fromFile).getSource().getInputStream();

    byte[] bytes = new byte[in.available()];
    in.read(bytes, 0, in.available());

    String name =
        getCompilationUnit(fromFile).getSource()
        .getFileOrNull().getAbsolutePath();
    name = StringUtil.replace(name, fromFile, toFile);
    OutputStream out = new FileOutputStream(name);
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] != toSkip) {
        out.write(bytes[i]);
      }
    }

    in.close();
    out.close();

    reloadTestProject();

    return name;
  }

  private void assertEqualFiles(String f1, String f2,
      byte toSkipInSecond) throws Exception {
    InputStream in1 = getCompilationUnit(f1).getSource().getInputStream();
    InputStream in2 = getCompilationUnit(f2).getSource().getInputStream();

    int pos = 0;

    while (in1.available() > 0) {
      pos++;

      int aByte = in1.read();
      if (aByte != toSkipInSecond) {
        assertEquals("Position " + pos + " (1st pos is 1) must be equal", aByte,
            in2.read());
      }
    }

    in1.close();
    in2.close();
  }

  private void reloadTestProject() throws Exception {
    this.testProject = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("FIXME Timestamp")
        );
    this.testProject.getProjectLoader().build();
  }

  public void setUp() throws Exception {
    reloadTestProject();
    this.tmpFileName = copyFile("TestI.java", "Test.java", (byte) 'I');

    timestamp = Calendar.getInstance();
    timestamp.set(2345, 1, 22);
  }

  public void tearDown() throws Exception {
    new File(this.tmpFileName).delete();
  }

  private Timestamper getTimestamper(String fileName) {
    List filesToScan = new ArrayList(1);
    filesToScan.add(getCompilationUnit(fileName));

    List wordsToLookFor = new ArrayList(2);
    wordsToLookFor.add("ATTN");
    wordsToLookFor.add("PS");

    return new Timestamper(filesToScan, wordsToLookFor, true, 0, 0);
  }

  public void testOnce() throws Exception {
    getTimestamper("Test.java").applyTimestamp(timestamp, format);

    assertEqualFiles("TestO.java", "Test.java", (byte) 'O');
  }

  public void testInternalCommentTextUpdate() throws Exception {
    Timestamper timestamper = getTimestamper("Test.java");
    timestamper.applyTimestamp(timestamp, format);
    timestamper.applyTimestamp(timestamp, format);
    timestamper.applyTimestamp(timestamp, format);

    assertEqualFiles("TestO.java", "Test.java", (byte) 'O');
  }

  public void testNoNpeWithLowercaseWords() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// hack this is lowercase\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// hack(22/02/45) this is lowercase\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Timestamper timestamper = new Timestamper(before.getCompilationUnits(),
        CollectionUtil.singletonArrayList("HACK"), true, 0, 0);
    timestamper.applyTimestamp(timestamp, format);

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testTimestampWithRegexpWords() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// hack this has regexp\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// (22/02/45)hack this has regexp\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Timestamper timestamper = new Timestamper(before.getCompilationUnits(),
        CollectionUtil.singletonArrayList(new JWordDialog.Word("hack", true)), true,
        0, 0);
    timestamper.applyTimestamp(timestamp, format);

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReckognizingTimstampsWrittenForOtherLocalesAndStyles() throws
      Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// hack(Tuesday, April 12, 1952 AD) this is lowercase\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "// hack(Tuesday, April 12, 1952 AD) this is lowercase\n" +
        "public class X{}",
        "X.java", null
        )
    });

    Timestamper timestamper = new Timestamper(before.getCompilationUnits(),
        CollectionUtil.singletonArrayList("HACK"), true, 0, 0);
    timestamper.applyTimestamp(timestamp, format);

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public static void main(String[] args) throws Exception {
    System.err.println("Sleeping 5 sec -- attach debugger now");
    Thread.sleep(5000);

    FixmeTimestampTest test = new FixmeTimestampTest("");

    test.setUp();
    try {
      test.testOnce();
    } finally {
      test.tearDown();
    }
  }
}
