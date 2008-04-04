/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;

import junit.framework.Test;
import junit.framework.TestSuite;


public class StaticImportTest extends AbstractRegressionTest {

  public StaticImportTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(StaticImportTest.class);
  }

  public void test001() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import static java.lang.Math.*;\n" +
        "import static java.lang.Double.*;\n" +
				"import static java.awt.Color.BLACK;\n" +
				"import static java.awt.Color.getColor;\n" +
				"import java.awt.Color;\n" + 
        "\n" +
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        double a = cos(MIN_VALUE);\n" +
        "        int t = BLACK.getTransparency();\n" +
        "        Color c = getColor(\"blue\");\n" +
        "        Color c2 = getColor(\"foo\", 42);\n" +
        "    }\n" +
        "}\n",
    },
        "SUCCESS");
  }
}
