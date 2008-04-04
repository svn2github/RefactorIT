/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.jdeveloper.vfs;


import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import oracle.ide.net.URLFileSystem;
import oracle.ide.net.URLPath;
import oracle.jdeveloper.model.JProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 *
 * @author  Tanel
 * @author  Anton Safonov
 */
public class JDevClassPath extends AbstractClassPath {

  JProject jproject = null;
  /** Creates new JDevClassPath */
  public JDevClassPath(JProject jproject) {
    this.jproject = jproject;
  }

  protected ClassPathElement[] createElements() {

    if ( jproject == null ) {
      return new ClassPathElement[0];
    }

    List list;

    list=getAutodetectedClassPathElements();

    
    return (ClassPathElement[]) list.toArray(new ClassPathElement[list.size()]);
  }


  public List getAutodetectedClassPathElements(){

    URLPath classPath = jproject.getClassPath();

    List list = new ArrayList( classPath.size() );

    StringTokenizer classPathTokenizer = new StringTokenizer(classPath.toString(), File.pathSeparator);

    while (classPathTokenizer.hasMoreTokens()) {
      File entry = new File(classPathTokenizer.nextToken());
      checkNaddEntry(entry,list);
    }

    return list;
  }

  /**
   * was extracted from <code>getAutodetectedClassPathElements</code> method and is
   * now also reused in <code>getUserSpecifiedClassPathElements</code> method
   */
  private void checkNaddEntry(File entry, List list){
    if (entry.isDirectory()) {
      list.add(new net.sf.refactorit.vfs.local.DirClassPathElement(entry) {
        class JDevEntry extends
            net.sf.refactorit.vfs.local.DirClassPathElement.Entry {
          protected JDevEntry(File f) {
            super(f);
          }

          public boolean delete() {
            try {
              URL url = file.toURL();

              // it is better to delete it with help of URLFileSystem
              // because it also calls some usefull notifications for JDev
              if (URLFileSystem.exists(url)) {
                boolean res = URLFileSystem.delete(url);
                if (res) {
//                    System.out.println( " Done" );
                  return true;
                }
              }
            } catch (MalformedURLException ignore) {
            }

            return super.delete();
          }
        }

        protected ClassPath.Entry createEntry(File file) {
          return new JDevEntry(file);
        }
      });
    }
    if (entry.isFile()) {
      list.add(new net.sf.refactorit.vfs.ZipClassPathElement(entry));
    }
  }

}
