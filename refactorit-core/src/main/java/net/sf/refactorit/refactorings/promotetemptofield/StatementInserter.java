/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;



/**
 * @author RISTO A
 */
public class StatementInserter {
  public static void append(String statement, BinMember target,
      final TransformationList transList) {
    BinItemFormatter formatter = target.getFormatter();

    transList.add(new StringInserter(target.getCompilationUnit(), formatter
        .findNewMemberPosition(), statement));
  }

  public static void breakStatementApart(CompilationUnit compilationUnit,
      SourceCoordinate coordinate, String prefixForNewLine,
      final TransformationList transList) {
    transList.add(new StringInserter(compilationUnit,
        coordinate.getLine(), coordinate.getColumn() - 1,
        ";" + FormatSettings.LINEBREAK +
        StringUtil.getIndent(
        compilationUnit.getSource().getContentOfLine(coordinate.getLine())) +
        prefixForNewLine));
  }
}
