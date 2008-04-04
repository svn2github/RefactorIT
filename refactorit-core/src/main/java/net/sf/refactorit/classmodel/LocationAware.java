/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;



import net.sf.refactorit.source.format.FormatSettings;

import java.util.Comparator;


/**
 * Classmodel item aware of its location in the source.
 */
public interface LocationAware {
  /**
   * Gets source file where this item is located.
   *
   * @return source file or <code>null</code> if it is not known.
   */
  CompilationUnit getCompilationUnit();

  /**
   * Gets first line of this item in the source file.
   *
   * @return line or <code>0</code> if line not known.
   */
  int getStartLine();

  /**
   * Gets last line of this item in the source file.
   *
   * @return line or <code>0</code> if line not known.
   */
  int getEndLine();

  /**
   * Gets first column of the first line taken by this item in the source file.
   *
   * @return column or <code>0</code> if column not known.
   */
  int getStartColumn();

  /**
   * Gets last column of the last line taken by this item in the source file.
   *
   * @return column or <code>0</code> if column not known.
   */
  int getEndColumn();

  int getStartPosition();

  int getEndPosition();

  /**
   * @param other LocationAware to be checked to be existing within this
   * @return <code>true</code> when other is within bounds of this
   */
  boolean contains(LocationAware other);

  /**
   * Works mostly for statements and members starting on separate line,
   * i.e. when nothing stays infront of them.
   * @return indent measured in spaces; takes tab size from
   *   {@link FormatSettings#getTabSize()}
   */
  int getIndent();

  /**
   * @param other other item
   * @return true if <code>this</code> is residing after given item in the source
   */
  boolean isAfter(LocationAware other);

  final class PositionSorter implements Comparator {
    public static final PositionSorter instance = new PositionSorter();

    private PositionSorter() {
    }

    public static final PositionSorter getInstance() {
      return instance;
    }

    public final int compare(Object o1, Object o2) {
      int res = ((LocationAware) o1).getStartLine()
          - ((LocationAware) o2).getStartLine();

      if (res == 0) {
        res = ((LocationAware) o1).getStartColumn()
            - ((LocationAware) o2).getStartColumn();
      }

      return res;
    }
  }
}
