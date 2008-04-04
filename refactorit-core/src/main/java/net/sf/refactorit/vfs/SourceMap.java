/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Contains file objects mapped to created Sources to avoid extra sources
 * creation.<br>
 * Note: Cannot be incorporated into SourcePath because may contain Sources of
 * the diffent types (e.g. LocalSource and NBSource) at the same time.
 * Probably, it is a bug in our architecture, but anyway it is this way for
 * historical reasons.
 *
 * @author Anton Safonov
 */
public final class SourceMap {
  private static final HashMap sources = new HashMap();

  public static final void addSource(Source source) {
    sources.put(source.getIdentifier(), source);
  }

  /**
   * @param toRemove
   */
  public static final void removeSource(Source toRemove) {
    removeSourceWithIdentifier(toRemove.getIdentifier());
  }

  public static void removeSourceWithIdentifier(Object identifier) {
    final Source removed = (Source) sources.remove(identifier);
    if (removed != null) {
      removed.invalidateCaches();
    }
  }

  public static final Source getSource(Object identifier) {
    final Source source = (Source) sources.get(identifier);

    if (source != null) {
      source.invalidateCaches(); // to be sure
    }
    return source;
  }

  public static final List getSourcesForName(final String name) {
    ArrayList result = new ArrayList();
    Iterator vals = sources.values().iterator();
    while (vals.hasNext()) {
      Source source = (Source) vals.next();
      if (name.equals(source.getName())) {
        result.add(source);
      }
    }

    return result;
  }

  public static final void clear() {
    //new Exception("SOURCE MAP.CLEAR called").printStackTrace(System.err);
    invalidateSourceCaches();
    sources.clear();
  }

  public static final void invalidateSourceCaches() {
    final Iterator it = sources.values().iterator();
    while (it.hasNext()) {
      ((Source) it.next()).invalidateCaches();
    }
  }

  public static final void removeNonExistingSources(Collection existingSources) {
    if (!(existingSources instanceof Set)) {
      existingSources = new HashSet(existingSources);
    }

    final Iterator it = sources.values().iterator();
    while (it.hasNext()) {
      Source source = (Source) it.next();
      if (!existingSources.contains(source)) {
        source.invalidateCaches();
        it.remove();
      }
    }
  }
}
