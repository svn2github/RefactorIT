/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.ArgumentsParser;
import net.sf.refactorit.cli.ArgumentsValidator;
import net.sf.refactorit.cli.UsageInfo;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.standalone.StartUp;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.ConsoleTestCase;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.utils.LinePositionUtil;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Risto Alas
 * @author Anton Safonov
 */
public class MainTest extends ConsoleTestCase {
  public static final File metricsFolder
      = new File(Utils.getTestProjectsDirectory(), "Metrics");
  public static final File defaultMetrics
      = new File(metricsFolder, "DefaultMetrics.profile");

  public static final File auditFolder
      = new File(Utils.getTestProjectsDirectory(), "Audit");
  public static final File defaultAudit
      = new File(auditFolder, "DefaultAudit.profile");

  public static Test suite() {return new TestSuite(MainTest.class);
  }

  private IDEController oldInstance;

  public void setUp() throws Exception {
    ModuleManager.loadModules();

    super.setUp();

    oldInstance = IDEController.getInstance();
  }

  public void tearDown() {
    if (oldInstance != null) {
      IDEController.setInstance(oldInstance);
    }
  }

//  public void testRunNotUsed() throws Exception {
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", createSimpleSourcepath(),
//        "-classpath", ".",
//        "-notused"});
//
//    assertEqualsIgnoreWhitespace("\n" + SIMPLE_NOT_USED_OUTPUT, getOut());
//  }
//
//  public void testWarningAboutClaspathNotSet() throws Exception {
//    Project p = Utils.createTestRbProjectFromArray(new String[] {
//        "class X {}", ""});
//    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
//        getSource().getAbsolutePath();
//
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", sourcepath,
//        "-notused"});
//
//    assertEqualsIgnoreWhitespace(
//        Arguments.WARNING + ArgumentsParser.ARGS.get(ArgumentsParser.CLASSPATH) +
//        Arguments.PARAMETER_IS_MISSING + "\n\n" +
//        "'Type' 'Not Used' 'Line' 'Source' 'Package' 'Class'\n" +
//        " Overall Not Used (1) \n" +
//        " Whole Types (1) \n" +
//        "Package <default package> <default package> \n" +
//        "Class X 1  class X {} <default package> X\n",
//        getOut());
//  }

  public void testParsingError() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X extends Missing{}", ""});
    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
        getSource().getAbsolutePath();

    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", sourcepath,
        "-classpath", ".",
        "-notused"});

    final String got = LinePositionUtil.useUnixNewlines(getOut());
//    FileWriter wr = new FileWriter(new File("C:/test.txt"));
//    wr.write("aaa: \"" + StringUtil.printableLinebreaks(got) + "\"");
//    wr.flush();
//    wr.close();
    final String expected = "\n\n"+"X0.java 1:1 - Could not resolve superclass Missing at location: X0.java\n\n" +
        "X0.java 1:17 - Class not found: Missing {IDENT} [1:17 - 1:24], 3\n\n\n";
    assertEqualsIgnoreWhitespace(expected, got);
  }

  public void testNonexistingPaths() throws Exception {
    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", "some-missing-folder",
        "-classpath", "some-missing-folder",
        "-notused"});

    assertEqualsIgnoreWhitespace(
        "\n\n" +
        "ERROR: Sourcepath is empty\n",
        LinePositionUtil.useUnixNewlines(getOut()));
  }

  public void testJustNoguiSwitch() throws Exception {
    StartUp.main(new String[] {"-nogui", "-metrics"});

    assertEqualsIgnoreWhitespace(
        Arguments.WARNING + ArgumentsParser.ARGS.get(ArgumentsParser.SOURCEPATH) +
        Arguments.PARAMETER_IS_MISSING + "\n\n\n" +
        "ERROR: Sourcepath is empty\n",
        LinePositionUtil.useUnixNewlines(getOut()));
  }

//  public void testRunMetrics() throws Exception {
//    Project p = Utils.createTestRbProjectFromArray(new String[] {
//        "class X {}", ""});
//    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
//        getSource().getAbsolutePath();
//
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", sourcepath,
//        "-classpath", ".",
//        "-metrics",
//        "-format", "text",
//        "-profile", defaultMetrics.getPath()
//    });
//
//    assertEqualsIgnoreWhitespace(
//        "\n" +
//        "'Type' 'Target' 'V(G)' 'LOC' 'NCLOC' 'CLOC' 'DC' 'NP' 'EXEC' 'WMC' 'RFC' 'DIT' 'NOC' 'Ca' 'Ce' 'I' 'A' 'NOT' 'NOTc' 'LSP' 'DIP' 'Package' 'Class'\n" +
//        " Metrics 1 2 0 0.0 1 0 1 0 \n" +
//        "Package <default package> 1 2 0 0.0 1 0 1 0 1 0.0 0 <default package> \n" +
//        "Class X 0 0 0 0.0 0 1 1 0 1 0 1 0 <default package> X\n",
//        getOut());
//  }

//  public void testRunMetrics_Html() throws Exception {
//    Project p = Utils.createTestRbProjectFromArray(new String[] {
//        "class X {}", ""});
//    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
//        getSource().getAbsolutePath();
//
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", sourcepath,
//        "-classpath", ".",
//        "-metrics",
//        "-format", "html",
//        "-profile", defaultMetrics.getPath()
//    });
//
//    assertEqualsIgnoreWhitespace(
//        "\n" +
//        "<html>\n" +
//        "<title></title>\n" +
//        "<body>\n" +
//        "<table border=1>\n" +
//        "<TR><th align=center>Type</th><th align=center>Target</th>" +
//        "<th align=center>V(G)</th><th align=center>LOC</th><th align=center>NCLOC</th>" +
//        "<th align=center>CLOC</th><th align=center>DC</th><th align=center>NP</th>" +
//        "<th align=center>EXEC</th><th align=center>WMC</th><th align=center>RFC</th>" +
//        "<th align=center>DIT</th><th align=center>NOC</th><th align=center>Ca</th>" +
//        "<th align=center>Ce</th><th align=center>I</th><th align=center>A</th>" +
//        "<th align=center>NOT</th><th align=center>NOTc</th><th align=center>LSP</th>" +
//        "<th align=center>DIP</th><th align=center>Package</th><th align=center>Class</th>" +
//        "</TR>\n" +
//        "<TR><TD>&nbsp;</TD><TD>Metrics</TD><TD>&nbsp;</TD><TD>1</TD><TD>2</TD><TD>0</TD><TD>0.0</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>1</TD><TD>0</TD><TD>1</TD><TD>0</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>\n" +
//        "<TR><TD>Package</TD><TD>&lt;default&nbsp;package&gt;</TD><TD>&nbsp;</TD><TD>1</TD><TD>2</TD><TD>0</TD><TD>0.0</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>1</TD><TD>0</TD><TD>1</TD><TD>0</TD><TD>1</TD><TD>0.0</TD><TD>0</TD><TD>&nbsp;</TD><TD>&lt;default&nbsp;package&gt;</TD><TD>&nbsp;</TD></TR>\n" +
//        "<TR><TD>Class</TD><TD>X</TD><TD>&nbsp;</TD><TD>0</TD><TD>0</TD><TD>0</TD><TD>0.0</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>0</TD><TD>1</TD><TD>1</TD><TD>0</TD><TD>1</TD><TD>0</TD><TD>1</TD><TD>0</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&lt;default&nbsp;package&gt;</TD><TD>X</TD></TR>\n\n" +
//        "</table>\n" +
//        "</body>\n" +
//        "</html>\n",
//        getOut());
//  }

  public void testMetricsWithBadFormat() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {}", ""});
    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
        getSource().getAbsolutePath();

    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", sourcepath,
        "-classpath", ".",
        "-metrics",
        "-format", "some-unsupported-format",
        "-profile", defaultMetrics.getPath()
    });

    assertEqualsIgnoreWhitespace(
        "ERROR: Unsupported format: some-unsupported-format",
        getOut().trim());
  }

  public void testNotUsedWithBadFormat() throws Exception {
    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", createSimpleSourcepath(),
        "-classpath", ".",
        "-notused",
        "-format", "some-unsupported-format"});

    assertEqualsIgnoreWhitespace(
        "ERROR: Unsupported format: some-unsupported-format",
        getOut().trim());
  }

//  public void testOutputToFile() throws Exception {
//    File out = TempFileCreator.getInstance().createRootFile().getFileOrNull();
//
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", createSimpleSourcepath(),
//        "-classpath", ".",
//        "-notused",
//        "-output", out.getAbsolutePath()});
//
//    assertEqualsIgnoreWhitespace("\n", getOut());
//    assertEqualsIgnoreWhitespace(SIMPLE_NOT_USED_OUTPUT,
//        FileUtil.readFileToString(out));
//  }
//
//  public void testRunAudit() throws Exception {
//    Project p = Utils.createSimpleProject();
//    AuditSupportTest t = new AuditSupportTest();
//    t.setUpFormatting();
//
//    try {
//      StartUp.main(new String[] {
//          "-nogui",
//          "-sourcepath", getSourcepath(p),
//          "-classpath", ".",
//          "-audit",
//          "-format", "text",
//          "-profile", defaultAudit.getPath()
//      });
//
//
//      //final String got = getOut();
//      //final String expected = "\n" + AuditSupportTest.createSimpleOutput();
//
//      assertEqualsIgnoreWhitespace(expected, got);
//    } finally {
//      t.tearDownFormatting();
//    }
//  }

//  public void testAuditProfile() throws Exception {
//    Project p = Utils.createSimpleProject();
//    AuditSupportTest t = new AuditSupportTest();
//    t.setUpFormatting();
//
//    try {
//      StartUp.main(new String[] {
//          "-nogui",
//          "-sourcepath", getSourcepath(p),
//          "-classpath", ".",
//          "-audit",
//          "-format", "text",
//          "-profile", AuditModelBuilderTest.nothing.getPath()
//      });
//
//      assertEqualsIgnoreWhitespace(
//          "\n" + AuditSupportTest.createNoOutput(),
//          getOut());
//    } finally {
//      t.tearDownFormatting();
//    }
//  }

  public void testCorruptedProfile() throws Exception {
    File badFile = ProfileTestUtil.createCorruptedProfile();

    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", ".",
        "-classpath", ".",
        "-audit",
        "-profile", badFile.getPath()});

    assertTrue(getOut(),
        getOut().indexOf("ERROR: Corrupted profile: " + badFile.getPath()) >= 0);
  }

//  public void testMetricsProfile() throws Exception {
//    Project p = Utils.createSimpleProject();
//
//    StartUp.main(new String[] {
//        "-nogui",
//        "-sourcepath", getSourcepath(p),
//        "-classpath", ".",
//        "-metrics",
//        "-profile", MetricsModelBuilderTest.loc.getPath()});
//
//    assertEqualsIgnoreWhitespace(
//        "\n" +
//        "'Type' 'Target' 'LOC' 'Package' 'Class'\n" +
//        " Metrics 1 \n" +
//        "Package <default package> 1 <default package> \n" +
//        "Class  X   1 <default package> X\n" +
//        "Method m() 1 <default package> X\n",
//        getOut());
//  }

  public void testMissingAction() throws Exception {
    Project p = Utils.createSimpleProject();

    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", getSourcepath(p),
        "-classpath", "."});
    // Action name missing

    assertEqualsIgnoreWhitespace(
        "\nERROR: Action name missing\n",
        getOut());
  }

  public void testWarningAboutExtraProfileSwitch() throws Exception {
    Project p = Utils.createSimpleProject();

    StartUp.main(new String[] {
        "-nogui",
        "-sourcepath", getSourcepath(p),
        "-classpath", ".",
        "-notused",
        "-profile", "x"}); // NotUsed does not support profiles

    assertTrue(
        Utils.normalizeWhitespace(getOut()),
        Utils.normalizeWhitespace(getOut()).startsWith(
        Arguments.WARNING + ArgumentsValidator.PROFILE_NOT_SUPPORTED + "\n\n" +
        "Package"));
  }

//  public void testCommaSeparatedFormat() throws Exception {
//    Project p = Utils.createSimpleProject();
//    AuditSupportTest t = new AuditSupportTest();
//    t.setUpFormatting();
//
//    try {
//      StartUp.main(new String[] {
//          "-nogui",
//          "-sourcepath", getSourcepath(p),
//          "-classpath", ".",
//          "-audit",
//          "-format", "comma-separated"});
//
//      final String expected = "\n" + AuditSupportTest.createSimpleOutputCommaSeparated();
//      final String got = getOut();
//
//      assertEqualsIgnoreWhitespace(expected, got);
//    } finally {
//      t.tearDownFormatting();
//    }
//  }

  public void testHelpPage() throws Exception {
    String[] supportedHelpSwitches = new String[] {
        "-nogui", "-somerandomswitch",
        "-help", "--help", "/help",
        "-?", "/?"
    };

    for (int i = 0; i < supportedHelpSwitches.length; i++) {
      resetOut();

      String helpSwitch = supportedHelpSwitches[i];
      StartUp.main(new String[] {helpSwitch});

      Utils.assertEqualsIgnoreWhitespace("For switch: " + helpSwitch,
          Utils.normalizeWhitespace(UsageInfo.HELP_PAGE.trim()),
          Utils.normalizeWhitespace(getOut().trim()));
    }
  }

  // Util methods

  public static String getSourcepath(Project p) {
    return ((CompilationUnit) p.getCompilationUnits().get(0)).
        getSource().getAbsolutePath();
  }

  public static void assertEqualsIgnoreWhitespace(String expected, String got) {
    assertEquals(Utils.normalizeWhitespace(expected),
        Utils.normalizeWhitespace(got));
  }

//  private static final String SIMPLE_NOT_USED_OUTPUT =
//      "'Type' 'Not Used' 'Line' 'Source' 'Package' 'Class'\n" +
//      " Overall Not Used (1) \n" +
//      " Whole Types (1) \n" +
//      "Package <default package> <default package> \n" +
//      "Class X 1  class X {} <default package> X\n";

  private String createSimpleSourcepath() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {}", ""});
    String sourcepath = ((CompilationUnit) p.getCompilationUnits().get(0)).
        getSource().getAbsolutePath();

    return sourcepath;
  }
}
