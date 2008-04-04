/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.common.util.Assert;


/**
 * @author Tonis Vaga
 */
public interface RefactorItConstants {

  boolean developingMode = Assert.enabled;

  /**
   * should not be enabled in release version!!!
   */
  boolean debugInfo = Assert.enabled;

  boolean runNotImplementedTests = false;

  public static final boolean alwaysIncrementalRebuild = true;
}
