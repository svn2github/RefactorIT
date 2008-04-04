/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;


import net.sf.refactorit.classmodel.Project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract base class for IDE specific file change monitors.
 *
 * @author  yuri
 */
public abstract class FileChangeMonitor {
  protected final List listeners = new ArrayList(10);

  public FileChangeMonitor() {
  }

  public void addFileChangeListener(FileChangeListener listener) {
    if (listener == null) {
      throw new NullPointerException("listener == null");
    }

    listeners.add(listener);
  }

  public void removeFileChangeListener(FileChangeListener listener) {
    if (listener == null) {
      throw new NullPointerException("listener == null");
    }

    listeners.remove(listener);
  }

  protected final void fireFileCreatedEvent(Source source) {
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      FileChangeListener listener = (FileChangeListener) i.next();
      listener.fileCreated(source);
    }
  }

  protected final void fireFileDeletedEvent(Source source) {
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      FileChangeListener listener = (FileChangeListener) i.next();
      listener.fileDeleted(source);
    }
  }

  protected final void fireFileContentsChangedEvent(Source source) {
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      FileChangeListener listener = (FileChangeListener) i.next();
      listener.fileContentsChanged(source);
    }
  }

  protected final void fireFileRenameEvent(Source source, String oldName) {
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      FileChangeListener listener = (FileChangeListener) i.next();
      listener.fileRenamed(source, oldName);
    }
  }

  protected final void fireUnknownChangesHappened() {
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      FileChangeListener listener = (FileChangeListener) i.next();
      listener.unknownChangesHappened();
    }
  }

  public void trigger(Project project) {}

  public abstract boolean hasPossiblePendingEvents();
}
