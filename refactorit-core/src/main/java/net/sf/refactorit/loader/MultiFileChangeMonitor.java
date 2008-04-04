/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.vfs.FileChangeListener;
import net.sf.refactorit.vfs.FileChangeMonitor;


/**
 *
 * @author  yuri
 */
public final class MultiFileChangeMonitor extends net.sf.refactorit.vfs.
    FileChangeMonitor {
  private final FileChangeMonitor[] others;

  public MultiFileChangeMonitor(FileChangeMonitor[] others) {
    this.others = others;
  }

  public void addFileChangeListener(FileChangeListener listener) {
    for (int i = 0; i < others.length; i++) {
      others[i].addFileChangeListener(listener);
    }
  }

  public void removeFileChangeListener(FileChangeListener listener) {
    for (int i = 0; i < others.length; i++) {
      others[i].removeFileChangeListener(listener);
    }
  }

  public void trigger(net.sf.refactorit.classmodel.Project project) {
    for (int i = 0; i < others.length; i++) {
      others[i].trigger(project);
    }
  }

  public boolean hasPossiblePendingEvents() {
    boolean result = false;

    for (int i = 0; i < others.length; i++) {
      if (others[i].hasPossiblePendingEvents()) {
        result = true;
        break;
      }
    }

    return result;
  }
}
