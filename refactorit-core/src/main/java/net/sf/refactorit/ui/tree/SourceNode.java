/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.source.SourceCoordinate;


public interface SourceNode extends UITreeNode {
  /**
   * Returns the CompilationUnit where its content came from.
   */
  CompilationUnit getCompilationUnit();

  /**
   * Returns the source's offset.
   */
  SourceCoordinate getStart();

  /**
   * Returns the source's end coordinate.
   */
  SourceCoordinate getEnd();
}
