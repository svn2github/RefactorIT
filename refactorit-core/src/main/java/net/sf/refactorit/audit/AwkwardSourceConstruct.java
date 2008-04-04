/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.classmodel.BinSourceConstruct;

/**
 *
 *
 * @author Villu Ruusmann
 */
public abstract class AwkwardSourceConstruct extends SimpleViolation {

  public AwkwardSourceConstruct(BinSourceConstruct construct, String message,
      String helpId) {
    super(construct.getOwner(), construct.getRootAst(),
        message, helpId);
    setTargetItem(construct);
  }

  public BinSourceConstruct getSourceConstruct() {
    return (BinSourceConstruct) getTargetItem();
  }
}
