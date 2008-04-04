/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.SelectionAnalyzer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;


public class BinSelectionFinder {

  private static final String SQUARE_BRACKET_OPEN = "/*[*/";
  private static final String SQUARE_BRACKET_CLOSE = "/*]*/";

  public static final int VALID_SELECTION = 1;
  public static final int INVALID_SELECTION = 2;
  public static final int COMPARE_WITH_OUTPUT = 3;

  public static BinSelection findSelectionIn(CompilationUnit source) throws
      Exception {

    String content = source.getContent();

    Assert.assertTrue("Source has content",
        content != null && content.length() > 0);

    /*    int start = content.indexOf(SQUARE_BRACKET_OPEN);
        assertTrue("Selection start not found", start > 0);
        start += SQUARE_BRACKET_OPEN.length();
        int end = content.indexOf(SQUARE_BRACKET_CLOSE, start);*/

    int start = -1;
    int end = -1;
    int includingStart = content.indexOf(SQUARE_BRACKET_OPEN);
    int excludingStart = content.indexOf(SQUARE_BRACKET_CLOSE);
    int includingEnd = content.lastIndexOf(SQUARE_BRACKET_CLOSE);
    int excludingEnd = content.lastIndexOf(SQUARE_BRACKET_OPEN);

    if (includingStart == -1 && excludingStart == -1
        && includingEnd == -1 && excludingEnd == -1) {
      return null;
    }

    if (includingStart > excludingStart && excludingStart != -1) {
      includingStart = -1;
    } else if (excludingStart > includingStart && includingStart != -1) {
      excludingStart = -1;
    }

    if (includingEnd < excludingEnd) {
      includingEnd = -1;
    } else if (excludingEnd < includingEnd) {
      excludingEnd = -1;
    }

    if (includingStart != -1) {
      start = includingStart;
    } else {
      start = excludingStart + SQUARE_BRACKET_CLOSE.length();
    }

    if (excludingEnd != -1) {
      end = excludingEnd;
    } else {
      end = includingEnd + SQUARE_BRACKET_CLOSE.length();
    }

    Assert.assertTrue("Selection is ok", start >= 0 && end >= 0 && end >= start);

//    System.err.println("|" + content.substring(start, end) + "|");

    return new BinSelection(source, content.substring(start, end), start, end);
  }

  public static BinSelection findSelectionIn(Project project) throws Exception {
    List sources = project.getCompilationUnits();
    if (sources.size() == 0) {
      project.getProjectLoader().build();
      sources = project.getCompilationUnits();
    }

    Assert.assertTrue("Project has no source files: " + project,
        sources.size() > 0);

    BinSelection selection = null;
    for (int i = 0, max = sources.size(); i < max; i++) {
      selection = findSelectionIn(((CompilationUnit) sources.get(i)));
      if (selection != null) {
        break;
      }
    }

    Assert.assertTrue("Project has selection: " + project + ", sources: "
        + sources,
        selection != null && selection.getText() != null
        && selection.getText().length() > 0);

    return selection;
  }

  /** Some tests have test selection coordinates specified as human sees them
   * in the editor, however there could be tab symbols on that line, so they
   * differ from our internal coordinates, which count tab as one char.
   * @param source source file
   * @param startLine start line
   * @param startColumn start column
   * @param endLine end line
   * @param endColumn end column
   * @param tabSize tab size for given file
   * @return new selection by given coordinates
   */
  public static BinSelection getSelectionByHumanCoordinates(
      CompilationUnit source,
      int startLine, int startColumn, int endLine, int endColumn,
      int tabSize) {
    final String content = source.getContent();

    int start = source.getLineIndexer().lineColToPos(startLine, 1);
    start = addColumnsWithTabInMind(content, tabSize, startColumn, start) - 1;
    int end = source.getLineIndexer().lineColToPos(endLine, 1);
    end = addColumnsWithTabInMind(content, tabSize, endColumn, end) - 1;

    Assert.assertTrue("Selection is ok", start >= 0 && end >= 0 && end >= start);

    return new BinSelection(source, content.substring(start, end), start, end);
  }

  private static int addColumnsWithTabInMind(final String content,
      final int tabSize, final int referenceColumn, final int referencePosition) {
    int res = referencePosition;
    int tempColumn = 0;
    while (tempColumn < referenceColumn) {
      if (content.charAt(res) == '\t') {
        tempColumn += tabSize;
      } else {
        tempColumn++;
      }
      ++res;
    }

    return res;
  }
  
  public static List getSelectedItemsFromProject(Project project) {
    List las = new ArrayList();
    try {
      
      final BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    SelectionAnalyzer analyzer = new SelectionAnalyzer(selection);
      TestCase.assertFalse("shouldn't have errors: "
          + CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()), (project.getProjectLoader().getErrorCollector()).hasErrors());
    las.addAll(analyzer.getSelectedItems());
    } catch (Exception e) {      
      TestCase.assertTrue(e.toString(), false);
    }
    return las;
  }

}
