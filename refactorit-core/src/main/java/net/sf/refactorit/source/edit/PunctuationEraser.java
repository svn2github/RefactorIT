/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;


/** @author  RISTO A */
public class PunctuationEraser extends StringEraser {
  public PunctuationEraser(SourceHolder source, SourceCoordinate coordinate,
      boolean left) {
    super(source,
        coordinate.getLine(), coordinate.getColumn() - 1,
        coordinate.getLine(), coordinate.getColumn() - 1,
        true, left);

    setRemoveLinesContainingOnlyComments(true);
  }
}
