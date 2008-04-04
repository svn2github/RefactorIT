/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;

import java.util.Arrays;
import java.util.List;


public interface SupportedArguments {
  public static final List ARGS = Arrays.asList(new String[] {
      "-nogui", "-sourcepath", "-classpath", "-format",
      "-notused", "-metrics", "-audit", "-output", "-profile"
  });

  public static final int NOGUI = 0;
  public static final int SOURCEPATH = 1;
  public static final int CLASSPATH = 2;
  public static final int FORMAT = 3;
  public static final int NOTUSED = 4;
  public static final int METRICS = 5;
  public static final int AUDIT = 6;
  public static final int OUTPUT = 7;
  public static final int PROFILE = 8;
}
