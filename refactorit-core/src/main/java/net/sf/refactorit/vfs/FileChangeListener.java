/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.vfs;

/**
 * Callback interface for IDE specific file change listeners.
 *
 * @author  Yuri Schimke
 */
public interface FileChangeListener {
  void fileCreated(Source source);

  void fileDeleted(Source source);

  void fileContentsChanged(Source source);

  void fileRenamed(Source newSource, String oldName);

  void unknownChangesHappened();
}
