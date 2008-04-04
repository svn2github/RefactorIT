/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.source.SourceCoordinate;


/**
 * @author  RISTO A
 */
public class PositionsForNewItems {
  /** Works for BinMethods, BinConstructors and BinInitializers */
  public static SourceCoordinate findNewStatementPositionAtEnd(BinMember m) {
    return findNewStatementPositionAtEnd(m.getCompilationUnit(),
        ASTUtil.getStatementList(m));
  }

  private static SourceCoordinate findNewStatementPositionAtEnd(CompilationUnit
      compilationUnit, ASTImpl blockAst) {
    int line = blockAst.getEndLine();
    int column = blockAst.getEndColumn() - 1; // subtract 1 to move to *before* the "}"

    String content = compilationUnit.getContent();
    LineIndexer indexer = compilationUnit.getLineIndexer();
    int pos = indexer.lineColToPos(line, column) - 1;
    while (pos > 0
        && (content.charAt(pos) == ' ' || content.charAt(pos) == '\t')) {
      --pos;
    }
    SourceCoordinate position = indexer.posToLineCol(pos + 1);
    position.convertToEditorCoordinateSpace();
    return position;
  }

  public static SourceCoordinate findNewFieldPosition(BinCIType type) {
    return findNewMemberPosition(type, JavaTokenTypes.VARIABLE_DEF);
  }

  public static SourceCoordinate findNewMethodPosition(BinCIType type) {
    return findNewMemberPosition(type, JavaTokenTypes.METHOD_DEF);
  }

  public static SourceCoordinate findNewConstructorPosition(BinCIType type) {
    return findNewMemberPosition(type, JavaTokenTypes.CTOR_DEF);
  }

  private static SourceCoordinate findNewMemberPosition(BinCIType type,
      int tokenType) {
    CompilationUnit compilationUnit = type.getCompilationUnit();
    ASTImpl offsetNode = type.getOffsetNode();

    ASTImpl objBlock = ASTUtil
        .getFirstChildOfType(offsetNode, JavaTokenTypes.OBJBLOCK);

    ASTImpl lastChild = ASTUtil.getLastChildOfType(objBlock, tokenType);

    int line, column;

    if (lastChild == null) {
      if (tokenType == JavaTokenTypes.METHOD_DEF) {
        return PositionsForNewItems
            .findNewStatementPositionAtEnd(compilationUnit, objBlock);
      } else { // VARIABLE_DEF and all the rest
        // just after opening brace
        line = objBlock.getStartLine();
        column = objBlock.getStartColumn() + 1;

        SourceCoordinate result = moveOneLinebreakForth(compilationUnit, line, column);
        result.convertToEditorCoordinateSpace();
        return result;
      }
    } else {
      line = lastChild.getEndLine();
      column = lastChild.getEndColumn();

      SourceCoordinate position = moveOneLinebreakForth(compilationUnit, line, column);
      position.convertToEditorCoordinateSpace();
      
      while(compilationUnit.isWithinGuardedBlocks(position.getLine(), position.getColumn())) {
        position.setLine(position.getLine() + 1);
      }
      
      return position;
    }
  }
  
  private static SourceCoordinate moveOneLinebreakForth(
      final CompilationUnit compilationUnit, final int line, final int column) {
    
    int pos = compilationUnit.getLineIndexer().lineColToPos(line, column);
    
    pos = StringUtil.moveOneLinebreakForth(compilationUnit.getContent(), pos);
    
    return compilationUnit.getLineIndexer().posToLineCol(pos);
  }
}
