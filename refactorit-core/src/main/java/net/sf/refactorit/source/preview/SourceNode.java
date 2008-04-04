/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.preview;

import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 * @author Tonis Vaga
 * @author Igor Malinin
 * @author Anton Safonov
 */
public class SourceNode extends BinTreeTableNode {
  public SourceNode(SourceHolder sf) {
    super(sf);
  }

  public SourceHolder getSource() {
    return (SourceHolder) getBin();
  }
}
