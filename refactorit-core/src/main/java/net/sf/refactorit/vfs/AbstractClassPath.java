/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.ui.RuntimePlatform;

import java.io.File;
import java.io.InputStream;


/**
 * Local filesystem ClassPath. Do not forget to call close() after using it.
 * There are three types of path elements : ordinary - #getElements()
 * RIT inner - getInnerElements and hidden  - getHiddenElements() classpath elements[tonis].
 *
 * @author Igor Malinin
 * @author Tonis Vaga
 */
public abstract class AbstractClassPath implements ClassPath {

  static String[] jasperPath = null;

  /**
   * Template method, fetched elements from cache or creates if cache doesn't exist
   */
  private final ClassPathElement[] getElements() {
    if ( cachedElements == null ) {
      cachedElements=createElements();
    }
    return cachedElements;
  }
  /**
   *  Creates classpath elements, used only by getElementsTemplate method. Don't use it directly!
   */
  protected abstract ClassPathElement[] createElements();

  public AbstractClassPath() {
//    initRITPath();
  }

  private final void initInnerPath() {
    innerElements = new ClassPathElement[0];
    if (jasperPath == null) {
      try {
        jasperPath = JspUtil.getJasperClasspath();
      } catch (Exception ex) {
        //fixme: i18n
        final String msg = "RefactorIT warning: initializing jasper failed, user is unable to use RefactorIT with JSP";
        RuntimePlatform.console.println(msg);
        AppRegistry.getLogger(AbstractClassPath.class).error(ex);
        return;
      }
    }
    if (jasperPath != null) {
      ClassPathElement pathElements[] = new ClassPathElement[jasperPath.length];
      for (int i = 0; i < jasperPath.length; ++i) {
        String name = jasperPath[i];
        File file = new File(name);
        if (!file.exists() || file.isDirectory()) {
          RuntimePlatform.console.println(
              "RefactorIT warning: hidden classpath element " + name
              + " does not exist");
        }
        pathElements[i] = new ZipClassPathElement(file);
      }

      addInnerElements(pathElements);
    } else {
      AppRegistry.getLogger(AbstractClassPath.class).debug("wrong jasper path");
    }
  }

  private static ClassPathElement[] innerElements = null;

  public final boolean containsInnerElement(String aClass) {
    ClassPathElement[] elements = getInnerElements();
    if (elements == null) {
      return false;
    }
    for (int i = 0; i < elements.length; i++) {
      if (elements[i].getEntry(aClass) != null) {
        return true;
      }
    }
    return false;

  }

  private final void addInnerElements(ClassPathElement[] elements) {
    if (elements == null) {
      return;
    }
//    File file = new File(name);
//    if ( !file.exists() || file.isDirectory() ) {
//      RuntimePlatform.console.println("RefactorIT warning: hidden classpath element " + name
//          + " does not exist");
//      return;
//    }
    ClassPathElement[] inElements = getInnerElements();
    if (inElements == null) {
      inElements = new ClassPathElement[0];
    }
    ClassPathElement[] newElements = new ClassPathElement[inElements.length
        + elements.length];
    int i = 0;
    for (; i < inElements.length; i++) {
      newElements[i] = inElements[i];
    }
    for (i = 0; i < elements.length; ++i) {
      newElements[inElements.length + i] = elements[i];
    }
    //newElements[i] = new ZipClassPathElement(file);
    innerElements = newElements;
  }

  public final boolean isAnythingChanged() {
    boolean changed = false;
    ClassPathElement elArray[][] = new ClassPathElement[][] {
        getElements(), getHiddenElements(), getInnerElements()};
    for (int i = 0; i < elArray.length; i++) {
      ClassPathElement[] elements = elArray[i];
      if (elements == null) {
        continue;
      }
      for (int j = 0; j < elements.length; j++) {
        // checking only jars here for speed reasons
        if (elements[j] instanceof ZipClassPathElement) {
          if (((ZipClassPathElement) elements[j]).isChanged()) {
            changed = true;
            // we can't stop it right away, since we wont to update caches for all jars
          }
        }
      }
    }

    return changed;
  }

  private ClassPath.Entry findEntry(String cls) {
    final ClassPathElement elArray[][] = new ClassPathElement[][] {
        getElements(), getHiddenElements(), getInnerElements()};
    for (int i = 0; i < elArray.length; i++) {
      final ClassPathElement[] elements = elArray[i];
      if (elements != null) {
        for (int j = 0; j < elements.length; j++) {
          final ClassPath.Entry entry = elements[j].getEntry(cls);
          if (entry != null) {
            return entry;
          }
        }
      }
    }
    return null;
  }

  public boolean delete(String cls) {
    ClassPath.Entry entry = findEntry(cls);

    if (entry != null) {
      return entry.delete();
    }

    return false;
  }

  /**
   * Time of last modification.
   * Returns 0 if unknown.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return  last modified time
   */
  public long lastModified(String cls) {
    ClassPath.Entry entry = findEntry(cls);
    return (entry != null) ? entry.lastModified() : 0;
  }

  /**
   * Returns size of binary representation of a class for a given full
   * qualified class name. Returns 0 if class does not exists.
   * Names of packages and classes are delimited by slashes, inner
   * classes are delimited from containing classes by dollar sign.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return binary class length
   */
  public long length(String cls) {
    ClassPath.Entry entry = findEntry(cls);
    return (entry != null) ? entry.length() : 0;
  }

  public boolean exists(String cls) {
    final ClassPathElement elArray[][] = new ClassPathElement[][] {
        getElements(), getHiddenElements(), getInnerElements()};
    outer: for (int i = 0; i < elArray.length; i++) {
      final ClassPathElement[] elements = elArray[i];
      if (elements != null) {
        for (int j = 0; j < elements.length; j++) {
          if (elements[j].existsEntry(cls)) {
            return true;
          }
//          final ClassPath.Entry entry = elements[j].getEntry(cls);
//          if (entry != null) {
//            return entry.exists();
//          }
        }
      }
    }

    return false;
  }

  /**
   * Provides binary stream for a given full qualified class name.
   * Names of packages and classes are delimited by slashes, inner
   * classes are delimited from containing classes by dollar sign.
   * Returns null if nothing found
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return input stream
   */
  public InputStream getInputStream(String cls) {
    ClassPath.Entry entry = findEntry(cls);
    return (entry != null) ? entry.getInputStream() : null;
  }

  /**
   * N.B! - this assumes some things about subclasses<BR>
   * elements == null means released<BR>
   * getElements() allocates<BR>
   * release() releases again
   * @return string form of elements
   */
  public String getStringForm() {
    boolean wasReleased = isReleased();

    String result = getStringFormOfElements(getElements());

    if (wasReleased) {
      release();
    }

    return result;
  }

  private String getStringFormOfElements(ClassPathElement[] elements) {
    StringBuffer buf = new StringBuffer(256);
    int k = 0;
    for (int i = 0; i < elements.length; i++) {
      String elem = elements[i].toString();
      if (elem == null || elem.trim().length() == 0) {
        continue;
      }
      if (k++ > 0) {
        buf.append(File.pathSeparatorChar);
      }
      buf.append(elem);
    }
    return buf.toString();
  }

  /**
   * Releases resources. Calls release for each resource
   * @post isReleased()==true
   */
  public void release() {
    ClassPathElement elArray[][] = new ClassPathElement[][] {
        getCachedElements(), getCachedHiddenElements(), getCachedInnerElements()};
    for (int i = 0; i < elArray.length; i++) {
      ClassPathElement[] currElements = elArray[i];
      if (currElements == null) {
        continue;
      }
      for (int j = 0; j < currElements.length; j++) {
        if (currElements[j] != null) {
          currElements[j].release();
        }
      }
    }
    cachedElements=null;
  }

  public final String toString() {
    if (isReleased()) {
      return "released";
    } else {
      return getStringFormOfElements(getElements());
    }
  }

  /**
   * @param aClass e.g. "java/lang/Object.class"
   * @return true if contains such class
   */
  public final boolean contains(String aClass) {
    try {
      ClassPathElement elArray[][] = new ClassPathElement[][] {
          getElements(), getHiddenElements(), getInnerElements()};
      for (int i = 0; i < elArray.length; i++) {
        ClassPathElement[] classPaths = elArray[i];
        if (classPaths == null) {
          continue;
        }
        for (int j = 0; j < classPaths.length; j++) {
          if (classPaths[j] != null && classPaths[j].getEntry(aClass) != null) {
            return true;
          }
        }
      }

    } catch (Exception e) {
      // This is thrown when code cannot access some file, for example.

      e.printStackTrace();
    }

    return false;
  }

  /**
   * @return hidden path elements. Subclasses should override it.
   */
  protected ClassPathElement[] getHiddenElements() {
    return null;
  }

  protected ClassPathElement[] getCachedHiddenElements() {
    return null;
  }

  private ClassPathElement[] getCachedInnerElements() {
    return innerElements;
  }

  /**
   * @return RIT inner classpath. For jasper etc.
   */
  private ClassPathElement[] getInnerElements() {
    if (innerElements == null) {
      initInnerPath();
    }
    return innerElements;
  }

  private ClassPathElement[] cachedElements;

  public ClassPathElement[] getCachedElements() {
    return cachedElements;
  }

  /**
   *
   * @return true if classpath is released
   */
  public boolean isReleased() {
    return cachedElements == null;
  }

  public ClassPathElement[] getAutodetectedElements() {
    return getElements();
  }

}
