/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;

import java.io.IOException;


public class StringInserter extends DefaultEditor {

  private int line;
  private int column;
  private String string;

  /**
   * Note: here line starts with 1 and column with 0.
   */
  public StringInserter(SourceHolder input, SourceCoordinate targetCoordinate,
      final String string) {
    this(input, targetCoordinate.getLine(), targetCoordinate.getColumn(), string);
  }

  /**
   * Note: here line starts with 1 and column with 0.
   */
  public StringInserter(SourceHolder input, int line, int column, String string) {
    super(input);

    this.line = line;
    this.column = column;
    this.string = string;
  }

  //public List apply(SourceManager manager) throws IOException {
  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    try {
      Line line = manager.getLine(getTarget(), this.line);

      try {
        line.insert(this.column, this.string);
      } catch (IndexOutOfBoundsException e) {
        e.printStackTrace(System.err);

        // errors.add(getInput().getSource().getDisplayPath() + " - " + this.line
        //     + ": " + e.toString());
        status.addEntry(getTarget().getSource().getDisplayPath()
            + " - " + this.line,
            CollectionUtil.singletonArrayList(e), RefactoringStatus.FATAL);
      }
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }

    return status;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": "
        + getTarget().getSource() + " " + line + ":" + column
        + " - " + StringUtil.printableLinebreaks(string);
  }
}
