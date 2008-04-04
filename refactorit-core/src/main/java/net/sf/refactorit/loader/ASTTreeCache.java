/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ASTTreeCache implements Serializable {
  /**
   * Serial Version ID.
   */
  private static final long serialVersionUID = 200502221212L;

  static final class CacheItem implements Serializable {
    long lastModified;
    Object payLoad;
  }


  private HashMap cacheRoots = new HashMap();

  private transient HashMap jspCacheRoots = new HashMap();

  public final void cleanAll() {
    cacheRoots = new HashMap();
    clearJspCache();
  }

  /**
   * @return new map
   */
  private HashMap clearJspCache() {
    return jspCacheRoots = new HashMap();
  }

  public final void removeNonExistingSources(final List existingSources) {
    final Set existings = new HashSet(existingSources.size());

    for (int i = 0; i < existingSources.size(); ++i) {
      existings.add(((Source) existingSources.get(i)).getIdentifier());
    }

//    final List toBeRemoved = new ArrayList();
//
//    for(Iterator i = cacheRoots.keySet().iterator() ; i.hasNext() ; ) {
//      final Object key = i.next();
//      if(!existings.contains(key)) {
//        toBeRemoved.add(key);
//      }
//    }
    //    if(toBeRemoved.size() > 0 ) {
//      for(int i = 0 ; i < toBeRemoved.size() ; ++i) {
//        cacheRoots.remove( toBeRemoved.get(i) );
//      }

    HashMap[] array = new HashMap[] {cacheRoots, jspCacheRoots};

    for (int index = 0; index < array.length; ++index) {
      if (array[index] == null) {
        continue;
      }
      Set entrySet = array[index].entrySet();
      Iterator iter = entrySet.iterator();

      for (; iter.hasNext(); ) {
        Map.Entry entry = (Map.Entry) iter.next();
        if (!existings.contains(entry.getKey())) {
          iter.remove();
        }
      }
    }

    SourceMap.removeNonExistingSources(existingSources);
  }

  final void removeSource(final Source source) {
    final Object key = source.getIdentifier();
    if (cacheRoots.remove(key) == null) {
      removeJspSource(source);
    }
  }

  final void removeJspSource(final Source source) {
    final Object key = source.getIdentifier();
    //DebugInfo.trace("removing jsp cache for "+source.getName());

    jspCacheRoots.remove(key);
  }

  final boolean isJspCacheEmpty() {
    return jspCacheRoots.isEmpty();
  }

  /**
   *   returns null if not in cache or has expired
   *   also when it is expired it will be removed from cache
   * @param source source
   * @return data
   */
  public final FileParsingData checkCacheFor(final Source source) {
    FileParsingData result = checkCacheFor(source, cacheRoots);

    if (result == null) {
      result = checkJSPCacheFor(source);
    }

    return result;
  }

  /**
   * @param source source
   * @return data
   */
  public final FileParsingData checkJSPCacheFor(final Source source) {
    return checkCacheFor(source, jspCacheRoots);
  }

  /**
   * @param source source
   * @param cacheSet cacheSet
   * @return data
   */
  private final FileParsingData checkCacheFor(final Source source,
      final HashMap cacheSet) {
    final Object key = source.getIdentifier();

    final CacheItem item = (CacheItem) cacheSet.get(key);
    if (item == null) {
      return null;
    }
//System.err.println("Checking cache for: " + source
//    + ", source.modified:" + new Date(source.lastModified())
//    + ", cache.modified: " + new Date(item.lastModified));

    if (source.lastModified() == item.lastModified
        // rebuild the cache if we played with the AST tree inside refactoring
        && ((FileParsingData) item.payLoad).astTree != null
        && !((FileParsingData) item.payLoad).astTree.isChanged()) {

      return (FileParsingData) item.payLoad;
    } else {
      cacheSet.remove(key);
    }

    return null;
  }

  public final void putToCache(final Source source, final FileParsingData data) {
    if (!(data.simpleComments instanceof UnmodifiableArrayList)) {
      data.simpleComments = new UnmodifiableArrayList(data.simpleComments);
      data.javadocComments = new UnmodifiableArrayList(data.javadocComments);
    }

    final CacheItem item = new CacheItem();
//    new Exception("putToCache: " + source + " - "
//        + new Date(source.lastModified())).printStackTrace();
    item.lastModified = source.lastModified();
    item.payLoad = data;

    cacheRoots.put(source.getIdentifier(), item);
  }

  public final void putToJspCache(final Source source,
      final FileParsingData data) {
    final CacheItem item = new CacheItem();

    item.lastModified = source.lastModified();
    item.payLoad = data;

    //DebugInfo.trace("putting to cache:"+source.getName());

    jspCacheRoots.put(source.getIdentifier(), item);
  }

  public static final void releaseUnneededMemory() {
    // FIXME: this is to do!
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
      IOException {
    ois.defaultReadObject();
    jspCacheRoots = new HashMap(); // for initialization
    if (cacheRoots == null) { // just in case to be sure
      cacheRoots = new HashMap();
    }
  }

  public static ASTTreeCache readCache(String path) {
    if (path == null) {
      return null;
    }

    File f = new File(path);
    if (!f.exists()) {
      return null;
    }

    try {
      return readCache(new FileInputStream(f), (int) f.length());
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public static ASTTreeCache readCache(InputStream inputStream,
      int streamLength) {
    if (streamLength < 100 || streamLength > 128 * 1024) {
      streamLength = 128 * 1024;
    }

    ASTTreeCache cache = null;
    ObjectInputStream ois = null;
    try {
      //long start = System.currentTimeMillis();
      ois = new ObjectInputStream(
          new BufferedInputStream(inputStream, streamLength));
      cache = (ASTTreeCache) ois.readObject();
      //System.err.println("read: " + (System.currentTimeMillis() - start) + "ms");
    } catch (InvalidClassException e) {
      // do nothing - failed to deserialize, so let it go without cache
    } catch (Exception e) {
      net.sf.refactorit.common.util.AppRegistry.getExceptionLogger().debug(e,
          ASTTreeCache.class);
    } finally {
      try {
        if (ois != null) {
          ois.close();
        }
      } catch (IOException ignore) {}
    }

    return cache;
  }

  public static synchronized void writeCache(ASTTreeCache cache,
      String path) {
    if (cache == null || path == null) {
      return;
    }

    try {
      File f = new File(path);

      if (!f.exists()) {
        int separatorIndex = path.lastIndexOf(File.separator);

        if ( separatorIndex != -1 ) {
          File dir = new File(path.substring(0, separatorIndex));
          dir.mkdirs();
        }
        f.createNewFile();
      }

      writeCache(cache, new FileOutputStream(f));
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public static synchronized void writeCache(ASTTreeCache cache,
      OutputStream outputStream) {
    ObjectOutputStream oo = null;
    try {
      //long start = System.currentTimeMillis();
      oo = new ObjectOutputStream(new BufferedOutputStream(outputStream, 8192));
      oo.writeObject(cache);
      //System.err.println("read: " + (System.currentTimeMillis() - start) + "ms");
      RuntimePlatform.console.println("RefactorIT: Project was cached successfully");
    } catch (Throwable e) {
      net.sf.refactorit.common.util.AppRegistry.getExceptionLogger().error(e,
          ASTTreeCache.class);
    } finally {
      try {
        if (oo != null) {
          oo.close();
        }
      } catch (IOException ignore) {
      }
    }
  }

//  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//    clearJSPCache();
//    out.defaultWriteObject();
//  }
//
//  public void removeJSPFromCache() {
//    Set elements=cacheRoots.entrySet();
//    Iterator it= elements.iterator();
//    FileParsingData data = null;
//    while (it.hasNext()) {
//      Map.Entry entry=(Map.Entry) it.next();
//      CacheItem item= (CacheItem)entry.getValue();
//      if ( item != null ) {
//        data=(FileParsingData)item.payLoad;
//        if ( data.jpi!=null ) {
//          it.remove();
//          continue;
//        }
//      } else {
//        if (RefactorItConstants.debugInfo ) {
//          DebugInfo.trace("Anomaly: cache item was null!");
//        }
//      }
//    }
//
//  }
}
