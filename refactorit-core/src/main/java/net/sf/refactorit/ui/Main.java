/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui;

import net.sf.refactorit.standalone.StartUp;

/**
 * We need this class for background compatibility: if a user runs RefactorIT Updater
 * under standalone (2.0.x -> 2.5), the updater will update .jar-files but not 
 * the RefactorIT.exe (it would still want to start "net.sf.refactorit.ui.Main").
 */
public class Main {
      
  public static void main(String[] args) {
    StartUp.main(args);
  }
  
}
