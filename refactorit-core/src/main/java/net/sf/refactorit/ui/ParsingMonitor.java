/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

/**
 * Listener on parsing thread events.
 *
 * @author Vladislav Vislogubov
 * @author Igor Malinin
 */
public interface ParsingMonitor {
  void parsingStarting();

  void parsingProgress(float percents);

  void parsingFinished(boolean error);
}
