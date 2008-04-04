/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.loader;


import net.sf.refactorit.jsp.JspPageInfo;
import net.sf.refactorit.parser.ASTTree;

import java.util.List;


public final class FileParsingData implements java.io.Serializable {
  public ASTTree astTree;
  public List simpleComments;
  public List javadocComments;
  public JspPageInfo jpi;
}
