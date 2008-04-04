/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Category;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;


public abstract class AbstractRegressionTest extends TestCase {
  private static final Category cat
      = Category.getInstance(AbstractRegressionTest.class.getName());

  int oldJvmMode = FastJavaLexer.JVM_14;

  protected AbstractRegressionTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
//    super.setUp();
//    if (this.verifier == null) {
//      this.verifier = new TestVerifier(true);
//      this.createdVerifier = true;
//    }
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  protected void tearDown() throws Exception {
//    if (this.createdVerifier) {
//      this.stop();
//    }
//    // clean up output dir
//    File outputDir = new File(OUTPUT_DIR);
//    if (outputDir.exists()) {
//      Util.flushDirectoryContent(outputDir);
//      outputDir.delete();
//    }
//    super.tearDown();
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  protected void runConformTest(String[] testFiles) {
    runConformTest(testFiles, null, null, true, null);
  }

  protected void runConformTest(String[] testFiles, String[] vmArguments) {
    runConformTest(testFiles, null, null, true, vmArguments);
  }

  protected void runConformTest(
      String[] testFiles,
      String expectedSuccessOutputString,
      String[] vmArguments) {

    runConformTest(testFiles, expectedSuccessOutputString, null, true,
        vmArguments);
  }

  protected void runConformTest(String[] testFiles,
      String expectedSuccessOutputString) {
    runConformTest(testFiles, expectedSuccessOutputString, null, true, null);
  }

  protected void runConformTest(
      String[] testFiles,
      String expectedSuccessOutputString,
      String[] classLib,
      boolean shouldFlushOutputDirectory,
      String[] vmArguments) {

    runConformTest(
        testFiles,
        expectedSuccessOutputString,
        classLib,
        shouldFlushOutputDirectory,
        vmArguments,
        null);
  }

  protected void runConformTest(
      String[] testFiles,
      String expectedSuccessOutputString,
      String[] classLib,
      boolean shouldFlushOutputDirectory,
      String[] vmArguments,
      Map customOptions) {

    cat.info("Testing whether project \"" + getName() + "\" loads properly");

    if (shouldFlushOutputDirectory) {
      Utils.flushHistory();
    }

    Project project;
    try {
      project = Utils.createTestRbProjectWithManyFiles(testFiles);
      project.getProjectLoader().build();
    } catch (Exception e) {
      project = null;
      e.printStackTrace();
      fail(e.getMessage());
    }

    if (!(project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors()) {
      final AbstractIndexer indexer = new AbstractIndexer() {};
      indexer.visit(project);
    }

    final Iterator errors = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors();
    if (errors.hasNext()) {
      String message = "Got errors: \n";
      int errorIndex = 0;
      while (errors.hasNext()) {
        final Object exception = errors.next();
        message += "Error #" + (errorIndex + 1) + ": " + exception + "\n";
        errorIndex++;
      }

      cat.error(message);
      fail(message);
    } else if ((project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors()) {
        cat.error("Project has critical user errors");
        fail("Project has critical user errors");
      }
    cat.info("SUCCESS");

//		if (shouldFlushOutputDirectory)
//			Util.flushDirectoryContent(new File(OUTPUT_DIR));
//
//		IProblemFactory problemFactory = getProblemFactory();
//		Requestor requestor =
//			new Requestor(
//				problemFactory,
//				OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator,
//				false);
//
//		Map options = getCompilerOptions();
//		if (customOptions != null) {
//			options.putAll(customOptions);
//		}
//		Compiler batchCompiler =
//			new Compiler(
//				getNameEnvironment(new String[]{}, classLib),
//				getErrorHandlingPolicy(),
//				options,
//				requestor,
//				problemFactory);
//		try {
//			batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
//		} catch(RuntimeException e) {
//			System.out.println(getClass().getName() + '#' + getName());
//			e.printStackTrace();
//			for (int i = 0; i < testFiles.length; i += 2) {
//				System.out.print(testFiles[i]);
//				System.out.println(" ["); //$NON-NLS-1$
//				System.out.println(testFiles[i + 1]);
//				System.out.println("]"); //$NON-NLS-1$
//			}
//			throw e;
//		}
//		if (!requestor.hasErrors) {
//			String sourceFile = testFiles[0];
//
//			// Compute class name by removing ".java" and replacing slashes with dots
//			String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
//
//			if (vmArguments != null) {
//				if (this.verifier != null) {
//					this.verifier.shutDown();
//				}
//				this.verifier = new TestVerifier(false);
//				this.createdVerifier = true;
//			}
//			boolean passed =
//				this.verifier.verifyClassFiles(
//					sourceFile,
//					className,
//					expectedSuccessOutputString,
//					this.classpaths,
//					null,
//					vmArguments);
//			if (!passed) {
//				System.out.println(getClass().getName() + '#' + getName());
//				for (int i = 0; i < testFiles.length; i += 2) {
//					System.out.print(testFiles[i]);
//					System.out.println(" ["); //$NON-NLS-1$
//					System.out.println(testFiles[i + 1]);
//					System.out.println("]"); //$NON-NLS-1$
//				}
//			}
//			assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
//					passed);
//			if (vmArguments != null) {
//				if (this.verifier != null) {
//					this.verifier.shutDown();
//				}
//				this.verifier = new TestVerifier(false);
//				this.createdVerifier = true;
//			}
//		} else {
//			System.out.println(getClass().getName() + '#' + getName());
//			System.out.println(Util.displayString(requestor.problemLog, 2));
//			for (int i = 0; i < testFiles.length; i += 2) {
//				System.out.print(testFiles[i]);
//				System.out.println(" ["); //$NON-NLS-1$
//				System.out.println(testFiles[i + 1]);
//				System.out.println("]"); //$NON-NLS-1$
//			}
//			assertTrue("Unexpected problems: " + requestor.problemLog, false);
//		}
  }

  /**
   * Log contains all problems (warnings+errors)
   */
  protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
    runNegativeTest(testFiles, expectedProblemLog, null, true);
  }

  /**
   * Log contains all problems (warnings+errors)
   */
  protected void runNegativeTest(
      String[] testFiles,
      String expectedProblemLog,
      String[] classLib,
      boolean shouldFlushOutputDirectory) {

    runNegativeTest(testFiles, expectedProblemLog, classLib,
        shouldFlushOutputDirectory, null);
  }

  /**
   * Log contains all problems (warnings+errors)
   */
  protected void runNegativeTest(
      String[] testFiles,
      String expectedProblemLog,
      String[] classLib,
      boolean shouldFlushOutputDirectory,
      Map customOptions) {

    runNegativeTest(testFiles, expectedProblemLog, classLib,
        shouldFlushOutputDirectory, customOptions, false);
  }

  /**
   * Log contains all problems (warnings+errors)
   */
  protected void runNegativeTest(
      String[] testFiles,
      String expectedProblemLog,
      String[] classLib,
      boolean shouldFlushOutputDirectory,
      Map customOptions,
      boolean generateOutput) {

    cat.info("Testing whether project \"" + getName() + "\" loads properly");

    if (shouldFlushOutputDirectory) {
      Utils.flushHistory();
    }

    Project project;
    try {
      project = Utils.createTestRbProjectWithManyFiles(testFiles);
      project.getProjectLoader().build();
    } catch (Exception e) {
      project = null;
      fail(e.getMessage());
    }

    if (!(project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()
        && !(project.getProjectLoader().getErrorCollector()).hasUserFriendlyInfos()) {
      final AbstractIndexer indexer = new AbstractIndexer() {};
      indexer.visit(project);

      if (RefactorItConstants.runNotImplementedTests) {
        assertTrue("Has parsing errors", (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()
            || (project.getProjectLoader().getErrorCollector()).hasUserFriendlyInfos());
      }
    }

    assertFalse("Does not have critical errors", (project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors());

//    final Iterator errors = project.getUserFriendlyErrors();
//    if (errors.hasNext()) {
//      String message = "Got errors: \n";
//      int errorIndex = 0;
//      while (errors.hasNext()) {
//        final Object exception = errors.next();
//        message += "Error #" + (errorIndex + 1) + ": " + exception + "\n";
//        errorIndex++;
//      }
//
//      cat.error(message);
//      fail(message);
//    } else if (project.hasCriticalUserErrors()) {
//      cat.error("Project has critical user errors");
//      fail("Project has critical user errors");
//    }
//    cat.info("SUCCESS");

//		if (shouldFlushOutputDirectory)
//			Util.flushDirectoryContent(new File(OUTPUT_DIR));
//
//		IProblemFactory problemFactory = getProblemFactory();
//		Requestor requestor =
//			new Requestor(
//				problemFactory,
//				OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator,
//				generateOutput);
//		Map options = getCompilerOptions();
//		if (customOptions != null) {
//			options.putAll(customOptions);
//		}
//		Compiler batchCompiler =
//			new Compiler(
//				getNameEnvironment(new String[]{}, classLib),
//				getErrorHandlingPolicy(),
//				options,
//				requestor, problemFactory);
//		batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
//		String computedProblemLog = Util.convertToIndependantLineDelimiter(requestor.problemLog.toString());
//		String platformIndependantExpectedLog = Util.convertToIndependantLineDelimiter(expectedProblemLog);
//		if (!platformIndependantExpectedLog.equals(computedProblemLog)) {
//			System.out.println(getClass().getName() + '#' + getName());
//			System.out.println(Util.displayString(computedProblemLog, 2));
//			for (int i = 0; i < testFiles.length; i += 2) {
//				System.out.print(testFiles[i]);
//				System.out.println(" ["); //$NON-NLS-1$
//				System.out.println(testFiles[i + 1]);
//				System.out.println("]"); //$NON-NLS-1$
//			}
//		}
//		assertEquals("Invalid problem log ", platformIndependantExpectedLog, computedProblemLog);
  }
}
