/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse.vfs;

import net.sf.refactorit.vfs.JavadocPath;


/**
 * EclipseJavadocPath
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.2 $ $Date: 2004/11/15 15:27:48 $
 */
public class EclipseJavadocPath extends JavadocPath {
  /*
   * @see net.sf.refactorit.vfs.JavadocPath#getElements()
   */
  public String[] getElements() {
    return new String[0];
  }
}
