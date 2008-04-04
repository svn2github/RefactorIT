/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 *
 *
 * @author Tonis Vaga
 */
public class MethodNode extends BinTreeTableNode {
  public MethodNode(BinMethod method) {
    super(method, false);
  }
}
