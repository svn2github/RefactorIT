/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;


public class GenericTypeTest extends AbstractComparisonTest {

  public GenericTypeTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(GenericTypeTest.class);
  }

  /*######################################
   * Specific method to let tests Sun javac compilation available...
   #######################################*/
  /*
   * Cleans up the given directory by removing all the files it contains as well
   * but leaving the directory.
   * @throws TargetException if the target path could not be cleaned up
   */
//  private void cleanupDirectory(File directory) throws TargetException {
//    if (!directory.exists()) {
//      return;
//    }
//    String[] fileNames = directory.list();
//    for (int i = 0; i < fileNames.length; i++) {
//      File file = new File(directory, fileNames[i]);
//      if (file.isDirectory()) {
//        cleanupDirectory(file);
//      } else {
//        if (!file.delete()) {
//          throw new TargetException("Could not delete file " + file.getPath());
//        }
//      }
//    }
//    if (!directory.delete()) {
//      throw new TargetException("Could not delete directory "
//          + directory.getPath());
//    }
//  }

  /*
   * Write given source test files in current output sub-directory.
   * Use test name for this sub-directory name (ie. test001, test002, etc...)
   */
//  private IPath writeFiles(String[] testFiles) {
//    // Compute and create specific dir
//    IPath outDir = new Path(Util.getOutputDirectory());
//    this.dirPath = outDir.append(shortTestName());
//    File dir = this.dirPath.toFile();
//    if (!dir.exists()) {
//      dir.mkdirs();
//    }
//
//    // For each given test files
//    IPath dirFilePath = null;
//    for (int i = 0, length = testFiles.length; i < length; i++) {
//      String contents = testFiles[i + 1];
//      String fileName = testFiles[i++];
//      IPath filePath = this.dirPath.append(fileName);
//      if (fileName.lastIndexOf('/') >= 0) {
//        dir = filePath.removeLastSegments(1).toFile();
//        if (!dir.exists()) {
//          dir.mkdirs();
//        }
//      }
//      if (dirFilePath == null
//          || (filePath.segmentCount() - 1) < dirFilePath.segmentCount()) {
//        dirFilePath = filePath.removeLastSegments(1);
//      }
//      Util.writeToFile(contents, filePath.toString());
//    }
//    return dirFilePath;
//  }

  /*
   * Write given source test files in current output sub-directory.
   * Use test name for this sub-directory name (ie. test001, test002, etc...)
   */
//  private void printFiles(String[] testFiles) {
//    for (int i = 0, length = testFiles.length; i < length; i++) {
//      System.out.println(testFiles[i++]);
//      System.out.println(testFiles[i]);
//    }
//    System.out.println("");
//  }

  /*
   * Run Sun compilation using javac.
   * Use JRE directory to retrieve javac bin directory and current classpath for
   * compilation.
   * Launch compilation in a thread and verify that it does not take more than 5s
   * to perform it. Otherwise abort the process and log in console.
   */
//  protected void runJavac(String[] testFiles, final String expectedProblemLog) {
//    try {
//      // Write files in dir
//      IPath dirFilePath = writeFiles(testFiles);
//
//      String testName = shortTestName();
//      Process process = null;
//      try {
//        // Compute classpath
//        String[] classpath = getDefaultClassPaths();
//        StringBuffer cp = new StringBuffer();
//        int length = classpath.length;
//        for (int i = 0; i < length; i++) {
//          if (classpath[i].indexOf(" ") != -1) {
//            cp.append("\"" + classpath[i] + "\"");
//          } else {
//            cp.append(classpath[i]);
//          }
//          if (i < (length - 1)) cp.append(";");
//        }
//        // Compute command line
//        IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
//        IPath javacPath = jdkDir.append("bin").append("javac.exe");
//        StringBuffer cmdLine = new StringBuffer(javacPath.toString());
//        cmdLine.append(" -classpath ");
//        cmdLine.append(cp);
//        cmdLine.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable recommended warnings
//        if (this.dirPath.equals(dirFilePath)) {
//          cmdLine.append("*.java");
//        } else {
//          IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(
//              GenericTypeTest.this.dirPath.segmentCount());
//          String subDirName = subDirPath.toString().substring(subDirPath.
//              getDevice().length());
//          cmdLine.append(subDirName);
//        }
//				//System.out.println(testName+": "+cmdLine.toString());
//				//System.out.println(GenericTypeTest.this.dirPath.toFile().getAbsolutePath());
//        // Launch process
//        process = Runtime.getRuntime().exec(cmdLine.toString(), null,
//            GenericTypeTest.this.dirPath.toFile());
//        // Log errors
//        Logger errorLogger = new Logger(process.getErrorStream(), "ERROR");
//
//        // Log output
//        Logger outputLogger = new Logger(process.getInputStream(), "OUTPUT");
//
//        // start the threads to run outputs (standard/error)
//        errorLogger.start();
//        outputLogger.start();
//
//        // Wait for end of process
//        int exitValue = process.waitFor();
//
//        // Compare compilation results
//        if (expectedProblemLog == null) {
//          if (exitValue != 0) {
//            System.out.println("========================================");
//            System.out.println(testName
//                + ": javac has found error(s) although we're expecting conform result:\n");
//            System.out.println(errorLogger.buffer.toString());
//            printFiles(testFiles);
//          } else if (errorLogger.buffer.length() > 0) {
//            System.out.println("========================================");
//            System.out.println(testName
//                + ": javac displays warning(s) although we're expecting conform result:\n");
//            System.out.println(errorLogger.buffer.toString());
//            printFiles(testFiles);
//          }
//        } else if (exitValue == 0) {
//          if (errorLogger.buffer.length() == 0) {
//            System.out.println("========================================");
//            System.out.println(testName
//                + ": javac has found no error/warning although we're expecting negative result:");
//            System.out.println(expectedProblemLog);
//            printFiles(testFiles);
//          } else if (expectedProblemLog.indexOf("ERROR") > 0) {
//            System.out.println("========================================");
//            System.out.println(testName
//                + ": javac has found warning(s) although we're expecting error(s):");
//            System.out.println("javac:");
//            System.out.println(errorLogger.buffer.toString());
//            System.out.println("eclipse:");
//            System.out.println(expectedProblemLog);
//            printFiles(testFiles);
//          } else {
//            // TODO (frederic) compare warnings in each result and verify they are similar...
////						System.out.println(testName+": javac has found warnings :");
////						System.out.print(errorLogger.buffer.toString());
////						System.out.println(testName+": we're expecting warning results:");
////						System.out.println(expectedProblemLog);
//          }
//        } else if (errorLogger.buffer.length() == 0) {
//          System.out.println("========================================");
//          System.out.println(testName
//              + ": javac displays no output although we're expecting negative result:\n");
//          System.out.println(expectedProblemLog);
//          printFiles(testFiles);
//        }
//      } catch (IOException ioe) {
//        System.out.println(testName
//            + ": Not possible to launch Sun javac compilation!");
//      } catch (InterruptedException e1) {
//        if (process != null) process.destroy();
//        System.out.println(testName + ": Sun javac compilation was aborted!");
//      }
//
//      // Clean up written file(s)
//      IPath testDir = new Path(Util.getOutputDirectory()).append(shortTestName());
//      cleanupDirectory(testDir.toFile());
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  /*#########################################
   * Override basic runConform and run Negative methods to compile test files
   * with Sun compiler (if specified) and compare its results with ours.
   ##########################################*/
  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String)
   */
//  protected void runConformTest(String[] testFiles,
//      String expectedSuccessOutputString, String[] classLib,
//      boolean shouldFlushOutputDirectory, String[] vmArguments,
//      Map customOptions) {
//    try {
//      super.runConformTest(testFiles, expectedSuccessOutputString,
//          classLib, shouldFlushOutputDirectory, vmArguments,
//          customOptions);
//    } catch (AssertionFailedError e) {
//      throw e;
//    } finally {
//      if (runJavac)
//        runJavac(testFiles, null);
//    }
//  }

  /* (non-Javadoc)
   * Override to compile test files with Sun compiler if specified and compare its results with ours.
   * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
   */
//  protected void runNegativeTest(String[] testFiles,
//      String expectedProblemLog, String[] classLib,
//      boolean shouldFlushOutputDirectory, Map customOptions,
//      boolean generateOutput) {
//    try {
//      super.runNegativeTest(testFiles, expectedProblemLog, classLib,
//          shouldFlushOutputDirectory, customOptions, generateOutput);
//    } catch (AssertionFailedError e) {
//      throw e;
//    } finally {
//      if (runJavac)
//        runJavac(testFiles, expectedProblemLog);
//    }
//  }

  private Map getCompilerOptions() {
    return null;
  }

  public void test001() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<Tx1 extends String, Tx2 extends Comparable>  extends XS<Tx2> {\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "        Integer w = new X<String,Integer>().get(new Integer(12));\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "class XS <Txs> {\n" +
        "    Txs get(Txs t) {\n" +
        "        return t;\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test002() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<Xp1 extends String, Xp2 extends Comparable>  extends XS<Xp2> {\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "        Integer w = new X<String,Integer>().get(new Integer(12));\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    Xp2 get(Xp2 t){\n" +
        "        System.out.print(\"{X::get}\");\n" +
        "        return super.get(t);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class XS <XSp1> {\n" +
        "    XSp1 get(XSp1 t) {\n" +
        "        System.out.print(\"{XS::get}\");\n" +
        "        return t;\n" +
        "    }\n" +
        "}\n"
    },
        "{X::get}{XS::get}SUCCESS");
  }

  // check cannot bind superclass to type variable
  public void test003() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <X> extends X {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <X> extends X {\n" +
        "	                           ^\n" +
        "Cannot refer to the type parameter X as a supertype\n" +
        "----------\n");
  }

  // check cannot bind superinterface to type variable
  public void test004() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <X> implements X {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <X> implements X {\n" +
        "	                              ^\n" +
        "Cannot refer to the type parameter X as a supertype\n" +
        "----------\n");
  }

  // check cannot bind type variable in static context
  public void test005() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    \n" +
        "    T t;\n" +
        "    static {\n" +
        "        T s;\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	T s;\n" +
        "	^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n");
  }

  // check static references to type variables
  public void test006() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    \n" +
        "    T ok1;\n" +
        "    static {\n" +
        "        T wrong1;\n" +
        "    }\n" +
        "    static void foo(T wrong2) {\n" +
        "		T wrong3;\n" +
        "    }\n" +
        "    class MX extends T {\n" +
        "        T ok2;\n" +
        "    }\n" +
        "    static class SMX extends T {\n" +
        "        T wrong4;\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	T wrong1;\n" +
        "	^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	static void foo(T wrong2) {\n" +
        "	                ^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 8)\n" +
        "	T wrong3;\n" +
        "	^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 10)\n" +
        "	class MX extends T {\n" +
        "	                 ^\n" +
        "Cannot refer to the type parameter T as a supertype\n" +
        "----------\n" +
        "5. ERROR in X.java (at line 13)\n" +
        "	static class SMX extends T {\n" +
        "	                         ^\n" +
        "Cannot refer to the type parameter T as a supertype\n" +
        "----------\n" +
        "6. ERROR in X.java (at line 14)\n" +
        "	T wrong4;\n" +
        "	^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n");
  }

  // check static references to type variables
  public void test007() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    \n" +
        "    T ok1;\n" +
        "    static class SMX {\n" +
        "        T wrong4;\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	T wrong4;\n" +
        "	^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n");
  }

  // check static references to type variables
  public void test008() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    \n" +
        "     T ok;\n" +
        "    static T wrong;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	static T wrong;\n" +
        "	       ^\n" +
        "Cannot make a static reference to the type parameter T\n" +
        "----------\n");
  }

  // Object cannot be generic
  public void test009() {
    this.runNegativeTest(
        new String[] {
        "Object.java",
        "package java.lang;\n" +
        "public class Object <T> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in Object.java (at line 2)\n" +
        "	public class Object <T> {\n" +
        "	                     ^\n" +
        "The type java.lang.Object cannot be declared as a generic\n" +
        "----------\n");
  }

  public void test010() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "class Foo {} \n" +
        "public class X<T extends Object & Comparable<? super T>> {\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<Foo>();\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	new X<Foo>();\n" +
        "	      ^^^\n" +
        "Bound mismatch: The type Foo is not a valid substitute for the bounded parameter <T extends Object & Comparable<? super T>> of the type X<T>\n" +
        "----------\n");
  }

  public void test011() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T extends Object & Comparable<? super T>> {\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<Foo>();\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	new X<Foo>();\n" +
        "	      ^^^\n" +
        "Foo cannot be resolved to a type\n" +
        "----------\n");
  }

  public void test012() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    T foo(T t) {\n" +
        "        return t;\n" +
        "    }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        String s = new X<String>().foo(\"SUCCESS\");\n" +
        "        System.out.println(s);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test013() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    T foo(T t) {\n" +
        "        return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>().baz(\"SUCCESS\");\n" +
        "    }\n" +
        "    void baz(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                System.out.println(foo(t));\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test014() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends Exception> {\n" +
        "    T foo(T t) throws T {\n" +
        "        return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<EX>().baz(new EX());\n" +
        "    }\n" +
        "    void baz(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                System.out.println(foo(t));\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "}\n" +
        "class EX extends Exception {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 11)\n" +
        "	System.out.println(foo(t));\n" +
        "	                   ^^^^^^\n" +
        "Unhandled exception type T\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 16)\n" +
        "	class EX extends Exception {\n" +
        "	      ^^\n" +
        "The serializable class EX does not declare a static final serialVersionUID field of type long\n" +
        "----------\n");
  }

  public void test015() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends Exception> {\n" +
        "    String foo() throws T {\n" +
        "        return \"SUCCESS\";\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<EX>().baz(new EX());\n" +
        "    }\n" +
        "    void baz(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                try {\n" +
        "	                System.out.println(foo());\n" +
        "                } catch (Exception t) {\n" +
        "                }\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "}\n" +
        "class EX extends Exception {\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test016() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E extends Exception> {\n" +
        "    void foo(E e) throws E {\n" +
        "        throw e;\n" +
        "    }\n" +
        "    void bar(E e) {\n" +
        "        try {\n" +
        "            foo(e);\n" +
        "        } catch(Exception ex) {\n" +
        "	        System.out.println(\"SUCCESS\");\n" +
        "        }\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<Exception>().bar(new Exception());\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test017() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.io.IOException;\n" +
        "public class X <E extends Exception> {\n" +
        "    void foo(E e) throws E {\n" +
        "        throw e;\n" +
        "    }\n" +
        "    void bar(E e) {\n" +
        "        try {\n" +
        "            foo(e);\n" +
        "        } catch(Exception ex) {\n" +
        "	        System.out.println(\"SUCCESS\");\n" +
        "        }\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<IOException>().bar(new Exception());\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 14)\n" +
        "	new X<IOException>().bar(new Exception());\n" +
        "	                     ^^^\n" +
        "The method bar(IOException) in the type X<IOException> is not applicable for the arguments (Exception)\n" +
        "----------\n");
  }

  public void test018() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T foo(T t) {\n" +
        "        System.out.println(t);\n" +
        "        return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<XY>() {\n" +
        "            void run() {\n" +
        "                foo(new XY());\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n" +
        "class XY {\n" +
        "    public String toString() {\n" +
        "        return \"SUCCESS\";\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test019() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "     private T foo(T t) {\n" +
        "        System.out.println(t);\n" +
        "        return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<XY>() {\n" +
        "            void run() {\n" +
        "                foo(new XY());\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n" +
        "class XY {\n" +
        "    public String toString() {\n" +
        "        return \"SUCCESS\";\n" +
        "    }\n" +
        "}",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	foo(new XY());\n" +
        "	^^^\n" +
        "The method foo(T) in the type X<T> is not applicable for the arguments (XY)\n" +
        "----------\n");
  }

  public void test020() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "     void foo(Y<T> y) {\n" +
        "		System.out.print(\"SUCC\");\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>().bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new Y<T>() {\n" +
        "            public void pre() {\n" +
        "                foo(this);\n" +
        "            }\n" +
        "        }.print(\"ESS\");\n" +
        "    }\n" +
        "}\n" +
        "class Y <P> {\n" +
        "	public void print(P p) {\n" +
        "		pre();\n" +
        "		System.out.println(p);\n" +
        "	}\n" +
        "	public void pre() {\n" +
        "	}\n" +
        "}",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 13)\n" +
        "	}.print(\"ESS\");\n" +
        "	  ^^^^^\n" +
        "The method print(T) in the type Y<T> is not applicable for the arguments (String)\n" +
        "----------\n");
  }

  public void test021() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    void foo(T t) {\n" +
        "    }\n" +
        "    void bar(String x) {\n" +
        "        foo(x);\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>().foo(new Object());\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 1)\n" +
        "	public class X <T extends String> {\n" +
        "	                          ^^^^^^\n" +
        "The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 5)\n" +
        "	foo(x);\n" +
        "	^^^\n" +
        "The method foo(T) in the type X<T> is not applicable for the arguments (String)\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 8)\n" +
        "	new X<String>().foo(new Object());\n" +
        "	                ^^^\n" +
        "The method foo(String) in the type X<String> is not applicable for the arguments (Object)\n" +
        "----------\n");
  }

  public void test022() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    X(T t) {\n" +
        "        System.out.println(t);\n" +
        "    }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "       new X<String>(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test023() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    X(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                System.out.println(t);\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test024() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends Exception> {\n" +
        "    X(final T t) throws T {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                System.out.println(t);\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<EX>(new EX());\n" +
        "    }\n" +
        "}\n" +
        "class EX extends Exception {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 10)\n" +
        "	new X<EX>(new EX());\n" +
        "	^^^^^^^^^^^^^^^^^^^\n" +
        "Unhandled exception type EX\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 13)\n" +
        "	class EX extends Exception {\n" +
        "	      ^^\n" +
        "The serializable class EX does not declare a static final serialVersionUID field of type long\n" +
        "----------\n");
  }

  public void test025() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends Exception> {\n" +
        "    String foo() throws T {\n" +
        "        return \"SUCCESS\";\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<EX>(new EX());\n" +
        "    }\n" +
        "    X(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                try {\n" +
        "	                System.out.println(foo());\n" +
        "                } catch (Exception t) {\n" +
        "                }\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "}\n" +
        "class EX extends Exception {\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test026() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E extends Exception> {\n" +
        "    void foo(E e) throws E {\n" +
        "        throw e;\n" +
        "    }\n" +
        "    X(E e) {\n" +
        "        try {\n" +
        "            foo(e);\n" +
        "        } catch(Exception ex) {\n" +
        "	        System.out.println(\"SUCCESS\");\n" +
        "        }\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<Exception>(new Exception());\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test027() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.io.IOException;\n" +
        "public class X <E extends Exception> {\n" +
        "    void foo(E e) throws E {\n" +
        "        throw e;\n" +
        "    }\n" +
        "    X(E e) {\n" +
        "        try {\n" +
        "            foo(e);\n" +
        "        } catch(Exception ex) {\n" +
        "	        System.out.println(\"SUCCESS\");\n" +
        "        }\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<IOException>(new Exception());\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 14)\n" +
        "	new X<IOException>(new Exception());\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor X<IOException>(Exception) is undefined\n" +
        "----------\n");
  }

  public void test028() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        String s = new X<String>(\"SU\").t;\n" +
        "        System.out.print(s);\n" +
        "        s = new X<String>(\"failed\").t = \"CC\";\n" +
        "        System.out.print(s);\n" +
        "        s = new X<String>(\"\").t += \"ESS\";\n" +
        "        System.out.println(s);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test029() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X() {\n" +
        "    }\n" +
        "    T foo(T a, T b) {\n" +
        "        T s;\n" +
        "        s = t = a;\n" +
        "		s = t += b;\n" +
        "		return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(new X<String>().foo(\"SUC\", \"CESS\"));\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	s = t += b;\n" +
        "	    ^^^^^^\n" +
        "The operator += is undefined for the argument type(s) T, T\n" +
        "----------\n");
  }

  public void test030() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X() {\n" +
        "    }\n" +
        "    T foo(T a) {\n" +
        "        T s;\n" +
        "        s = t = a;\n" +
        "		return t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(new X<String>().foo(\"SUCCESS\"));\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test031() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<String>(\"INNER\") {\n" +
        "            void run() {\n" +
        "                \n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "		                String s = t = \"SUC\";\n" +
        "		                s = t+= \"CESS\";\n" +
        "				        System.out.println(t);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test032() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<String>(\"INNER\") {\n" +
        "            void run() {\n" +
        "                String s = t = \"SUC\";\n" +
        "                s = t+= \"CESS\";\n" +
        "		        System.out.println(t);\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test033() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E, T> {\n" +
        "	void foo(E e){}\n" +
        "	void foo(T t){}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(E e){}\n" +
        "	     ^^^^^^^^\n" +
        "Method foo(E) has the same erasure foo(Object) as another method in type X<E,T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	void foo(T t){}\n" +
        "	     ^^^^^^^^\n" +
        "Method foo(T) has the same erasure foo(Object) as another method in type X<E,T>\n" +
        "----------\n");
  }

  public void test034() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E extends Exception, T extends Exception> {\n" +
        "	void foo(E e){}\n" +
        "	void foo(T t){}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(E e){}\n" +
        "	     ^^^^^^^^\n" +
        "Method foo(E) has the same erasure foo(Exception) as another method in type X<E,T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	void foo(T t){}\n" +
        "	     ^^^^^^^^\n" +
        "Method foo(T) has the same erasure foo(Exception) as another method in type X<E,T>\n" +
        "----------\n");
  }

  public void test035() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E extends Exception, T extends Thread> {\n" +
        "	void foo(E e, Thread t){}\n" +
        "	void foo(Exception e, T t){}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(E e, Thread t){}\n" +
        "	     ^^^^^^^^^^^^^^^^^^\n" +
        "Method foo(E, Thread) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	void foo(Exception e, T t){}\n" +
        "	     ^^^^^^^^^^^^^^^^^^^^^\n" +
        "Method foo(Exception, T) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" +
        "----------\n");
  }

  public void test036() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E extends Exception, T extends Thread> {\n" +
        "	void foo(E e){}\n" +
        "	void foo(T t){}\n" +
        "    public static void main(String[] args) {\n" +
        "		 System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test037() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" +
        "	void foo(E e){}\n" +
        "	void foo(T t){}\n" +
        "    public static void main(String[] args) {\n" +
        "		 System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test038() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" +
        "	void foo(E e){}\n" +
        "	void foo(T t){}\n" +
        "	public static void main(String[] args) {\n" +
        "		X<XY,XY> x = new X<XY, XY>();\n" +
        "		x.foo(new XY());\n" +
        "	}\n" +
        "}\n" +
        "class XY extends Thread implements Cloneable {\n" +
        "}\n",
    }, "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	x.foo(new XY());\n" +
        "	  ^^^\n" +
        "The method foo(XY) is ambiguous for the type X<XY,XY>\n" +
        "----------\n");
  }

  public void test039() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E extends Cloneable, T extends Thread> {\n" +
        "	void foo(L<E> l1){}\n" +
        "	void foo(L<T> l2){}\n" +
        "	void foo(L l){}\n" +
        "}\n" +
        "\n" +
        "class L<E> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(L<E> l1){}\n" +
        "	     ^^^^^^^^^^^^\n" +
        "Method foo(L<E>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	void foo(L<T> l2){}\n" +
        "	     ^^^^^^^^^^^^\n" +
        "Method foo(L<T>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 4)\n" +
        "	void foo(L l){}\n" +
        "	     ^^^^^^^^\n" +
        "Duplicate method foo(L) in type X<E,T>\n" +
        "----------\n");
  }

  public void test040() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends X> {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test041() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T, U extends T> {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test042() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends U, U> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <T extends U, U> {\n" +
        "	                ^\n" +
        "Illegal forward reference to type parameter U\n" +
        "----------\n");
  }

  public void test043() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends L<T> , U extends T> {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "class L<E>{}\n",
    },
        "SUCCESS");
  }

  public void test044() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X extends L<X> {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n" +
        "class L<E> {}\n",
    },
        "SUCCESS");
  }

  public void test045() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public Z<T> var;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	public Z<T> var;\n" +
        "	       ^\n" +
        "Z cannot be resolved to a type\n" +
        "----------\n");
  }

  public void test046() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public Object<T> var;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	public Object<T> var;\n" +
        "	              ^\n" +
        "T cannot be resolved to a type\n" +
        "----------\n");
  }

  public void test047() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    private T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new MX<String>(\"INNER\") {\n" +
        "            void run() {\n" +
        "                \n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "		                String s = t = \"SUC\";\n" +
        "		                s = t+= \"CESS\";\n" +
        "				        System.out.println(t);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n" +
        "class MX<U> {\n" +
        "    MX(U u){}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 15)\n" +
        "	String s = t = \"SUC\";\n" +
        "	       ^\n" +
        "Type mismatch: cannot convert from T to String\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 15)\n" +
        "	String s = t = \"SUC\";\n" +
        "	               ^^^^^\n" +
        "Type mismatch: cannot convert from String to T\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 16)\n" +
        "	s = t+= \"CESS\";\n" +
        "	    ^^^^^^^^^^\n" +
        "The operator += is undefined for the argument type(s) T, String\n" +
        "----------\n");
  }

  // Access to enclosing 't' of type 'T' (not substituted from X<X> as private thus non inherited)
  // javac finds no error/warning on this test but it should
  public void test048() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    private T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<X>(this) {\n" +
        "            void run() {\n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "                        X x = t;\n" +
        "				        System.out.println(x);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 14)\n" +
        "	X x = t;\n" +
        "	  ^\n" +
        "Type mismatch: cannot convert from T to X\n" +
        "----------\n");
  }

  public void test049() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    public T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<X>(this) {\n" +
        "            void run() {\n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "                        X x = t;\n" +
        "				        System.out.println(\"SUCCESS:\"+x);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test050() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "class Super {class M {}}\n" +
        "public class X <T extends M> extends Super {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	public class X <T extends M> extends Super {\n" +
        "	                          ^\n" +
        "M cannot be resolved to a type\n" +
        "----------\n");
  }

  public void test051() {
    this.runConformTest(
        new String[] {
        "X.java",
        "class Super {class M {}}\n" +
        "public class X extends Super {\n" +
        "	class N <T extends M> {}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(\"SUCCESS:\");\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test052() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(\"SUCCESS:\");\n" +
        "	}\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test053() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<X>(this) {\n" +
        "            void run() {\n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "                        print(t);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test054() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<String>(\"OUTER\").bar();\n" +
        "    }\n" +
        "    void bar() {\n" +
        "        new X<X>(this) {\n" +
        "            void run() {\n" +
        "                new Object() {\n" +
        "                    void run() {\n" +
        "                        print(X.this.t);\n" +
        "                    }\n" +
        "                }.run();\n" +
        "            }\n" +
        "        }.run();\n" +
        "    }\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 14)\n" +
        "	print(X.this.t);\n" +
        "	^^^^^\n" +
        "The method print(X) in the type A<X> is not applicable for the arguments (T)\n" +
        "----------\n");
  }

  public void test055() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "	  X<String> xs = new X<String>(\"SUCCESS\");\n" +
        "	  System.out.println(xs.t);\n" +
        "    }\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "	 protected P p;\n" +
        "    protected A(P p) {\n" +
        "       this.p = p; \n" +
        "    } \n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test056() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "	  X<String> xs = new X<String>(\"SUCCESS\");\n" +
        "	  System.out.println((X)xs.t);\n" +
        "    }\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "	 protected P p;\n" +
        "    protected A(P p) {\n" +
        "       this.p = p; \n" +
        "    } \n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	System.out.println((X)xs.t);\n" +
        "	                   ^^^^^^^\n" +
        "Cannot cast from String to X\n" +
        "----------\n" +
        "----------\n" +
        "1. WARNING in p\\A.java (at line 7)\n" +
        "	protected void print(P p) {\n" +
        "	                       ^\n" +
        "The parameter p is hiding a field from type A<P>\n" +
        "----------\n");
  }

  public void test057() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "	  X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" +
        "	  System.out.println(xs.t.t);\n" +
        "    }\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "	 protected P p;\n" +
        "    protected A(P p) {\n" +
        "       this.p = p; \n" +
        "    } \n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // JSR14-v10[�2.1,�2.2]: Valid multiple parameter types
  public void test058() {
    this.runConformTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "// Valid Parameterized Type Declaration\n" +
        "public class X<A1, A2, A3> {\n" +
        "}\n" +
        "// Valid Type Syntax\n" +
        "class Y {\n" +
        "	X<String, Number, Integer> x;\n" +
        "}\n"
    }
        );
  }

  // JSR14-v10[�2.1,�2.2]: Invalid multiple parameter types: more declared than referenced
  public void test059() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "// Valid Parameterized Type Declaration\n" +
        "public class X<A1, A2, A3, A4> {\n" +
        "}\n" +
        "// Invalid Valid Type Syntax (not enough parameters)\n" +
        "class Y {\n" +
        "	X<String, Number, Integer> x;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 7)\n" +
        "	X<String, Number, Integer> x;\n" +
        "	^\n" +
        "Incorrect number of arguments for type X<A1,A2,A3,A4>; it cannot be parameterized with arguments <String, Number, Integer>\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Invalid multiple parameter types: more referenced than declared
  public void test060() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "// Valid Parameterized Type Declaration\n" +
        "public class X<A1, A2> {\n" +
        "}\n" +
        "// Invalid Valid Type Syntax (too many parameters)\n" +
        "class Y {\n" +
        "	X<String, Number, Integer> x;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 7)\n" +
        "	X<String, Number, Integer> x;\n" +
        "	^\n" +
        "Incorrect number of arguments for type X<A1,A2>; it cannot be parameterized with arguments <String, Number, Integer>\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Invalid multiple parameter types: primitive types
  public void test061() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "// Valid Parameterized Type Declaration\n" +
        "public class X<A1, A2, A3, A4, A5, A6, A7> {\n" +
        "}\n" +
        "// Invalid Valid Type Syntax (primitive cannot be parameters)\n" +
        "class Y {\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	  ^^^\n" +
        "Syntax error on token \"int\", Dimensions expected after this token\n" +
        "----------\n" +
        "2. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	       ^^^^^\n" +
        "Syntax error on token \"short\", Dimensions expected after this token\n" +
        "----------\n" +
        "3. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	              ^^^^\n" +
        "Syntax error on token \"long\", Dimensions expected after this token\n" +
        "----------\n" +
        "4. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	                    ^^^^^\n" +
        "Syntax error on token \"float\", Dimensions expected after this token\n" +
        "----------\n" +
        "5. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	                           ^^^^^^\n" +
        "Syntax error on token \"double\", Dimensions expected after this token\n" +
        "----------\n" +
        "6. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	                                   ^^^^^^^\n" +
        "Syntax error on token \"boolean\", Dimensions expected after this token\n" +
        "----------\n" +
        "7. ERROR in test\\X.java (at line 7)\n" +
        "	X<int, short, long, float, double, boolean, char> x;\n" +
        "	                                            ^^^^\n" +
        "Syntax error on token \"char\", Dimensions expected after this token\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Valid multiple parameter types: primitive type arrays
  public void test062() {
    this.runConformTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "// Valid Parameterized Type Declaration\n" +
        "public class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" +
        "}\n" +
        "// Valid Type Syntax\n" +
        "class Y {\n" +
        "	X<int[], short[][], long[][][], float[][][][], double[][][][][], boolean[][][][][][], char[][][][][][][], Object[][][][][][][][][]> x;\n" +
        "}\n"
    },
        ""
        );
  }

  public void test063() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> extends p.A<T> {\n" +
        "    \n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        X x = new X(args);\n" +
        "        X<String> xs = new X<String>(args);\n" +
        "	}\n" +
        "}\n",
        "p/A.java",
        "package p; \n" +
        "public class A<P> {\n" +
        "	 protected P p;\n" +
        "    protected A(P p) {\n" +
        "       this.p = p; \n" +
        "    } \n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 7)\n" +
        "	X x = new X(args);\n" +
        "	      ^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 8)\n" +
        "	X<String> xs = new X<String>(args);\n" +
        "	               ^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor X<String>(String[]) is undefined\n" +
        "----------\n" +
        "----------\n" +
        "1. WARNING in p\\A.java (at line 7)\n" +
        "	protected void print(P p) {\n" +
        "	                       ^\n" +
        "The parameter p is hiding a field from type A<P>\n" +
        "----------\n");
  }

  // raw type: variable map to its strict erasure
  public void test064() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T extends Exception & IX> {\n" +
        "    T t;\n" +
        "    void bar(T t) {\n" +
        "        t.getMessage();\n" +
        "        t.foo();\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X x = new X();\n" + // raw type
        "		x.t.getMessage();\n" + // T is strictly exception !
        "		x.t.foo();\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "interface IX {\n" +
        "    void foo();\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	void bar(T t) {\n" +
        "	           ^\n" +
        "The parameter t is hiding a field from type X<T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 10)\n" +
        "	x.t.foo();\n" +
        "	    ^^^\n" +
        "The method foo() is undefined for the type Exception\n" +
        "----------\n");
  }

  // raw type: assignments
  public void test065() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.io.IOException;\n" +
        "\n" +
        "public class X<T extends Exception> {\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "		X x = new X();\n" +
        "		X<IOException> xioe = new X<IOException>(); // ok\n" +
        "		\n" +
        "		X x2 = xioe;\n" +
        "		X<IOException> xioe2 = x; // unsafe\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 10)\n" +
        "	X<IOException> xioe2 = x; // unsafe\n" +
        "	                       ^\n" +
        "Unsafe type operation: Should not convert expression of raw type X to type X<IOException>. References to generic type X<T> should be parameterized\n" +
        "----------\n",
        null,
        true,
        customOptions
        );
  }

  // JSR14-v10[�2.1,�2.2]: Invalid PT declaration (mix with reference)
  public void test066() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Valid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1 extends X2<A2>> {\n" +
        "	A1 a1;\n" +
        "}\n" +
        "// Valid Parameterized Type Declaration\n" +
        "class X2<A2>{\n" +
        "	A2 a2;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A2>> {\n" +
        "	                              ^^\n" +
        "A2 cannot be resolved to a type\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Invalid PT declaration (mix with reference)
  public void test067() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Valid Consecutive Parameterized Type Declaration\n" +
        "public class X1< A1 extends X2	<	A2	>     			> {\n" +
        "	A1 a1;\n" +
        "}\n" +
        "// Valid Parameterized Type Declaration\n" +
        "class X2<A2>{\n" +
        "	A2 a2;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1< A1 extends X2	<	A2	>     			> {\n" +
        "	                              	 	^^\n" +
        "A2 cannot be resolved to a type\n" +
        "----------\n"
        );
  }

  // JSR14-V10[�2.4]: Not terminated consecutive declaration
  // TODO (david) diagnosis message on error 3 sounds strange, doesn't it?
  public void test068() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1 extends X2<A2> {\n" +
        "	A1 a1;\n" +
        "}\n" +
        "// Invalid Parameterized Type Declaration\n" +
        "class X2<A2 {\n" +
        "	A2 a2;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A2> {\n" +
        "	                              ^^\n" +
        "A2 cannot be resolved to a type\n" +
        "----------\n" +
        "2. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A2> {\n" +
        "	                                ^\n" +
        "Syntax error, insert \">\" to complete ReferenceType1\n" +
        "----------\n" +
        "3. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A2> {\n" +
        "	                                ^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "4. ERROR in test\\X1.java (at line 7)\n" +
        "	class X2<A2 {\n" +
        "	         ^^\n" +
        "Syntax error on token \"A2\", > expected after this token\n" +
        "----------\n"
        );
  }

  // JSR14-V10[�2.4]: Not terminated consecutive declaration
  public void test069() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1 extends X2<A2 {\n" +
        "	A1 a1;\n" +
        "}\n" +
        "// Invalid Parameterized Type Declaration\n" +
        "class X2<A2> {\n" +
        "	A2 a2;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A2 {\n" +
        "	                              ^^\n" +
        "Syntax error, insert \">>\" to complete ReferenceType2\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.4]: Unexpected consecutive PT declaration (right-shift symbol)
  // TODO (david) surround expected token with (double-)quotes
  public void test070() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1>> {\n" +
        "	A1 a1;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1>> {\n" +
        "	                  ^^\n" +
        "Syntax error on token \">>\", > expected\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Unexpected consecutive PT declaration (with spaces)
  public void test071() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1 < A1 > > {\n" +
        "	A1 a1;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1 < A1 > > {\n" +
        "	                       ^\n" +
        "Syntax error on token \">\", delete this token\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.4]: Unexpected consecutive PT declaration (unary right-shift symbol)
  // TODO (david) surround expected token with (double-)quotes
  public void test072() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1>>> {\n" +
        "	A1 a1;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1>>> {\n" +
        "	                  ^^^\n" +
        "Syntax error on token \">>>\", > expected\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.4]: Unexpected consecutive PT declaration (right-shift symbol)
  // TODO (david) surround expected token with (double-)quotes
  public void test073() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1<A1 extends X2<A1>>> {\n" +
        "	A1 a1;\n" +
        "}\n" +
        "// Valid Parameterized Type Declaration\n" +
        "class X2<A2> {\n" +
        "	A2 a2;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1<A1 extends X2<A1>>> {\n" +
        "	                                ^^^\n" +
        "Syntax error on token \">>>\", >> expected\n" +
        "----------\n"
        );
  }

  // JSR14-v10[�2.1,�2.2]: Unexpected consecutive PT declaration (with spaces)
  public void test074() {
    this.runNegativeTest(
        new String[] {
        "test/X1.java",
        "package test;\n" +
        "// Invalid Consecutive Parameterized Type Declaration\n" +
        "public class X1 < A1 > > > {\n" +
        "	A1 a1;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X1.java (at line 3)\n" +
        "	public class X1 < A1 > > > {\n" +
        "	                       ^^^\n" +
        "Syntax error on tokens, delete these tokens\n" +
        "----------\n"
        );
  }

  // A is not an interface
  public void test075() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "}",
        "p/A.java",
        "package p;\n" +
        "public class A<P> {\n" +
        "    protected P p;\n" +
        "    protected A(P p) {\n" +
        "        this.p = p;\n" +
        "    }\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" +
        "	                                   ^^^\n" +
        "The type A<? super T> is not an interface; it cannot be specified as a bounded parameter\n" +
        "----------\n"
        );
  }

  // A is not an interface
  public void test076() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends Object & p.A> extends p.A<T> {\n" +
        "    protected T t;\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "}",
        "p/A.java",
        "package p;\n" +
        "public class A<P> {\n" +
        "    protected P p;\n" +
        "    protected A(P p) {\n" +
        "        this.p = p;\n" +
        "    }\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <T extends Object & p.A> extends p.A<T> {\n" +
        "	                                   ^^^\n" +
        "The type A is not an interface; it cannot be specified as a bounded parameter\n" +
        "----------\n"
        );
  }

  // unsafe type operation: only for constructors with signature change
  public void test077() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> extends p.A<T> {\n" +
        "	 X() {\n" +
        "		super(null);\n" +
        "	}\n" +
        "    X(T t) {\n" +
        "        super(t);\n" +
        "    }\n" +
        "    X(X<T> xt) {\n" +
        "        super(xt.t);\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        X x = new X();\n" +
        "        X x1 = new X(args);\n" +
        "        X x2 = new X(x);\n" +
        "        X<String> xs = new X<String>(args);\n" +
        "	}\n" +
        "}\n",
        "p/A.java",
        "package p;\n" +
        "public class A<P> {\n" +
        "    protected P p;\n" +
        "    protected A(P p) {\n" +
        "        this.p = p;\n" +
        "    }\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	super(xt.t);\n" +
        "	      ^^^^\n" +
        "xt.t cannot be resolved or is not a field\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 13)\n" +
        "	X x1 = new X(args);\n" +
        "	       ^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 14)\n" +
        "	X x2 = new X(x);\n" +
        "	       ^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor X(X<T>) of raw type X. References to generic type X<T> should be parameterized\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 15)\n" +
        "	X<String> xs = new X<String>(args);\n" +
        "	               ^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor X<String>(String[]) is undefined\n" +
        "----------\n");
  }

  public void test078() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import p.A;\n" +
        "public class X {\n" +
        "    X(A<String> a, A<String> b) {\n" +
        "    }\n" +
        "    void foo(A<String> a) {\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        X x = new X((A)null, (A)null);\n" +
        "        A a = new A((A)null);\n" +
        "		x.foo(a);\n" +
        "		a.print(x);\n" +
        "		A<String> as = new A<String>(null);\n" +
        "		as.print(\"hello\");\n" +
        "	}\n" +
        "}\n",
        "p/A.java",
        "package p;\n" +
        "public class A<P> {\n" +
        "    protected P p;\n" +
        "    protected A(P p) {\n" +
        "        this.p = p;\n" +
        "    }\n" +
        "    protected void print(P p) {\n" +
        "        System.out.println(\"SUCCESS\"+p);\n" +
        "    }\n" +
        "    protected void print(A<P> a) {\n" +
        "        print(a.p);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 8)\n" +
        "	X x = new X((A)null, (A)null);\n" +
        "	            ^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 8)\n" +
        "	X x = new X((A)null, (A)null);\n" +
        "	                     ^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 9)\n" +
        "	A a = new A((A)null);\n" +
        "	      ^^^^^^^^^^^^^^\n" +
        "The constructor A(P) is not visible\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 10)\n" +
        "	x.foo(a);\n" +
        "	      ^\n" +
        "Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" +
        "----------\n" +
        "5. ERROR in X.java (at line 11)\n" +
        "	a.print(x);\n" +
        "	  ^^^^^\n" +
        "The method print(P) from the type A is not visible\n" +
        "----------\n" +
        "6. ERROR in X.java (at line 12)\n" +
        "	A<String> as = new A<String>(null);\n" +
        "	               ^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor A<String>(P) is not visible\n" +
        "----------\n" +
        "7. ERROR in X.java (at line 13)\n" +
        "	as.print(\"hello\");\n" +
        "	   ^^^^^\n" +
        "The method print(P) from the type A<String> is not visible\n" +
        "----------\n" +
        "----------\n" +
        "1. WARNING in p\\A.java (at line 7)\n" +
        "	protected void print(P p) {\n" +
        "	                       ^\n" +
        "The parameter p is hiding a field from type A<P>\n" +
        "----------\n");
  }

  // JSR14-v10[�2.4]: Valid consecutive Type Parameters Brackets
  public void test079() {
    this.runConformTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "public class X<A extends X1<X2<X3<String>>>> {\n" +
        "	A a;\n" +
        "	public static void main(String[] args) {\n" +
        "		X<X1<X2<X3<String>>>> x = new X<X1<X2<X3<String>>>>();\n" +
        "		x.a = new X1<X2<X3<String>>>();\n" +
        "		x.a.a1 = new X2<X3<String>>();\n" +
        "		x.a.a1.a2 = new X3<String>();\n" +
        "		x.a.a1.a2.a3 = \"SUCCESS\";\n" +
        "		System.out.println(x.a.a1.a2.a3);\n" +
        "	}\n" +
        "}\n" +
        "class X1<A extends X2<X3<String>>> {\n" +
        "	A a1;\n" +
        "}\n" +
        "class X2<A extends X3<String>> {\n" +
        "	A a2;\n" +
        "}\n" +
        "class X3<A> {\n" +
        "	A a3;\n" +
        "}\n"
    },
        "SUCCESS"
        );
  }

  // TODO (david) remove errors: insert dimension to complete array type
  public void test080() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "public class X<A extends X1<X2<X3<String>>> {}\n" +
        "class X1<A extends X2<X3<String>> {}\n" +
        "class X2<A extends X3<String> {}\n" +
        "class X3<A {}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String>>> {}\n" +
        "	                                        ^^^\n" +
        "Syntax error, insert \">\" to complete ReferenceType1\n" +
        "----------\n" +
        "2. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String>>> {}\n" +
        "	                                        ^^^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "3. ERROR in test\\X.java (at line 3)\n" +
        "	class X1<A extends X2<X3<String>> {}\n" +
        "	                               ^^\n" +
        "Syntax error, insert \">\" to complete ReferenceType1\n" +
        "----------\n" +
        "4. ERROR in test\\X.java (at line 3)\n" +
        "	class X1<A extends X2<X3<String>> {}\n" +
        "	                               ^^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "5. ERROR in test\\X.java (at line 4)\n" +
        "	class X2<A extends X3<String> {}\n" +
        "	                            ^\n" +
        "Syntax error, insert \">\" to complete ReferenceType1\n" +
        "----------\n" +
        "6. ERROR in test\\X.java (at line 4)\n" +
        "	class X2<A extends X3<String> {}\n" +
        "	                            ^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "7. ERROR in test\\X.java (at line 5)\n" +
        "	class X3<A {}\n" +
        "	         ^\n" +
        "Syntax error on token \"A\", > expected after this token\n" +
        "----------\n"
        );
  }

  // TODO (david) remove errors: insert dimension to complete array type
  public void test081() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "public class X<A extends X1<X2<X3<String>> {}\n" +
        "class X1<A extends X2<X3<String> {}\n" +
        "class X2<A extends X3<String {}\n" +
        "class X3<A> {}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String>> {}\n" +
        "	                                        ^^\n" +
        "Syntax error, insert \">>\" to complete ReferenceType2\n" +
        "----------\n" +
        "2. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String>> {}\n" +
        "	                                        ^^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "3. ERROR in test\\X.java (at line 3)\n" +
        "	class X1<A extends X2<X3<String> {}\n" +
        "	                               ^\n" +
        "Syntax error, insert \">>\" to complete ReferenceType2\n" +
        "----------\n" +
        "4. ERROR in test\\X.java (at line 3)\n" +
        "	class X1<A extends X2<X3<String> {}\n" +
        "	                               ^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "5. ERROR in test\\X.java (at line 4)\n" +
        "	class X2<A extends X3<String {}\n" +
        "	                      ^^^^^^\n" +
        "Syntax error, insert \">>\" to complete ReferenceType2\n" +
        "----------\n"
        );
  }

  // TODO (david) remove error: insert dimension to complete array type
  public void test082() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "public class X<A extends X1<X2<X3<String> {}\n" +
        "class X1<A extends X2<X3<String {}\n" +
        "class X2<A extends X3<String>> {}\n" +
        "class X3<A> {}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String> {}\n" +
        "	                                        ^\n" +
        "Syntax error, insert \">>>\" to complete ReferenceType3\n" +
        "----------\n" +
        "2. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String> {}\n" +
        "	                                        ^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "3. ERROR in test\\X.java (at line 3)\n" +
        "	class X1<A extends X2<X3<String {}\n" +
        "	                         ^^^^^^\n" +
        "Syntax error, insert \">>>\" to complete ReferenceType3\n" +
        "----------\n"
        );
  }

  // TODO (david) remove error: insert dimension to complete array type
  public void test083() {
    this.runNegativeTest(
        new String[] {
        "test/X.java",
        "package test;\n" +
        "public class X<A extends X1<X2<X3<String {}\n" +
        "class X1<A extends X2<X3<String>>> {}\n" +
        "class X2<A extends X3<String>> {}\n" +
        "class X3<A> {}\n"
    },
        "----------\n" +
        "1. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String {}\n" +
        "	                                  ^^^^^^\n" +
        "Syntax error, insert \">\" to complete ReferenceType1\n" +
        "----------\n" +
        "2. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String {}\n" +
        "	                                  ^^^^^^\n" +
        "Syntax error, insert \"Dimensions\" to complete ArrayType\n" +
        "----------\n" +
        "3. ERROR in test\\X.java (at line 2)\n" +
        "	public class X<A extends X1<X2<X3<String {}\n" +
        "	                                  ^^^^^^\n" +
        "Syntax error, insert \">>>\" to complete ReferenceType3\n" +
        "----------\n"
        );
  }

  public void test084() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    X(AX<String> a, AX<String> b) {\n" +
        "    }\n" +
        "    void foo(AX<String> a) {\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        X x = new X((AX)null, (AX)null);\n" +
        "        AX a = new AX((AX)null);\n" +
        "        AX a2 = new AX(null);\n" +
        "		x.foo(a);\n" +
        "		a.foo(a);\n" +
        "		a.bar(a);\n" +
        "		AX<String> as = new AX<String>(null);\n" +
        "		as.print(a);\n" +
        "		as.bar(a);\n" +
        "	}\n" +
        "}\n" +
        "class AX <P> {\n" +
        "    AX(AX<P> ax){}\n" +
        "    AX(P p){}\n" +
        "    void print(P p){}\n" +
        "    void foo(AX rawAx){}\n" +
        "    void bar(AX<P> ax){}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 7)\n" +
        "	X x = new X((AX)null, (AX)null);\n" +
        "	            ^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 7)\n" +
        "	X x = new X((AX)null, (AX)null);\n" +
        "	                      ^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 8)\n" +
        "	AX a = new AX((AX)null);\n" +
        "	       ^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 9)\n" +
        "	AX a2 = new AX(null);\n" +
        "	        ^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "5. WARNING in X.java (at line 10)\n" +
        "	x.foo(a);\n" +
        "	      ^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "6. WARNING in X.java (at line 12)\n" +
        "	a.bar(a);\n" +
        "	^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the method bar(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "7. ERROR in X.java (at line 13)\n" +
        "	AX<String> as = new AX<String>(null);\n" +
        "	                ^^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor AX<String>(AX<String>) is ambiguous\n" +
        "----------\n" +
        "8. ERROR in X.java (at line 14)\n" +
        "	as.print(a);\n" +
        "	   ^^^^^\n" +
        "The method print(String) in the type AX<String> is not applicable for the arguments (AX)\n" +
        "----------\n" +
        "9. WARNING in X.java (at line 15)\n" +
        "	as.bar(a);\n" +
        "	       ^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" +
        "----------\n");
  }

  public void test085() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX ax = new AX();\n" +
        "        X x = (X)ax.p;\n" +
        "        System.out.println(x);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class AX <P> {\n" +
        "    \n" +
        "    P p;\n" +
        "}\n",
    },
        "null");
  }

  public void test086() {
//    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX ax = new AX();\n" +
        "        AX ax2 = ax.p;\n" +
        "        ax.p = new AX<String>();\n" +
        "        System.out.println(ax2);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class AX <P> {\n" +
        "    AX<P> p;\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 6)\n" +
        "	ax.p = new AX<String>();\n" +
        "	^^^^\n" +
        "Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n",
        null,
        true
//        ,
//        customOptions
        );
  }

  public void test087() {
    Map customOptions = getCompilerOptions();
    // check no unsafe type operation problem is issued
//    customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation,
//        CompilerOptions.ERROR);
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX ax = new AX();\n" +
        "        AX ax2 = ax.p;\n" +
        "        AX ax3 = new AX<String>();\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class AX <P> {\n" +
        "    AX<P> p;\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        true,
        null,
        customOptions);
  }

  public void test088() {
    Map customOptions = getCompilerOptions();
    // check no unsafe type operation problem is issued
//    customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation,
//        CompilerOptions.ERROR);
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "     AX ax = new AX();\n" +
        "     AX ax2 = ax.p;\n" +
        "     AX ax3 = new AX<String>();\n" +
        "}\n" +
        "\n" +
        "class AX <P> {\n" +
        "    AX<P> p;\n" +
        "}\n",
    },
        "",
        null,
        true,
        null,
        customOptions);
  }

  public void test089() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    T q;\n" +
        "     public static void main(String[] args) {\n" +
        "         X<String[]> xss = new X<String[]>();\n" +
        "         X<X<String[]>> xxs = new X<X<String[]>>();\n" +
        "         xxs.q = xss;\n" +
        "         System.out.println(\"SUCCESS\");\n" +
        "     }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test090() {
    Map customOptions = getCompilerOptions();
    // check no unsafe type operation problem is issued
//    customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation,
//        CompilerOptions.ERROR);
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    T q;\n" +
        "    \n" +
        "     public static void main(String[] args) {\n" +
        "         X<String[]> xss = new X<String[]>();\n" +
        "         X<X<String[]>> xxs = new X<X<String[]>>();\n" +
        "         xxs.q = xss;\n" +
        "         System.out.println(\"SUCCESS\");\n" +
        "     }\n" +
        "      void foo(X[] xs) {\n" +
        "          xs[0] = new X<String>();\n" +
        "     }\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        true,
        null,
        customOptions);
  }

  public void test091() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "      void foo(X<String>[] xs) {\n" +
        "     }\n" +
        "}\n",
    },
        "");
  }

  public void test092() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "     void foo() {\n" +
        "         X<String> xs = new X<String>(\"\");\n" +
        "         X<String> xs2 = (X<String>) xs;\n" +
        "         \n" +
        "         ((X)xs).t = this;\n" +
        "         \n" +
        "         System.out.prinln((T) this.t);\n" +
        "     }\n" +
        "     public static void main(String[] args) {\n" +
        "		new X<String>(\"SUCCESS\").foo();\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 8)\n" +
        "	X<String> xs2 = (X<String>) xs;\n" +
        "	                ^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type X<String> for expression of type X<String>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 10)\n" +
        "	((X)xs).t = this;\n" +
        "	        ^\n" +
        "Unsafe type operation: Should not assign expression of type X<T> to the field t of raw type X. References to generic type X<T> should be parameterized\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 12)\n" +
        "	System.out.prinln((T) this.t);\n" +
        "	           ^^^^^^\n" +
        "The method prinln(T) is undefined for the type PrintStream\n" +
        "----------\n");
  }

  public void test093() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    public static void main(String[] args) {\n" +
        "        AX ax = new AX();\n" +
        "        AX ax2 = new AX();\n" +
        "        ax.p = ax2.p;\n" + // javac reports unchecked warning, which seems a bug as no difference in between lhs and rhs types
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "class AX <P> {\n" +
        "    AX<P> p;\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // same as test001, but every type is now a SourceTypeBinding
  public void test094() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "        I w = new X<S,I>().get(new I());\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "class S {}\n" +
        "class I implements C<I> {}\n" +
        "interface C<Tc> {}\n" +
        "class XS <Txs> {\n" +
        "    Txs get(Txs t) {\n" +
        "        return t;\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test095() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "        I w = new X<S,I>().get(new I());\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "class S {}\n" +
        "class I implements C {}\n" +
        "interface C<Tc> {}\n" +
        "class XS <Txs> {\n" +
        "    Txs get(Txs t) {\n" +
        "        return t;\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test096() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> extends X {}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X<T> extends X {}\n" +
        "	                          ^\n" +
        "Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" +
        "----------\n");
  }

  public void test097() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> extends X<String> {}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X<T> extends X<String> {}\n" +
        "	                          ^\n" +
        "Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" +
        "----------\n");
  }

  public void test098() {
    Map customOptions = getCompilerOptions();
//    customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation,
//        CompilerOptions.ERROR);
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX ax = new AX();\n" +
        "        AX ax2 = ax.p;\n" +
        "        ax.p = new AX<String>();\n" +
        "        ax.q = new AX<String>();\n" +
        "        ax.r = new AX<Object>();\n" +
        "        ax.s = new AX<String>();\n" +
        "        System.out.println(ax2);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class AX <P> {\n" +
        "    AX<P> p;\n" +
        "    AX<Object> q;\n" +
        "    AX<String> r;\n" +
        "    BX<String> s;\n" +
        "}\n" +
        "\n" +
        "class BX<Q> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	ax.p = new AX<String>();\n" +
        "	^^^^\n" +
        "Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	ax.q = new AX<String>();\n" +
        "	^^^^\n" +
        "Unsafe type operation: Should not assign expression of type AX<String> to the field q of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 8)\n" +
        "	ax.r = new AX<Object>();\n" +
        "	^^^^\n" +
        "Unsafe type operation: Should not assign expression of type AX<Object> to the field r of raw type AX. References to generic type AX<P> should be parameterized\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 9)\n" +
        "	ax.s = new AX<String>();\n" +
        "	       ^^^^^^^^^^^^^^^^\n" +
        "Type mismatch: cannot convert from AX<String> to BX\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // wildcard bound cannot be base type
  // TODO (david) only syntax error should be related to wilcard bound being a base type. Ripple effect is severe here.
  public void test099() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X  <T extends AX<? super int>> {\n" +
        "    public static void main(String[] args) {\n" +
        "		AX<String> ax;\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "	void foo(X<?> x) {\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X  <T extends AX<? super int>> {\n" +
        "	                                      ^^^\n" +
        "Syntax error on token \"int\", Dimensions expected after this token\n" +
        "----------\n");
  }

  // type parameterized with wildcard cannot appear in allocation
  public void test100() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "    P foo() { return null; }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" +
        "	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Bound mismatch: The constructor X(? extends AX) of type X<? extends AX> is not applicable for the arguments (AX<String>). The wildcard parameter ? extends AX has no lower bound, and may actually be more restrictive than argument AX<String>\n" +
        "----------\n");
  }

  // wilcard may not pass parameter bound check
  public void test101() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends String> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" +
        "		x.t.foo(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.println(p);\n" +
        "   }\n" +
        "}\n" +
        "\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 1)\n" +
        "	public class X <T extends String> {\n" +
        "	                          ^^^^^^\n" +
        "The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" +
        "	  ^^^^^^^^^^^^\n" +
        "Bound mismatch: The type ? extends AX is not a valid substitute for the bounded parameter <T extends String> of the type X<T>\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 7)\n" +
        "	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" +
        "	                          ^^\n" +
        "Bound mismatch: The type AX<String> is not a valid substitute for the bounded parameter <T extends String> of the type X<T>\n" +
        "----------\n");
  }

  // unbound wildcard implicitly bound by matching parameter bounds
  public void test102() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<?> x = new X<BX<String>>(new BX<String>());\n" +
        "		x.t.foo(\"SUCC\");\n" +
        "		x.t.bar(\"ESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.print(p);\n" +
        "   }\n" +
        "}\n" +
        "\n" +
        "class BX<Q> extends AX<Q> {\n" +
        "   void bar(Q q) { \n" +
        "		System.out.println(q);\n" +
        "   }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	x.t.bar(\"ESS\");\n" +
        "	    ^^^\n" +
        "The method bar(String) is undefined for the type ?\n" +
        "----------\n");
  }

  public void test103() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<? extends BX> x = new X<BX<String>>(new BX<String>());\n" +
        "		x.t.foo(\"SUCC\");\n" +
        "		x.t.bar(\"ESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.print(p);\n" +
        "   }\n" +
        "}\n" +
        "\n" +
        "class BX<Q> extends AX<Q> {\n" +
        "   void bar(Q q) { \n" +
        "		System.out.println(q);\n" +
        "   }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // wildcard bound check
  public void test104() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" +
        "		x.t.foo(\"SUCC\");\n" +
        "		x.t.bar(\"ESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.print(p);\n" +
        "   }\n" +
        "}\n" +
        "\n" +
        "class BX<Q> extends AX<Q> {\n" +
        "   void bar(Q q) { \n" +
        "		System.out.println(q);\n" +
        "   }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" +
        "	                ^\n" +
        "Type mismatch: cannot convert from X<AX<String>> to X<? extends BX>\n" +
        "----------\n");
  }

  public void test105() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" +
        "		x.t.foo(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.println(p);\n" +
        "   }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test106() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        X<BX<String>> x = new X<BX<String>>(new BX<String>());\n" +
        "		x.t.foo(\"SUCC\");\n" +
        "		x.t.bar(\"ESS\");\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.print(p);\n" +
        "   }\n" +
        "}\n" +
        "\n" +
        "class BX<Q> extends AX<Q> {\n" +
        "   void bar(Q q) { \n" +
        "		System.out.println(q);\n" +
        "   }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // unsafe assignment thru binaries
  public void test107() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "\n" +
        "public class X  {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        \n" +
        "        Iterable<String> is = new ArrayList();\n" +
        "		is.iterator();\n" +
        "    }\n" +
        "}\n" +
        "\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 7)\n" +
        "	Iterable<String> is = new ArrayList();\n" +
        "	                      ^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type ArrayList to type Iterable<String>. References to generic type Iterable<T> should be parameterized\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // class literal: Integer.class of type Class<Integer>
  public void test108() {
    // also ensure no unsafe type operation problem is issued (assignment to variable of type raw)
    Map customOptions = getCompilerOptions();
//    customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation,
//        CompilerOptions.ERROR);
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    Class k;\n" +
        "    public static void main(String args[]) {\n" +
        "        new X().foo();\n" +
        "    }\n" +
        "    void foo() {\n" +
        "        Class c = this.getClass();\n" +
        "        this.k = this.getClass();\n" +
        "        this.k = Integer.class;\n" +
        "        try {\n" +
        "            Integer i = Integer.class.newInstance();\n" +
        "        } catch (Exception e) {\n" +
        "        }\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        true,
        null,
        customOptions);
  }

  // parameterized interface cannot be implemented simultaneously with distinct arguments
  public void test109() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X implements AX<String> {}\n" +
        "class Y extends X implements AX<Thread> {}\n" +
        "interface AX<P> {}\n" +
        "\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	class Y extends X implements AX<Thread> {}\n" +
        "	      ^\n" +
        "The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX<String>\n" +
        "----------\n");
  }

  public void test110() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X implements AX {}\n" +
        "class Y extends X implements AX<Thread> {}\n" +
        "interface AX<P> {}\n" +
        "\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	class Y extends X implements AX<Thread> {}\n" +
        "	      ^\n" +
        "The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX\n" +
        "----------\n");
  }

  public void test111() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X implements AX<Object> {}\n" +
        "class Y extends X implements AX {}\n" +
        "interface AX<P> {}\n" +
        "\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	class Y extends X implements AX {}\n" +
        "	      ^\n" +
        "The interface AX cannot be implemented simultaneously with different arguments: AX and AX<Object>\n" +
        "----------\n");
  }

  // test member types
  public void test112() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "    void foo(X<Thread>.MX.MMX<X> mx) {}\n" +
        "    class MX <MT> {\n" +
        "        class MMX <MMT> {}\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(X<Thread>.MX.MMX<X> mx) {}\n" +
        "	           ^^^^^^\n" +
        "Bound mismatch: The type Thread is not a valid substitute for the bounded parameter <T extends X.MX<Runnable>.MMX<Iterable<String>>> of the type X<T>\n" +
        "----------\n");
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "    class MX <MT extends Comparable> {\n" +
        "        class MMX <MMT> {}\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "	                               ^^^^^^^^\n" +
        "Bound mismatch: The type Runnable is not a valid substitute for the bounded parameter <MT extends Comparable> of the type X<T>.MX<MT>\n" +
        "----------\n");
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "    class MX <MT> {\n" +
        "        class MMX <MMT extends Comparable> {}\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "	                                             ^^^^^^^^\n" +
        "Bound mismatch: The type Iterable<String> is not a valid substitute for the bounded parameter <MMT extends Comparable> of the type X<T>.MX<MT>.MMX<MMT>\n" +
        "----------\n");
  }

  public void test113() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "  class MX<U> {\n" +
        "  }\n" +
        "\n" +
        "  public static void main(String[] args) {\n" +
        "    new X<Thread>().foo();\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "  }\n" +
        "  void foo() {\n" +
        "		new X<String>().new MX<T>();\n" +
        "  }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test114() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "  class MX<U> {\n" +
        "  }\n" +
        "\n" +
        "  public static void main(String[] args) {\n" +
        "    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
        "  }\n" +
        "  void foo(X.MX<Thread> mx) {\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "  }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test115() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "  class MX<U> {\n" +
        "  }\n" +
        "\n" +
        "  public static void main(String[] args) {\n" +
        "    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
        "  }\n" +
        "  void foo(X<String>.MX mx) {\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "  }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test116() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "  class MX<U> {\n" +
        "  }\n" +
        "\n" +
        "  public static void main(String[] args) {\n" +
        "    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
        "  }\n" +
        "  void foo(X.MX mx) {\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "  }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // test member types
  public void test117() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "    public static void main(String [] args) {\n" +
        "        \n" +
        "        new X<X.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    void foo(X<X.MX.MMX>.MX.MMX<X> mx) {\n" +
        "    }\n" +
        "    \n" +
        "    class MX <MT> {\n" +
        "        class MMX <MMT> {\n" +
        "        }\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // test generic method with recursive parameter bound <T extends Comparable<? super T>>
  public void test118() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        java.util.Collections.sort(new java.util.LinkedList<String>());\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // test binary member types
  public void test119() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" +
        "    public static void main(String [] args) {\n" +
        "        \n" +
        "        new X<X.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    void foo(X<X.MX.MMX>.MX.MMX<X> mx) {\n" +
        "    }\n" +
        "    void foo2(X.MX.MMX<X> mx) {\n" +
        "    }\n" +
        "    void foo3(X<X.MX<Runnable>.MMX<Iterable<String>>> mx) {\n" +
        "    }\n" +
        "    \n" +
        "    class MX <MT> {\n" +
        "        class MMX <MMT> {\n" +
        "        }\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");

    this.runConformTest(
        new String[] {
        "Y.java",
        "public class Y extends X {\n" +
        "    public static void main(String [] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        false, // do not flush output
        null);
  }

  // test generic method
  public void test120() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T>{\n" +
        "    public static void main(String[] args) {\n" +
        "        \n" +
        "        String s = new X<String>().foo(\"SUCCESS\");\n" +
        "	}\n" +
        "    <U extends String> T foo (U u) {\n" +
        "        System.out.println(u);\n" +
        "        return null;\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // substitute array types
  public void test121() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    public static void main(String[] args) {\n" +
        "		new X<String>().foo(args);\n" +
        "	}\n" +
        "    \n" +
        "    void foo(T[] ts) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // generic method with most specific common supertype: U --> String
  public void test122() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    public static void main(String[] args) {\n" +
        "		new X<String>().foo(args, new X<X<String>>());\n" +
        "	}\n" +
        "    <U> void foo(U[] us, X<X<U>> xxu) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // invalid parameterized type
  public void test123() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "    T<String> ts;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	T<String> ts;\n" +
        "	^\n" +
        "The type T is not generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // generic method with indirect type inference: BX<String, Thread> --> AX<W>
  public void test124() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    <W> void foo(AX<W> aw) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "     }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "		new X().foo(new BX<String,Thread>());\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<T> {\n" +
        "}\n" +
        "class BX<U, V> extends AX<V> {\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // generic method with indirect type inference: CX  --> AX<W>
  public void test125() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    <W> void foo(AX<W> aw) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "     }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "		new X().foo(new CX());\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<T> {\n" +
        "}\n" +
        "class BX<U, V> extends AX<V> {\n" +
        "}\n" +
        "class CX extends BX<String, Thread> {\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // variation on test125 with typo: CX extends B instead of BX.
  public void test126() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    <W> void foo(AX<W> aw) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "     }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "		new X().foo(new CX());\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class AX<T> {\n" +
        "}\n" +
        "class BX<U, V> extends AX<V> {\n" +
        "}\n" +
        "class CX extends B<String, Thread> {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X().foo(new CX());\n" +
        "	        ^^^\n" +
        "The method foo(AX<W>) in the type X is not applicable for the arguments (CX)\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 16)\n" +
        "	class CX extends B<String, Thread> {\n" +
        "	                 ^\n" +
        "B cannot be resolved to a type\n" +
        "----------\n");
  }

  // 57784: test generic method
  public void test127() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        java.util.Arrays.asList(new Object[] {\"1\"});\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // 58666: special treatment for Object#getClass declared of type: Class<? extends Object>
  // but implicitly converted to Class<? extends X> for free.
  public void test128() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "    public static void main(String[] args) {\n" +
        "		X x = new X();\n" +
        "		Class c1 = x.getClass();\n" +
        "		Class<? extends X> c2 = x.getClass();\n" +
        "		String s = \"hello\";\n" +
        "		Class<? extends X> c3 = s.getClass();\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	Class<? extends X> c3 = s.getClass();\n" +
        "	                   ^^\n" +
        "Type mismatch: cannot convert from Class<? extends String> to Class<? extends X>\n" +
        "----------\n");
  }

  // variation on test128
  public void test129() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "		XY xy = new XY();\n" +
        "		Class c1 = xy.getClass();\n" +
        "		Class<? extends XY> c2 = xy.getClass();\n" +
        "		String s = \"hello\";\n" +
        "		Class<? extends XY> c3 = s.getClass();\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class XY extends X {\n" +
        "    public Class <? extends Object> getClass() {\n" +
        "        return super.getClass();\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	Class<? extends XY> c3 = s.getClass();\n" +
        "	                    ^^\n" +
        "Type mismatch: cannot convert from Class<? extends String> to Class<? extends XY>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 14)\n" +
        "	public Class <? extends Object> getClass() {\n" +
        "	                                ^^^^^^^^^^\n" +
        "Cannot override the final method from Object\n" +
        "----------\n");
  }

  // getClass on array type
  public void test130() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "\n" +
        "    public static void main(String[] args) {\n" +
        "		X[] x = new X[0];\n" +
        "		Class<? extends X[]> c = x.getClass();\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // 58979
  public void test131() {
    this.runNegativeTest(
        new String[] {
        "ArrayList.java",
        " interface List<T> {\n" +
        "	 List<T> foo();\n" +
        "}\n" +
        "\n" +
        " class ArrayList<T> implements List<T> {\n" +
        "	public List<T> foo() {\n" +
        "		List<T> lt = this;\n" +
        "		lt.bar();\n" +
        "		return this;\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in ArrayList.java (at line 8)\n" +
        "	lt.bar();\n" +
        "	   ^^^\n" +
        "The method bar() is undefined for the type List<T>\n" +
        "----------\n");
  }

  public void test132() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "  <T extends X<W>.Z> foo() {}\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	<T extends X<W>.Z> foo() {}\n" +
        "	             ^\n" +
        "W cannot be resolved to a type\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 2)\n" +
        "	<T extends X<W>.Z> foo() {}\n" +
        "	                   ^^^^^\n" +
        "Return type for the method is missing\n" +
        "----------\n");
  }

  // bridge method
  public void test133() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    public static void main(String[] args) {\n" +
        "        X x = new Y();\n" +
        "        System.out.println(x.foo());\n" +
        "    }\n" +
        "   T foo() {return null;}\n" +
        "   void foo(T t) {}\n" +
        "}\n" +
        "class Y extends X<Object> {\n" +
        "    String foo() {return \"SUCCESS\";}\n" +
        "    void foo(String s) {}\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test134() {
    this.runConformTest(
        new String[] {
        "Z.java",
        "import java.util.ArrayList;\n" +
        "import java.util.List;\n" +
        "\n" +
        "public class Z <T extends List> { \n" +
        "    T t;\n" +
        "    public static void main(String[] args) {\n" +
        "        foo(new Z<ArrayList>().set(new ArrayList<String>()));\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    Z<T> set(T t) {\n" +
        "        this.t = t;\n" +
        "        return this;\n" +
        "    }\n" +
        "    T get() { \n" +
        "        return this.t; \n" +
        "    }\n" +
        "    \n" +
        "    static void foo(Z<? super ArrayList> za) {\n" +
        "        za.get().isEmpty();\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test135() {
    this.runNegativeTest(
        new String[] {
        "Z.java",
        "public class Z <T extends ZA> { \n" +
        "    public static void main(String[] args) {\n" +
        "        foo(new Z<ZA>());\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    static void foo(Z<? super String> zs) {\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class ZA {\n" +
        "    void foo() {}\n" +
        "}\n" +
        "\n" +
        "class ZB extends ZA {\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in Z.java (at line 3)\n" +
        "	foo(new Z<ZA>());\n" +
        "	^^^\n" +
        "The method foo(Z<? super String>) in the type Z<T> is not applicable for the arguments (Z<ZA>)\n" +
        "----------\n" +
        "2. ERROR in Z.java (at line 6)\n" +
        "	static void foo(Z<? super String> zs) {\n" +
        "	                  ^^^^^^^^^^^^^^\n" +
        "Bound mismatch: The type ? super String is not a valid substitute for the bounded parameter <T extends ZA> of the type Z<T>\n" +
        "----------\n");
  }

  public void test136() {
    this.runNegativeTest(
        new String[] {
        "Z.java",
        "public class Z <T extends ZB> { \n" +
        "    public static void main(String[] args) {\n" +
        "        foo(new Z<ZB>());\n" +
        "    }\n" +
        "    static void foo(Z<? super ZA> zs) {\n" +
        "        zs.foo();\n" +
        "    }\n" +
        "}\n" +
        "class ZA {\n" +
        "}\n" +
        "class ZB extends ZA {\n" +
        "    void foo() {}\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in Z.java (at line 3)\n" +
        "	foo(new Z<ZB>());\n" +
        "	^^^\n" +
        "The method foo(Z<? super ZA>) in the type Z<T> is not applicable for the arguments (Z<ZB>)\n" +
        "----------\n" +
        "2. ERROR in Z.java (at line 5)\n" +
        "	static void foo(Z<? super ZA> zs) {\n" +
        "	                  ^^^^^^^^^^\n" +
        "Bound mismatch: The type ? super ZA is not a valid substitute for the bounded parameter <T extends ZB> of the type Z<T>\n" +
        "----------\n" +
        "3. ERROR in Z.java (at line 6)\n" +
        "	zs.foo();\n" +
        "	   ^^^\n" +
        "The method foo(Z<? super ZA>) in the type Z<? super ZA> is not applicable for the arguments ()\n" +
        "----------\n");
  }

  public void test137() {
    this.runConformTest(
        new String[] {
        "Z.java",
        "import java.util.ArrayList;\n" +
        "import java.util.List;\n" +
        "\n" +
        "public class Z <T extends List> { \n" +
        "    T t;\n" +
        "    public static void main(String[] args) {\n" +
        "        foo(new Z<ArrayList>().set(new ArrayList<String>()));\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    Z<T> set(T t) {\n" +
        "        this.t = t;\n" +
        "        return this;\n" +
        "    }\n" +
        "    T get() { \n" +
        "        return this.t; \n" +
        "    }\n" +
        "    \n" +
        "    static void foo(Z<? extends ArrayList> za) {\n" +
        "        za.get().isEmpty();\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // unbound wildcard still remembers its variable bound: Z<?> behaves like Z<AX>
  public void test138() {
    this.runConformTest(
        new String[] {
        "Z.java",
        "public class Z <T extends AX> {\n" +
        "    T t;\n" +
        "    Z(T t){\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "		 Z<AX<String>> zax = new Z<AX<String>>(new AX<String>());\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    void baz(Z<?> zu){\n" +
        "        zu.t.foo(null);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class AX<P> {\n" +
        "   void foo(P p) { \n" +
        "		System.out.print(p);\n" +
        "   }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // extending wildcard considers its bound prior to its corresponding variable
  public void test139() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    T get() {\n" +
        "        return this.t;\n" +
        "    }\n" +
        "    void bar(X<? extends BX> x) {\n" +
        "        x.get().afoo();\n" +
        "        x.get().bfoo();\n" +
        "    }\n" +
        "}\n" +
        "class AX {\n" +
        "    void afoo() {}\n" +
        "}\n" +
        "class BX {\n" +
        "    void bfoo() {}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	void bar(X<? extends BX> x) {\n" +
        "	           ^^^^^^^^^^^^\n" +
        "Bound mismatch: The type ? extends BX is not a valid substitute for the bounded parameter <T extends AX> of the type X<T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 10)\n" +
        "	x.get().afoo();\n" +
        "	        ^^^^\n" +
        "The method afoo() is undefined for the type ? extends BX\n" +
        "----------\n");
  }

  // extending wildcard considers its bound prior to its corresponding variable
  public void test140() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    T get() {\n" +
        "        return this.t;\n" +
        "    }\n" +
        "    void bar(X<? extends BX> x) {\n" +
        "        x.get().afoo();\n" +
        "        x.get().bfoo();\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n" +
        "class AX {\n" +
        "    void afoo() {}\n" +
        "}\n" +
        "class BX extends AX {\n" +
        "    void bfoo() {}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // super wildcard considers its variable for lookups
  public void test141() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    T get() {\n" +
        "        return this.t;\n" +
        "    }\n" +
        "    void bar(X<? super BX> x) {\n" +
        "        x.get().afoo();\n" +
        "        x.get().bfoo();\n" +
        "    }\n" +
        "}\n" +
        "class AX {\n" +
        "    void afoo() {}\n" +
        "}\n" +
        "class BX extends AX {\n" +
        "    void bfoo() {}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 11)\n" +
        "	x.get().bfoo();\n" +
        "	        ^^^^\n" +
        "The method bfoo() is undefined for the type ? super BX\n" +
        "----------\n");
  }

  public void test142() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T extends AX> {\n" +
        "    T t;\n" +
        "    X(T t) {\n" +
        "        this.t = t;\n" +
        "    }\n" +
        "    T get() {\n" +
        "        return this.t;\n" +
        "    }\n" +
        "    void bar(X<? extends X> x) {\n" +
        "        x = identity(x);\n" +
        "    }\n" +
        "    <P extends AX> X<P> identity(X<P> x) {\n" +
        "        return x;\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "    }\n" +
        "}\n" +
        "class AX {\n" +
        "    void afoo() {}\n" +
        "}\n" +
        "class BX extends AX {\n" +
        "    void bfoo() {}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	void bar(X<? extends X> x) {\n" +
        "	           ^^^^^^^^^^^\n" +
        "Bound mismatch: The type ? extends X is not a valid substitute for the bounded parameter <T extends AX> of the type X<T>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 10)\n" +
        "	x = identity(x);\n" +
        "	    ^^^^^^^^\n" +
        "Bound mismatch: The generic method identity(X<P>) of type X<T> is not applicable for the arguments (X<? extends X>) since the type ? extends X is not a valid substitute for the bounded parameter <P extends AX>\n" +
        "----------\n");
  }

  public void test143() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        Class<? extends X> xx = null;\n" +
        "        Class<? extends Object> xo = xx;\n" +
        "        Class<Object> xo2 = xx;\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	Class<Object> xo2 = xx;\n" +
        "	              ^^^\n" +
        "Type mismatch: cannot convert from Class<? extends X> to Class<Object>\n" +
        "----------\n");
  }

  public void test144() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        Class<? extends X> xx = null;\n" +
        "        Class<? extends Object> xo = xx;\n" +
        "        X x = get(xx);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    static <P> P get(Class<P> cp) {\n" +
        "        return null;\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 59641: check assign/invoke with wildcards
  public void test145() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "		XList<?> lx = new XList<X>();\n" +
        "		X x = lx.get();\n" +
        "		lx.add(null);\n" +
        "		lx.add(x);\n" +
        "		lx.slot = x;\n" +
        "		lx.addAll(lx);\n" +
        "    }    	\n" +
        "}\n" +
        "class XList<E extends X> {\n" +
        "    E slot;\n" +
        "    void add(E e) {}\n" +
        "    E get() { return null; \n" +
        "    }\n" +
        "    void addAll(XList<E> le) {}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	lx.add(x);\n" +
        "	^^^^^^^^^\n" +
        "Bound mismatch: The method add(?) of type XList<?> is not applicable for the arguments (X). The wildcard parameter ? has no lower bound, and may actually be more restrictive than argument X\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	lx.slot = x;\n" +
        "	          ^\n" +
        "Bound mismatch: Cannot assign expression of type X to wildcard type ?. The wildcard type has no lower bound, and may actually be more restrictive than expression type\n" +
        "----------\n");
  }

  // 59628
  public void test146() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.AbstractList;\n" +
        "public class X extends AbstractList {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "    public int size() { return 0; }\n" +
        "    public Object get(int index) { return null; }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // 59723
  public void test147() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "\n" +
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "	    char[][] tokens = new char[0][];\n" +
        "	    ArrayList list = new ArrayList();\n" +
        "		list.toArray(tokens);\n" +
        "      System.out.println(\"SUCCESS\");\n" +
        "    }    	\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // bridge method
  public void test148() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X extends AX<String>{\n" +
        "    \n" +
        "    String foo(String s) {\n" +
        "        System.out.println(s);\n" +
        "        return s;\n" +
        "    }\n" +
        "	public static void main(String[] args) {\n" +
        "	   new X().bar(\"SUCCESS\");\n" +
        "    }    	\n" +
        "}\n" +
        "class AX<T> {\n" +
        "    T foo(T t) {\n" +
        "        return null;\n" +
        "    }\n" +
        "    void bar(T t) {\n" +
        "        foo(t);\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // method compatibility
  public void test149() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public abstract class X implements java.util.Collection {\n" +
        "	public Object[] toArray(Object[] a) {\n" +
        "		return a;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	   System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public abstract class X implements java.util.Collection<Object> {\n" +
        "	public Object[] toArray(Object[] a) {\n" +
        "		return a;\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 2)\n" +
        "	public Object[] toArray(Object[] a) {\n" +
        "	                ^^^^^^^^^^^^^^^^^^^\n" +
        "return type needs unchecked conversion from Collection<Object>.toArray(T[])\n" +
        "----------\n");
  }

  public void test150() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X {\n" +
        "    \n" +
        "    <T extends X> void foo(T[] ta, List<T> lt) {\n" +
        "    }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "		new X().foo(args, new ArrayList<String>());\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X().foo(args, new ArrayList<String>());\n" +
        "	        ^^^\n" +
        "Bound mismatch: The generic method foo(T[], List<T>) of type X is not applicable for the arguments (String[], List<String>) since the type String is not a valid substitute for the bounded parameter <T extends X>\n" +
        "----------\n");
  }

  public void test151() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X <E>{\n" +
        "    \n" +
        "    <T extends X> X(T[] ta, List<T> lt) {\n" +
        "    }\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "		new X<Object>(args, new ArrayList<String>());\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X<Object>(args, new ArrayList<String>());\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Bound mismatch: The generic constructor X(T[], List<T>) of type X<E> is not applicable for the arguments (String[], List<String>) since the type String is not a valid substitute for the bounded parameter <T extends X>\n" +
        "----------\n");
  }

  // 60556
  public void test152() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    List<X> x(List<X> list) {\n" +
        "        return Collections.unmodifiableList(list);\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test153() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<X> a = bar(ax);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<? extends T> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test154() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<X> a = bar(ax);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<? super T> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test155() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<X> a = bar(ax);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<?> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test156() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<X> a = bar(ax);\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<?> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E extends X> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	public static <T> AX<T> bar(AX<?> a) {\n" +
        "	                     ^\n" +
        "Bound mismatch: The type T is not a valid substitute for the bounded parameter <E extends X> of the type AX<E>\n" +
        "----------\n");
  }

  public void test157() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<String> as = new AX<String>();\n" +
        "        AX<X> a = bar(ax, as);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T,U> AX<T> bar(AX<? extends U> a, AX<? super U> b) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	AX<X> a = bar(ax, as);\n" +
        "	          ^^^\n" +
        "The method bar(AX<? extends U>, AX<? super U>) in the type X is not applicable for the arguments (AX<X>, AX<String>)\n" +
        "----------\n");
  }

  public void test158() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<String> as = new AX<String>();\n" +
        "        AX<X> a = bar(ax, as);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T,U> AX<T> bar(AX<?> a, AX<? super U> b) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test159() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "        AX<String> as = new AX<String>(\"SUCCESS\");\n" +
        "        AX<X> a = bar(ax, as);\n" +
        "	}\n" +
        "    public static <T,U> T bar(AX<?> a, AX<? super U> b) {\n" +
        "		return a.get();\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from ? to T\n" +
        "----------\n");
  }

  public void test160() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        String s = foo(new AX<String>(\"aaa\"));\n" +
        "	}\n" +
        "    static <V> V foo(AX<String> a) {\n" +
        "        return a.get();\n" +
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from String to V\n" +
        "----------\n");
  }

  public void test161() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        boolean b = foo(new AX<String>(\"aaa\")).equals(args);\n" +
        "	}\n" +
        "    static <V> V foo(AX<String> a) {\n" +
        "        return a.get();\n" +
        "    }\n" +
        "    String bar() {\n" +
        "        return \"bbb\";\n" +
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from String to V\n" +
        "----------\n");
  }

  public void test162() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        String s = foo(new AX<String>(\"aaa\")).bar();\n" +
        "	}\n" +
        "    static <V> V foo(AX<String> a) {\n" +
        "        return a.get();\n" +
        "    }\n" +
        "    String bar() {\n" +
        "        return \"bbb\";\n" +
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	String s = foo(new AX<String>(\"aaa\")).bar();\n" +
        "	                                      ^^^\n" +
        "The method bar() is undefined for the type V\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from String to V\n" +
        "----------\n");
  }

  public void test163() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        String s = foo(new AX<String>(\"aaa\")).bar();\n" +
        "	}\n" +
        "    static <V> V foo(AX<String> a) {\n" +
        "        return a.get();\n" +
        "    }\n" +
        "    String bar() {\n" +
        "        return \"bbb\";\n" +
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	String s = foo(new AX<String>(\"aaa\")).bar();\n" +
        "	                                      ^^^\n" +
        "The method bar() is undefined for the type V\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from String to V\n" +
        "----------\n");
  }

  public void test164() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        foo(new AX<String>(\"SUCCESS\"));\n" +
        "	}\n" +
        "    static <V> List<V> foo(AX<String> a) {\n" +
        "        System.out.println(a.get());\n" +
        "        List<V> v = null;\n" +
        "        if (a == null) v = foo(a); \n" +
        "        return v;\n" +
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test165() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>();\n" +
        "        AX<String> a = bar(ax);\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<?> a) {\n" +
        "		 if (a == null) {\n" +
        "        	AX<String> as = bar(a);\n" +
        "        	String s = as.get();\n" +
        "		}\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E get() { return null; }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test166() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "        AX<String> a = bar(ax, true);\n" +
        "        String s = a.get();\n" +
        "        System.out.println(s);\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(AX<?> a, boolean recurse) {\n" +
        "        if (recurse) {\n" +
        "	        AX<String> as = bar(a, false);\n" +
        "			String s = as.get();\n" +
        "        }\n" +
        "		return new AX(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test167() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<String, Thread> a = bar();\n" +
        "        String s = a.get();\n" +
        "        System.out.println(s);\n" +
        "	}\n" +
        "    public static <T, U> AX<T, U> bar() {\n" +
        "		return new AX(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E, F> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test168() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<String, Thread> a = bar();\n" +
        "        String s = a.get();\n" +
        "        System.out.println(s);\n" +
        "	}\n" +
        "    public static <T, U> AX<AX<T, T>, U> bar() {\n" +
        "		return new AX(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E, F> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	AX<String, Thread> a = bar();\n" +
        "	                   ^\n" +
        "Type mismatch: cannot convert from AX<AX<T,T>,Thread> to AX<String,Thread>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 9)\n" +
        "	return new AX(\"SUCCESS\");\n" +
        "	       ^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor AX(E) of raw type AX. References to generic type AX<E,F> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 9)\n" +
        "	return new AX(\"SUCCESS\");\n" +
        "	       ^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<AX<T,T>,U>. References to generic type AX<E,F> should be parameterized\n" +
        "----------\n");
  }

  public void test169() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<String> a = bar(new X());\n" +
        "        String s = a.get();\n" +
        "        System.out.println(s);\n" +
        "	}\n" +
        "    public static <T> AX<T> bar(T t) {\n" +
        "		return new AX(\"SUCCESS\");\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "    E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	AX<String> a = bar(new X());\n" +
        "	           ^\n" +
        "Type mismatch: cannot convert from AX<X> to AX<String>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 9)\n" +
        "	return new AX(\"SUCCESS\");\n" +
        "	       ^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the constructor AX(E) of raw type AX. References to generic type AX<E> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 9)\n" +
        "	return new AX(\"SUCCESS\");\n" +
        "	       ^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<T>. References to generic type AX<E> should be parameterized\n" +
        "----------\n");
  }

  // Expected type inference for cast operation
  public void test170() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "        AX<String> as = new AX<String>(\"\");\n" +
        "        ax = (AX)bar(ax);\n" + // shouldn't complain about unnecessary cast
        "	}\n" +
        "    public static <T> T bar(AX<?> a) {\n" +
        "		return a.get();\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 6)\n" +
        "	ax = (AX)bar(ax);\n" +
        "	     ^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type AX to type AX<X>. References to generic type AX<E> should be parameterized\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 9)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from ? to T\n" +
        "----------\n");
  }

  // Expected type inference for cast operation
  public void test171() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "        AX<String> as = new AX<String>(\"\");\n" +
        "        ax = (AX<X>)bar(ax);\n" + // should still complain about unnecessary cast as return type inference would have
        "	}\n" + // worked the same without the cast due to assignment
        "    public static <T> T bar(AX<?> a) {\n" +
        "		return a.get();\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 6)\n" +
        "	ax = (AX<X>)bar(ax);\n" +
        "	     ^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type AX<X> for expression of type AX<X>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 9)\n" +
        "	return a.get();\n" +
        "	       ^^^^^^^\n" +
        "Type mismatch: cannot convert from ? to T\n" +
        "----------\n");
  }

  // Expected type inference for cast operation
  public void test172() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "        AX<String> as = new AX<String>(\"SUCCESS\");\n" +
        "        ax = (AX<X>)bar(ax);\n" + // no warn for unsafe cast, since forbidden cast
        "	}\n" +
        "    public static <T> String bar(AX<?> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	ax = (AX<X>)bar(ax);\n" +
        "	     ^^^^^^^^^^^^^^\n" +
        "Cannot cast from String to AX<X>\n" +
        "----------\n");
  }

  // Expected type inference for return statement
  public void test173() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "    	foo();\n" +
        "    	System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> T bar(AX<?> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "    public static AX<X> foo() {\n" +
        "        AX<X> ax = new AX<X>(new X());\n" +
        "       return bar(ax);\n" + // use return type of enclosing method for type inference
        "    }\n" +
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n" +
        "\n"
    },
        "SUCCESS");
  }

  // Expected type inference for field declaration
  public void test174() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    \n" +
        "    public static void main(String[] args) {\n" +
        "    	Object o = foo;\n" +
        "    	System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "    public static <T> T bar(AX<?> a) {\n" +
        "		return null;\n" +
        "    }    \n" +
        "    static AX<X> foo = bar(new AX<X>(new X()));\n" + // use field type for type inference
        "}\n" +
        "class AX<E> {\n" +
        "	 E e;\n" +
        "    AX(E e) { this.e = e; }\n" +
        "    E get() { return this.e; }\n" +
        "}\n" +
        "\n"
    },
        "SUCCESS");
  }

  // 60563
  public void test175() {
    this.runConformTest(
        new String[] {
        "X.java",
        "    interface A<T> {\n" +
        "        T[] m1(T x);                          \n" +
        "    }\n" +
        "    public class X { \n" +
        "    	public static void main(String[] args) {\n" +
        "			new X().m2(new A<X>(){ \n" +
        "				public X[] m1(X x) { \n" +
        "					System.out.println(\"SUCCESS\");\n" +
        "					return null;\n" +
        "				}\n" +
        "			});\n" +
        "		}\n" +
        "        void m2(A<X> x) { \n" +
        "            m3(x.m1(new X())); \n" +
        "        }\n" +
        "        void m3(X[] x) {\n" +
        "        }                    \n" +
        "    }\n"
    },
        "SUCCESS");
  }

  // unsafe raw return value
  public void test176() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "\n" +
        "public class X {\n" +
        "    <T> Vector<T> valuesOf(Hashtable<?, T> h) {\n" +
        "        return new Vector();\n" +
        "    }\n" +
        "    Vector<Object> data;\n" +
        "    \n" +
        "    public void t() {\n" +
        "        Vector<Object> v = (Vector<Object>) data.elementAt(0);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 5)\n" +
        "	return new Vector();\n" +
        "	       ^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type Vector to type Vector<T>. References to generic type Vector<E> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 10)\n" +
        "	Vector<Object> v = (Vector<Object>) data.elementAt(0);\n" +
        "	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Object to parameterized type Vector<Object> will not check conformance of type arguments at runtime\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // cast to type variable allowed, can be diagnosed as unnecessary
  public void test177() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "	\n" +
        "	T foo(T t) {\n" +
        "		return (T) t;\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 4)\n" +
        "	return (T) t;\n" +
        "	       ^^^^^\n" +
        "Unnecessary cast to type T for expression of type T\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // reject instanceof type variable or parameterized type
  public void test178() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "	\n" +
        "	T foo(T t) {\n" +
        "		if (t instanceof X<T>) {\n" +
        "			return t;\n" +
        "		} else if (t instanceof X<String>) {\n" +
        "			return t;\n" +
        "		} else 	if (t instanceof T) {\n" +
        "			return t;\n" +
        "		} else if (t instanceof X) {\n" +
        "			return t;\n" +
        "		}\n" +
        "		return null;\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	if (t instanceof X<T>) {\n" +
        "	    ^^^^^^^^^^^^^^\n" +
        "Cannot perform instanceof check against parameterized type X<T>. Use instead its raw form X since generic type information will be erased at runtime\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	} else if (t instanceof X<String>) {\n" +
        "	           ^^^^^^^^^^^^^^\n" +
        "Cannot perform instanceof check against parameterized type X<String>. Use instead its raw form X since generic type information will be erased at runtime\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 8)\n" +
        "	} else 	if (t instanceof T) {\n" +
        "	       	    ^^^^^^^^^^^^^^\n" +
        "Cannot perform instanceof check against type parameter T. Use instead its erasure Object since generic type information will be erased at runtime\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // 61507
  public void test179() {
    this.runConformTest(
        new String[] {
        "X.java",
        "class U {\n" +
        " static <T> T notNull(T t) { return t; }\n" +
        "}\n" +
        "public class X {\n" +
        " void t() {\n" +
        "  String s = U.notNull(null);\n" +
        " }\n" +
        " public static void main(String[] args) {\n" +
        "	new X().t();\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test180() {
    this.runConformTest(
        new String[] {
        "X.java",
        "class U {\n" +
        " static <T> T notNull(T t) { return t; }\n" +
        "}\n" +
        "public class X {\n" +
        " void t() {\n" +
        "  String s = U.notNull(\"\");\n" +
        " }\n" +
        " public static void main(String[] args) {\n" +
        "	new X().t();\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 61507 - variation computing most specific type with 'null'
  public void test181() {
    this.runConformTest(
        new String[] {
        "X.java",
        "class U {\n" +
        " static <T> T notNull(T t, V<T> vt) { return t; }\n" +
        "}\n" +
        "class V<T> {}\n" +
        "\n" +
        "public class X {\n" +
        " void t() {\n" +
        "  String s = U.notNull(null, new V<String>());\n" +
        " }\n" +
        " public static void main(String[] args) {\n" +
        "	new X().t();\n" +
        "	System.out.println(\"SUCCESS\");\n" +
        "}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  public void test182() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<E> {\n" +
        "    X<E> foo() {\n" +
        "    	return (X<E>) this;\n" +
        "    }\n" +
        "    X<String> bar() {\n" +
        "    	return (AX<String>) new X<String>();\n" +
        "    }\n" +
        "    X<String> bar(Object o) {\n" +
        "    	return (AX<String>) o;\n" +
        "    }\n" +
        "    X<E> foo(Object o) {\n" +
        "    	return (AX<E>) o;\n" +
        "    }    \n" +
        "    X<E> baz(Object o) {\n" +
        "    	return (AX<E>) null;\n" +
        "    }\n" +
        "    X<String> baz2(BX bx) {\n" +
        "    	return (X<String>) bx;\n" +
        "    }    \n" +
        "}\n" +
        "class AX<F> extends X<F> {}\n" +
        "class BX extends AX<String> {}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	return (X<E>) this;\n" +
        "	       ^^^^^^^^^^^\n" +
        "Unnecessary cast to type X<E> for expression of type X<E>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 9)\n" +
        "	return (AX<String>) o;\n" +
        "	       ^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Object to parameterized type AX<String> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 12)\n" +
        "	return (AX<E>) o;\n" +
        "	       ^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Object to parameterized type AX<E> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 15)\n" +
        "	return (AX<E>) null;\n" +
        "	       ^^^^^^^^^^^^\n" +
        "Unnecessary cast to type AX<E> for expression of type null\n" +
        "----------\n" +
        "5. WARNING in X.java (at line 18)\n" +
        "	return (X<String>) bx;\n" +
        "	       ^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type X<String> for expression of type BX\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  public void test183() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "\n" +
        "public class X {\n" +
        "	\n" +
        "	{\n" +
        "		Dictionary<String, Integer> d;\n" +
        "		Object o;\n" +
        "		\n" +
        "		Object a1 = (Hashtable<String,Integer>) d;\n" +
        "		Object a2 = (Hashtable) o;\n" +
        "\n" +
        "		Object a3 = (Hashtable<Float, Double>) d;\n" +
        "		Object a4 = (Hashtable<String,Integer>) o;\n" +
        "		\n" +
        "		abstract class Z1 extends Hashtable<String,Integer> {\n" +
        "		}\n" +
        "		Z1 z1;\n" +
        "		Object a5 = (Hashtable<String,Integer>) z1;\n" +
        "\n" +
        "		abstract class Z2 extends Z1 {\n" +
        "		}\n" +
        "		Object a6 = (Z2) z1;\n" +
        "\n" +
        "		abstract class Z3 extends Hashtable {\n" +
        "		}\n" +
        "		Z3 z3;\n" +
        "		Object a7 = (Hashtable<String,Integer>) z3;\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 12)\n" +
        "	Object a3 = (Hashtable<Float, Double>) d;\n" +
        "	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Cannot cast from Dictionary<String,Integer> to Hashtable<Float,Double>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 13)\n" +
        "	Object a4 = (Hashtable<String,Integer>) o;\n" +
        "	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Object to parameterized type Hashtable<String,Integer> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 18)\n" +
        "	Object a5 = (Hashtable<String,Integer>) z1;\n" +
        "	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type Hashtable<String,Integer> for expression of type Z1\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 27)\n" +
        "	Object a7 = (Hashtable<String,Integer>) z3;\n" +
        "	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Z3 to parameterized type Hashtable<String,Integer> will not check conformance of type arguments at runtime\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // 62292 - parameterized message send
  public void test184() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	static <T, U> T foo(T t, U u) {\n" +
        "		return t;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(X.<String,X>foo(\"SUCCESS\", null));\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // parameterized message send - variation on 184 with non-static generic method
  public void test185() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	<T, U> T foo(T t, U u) {\n" +
        "		return t;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String,X>foo(\"SUCCESS\", null));\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // message send parameterized with type not matching parameter bounds
  public void test186() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	<T, U extends String> T foo(T t, U u) {\n" +
        "		return t;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String, X>foo(\"SUCCESS\", null));\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	<T, U extends String> T foo(T t, U u) {\n" +
        "	              ^^^^^^\n" +
        "The type parameter U should not be bounded by the final type String. Final types cannot be further extended\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	System.out.println(new X().<String, X>foo(\"SUCCESS\", null));\n" +
        "	                                      ^^^\n" +
        "Bound mismatch: The generic method foo(T, U) of type X is not applicable for the arguments (String, X) since the type X is not a valid substitute for the bounded parameter <U extends String>\n" +
        "----------\n");
  }

  // invalid type argument arity for parameterized message send
  public void test187() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	<T, U extends String> T foo(T t, U u) {\n" +
        "		return t;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	<T, U extends String> T foo(T t, U u) {\n" +
        "	              ^^^^^^\n" +
        "The type parameter U should not be bounded by the final type String. Final types cannot be further extended\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 7)\n" +
        "	System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" +
        "	                                   ^^^\n" +
        "Incorrect number of type arguments for generic method <T, U>foo(T, U) of type X; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // parameterized invocation of non generic method with incorrect argument count
  public void test188() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	void foo() {\n" +
        "		return;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" +
        "	                                   ^^^\n" +
        "The method foo() in the type X is not applicable for the arguments (String, null)\n" +
        "----------\n");
  }

  // parameterized invocation of non generic method
  public void test189() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	void foo() {\n" +
        "		return;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String>foo());\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	System.out.println(new X().<String>foo());\n" +
        "	                                   ^^^\n" +
        "The method foo() of type X is not generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // parameterized allocation
  public void test190() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new <String>X(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // parameterized allocation - wrong arity
  public void test191() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new <String, String>X(\"FAILED\");\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	new <String, String>X(\"FAILED\");\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Incorrect number of type arguments for generic constructor <T>X(T) of type X; it cannot be parameterized with arguments <String, String>\n" +
        "----------\n");
  }

  // parameterized allocation - non generic target constructor
  public void test192() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public X(String t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new <String>X(\"FAILED\");\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	new <String>X(\"FAILED\");\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor X(String) of type X is not generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // parameterized allocation - argument type mismatch
  public void test193() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new <String>X(new X(null));\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	new <String>X(new X(null));\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The parameterized constructor <String>X(String) of type X is not applicable for the arguments (X)\n" +
        "----------\n");
  }

  // parameterized invocation - argument type mismatch
  public void test194() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "\n" +
        "	<T> void foo(T t) {\n" +
        "		return;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		System.out.println(new X().<String>foo(new X()));\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	System.out.println(new X().<String>foo(new X()));\n" +
        "	                                   ^^^\n" +
        "The parameterized method <String>foo(String) of type X is not applicable for the arguments (X)\n" +
        "----------\n");
  }

  // parameterized qualified allocation
  public void test195() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public class MX {\n" +
        "		public <T> MX(T t){\n" +
        "			System.out.println(t);\n" +
        "		}\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new X().new <String>MX(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // parameterized qualified allocation - wrong arity
  public void test196() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public class MX {\n" +
        "		public <T> MX(T t){\n" +
        "			System.out.println(t);\n" +
        "		}\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new X().new <String,String>MX(\"FAILED\");\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X().new <String,String>MX(\"FAILED\");\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Incorrect number of type arguments for generic constructor <T>MX(T) of type X.MX; it cannot be parameterized with arguments <String, String>\n" +
        "----------\n");
  }

  // parameterized qualified allocation - non generic target constructor
  public void test197() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public class MX {\n" +
        "		public MX(String t){\n" +
        "			System.out.println(t);\n" +
        "		}\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new X().new <String>MX(\"FAILED\");\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X().new <String>MX(\"FAILED\");\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The constructor MX(String) of type X.MX is not generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // parameterized qualified allocation - argument type mismatch
  public void test198() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public class MX {\n" +
        "		public <T>MX(T t){\n" +
        "			System.out.println(t);\n" +
        "		}\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		new X().new <String>MX(new X());\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	new X().new <String>MX(new X());\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The parameterized constructor <String>MX(String) of type X.MX is not applicable for the arguments (X)\n" +
        "----------\n");
  }

  // parameterized explicit constructor call
  public void test199() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		class Local extends X {\n" +
        "			Local() {\n" +
        "				<String>super(\"SUCCESS\");\n" +
        "			}\n" +
        "		};\n" +
        "		new Local();\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // parameterized explicit constructor call - wrong arity
  public void test200() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		class Local extends X {\n" +
        "			Local() {\n" +
        "				<String,String>super(\"FAILED\");\n" +
        "			}\n" +
        "		};\n" +
        "		new Local();\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	<String,String>super(\"FAILED\");\n" +
        "	               ^^^^^^^^^^^^^^^\n" +
        "Incorrect number of type arguments for generic constructor <T>X(T) of type X; it cannot be parameterized with arguments <String, String>\n" +
        "----------\n");
  }

  // parameterized explicit constructor call - non generic target constructor
  public void test201() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public X(String t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		class Local extends X {\n" +
        "			Local() {\n" +
        "				<String>super(\"FAILED\");\n" +
        "			}\n" +
        "		};\n" +
        "		new Local();\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	<String>super(\"FAILED\");\n" +
        "	        ^^^^^^^^^^^^^^^\n" +
        "The constructor X(String) of type X is not generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // parameterized explicit constructor call - argument type mismatch
  public void test202() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public <T> X(T t){\n" +
        "		System.out.println(t);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		class Local extends X {\n" +
        "			Local() {\n" +
        "				<String>super(new X(null));\n" +
        "			}\n" +
        "		};\n" +
        "		new Local();\n" +
        "	}\n" +
        "}\n", },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	<String>super(new X(null));\n" +
        "	        ^^^^^^^^^^^^^^^^^^\n" +
        "The parameterized constructor <String>X(String) of type X is not applicable for the arguments (X)\n" +
        "----------\n");
  }

  // 62822 - supertypes partially resolved during bound check
  public void test203() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "		demo.AD ad;\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n",
        "demo/AD.java",
        "package demo;\n" +
        "public interface AD extends LIST<ADXP> {}\n",
        "demo/ADXP.java",
        "package demo;\n" +
        "public interface ADXP extends BIN {}\n",
        "demo/ANY.java",
        "package demo;\n" +
        "public interface ANY {}\n",
        "demo/BL.java",
        "package demo;\n" +
        "public interface BL extends ANY {}\n",
        "demo/LIST.java",
        "package demo;\n" +
        "public interface LIST<T extends ANY> extends ANY {}\n",
        "demo/BIN.java",
        "package demo;\n" +
        "public interface BIN extends LIST<BL> {}\n",
    },
        "SUCCESS");
  }

  // 62806
  public void test204() {
    this.runConformTest(
        new String[] {
        "Function.java",
        "public abstract class Function<Y,X> {\n" +
        "    public abstract Y eval(X x);\n" +
        "\n" +
        "}\n",
        "FunctionMappedComparator.java",
        "import java.util.*;\n" +
        "public class FunctionMappedComparator<Y,X> implements Comparator<X> {\n" +
        "	/*\n" +
        "	 * \'Function\' is highlighted as an error here - the message is:\n" +
        "	 * The type Function is not generic; it cannot be parameterized with arguments <Y, X>\n" +
        "	 */\n" +
        "    protected Function<Y,X> function;\n" +
        "    protected Comparator<Y> comparator;\n" +
        "    public FunctionMappedComparator(Function<Y,X> function,Comparator<Y> comparator ) {\n" +
        "        this.function=function;\n" +
        "        this.comparator=comparator;\n" +
        "    }\n" +
        "\n" +
        "    public int compare(X x1, X x2) {\n" +
        "        return comparator.compare(function.eval(x1),function.eval(x2));\n" +
        "    }\n" +
        "}\n",
    },
        "");
  }

  // 63555 - reference to static type parameter allowed inside type itself
  public void test205() {
    this.runConformTest(
        new String[] {
        "Alpha.java",
        "public class Alpha {\n" +
        "	static class Beta<T> {\n" +
        "		T obj;\n" +
        "	}\n" +
        "}\n",
    },
        "");
  }

  // 63555 - variation on static method type parameter
  public void test206() {
    this.runConformTest(
        new String[] {
        "Alpha.java",
        "public class Alpha {\n" +
        "	static <T> void Beta(T t) {\n" +
        "	}\n" +
        "}\n",
    },
        "");
  }

  // 63590 - disallow parameterized type in catch/throws clause
  public void test207() {
    this.runNegativeTest(
        new String[] {
        "Alpha.java",
        "public class Alpha<T> extends RuntimeException {\n" +
        "	public static void main(String[] args) {\n" +
        "		new Object() {\n" +
        "			public void m() throws Alpha<String> {\n" +
        "				System.out.println(\"SUCCESS\");\n" +
        "			}\n" +
        "		}.m();\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in Alpha.java (at line 1)\n" +
        "	public class Alpha<T> extends RuntimeException {\n" +
        "	             ^^^^^\n" +
        "The serializable class Alpha does not declare a static final serialVersionUID field of type long\n" +
        "----------\n" +
        "2. ERROR in Alpha.java (at line 1)\n" +
        "	public class Alpha<T> extends RuntimeException {\n" +
        "	                              ^^^^^^^^^^^^^^^^\n" +
        "The generic class Alpha<T> may not subclass java.lang.Throwable\n" +
        "----------\n" +
        "3. ERROR in Alpha.java (at line 4)\n" +
        "	public void m() throws Alpha<String> {\n" +
        "	                       ^^^^^\n" +
        "Cannot use the parameterized type Alpha<String> either in catch block or throws clause\n" +
        "----------\n");
  }

  // 63590 - disallow parameterized type in catch/throws clause
  public void test208() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> extends RuntimeException {\n" +
        "	public static void main(String[] args) {\n" +
        "		try {\n" +
        "			throw new X<String>();\n" +
        "		} catch(X<String> e) {\n" +
        "			System.out.println(\"X<String>\");\n" +
        "		} catch(X<X<String>> e) {\n" +
        "			System.out.println(\"X<X<String>>\");\n" +
        "		} catch(RuntimeException e) {\n" +
        "			System.out.println(\"RuntimeException\");\n" +
        "		}\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 1)\n" +
        "	public class X<T> extends RuntimeException {\n" +
        "	             ^\n" +
        "The serializable class X does not declare a static final serialVersionUID field of type long\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 1)\n" +
        "	public class X<T> extends RuntimeException {\n" +
        "	                          ^^^^^^^^^^^^^^^^\n" +
        "The generic class X<T> may not subclass java.lang.Throwable\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 5)\n" +
        "	} catch(X<String> e) {\n" +
        "	                  ^\n" +
        "Cannot use the parameterized type X<String> either in catch block or throws clause\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 7)\n" +
        "	} catch(X<X<String>> e) {\n" +
        "	                     ^\n" +
        "Cannot use the parameterized type X<X<String>> either in catch block or throws clause\n" +
        "----------\n");
  }

  // 63556 - should resolve all occurrences of A to type variable
  public void test209() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<A,B,C extends java.util.List<A>> {}\n" +
        "class X2<A,C extends java.util.List<A>, B> {}\n" +
        "class X3<B, A,C extends java.util.List<A>> {}\n",
    },
        "");
  }

  // 68006 - Invalid modifier after parse
  public void test210() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	void foo(Map<? super Object, ? extends String> m){\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	void foo(Map<? super Object, ? extends String> m){\n" +
        "	         ^^^\n" +
        "Map cannot be resolved to a type\n" +
        "----------\n");
  }

  // test compilation against binaries
  public void test211() {
    this.runConformTest(
        new String[] {
        "p/Top.java",
        "package p;\n" +
        "public interface Top<T> {}\n",
    },
        "");

    this.runConformTest(
        new String[] {
        "p/Super.java",
        "package p;\n" +
        "public class Super<T> implements Top<T>{\n" +
        "    public static void main(String [] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        false, // do not flush output
        null);
  }

  // check type variable equivalence
  public void test212() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "\n" +
        "public class X<T>{\n" +
        "    public static void main(String [] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "	X<T> _recurse; \n" +
        "	public List<T> toList(){\n" +
        "		List<T> result = new ArrayList<T>();\n" +
        "		result.addAll(_recurse.toList()); // should be applicable\n" +
        "		return result;\n" +
        "	}\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test213() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<R,T extends Comparable<T>>{\n" +
        "	T test;\n" +
        "	public Comparable<? extends T> getThis(){\n" +
        "		return test;\n" +
        "	}\n" +
        "    public static void main(String [] args) {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // 68133 - verify error
  public void test214() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        ArrayList<Object> l;\n" +
        "        switch (args.length) {\n" +
        "        case 1:\n" +
        "            l = new ArrayList<Object>();\n" +
        "            System.out.println(l);\n" +
        "            break;\n" +
        "        default:\n" +
        "            System.out.println(\"SUCCESS\");\n" +
        "            return;\n" +
        "        }\n" +
        "    }\n" +
        "}\n"
    },
        "SUCCESS");
  }

  // 68133 variation
  public void test215() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "		java.util.ArrayList<Object> i;	\n" +
        "		outer: {\n" +
        "			if (args == null) {\n" +
        "				i = null;\n" +
        "				break outer;\n" +
        "			}\n" +
        "			return;\n" +
        "		}\n" +
        "		System.out.println(i);	\n" +
        "		System.out.println(\"SUCCESS\");	\n" +
        "	}\n" +
        "}\n",
    },
        "");

// JAVA5: todo - remove or reimplement!
//    String expectedOutput =
//        "  // Method descriptor  #15 ([Ljava/lang/String;)V\n" +
//        "  // Stack: 2, Locals: 2\n" +
//        "  public static void main(String[] args);\n" +
//        "     0  aload_0\n" +
//        "     1  ifnonnull 9\n" +
//        "     4  aconst_null\n" +
//        "     5  astore_1\n" +
//        "     6  goto 10\n" +
//        "     9  return\n" +
//        "    10  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" +
//        "    13  aload_1\n" +
//        "    14  invokevirtual #27 <Method java/io/PrintStream.println(Ljava/lang/Object;)V>\n" +
//        "    17  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" +
//        "    20  ldc #29 <String \"SUCCESS\">\n" +
//        "    22  invokevirtual #32 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" +
//        "    25  return\n" +
//        "      Line numbers:\n" +
//        "        [pc: 0, line: 5]\n" +
//        "        [pc: 4, line: 6]\n" +
//        "        [pc: 6, line: 7]\n" +
//        "        [pc: 9, line: 9]\n" +
//        "        [pc: 10, line: 11]\n" +
//        "        [pc: 17, line: 12]\n" +
//        "        [pc: 25, line: 13]\n" +
//        "      Local variable table:\n" +
//        "        [pc: 0, pc: 26] local: args index: 0 type: [Ljava/lang/String;\n" +
//        "        [pc: 6, pc: 9] local: i index: 1 type: Ljava/util/ArrayList;\n" +
//        "        [pc: 10, pc: 26] local: i index: 1 type: Ljava/util/ArrayList;\n" +
//        "      Local variable type table:\n" +
//        "        [pc: 6, pc: 9] local: i index: 1 type: Ljava/util/ArrayList<Ljava/lang/Object;>;\n" +
//        "        [pc: 10, pc: 26] local: i index: 1 type: Ljava/util/ArrayList<Ljava/lang/Object;>;\n";
//
//    try {
//      File f = new File(OUTPUT_DIR + File.separator + "X.class");
//      byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.
//          getFileByteContent(f);
//      ClassFileBytesDisassembler disassembler = ToolFactory.
//          createDefaultClassFileBytesDisassembler();
//      String result = disassembler.disassemble(classFileBytes, "\n",
//          ClassFileBytesDisassembler.DETAILED);
//      int index = result.indexOf(expectedOutput);
//      if (index == -1 || expectedOutput.length() == 0) {
//        System.out.println(Util.displayString(result, 3));
//      }
//      if (index == -1) {
//        assertEquals("Wrong contents", expectedOutput, result);
//      }
//    } catch (org.eclipse.jdt.core.util.ClassFormatException e) {
//      assertTrue(false);
//    } catch (IOException e) {
//      assertTrue(false);
//    }
  }

  // 68998 parameterized field constants
  public void test216() {
    this.runConformTest(
        new String[] {
        "test/cheetah/NG.java",
        "package test.cheetah;\n" +
        "public class NG extends G {\n" +
        "		public static void main(String[] args) {\n" +
        "			System.out.println(\"SUCCESS\");	\n" +
        "		}\n" +
        "    public boolean test() {\n" +
        "        return o == null;\n" +
        "    }\n" +
        "}\n",
        "test/cheetah/G.java",
        "package test.cheetah;\n" +
        "public class G<E> {\n" +
        "    protected Object o;\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69135 - unnecessary cast operation
  public void test217() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "public class X {\n" +
        "    public static void main(String [] args) {\n" +
        "		ArrayList<String> l= new ArrayList<String>();\n" +
        "		String string = (String) l.get(0);\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 5)\n" +
        "	String string = (String) l.get(0);\n" +
        "	                ^^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type String for expression of type String\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // 64154 visibility issue due to invalid use of parameterized binding
  public void test218() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T>{\n" +
        "	private final T _data;\n" +
        "	private X(T data){\n" +
        "		_data = data;\n" +
        "	}\n" +
        "	public T getData(){\n" +
        "		return _data;\n" +
        "	}\n" +
        "	public static <E> X<E> create(E data) {\n" +
        "		return new X<E>(data);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		create(new Object());\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 64154 variation
  public void test219() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T>{\n" +
        "	private final T _data;\n" +
        "	private X(T data){\n" +
        "		_data = data;\n" +
        "	}\n" +
        "	public T getData(){\n" +
        "		return _data;\n" +
        "	}\n" +
        "	public static <E> E create(E data) {\n" +
        "		return new X<E>(data)._data;\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		create(new Object());\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69141 unsafe wildcard operation tolerates wildcard with lower bounds
  public void test220() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "\n" +
        "public class X {\n" +
        "	void foo() {\n" +
        "		ArrayList<? super Integer> al = new ArrayList<Object>();\n" +
        "		al.add(new Integer(1)); // (1)\n" +
        "		Integer i = al.get(0);  // (2)\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	Integer i = al.get(0);  // (2)\n" +
        "	        ^\n" +
        "Type mismatch: cannot convert from ? super Integer to Integer\n" +
        "----------\n");
  }

  // 69141 variation
  public void test221() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "\n" +
        "public class X {\n" +
        "	void foo() {\n" +
        "		ArrayList<? extends Integer> al = new ArrayList<Integer>();\n" +
        "		al.add(new Integer(1)); // (1)\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	al.add(new Integer(1)); // (1)\n" +
        "	^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Bound mismatch: The method add(? extends Integer) of type ArrayList<? extends Integer> is not applicable for the arguments (Integer). The wildcard parameter ? extends Integer has no lower bound, and may actually be more restrictive than argument Integer\n" +
        "----------\n");
  }

  // 69141: variation
  public void test222() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "		XList<? super Integer> lx = new XList<Integer>();\n" +
        "		lx.slot = new Integer(1);\n" +
        "		Integer i = lx.slot;\n" +
        "    }    	\n" +
        "}\n" +
        "class XList<E> {\n" +
        "    E slot;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	Integer i = lx.slot;\n" +
        "	        ^\n" +
        "Type mismatch: cannot convert from ? super Integer to Integer\n" +
        "----------\n");
  }

  // 69251- instantiating wildcards
  public void test223() {
    Map customOptions = getCompilerOptions();
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.HashMap;\n" +
        "import java.util.Map;\n" +
        "public class X {\n" +
        "    static final Map<String, Class<? extends Object>> classes \n" +
        "            = new HashMap<String, Class<? extends Object>>();\n" +
        "    \n" +
        "    static final Map<String, Class<? extends Object>> classes2 \n" +
        "            = new HashMap<String, Class>();\n" +
        "    \n" +
        "    class MX<E> {\n" +
        "    	E get() { return null; }\n" +
        "    	void foo(E e) {}\n" +
        "    }\n" +
        "    \n" +
        "    void foo() {\n" +
        "    	MX<Class<? extends Object>> mx1 = new MX<Class<? extends Object>>();\n" +
        "    	MX<Class> mx2 = new MX<Class>();\n" +
        "    	mx1.foo(mx2.get());\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	static final Map<String, Class<? extends Object>> classes2 \n" +
        "	                                                  ^^^^^^^^\n" +
        "Type mismatch: cannot convert from HashMap<String,Class> to Map<String,Class<? extends Object>>\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 18)\n" +
        "	mx1.foo(mx2.get());\n" +
        "	        ^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type Class to type Class<? extends Object>. References to generic type Class<T> should be parameterized\n" +
        "----------\n",
        null,
        true,
        customOptions);
  }

  // 68998 variation
  public void test224() {
    this.runNegativeTest(
        new String[] {
        "test/cheetah/NG.java",
        "package test.cheetah;\n" +
        "public class NG extends G {\n" +
        "		public static void main(String[] args) {\n" +
        "			System.out.println(\"SUCCESS\");	\n" +
        "		}\n" +
        "    public boolean test() {\n" +
        "        return o == null;\n" +
        "    }\n" +
        "}\n",
        "test/cheetah/G.java",
        "package test.cheetah;\n" +
        "public class G<E> {\n" +
        "    protected final Object o;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in test\\cheetah\\G.java (at line 2)\r\n" +
        "	public class G<E> {\r\n" +
        "	             ^\n" +
        "The blank final field o may not have been initialized\n" +
        "----------\n");
  }

  // 69353 - prevent using type parameter in catch block
  public void test225() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T extends Exception> {\n" +
        "    String foo() throws T {\n" +
        "        return \"SUCCESS\";\n" +
        "    }\n" +
        "    public static void main(String[] args) {\n" +
        "        new X<EX>().baz(new EX());\n" +
        "    }\n" +
        "    void baz(final T t) {\n" +
        "        new Object() {\n" +
        "            void print() {\n" +
        "                try {\n" +
        "	                System.out.println(foo());\n" +
        "                } catch (T t) {\n" +
        "                }\n" +
        "            }\n" +
        "        }.print();\n" +
        "    }\n" +
        "}\n" +
        "class EX extends Exception {\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 13)\n" +
        "	} catch (T t) {\n" +
        "	           ^\n" +
        "Cannot use the type parameter T in a catch block\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 19)\n" +
        "	class EX extends Exception {\n" +
        "	      ^^\n" +
        "The serializable class EX does not declare a static final serialVersionUID field of type long\n" +
        "----------\n");
  }

  // 69170 - invalid generic array creation
  public void test226() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T>{\n" +
        "	 Object x1= new T[0];\n" +
        "	 Object x2= new X<String>[0];	 \n" +
        "	 Object x3= new X<T>[0];	 \n" +
        "	 Object x4= new X[0];	 \n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	Object x1= new T[0];\n" +
        "	           ^^^^^^^^\n" +
        "Cannot create a generic array of T\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	Object x2= new X<String>[0];	 \n" +
        "	           ^^^^^^^^^^^^^^^^\n" +
        "Cannot create a generic array of X<String>\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 4)\n" +
        "	Object x3= new X<T>[0];	 \n" +
        "	           ^^^^^^^^^^^\n" +
        "Cannot create a generic array of X<T>\n" +
        "----------\n");
  }

  // 69359 - unsafe cast diagnosis
  public void test227() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        " import java.util.*;\n" +
        " public class X {\n" +
        "  List list() { return null; }\n" +
        "  void m() { List<X> l = (List<X>)list(); } // unsafe cast\n" +
        "  void m0() { List<X> l = list(); } // unsafe conversion\n" +
        "  void m1() { for (X a : list()); } // type mismatch\n" +
        "  void m2() { for (Iterator<X> i = list().iterator(); i.hasNext();); }  // unsafe conversion\n" +
        "  void m3() { Collection c = null; List l = (List<X>)c; } // unsafe cast\n" +
        "  void m4() { Collection c = null; List l = (List<?>)c; } // ok\n" +
        "  void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" +
        "  void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 4)\n" +
        "	void m() { List<X> l = (List<X>)list(); } // unsafe cast\n" +
        "	                       ^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from List to parameterized type List<X> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 5)\n" +
        "	void m0() { List<X> l = list(); } // unsafe conversion\n" +
        "	                        ^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type List to type List<X>. References to generic type List<E> should be parameterized\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 6)\n" +
        "	void m1() { for (X a : list()); } // type mismatch\n" +
        "	                       ^^^^^^\n" +
        "Type mismatch: cannot convert from element type Object to X\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 7)\n" +
        "	void m2() { for (Iterator<X> i = list().iterator(); i.hasNext();); }  // unsafe conversion\n" +
        "	                                 ^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type Iterator to type Iterator<X>. References to generic type Iterator<E> should be parameterized\n" +
        "----------\n" +
        "5. WARNING in X.java (at line 8)\n" +
        "	void m3() { Collection c = null; List l = (List<X>)c; } // unsafe cast\n" +
        "	                                          ^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from Collection to parameterized type List<X> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "6. ERROR in X.java (at line 10)\n" +
        "	void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" +
        "	                                ^\n" +
        "Type mismatch: cannot convert from Collection<X> to List\n" +
        "----------\n" +
        "7. WARNING in X.java (at line 10)\n" +
        "	void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" +
        "	                                    ^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from List to parameterized type Collection<X> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "8. ERROR in X.java (at line 11)\n" +
        "	void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" +
        "	                                ^\n" +
        "Type mismatch: cannot convert from Collection<?> to List\n" +
        "----------\n" +
        "9. WARNING in X.java (at line 11)\n" +
        "	void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" +
        "	                                    ^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type Collection<?> for expression of type List\n" +
        "----------\n");
  }

  // conversion from raw to X<?> is safe (no unsafe warning)
  public void test228() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        " import java.util.*;\n" +
        " public class X {\n" +
        " 	List<?> list = new ArrayList();\n" +
        " }\n",
    },
        "");
  }

  // can resolve member through type variable
  public void test229() {
    this.runConformTest(
        new String[] {
        "X.java",
        " public class X <T extends XC> {\n" +
        " 	T.MXC f;\n" +
        " 	public static void main(String[] args) {\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        " }\n" +
        "\n" +
        " class XC {\n" +
        " 	class MXC {}\n" +
        " }\n",
    },
        "SUCCESS");
  }

  // 69375 - equivalence of wildcards
  public void test230() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "\n" +
        "public class X {\n" +
        "	void foo() {\n" +
        "		List<? extends Integer> li= null;\n" +
        "		List<? extends Number> ln= null;\n" +
        "		ln = li;\n" +
        "		li= ln;\n" +
        "	}\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	li= ln;\n" +
        "	    ^^\n" +
        "Type mismatch: cannot convert from List<? extends Number> to List<? extends Integer>\n" +
        "----------\n");
  }

  // 69170 - variation
  public void test231() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T>{\n" +
        "	 Object x1= new X<?>[0];	 \n" +
        "	 Object x2= new X<? super String>[0];	 \n" +
        "	 Object x3= new X<? extends Thread>[0];	 \n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	Object x2= new X<? super String>[0];	 \n" +
        "	           ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Cannot create a generic array of X<? super String>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 4)\n" +
        "	Object x3= new X<? extends Thread>[0];	 \n" +
        "	           ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Cannot create a generic array of X<? extends Thread>\n" +
        "----------\n");
  }

  // 69542 - generic cast should be less strict
  public void test232() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    private T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(\"BAD\");\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Object someVal = cont.getVal(); // no cast \n" +
        "	    System.out.println(cont.getVal()); // no cast \n" +
        "	}\n" +
        "}\n",
    },
        "BAD");
  }

  // 69542 - variation
  public void test233() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    private T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(new Long(0));\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Number someVal = cont.getVal();// only cast to Number \n" +
        "	    System.out.println(\"SUCCESS\"); \n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69542 - variation
  public void test234() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(\"BAD\");\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Object someVal = cont.val; // no cast \n" +
        "	    System.out.println(cont.val); // no cast \n" +
        "	}\n" +
        "}\n",
    },
        "BAD");
  }

  // 69542 - variation
  public void test235() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(new Long(0));\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Number someVal = cont.val;// only cast to Number \n" +
        "	    System.out.println(\"SUCCESS\"); \n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69542 - variation
  public void test236() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(\"BAD\");\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Object someVal = (cont).val; // no cast \n" +
        "	    System.out.println((cont).val); // no cast \n" +
        "	}\n" +
        "}\n",
    },
        "BAD");
  }

  // 69542 - variation
  public void test237() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(new Long(0));\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont=new Container<Integer>();\n" +
        "	    cont.setVal(new Integer(0));\n" +
        "	    badMethod(cont);\n" +
        "	    Number someVal = (cont).val;// only cast to Number \n" +
        "	    System.out.println(\"SUCCESS\"); \n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69542 - variation
  public void test238() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "		Container<T> next;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(\"BAD\");\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont = new Container<Integer>();\n" +
        "		cont.next = new Container<Integer>();\n" +
        "	    cont.next.setVal(new Integer(0));\n" +
        "	    badMethod(cont.next);\n" +
        "	    Object someVal = cont.next.val; // no cast \n" +
        "	    System.out.println(cont.next.val); // no cast \n" +
        "	}\n" +
        "}\n",
    },
        "BAD");
  }

  // 69542 - variation
  public void test239() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        " 	static class Container<T>{\n" +
        "	    public T val;\n" +
        "		Container<T> next;\n" +
        "	    public T getVal() {\n" +
        "	        return val;\n" +
        "	    }\n" +
        "	    public void setVal(T val) {\n" +
        "	        this.val = val;\n" +
        "	    }\n" +
        "	}\n" +
        "	public static void badMethod(Container<?> param){\n" +
        "	    Container x=param;\n" +
        "	    x.setVal(new Long(0));\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	    Container<Integer> cont = new Container<Integer>();\n" +
        "		cont.next = new Container<Integer>();\n" +
        "	    cont.next.setVal(new Integer(0));\n" +
        "	    badMethod(cont.next);\n" +
        "	    Number someVal = cont.next.val;// only cast to Number \n" +
        "	    System.out.println(\"SUCCESS\"); \n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69713 NPE due to length pseudo field
  public void test240() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    String[] elements = null;\n" +
        "	\n" +
        "    public X() {\n" +
        "        String s = \"a, b, c, d\";\n" +
        "        elements = s.split(\",\");\n" +
        "        if(elements.length = 3) {\n" +
        "        }\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 7)\n" +
        "	if(elements.length = 3) {\n" +
        "	   ^^^^^^^^^^^^^^^^^^^\n" +
        "Type mismatch: cannot convert from int to boolean\n" +
        "----------\n");
  }

  // 69776 - missing checkcast on cast operation
  public void test241() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.HashMap;\n" +
        "import java.util.Map;\n" +
        "public class X {\n" +
        "    private static final Map<String, Class> classes = new HashMap<String, Class>();\n" +
        "    public static void main(String[] args) throws Exception {\n" +
        "    	classes.put(\"test\", X.class);\n" +
        "        final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" +
        "        Object o = clazz.newInstance();\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }

  // 69776 - variation
  public void test242() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.HashMap;\n" +
        "import java.util.Map;\n" +
        "public class X {\n" +
        "    private static final Map<String, Class> classes = new HashMap<String, Class>();\n" +
        "    public static void main(String[] args) throws Exception {\n" +
        "    	classes.put(\"test\", X.class);\n" +
        "        final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" +
        "        Object o = clazz.newInstance();\n" +
        "    }\n" +
        "}\n",
    },
        "----------\n" +
        "1. WARNING in X.java (at line 7)\n" +
        "	final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" +
        "	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type Class<? extends Object> for expression of type Class\n" +
        "----------\n");
  }

  public void test243() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public X foo() {\n" +
        "        System.out.println(\"Did NOT add bridge method\");\n" +
        "        return this;\n" +
        "    }\n" +
        "    public static void main(String[] args) throws Exception {\n" +
        "        X x = new A();\n" +
        "        x.foo();\n" +
        "        System.out.print(\" + \");\n" +
        "        I i = new A();\n" +
        "        i.foo();\n" +
        "    }\n" +
        "}\n" +
        "interface I {\n" +
        "    public I foo();\n" +
        "}\n" +
        "class A extends X implements I {\n" +
        "    public A foo() {\n" +
        "        System.out.print(\"Added bridge method\");\n" +
        "        return this;\n" +
        "    }\n" +
        "}\n"
    },
        "Added bridge method + Added bridge method");
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    public X foo() { return this; }\n" +
        "    public static void main(String[] args) throws Exception {\n" +
        "        System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
        "SubTypes.java",
        "class A extends X {\n" +
        "    public A foo() { return this; }\n" +
        "}\n" +
        "class B extends X {\n" +
        "    public X foo() { return new X(); }\n" +
        "    public B foo() { return this; }\n" +
        "}\n" +
        "class C extends A {\n" +
        "    public X foo() { return new X(); }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in SubTypes.java (at line 5)\n" +
        "	public X foo() { return new X(); }\n" +
        "	         ^^^^^\n" +
        "Duplicate method foo() in type B\n" +
        "----------\n" +
        "2. ERROR in SubTypes.java (at line 6)\n" +
        "	public B foo() { return this; }\n" +
        "	         ^^^^^\n" +
        "Duplicate method foo() in type B\n" +
        "----------\n" +
        "3. ERROR in SubTypes.java (at line 9)\n" +
        "	public X foo() { return new X(); }\n" +
        "	         ^^^^^\n" +
        "The return type is incompatible with A.foo()\n" +
        "----------\n");
  }

  // generic method of raw type
  public void test244() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> { \n" +
        "	<G> T foo(G g) {\n" +
        "		return null;\n" +
        "	}\n" +
        "	\n" +
        "	public static void main(String[] args) {\n" +
        "		X rx = new X();\n" +
        "		rx.foo(\"hello\");\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 8)\n" +
        "	rx.foo(\"hello\");\n" +
        "	^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the method foo(G) of raw type X. References to generic type X<T> should be parameterized\n" +
        "----------\n");
  }

  // generic method of raw type
  public void test245() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <T> { \n" +
        "	<G> T foo(G g) {\n" +
        "		return null;\n" +
        "	}\n" +
        "	\n" +
        "	public static void main(String[] args) {\n" +
        "		X rx = new X();\n" +
        "		rx.<String>foo(\"hello\");\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	rx.<String>foo(\"hello\");\n" +
        "	           ^^^\n" +
        "The method foo(Object) of raw type X is no more generic; it cannot be parameterized with arguments <String>\n" +
        "----------\n");
  }

  // 69320 parameterized type compatibility
  public void test246() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "    class MX<E> {\n" +
        "    }\n" +
        "    void foo() {\n" +
        "      MX<Class<? extends Object>> mx2 = new MX<Class>();\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	MX<Class<? extends Object>> mx2 = new MX<Class>();\n" +
        "	                            ^^^\n" +
        "Type mismatch: cannot convert from X.MX<Class> to X.MX<Class<? extends Object>>\n" +
        "----------\n");
  }

  // 69320 variation
  public void test247() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "    void foo() {\n" +
        "      MX<Class<? extends Object>> mx2 = new MX<Class>(); // wrong\n" +
        "      MX<Class<? extends Object>> mx3 = new MX<Class<? extends String>>(); // wrong\n" +
        "      MX<Class<? extends Object>> mx4 = new MX<Class<String>>(); // wrong\n" +
        "      MX<? extends Class> mx5 = new MX<Class>(); // ok\n" +
        "      MX<? super Class> mx6 = new MX<Class>(); // ok\n" +
        "      MX<Class<? extends Class>> mx7 = new MX<Class<Class>>(); // wrong\n" +
        "      MX<MX<? extends Class>> mx8 = new MX<MX<Class>>(); // wrong\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "class MX<E> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	MX<Class<? extends Object>> mx2 = new MX<Class>(); // wrong\n" +
        "	                            ^^^\n" +
        "Type mismatch: cannot convert from MX<Class> to MX<Class<? extends Object>>\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 4)\n" +
        "	MX<Class<? extends Object>> mx3 = new MX<Class<? extends String>>(); // wrong\n" +
        "	                            ^^^\n" +
        "Type mismatch: cannot convert from MX<Class<? extends String>> to MX<Class<? extends Object>>\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 5)\n" +
        "	MX<Class<? extends Object>> mx4 = new MX<Class<String>>(); // wrong\n" +
        "	                            ^^^\n" +
        "Type mismatch: cannot convert from MX<Class<String>> to MX<Class<? extends Object>>\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 8)\n" +
        "	MX<Class<? extends Class>> mx7 = new MX<Class<Class>>(); // wrong\n" +
        "	                           ^^^\n" +
        "Type mismatch: cannot convert from MX<Class<Class>> to MX<Class<? extends Class>>\n" +
        "----------\n" +
        "5. ERROR in X.java (at line 9)\n" +
        "	MX<MX<? extends Class>> mx8 = new MX<MX<Class>>(); // wrong\n" +
        "	                        ^^^\n" +
        "Type mismatch: cannot convert from MX<MX<Class>> to MX<MX<? extends Class>>\n" +
        "----------\n");
  }

  // 70247 check type variable is bound during super type resolution
  public void test248() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X<T> extends Vector<? super X<int[]>>{}\n"},
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	public class X<T> extends Vector<? super X<int[]>>{}\n" +
        "	                          ^^^^^^\n" +
        "The type X cannot extend or implement Vector<? super X<int[]>>. A supertype may not specify any wildcard\n" +
        "----------\n");
  }

  // 70247 variation
  public void test249() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X<T> implements List<? super X<int[]>>{}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	public class X<T> implements List<? super X<int[]>>{}\n" +
        "	                             ^^^^\n" +
        "The type X cannot extend or implement List<? super X<int[]>>. A supertype may not specify any wildcard\n" +
        "----------\n");
  }

  // 70295 Class<? extends Object> is compatible with Class<?>
  public void test250() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "    void test(Object o) {\n" +
        "        X.class.isAssignableFrom(o.getClass());\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // 69800 '? extends Object' is not compatible with A
  public void test251() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X { \n" +
        "    static class A {\n" +
        "    }\n" +
        "    A test() throws Exception {\n" +
        "        Class<? extends Object> clazz = null;\n" +
        "        return clazz.newInstance(); // ? extends Object\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	return clazz.newInstance(); // ? extends Object\n" +
        "	       ^^^^^^^^^^^^^^^^^^^\n" +
        "Type mismatch: cannot convert from ? extends Object to X.A\n" +
        "----------\n");
  }

  // 69799 NPE in foreach checkcast
  public void test252() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X {\n" +
        "	public static void main(String[] args) {\n" +
        "		Set<X> channel = channels.get(0);\n" +
        "	    for (Iterator<X> iter = channel.iterator(); iter.hasNext();) {\n" +
        "	        Set<X> element;\n" +
        "	        element = (Set<X>) iter.next();\n" +
        "		}\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	Set<X> channel = channels.get(0);\n" +
        "	                 ^^^^^^^^\n" +
        "channels cannot be resolved\n" +
        "----------\n");
  }

  // 70243 unsafe cast when wildcards
  public void test253() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        List<Integer> li= new ArrayList<Integer>();\n" +
        "        List<? extends Number> ls= li;       \n" +
        "        List<Number> x2= (List<Number>)ls;//unsafe\n" +
        "        x2.add(new Float(1.0));\n" +
        "        \n" +
        "        Integer i= li.get(0);//ClassCastException!\n" +
        "        \n" +
        "        List<Number> ls2 = (List<? extends Number>)ls;\n" +
        "        List<? extends Number> ls3 = (List<? extends Number>) li;\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 6)\n" +
        "	List<Number> x2= (List<Number>)ls;//unsafe\n" +
        "	                 ^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from List<? extends Number> to parameterized type List<Number> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 11)\n" +
        "	List<Number> ls2 = (List<? extends Number>)ls;\n" +
        "	             ^^^\n" +
        "Type mismatch: cannot convert from List<? extends Number> to List<Number>\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 11)\n" +
        "	List<Number> ls2 = (List<? extends Number>)ls;\n" +
        "	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type List<? extends Number> for expression of type List<? extends Number>\n" +
        "----------\n" +
        "4. WARNING in X.java (at line 12)\n" +
        "	List<? extends Number> ls3 = (List<? extends Number>) li;\n" +
        "	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unnecessary cast to type List<? extends Number> for expression of type List<Integer>\n" +
        "----------\n");
  }

  // 70053 missing checkcast in string concatenation
  public void test254() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.util.*;\n" +
        "public class X {\n" +
        " public static void main(String[] args) {\n" +
        "  X x = new X();\n" +
        "  System.out.print(\"S\" + x.a() + \"U\" + x.b().get(0) + \"C\" + x.a() + \"C\");\n" +
        "  System.out.println(new StringBuilder(\"E\").append(x.a()).append(\"S\").append(x.b().get(0)).append(\"S\").append(x.a()).append(\"!\"));  \n" +
        " }\n" +
        " String a() { return \"\"; }\n" +
        " List<String> b() { \n" +
        "  ArrayList<String> als = new ArrayList<String>(1);\n" +
        "  als.add(a());\n" +
        "  return als;\n" +
        " }\n" +
        "}\n"
    },
        "SUCCESS!");
  }

  // 69351 generic type cannot extend Throwable
  public void test255() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T, U> extends Throwable {\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 1)\n" +
        "	public class X<T, U> extends Throwable {\n" +
        "	             ^\n" +
        "The serializable class X does not declare a static final serialVersionUID field of type long\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 1)\n" +
        "	public class X<T, U> extends Throwable {\n" +
        "	                             ^^^^^^^^^\n" +
        "The generic class X<T,U> may not subclass java.lang.Throwable\n" +
        "----------\n");
  }

  // 70616 - reference to binary Enum
  public void test256() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	\n" +
        "	public static void main(String[] args) {\n" +
        "\n" +
        "		Enum<X> ex = null;\n" +
        "		String s = ex.name();\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	Enum<X> ex = null;\n" +
        "	     ^\n" +
        "Bound mismatch: The type X is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type Enum<E>\n" +
        "----------\n");
  }

  // 70618 - reference to variable allowed in parameterized super type
  public void test257() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "    public abstract class M extends java.util.AbstractList<T> {}\n" +
        "}\n" +
        "class Y<T> extends T {}\n" +
        "class Z<T> {\n" +
        "    class M extends T {}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	class Y<T> extends T {}\n" +
        "	                   ^\n" +
        "Cannot refer to the type parameter T as a supertype\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	class M extends T {}\n" +
        "	                ^\n" +
        "Cannot refer to the type parameter T as a supertype\n" +
        "----------\n");
  }

  public void test258() {
    this.runConformTest(
        new String[] {
        "X.java",
        "abstract class X<K,V> implements java.util.Map<K,V> {\n" +
        "    static abstract class M<K,V> implements Entry<K,V> {}\n" +
        "}\n"
    },
        "");
  }

  // 70767 - NPE compiling code with explicit constructor invocation
  public void test259() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<E> {\n" +
        "	\n" +
        "	<E> X(E e) {\n" +
        "		<E> this();\n" +
        "	}\n" +
        "	\n" +
        "	<E> X() {\n" +
        "	}\n" +
        "}\n"
    },
        "");
  }

  public void test260() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E> {\n" +
        "	class MX <F> {\n" +
        "	}\n" +
        "}\n" +
        "\n" +
        "class XC<G> extends X<G> {\n" +
        "	class MXC<H> extends MX<H> {\n" +
        "	}\n" +
        "}\n"
    },
        "");
  }

  public void test261() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E> {\n" +
        "	void foo(){\n" +
        "		X<Integer> xi = (X<Integer>) new X<String>();\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	X<Integer> xi = (X<Integer>) new X<String>();\n" +
        "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Cannot cast from X<String> to X<Integer>\n" +
        "----------\n");
  }

  public void test262() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E,F> {\n" +
        "	void foo(){\n" +
        "		X<E,String> xe = (X<E,String>) new X<String,String>();\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	X<E,String> xe = (X<E,String>) new X<String,String>();\n" +
        "	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from X<String,String> to parameterized type X<E,String> will not check conformance of type arguments at runtime\n" +
        "----------\n");
  }

  public void test263() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E,F> {\n" +
        "	void foo(){\n" +
        "		XC<E,String> xe = (XC<E,String>) new X<String,String>();\n" +
        "	}\n" +
        "}\n" +
        "class XC<G,H> extends X<G,H> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	XC<E,String> xe = (XC<E,String>) new X<String,String>();\n" +
        "	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from X<String,String> to parameterized type XC<E,String> will not check conformance of type arguments at runtime\n" +
        "----------\n");
  }

  public void test264() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E,F> {\n" +
        "	void foo(){\n" +
        "		XC<E,String> xe = (XC<E,String>) new X<String,Integer>();\n" +
        "	}\n" +
        "}\n" +
        "class XC<G,H> extends X<G,H> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	XC<E,String> xe = (XC<E,String>) new X<String,Integer>();\n" +
        "	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Cannot cast from X<String,Integer> to XC<E,String>\n" +
        "----------\n");
  }

  public void test265() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X <E> {\n" +
        "	<U> void foo(){\n" +
        "			XC<U> xcu = (XC<U>) new X<E>();\n" +
        "			XC<U> xcu1 = (XC<?>) new X<E>();			\n" +
        "			XC<?> xcu2 = (XC<? extends X>) new X<E>();						\n" +
        "	}\n" +
        "}\n" +
        "class XC<G> extends X<G> {\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 3)\n" +
        "	XC<U> xcu = (XC<U>) new X<E>();\n" +
        "	            ^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from X<E> to parameterized type XC<U> will not check conformance of type arguments at runtime\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 4)\n" +
        "	XC<U> xcu1 = (XC<?>) new X<E>();			\n" +
        "	      ^^^^\n" +
        "Type mismatch: cannot convert from XC<?> to XC<U>\n" +
        "----------\n" +
        "3. WARNING in X.java (at line 5)\n" +
        "	XC<?> xcu2 = (XC<? extends X>) new X<E>();						\n" +
        "	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: The cast from X<E> to parameterized type XC<? extends X> will not check conformance of type arguments at runtime\n" +
        "----------\n");
  }

  public void test266() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <E> {\n" +
        "	void bar() {\n" +
        "		X<? extends E> xe = new X<E>();\n" +
        "	}\n" +
        "}\n"
    },
        "");
  }

  public void test267() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X <T> {\n" +
        "	static void foo(X<?> xany) { \n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		foo(new X<Object[]>());\n" +
        "	}\n" +
        "}\n"
    },
        "SUCCESS");
  }

  public void test268() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "public class X <T> {\n" +
        "	X[] foo() {\n" +
        "		ArrayList<X> list = new ArrayList();\n" +
        "		return list.toArray(new X[list.size()]);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 4)\n" +
        "	ArrayList<X> list = new ArrayList();\n" +
        "	                    ^^^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not convert expression of raw type ArrayList to type ArrayList<X>. References to generic type ArrayList<E> should be parameterized\n" +
        "----------\n");
  }

  // 70975 - test compilation against binary generic method
  public void test269() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> {\n" +
        "\n" +
        "	<U> U[] bar(U[] u)  { \n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "		return null; }\n" +
        "\n" +
        "	static String[] foo() {\n" +
        "		X<String> xs = new X<String>();\n" +
        "		return xs.bar(new String[0]);\n" +
        "	}\n" +
        "	public static void main(String[] args) {\n" +
        "		foo();\n" +
        "	}\n" +
        "}\n",
    },
        "SUCCESS");

    this.runConformTest(
        new String[] {
        "Y.java",
        "public class Y {\n" +
        "    public static void main(String [] args) {\n" +
        "		X<String> xs = new X<String>();\n" +
        "		String[] s = xs.bar(new String[0]);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS",
        null,
        false, // do not flush output
        null);
  }

  // 70969 - lub(List<String>, List<Object>) --> List<? extends Object>
  public void test270() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.ArrayList;\n" +
        "\n" +
        "public class X {\n" +
        "    public void test(boolean param) {\n" +
        "        ArrayList<?> ls = (param) \n" +
        "        		? new ArrayList<String>()\n" +
        "        		: new ArrayList<Object>();\n" +
        "        		\n" +
        "        X x = param ? new XY() : new XZ();\n" +
        "        XY y = (XY) new XZ();\n" +
        "    }\n" +
        "}\n" +
        "class XY extends X {}\n" +
        "class XZ extends X {}\n"
    },

        "----------\n" +
        "1. ERROR in X.java (at line 10)\n" +
        "	XY y = (XY) new XZ();\n" +
        "	       ^^^^^^^^^^^^^\n" +
        "Cannot cast from XZ to XY\n" +
        "----------\n");
  }

  // 71080 - parameter bound <T extends Enum<T>> should be allowed
  public void test271() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T extends Enum<T>> {\n" +
        "}\n"
    },
        "");
  }

  // 71080 - variation
  public void test272() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T extends XY<T>> {\n" +
        "}\n" +
        "\n" +
        "class XY<U extends Cloneable> implements Cloneable {\n" +
        "}\n"
    },
        "");
  }

  // 71080 - variation
  public void test273() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T extends XY<T> & Cloneable> {\n" +
        "}\n" +
        "\n" +
        "class XY<U extends Cloneable> {\n" +
        "}\n"
    },
        "");
  }

  // 71241
  public void test274() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "public class X {\n" +
        "    public List useList(List l) {\n" +
        "        l.add(\"asdf\");\n" +
        "        return l;\n" +
        "    }\n" +
        "}\n" +
        "class Y extends X {\n" +
        "    public List<String> useList(List<String> l) {\n" +
        "        l.add(\"asdf\");\n" +
        "        return l;\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 4)\n" +
        "	l.add(\"asdf\");\n" +
        "	^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the method add(E) of raw type List. References to generic type List<E> should be parameterized\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 9)\n" +
        "	public List<String> useList(List<String> l) {\n" +
        "	                    ^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "name clash : has the same erasure as X.useList(List) but does not override it\n" +
        "----------\n");
  }

  // 71241 - variation
  public void test275() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "public class X {\n" +
        "    public List<String> useList(List<String> l) {\n" +
        "        l.add(\"asdf\");\n" +
        "        return l;\n" +
        "    }\n" +
        "}\n" +
        "class Y extends X {\n" +
        "    public List useList(List l) {\n" +
        "        l.add(\"asdf\");\n" +
        "        return l;\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 9)\n" +
        "	public List useList(List l) {\n" +
        "	            ^^^^^^^^^^^^^^^\n" +
        "return type needs unchecked conversion from X.useList(List<String>)\n" +
        "----------\n" +
        "2. WARNING in X.java (at line 10)\n" +
        "	l.add(\"asdf\");\n" +
        "	^^^^^^^^^^^^^\n" +
        "Unsafe type operation: Should not invoke the method add(E) of raw type List. References to generic type List<E> should be parameterized\n" +
        "----------\n");
  }

  // 71241 - variation
  public void test276() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "public class X {\n" +
        "    public void useList(List l) {}\n" +
        "}\n" +
        "class Y extends X {\n" +
        "    public void useList(List<String> l) {\n" +
        "		super.useList(l);\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	public void useList(List<String> l) {\n" +
        "	            ^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "name clash : has the same erasure as X.useList(List) but does not override it\n" +
        "----------\n");
  }

  // 71241 - variation
  public void test277() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.util.List;\n" +
        "public class X {\n" +
        "    public void useList(List<String> l) {}\n" +
        "}\n" +
        "class Y extends X {\n" +
        "    public void useList(List l) {\n" +
        "		super.useList(l);\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 7)\n" +
        "	super.useList(l);\n" +
        "	              ^\n" +
        "Unsafe type operation: Should not convert expression of raw type List to type List<String>. References to generic type List<E> should be parameterized\n" +
        "----------\n");
  }

  // 71241 - variation
  public void test278() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X<T> implements I {\n" +
        "    public Class<T> getDeclaringClass() { return null; }\n" +
        "}\n" +
        "class Y implements I {\n" +
        "    public Class<?> getDeclaringClass() { return null; }\n" +
        "}\n" +
        "interface I {\n" +
        "	public Class getDeclaringClass();\n" +
        "}\n"
    },
        "");
  }

  // 69901
  public void test279() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X implements ISomething {\n" +
        "    public Class getSomething() { return null; }\n" +
        "}\n" +
        "class Y {}\n" +
        "interface ISomething {\n" +
        "    public Class<? extends Y> getSomething();\n" +
        "}\n"
    },
        "----------\n" +
        "1. WARNING in X.java (at line 2)\n" +
        "	public Class getSomething() { return null; }\n" +
        "	             ^^^^^^^^^^^^^^\n" +
        "return type needs unchecked conversion from ISomething.getSomething()\n" +
        "----------\n");
  }

  // 62822
  public void test280() {
    this.runConformTest(
        new String[] {
        "X.java",
        "interface X<T1 extends Y<T2>, T2 extends Z> {}\n" +
        "interface Y<T3 extends Z> {}\n" +
        "interface Z {}\n"
    },
        "");
  }

  public void test281() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "interface X<T1 extends Y<T2>, T2 extends Z> {}\n" +
        "interface Y<T3 extends Comparable> {}\n" +
        "interface Z {}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	interface X<T1 extends Y<T2>, T2 extends Z> {}\n" +
        "	                         ^^\n" +
        "Bound mismatch: The type T2 is not a valid substitute for the bounded parameter <T3 extends Comparable> of the type Y<T3>\n" +
        "----------\n");
  }

  public void test282() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X extends Y.Member<String> {}\n" +
        "class Y { static class Member<T> {} }\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "p1/X.java",
        "package p1;\n" +
        "public class X extends p1.Y.Member<String> {}\n" +
        "class Y { static class Member<T> {} }\n"
    },
        "");
  }

  public void test283() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public class X extends Y.Missing<String> {}\n" +
        "class Y { static class Member<T> {} }\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public class X extends Y.Missing<String> {}\n" +
        "	                       ^^^^^^^^^\n" +
        "Y.Missing cannot be resolved to a type\n" +
        "----------\n");
    this.runNegativeTest(
        new String[] {
        "p1/X.java",
        "package p1;\n" +
        "public class X extends Y.Missing<String> {}\n" +
        "class Y { static class Member<T> {} }\n"
    },
        "----------\n" +
        "1. ERROR in p1\\X.java (at line 2)\n" +
        "	public class X extends Y.Missing<String> {}\n" +
        "	                       ^^^^^^^^^\n" +
        "Y.Missing cannot be resolved to a type\n" +
        "----------\n");
  }

  // 72083
  public void test284() {
    this.runConformTest(
        new String[] {
        "p1/A.java",
        "package p1;\n" +
        "public class A <T1 extends A<T1, T2>, T2 extends B<T1, T2>> {\n" +
        "    public static void main(String [] args) {\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
        "p1/B.java",
        "package p1;\n" +
        "public class B <T3 extends A<T3, T4>, T4 extends B<T3, T4>> {}\n"
    },
        "SUCCESS");
    this.runConformTest(
        new String[] {
        "p1/A.java",
        "package p1;\n" +
        "public class A <T1 extends B<T1, T2>, T2 extends A<T1, T2>> {\n" +
        "    public static void main(String [] args) {\n" +
        "		System.out.println(\"SUCCESS\");\n" +
        "    }\n" +
        "}\n",
        "p1/B.java",
        "package p1;\n" +
        "public class B <T3 extends B<T3, T4>, T4 extends A<T3, T4>> {}\n"
    },
        "SUCCESS");
  }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73530
	public void test285() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" +
				"public class X {\n" +
				"  public static void main(String[] args){\n" +
				"    Vector<Integer[]> v = new Vector<Integer[]>();\n" +
				"    Integer[] array1 = new Integer[5];\n" +
				"    array1[0] = new Integer(17);\n" +
				"    array1[1] = new Integer(42);\n" +
				"    v.add(array1);\n" +
				"    Integer twentyfour = v.get(0)[1];  // responsible for the crash\n" +
				"    System.out.println(twentyfour);\n" +
				"  }\n" +
				"}"
			},
			"42");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72644
	// TODO (philippe) we need a way to test these 2 methods & find them 'equivalent'... right isEquivalentTo return false
	public void test286() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<T extends Object> T foo(Class<T> c) {return null;}\n" +
				"}\n" +
				"class Y extends X {\n" +
				"	<T extends Object> T foo(Class<T> c) {return null;}\n" +
				"}"
			},
			"");
	}
	public void test287() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"\n" +
				"	public class A <U> {\n" +
				"		\n" +
				"		public class B <V> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		\n" +
				"		X.A.B<String> bs;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	X.A.B<String> bs;\n" +
			"	^^^^^\n" +
			"The member type X.A.B<String> must be qualified with a parameterized type, since it is not static\n" +
			"----------\n");
	}
	public void test288() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"\n" +
				"	public static class A <U> {\n" +
				"		\n" +
				"		public static class B <V> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		\n" +
				"		X.A.B<String> bs;\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	public void test289() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"\n" +
				"	public class A <U> {\n" +
				"		\n" +
				"		public class B <V> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		\n" +
				"		X<String>.A.B<String> bs;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	X<String>.A.B<String> bs;\n" +
			"	^^^^^^^^^^^^^\n" +
			"The member type X<String>.A must be parameterized, since it is qualified with a parameterized type\n" +
			"----------\n");
	}
	public void test290() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"\n" +
				"	public static class A <U> {\n" +
				"		\n" +
				"		public class B <V> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		\n" +
				"		X<String>.A.B<String> bs;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	X<String>.A.B<String> bs;\n" +
			"	^^^^^^^^^^^^^\n" +
			"The member type X<String>.A cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type X<String>\n" +
			"----------\n");
	}
	// ensure bound check deals with supertype (and their enclosing type)
	public void test291() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Iterable>{\n" +
				"	class MX<U extends Iterable> {\n" +
				"	}\n" +
				"}\n" +
				"class SX extends X<Thread>.MX<Object> {\n" +
				"	SX(X x){\n" +
				"		x.super();\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	class SX extends X<Thread>.MX<Object> {\n" +
			"	                   ^^^^^^\n" +
			"Bound mismatch: The type Thread is not a valid substitute for the bounded parameter <T extends Iterable> of the type X<T>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	class SX extends X<Thread>.MX<Object> {\n" +
			"	                              ^^^^^^\n" +
			"Bound mismatch: The type Object is not a valid substitute for the bounded parameter <U extends Iterable> of the type X<T>.MX<U>\n" +
			"----------\n");
	}
	public void test292() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"	class Y {\n" +
				"		class Z<U> {\n" +
				"		}\n" +
				"	}\n" +
				"    public static void main(String[] args) {\n" +
				"		X<Object>.Y.Z<Object> zo;\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73837
	public void test293() {
		this.runConformTest(
			new String[] {
				"B.java", //---------------------------
				"public class B<X>{\n"+
				"    public B(X str,D dValue){}\n"+
				"    public static void main(String [] args) {\n" +
				"        System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"	,
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
			"SUCCESS");

		this.runConformTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<Z,Y> {\n" +
				"    public B<Z> test(Z zValue,D<Y> yValue){ return new B<Z>(zValue,yValue); }\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73837 variation
	public void test294() {
		this.runConformTest(
			new String[] {
				"B.java", //---------------------------
				"public class B<X>{\n"+
				"    public B(X str, B<D> dValue){}\n"+
				"    public static void main(String [] args) {\n" +
				"        System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"	,
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
			"SUCCESS");

		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<Z,Y> {\n" +
				"    public B<Z> test(Z zValue,B<D<Y>> yValue){ return new B<Z>(zValue,yValue); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in C.java (at line 2)\r\n" +
			"	public B<Z> test(Z zValue,B<D<Y>> yValue){ return new B<Z>(zValue,yValue); }\r\n" +
			"	                                                  ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The constructor B<Z>(Z, B<D<Y>>) is undefined\n" +
			"----------\n",
			null,
			false, // do not flush output
			null);
	}
	// non-static method #start() gets its type substituted when accessed through raw type
	public void test295() {
		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<U> {\n" +
				"\n" +
				"	void bar() {\n" +
				"		new B().start().get(new B().start()).get(new B().start());\n" +
				"	}\n" +
				"}\n",
				"B.java", //---------------------------
				"public class B<X>{\n" +
				"	X get(B<X> bx) { return null; }\n" +
				"	B<B<D>> start() { return null; }\n" +
				"}",
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
			"----------\n" +
			"1. WARNING in C.java (at line 4)\n" +
			"	new B().start().get(new B().start()).get(new B().start());\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method get(B) belongs to the raw type B. References to generic type B<X> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in C.java (at line 4)\n" +
			"	new B().start().get(new B().start()).get(new B().start());\n" +
			"	                                     ^^^\n" +
			"The method get(B) is undefined for the type Object\n" +
			"----------\n");
	}
	// static method #start() gets its type does not get substituted when accessed through raw type
	public void test296() {
		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<U> {\n" +
				"\n" +
				"	void bar() {\n" +
				"		new B().start().get(new B().start()).get(new B().start());\n" +
				"	}\n" +
				"}\n",
				"B.java", //---------------------------
				"public class B<X>{\n" +
				"	X get(B<X> bx) { return null; }\n" +
				"	static B<B<D>> start() { return null; }\n" +
				"}",
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
		"----------\n" +
		"1. WARNING in C.java (at line 4)\n" +
		"	new B().start().get(new B().start()).get(new B().start());\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"The static method start() from the type B should be accessed in a static way\n" +
		"----------\n" +
		"2. WARNING in C.java (at line 4)\n" +
		"	new B().start().get(new B().start()).get(new B().start());\n" +
		"	                    ^^^^^^^^^^^^^^^\n" +
		"The static method start() from the type B should be accessed in a static way\n" +
		"----------\n" +
		"3. ERROR in C.java (at line 4)\n" +
		"	new B().start().get(new B().start()).get(new B().start());\n" +
		"	                                     ^^^\n" +
		"The method get(B<D>) in the type B<D> is not applicable for the arguments (B<B<D>>)\n" +
		"----------\n" +
		"4. WARNING in C.java (at line 4)\n" +
		"	new B().start().get(new B().start()).get(new B().start());\n" +
		"	                                         ^^^^^^^^^^^^^^^\n" +
		"The static method start() from the type B should be accessed in a static way\n" +
		"----------\n");
	}
	public void test297() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.HashMap;\n" +
				"import java.util.Iterator;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public class X {\n" +
				"		 public static void main(String[] args) {\n" +
				"		 		 Map<String, String> map = new HashMap<String, String>();\n" +
				"		 		 \n" +
				"		 		 map.put(\"foo\", \"bar\");\n" +
				"		 		 \n" +
				"		 		 // Error reported on the following line\n" +
				"		 		 Iterator<Map.Entry<String,String>> i = map.entrySet().iterator();\n" +
				"		 		 while (i.hasNext()) {\n" +
				"		 		 		 Map.Entry<String, String> entry = i.next();\n" +
				"		 		 		 System.out.println(entry.getKey() + \", \" + entry.getValue());\n" +
				"		 		 }\n" +
				"		 }\n" +
				"}\n",
			},
			"foo, bar");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72644
	public void test298() {
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.Collection;\n" +
				"import java.util.Map;\n" +
				"import java.util.Set;\n" +
				"\n" +
				"public class X<V> implements Map<String, V> {\n" +
				"   private Map<String, V> backingMap;\n" +
				"   public int size() { return 0; }\n" +
				"   public boolean isEmpty() { return false; }\n" +
				"   public boolean containsKey(Object key) { return false; }\n" +
				"   public boolean containsValue(Object value) { return false; }\n" +
				"   public V get(Object key) { return null; }\n" +
				"   public V put(String key, V value) { return null; }\n" +
				"   public V remove(Object key) { return null; }\n" +
				"   public void clear() { }\n" +
				"   public Set<String> keySet() { return null; }\n" +
				"   public Collection<V> values() { return null; }\n" +
				"   public void putAll(Map<String, ? extends V> t) { }\n" +
				"   public Set<Map.Entry<String, V>> entrySet() {\n" +
				"      return this.backingMap.entrySet();\n" +
				"   }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	public class X<V> implements Map<String, V> {\n" +
			"	             ^\n" +
			"The type X<V> must implement the inherited abstract method Map<String,V>.putAll(Map<? extends String,? extends V>)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 17)\n" +
			"	public void putAll(Map<String, ? extends V> t) { }\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method putAll(Map<String,? extends V>) of type X<V> has the same erasure as putAll(Map<? extends K,? extends V>) of type Map<K,V> but does not override it\n" +
			"----------\n");
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public abstract class X<S, V> implements java.util.Map<Object, Object> {\n" +
				"   public void putAll(java.util.Map<?, ?> t) { }\n" +
				"}\n",
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"public abstract class X<S, V> implements java.util.Map<S, V> {\n" +
				"   public void putAll(java.util.Map<? extends String, ? extends V> t) { }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void putAll(java.util.Map<? extends String, ? extends V> t) { }\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method putAll(Map<? extends String,? extends V>) of type X<S,V> has the same erasure as putAll(Map<? extends K,? extends V>) of type Map<K,V> but does not override it\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74244
	public void test299() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" +
				" public static void main(String argv[]) {\n" +
				" 	System.out.println(Boolean.class == boolean.class ? \"FAILED\" : \"SUCCESS\");\n" +
				" }\n" +
				"}\n",
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74119
	public void test300() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" +
				"    static interface I extends Visitible<I> {\n" +
				"    }\n" +
				"    static interface Visitible<T> {\n" +
				"        void acceptVisitor(Visitor<? super T> visitor);\n" +
				"    }\n" +
				"    static interface Visitor<T> {\n" +
				"        void visit(T t);\n" +
				"    }\n" +
				"    static class C implements I {\n" +
				"        public void acceptVisitor(Visitor<? super I> visitor) {\n" +
				"            visitor.visit(this); // should be ok\n" +
				"            visitor.visit((I) this); // (2) This is a workaround\n" +
				"        }\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74320: check no complaint for unused private method
	public void test301() {
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.List;\n" +
				"public class X {\n" +
				"	public static void reverse(List<?> list) { \n" +
				"		rev(list);\n" +
				"	}\n" +
				"	private static <T> void rev(List<T> list) {\n" +
				"	}\n" +
				"	Zork foo() {\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Zork foo() {\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74514
	public void test302() {
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.ArrayList;\n" +
				"import java.util.Enumeration;\n" +
				"import java.util.Iterator;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	public void test2() {\n" +
				"		List<String> l= new ArrayList<String>();\n" +
				"		for (Iterator<String> i= l.iterator(); i.next(); ) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	import java.util.Enumeration;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import java.util.Enumeration is never used\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	for (Iterator<String> i= l.iterator(); i.next(); ) {\n" +
			"	                                       ^^^^^^^^\n" +
			"Type mismatch: cannot convert from String to boolean\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74544
	public void test303() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public  class X {\n" +
				"  	public static void main(String[] args) {\n" +
				"  		Y<String> ys = new Y<String>();\n" +
				"	    Y<String>.Member m = ys.new Member();\n" +
				"	    m.foo();\n" +
				"  	}    \n" +
				"  }\n" +
				"  class Y<T> {\n" +
				"    class Member {\n" +
				"    	void foo(){\n" +
				"    		System.out.println(\"SUCCESS\");\n" +
				"    	}\n" +
				"    }\n" +
				"  }\n" +
				"\n",
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74592
	public void test304() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T extends Y> {}\n" +
				"class Y extends X {}"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74420
	public void test305() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T> {\n" +
				"  	T x;\n" +
				"  	<U extends T> T foo(U u) { return u; }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74096
	public void test306() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T extends X<T>> {\n" +
				"  	private int i = 1;\n" +
				"  	private int i() {return i;}\n" +
				"  	private static class M { private static int j = 2; }\n" +
				"  	public int foo(T t) { return t.i + t.i() + T.M.j; }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72583
	public void test307() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" +
				"	static <T> T foo(T t1, T t2){ return t1; }\n" +
				"	public static void main(String[] args) {\n" +
				"		IX s = null;\n" +
				"		 foo(new Object(), s);\n" +
				"	}\n" +
				"}\n" +
				"interface IX {}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73696
	public void test308() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
					"public class X<T> {\n" +
					"	class Member {}\n" +
					"}\n",
				"p/Y.java",
				"package p;\n" +
					"public class Y {\n" +
					"	p.X.Member m;\n" +
					"	p.X<String>.Member ms = m;\n" +
					"}\n"
			});
	}
	public void test309() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
					"public class X<T> {\n" +
					"	class Member {\n" +
					"		class Sub {}\n" +
					"	}\n" +
					"}\n",
				"p/Y.java",
				"package p;\n" +
					"public class Y {\n" +
					"	p.X.Member.Sub s;\n" +
					"	p.X<Exception>.Member.Sub es = s;\n" +
					"}\n"
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75156 - should report name clash
	public void test310() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X extends X2 {\n" +
				"	void foo(List<X> lx) { }\n" +
				"}\n" +
				"\n" +
				"abstract class X2 {\n" +
				"	void foo(List<Object> lo) { }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo(List<X> lx) { }\n" +
			"	     ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(List<X>) of type X has the same erasure as foo(List<Object>) of type X2 but does not override it\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75156 variation - should report name clash and ambiguity
	public void test311() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X extends X2 {\n" +
				"	void foo(List<X> lx) { }\n" +
				"	void bar(){\n" +
				"		this.foo((List)null);\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"abstract class X2 {\n" +
				"	void foo(List<Object> lo) { }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo(List<X> lx) { }\n" +
			"	     ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(List<X>) of type X has the same erasure as foo(List<Object>) of type X2 but does not override it\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	this.foo((List)null);\n" +
			"	     ^^^\n" +
			"The method foo(List<X>) is ambiguous for the type X\n" +
			"----------\n");
	}
	// 75156 variation - should report name clash instead of final method override
	public void test312() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X extends X2 {\n" +
				"	void foo(List<X> lx) { }\n" +
				"}\n" +
				"\n" +
				"abstract class X2 {\n" +
				"	final void foo(List<Object> lo) { }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo(List<X> lx) { }\n" +
			"	     ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(List<X>) of type X has the same erasure as foo(List<Object>) of type X2 but does not override it\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73963
	public void test313() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.Inet6Address;\n" +
				"import java.net.InetAddress;\n" +
				"import java.util.AbstractList;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"void takeAbstract(AbstractList<? extends InetAddress> arg) { }\n" +
				"\n" +
				"void takeList(List<? extends InetAddress> arg) { }\n" +
				"\n" +
				"void construct() {\n" +
				"	AbstractList<InetAddress> a= new ArrayList<InetAddress>();\n" +
				"	takeAbstract(a);\n" +
				"	takeAbstract(new ArrayList<InetAddress>()); // a inlined: error 1:\n" +
				"//The method takeAbstract(AbstractList<? extends InetAddress>) in the type A\n" +
				"// is not applicable for the arguments (ArrayList<InetAddress>)\n" +
				"	\n" +
				"	List<InetAddress> l= new ArrayList<InetAddress>();\n" +
				"	takeList(l);\n" +
				"	takeList(new ArrayList<InetAddress>()); // l inlined: ok\n" +
				"	\n" +
				"	ArrayList<? extends InetAddress> aw= new ArrayList<InetAddress>();\n" +
				"	takeAbstract(aw);\n" +
				"	takeAbstract(new ArrayList<Inet6Address>()); // aw inlined: error 2:\n" +
				"//The method takeAbstract(AbstractList<? extends InetAddress>) in the type A\n" +
				"// is not applicable for the arguments (ArrayList<Inet6Address>)\n" +
				"\n" +
				"	takeList(aw);\n" +
				"	takeList(new ArrayList<Inet6Address>()); //aw inlined: ok\n" +
				"}\n" +
				"}"
			},
			"");
	}
	public void test314() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" +
				"	static class XMember<F> {}\n" +
				"\n" +
				"	// with toplevel element type\n" +
				"	void foo() {\n" +
				"		XIter<XElement<E>> iter = fooSet().iterator();\n" +
				"	}\n" +
				"	XSet<XElement<E>> fooSet()	 { return null; }\n" +
				"\n" +
				"	// with member element type\n" +
				"	void bar() {\n" +
				"		XIter<XMember<E>> iter = barSet().iterator();\n" +
				"	}\n" +
				"	XSet<XMember<E>> barSet()	 { return null; }\n" +
				"\n" +
				"	\n" +
				"}\n" +
				"\n" +
				"class XSet<G> {\n" +
				"	XIter<G> iterator() { return null; }\n" +
				"}\n" +
				"class XIter<H> {\n" +
				"}\n" +
				"class XElement<I> {\n" +
				"}\n"
			},
			"");
	}
	public void test315() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" +
				"	static class XMember<F> {}\n" +
				"\n" +
				"	// with member element type\n" +
				"	void bar() {\n" +
				"		XIter<X.XMember<E>> iter = barSet().iterator();\n" +
				"	}\n" +
				"	XSet<XMember<E>> barSet()	 { return null; }\n" +
				"\n" +
				"	\n" +
				"}\n" +
				"\n" +
				"class XSet<G> {\n" +
				"	XIter<G> iterator() { return null; }\n" +
				"}\n" +
				"class XIter<H> {\n" +
				"}\n" +
				"class XElement<I> {\n" +
				"}\n"
			},
			"");
	}
	public void test316() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X <E extends List & Runnable> {\n" +
				"	\n" +
				"	E element() { return null; }\n" +
				"	\n" +
				"	void bar(X<E> xe) {\n" +
				"		xe.element().add(this);\n" +
				"		xe.element().run();\n" +
				"	}\n" +
				"	void foo(X<?> xe) {\n" +
				"		xe.element().add(this);\n" +
				"		xe.element().run();\n" +
				"	}\n" +
				"	void baz(X<? extends XM> xe) {\n" +
				"		xe.element().add(this);\n" +
				"		xe.element().run();\n" +
				"	}\n" +
				"	abstract class XM implements List, Runnable {}\n" +
				"  Zork z;\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	xe.element().add(this);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 16)\n" +
			"	xe.element().add(this);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	public void test317() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X <E extends List & Runnable> {\n" +
				"	\n" +
				"	E element() { return null; }\n" +
				"	\n" +
				"	void foo(X<? extends XI> xe) {\n" +
				"		xe.element().add(this);\n" +
				"		xe.element().run();\n" +
				"	}\n" +
				"	void baz(X<? extends XM> xe) {\n" +
				"		xe.element().add(this);\n" +
				"		xe.element().run();\n" +
				"	}\n" +
				"	interface XI extends Runnable {}\n" +
				"	\n" +
				"	class XM {\n" +
				"		void foo() {}\n" +
				"	}\n" +
				"  Zork z;\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	xe.element().add(this);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 12)\n" +
			"	xe.element().add(this);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"	);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75548
	public void test318() {
		this.runConformTest(
			new String[] {
				"MyCache.java",
				"class Cache<K, V> {\n" +
				"}\n" +
				"\n" +
				"class Index<K, V> {\n" +
				"  public Index(Cache<?, V> parentCache) {\n" +
				"  }\n" +
				"}\n" +
				"\n" +
				"public class MyCache extends Cache<Integer, String> {\n" +
				"  class AnIndex extends Index<String, String> {\n" +
				"    public AnIndex() {\n" +
				"      super(MyCache.this); // <-- Eclipse cannot find the constructor!\n" +
				"    }\n" +
				"  }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76729
	public void test319() {
		this.runConformTest(
			new String[] {
				"test/Test1.java",
				"package test;\n" +
				"\n" +
				"class A<BB extends B>\n" +
				"{}\n" +
				"\n" +
				"class B<AA extends A>\n" +
				"{}\n" +
				"\n" +
				"public interface Test1<C extends B<?>, D extends A<?>>\n" +
				"{}\n" +
				"\n" +
				"class AbstractA extends A<AbstractB> {};\n" +
				"class AbstractB extends B<AbstractA> {};\n" +
				"\n" +
				"class Test2<E extends AbstractB, F extends AbstractA> implements Test1<E, F>\n" +
				"{}"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74032
	public void test320() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"class TestElement extends ArrayList implements Runnable {\n" +
				"  public void run() {\n" +
				"  }\n" +
				"}\n" +
				"public class X <E extends List & Runnable> {\n" +
				"  public X(E element) {\n" +
				"    element.run();\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    new X<TestElement>(new TestElement());\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"  }\n" +
				"}\n"
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74032 - variation with wildcard
	public void test321() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"class TestElement extends ArrayList implements Runnable {\n" +
				"  static final long serialVersionUID = 1l;\n" +
				"  public void run() {\n" +
				"  	// empty\n" +
				"  }\n" +
				"}\n" +
				"public class X <E extends List & Runnable> {\n" +
				"	E element;\n" +
				"  public X(E element) {\n" +
				"  	this.element = element;\n" +
				"    element.run();\n" +
				"  }\n" +
				"  public X(X<?> x) {\n" +
				"    x.element.run();\n" + // should be able to bind to #run()
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    new X<TestElement>(new TestElement());\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"  }\n" +
				"}\n"
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75134
	public void test322() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<A> {\n" +
				"\n" +
				"  A v2;\n" +
				"  X(A a) { v2 = a; }\n" +
				"  \n" +
				"  void func() {\n" +
				"    List<B<A>> l = new ArrayList<B<A>>();\n" +
				"  }\n" +
				"\n" +
				"  class B<T> {\n" +
				"    T v1;\n" +
				"    B(T b) {  v1 = b; }\n" +
				"  }\n" +
				"  \n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76359 - also check warnings for raw conversion
	public void test323() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class G<T> {\n" +
				"	class Member {}\n" +
				"}\n" +
				"public class X {\n" +
				"	G<String> g = new G();\n" +
				"	G<String>.Member gsm = g.new Member();\n" +
				"	G.Member gm = null;\n" +
				"	G<Thread>.Member gtm = gm;\n" +
				"	Zork z;\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	G<String> g = new G();\n" +
			"	              ^^^^^^^\n" +
			"Type safety: The expression of type G needs unchecked conversion to conform to G<String>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	G<Thread>.Member gtm = gm;\n" +
			"	                       ^^\n" +
			"Type safety: The expression of type G.Member needs unchecked conversion to conform to G<Thread>.Member\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72998
	public void test324() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.Set;\n" +
				"\n" +
				"public class X<E> {\n" +
				"   private TreeNode<E> root;\n" +
				"\n" +
				"   public void doSomething() {\n" +
				"      for (TreeNode<E> child : root.children()) {\n" +
				"         // root.children() should work??\n" +
				"      }\n" +
				"   }\n" +
				"\n" +
				"   public void doSomethingElse() {\n" +
				"      for (Iterator<TreeNode<E>> it = root.children().iterator(); it.hasNext();) {\n" +
				"         // this also should work\n" +
				"      }\n" +
				"   }\n" +
				"}\n" +
				"\n" +
				"class TreeNode<E> {\n" +
				"   private Set<TreeNode<E>> children;\n" +
				"   \n" +
				"   public Set<TreeNode<E>> children() {\n" +
				"      return children;\n" +
				"   }\n" +
				"}\n"
			},
			"");
	}
	public void test325() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"	void foo1() {\n" +
				"		X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();\n" +
				"	}\n" +
				"	void foo2() {\n" +
				"		X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();\n" + // allowed per grammar
				"	}\n" +
				"	void foo3() {\n" +
				"		X.Item k = new X.Item();\n" +
				"	}\n" +
				"	static void foo4() {\n" +
				"		X.Item k = new X.Item();\n" +
				"	}\n" +
				"	class Item <E> {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();\n" +
			"	                       ^\n" +
			"Type mismatch: cannot convert from X<Exception>.Item<Thread> to X<String>.Item<Thread>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();\n" +
			"	                                  ^^^^^^^^^^^^^^^^^\n" +
			"Cannot allocate the member type X<Exception>.Item<Thread> using a parameterized compound name; use its simple name and an enclosing instance of type X<Exception>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	X.Item k = new X.Item();\n" +
			"	           ^^^^^^^^^^^^\n" +
			"No enclosing instance of type X<T> is accessible. Must qualify the allocation with an enclosing instance of type X<T> (e.g. x.new A() where x is an instance of X<T>).\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75400
	public void test326() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> implements I<T> {\n" +
				"    public I.A foo() {\n" +
				"        return a;\n" +
				"    }\n" +
				"}    \n" +
				"interface I<T> {\n" +
				"    A a = new A();\n" +
				"    class A {\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class XM<T> {\n" +
				"    A a = new A();\n" +
				"    class A {\n" +
				"    }\n" +
				"}	\n" +
				"\n" +
				"class XMSub<T> extends XM<T> {\n" +
				"    public XM.A foo() {\n" +
				"        return a;\n" +
				"    }\n" +
				"}    \n" +
				"\n"
			},
			"");
	}
	// wildcard captures bound and variable superinterfaces
	public void test327() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends IFoo> {\n" +
				"	\n" +
				"	T element() { return null; }\n" +
				"	void baz(X<? extends IBar> x) {\n" +
				"		x.element().foo();\n" +
				"		x.element().bar();\n" +
				"	}\n" +
				"}\n" +
				"interface IFoo {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n"
			},
			"");
	}
	// wildcard captures bound and variable superinterfaces
	public void test328() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends IFoo> {\n" +
				"	T element;\n" +
				"	X(T element) { \n" +
				"		this.element = element; \n" +
				"	}\n" +
				"	static void baz(X<? extends IBar> x) {\n" +
				"		x.element.foo();\n" +
				"		x.element.bar();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<Foo> x1 = new X<Foo>(new Foo());\n" +
				"		baz(x1);\n" +
				"		X<Bar> x2 = new X<Bar>(new Bar());\n" +
				"		baz(x2);\n" +
				"		X<FooBar> x3 = new X<FooBar>(new FooBar());\n" +
				"		baz(x3);\n" +
				"	}\n" +
				"}\n" +
				"interface IFoo {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n" +
				"class Foo implements IFoo {\n" +
				"	public void foo() {\n" +
				"		System.out.print(\"FOO\");\n" +
				"	}\n" +
				"}\n" +
				"class Bar implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n" +
				"class FooBar extends Foo implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	baz(x1);\n" +
			"	^^^\n" +
			"The method baz(X<? extends IBar>) in the type X<T> is not applicable for the arguments (X<Foo>)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	X<Bar> x2 = new X<Bar>(new Bar());\n" +
			"	  ^^^\n" +
			"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <T extends IFoo> of the type X<T>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	X<Bar> x2 = new X<Bar>(new Bar());\n" +
			"	                  ^^^\n" +
			"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <T extends IFoo> of the type X<T>\n" +
			"----------\n");
	}
	// wildcard captures bound and variable superinterfaces
	public void test329() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends IFoo> {\n" +
				"	T element;\n" +
				"	X(T element) { \n" +
				"		this.element = element; \n" +
				"	}\n" +
				"	static void baz(X<? extends IBar> x) {\n" +
				"		x.element.foo();\n" +
				"		x.element.bar();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<FooBar> x3 = new X<FooBar>(new FooBar());\n" +
				"		baz(x3);\n" +
				"	}\n" +
				"}\n" +
				"interface IFoo {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n" +
				"class FooBar implements IFoo, IBar {\n" +
				"	public void foo() {\n" +
				"		System.out.print(\"FOO\");\n" +
				"	}\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n",
			},
			"FOOBAR");
	}
	// wildcard captures bound superclass and variable superclass
	public void test330() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends Foo> {\n" +
				"	T element;\n" +
				"	X(T element) { \n" +
				"		this.element = element; \n" +
				"	}\n" +
				"	static void baz(X<? extends FooBar> x) {\n" +
				"		x.element.foo();\n" +
				"		x.element.bar();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<FooBar> x3 = new X<FooBar>(new FooBar());\n" +
				"		baz(x3);\n" +
				"	}\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n" +
				"class Foo {\n" +
				"	public void foo() {\n" +
				"		System.out.print(\"FOO\");\n" +
				"	}\n" +
				"}\n" +
				"class FooBar extends Foo implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n",
			},
			"FOOBAR");
	}
	// wildcard captures bound superclass and variable superclass
	public void test331() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends Foo> {\n" +
				"	T element;\n" +
				"	X(T element) { \n" +
				"		this.element = element; \n" +
				"	}\n" +
				"	static void baz(X<? extends IBar> x) {\n" +
				"		x.element.foo();\n" +
				"		x.element.bar();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<FooBar> x3 = new X<FooBar>(new FooBar());\n" +
				"		baz(x3);\n" +
				"	}\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n" +
				"class Foo {\n" +
				"	public void foo() {\n" +
				"		System.out.print(\"FOO\");\n" +
				"	}\n" +
				"}\n" +
				"class FooBar extends Foo implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n",
			},
			"FOOBAR");
	}
	// wildcard considers bound superclass or variable superclass
	public void test332() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Foo> {\n" +
				"	T element;\n" +
				"	X(T element) { \n" +
				"		this.element = element; \n" +
				"	}\n" +
				"	static void baz(X<? extends IBar> x) {\n" + // captures Foo & IBar
				"		x.element.foo();\n" +
				"		x.element.bar();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		baz(new X<FooBar>(new FooBar()));\n" +
				"		baz(new X<Bar>(new Bar()));\n" +
				"	}\n" +
				"}\n" +
				"interface IBar {\n" +
				"	void bar();\n" +
				"}\n" +
				"\n" +
				"class Bar implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class Foo {\n" +
				"	public void foo() {\n" +
				"		System.out.print(\"FOO\");\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class FooBar extends Foo implements IBar {\n" +
				"	public void bar() {\n" +
				"		System.out.print(\"BAR\");\n" +
				"	}\n" +
				"}\n"	,
			},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	baz(new X<Bar>(new Bar()));\n" +
		"	          ^^^\n" +
		"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <T extends Foo> of the type X<T>\n" +
		"----------\n");
	}
	// receveir generic cast matches receiver type (not declaring class)
	public void test333() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	T element;\n" +
				"	X(T element) { this.element = element; }\n" +
				"	T element() { return this.element; }\n" +
				"	public static void main(String[] args) {\n" +
				"		new X<XB>(new XB()).element().afoo();\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class XA {\n" +
				"	void afoo() {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"   }\n" +
				"}\n" +
				"class XB extends XA {\n" +
				"	void bfoo() {}\n" +
				"}\n"	,
			},
		"SUCCESS");
	}
	// check cannot allocate type parameters
	public void test334() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" +
				"  public X() {\n" +
				"  	new E();\n" +
				"  	new E() {\n" +
				"  		void perform() {\n" +
				"  			run();\n" +
				"  		}\n" +
				"  	}.perform();\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new E();\n" +
			"	    ^\n" +
			"Cannot instantiate the type E\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	new E() {\n" +
			"  		void perform() {\n" +
			"  			run();\n" +
			"  		}\n" +
			"  	}.perform();\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot refer to the type parameter E as a supertype\n" +
			"----------\n");
	}
	// variation - check cannot allocate type parameters
	public void test335() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends String> {\n" + // firstBound is class, still cannot be instantiated
				"  public X() {\n" +
				"  	new E();\n" +
				"  	new E() {\n" +
				"  		void perform() {\n" +
				"  			run();\n" +
				"  		}\n" +
				"  	}.perform();\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public class X <E extends String> {\n" +
			"	                          ^^^^^^\n" +
			"The type parameter E should not be bounded by the final type String. Final types cannot be further extended\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new E();\n" +
			"	    ^\n" +
			"Cannot instantiate the type E\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	new E() {\n" +
			"  		void perform() {\n" +
			"  			run();\n" +
			"  		}\n" +
			"  	}.perform();\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot refer to the type parameter E as a supertype\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74669
	public void test336() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IMyInterface {\n" +
				"}\n" +
				"class MyClass <Type> {\n" +
				"\n" +
				"	public <Type> Type myMethod(Object obj, Class type) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static <Type> Type myStaticMethod(Object obj, Class type) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"    public IMyInterface getThis() {\n" +
				"		if (true)\n" +
				"			return new MyClass().myMethod(this, IMyInterface.class);\n" +
				"		else\n" +
				"			return MyClass.myStaticMethod(this, IMyInterface.class);\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 15)\n" +
			"	return new MyClass().myMethod(this, IMyInterface.class);\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from Object to IMyInterface\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77078
	public void test337() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" +
				"public class X {\n" +
				"    public void foo() {\n" +
				"        Vector<Object> objectVector = new Vector<Object>() {\n" +
				"            protected void bar() {\n" +
				"                baz(this); /* ERROR */\n" +
				"            }\n" +
				"        };\n" +
				"        baz(objectVector);\n" +
				"        baz(new Vector<Object>());\n" +
				"    }\n" +
				"    public void baz(Vector<?> mysteryVector) { }\n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77052
	public void test338() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface M<X> { }\n" +
				"\n" +
				"class N<C> { \n" +
				"  M<N<C>> pni = null;\n" +
				"}\n" +
				"\n" +
				"public class X<I> {\n" +
				"  N<I> var1 = null;\n" +
				"\n" +
				"  M<N<I>> var2 = var1.pni;\n" +
				"  // Above line reports as error in Eclipse. \n" +
				"  // \"var2\" is underlined and the error message is: \n" +
				"  // Type mismatch: cannot convert from M<N<C>> to M<N<I>>\n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77052 - variation
	public void test339() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.Set;\n" +
				"\n" +
				"class X <K, V> {\n" +
				"	static class Entry<K, V> {}\n" +
				"	void foo() {\n" +
				"		Iterator<Entry<K,V>> i = entrySet().iterator();\n" +
				"	}\n" +
				"	Set<Entry<K,V>> entrySet()	 { return null; }\n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76313
	public void test340() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	private T data;\n" +
				"	private X(T data){ this.data=data; }\n" +
				"	public static <S> X<S> createObject(S data){\n" +
				"		System.out.println(data);\n" +
				"		return new X<S>(data);\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> res=X.createObject(\"Hallo\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hallo");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77118
	public void test341() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public Object getItem() { return null; }\n" +
				"}\n",
				"Y.java",
				"public class Y extends X {\n" +
				"	public String getItem() { return null; }\n" +
				"}\n",
				"Z.java",
				"public class Z extends X {\n" +
				"	public Comparable getItem() { return null; }\n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77142 - check no raw unsafe warning is issued when accessing generic method from raw type
	public void test342() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"class MyClass<T> {\n" +
				"		 \n" +
				"		 private T thing;\n" +
				"       { Zork z; }\n" +
				"		 \n" +
				"		 public\n" +
				"		 MyClass(T thing) {\n" +
				"		 		 this.thing = thing;\n" +
				"		 }\n" +
				"		 \n" +
				"		 public static <U> MyClass<U>\n" +
				"		 factoryMakeMyClass(U thing)		 {\n" +
				"		 		 return new MyClass<U>(thing);\n" +
				"		 }\n" +
				"}\n" +
				"\n" +
				"class External {\n" +
				"\n" +
				"		 public static <U> MyClass<U>\n" +
				"		 factoryMakeMyClass(U thing)		 {\n" +
				"		 		 return new MyClass<U>(thing);\n" +
				"		 }\n" +
				"}\n" +
				"\n" +
				"public class Test {\n" +
				"		 public static void\n" +
				"		 test()\n" +
				"		 {\n" +
				"		 		 // No problem with this line:\n" +
				"		 		 MyClass<String> foo = External.factoryMakeMyClass(\"hi\");\n" +
				"		 		 \n" +
				"		 		 // This line gives me an error:\n" +
				"		 		 // Type mismatch: cannot convert from MyClass<Object> to MyClass<String>\n" +
				"		 		 MyClass<String> bar = MyClass.factoryMakeMyClass(\"hi\");\n" +
				"		 		 MyClass<String> bar2 = MyClass.<String>factoryMakeMyClass(\"hi\");\n" +
				"		 }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 4)\n" +
			"	{ Zork z; }\n" +
			"	  ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74588
	public void test343() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends Number> {\n" +
				"    T m;\n" +
				"\n" +
				"    class Y<T> {\n" +
				"        void test() {\n" +
				"            new Y<Integer>() {\n" +
				"                void test() {\n" +
				"                    System.out.println(X.this.m);\n" +
				"                }\n" +
				"            }.test();\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"\n",
			},
			"");
	}
	// checking scenario where generic type and method share the same type parameter name
	public void test344() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public abstract class X<T extends Runnable> {\n" +
				"	\n" +
				"	public abstract <T extends Exception> T bar(T t);\n" +
				"\n" +
				"	static void foo(X x) {\n" +
				"		x.<Exception>bar(null);\n" +
				"		\n" +
				"		class R implements Runnable {\n" +
				"			public void run() {\n" +
				"			}\n" +
				"		}\n" +
				"		X<R> xr = new X<R>(){  \n" +
				"			public <T> T bar(T t) { \n" +
				"				return t; \n" +
				"			}\n" +
				"		};\n" +
				"		IOException e = xr.bar(new IOException());\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	x.<Exception>bar(null);\n" +
			"	             ^^^\n" +
			"The method bar(Exception) of raw type X is no longer generic; it cannot be parameterized with arguments <Exception>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	X<R> xr = new X<R>(){  \n" +
			"	              ^^^^^^\n" +
			"The type new X<R>(){} must implement the inherited abstract method X<R>.bar(T)\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74594
	public void test345() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String argv[]) {\n" +
				"       X1<Integer> o1 = new X1<Integer>();\n" +
				"        ((J<Integer>)o1).get();\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class X1<T> implements I<T> {\n" +
				"    public X1 get() {\n" +
				"    	System.out.println(\"SUCCESS\");\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"interface I<T> extends J<T> {\n" +
				"    I get();\n" +
				"}\n" +
				"\n" +
				"interface J<T>  {\n" +
				"    J get();\n" +
				"}",
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74594
	public void test346() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String argv[]) {\n" +
				"       X1<Integer> o1 = new X1<Integer>(new Integer(4));\n" +
				"        System.out.println(o1.get().t);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class X1<T> implements I<T> {\n" +
				"    T t;\n" +
				"    X1(T arg) {\n" +
				"        t = arg;\n" +
				"    }\n" +
				"    public X1 get() {\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"interface I<T> extends J<T> {\n" +
				"    I get();\n" +
				"}\n" +
				"\n" +
				"interface J<T>  {\n" +
				"    J get();\n" +
				"}"
	,
			},
			"4");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74594
	public void test347() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String argv[]) {\n" +
				"        X1<Integer> o = new X1<Integer>(new Integer(4));\n" +
				"        System.out.println(o.get().t);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class X1<T> implements I<T> {\n" +
				"    T t;\n" +
				"    X1(T arg) {\n" +
				"        t = arg;\n" +
				"    }\n" +
				"    public X1 get() {\n" +
				"        return this;\n" +
				"    }\n" +
				"}    \n" +
				"\n" +
				"interface I<T> extends K<T>, L<T> {\n" +
				"    I get();\n" +
				"}\n" +
				"\n" +
				"interface J<T>  {\n" +
				"    J get();\n" +
				"}\n" +
				"\n" +
				"interface K<T> extends J<T> {\n" +
				"}\n" +
				"\n" +
				"interface L<T>  {\n" +
				"    K get();\n" +
				"}",
			},
			"4");
	}
	// checking scenario where generic type and method share the same type parameter name
	public void test348() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public abstract class X<T extends Runnable> {\n" +
				"	public abstract <T extends Exception> T bar(T t);\n" +
				"	static void foo(X x) {\n" +
				"		x.<Exception>bar(null);\n" +
				"		class R implements Runnable {\n" +
				"			public void run() {}\n" +
				"		}\n" +
				"		X<R> xr = new X<R>(){  \n" +
				"			public <T extends Exception> T bar(T t) { return t; }\n" +
				"		};\n" +
				"		IOException e = xr.bar(new IOException());\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	x.<Exception>bar(null);\n" +
			"	             ^^^\n" +
			"The method bar(Exception) of raw type X is no longer generic; it cannot be parameterized with arguments <Exception>\n" +
			"----------\n");
	}
	// test wildcard compatibilities
	public void test349() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	T element;\n" +
				"	static void foo(X<? super Exception> out, X1<? extends Exception> in) {\n" +
				"		out.element = in.element;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n" +
				"class X1<U>{\n" +
				"	U element;\n" +
				"}\n",
			},
			"SUCCESS");
	}
	// test wildcard compatibilities
	public void test350() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	T element;\n" +
				"	static void foo(X<?> out, X1<?> in) {\n" +
				"		out.element = in.element;\n" +
				"	}\n" +
				"}\n" +
				"class X1<U>{\n" +
				"	U element;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	out.element = in.element;\n" +
			"	              ^^^^^^^^^^\n" +
			"Bound mismatch: Cannot assign expression of type ? to wildcard type ?. The wildcard type has no lower bound, and may actually be more restrictive than expression type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75328
	public void test351() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface Intf<D extends Comparable<D>, I extends Comparable<D>> { \n" +
				"  public void f(Intf<D,?> val);\n" +
				"}\n" +
				"\n" +
				"public class X <M extends Comparable<M>, P extends Comparable<M>>  implements Intf<M,P> {\n" +
				"\n" +
				"  public void f(Intf<M,?> val) { } \n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77051
	public void test352() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface C<A> { }\n" +
				"interface PC<X> extends C<X> { } \n" +
				"interface PO<Y>  {  \n" +
				"	  C<Y> proc1();\n" +
				"	  C<? super Y> proc2();\n" +
				"	  C<? extends Y> proc3();\n" +
				"}\n" +
				"abstract class X<Z> implements PO<Z> {\n" +
				"	  public C<Z> proc1() { return result1; }\n" +
				"	  private final PC<Z> result1 = null;\n" +
				"	  public C<? super Z> proc2() { return result2; }\n" +
				"	  private final PC<? super Z> result2 = null;\n" +
				"	  public C<? extends Z> proc3() { return result3; }\n" +
				"	  private final PC<? extends Z> result3 = null;\n" +
				"}\n",
			},
			"");
	}
	public void test353() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T> T foo(Class<T> c) { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<T> T foo(Class<T> c) { return null; }\n" +
				"}"
			},
			"");
	}
	public void test354() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T, S> S foo(Class<T> c) { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<S, T> T foo(Class<S> c) { return null; }\n" +
				"}"
			},
			"");
	}
	public void test355() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T, S> S foo(Class<S> c) { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<S, T> S foo(Class<S> c) { return null; }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	<T, S> S foo(Class<S> c) { return null; }\r\n" +
			"	         ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<S>) of type X has the same erasure as foo(Class<S>) of type Y but does not override it\n" +
			"----------\n");
	}
	public void test356() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T, S> T foo(Class<T> c) { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<T> T foo(Class<T> c) { return null; }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	<T, S> T foo(Class<T> c) { return null; }\r\n" +
			"	         ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<T>) of type X has the same erasure as foo(Class<T>) of type Y but does not override it\n" +
			"----------\n");
	}
	public void test357() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T> T foo(Class<T> c) { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<T, S> T foo(Class<T> c) { return null; }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	<T> T foo(Class<T> c) { return null; }\r\n" +
			"	      ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<T>) of type X has the same erasure as foo(Class<T>) of type Y but does not override it\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76720
	public void test358() {
		this.runConformTest(
			new String[] {
				"MyClass.java",
				"public class MyClass {}\n",
				"A.java",
				"public interface A<M extends MyClass> {}\n",
				"B.java",
				"public interface B<M extends MyClass> extends A<M> {}\n",
				"C.java",
				"public class C implements B<MyClass> {}\n", // compile against sources
				"D.java",
				"public class D implements A<MyClass>{}\n", // compile against sources
			},
			"");
		// compile against generated binaries
		this.runConformTest(
			new String[] {
				"C.java",
				"public class C implements B<MyClass> {}\n",
				"D.java",
				"public class D implements A<MyClass>{}\n",
			},
			"",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76790
	public void test359() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
					"public class X {\n" +
					"    class List1<E> extends LinkedList<E> {};\n" +
					"    public static void main (String[] args) {\n" +
					"        Map<String, List<Integer>> x = new HashMap<String, List<Integer>>();\n" +
					"        Map<String, List1<Integer>> m = new HashMap<String, List1<Integer>>();\n" +
					"    }\n" +
					"}"
			}
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76786
	public void test360() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.Comparable;\n" +
					"public class Test {\n" +
					"    private static final class X<T1, T2> implements Comparable<X<T1, T2>> {\n" +
					"        public int compareTo(X<T1, T2> arg0) { return 0; }\n" +
					"    };\n" +
					"    private static class Y<T1, T2> {};\n" +
					"    private static final class Z<T1, T2> extends Y<T1, T2> implements Comparable<Z<T1, T2>> {\n" +
					"        public int compareTo(Z<T1, T2> arg0) { return 0; }\n" +
					"    };\n" +
					"    public static <T> void doSomething(Comparable<? super T> a, Comparable<? super T> b) {}\n" +
					"    public static <V1, V2> void doSomethingElse(Z<V1, V2> a, Z<V1, V2> b) {\n" +
					"        doSomething(a, b);\n" +
					"    }\n" +
					"    private static final class W { };\n" +
					"    public static void main(String[] args) {\n" +
					"        doSomething(new X<Integer, String>(), new X<Integer, String>());\n" +
					"        doSomething(new Z<Integer, String>(), new Z<Integer, String>());\n" +
					"        doSomethingElse(new Z<Integer, String>(), new Z<Integer, String>());\n" +
					"        doSomethingElse(new Z<W, String>(), new Z<W, String>());\n" +
					"        // The next line won\'t compile.  It\'s the generic<generic which seems\n" +
					"        // to be the problem\n" +
					"        doSomethingElse(new Z<X<W, W>, String>(), new Z<X<W, W>, String>());\n" +
					"    }\n" +
					"}"
			}
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75525
	public void test361() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.util.AbstractSet;\n" +
					"import java.util.Iterator;\n" +
					"import java.util.Map.Entry;\n" +
					"public class Test extends AbstractSet<Entry<String,Integer>> {\n" +
					"	public Iterator<Entry<String, Integer>> iterator() {\n" +
					"		return new Iterator<Entry<String,Integer>>() {\n" +
					"			public boolean hasNext() {return false;}\n" +
					"			public Entry<String, Integer> next() {return null;}\n" +
					"			public void remove() {}	\n" +
					"		};\n" +
					"	}\n" +
					"	public int size() {return 0;}\n" +
					"}"
			}
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72643
	public void test362() {
//		Map customOptions= getCompilerOptions();
//		customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"public class Test {\n" +
					"   public void a() {\n" +
					"      List<String> list1 = new ArrayList<String>();\n" +
					"      List<String> list2 = new ArrayList<String>();\n" +
					"      compare(list1, list2);\n" +
					"   }\n" +
					"   private <E> void compare(List<E> list1, List<E> list2) {\n" +
					"      // do some comparing logic...\n" +
					"   }\n" +
					"}\n" +
					"\n"
			},
		"",
		null,
		true,
		null,
		null, //customOptions,
		null/*no custom requestor*/);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76434
	public void test363() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.Set;\n" +
				"public class X {\n" +
				"  Set<Map.Entry<Integer, ?>> m_values;\n" +
				"  X(Map<Integer, ?> values) {\n" +
				"    m_values = values.entrySet();\n" +
				"  }\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\r\n" +
		"	m_values = values.entrySet();\r\n" +
		"	           ^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Set<Map.Entry<Integer,?>> to Set<Map.Entry<Integer,?>>\n" +
		"----------\n");
	}
	// check param type equivalences
	public void test364() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" +
				"	\n" +
				"	void bar1(MX<Class<? extends String>> mxcs, MX<Class<? extends Object>> mxco) {\n" +
				"		mxco = mxcs;\n" + // wrong
				"	}\n" +
				"	void bar1(Class<? extends String> cs, Class<? extends Object> co) {\n" +
				"		co = cs;\n" + // ok
				"	}\n" +
				"	\n" +
				"}\n" +
				"class MX<E> {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	mxco = mxcs;\n" +
			"	       ^^^^\n" +
			"Type mismatch: cannot convert from MX<Class<? extends String>> to MX<Class<? extends Object>>\n" +
			"----------\n");
	}
	// check param type equivalences
	public void test365() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Runnable> {\n" +
				"	\n" +
				"	class MX <U> {\n" +
				"	}\n" +
				"	\n" +
				"	MX<T> createMX() { return new MX<T>(); }\n" +
				"\n" +
				"	void foo(X<?> x, MX<?> mx) {\n" +
				"		mx = x.createMX();\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	mx = x.createMX();\n" +
			"	     ^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from X<?>.MX<?> to X<T>.MX<?>\n" +
			"----------\n");
	}
	// check param type equivalences
	public void test366() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" +
				"	\n" +
				"	void foo1(MX<Class<? extends Object>> target, MX<Class> value) {\n" +
				"		target= value; // foo1 - wrong\n" +
				"	}\n" +
				"	void foo2(MX<Class<? extends Object>> target, MX<Class<? extends String>> value) {\n" +
				"		target= value; // foo2 - wrong\n" +
				"	}\n" +
				"	void foo3(MX<Class<? extends Object>> target, MX<Class<? extends String>> value) {\n" +
				"		target= value; // foo3 - wrong\n" +
				"	}\n" +
				"	void foo4(MX<Class<? extends Object>> target, MX<Class<String>> value) {\n" +
				"		target= value; // foo4 - wrong\n" +
				"	}\n" +
				"	void foo5(MX<? extends Class> target, MX<Class> value) {\n" +
				"		target= value; // foo5\n" +
				"	}\n" +
				"	void foo6(MX<? super Class> target, MX<Class> value) {\n" +
				"		target= value; // foo6\n" +
				"	}\n" +
				"	void foo7(MX<Class<? extends Class>> target, MX<Class<Class>> value) {\n" +
				"		target= value; // foo7 - wrong\n" +
				"	}\n" +
				"	void foo8(MX<MX<? extends Class>> target, MX<MX<Class>> value) {\n" +
				"		target= value; // foo8 - wrong\n" +
				"	}\n" +
				"	void foo9(MX<? extends Object> target, MX<? extends String> value) {\n" +
				"		target= value; // foo9\n" +
				"	}\n" +
				"	void foo10(MX<? extends String> target, MX<? extends Object> value) {\n" +
				"		target= value; // foo10 - wrong\n" +
				"	}\n" +
				"	void foo11(MX<? super Object> target, MX<? super String> value) {\n" +
				"		target= value; // foo11 - wrong\n" +
				"	}\n" +
				"	void foo12(MX<? super String> target, MX<? super Object> value) {\n" +
				"		target= value; // foo12\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class MX<E> {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	target= value; // foo1 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<Class> to MX<Class<? extends Object>>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	target= value; // foo2 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<Class<? extends String>> to MX<Class<? extends Object>>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	target= value; // foo3 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<Class<? extends String>> to MX<Class<? extends Object>>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 13)\n" +
			"	target= value; // foo4 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<Class<String>> to MX<Class<? extends Object>>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 22)\n" +
			"	target= value; // foo7 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<Class<Class>> to MX<Class<? extends Class>>\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 25)\n" +
			"	target= value; // foo8 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<MX<Class>> to MX<MX<? extends Class>>\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 31)\n" +
			"	target= value; // foo10 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<? extends Object> to MX<? extends String>\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 34)\n" +
			"	target= value; // foo11 - wrong\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from MX<? super String> to MX<? super Object>\n" +
			"----------\n");
	}
	// check param type equivalences
	public void test367() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { \n" +
				"	\n" +
				"	void foo1(MX<? extends MX> target, MX<MX<String>> value) {\n" +
				"		target= value; // foo1\n" +
				"	}\n" +
				"	void foo2(MX<?> target, MX<MX<String>> value) {\n" +
				"		target= value; // foo2\n" +
				"	}\n" +
				"	void foo3(MX<? super MX> target, MX<MX<String>> value) {\n" +
				"		target= value; // foo3\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class MX<E> {\n" +
				"}\n"	,
			},
			"");
	}
	// check param type equivalences
	public void test368() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends Runnable> {\n" +
				"	\n" +
				"	static class MX <U> {\n" +
				"	}\n" +
				"	\n" +
				"	MX<T> createMX() { return new MX<T>(); }\n" +
				"\n" +
				"	void foo(X<?> x, MX<?> mx) {\n" +
				"		mx = x.createMX();\n" +
				"	}\n" +
				"}\n"	,
			},
			"");
	}
	// bound check for Enum<T>
	public void test369() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	<T extends Enum<T>> T foo(T t) { return null; }\n" +
				"}\n",
			},
			"");
	}
	// decoding raw binary type
	public void test370() {
		this.runConformTest(
			new String[] {
				"X.java",
			"import java.lang.annotation.Annotation;\n" +
			"import java.util.Map;\n" +
			"\n" +
			"import sun.reflect.annotation.AnnotationParser;\n" +
			"\n" +
			"public class X {\n" +
			"	{\n" +
			"		Map<Class, Annotation> map = AnnotationParser.parseAnnotations(null, null, null);\n" +
			"	}\n" +
			"}\n",
			},
			"");
	}
	// X<? extends Y> is not compatible with X<Y>
	public void test371() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   	public void foo(XC<Runnable> target, XC<? extends Runnable> value) {\n" +
				"   		target = value;\n" +
				"   	}\n" +
				"}\n" +
				"class XC <E>{\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	target = value;\n" +
			"	         ^^^^^\n" +
			"Type mismatch: cannot convert from XC<? extends Runnable> to XC<Runnable>\n" +
			"----------\n");
	}
	public void test372() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class X <K, V> {\n" +
				"\n" +
				"	void foo(Iterator<Map.Entry<K,V>> iter) {\n" +
				"		new XA.MXA<K,V>(iter.next());\n" +
				"	}\n" +
				"}\n" +
				"class XA <K, V> {\n" +
				"	static class MXA <K, V>  implements Entry<K,V> {\n" +
				"		MXA(Entry<K,V> e) {\n" +
				"		}\n" +
				"		public K getKey() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V getValue() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V setValue(V value) {\n" +
				"			return null;\n" +
				"		}\n" +
				"	}\n" +
				"}\n"	,
			},
			"");
	}
	public void test373() {
		this.runConformTest(
			new String[] {
				"XA.java",
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class XA <K, V> {\n" +
				"	static class MXA <K, V>  implements Entry<K,V> {\n" +
				"		MXA(Entry<K,V> e) {\n" +
				"		}\n" +
				"		public K getKey() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V getValue() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V setValue(V value) {\n" +
				"			return null;\n" +
				"		}\n" +
				"	}\n" +
				"}\n"	,
			},
			"");
		// compile against binaries
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class X <K, V> {\n" +
				"\n" +
				"	void foo(Iterator<Map.Entry<K,V>> iter) {\n" +
				"		new XA.MXA<K,V>(iter.next());\n" +
				"	}\n" +
				"}\n"	,
			},
			"",
			null,
			false,
			null);
	}
	// wildcard with no upper bound uses type variable as upper bound
	public void test374() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" +
				"\n" +
				"	void foo1(X <? extends Exception> target, X<?> value) {\n" +
				"		target = value; // foo1\n" +
				"	}\n" +
				"	void foo2(X <? extends Exception> target, X<? super RuntimeException> value) {\n" +
				"		target = value;  // foo2\n" +
				"	}	\n" +
				"}\n",
			},
			"");
	}
	public void test375() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"\n" +
				"	void foo1(X <? super Exception> target, X<? extends Exception> value) {\n" +
				"		target = value; // foo1\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	target = value; // foo1\n" +
			"	         ^^^^^\n" +
			"Type mismatch: cannot convert from X<? extends Exception> to X<? super Exception>\n" +
			"----------\n");
	}
	public void test376() {
		this.runConformTest(
			new String[] {
				"XA.java",
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class XA <K, V> {\n" +
				"   XA<K,V> self() { return this; } \n" +
				"	static class MXA <K, V>  implements Entry<K,V> {\n" +
				"		MXA(Entry<K,V> e) {\n" +
				"		}\n" +
				"		public K getKey() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V getValue() {\n" +
				"			return null;\n" +
				"		}\n" +
				"		public V setValue(V value) {\n" +
				"			return null;\n" +
				"		}\n" +
				"	}\n" +
				"}\n"	,
			},
			"");
		// compile against binaries
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"import java.util.Map;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class X <K, V> {\n" +
				"\n" +
				"	void foo(Iterator<Map.Entry<K,V>> iter) {\n" +
				"		new XA.MXA<K,V>(iter.next());\n" +
				"	}\n" +
				"}\n"	,
			},
			"",
			null,
			false,
			null);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76601
	public void test377() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
					" public static void main (String[] args) {\n" +
					"  final String val = (args == null||args.length==0 ? \"SUCC\" : args[0]) + \"ESS\";\n" +
					"  class AllegedBoundMismatch<E2 extends SuperI<E2>> {\n" +
					"   String field = val;\n" +
					"  }\n" +
					"  System.out.println(new Object() {\n" +
					"   AllegedBoundMismatch<SubI<Q>> trial = new AllegedBoundMismatch<SubI<Q>>();\n" +
					"  }.trial.field);\n" +
					" }\n" +
					"}\n" +
					"class Q {}\n" +
					"interface SubI<Q> extends SuperI<SubI<Q>> {}\n" +
					"interface SuperI<Q> {}"
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76219
	public void test378() {
		this.runConformTest(
			new String[] {
				"BB.java",
				"interface AA<W, Z extends AA<W, Z>> { \n" +
					" public boolean m(AA<W, ?> that); \n" +
					" public Z z(); \n" +
					" public boolean b(); \n" +
					"}\n" +
					"abstract class BB<U, V extends AA<U, V>> implements AA<U,V> { \n" +
					" public boolean m(AA<U, ?> wht) { return wht.z().b(); } \n" +
					"}\n"}
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=71612
	public void test379() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.util.AbstractSet;\n" +
				"import java.util.Iterator;\n" +
				"public class Test extends AbstractSet<Runnable>{\n" +
				"    public static void main(String[] args) {\n" +
				"        Test t=new Test();\n" +
				"        t.add(null);\n" +
				"    }\n" +
				"    public boolean add(Runnable run) {\n" +
				"        System.out.println(\"success\");\n" +
				"        return true;\n" +
				"    }\n" +
				"    public Iterator<Runnable> iterator() {return null;}\n" +
				"    public int size() {return 0;}\n" +
				"}"
				}
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77327
	public void test380() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.util.List;\n" +
				"public class Test {\n" +
				"	List<? super Number> wsn= null; // Contravariance\n" +
				"	List<? super Integer> wsi= wsn; // should work!\n" +
				"}\n"
				}
		);
	}

	public void test381() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	void foo(Class<? extends String> s) {}\n" +
				"}\n" +
				"class Y {\n" +
				"	void foo(Class<String> s) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(Class<? extends String> s) {}\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<? extends String>) of type X has the same erasure as foo(Class<String>) of type Y but does not override it\n" +
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	void foo(Class<String> s) {}\n" +
				"}\n" +
				"class Y {\n" +
				"	void foo(Class<? extends String> s) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo(Class<String> s) {}\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<String>) of type X has the same erasure as foo(Class<? extends String>) of type Y but does not override it\n" +
			"----------\n");
	}
	public void test382() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y implements I {}\n" +
				"interface I { void foo(Class<? extends String> s); }\n" +
				"class Y { void foo(Class<String> s) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I {}\n" +
			"	             ^\n" +
			"Name clash: The method foo(Class<String>) of type Y has the same erasure as foo(Class<? extends String>) of type I but does not override it\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I {}\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method I.foo(Class<? extends String>)\n" +
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I {}\n" +
				"interface I { void foo(Class<String> s); }\n" +
				"class Y { void foo(Class<? extends String> s) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public abstract class X extends Y implements I {}\n" +
			"	                      ^\n" +
			"Name clash: The method foo(Class<? extends String>) of type Y has the same erasure as foo(Class<String>) of type I but does not override it\n" +
			"----------\n");
	}
	public void test383() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y implements I { public <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n" +
				"class Y { public <T> void foo(Class<T> s) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I { public <T> void foo(Class<T> s) {} }\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method I.foo(Class<T>)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I { public <T> void foo(Class<T> s) {} }\n" +
			"	                                                        ^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(Class<T>) of type X has the same erasure as foo(Class<T>) of type I but does not override it\n" +
			"----------\n");
			/*
			X.java:1: X is not abstract and does not override abstract method <T,S>foo(java.lang.Class<T>) in I
			public class X extends Y implements I { public <T> void foo(Class<T> s) {} }
			       ^
       		*/
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y implements I {}\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n" +
				"class Y { public <T> void foo(Class<T> s) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I {}\n" +
			"	             ^\n" +
			"Name clash: The method foo(Class<T>) of type Y has the same erasure as foo(Class<T>) of type I but does not override it\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public class X extends Y implements I {}\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method I.foo(Class<T>)\n" +
			"----------\n");
			/*
			X.java:1: X is not abstract and does not override abstract method <T,S>foo(java.lang.Class<T>) in I
			public class X extends Y implements I {}
			       ^
			*/
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I {}\n" + // NOTE: X is abstract
				"interface I { <T> void foo(Class<T> s); }\n" +
				"class Y { public <T, S> void foo(Class<T> s) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public abstract class X extends Y implements I {}\n" +
			"	                      ^\n" +
			"Name clash: The method foo(Class<T>) of type Y has the same erasure as foo(Class<T>) of type I but does not override it\n" +
			"----------\n");
			/*
			X.java:1: name clash: <T,S>foo(java.lang.Class<T>) in Y and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
			public abstract class X extends Y implements I {}
			                ^
			 */
	}
	public void test384() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	<T> java.util.List<T> foo3(java.util.List<T> t) { return t; }\n" +
				"	Class<String> foo4() { return null; }\n" +
				"	Class<String>[] foo5() { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	<T> java.util.List<T> foo3(java.util.List<T> t) { return t; }\n" +
				"	Class<? extends String> foo4() { return null; }\n" +
				"	Class<? extends String>[] foo5() { return null; }\n" +
				"}\n"
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y {\n" +
				"	Class<? extends String> foo() { return null; }\n" +
				"	Class<? extends String>[] foo2() { return null; }\n" +
				"}\n" +
				"class Y {\n" +
				"	Class<String> foo() { return null; }\n" +
				"	Class<String>[] foo2() { return null; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	Class<? extends String> foo() { return null; }\r\n" +
			"	                        ^^^^^\n" +
			"The return type is incompatible with Y.foo()\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\r\n" +
			"	Class<? extends String>[] foo2() { return null; }\r\n" +
			"	                          ^^^^^^\n" +
			"The return type is incompatible with Y.foo2()\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77496
	public void test385() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"interface IDoubles { List<Double> getList(); }\n" +
				"class A implements IDoubles {\n" +
				"	public List<String> getList() { return null; }\n" +
				"}\n" +
				"class B {\n" +
				"	 public List<String> getList() { return null; }\n" +
				"}\n" +
				"class C extends B implements IDoubles {\n" +
				"	void use() { List<String> l= getList(); }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	public List<String> getList() { return null; }\n" +
			"	                    ^^^^^^^^^\n" +
			"The return type is incompatible with IDoubles.getList()\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	class C extends B implements IDoubles {\n" +
			"	      ^\n" +
			"The return type is incompatible with IDoubles.getList(), B.getList()\n" +
			"----------\n");
			/*
			X.java:3: A is not abstract and does not override abstract method getList() in IDoubles
			class A implements IDoubles {
			^
			X.java:4: getList() in A cannot implement getList() in IDoubles; attempting to use incompatible return type
			found   : java.util.List<java.lang.String>
			required: java.util.List<java.lang.Double>
				public List<String> getList() { return null; }
			                            ^
			X.java:9: C is not abstract and does not override abstract method getList() in IDoubles
			class C extends B implements IDoubles {
			 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77325
	public void test386() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X <R,U,V, T> {\n" +
					"	private U u;\n" +
					"	private V v;\n" +
					"	public X(U u,V v) { this.u= u; this.v= v; }\n" +
					"	public R getU() { return (R)u; } // Warning\n" +
					"	public R getV() { return (R)v; } // Warning\n" +
					"	Object o;\n" +
					"	public T getT() { return (T)o; } // Warning\n" +
					"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	public R getU() { return (R)u; } // Warning\n" +
			"	                         ^^^^\n" +
			"Type safety: The cast from U to R is actually checking against the erased type Object\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\n" +
			"	public R getV() { return (R)v; } // Warning\n" +
			"	                         ^^^^\n" +
			"Type safety: The cast from V to R is actually checking against the erased type Object\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 8)\n" +
			"	public T getT() { return (T)o; } // Warning\n" +
			"	                         ^^^^\n" +
			"Type safety: The cast from Object to T is actually checking against the erased type Object\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - generic varargs method
	public void test387() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X<T>\n" +
				"{\n" +
				"\n" +
				"	public boolean test1()\n" +
				"	{\n" +
				"			test2(\"test\", null, 0);\n" +
				"	}\n" +
				"\n" +
				"	public <F> List<F> test2(final List<F> list, final String... strings)\n" +
				"	{\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	test2(\"test\", null, 0);\n" +
			"	^^^^^\n" +
			"The method test2(List<F>, String...) in the type X<T> is not applicable for the arguments (String, null, int)\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - variation
	public void test388() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X<T>\n" +
				"{\n" +
				"\n" +
				"	public boolean test1()\n" +
				"	{\n" +
				"			test2(null, null, \"test\");\n" +
				"			return false;\n" +
				"	}\n" +
				"\n" +
				"	public <F> List<F> test2(final List<F> list, final String... strings)\n" +
				"	{\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - variation
	public void test389() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public boolean test1()	{\n" +
				"		String s = foo(\"hello\");\n" +
				"		return s != null;\n" +
				"	}\n" +
				"\n" +
				"	public <F> F foo(F f, F... others) {\n" +
				"		return f;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - variation
	public void test390() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public boolean test1()	{\n" +
				"		String s = foo(null, \"hello\");\n" +
				"		return s != null;\n" +
				"	}\n" +
				"\n" +
				"	public <F> F foo(F f, F... others) {\n" +
				"		return f;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - variation
	public void test391() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public boolean test1()	{\n" +
				"		String[] s = foo(null, new String[]{ \"hello\" });\n" +
				"		return s != null;\n" +
				"	}\n" +
				"\n" +
				"	public <F> F foo(F f, F... others) {\n" +
				"		return f;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	String[] s = foo(null, new String[]{ \"hello\" });\n" +
			"	         ^\n" +
			"Type mismatch: cannot convert from String to String[]\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77422 - variation
	public void test392() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public boolean test1()	{\n" +
				"		foo(null, \"hello\");\n" + // no inference on expected type
				"		return true;\n" +
				"	}\n" +
				"\n" +
				"	public <F> F foo(F f, F... others) {\n" +
				"		return f;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78049 - chech invalid array initializer
	public void test393() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public boolean test1()	{\n" +
				"		foo(null, \"hello\");\n" + // no inference on expected type
				"		return true;\n" +
				"	}\n" +
				"\n" +
				"	public <F> F foo(F f, F... others) {\n" +
				"		return f;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78027
	public void test394() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X \n" +
				"{\n" +
				"}\n" +
				"\n" +
				"interface ITest<C extends X>\n" +
				"{ \n" +
				"}\n" +
				"\n" +
				"abstract class Test<C extends X> implements ITest<C>\n" +
				"{\n" +
				"  protected Manager<C> m_manager;\n" +
				"  \n" +
				"  public ITest<C> get()\n" +
				"  {\n" +
				"    return m_manager.getById(getClass(), new Integer(1));\n" +
				"  }\n" +
				"    \n" +
				"  public static class Manager<C extends X>\n" +
				"  {\n" +
				"    public <T extends ITest<C>> T getById(Class<T> cls, Integer id)\n" +
				"    {\n" +
				"      return null;\n" +
				"    }\n" +
				"  }\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74119 - variation
	public void test395() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Exception> {\n" +
				"	T element;\n" +
				"	\n" +
				"	void foo(X<? super NullPointerException> xnpe) {\n" +
				"		xnpe.element = new java.io.IOException();\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	xnpe.element = new java.io.IOException();\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from IOException to ? super NullPointerException\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78139 - downcast generic method inference
	public void test396() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Collection;\n" +
				"import java.util.List;\n" +
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X\n" +
				"{\n" +
				"    public static <T> List<T> emptyList() {\n" +
				"        return new ArrayList<T>();\n" +
				"    }\n" +
				"    public static <T> Collection<T> emptyCollection() {\n" +
				"        return new ArrayList<T>();\n" +
				"    }\n" +
				"    public static <T> Iterable<T> emptyIterable() {\n" +
				"        return new ArrayList<T>();\n" +
				"    }\n" +
				"    \n" +
				"    public static void main(String[] args) {\n" +
				"    	 // generic inference using expected lhs type: T --> String\n" +
				"        final List<String> lL = emptyList(); // 1\n" +
				"        \n" +
				"    	 // generic inference using expected cast type: T --> String\n" +
				"        final Collection<String> cL = (Collection<String>)emptyList(); // 2\n" +
				"        \n" +
				"    	 // generic inference using expected cast type: T --> String\n" +
				"        final Iterable<String> iL = (Iterable<String>)emptyList(); // 3\n" +
				"        \n" +
				"    	 // generic inference using expected lhs type: T --> String\n" +
				"        final Collection<String> cC = emptyCollection(); // 4\n" +
				"        \n" +
				"    	 // generic inference using expected cast type: T --> String\n" +
				"        final Iterable<String> iC = (Iterable<String>)emptyCollection(); // 5\n" +
				"        \n" +
				"    	 // generic inference using expected lhs type: T --> String\n" +
				"        final Iterable<String> iI = emptyIterable(); // 6\n" +
				"        \n" +
				"    	 // generic inference using expected lhs type: T --> String\n" +
				"        final Collection<String> cL2 = emptyList(); // 7\n" +
				"        \n" +
				"    	 // generic inference using expected lhs type: T --> String\n" +
				"        final Iterable<String> iC2 = emptyCollection(); // 8\n" +
				"    }\n" +
				"}"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76132
	public void test397() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface K1<A> { \n" +
				"        public <B extends A> void kk(K1<B> x); \n" +
				"} \n" +
				" \n" +
				"class K2<C> implements K1<C> { \n" +
				"        public <D extends C> void kk(K1<D> y) { \n" +
				"                System.out.println(\"K2::kk(\" + y.toString() + \")\"); \n" +
				"        } \n" +
				"} \n" +
				" \n" +
				"// --------------------------------------------------- \n" +
				" \n" +
				"interface L1<E> { \n" +
				"        public void ll(L1<? extends E> a); \n" +
				"} \n" +
				" \n" +
				"class L2<KK> implements L1<KK> { \n" +
				"        public void ll(L1<? extends KK> b) { \n" +
				"                ll2(b); \n" +
				"        } \n" +
				" \n" +
				"        private <LL extends KK> void ll2(L1<LL> c) { \n" +
				"                System.out.println(\"L2::ll2(\" + c.toString() + \")\"); \n" +
				"        } \n" +
				"} \n" +
				" \n" +
				"// --------------------------------------------------- \n" +
				" \n" +
				"interface M1<H> { \n" +
				"        public void mm(M1<? extends H> p); \n" +
				"} \n" +
				" \n" +
				"class M2<I> implements M1<I> { \n" +
				"        public <J extends I> void mm(M1<J> q) { \n" +
				"                System.out.println(\"M2::mm(\" + q.toString() + \")\"); \n" +
				"        } \n" +
				"} \n" +
				" \n" +
				"// =================================================== \n" +
				" \n" +
				"class XX            { public String toString() { return \"XX\"; } } \n" +
				"class YY extends XX { public String toString() { return \"YY\"; } } \n" +
				"class ZZ extends YY { public String toString() { return \"ZZ\"; } } \n" +
				" \n" +
				"// --------------------------------------------------- \n" +
				" \n" +
				"public class X { \n" +
				"        public static void main(String arg[]) { \n" +
				"                goK(new K2<YY>()); \n" +
				"                goL(new L2<YY>()); \n" +
				"                goM(new M2<YY>()); \n" +
				"        } \n" +
				" \n" +
				" \n" +
				"        public static void goK(K1<YY> k) { \n" +
				"                // k.kk(new K2<XX>()); // Would fail \n" +
				"                k.kk(new K2<YY>()); \n" +
				"                k.kk(new K2<ZZ>()); \n" +
				"        } \n" +
				" \n" +
				" \n" +
				"        public static void goL(L1<YY> l) { \n" +
				"                // l.ll(new L2<XX>()); // Would fail \n" +
				"                l.ll(new L2<YY>()); \n" +
				"                l.ll(new L2<ZZ>()); \n" +
				"        } \n" +
				" \n" +
				" \n" +
				"        public static void goM(M1<YY> m) { \n" +
				"                // m.mm(new M2<XX>()); // Would fail \n" +
				"                m.mm(new M2<YY>()); \n" +
				"                m.mm(new M2<ZZ>()); \n" +
				"        } \n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 33)\n" +
			"	class M2<I> implements M1<I> { \n" +
			"	      ^^\n" +
			"The type M2<I> must implement the inherited abstract method M1<I>.mm(M1<? extends I>)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 34)\n" +
			"	public <J extends I> void mm(M1<J> q) { \n" +
			"	                          ^^^^^^^^^^^\n" +
			"Name clash: The method mm(M1<J>) of type M2<I> has the same erasure as mm(M1<? extends H>) of type M1<H> but does not override it\n" +
			"----------\n"
		);
	}
	// cannot allocate parameterized type with wildcards
	public void test398() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"    X(){\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"		new X<?>();\n" +
				"		new X<? extends String>();\n" +
				"		new X<?>(){};\n" +
				"		new X<? extends String>(){};\n" +
				"	}\n" +
				"}\n"	,
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	new X<?>();\n" +
			"	    ^\n" +
			"Cannot instantiate the type X<?>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	new X<? extends String>();\n" +
			"	    ^\n" +
			"Cannot instantiate the type X<? extends String>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	new X<?>(){};\n" +
			"	    ^\n" +
			"The type new X(){} cannot extend or implement X<?>. A supertype may not specify any wildcard\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	new X<? extends String>(){};\n" +
			"	    ^\n" +
			"The type new X(){} cannot extend or implement X<? extends String>. A supertype may not specify any wildcard\n" +
			"----------\n");
	}

	public void test399() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"    T t;\n" +
				"    X(T t){\n" +
				"        this.t = t;\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"		X<? extends AX> x = new X<AX<Math>>(new AX<String>());\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class AX<P> {\n" +
				"    P foo() { return null; }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	X<? extends AX> x = new X<AX<Math>>(new AX<String>());\n" +
			"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The constructor X<AX<Math>>(AX<String>) is undefined\n" +
			"----------\n");
	}

	public void test400() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"    T t;\n" +
				"    X(X<? extends T> xt){\n" +
				"        this.t = xt.t;\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"		X<? extends AX> x = new X<AX<Math>>(new X<AX<String>>(null));\n" +
				"	}\n" +
				"}\n" +
				"class AX<P> {\n" +
				"    P foo() { return null; }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	X<? extends AX> x = new X<AX<Math>>(new X<AX<String>>(null));\n" +
			"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The constructor X<AX<Math>>(X<AX<String>>) is undefined\n" +
			"----------\n");
	}

	// legal to allocate/inherit from a type with wildcards, as long as non direct arguments
	public void test401() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	void foo() {\n" +
				"		new X<X<?>>();\n" +
				"		new X<X<? extends String>>();\n" +
				"		new X<X<?>>(){};\n" +
				"		new X<X<? extends String>>(){};\n" +
				"	}\n" +
				"}",
			},
			"");
	}

	// legal to inherit from a type with wildcards, as long as non direct arguments
	public void test402() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y<Y<?>> {\n" +
				"}\n" +
				"class Y<T> {}",
			},
			"");
	}
	// check cast between generic types
	public void test403() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"	\n" +
				"	void foo(X<X<? extends String>> xs) {\n" +
				"		X<X<String>> x = (X<X<String>>) xs;\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	X<X<String>> x = (X<X<String>>) xs;\n" +
			"	                 ^^^^^^^^^^^^^^^^^\n" +
			"Cannot cast from X<X<? extends String>> to X<X<String>>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// check cast between generic types
	public void test404() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" +
				"	\n" +
				"	void foo(X<? extends String> xs) {\n" +
				"		X<String> x = (X<String>) xs;\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	X<String> x = (X<String>) xs;\n" +
			"	              ^^^^^^^^^^^^^^\n" +
			"Type safety: The cast from X<? extends String> to X<String> is actually checking against the erased type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// check cast between generic types
	public void test405() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" +
				"	\n" +
				"	<T> void foo(X<X<T>> xs) {\n" +
				"		X<X<String>> x = (X<X<String>>) xs;\n" +
				"	}\n" +
				"	<T> void bar(X<T> xs) {\n" +
				"		X<String> x = (X<String>) xs;\n" +
				"	}	\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	X<X<String>> x = (X<X<String>>) xs;\n" +
			"	                 ^^^^^^^^^^^^^^^^^\n" +
			"Cannot cast from X<X<T>> to X<X<String>>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	X<String> x = (X<String>) xs;\n" +
			"	              ^^^^^^^^^^^^^^\n" +
			"Type safety: The cast from X<T> to X<String> is actually checking against the erased type X\n" +
			"----------\n");
	}

	public void test406() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X<K1,V1> implements M<K1,V1> {\n" +
				"	abstract M<K1,V1> other();\n" +
				"	public S<E<K1,V1>> entrySet() {\n" +
				"		return other().entrySet();\n" +
				"	}\n" +
				"}\n" +
				"interface M<K2,V2> {\n" +
				"	 interface E<K3,V3> { }\n" +
				"	 S<E<K2, V2>> entrySet();\n" +
				"}\n" +
				"interface S<T> {}",
			},
			"");
	}

	public void test407() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X<K1,V1> implements M<K1,V1> {\n" +
				"	abstract M<K1,V1> other();\n" +
				"	public S<M.E<K1,V1>> entrySet() {\n" + // qualified M.E...
				"		return other().entrySet();\n" +
				"	}\n" +
				"}\n" +
				"interface M<K2,V2> {\n" +
				"	 interface E<K3,V3> { }\n" +
				"	 S<E<K2, V2>> entrySet();\n" +
				"}\n" +
				"interface S<T> {}",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78008
	public void test408() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"    public Integer[] getTypes() {\n" +
				"        List<Integer> list = new ArrayList<Integer>();\n" +
				"        return list == null \n" +
				"            ? new Integer[0] \n" +
				"            : list.toArray(new Integer[list.size()]);\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        Class clazz = null;\n" +
				"        try {\n" +
				"            clazz = Class.forName(\"X\");\n" +
				"    	     System.out.println(\"SUCCESS\");\n" +
				"        } catch (Throwable e) {\n" +
				"            e.printStackTrace();\n" +
				"        }\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78008
	public void test409() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"    public Number getTypes() {\n" +
				"        List<Integer> list = new ArrayList<Integer>();\n" +
				"        return list == null \n" +
				"            ? Float.valueOf(0)\n" +
				"            : list.get(0);\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        Class clazz = null;\n" +
				"        try {\n" +
				"            clazz = Class.forName(\"X\");\n" +
				"    	     System.out.println(\"SUCCESS\");\n" +
				"        } catch (Throwable e) {\n" +
				"            e.printStackTrace();\n" +
				"        }\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=74178
	public void test410() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"public void write(List<? super Exception> list) {\n" +
				"	\n" +
				"  list.add(new RuntimeException());             // works\n" +
				"  list.add(new IllegalMonitorStateException()); // works\n" +
				"  Exception exc = new Exception();\n" +
				"  list.add(exc);                                // works\n" +
				"  list.add(new Object());                       // should fail\n" +
				"  list.add(new Throwable());                    // should fail\n" +
				"  list.add(new Exception());                    // works\n" +
				"}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\r\n" +
			"	list.add(new Object());                       // should fail\r\n" +
			"	     ^^^\n" +
			"The method add(? super Exception) in the type List<? super Exception> is not applicable for the arguments (Object)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\r\n" +
			"	list.add(new Throwable());                    // should fail\r\n" +
			"	     ^^^\n" +
			"The method add(? super Exception) in the type List<? super Exception> is not applicable for the arguments (Throwable)\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78015
	public void test411() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I<T> {\n" +
				"    void m1(T t);\n" +
				"    void m2(T t);\n" +
				"}\n" +
				"\n" +
				"class A {};\n" +
				"\n" +
				"class B implements I<A> {\n" +
				"    public void m1(A a) {\n" +
				"    	System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"    public void m2(A a) {}\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        m(new B());\n" +
				"    }\n" +
				"\n" +
				"    public static void m(I<A> x) {\n" +
				"        x.m1(null);\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78467
	public void _test412() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"    public static <T> T first(T... args) {\n" +
				"        return args[0];\n" +
				"    }\n" +
				"    \n" +
				"    public static void main(String[] args) {\n" +
				"    	if (false) { \n" +
				"    		String s = first(); \n" +
				"    		int i; \n" +
				"    		i++; \n" +
				"    	}\n" +
				"        System.out.println(first(\"SUCCESS\", \"List\"));\n" +
				"    }\n" +
				"   Zork z;\n" +
				"}",
			},
			"should warn about unchecked array conversion for T[]");
	}

	public void test413() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	static class TLM {\n" +
				"	}\n" +
				"    TLM getMap(TL t) {\n" +
				"        return t.tls;\n" +
				"    }\n" +
				"    static TLM createInheritedMap(TLM parentMap) {\n" +
				"        return new TLM();\n" +
				"    }  \n" +
				"}\n" +
				"\n" +
				"class TL {\n" +
				"   X.TLM tls = null;\n" +
				"}",
			},
			"");
	}

	public void test414() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(L l, C<? super X> c) {\n" +
				"		bar(l, c);\n" +
				"	}\n" +
				"	<T> void bar(L<T> l, C<? super T> c) { \n" +
				"	}	\n" +
				"}\n" +
				"class C<E> {}\n" +
				"class L<E> {}",
			},
			"");
	}

	public void test415() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public S<M.E<Object,Object>> foo(HM hm) {\n" +
				"		return C.bar(hm).foo();\n" +
				"    }\n" +
				"}\n" +
				"class C {\n" +
				"    public static <K,V> M<K,V> bar(M<? extends K,? extends V> m) {\n" +
				"		return null;\n" +
				"    }\n" +
				"}\n" +
				"class S<E> {\n" +
				"}\n" +
				"abstract class HM<U,V> implements M<U,V>{\n" +
				"}\n" +
				"interface M<A,B> {\n" +
				"	static class E<S,T> {}\n" +
				"	S<E<A,B>> foo();	\n" +
				"}",
			},
			"");
	}

	public void test416() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public S<M.E<Object,String>> foo(HM hm) {\n" +
				"    	M<Object, String> m = C.bar(hm);\n" +
				"    	if (false) return m.foo();\n" +
				"		return C.bar(hm).foo();\n" +
				"    }\n" +
				"}\n" +
				"class C {\n" +
				"    public static <K,V> M<K,V> bar(M<? extends K,? extends V> m) {\n" +
				"		return null;\n" +
				"    }\n" +
				"}\n" +
				"class S<E> {\n" +
				"}\n" +
				"abstract class HM<U,V> implements M<U,V>{\n" +
				"}\n" +
				"interface M<A,B> {\n" +
				"	static class E<S,T> {}\n" +
				"	S<E<A,B>> foo();	\n" +
				"}",
			},
			"");
	}

	public void test417() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" +
				"	\n" +
				"	<T> X<T> foo(X<T> xt) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	X<E> identity() {\n" +
				"		return this;\n" +
				"	}\n" +
				"	void bar(X x) {\n" +
				"		X<String> xs = foo(x).identity();\n" +
				"	}\n" +
				"}\n",
			},
			"");
	}

	public void test418() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" +
				"	\n" +
				"	<T> X<T> foo(X<T> xt, X<T> xt2) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	X<E> identity() {\n" +
				"		return this;\n" +
				"	}\n" +
				"	void bar(X x, X<String> xs) {\n" +
				"		X<String> xs2 = foo(x, xs).identity();\n" +
				"	}\n" +
				"}\n",
			},
			"");
	}

	public void test419() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" +
				"	\n" +
				"	<T,U> X<T> foo(X<T> xt, X<U> xt2) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	X<E> identity() {\n" +
				"		return this;\n" +
				"	}\n" +
				"	void bar(X x, X<String> xs) {\n" +
				"		X<String> xs2 = foo(x, xs).identity();\n" +
				"	}\n" +
				"}\n",
			},
			"");
	}

	public void test420() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" +
				"	\n" +
				"	<T,U> X<U> foo(X<T> xt, X<U> xt2) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	X<E> identity() {\n" +
				"		return this;\n" +
				"	}\n" +
				"	void bar(X x, X<String> xs) {\n" +
				"		X<String> xs2 = foo(x, xs).identity();\n" +
				"	}\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78863
	public void test421() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import java.util.HashMap;\n" +
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public class Test\n" +
				"{\n" +
				"  protected Map<Class<? extends Object>, List<Object>> m_test\n" +
				"    = new HashMap<Class<? extends Object>, List<Object>>();\n" +
				"}\n",
				"Test2.java",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"\n" +
				"public class Test2 extends Test\n" +
				"{\n" +
				"  public Map<Class<? extends Object>, List<Object>> test()\n" +
				"  {\n" +
				"    return m_test;\n" +
				"  }\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78704
	public void test422() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	String foo() {\n" +
				"		return new X();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	return new X();\n" +
			"	       ^^^^^^^\n" +
			"Type mismatch: cannot convert from X to String\n" +
			"----------\n");
	}

	public void test423() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"    static <T extends X> T bar() {\n" +
				"        return null;\n" +
				"    }\n" +
				"    static <U extends X&Runnable> U foo() {\n" +
				"        return null;\n" +
				"    }\n" +
				"\n" +
				"    public static void main(String argv[]) {\n" +
				"    	bar();\n" +
				"        foo();\n" +
				"    }\n" +
				"\n" +
				"}",
			},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	foo();\n" +
		"	^^^\n" +
		"Bound mismatch: The generic method foo() of type X is not applicable for the arguments () since the type X is not a valid substitute for the bounded parameter <U extends X & Runnable>\n" +
		"----------\n");
	}

	public void test424() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	<T extends A> T foo(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().bar();\n" +
				"	}\n" +
				"	void bar() {\n" +
				"		B b = foo(new B());\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"\n",
			},
		"");
	}

	// check tiebreak eliminates related generic methods which are less specific
	public void test425() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"    static <E extends A> void m(E e) { System.out.println(\"A:\"+e.getClass()); }\n" +
				"    static <F extends B> void m(F f) throws Exception { System.out.println(\"B:\"+f.getClass()); }\n" +
				"    static <G extends C> void m(G g) throws IOException { System.out.println(\"C:\"+g.getClass()); }\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        m(new A());\n" +
				"        m(new B());\n" +
				"        m(new C());\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends A {}\n" +
				"\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	m(new B());\n" +
			"	^^^^^^^^^^\n" +
			"Unhandled exception type Exception\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	m(new C());\n" +
			"	^^^^^^^^^^\n" +
			"Unhandled exception type IOException\n" +
			"----------\n");
	}

	// check inferred return types are truly based on arguments, and not on parameter erasures
	public void test426() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    static <E extends A> E m(E e) { System.out.print(\"[A:\"+e.getClass()+\"]\"); return e; }\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        A a = m(new A());\n" +
				"        B b = m(new B());\n" +
				"        C c = m(new C());\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends A {}\n",
			},
			"[A:class A][A:class B][A:class C]");
	}

	// check inferred return types are truly based on arguments, and not on parameter erasures
	public void test427() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    static <E extends A> E m(E e, E... e2) { System.out.print(\"[A:\"+e.getClass()+\"]\"); return e; }\n" +
				"    static <F extends B> F m(F f, F... f2) { System.out.print(\"[B:\"+f.getClass()+\"]\"); return f; }\n" +
				"    static <G extends C> G m(G g, G... g2) { System.out.print(\"[C:\"+g.getClass()+\"]\"); return g; }\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        A a = m(new A(), new A());\n" +
				"        B b = m(new B(), new B());\n" +
				"        C c = m(new C(), new C());\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends A {}\n",
			},
			"[A:class A][B:class B][C:class C]");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79390
	public void test428() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   Zork z;\n" +
				"	public static void foo() {\n" +
				"		class A<T extends Number> {\n" +
				"			T t = null;\n" +
				"			T get() {\n" +
				"				return t;\n" +
				"			}\n" +
				"		}\n" +
				"		A<Long> a = new A<Long>() {\n" +
				"			Long get() {\n" +
				"				return new Long(5);\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78293
	public void test429() {
		this.runConformTest(
			new String[] {
				"X1.java",
				"class X1 <T extends Y & Comparable<Y>> {}\n" +
				"abstract class Y implements Comparable<Y> {}",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X2.java",
				"class X2 <T extends Y & Comparable<Y>> {}\n" +
				"abstract class Y extends Z {}\n" +
				"abstract class Z implements Comparable<Y> {}",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X3.java",
				"class X3 <T extends Y & Comparable<Z>> {}\n" +
				"abstract class Y extends Z {}\n" +
				"abstract class Z implements Comparable<Z> {}",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X4.java",
				"class X4 <T extends Comparable<Z> & Comparable<Z>> {}\n" +
				"abstract class Y extends Z {}\n" +
				"abstract class Z implements Comparable<Z> {}",
			},
			"----------\n" +
			"1. ERROR in X4.java (at line 1)\r\n" +
			"	class X4 <T extends Comparable<Z> & Comparable<Z>> {}\r\n" +
			"	                                    ^^^^^^^^^^\n" +
			"Duplicate bound Comparable<Z>\n" +
			"----------\n"
			// no complaints about duplicates if they are both parameterized with same args
			// but you cannot extend Comparable & Comparable so we'll report an error
		);
		this.runNegativeTest(
			new String[] {
				"X5.java",
				"class X5 <T extends Y & Comparable<X5>> {}\n" +
				"abstract class Y implements Comparable<Y> {}",
			},
			"----------\n" +
			"1. ERROR in X5.java (at line 1)\n" +
			"	class X5 <T extends Y & Comparable<X5>> {}\n" +
			"	                        ^^^^^^^^^^\n" +
			"Bound conflict: Comparable<X5> is inherited with conflicting arguments\n" +
			"----------\n"
			// Comparable cannot be inherited with different arguments: <X5> and <Y>
		);
		this.runNegativeTest(
			new String[] {
				"X6.java",
				"class X6 <T extends Y & Comparable<X6>> {}\n" +
				"abstract class Y extends Z {}\n" +
				"abstract class Z implements Comparable<Z> {}",
			},
			"----------\n" +
			"1. ERROR in X6.java (at line 1)\r\n" +
			"	class X6 <T extends Y & Comparable<X6>> {}\r\n" +
			"	                        ^^^^^^^^^^\n" +
			"Bound conflict: Comparable<X6> is inherited with conflicting arguments\n" +
			"----------\n"
			// Comparable cannot be inherited with different arguments: <X6> and <Y>
		);
		this.runNegativeTest(
			new String[] {
				"X7.java",
				"class X7 <T extends Comparable<Z> & Comparable<X7>> {}\n" +
				"abstract class Y extends Z {}\n" +
				"abstract class Z implements Comparable<Z> {}",
			},
			"----------\n" +
			"1. ERROR in X7.java (at line 1)\r\n" +
			"	class X7 <T extends Comparable<Z> & Comparable<X7>> {}\r\n" +
			"	                                    ^^^^^^^^^^\n" +
			"Bound conflict: Comparable<X7> is inherited with conflicting arguments\n" +
			"----------\n"
			// Comparable cannot be inherited with different arguments: <Z> and <X7>
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79797
	public void test430() {
		this.runConformTest(
			new String[] {
				"p/MMM.java",
				"package p;\n" +
				"public interface MMM< F extends MMM<F,G>, G extends NNN> { } \n",
				"p/NNN.java",
				"package p;\n" +
				"public interface NNN { } \n",
			},
			"");

		this.runConformTest(
			new String[] {
				"X.java",
				"import p.MMM;\n" +
				"import p.NNN;\n" +
				"\n" +
				"interface RRR< A extends MMM<A, B>, B extends NNN> {}\n" +
				"\n" +
				"class J1 implements MMM<J1, J2> { }\n" +
				"class J2 implements NNN { }\n" +
				"\n" +
				"class J3 implements RRR<J1,J2> {} \n" +
				"\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    J3 thing = null;\n" +
				"  }\n" +
				"}\n",
			},
			"",
			null,
			false, // do not flush output
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79891
	public void test431() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<Type> {\n" +
				"  private class Element {\n" +
				"  }\n" +
				"  public X() {\n" +
				"    Element[] eArray = new Element[10];\n" +
				"  }\n" +
				"}\n",
			},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Element[] eArray = new Element[10];\n" +
		"	                   ^^^^^^^^^^^^^^^\n" +
		"Cannot create a generic array of X<Type>.Element\n" +
		"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79891
	public void test432() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Type> {\n" +
				"  private static class Element {\n" +
				"  }\n" +
				"  public X() {\n" +
				"    Element[] eArray = new Element[10];\n" +
				"  }\n" +
				"}\n",
			},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80144
	public void test433() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"interface Alpha<\n" +
				"	A1 extends Alpha<A1, B1>, \n" +
				"	B1 extends Beta<A1, B1>> {\n" +
				"}\n" +
				"interface Beta<\n" +
				"	A2 extends Alpha<A2, B2>, \n" +
				"	B2 extends Beta<A2, B2>> {\n" +
				"}\n" +
				"interface Phi<\n" +
				"	A3 extends Alpha<A3, B3>, \n" +
				"	B3 extends Beta<A3, B3>> {\n" +
				"	\n" +
				"	public void latinize(A3 s);\n" +
				"}\n" +
				"\n" +
				"public class X<\n" +
				"	A extends Alpha<A, B>, \n" +
				"	B extends Beta<A, B>, \n" +
				"	P extends Phi<A, B>> extends ArrayList<P> implements Phi<A, B> {\n" +
				"	\n" +
				"	public final void latinize(A a) {\n" +
				"		frenchify(this, a); // (X<A,B,P>, A)\n" +
				"	}\n" +
				"	// -----------------------------------------------------------------\n" +
				"	public static final <AA extends Alpha<AA, BB>, BB extends Beta<AA, BB>> \n" +
				"	void frenchify(Collection< ? extends Phi<AA, BB>> phis, AA aa) {\n" +
				"		for (final Phi<AA, BB> phi : phis)\n" +
				"			phi.latinize(aa);\n" +
				"	}\n" +
				"}\n",
			},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80083
	public void test434() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X\n" +
				"{\n" +
				"\n" +
				"  public static void main(String[] args)\n" +
				"  {\n" +
				"    ArrayList<String> l = new ArrayList<String>();\n" +
				"    l.add(\"x\");\n" +
				"    String s = \"\";\n" +
				"    s += l.get(0); // X\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"  }\n" +
				"\n" +
				"}\n",
			},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80765
	public void test435() {
		this.runNegativeTest(
			new String[] {
				"Test.java",//===============================
				"import java.lang.reflect.InvocationTargetException;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"import orders.DiscreteOrder;\n" +
				"import orders.impl.IntegerOrder;\n" +
				"import orders.impl.IntegerOrder2;\n" +
				"\n" +
				"public class Test {\n" +
				"\n" +
				"    public static void main(String[] args) throws SecurityException,\n" +
				"            NoSuchMethodException, IllegalArgumentException,\n" +
				"            IllegalAccessException {\n" +
				"        Test test = new Test();\n" +
				"\n" +
				"        for (String method : new String[] { \"test1\", \"test2\", \"test3\", \"test4\" }) {\n" +
				"            Method m = test.getClass().getMethod(method);\n" +
				"            try {\n" +
				"                m.invoke(test);\n" +
				"                System.out.print(\"*** \" + m + \": success\");\n" +
				"            } catch (InvocationTargetException e) {\n" +
				"                System.out.print(\"*** \" + m + \": failed, stacktrace follows\");\n" +
				"                e.getCause().printStackTrace(System.out);\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    public void test1() { // works\n" +
				"        new IntegerOrder().next(new Integer(0)); // works\n" +
				"    }\n" +
				"\n" +
				"    public void test2() { // doesn\'t work\n" +
				"        final DiscreteOrder<Integer> order = new IntegerOrder();\n" +
				"        order.next(new Integer(0));\n" +
				"    }\n" +
				"\n" +
				"    public void test3() { // works\n" +
				"        new IntegerOrder2().next(new Integer(0)); // works\n" +
				"    }\n" +
				"\n" +
				"    public void test4() { // doesn\'t work\n" +
				"        final DiscreteOrder<Integer> order = new IntegerOrder2();\n" +
				"        order.next(new Integer(0));\n" +
				"    }\n" +
				"}\n",
				"orders/DiscreteOrder.java",//===============================
				"package orders;\n" +
				"public interface DiscreteOrder<E extends Comparable<E>> {\n" +
				"    /**\n" +
				"     * @return The element immediately before <code>element</code> in the\n" +
				"     *         discrete ordered space.\n" +
				"     */\n" +
				"    public E previous(E element);\n" +
				"    /**\n" +
				"     * @return The element immediately after <code>element</code> in the\n" +
				"     *         discrete ordered space.\n" +
				"     */\n" +
				"    public E next(E element);\n" +
				"}\n",
				"orders/impl/IntegerOrder.java",//===============================
				"package orders.impl;\n" +
				"import orders.DiscreteOrder;\n" +
				"\n" +
				"public class IntegerOrder implements DiscreteOrder<Integer> {\n" +
				"\n" +
				"    public IntegerOrder() {\n" +
				"        super();\n" +
				"    }\n" +
				"\n" +
				"    public Integer previous(Integer arg0) {\n" +
				"        return new Integer(arg0.intValue() - 1);\n" +
				"    }\n" +
				"\n" +
				"    public Integer next(Integer arg0) {\n" +
				"        return new Integer(arg0.intValue() + 1);\n" +
				"    }\n" +
				"}\n",
				"orders/impl/IntegerOrder2.java",//===============================
				"package orders.impl;\n" +
				"\n" +
				"\n" +
				"public class IntegerOrder2 extends IntegerOrder {\n" +
				"\n" +
				"    public IntegerOrder2() {\n" +
				"        super();\n" +
				"    }\n" +
				"\n" +
				"    public Comparable previous(Comparable arg0) {\n" +
				"        return previous((Integer) arg0);\n" +
				"    }\n" +
				"\n" +
				"    public Comparable next(Comparable arg0) {\n" +
				"        return next((Integer) arg0);\n" +
				"    }\n" +
				"\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in orders\\impl\\IntegerOrder2.java (at line 10)\r\n" +
			"	public Comparable previous(Comparable arg0) {\r\n" +
			"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method previous(Comparable) of type IntegerOrder2 has the same erasure as previous(E) of type DiscreteOrder<E> but does not override it\n" +
			"----------\n" +
			"2. ERROR in orders\\impl\\IntegerOrder2.java (at line 14)\r\n" +
			"	public Comparable next(Comparable arg0) {\r\n" +
			"	                  ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method next(Comparable) of type IntegerOrder2 has the same erasure as next(E) of type DiscreteOrder<E> but does not override it\n" +
			"----------\n"
			// "*** public void Test.test1(): success*** public void Test.test2(): success*** public void Test.test3(): success*** public void Test.test4(): success"
			// name clash: next(java.lang.Comparable) in orders.impl.IntegerOrder2 and next(E) in orders.DiscreteOrder<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80028
	public void test436() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		Number n= new Integer(1);\n" +
				"		X x = new X<Number>();\n" +
				"		x.m(n);\n" +
				"		x.m(new Integer(2));\n" +
				"		Y y= new Y();\n" +
				"		y.m(n);\n" +
				"		y.m(new Integer(2));\n" +
				"	}\n" +
				"}\n",
				"X.java",
				"class X<T> {\n" +
				"	public void m(Number num) { System.out.print(\"X.m(Number) = \" + num + ','); }\n" +
				"	public void m(T t) { System.out.print(\"X.m(T) = \" + t + ','); }\n" +
				"}\n",
				"Y.java",
				"class Y extends X<Number> {\n" +
				"	public void m(Number num) { System.out.print(\"Y.m(Number) = \" + num + ','); }\n" +
				"}\n",
			},
		"X.m(Number) = 1,X.m(Number) = 2,Y.m(Number) = 1,Y.m(Number) = 2,");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80028
	public void test437() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		Number n= new Integer(1);\n" +
				"		X x = new X<Number>();\n" +
				"		x.m(n);\n" +
				"		x.m(new Integer(2));\n" +
				"		Y y= new Y();\n" +
				"		y.m(n);\n" +
				"		y.m(new Integer(2));\n" +
				"	}\n" +
				"}\n",
				"X.java",
				"class X<T> {\n" +
				"	public void m(Number num) { System.out.print(\"X.m(Number) = \" + num + ','); }\n" +
				"	public void m(T t) { System.out.print(\"X.m(T) = \" + t + ','); }\n" +
				"}\n",
				"Y.java",
				"class Y extends X<Number> {\n" +
				"	public void m(Number num) { System.out.print(\"Y.m(Number) = \" + num + ','); }\n" +
				"}\n",
			},
		"X.m(Number) = 1,X.m(Number) = 2,Y.m(Number) = 1,Y.m(Number) = 2,");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78591
	public void test438() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X<T> {\n" +
				"    Zork z;\n" +
				"    List<T> list;\n" +
				"    void add(Object abs) {\n" +
				"        list.add((T) list.get(0)); // checked cast\n" +
				"        list.add((T) abs); // unchecked cast\n" +
				"    }\n" +
				"    void bar(List<? extends T> other) {\n" +
				"    	list.add((T) other.get(0)); // checked cast\n" +
				"    }\n" +
				"    void baz(List<? super T> other) {\n" +
				"    	list.add((T) other.get(0)); // unchecked cast\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\n" +
			"	list.add((T) list.get(0)); // checked cast\n" +
			"	         ^^^^^^^^^^^^^^^\n" +
			"Unnecessary cast from T to T\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 7)\n" +
			"	list.add((T) abs); // unchecked cast\n" +
			"	         ^^^^^^^\n" +
			"Type safety: The cast from Object to T is actually checking against the erased type Object\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 10)\n" +
			"	list.add((T) other.get(0)); // checked cast\n" +
			"	         ^^^^^^^^^^^^^^^^\n" +
			"Unnecessary cast from ? extends T to T\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 13)\n" +
			"	list.add((T) other.get(0)); // unchecked cast\n" +
			"	         ^^^^^^^^^^^^^^^^\n" +
			"Type safety: The cast from ? super T to T is actually checking against the erased type Object\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78592
	public void test439() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Node {\n" +
				"}\n" +
				"class Composite<E> {\n" +
				"}\n" +
				"class Concrete extends Composite {\n" +
				"}\n" +
				"public class X {\n" +
				"    Composite<Node> comp = new Concrete(); // unchecked cast\n" +
				"    Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	Composite<Node> comp = new Concrete(); // unchecked cast\n" +
			"	                       ^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type Concrete needs unchecked conversion to conform to Composite<Node>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	public void test440() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	class Y<U> {\n" +
				"		public void foo(X<T> xt) {\n" +
				"			U u = (U) xt;\n" +
				"		}\n" +
				"	}\n" +
				"  Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	U u = (U) xt;\n" +
			"	      ^^^^^^\n" +
			"Type safety: The cast from X<T> to U is actually checking against the erased type Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	public void test441() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Number> {\n" +
				"    T[] array;\n" +
				"    X(int s) {\n" +
				"        array = (T[]) new Number[s];   // Unnecessary cast from Number[] to T[]\n" +
				"        array = new Number[s];   // Type mismatch: cannot convert from Number[] to T[]\n" +
				"     }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	array = (T[]) new Number[s];   // Unnecessary cast from Number[] to T[]\n" +
			"	        ^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The cast from Number[] to T[] is actually checking against the erased type Number[]\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	array = new Number[s];   // Type mismatch: cannot convert from Number[] to T[]\n" +
			"	        ^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from Number[] to T[]\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82053
	public void test442() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Foo {\n" +
				"	public interface Model {\n" +
				"	}\n" +
				"	public interface View<M extends Model> {\n" +
				"		M getTarget() ;\n" +
				"	}\n" +
				"}\n" +
				"class Bar {\n" +
				"	public interface Model extends Foo.Model {\n" +
				"	}\n" +
				"	public interface View<M extends Model> extends Foo.View<M> {\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	public void baz() {\n" +
				"		Bar.View<?> bv = null ;\n" +
				"		Bar.Model m = bv.getTarget() ;\n" +
				"	}\n" +
				"}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81757
	public void test443() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"public class X implements Iterator<String> {\n" +
				"    public boolean hasNext() { return false; }\n" +
				"    public String next() { return null; }\n" +
				"    public void remove() {}\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81824
	public void test444() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I<Integer>, I<String> {}\n" +
				"interface I<T> {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\r\n" +
			"	public class X implements I<Integer>, I<String> {}\r\n" +
			"	             ^\n" +
			"Duplicate interface I<T> for the type X\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78810
	public void test445() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X {\n" +
				"    public abstract Object getProperty(final Object src, final String name);\n" +
				"    Zork z;\n" +
				"    public <T> T getTheProperty(final Object src, final String name)\n" +
				"    {\n" +
				"        final T val = (T) getProperty(src, name); // this gives erroneous cast warning\n" +
				"        return val;\n" +
				"    }\n" +
				"}\n"	,
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	Zork z;\r\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\r\n" +
			"	final T val = (T) getProperty(src, name); // this gives erroneous cast warning\r\n" +
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The cast from Object to T is actually checking against the erased type Object\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159
	public void test446() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  class Inner<B> { }\n" +
				"\n" +
				"  void method() {\n" +
				"    X<String>.Inner<Integer> a= new X<String>().new Inner<Integer>();\n" +
				"    Inner<Integer> b= new X<A>().new Inner<Integer>();\n" +
				"    Inner<Integer> c= new Inner<Integer>();\n" +
				"    //OK for javac and eclipse\n" +
				"\n" +
				"    X<String>.Inner<Integer> d= new X<String>.Inner<Integer>();\n" +
				"    //eclipse: OK\n" +
				"    //javac: error: \'(\' or \'[\' expected\n" +
				"\n" +
				"    X<A>.Inner<Integer> e= new X<A>().new Inner<Integer>();\n" +
				"    X<A>.Inner<Integer> f= new Inner<Integer>();\n" +
				"    e= b;\n" +
				"    f= c;\n" +
				"    //javac: OK\n" +
				"    //eclipse: Type mismatch: cannot convert from X<A>.Inner<Integer> to X<A>.Inner<Integer>\n" +
				"\n" +
				"  }\n" +
				"}\n" +
				"\n" +
				"class External {\n" +
				"  void m() {\n" +
				"    X<String>.Inner<Integer> x= new X<String>().new Inner<Integer>();\n" +
				"    //OK for javac and eclipse\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	X<String>.Inner<Integer> d= new X<String>.Inner<Integer>();\n" +
			"	                                ^^^^^^^^^^^^^^^\n" +
			"Cannot allocate the member type X<String>.Inner<Integer> using a parameterized compound name; use its simple name and an enclosing instance of type X<String>\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159 - variation
	public void test447() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  class Inner<B> { }\n" +
				"\n" +
				"  void method() {\n" +
				"    X<String>.Inner<Integer> d1 = new X<String>.Inner<Integer>();\n" +
				"    X.Inner d2 = new X.Inner();\n" +
				"    X.Inner<Integer> d3 = new X.Inner<Integer>();\n" +
				"    d1 = d2;\n" +
				"    d2 = d1;\n" +
				"    d1 = d3;\n" +
				"    d3 = d1;\n" +
				"    d2 = d3;\n" +
				"    d3 = d2;\n" +
				"\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X<String>.Inner<Integer> d1 = new X<String>.Inner<Integer>();\n" +
			"	                                  ^^^^^^^^^^^^^^^\n" +
			"Cannot allocate the member type X<String>.Inner<Integer> using a parameterized compound name; use its simple name and an enclosing instance of type X<String>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	X.Inner<Integer> d3 = new X.Inner<Integer>();\n" +
			"	^^^^^^^\n" +
			"The member type X.Inner<Integer> must be qualified with a parameterized type, since it is not static\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	X.Inner<Integer> d3 = new X.Inner<Integer>();\n" +
			"	                          ^^^^^^^\n" +
			"The member type X.Inner<Integer> must be qualified with a parameterized type, since it is not static\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 8)\n" +
			"	d1 = d2;\n" +
			"	     ^^\n" +
			"Type safety: The expression of type X.Inner needs unchecked conversion to conform to X<String>.Inner<Integer>\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 10)\n" +
			"	d1 = d3;\n" +
			"	     ^^\n" +
			"Type safety: The expression of type X.Inner<Integer> needs unchecked conversion to conform to X<String>.Inner<Integer>\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 13)\n" +
			"	d3 = d2;\n" +
			"	     ^^\n" +
			"Type safety: The expression of type X.Inner needs unchecked conversion to conform to X.Inner<Integer>\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159 - variation
	public void test448() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  static class Inner<B> { }\n" +
				"\n" +
				"  void method() {\n" +
				"    X.Inner<Integer> d = new X.Inner<Integer>();    \n" +
				"  }\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159 - variation
	public void test449() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  class Inner<B> { \n" +
				"  }\n" +
				"\n" +
				"  void method() {\n" +
				"    X<String>.Inner<Integer> d4 = new X.Inner<Integer>();\n" +
				"  }\n" +
				"}\n" ,
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	X<String>.Inner<Integer> d4 = new X.Inner<Integer>();\n" +
			"	                              ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X.Inner<Integer> needs unchecked conversion to conform to X<String>.Inner<Integer>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	X<String>.Inner<Integer> d4 = new X.Inner<Integer>();\n" +
			"	                                  ^^^^^^^\n" +
			"The member type X.Inner<Integer> must be qualified with a parameterized type, since it is not static\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159 - variation
	public void test450() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  static class Inner<B> { \n" +
				"  }\n" +
				"\n" +
				"  void method() {\n" +
				"    X<String>.Inner<Integer> d4 = new X<String>.Inner<Integer>();\n" +
				"  }\n" +
				"}\n" ,
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\r\n" +
			"	X<String>.Inner<Integer> d4 = new X<String>.Inner<Integer>();\r\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"The member type X<String>.Inner cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type X<String>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\r\n" +
			"	X<String>.Inner<Integer> d4 = new X<String>.Inner<Integer>();\r\n" +
			"	                                  ^^^^^^^^^^^^^^^\n" +
			"The member type X<String>.Inner cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type X<String>\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82159 - variation
	public void test451() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"  class Inner<B> { \n" +
				"  }\n" +
				"\n" +
				"  void method() {\n" +
				"    X<String>.Inner<Integer> d4 = new X<String>.Inner<Integer>() {};\n" +
				"  }\n" +
				"}\n" ,
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	X<String>.Inner<Integer> d4 = new X<String>.Inner<Integer>() {};\n" +
			"	                                  ^^^^^^^^^^^^^^^\n" +
			"Cannot allocate the member type X<String>.Inner<Integer> using a parameterized compound name; use its simple name and an enclosing instance of type X<String>\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82187
	public void test452() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	\n" +
				"	 public <E extends Object, S extends Collection<E>> S test1(S param){\n" +
				"	 	System.out.println(\"SUCCESS\");\n" +
				"	 	return null;\n" +
				"	 }\n" +
				"	 \n" +
				"	 public void test2() {\n" +
				"	 	test1(new Vector<String>());\n" +
				"	 }\n" +
				"\n" +
				"	 public static void main(String[] args) {\n" +
				"		new X().test2();\n" +
				"	}\n" +
				"}\n" ,
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82250
	public void test453() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends I & I> {}\n" +
				"interface I {}\n" ,
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\r\n" +
			"	public class X<T extends I & I> {}\r\n" +
			"	                             ^\n" +
			"Duplicate bound I\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82504
	public void test454() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, U extends X> {\n" +
				"	Object[] objectArr;\n" +
				"	void foo(T t) {\n" +
				"		T x1= (T) objectArr;\n" +
				"		U x2= (U) objectArr;\n" +
				"		int[] x= (int[]) t;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	T x1= (T) objectArr;\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"Type safety: The cast from Object[] to T is actually checking against the erased type Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	U x2= (U) objectArr;\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"Cannot cast from Object[] to U\n" +
			"----------\n");
	}


	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81719
	public void test455() {
		this.runConformTest(
			new String[] {
				"AbstractTest.java",
				"public abstract class AbstractTest<T> {\n" +
				"  abstract void array(T[] a);\n" +
				"  abstract void type(T a);\n" +
				"  abstract T[] foo();\n" +
				"}\n",
			},
			"");

		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test<T> extends AbstractTest<T> {\n" +
				"  void array(T[] a) {}\n" +
				"  void type(T a) {}\n" +
				"  T[] foo() { return null; }\n" +
				"}\n",
			},
			"",
			null,
			false, // do not flush output
			null);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81721
	public void test456() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I<T> {\n" +
				"	<S extends T> void doTest(S[] a);\n" +
				"}\n" +
				"\n" +
				"abstract class AbstractTest<U> implements I<U> {\n" +
				"	public <V extends U> void doTest(V[] a) {}\n" +
				"}\n" +
				"\n" +
				"public class X<M> extends AbstractTest<M> {}\n",
			},
			"");
	}

	public void test457() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	\n" +
				" void add(List<? super X> l) { \n" +
				" 	l.add(new X()); \n" +
				" }\n" +
				" void add2(List<? extends X> l) { \n" +
				" 	l.add(new X()); \n" +
				" }\n" +
				" \n" +
				" static <T> void add3(List<T> l, List<T> l2) { \n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"	List<X> lx = null;\n" +
				"	List<String> ls = null;\n" +
				"	add3(lx, ls);\n" +
				" } \n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	l.add(new X()); \n" +
			"	^^^^^^^^^^^^^^\n" +
			"Bound mismatch: The method add(? extends X) of type List<? extends X> is not applicable for the arguments (X). The wildcard parameter ? extends X has no lower bound, and may actually be more restrictive than argument X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 17)\n" +
			"	add3(lx, ls);\n" +
			"	^^^^\n" +
			"The method add3(List<T>, List<T>) in the type X is not applicable for the arguments (List<X>, List<String>)\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82243
	public void test458() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface A<E>{\n" +
				"	E getOne();\n" +
				"}\n" +
				"\n" +
				"\n" +
				"abstract class B<T extends Number> implements A<T> {\n" +
				"	Number getTwo() {\n" +
				"		return getOne(); // succeeds\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"abstract class C extends B<Integer> {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(A a, B b, C c){\n" +
				"		Object o= a.getOne();\n" +
				"		Number n1= b.getOne(); // fails\n" +
				"		Number n2= b.getTwo(); // succeeds, but inlining fails\n" +
				"		Integer i = c.getOne(); // succeeds\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 18)\n" +
			"	Number n1= b.getOne(); // fails\n" +
			"	       ^^\n" +
			"Type mismatch: cannot convert from Object to Number\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78027 - variation (check unchecked warnings)
	public void _test459() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X \n" +
				"{\n" +
				"Zork z;\n" +
				"}\n" +
				"\n" +
				"interface ITest<C extends X>\n" +
				"{ \n" +
				"}\n" +
				"\n" +
				"abstract class Test<C extends X> implements ITest<C>\n" +
				"{\n" +
				"  protected Manager<C> m_manager;\n" +
				"  \n" +
				"  public ITest<C> get()\n" +
				"  {\n" +
				"    return m_manager.getById(getClass(), new Integer(1));\n" +
				"  }\n" +
				"    \n" +
				"  public static class Manager<C extends X>\n" +
				"  {\n" +
				"    public <T extends ITest<C>> T getById(Class<T> cls, Integer id)\n" +
				"    {\n" +
				"      return null;\n" +
				"    }\n" +
				"  }\n" +
				"}\n"
			},
			"should be 2 unchecked warnings"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82439
	public void test460() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public <E extends Object, S extends Collection<E>> S test(S param) {\n" +
				"		\n" +
				"		Class<? extends Collection> c = param.getClass(); // ok\n" +
				"		Class<? extends Collection> d = getClazz(); // ko\n" +
				"		return null;\n" +
				"	}\n" +
				"	Class<? extends Object> getClazz() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"abstract class Z implements Collection<String> {\n" +
				"	void foo() {\n" +
				"		Class<? extends Collection> c = getClass(); // ok\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Class<? extends Collection> d = getClazz(); // ko\n" +
			"	                            ^\n" +
			"Type mismatch: cannot convert from Class<? extends Object> to Class<? extends Collection>\n" +
			"----------\n"	);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82844
	public void test461() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends int[]> {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<T extends int[]> {\n" +
			"	                         ^^^^^\n" +
			"The array type int[] cannot be used as a type parameter bound\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79628
	public void test462() {
		this.runConformTest(
			new String[] {
				"PropertiedObject.java",
				"interface PropertiedObject<B extends PropertiedObject<B>> {}\n" +
				"interface Model extends PropertiedObject<Model> {}\n" +
				"interface View<T extends Model,U> extends PropertiedObject<View<?,?>> {}\n"
			},
			"");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79144
	public void test463() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Set;\n" +
				"public class X {\n" +
				"   Zork z;\n" +
				"	public Set<String>[] test() {\n" +
				"	   Set[] sets = new Set[10];\n" +
				"	   return sets;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	Zork z;\r\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\r\n" +
			"	return sets;\r\n" +
			"	       ^^^^\n" +
			"Type safety: The expression of type Set[] needs unchecked conversion to conform to Set<String>[]\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79144
	public void test464() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"    Zork z;\n" +
				"    public static void main(String[] args) {\n" +
				"        List<Integer>[] nums = new List[] {Collections.singletonList(\"Uh oh\")};\n" +
				"        System.out.println(nums[0].get(0).intValue());\n" +
				"    } \n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\r\n" +
			"	Zork z;\r\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\r\n" +
			"	List<Integer>[] nums = new List[] {Collections.singletonList(\"Uh oh\")};\r\n" +
			"	                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type List[] needs unchecked conversion to conform to List<Integer>[]\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82547
	public void test465() {
		this.runNegativeTest(
			new String[] {
				"Cla.java",
				"class Cla<T> {\n" +
				"    T getT() {\n" +
				"        return null;\n" +
				"    }\n" +
				"    \n" +
				"    void m() {\n" +
				"        String s= new Cla<String>.getT();\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Cla.java (at line 7)\n" +
			"	String s= new Cla<String>.getT();\n" +
			"	              ^^^^^^^^^^^^^^^^\n" +
			"Cla.getT cannot be resolved to a type\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83096
	public void test466() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A, A> { }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<A, A> { }\n" +
			"	                  ^\n" +
			"Duplicate type parameter A\n" +
			"----------\n"
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82671
	public void test467() {
		this.runConformTest(
			new String[] {
				"test/Foo.java",
				"package test; \n" +
				"public class Foo { \n" +
				"   protected String s; \n" +
				"   protected String dosomething(){ return \"done\"; } \n" +
				"   protected class Bar {} \n" +
				"} \n",
				"test2/FooBar.java",
				"package test2; \n" +
				"import test.Foo; \n" +
				"public class FooBar<R> extends Foo { \n" +
				"   void fail() { \n" +
				"      FooBar f = new FooBar(); \n" +
				"      f.s = \"foo\"; \n" +
				"      this.s = \"foo\";\n" +
				"      f.dosomething(); \n" +
				"      this.dosomething();  \n" +
				"      Bar b1; \n" +
				"      FooBar.Bar b2; \n" +
				"      Foo.Bar b3; \n" +
				"   } \n" +
				"}\n"
			},
			""
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82671 - variation
	public void test468() {
		this.runConformTest(
			new String[] {
				"test/Foo.java",
				"package test; \n" +
				"public class Foo { \n" +
				"   String s; \n" +
				"   String dosomething(){ return \"done\"; } \n" +
				"   class Bar {} \n" +
				"} \n",
				"test/FooBar.java",
				"package test; \n" +
				"import test.Foo; \n" +
				"public class FooBar<R> extends Foo { \n" +
				"   void fail() { \n" +
				"      FooBar f = new FooBar(); \n" +
				"      f.s = \"foo\"; \n" +
				"      this.s = \"foo\";\n" +
				"      f.dosomething(); \n" +
				"      this.dosomething();  \n" +
				"      Bar b1; \n" +
				"      FooBar.Bar b2; \n" +
				"      Foo.Bar b3; \n" +
				"   } \n" +
				"}\n"
			},
			""
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83083
	public void _test469() {
		this.runConformTest(
			new String[] {
				"a/C.java",
				"package a; \n" +
				"import p.B; \n" +
				"public class C extends B { \n" +
				"	public void foo(Object obj) {} \n" +
				"} \n",
				"p/B.java",
				"package p; \n" +
				"public class B<E> extends A<E> {} \n",
				"p/A.java",
				"package p; \n" +
				"public class A<E> { \n" +
				"	public void foo(E e) {} \n" +
				"}\n",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"a/C.java",
				"package a; \n" +
				"import p.B; \n" +
				"public class C extends B { \n" +
				"	public void foo(Object obj) {} \n" +
				"} \n",
				"p/A.java",
				"package p; \n" +
				"public class A<E> { \n" +
				"	public void foo(E e) {} \n" +
				"}\n",
			},
			"",
			null,
			false, // do not flush output
			null);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83225
	public void test470() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static <T> T choose(boolean b, T t1, T t2) {\n" +
				"		if (b)\n" +
				"			return t1;\n" +
				"		return t2;\n" +
				"	}\n" +
				"\n" +
				"	public static void foo() {\n" +
				"		Comparable s1 = choose(true, \"string\", new Integer(1));\n" +
				"		Number s2 = choose(true, new Integer(1), new Float(2));\n" +
				"		Comparable s3 = choose(true, new Integer(1), new Float(2));\n" +
				"		Cloneable s4 = choose(true, new Integer(1), new Float(2));\n" +
				"		Cloneable s5 = choose(true, \"string\", new Integer(1));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Cloneable s4 = choose(true, new Integer(1), new Float(2));\n" +
			"	          ^^\n" +
			"Type mismatch: cannot convert from Number&Comparable<?> to Cloneable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	Cloneable s5 = choose(true, \"string\", new Integer(1));\n" +
			"	          ^^\n" +
			"Type mismatch: cannot convert from Object&Serializable&Comparable<?> to Cloneable\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82671 - variation
	public void test471() {
		this.runNegativeTest(
			new String[] {
				"test/Foo.java",
				"package test; \n" +
				"public class Foo<R> { \n" +
				"   protected R s; \n" +
				"   protected R dosomething(){ return s; } \n" +
				"   protected class Bar {} \n" +
				"} \n",
				"test2/FooBar.java",
				"package test2; \n" +
				"import test.Foo; \n" +
				"public class FooBar<R> extends Foo<R> { \n" +
				"   void fail() { \n" +
				"      FooBar<String> f = new FooBar<String>(); \n" +
				"      f.s = \"foo\"; \n" +
				"      this.s = \"foo\";\n" +
				"      f.dosomething(); \n" +
				"      this.dosomething();  \n" +
				"      Bar b1; \n" +
				"      FooBar<String>.Bar b2; \n" +
				"      Foo<String>.Bar b3; \n" +
				"   } \n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in test2\\FooBar.java (at line 7)\r\n" +
			"	this.s = \"foo\";\r\n" +
			"	         ^^^^^\n" +
			"Type mismatch: cannot convert from String to R\n" +
			"----------\n"	);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82671 - variation
	public void test472() {
		this.runNegativeTest(
			new String[] {
				"test/Foo.java",
				"package test; \n" +
				"public class Foo<R> { \n" +
				"   private R s; \n" +
				"   private R dosomething(){ return s; } \n" +
				"   private class Bar {} \n" +
				"} \n",
				"test2/FooBar.java",
				"package test2; \n" +
				"import test.Foo; \n" +
				"public class FooBar<R> extends Foo<R> { \n" +
				"   void fail() { \n" +
				"      FooBar<String> f = new FooBar<String>(); \n" +
				"      f.s = \"foo\"; \n" +
				"      this.s = \"foo\";\n" +
				"      f.dosomething(); \n" +
				"      this.dosomething();  \n" +
				"      Bar b1; \n" +
				"      FooBar<String>.Bar b2; \n" +
				"      Foo<String>.Bar b3; \n" +
				"   } \n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in test\\Foo.java (at line 4)\n" +
			"	private R dosomething(){ return s; } \n" +
			"	          ^^^^^^^^^^^^^\n" +
			"The private method dosomething() from the type Foo<R> is never used locally\n" +
			"----------\n" +
			"2. WARNING in test\\Foo.java (at line 5)\n" +
			"	private class Bar {} \n" +
			"	              ^^^\n" +
			"The private type Foo<R>.Bar is never used locally\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in test2\\FooBar.java (at line 6)\n" +
			"	f.s = \"foo\"; \n" +
			"	^^^\n" +
			"The field f.s is not visible\n" +
			"----------\n" +
			"2. ERROR in test2\\FooBar.java (at line 7)\n" +
			"	this.s = \"foo\";\n" +
			"	^^^^^^\n" +
			"The field s is not visible\n" +
			"----------\n" +
			"3. ERROR in test2\\FooBar.java (at line 8)\n" +
			"	f.dosomething(); \n" +
			"	  ^^^^^^^^^^^\n" +
			"The method dosomething() from the type Foo<String> is not visible\n" +
			"----------\n" +
			"4. ERROR in test2\\FooBar.java (at line 9)\n" +
			"	this.dosomething();  \n" +
			"	     ^^^^^^^^^^^\n" +
			"The method dosomething() from the type Foo<R> is not visible\n" +
			"----------\n" +
			"5. ERROR in test2\\FooBar.java (at line 10)\n" +
			"	Bar b1; \n" +
			"	^^^\n" +
			"The type Bar is not visible\n" +
			"----------\n" +
			"6. ERROR in test2\\FooBar.java (at line 11)\n" +
			"	FooBar<String>.Bar b2; \n" +
			"	^^^^^^^^^^^^^^^^^^\n" +
			"The type FooBar.Bar is not visible\n" +
			"----------\n" +
			"7. ERROR in test2\\FooBar.java (at line 12)\n" +
			"	Foo<String>.Bar b3; \n" +
			"	^^^^^^^^^^^^^^^\n" +
			"The type Foo.Bar is not visible\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=81594
	public void test473() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X\n" +
				"{\n" +
				"	List<B> itsList;\n" +
				"	B itsB;\n" +
				"	MyTyped itsTyped;\n" +
				"	\n" +
				"	\n" +
				"	public void test()\n" +
				"	{\n" +
				"		method (itsList, itsB, itsTyped);\n" +
				"	}\n" +
				"	\n" +
				"	public <T> void method (List<? extends T> arg1, T arg2, Typed<? super T> arg3)\n" +
				"	{\n" +
				"	}\n" +
				"	\n" +
				"	interface A{}\n" +
				"	class B implements A{}\n" +
				"	class Typed<T>{}\n" +
				"	class MyTyped extends Typed<A>{}\n" +
				"\n" +
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=81594 - variation
	public void test474() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	Typed<B> itsList;\n" +
				"	Typed<A> itsTyped;\n" +
				"	public void test() {\n" +
				"		method(itsList, itsTyped);\n" +
				"	}\n" +
				"	public <T> void method(Typed<? extends T> arg1, Typed<? super T> arg3) {\n" +
				"	}\n" +
				"	interface A {\n" +
				"	}\n" +
				"	class B implements A {\n" +
				"	}\n" +
				"	class Typed<T> {\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398
	public void test475() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"    void method(List<? super Number> list) {\n" +
				"        list.add(new Object());   // should fail\n" +
				"        list.add(new Integer(3)); // correct\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	list.add(new Object());   // should fail\n" +
			"	     ^^^\n" +
			"The method add(? super Number) in the type List<? super Number> is not applicable for the arguments (Object)\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test476() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"    void method(List<? super Number> list, List<Object> lo) {\n" +
				"    	list = lo;\n" +
				"    	lo = list;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	lo = list;\n" +
			"	     ^^^^\n" +
			"Type mismatch: cannot convert from List<? super Number> to List<Object>\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test477() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<T extends Number> {\n" +
				"	List<? super T> lhs;\n" +
				"	List<? extends Number> rhs;\n" +
				"	{\n" +
				"		lhs.add(rhs.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	lhs.add(rhs.get(0));\n" +
			"	    ^^^\n" +
			"The method add(? super T) in the type List<? super T> is not applicable for the arguments (? extends Number)\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test478() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<U extends Number> {\n" +
				"	List<? super Number> lhs;\n" +
				"	List<? super U> rhs;\n" +
				"	{\n" +
				"		lhs.add(rhs.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	lhs.add(rhs.get(0));\n" +
			"	    ^^^\n" +
			"The method add(? super Number) in the type List<? super Number> is not applicable for the arguments (? super U)\n" +
			"----------\n");
	}


	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test479() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<U extends Number> {\n" +
				"	List<? super Number> lhs;\n" +
				"	List<? extends U> rhs;\n" +
				"	{\n" +
				"		lhs.add(rhs.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test480() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<U extends Number> {\n" +
				"	List<? super Integer> lhs;\n" +
				"	List<? extends Number> rhs;\n" +
				"	{\n" +
				"		lhs.add(rhs.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	lhs.add(rhs.get(0));\n" +
			"	    ^^^\n" +
			"The method add(? super Integer) in the type List<? super Integer> is not applicable for the arguments (? extends Number)\n" +
			"----------\n");
	}


	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83398 - variation
	public void test481() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<U extends Number> {\n" +
				"	List<? super Number> lhs;\n" +
				"	List<? super Integer> rhs;\n" +
				"	{\n" +
				"		lhs.add(rhs.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	lhs.add(rhs.get(0));\n" +
			"	    ^^^\n" +
			"The method add(? super Number) in the type List<? super Number> is not applicable for the arguments (? super Integer)\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83799
	public void test482() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public final class X {\n" +
				"	public <T> void testEquals(final String x, T one, T two) {\n" +
				"	}\n" +
				"\n" +
				"	public <T1, T2> void testEqualsAlt(final String x, T1 one, T2 two) {\n" +
				"	}\n" +
				"\n" +
				"	public interface Fooey {\n" +
				"	}\n" +
				"\n" +
				"	public interface Bar extends Fooey {\n" +
				"	}\n" +
				"\n" +
				"	public interface GenericFooey<T> {\n" +
				"	}\n" +
				"\n" +
				"	public interface GenericBar<T> extends GenericFooey<T> {\n" +
				"	}\n" +
				"\n" +
				"	public void testGeneric() {\n" +
				"		testEquals(\"Should work\", new GenericBar<Long>() {\n" +
				"		}, new GenericBar<Long>() {\n" +
				"		});\n" +
				"		final GenericBar<Long> child = new GenericBar<Long>() {\n" +
				"		};\n" +
				"		final GenericFooey<Long> parent = child;\n" +
				"		testEquals(\"Doesn\'t work but should\", child, parent); // this\n" +
				"		// fails\n" +
				"		// but should work it\'s identical to next line.\n" +
				"		testEquals(\"Doesn\'t work but should\", (GenericFooey<Long>) child, parent);\n" +
				"		testEqualsAlt(\"Should work\", child, parent);\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		testEquals(\"Should work\", new Bar() {\n" +
				"		}, new Bar() {\n" +
				"		});\n" +
				"		final Bar child = new Bar() {\n" +
				"		};\n" +
				"		final Fooey parent = child;\n" +
				"		testEquals(\"Doesn\'t work but should\", child, parent);\n" +
				"		testEquals(\"Doesn\'t work but should\", (Fooey) child, parent);\n" +
				"		testEqualsAlt(\"Should work\", child, parent);\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83904
	public void test483() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Y<T extends Number> {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String argv[]) {\n" +
				"        m(new Y<Short>(), new Y<Integer>());\n" +
				"    }\n" +
				"\n" +
				"    public static <T extends Number> void m(Y<T> x, Y<T> y) {\n" +
				"    }\n" +
				"}\n" +
				"\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\r\n" +
			"	m(new Y<Short>(), new Y<Integer>());\r\n" +
			"	^\n" +
			"The method m(Y<T>, Y<T>) in the type X is not applicable for the arguments (Y<Short>, Y<Integer>)\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82349
	public void test484() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Base<T> {\n" +
				"	public class Inner {\n" +
				"	}\n" +
				"	Inner a;\n" +
				"}\n" +
				"\n" +
				"public class X extends Base<Integer> {\n" +
				"	class DerivedInner extends Inner {\n" +
				"	}\n" +
				"	X() {\n" +
				"		a = new DerivedInner();\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82349 - variation
	public void test485() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Base<T> {\n" +
				"	public class Inner<U> {\n" +
				"	}\n" +
				"	Inner a;\n" +
				"}\n" +
				"\n" +
				"public class X extends Base<Integer> {\n" +
				"	class DerivedInner extends Inner {\n" +
				"	}\n" +
				"	X() {\n" +
				"		a = new DerivedInner();\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82349 - variation
	public void test486() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Base<T> {\n" +
				"	public class Inner<U> {\n" +
				"	}\n" +
				"	Inner a;\n" +
				"}\n" +
				"\n" +
				"public class X extends Base<Integer> {\n" +
				"	class DerivedInner extends Inner<Float> {\n" +
				"	}\n" +
				"	X() {\n" +
				"		a = new DerivedInner();\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
	public void test487() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(List<String> ls) {\n" +
				"		List<?> l = ls;\n" +
				"		bar(l, \"\"); \n" +
				"	}\n" +
				"	<T> void bar(List<? super T> l, T t) {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	bar(l, \"\"); \n" +
			"	^^^\n" +
			"The method bar(List<? super T>, T) in the type X is not applicable for the arguments (List<?>, String)\n" +
			"----------\n");
	}
	public void _test488() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Foo<?> f1 = new Foo<Integer>();\n" +
				"        Foo<?> f2 = new Foo<String>();\n" +
				"        f1.bar = f2.bar;\n" +
				"    }\n" +
				"    static class Foo<T> {\n" +
				"       Bar<T> bar = new Bar<T>();\n" +
				"    }\n" +
				"    static class Bar<T> {\n" +
				"        T t;\n" +
				"    }\n" +
				"}\n"
			},
			"should report type mismatch");
	}
	public void _test489() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Foo<?> f1 = new Foo<Integer>();\n" +
				"        f1.bar = f1.bar;\n" +
				"    }\n" +
				"    static class Foo<T> {\n" +
				"       Bar<T> bar = new Bar<T>();\n" +
				"    }\n" +
				"    static class Bar<T> {\n" +
				"        T t;\n" +
				"    }\n" +
				"}\n"
			},
			"should report type mismatch?!");
	}
	public void test490() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	T t;\n" +
				"	void foo(X<?> lhs, X<?> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void bar(X<X<?>> lhs, X<X<?>> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}}\n" +
				"\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	lhs.t = rhs.t;\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from ? to ?\n" +
			"----------\n");
	}

	public void test491() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	T t;\n" +
				"	void foo(X<?> lhs, X<?> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void bar(X<X<?>> lhs, X<X<?>> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void baz(X<? super Number> lhs, X<? extends Number> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void baz2(X<? extends Number> lhs, X<? extends Number> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void baz3(X<? extends Number> lhs, X<? super Number> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"	void baz4(X<? super Number> lhs, X<? super Number> rhs) {\n" +
				"		lhs = rhs;\n" +
				"		lhs.t = rhs.t;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	lhs.t = rhs.t;\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from ? to ?\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	lhs = rhs;\n" +
			"	      ^^^\n" +
			"Type mismatch: cannot convert from X<? extends Number> to X<? super Number>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 17)\n" +
			"	lhs.t = rhs.t;\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from ? extends Number to ? extends Number\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 20)\n" +
			"	lhs = rhs;\n" +
			"	      ^^^\n" +
			"Type mismatch: cannot convert from X<? super Number> to X<? extends Number>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 21)\n" +
			"	lhs.t = rhs.t;\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from ? super Number to ? extends Number\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 25)\n" +
			"	lhs.t = rhs.t;\n" +
			"	        ^^^^^\n" +
			"Type mismatch: cannot convert from ? super Number to ? super Number\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81576
	public void test492() {
		this.runConformTest(
			new String[] {
				"SuperType.java",//====================================
				"public class SuperType<T> {\n" +
				"	protected InnerType valueWrapper;\n" +
				"	protected class InnerType {\n" +
				"		private T value;\n" +
				"		protected InnerType(T value) {\n" +
				"			this.value = value;\n" +
				"		}\n" +
				"	}\n" +
				"	public SuperType(T value) {\n" +
				"		/*\n" +
				"		 * This constructor exists only to show that the usage of the inner\n" +
				"		 * class within its enclosing class makes no problems\n" +
				"		 */\n" +
				"		this.valueWrapper = new InnerType(value);\n" +
				"	}\n" +
				"	protected SuperType() {\n" +
				"		// Provided for the convenience of subclasses\n" +
				"	}\n" +
				"}\n",
				"SubType.java",//====================================
				"public class SubType<T> extends SuperType<T> {\n" +
				"\n" +
				"	public SubType(T value) {\n" +
				"\n" +
				"		/* The constructor SuperType <T>.InnerType(T) is undefined */\n" +
				"		InnerType localValueWrapper = new InnerType(value);\n" +
				"\n" +
				"		/*\n" +
				"		 * Type mismatch: cannot convert from SuperType <T>.InnerType to\n" +
				"		 * SuperType <T>.InnerType\n" +
				"		 * \n" +
				"		 * Type safety: The expression of raw type SuperType.InnerType is\n" +
				"		 * converted to SuperType <T>.InnerType. References to generic type\n" +
				"		 * SuperType <T>.InnerType should be parametrized.\n" +
				"		 */\n" +
				"		localValueWrapper = super.valueWrapper;\n" +
				"	}\n" +
				"\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83611
	public void test493() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public class M<T> { M(Class<T> templateClass) {} }\n" +
				"}\n",
				"Y.java",
				"public class Y extends X {\n" +
				"	void test() { M<X> m = new M<X>(X.class); }\n" +
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"Y.java",
				"public class Y extends X {\n" +
				"	void test() { M<X> m = new M<X>(X.class); }\n" +
				"}\n"
			},
			"",
			null,
			false,
			null
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83615
	public void test494() {
		this.runNegativeTest(
			new String[] {
				"X.java",//====================================
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Number n= null;\n" +
				"		Integer i= null;\n" +
				"		new X().nextTry(i, n);\n" +
				"		new X().nextTry2(n, i);\n" +
				"	}	\n" +
				"	\n" +
				"	<I, N extends I> void nextTry(I i, N n) {}\n" +
				"	\n" +
				"	<N, I extends N> void nextTry2(N n, I i) {}	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new X().nextTry(i, n);\n" +
			"	        ^^^^^^^\n" +
			"Bound mismatch: The generic method nextTry(I, N) of type X is not applicable for the arguments (Integer, Number) since the type Number is not a valid substitute for the bounded parameter <N extends I>\n" +
			"----------\n");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84422
	public void test495() {
		this.runConformTest(
			new String[] {
				"X.java",//====================================
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	List l= null; \n" +
				"\n" +
				"	void add(String s) {\n" +
				"		l.add(s);\n" +
				"	}\n" +
				"	\n" +
				"	void addAll(String[] ss) {\n" +
				"		l.addAll(Arrays.asList(ss));\n" +
				"	}\n" +
				"	\n" +
				"	String[] get() {\n" +
				"		return (String[])l.toArray(new String[l.size()]);\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84593
	public void test496() {
		this.runConformTest(
			new String[] {
				"X.java",//====================================
				"class Super<S> {\n" +
				"	class A<E> { }\n" +
				"	<T> void take(A<S> o) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n" +
				"class Sub extends Super<Double> {\n" +
				"	void test() {\n" +
				"		take(new A());\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		new Sub().test();\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84593 - variation - uncheck warnings
	public void test497() {
		this.runNegativeTest(
			new String[] {
				"X.java",//====================================
				"class Super<S> {\n" +
				"	class A<E> { }\n" +
				"	<T> void take(A<S> o) {\n" +
				"	}\n" +
				"}\n" +
				"class Sub extends Super<Double> {\n" +
				"	void test() {\n" +
				"		take(new A());\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		new Sub().test();\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	take(new A());\n" +
			"	     ^^^^^^^\n" +
			"Type safety: The expression of type Super<Double>.A needs unchecked conversion to conform to Super<Double>.A<Double>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84743 - variation in -source 1.4 mode but 1.5 compliance (ignore covariance)
public void test498(){
//	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"   String foo();\n" +
			"}\n" +
			"interface J {\n" +
			"   Object foo();\n" +
			"}\n" +
			" \n" +
			"public class X implements I {\n" +
			"   public String foo() {\n" +
			" 	return \"\";\n" +
			"   }\n" +
			"   public static void main(String[] args) {\n" +
			"         I i = new X();\n" +
			"         try {\n" +
			"	        J j = (J) i;\n" +
			"         } catch(ClassCastException e) {\n" +
			"	        System.out.println(\"SUCCESS\");\n" +
			"         }\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	J j = (J) i;\n" +
		"	      ^^^^^\n" +
		"Cannot cast from I to J\n" +
		"----------\n",
		null,
		true,
		null/*customOptions*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85157
public void test499(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		 public static void main(String argv[]) {\n" +
			"		 		 String[] tab1 = new String[0];\n" +
			"		 		 Integer[] tab2 = new Integer[0];\n" +
			"		 		 boolean cond = true;\n" +
			"		 		 Integer[] var = cond ? tab1 : tab2;\n" +
			"		 		 System.out.println(var);\n" +
			"		 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	Integer[] var = cond ? tab1 : tab2;\n" +
		"	          ^^^\n" +
		"Type mismatch: cannot convert from Object&Serializable&Cloneable to Integer[]\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84251
public void test500(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Collection;\n" +
			"\n" +
			"interface Sink<T> { \n" +
			"	void flush(T t);\n" +
			"}\n" +
			"class SimpleSinkImpl<T> implements Sink<T> {\n" +
			"	public void flush(T t) {}\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"    private <T> T writeAll(Collection<T> coll, Sink<? super T> snk) { \n" +
			"        T last = null;\n" +
			"        for (T t : coll) { \n" +
			"            last = t;\n" +
			"            snk.flush(last);\n" +
			"        }\n" +
			"        return last;\n" +
			"    }\n" +
			"\n" +
			"    public void test1() {\n" +
			"        Sink<Object> s = new SimpleSinkImpl<Object>();\n" +
			"        Collection<String> cs = new ArrayList<String>();\n" +
			"        cs.add(\"hello!\");\n" +
			"        cs.add(\"goodbye\");\n" +
			"        cs.add(\"see you\");\n" +
			"        \n" +
			"        String str = this.writeAll(cs, s);  \n" +
			"    }\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        X test = new X();\n" +
			"        \n" +
			"        test.test1();\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
}
