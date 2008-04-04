/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.Project;

import java.lang.ref.WeakReference;


/**
 * @author Arseni Grigorjev
 */
public abstract class CacheableReference extends BinItemReference {

  private WeakReference weakReference;
  private long lastProjectRebuildTime;

  public CacheableReference(final Object itemToCache, final Project project) {
    this.lastProjectRebuildTime = getRebuildTime(project, itemToCache);
    this.weakReference = new WeakReference(itemToCache);
  }

  public Object restore(Project project){
    Object result = null;
    if (BinItemReference.cacheEnabled && weakReference != null){
      final Object fromWeakRef = weakReference.get();
      if (fromWeakRef != null && getRebuildTime(project, fromWeakRef)
          == lastProjectRebuildTime){
        result = fromWeakRef;
      }
    }
    
    if (result == null) {
      result = super.restore(project);
      if (result != null){
        updateCache(result, project);
      }
    }

    return result;
  }

  public void updateCache(final Object itemToCache, final Project proj){
    this.lastProjectRebuildTime = getRebuildTime(proj, itemToCache);
    this.weakReference = new WeakReference(itemToCache);
  }

  private long getRebuildTime(Project project, Object fromWeakRef){
    if (project != null){
      return project.getLastRebuilded();
    } else {
      // then we probably have to deal with Project reference
      try {
        return ((Project) fromWeakRef).getLastRebuilded();
      } catch (ClassCastException e){
        log.error("Project reference was not resolved properly.", e);
        return 0;
      }
    }
  }
}
