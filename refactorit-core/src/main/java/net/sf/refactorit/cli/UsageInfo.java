/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.RuntimePlatform;



public class UsageInfo {
  public static final String HELP_PAGE =
      "\n" +
      "Usage: run.sh -nogui <options>\n\n" +
      "Where options include (some are optional):\n\n" +

      "-sourcepath <path>                   Project sourcepath (default='.')\n" +
      "-classpath <path>                    Project classpath (default='.')\n" +
      "-format [html|text|comma-separated]  Output format (default='text')\n" +
      "-profile <path>                      Audit or Metrics profile (opt)\n" +
      "-output <path>                       Output file path (default=stdout)\n" +
      "-metrics                             Runs Metrics on the project\n" +
      "-notused                             Runs NotUsed on the project\n" +
      "-audit                               Runs Audit on the project\n\n" +

      "Example: run Metrics under Windows:\n\n" +

      "run.bat -nogui -sourcepath c:\\temp -classpath c:\\temp\\x.jar c:\\temp\\y.jar -metrics\n" +
      "        -format comma-separated\n\n" +

      "For more info, see 'Command Line Interface' in RefactorIT help\n";

  public void show() {
    RuntimePlatform.console.println(
        StringUtil.replace(HELP_PAGE, "\n", StringUtil.NEWLINE));
  }
}
