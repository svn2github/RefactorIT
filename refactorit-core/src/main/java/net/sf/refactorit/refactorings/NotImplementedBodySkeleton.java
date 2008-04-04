/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.refactorings.delegate.MethodBodySkeleton;
import net.sf.refactorit.source.format.FormatSettings;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class NotImplementedBodySkeleton extends MethodBodySkeleton {
  public NotImplementedBodySkeleton() {
    this("");
  }

  public NotImplementedBodySkeleton(String name) {
    super(" // FIXME " + FormatSettings.LINEBREAK +
        "throw new java.lang.UnsupportedOperationException( \"method " + name +
        " not implemented yet\");");
  }
}
