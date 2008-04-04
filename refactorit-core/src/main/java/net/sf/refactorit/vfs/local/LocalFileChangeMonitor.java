/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.vfs.FileChangeMonitor;
import net.sf.refactorit.vfs.Source;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Monitors Source files for changes and fires events fo added, removed or changed files.
 * because ther is no IDE integration the events must be prompted with a call to
 * trigger().
 *
 * @author  Yuri Schimke
 */
public abstract class LocalFileChangeMonitor extends FileChangeMonitor {
  private final HashMap knownSources = new HashMap(200);
  private final Set tempSet = new HashSet(200);

  public LocalFileChangeMonitor(Collection initialSources) {
    Iterator i = initialSources.iterator();
    while (i.hasNext()) {
      Source file = (Source) i.next();
      knownSources.put(file, new Long(file.lastModified()));
    }
  }

  public final void trigger(Project project) {
    if (listeners.isEmpty()) {
      return;
    }

    HashMap previousSources = (HashMap) knownSources.clone();

    tempSet.clear();

    collectSources(tempSet);

    Iterator i = tempSet.iterator();

    while (i.hasNext()) {
      Source file = (Source) i.next();
      //System.err.println("checking: " + file);

      Long previousModTime = (Long) previousSources.remove(file);
      Long newModTime = new Long(file.lastModified());

      if (previousModTime == null) {
//        if (removeByRealHash(previousSources, file)) {
//          // probably, file was renamed, but source left the same
//          fireFileContentsChangedEvent(file);
//        } else {
        // added
        fireFileCreatedEvent(file);
//        }
      } else if (previousModTime.compareTo(newModTime) < 0) {
        // changed (newer mod time)
        fireFileContentsChangedEvent(file);
      } else if (previousModTime.compareTo(newModTime) > 0) {
        // changed (older mod time ??)
        // possibly could happen when file was updated to the older version outside of RefactorIT
        fireFileContentsChangedEvent(file);
      }

      // whatever happened this is the new correct time
      knownSources.put(file, newModTime);
    }

    Iterator j = previousSources.keySet().iterator();
    while (j.hasNext()) {
      Source deletedSource = (Source) j.next();
      knownSources.remove(deletedSource);

      // deleted
      fireFileDeletedEvent(deletedSource);
    }
  }

//  private boolean removeByRealHash(HashMap sources, Source file) {
//    final Iterator it = sources.entrySet().iterator();
//    while (it.hasNext()) {
//      final Map.Entry entry = (Map.Entry) it.next();
//      if (System.identityHashCode(entry.getKey()) == System.identityHashCode(file)) {
//        it.remove();
//        return true;
//      }
//    }
//
//    return false;
//  }

  /**
   * Get all sources that this monitor should be interested in.
   *
   * @param result the collection to store source files in.
   */
  protected abstract void collectSources(Collection result);

  public final boolean hasPossiblePendingEvents() {
    return true;
  }
}
