/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;


import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPathElement;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Implementation of ClassPath VFS for netbeans/forte
 *
 * @author Igor Malinin
 * @author Tonis Vaga
 */
public class NBClassPath extends AbstractClassPath {

  private static ClassPathElement[] hiddenElements = null;
  private WeakReference ideProjectRef;

  public NBClassPath(Object ideProject) {
    this.ideProjectRef = new WeakReference(ideProject);
  }

  public ClassPathElement[] getAutodetectedElements() {
    final Object ideProject = ideProjectRef.get();
    if(ideProject == null) {
      return new ClassPathElement[]{};
    } else {
      return getElementsFromFileObjects(
        PathUtil.getInstance().getAutodetectedClasspath(ideProject));
    }
  }

  protected ClassPathElement[] createElements() {
    final Object ideProject = ideProjectRef.get();
    if(ideProject == null) {
      return new ClassPathElement[] {};
    }
    
    return getElementsFromFileObjects(
        PathUtil.getInstance().getClasspath(ideProject));
  }

  private ClassPathElement[] getElementsFromFileObjects(
      PathItemReference[] fileObjects) {
    List elements = new ArrayList();
    if (fileObjects == null) {
      return new ClassPathElement[0];
    }
    for (int i = 0; i < fileObjects.length; i++) {
      if (fileObjects[i].isValid()) {
        elements.add(fileObjects[i].getClassPathElement());
      }
    }
    ClassPathElement[] result = new ClassPathElement[elements.size()];
    result = (ClassPathElement[]) elements.toArray(result);
    return result;
  }

  private void initializeHiddenElements() {
    Repository rep = Repository.getDefault();
    Enumeration e = rep.getFileSystems();
    List elementsList = new ArrayList();
    while (e.hasMoreElements()) {
      FileSystem fobject = (FileSystem) e.nextElement();
      if (fobject.isHidden()) {
        String name = fobject.getDisplayName();
        // ugly hack for adding j2ee for S1S
        if (name.startsWith("j2ee") && name.endsWith(".jar")) {
          NBClassPathElement element = new NBClassPathElement(fobject);
          elementsList.add(element);
//          System.out.println("tonisdebug:hidden element added "
//              + fobject.getDisplayName());
//          //fobject.
        }
      }
    }

    int length = elementsList.size();

    hiddenElements = (ClassPathElement[])
        elementsList.toArray(new ClassPathElement[length]);
  }

  public ClassPathElement[] getCachedHiddenElements() {
    return hiddenElements;
  }

  public ClassPathElement[] getHiddenElements() {
    if (hiddenElements == null) {
      initializeHiddenElements();
    }

    return hiddenElements;
  }
}
