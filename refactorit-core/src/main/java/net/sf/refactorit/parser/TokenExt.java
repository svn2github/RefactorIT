/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

/**
 * Interface for both AST and Token to have end coordinates.
 * @author Anton Safonov
 */
public interface TokenExt {
  int getLine();

  int getColumn();

  int getEndLine();

  void setEndLine(int endLine);

  int getEndColumn();

  void setEndColumn(int endColumn);
}
