/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.vfs;

import com.borland.jbuilder.node.JBProject;
import com.borland.jbuilder.paths.ProjectPathSet;
import com.borland.primetime.vfs.FileFilesystem;
import com.borland.primetime.vfs.Url;
import com.borland.primetime.vfs.ZipFilesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.refactorit.jbuilder.RefactorItPropGroup;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.ZipClassPathElement;
import net.sf.refactorit.vfs.local.DirClassPathElement;


/**
 * Implementation of ClassPath VFS for JBuilder.
 *
 * @author  Igor Malinin
 */
public class JBClassPath extends AbstractClassPath {
  private JBProject jbproject;
  /** Creates new JBClassPath */
  public JBClassPath(JBProject project) {
    jbproject = project;
  }

  public ClassPathElement[] getAutodetectedElements() {
    return (ClassPathElement[]) getAutodetectedClassPathElements().toArray(new
        ClassPathElement[0]);
  }

  protected ClassPathElement[] createElements() {

    List list;

    jbproject.check(); // resync properties

    if (RefactorItPropGroup.getProjectPropertyBoolean(
        RefactorItPropGroup.AUTODETECT_PATHS, true)) {
      list = getAutodetectedClassPathElements();
    } else {
      list = getUserSpecifiedClassPathElements();
    }

    return (ClassPathElement[]) list.toArray(new ClassPathElement[list.size()]);
  }

  /**
   * returns list of ClassPathElements
   * @return list of ClassPathElements
   */
  public List getUserSpecifiedClassPathElements() {
    String specifiedSourcepath = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_CLASSPATH, "");
    if (JBSourcePath.isOldStylePath(specifiedSourcepath)) {
      return new ArrayList(0);
    }

    StringTokenizer st = new StringTokenizer(specifiedSourcepath,
        File.pathSeparator);

    List result = new ArrayList(st.countTokens());

    while (st.hasMoreElements()) {
      String pathStr = st.nextToken();
      File file = new File(pathStr);
      if (ClasspathUtil.isJarFile(file)) {
        result.add(new ZipClassPathElement(file));
      } else {
        result.add(new DirClassPathElement(file));
      }
    }
    return result;
  }

  public List getAutodetectedClassPathElements() {
    List list;
    ProjectPathSet pathset = jbproject.getPaths();

    Url outpath = pathset.getOutPath();
    Url[] sourcepath = pathset.getSourcePath();
    Url[] urls = pathset.getFullClassPath();

    list = new ArrayList(urls.length);
    boolean foundRTJar = false;

    for (int i = 0; i < urls.length; i++) {
      Url url = urls[i];
      if (!contains(sourcepath, url) && !outpath.equals(url)) {
        list.add(createClassPathElementFromUrl(url));
        if (url.getFullName().endsWith("rt.jar")) {
          foundRTJar = true;
        }
      }
    }

    if (!foundRTJar) {
      urls = pathset.getJDKPathSet().getFullClassPath();
      for (int i = 0; i < urls.length; i++) {
        Url url = urls[i];
        if (!contains(sourcepath, url) && !outpath.equals(url)) {
          list.add(createClassPathElementFromUrl(url));
        }
      }
    }

    return list;
  }

  private ClassPathElement createClassPathElementFromUrl(Url url) {
    if (url.getFilesystem() instanceof FileFilesystem) {
      return new DirClassPathElement(url.getFileObject());
    } else if (url.getFilesystem() instanceof ZipFilesystem) {
      return new ZipClassPathElement(url.getFileObject());
    } else {
      return new JBClassPathElement(url);
    }
  }

  private static boolean contains(Url[] sourcepath, Url path) {
    for (int i = 0; i < sourcepath.length; i++) {
      if (sourcepath[i].equals(path)) {
        return true;
      }
    }

    return false;
  }

}
