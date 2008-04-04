/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.source.SourceCoordinate;


/**
 * @author  sander
 * @author jevgeni
 */
public final class EmptyLine extends LocationAwareImpl {

  public EmptyLine(CompilationUnit source, int startLine, int startColumn,
      int endLine, int endColumn) {
    super(source, startLine, startColumn, endLine, endColumn);

    String content = source.getContent();
    int startPos = source.getLineIndexer().lineColToPos(startLine, startColumn);
    int endPos = source.getLineIndexer().lineColToPos(endLine, endColumn);

    for(int i = endPos; i >= startPos; i--) {
      if(!Character.isWhitespace(content.charAt(i))) {
        SourceCoordinate coord = source.getLineIndexer().posToLineCol(++i);
        super.setStartLine(coord.getLine());
        super.setStartColumn(coord.getColumn());
        break;
      }
    }
  }

}
