/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;

import java.io.IOException;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class RenameEditor extends DefaultEditor {

  private String newName;
  private String oldName = null;

  private List nodes;
  private boolean trimTrailingSpaces = false;

  public RenameEditor(SourceHolder input, List nodes, String newName,
      boolean trimTrailingSpaces) {
    super(input);
    this.newName = newName;
    this.nodes = nodes;
    this.trimTrailingSpaces = trimTrailingSpaces;
  }

  private String getProbableOldName() {
    if (oldName == null) {
      try {
        oldName = ((ASTImpl) nodes.get(nodes.size() - 1)).getText();
      } catch (Exception e) {
        e.printStackTrace(System.err);
        oldName = "";
      }
    }

    return oldName;
  }

  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    try {
      for (int i = 0, iMax = this.nodes.size(); i < iMax; i++) {
        final ASTImpl current = (ASTImpl)this.nodes.get(i);

        // Validate node's content
//        if (!current.getText().equals(getProbableOldName())) {
//          final String message = getTarget().getSource().getDisplayPath()
//              + " - "
//              + current.getLine() + ":" + current.getColumn()
//              + " Found illegal node \'" + current.getText()
//              + "\', expected \'" + getProbableOldName() + "\'";
//
//          status.addEntry(message, RefactoringStatus.ERROR);
//          continue;
//        }

        // Compute indices
        int startColumn = current.getStartColumn() - 1;
        int endColumn = current.getEndColumn() - 1;

        Line line = manager.getLine(getTarget(), current.getStartLine());

        // Validate indices
        if (startColumn >= line.length()
            || endColumn >= line.length()) {
          final String message = getTarget().getSource().getDisplayPath()
              + " - "
              + current.getLine() + ":" + current.getColumn()
              + " File was probably changed outside the RefactorIT,"
              + " expected to find \'" + getProbableOldName() + "\'";

          status.addEntry(message, RefactoringStatus.ERROR);

          continue;
        }

        if (current.getStartLine() == current.getEndLine()) {

          String oldText = line.substring(startColumn, endColumn);

          // check that just one edit edits a place to avoid that 2 renames rename a single name
          if (!getTarget().getSource().getText(current).equals(oldText)) {
            final String message = getTarget().getSource().getDisplayPath()
                + " - "
                + current.getLine() + ":" + current.getColumn()
                + " Found illegal string \'" + oldText
                + "\' when expecting \'" + getProbableOldName() + "\'";

            status.addEntry(message, RefactoringStatus.ERROR);
            continue;
          }

          if (this.trimTrailingSpaces) {
            while (endColumn + 1 < line.length()
                && (line.charAt(endColumn) == ' '
                || line.charAt(endColumn) == '\t')) {
              ++endColumn;
            }
          }

          line.replace(startColumn, endColumn, this.newName);

        } else {

          String oldText = "";

          for (int lineNr = current.getStartLine(); lineNr < current.getEndLine();
              lineNr++) {
            line = manager.getLine(getTarget(), lineNr);

            if (lineNr == current.getStartLine()) {
              oldText += line.substring(startColumn);
            } else {

              oldText += line.getContent();
            }
          }

          line = manager.getLine(getTarget(), current.getEndLine());

          oldText += line.substring(0, endColumn);

//          oldText = StringUtil.replace(oldText, " ", "");
//          oldText = StringUtil.replace(oldText, "\n", "");
//          oldText = StringUtil.replace(oldText, "\r", "");
//          oldText = StringUtil.replace(oldText, "\t", "");

          if (!getTarget().getSource().getText(current).equals(oldText)) {
            final String message = getTarget().getSource().getDisplayPath()
                + " - "
                + current.getLine() + ":" + current.getColumn()
                + " Found illegal string \'" + oldText
                + "\' when expecting \'" + getProbableOldName() + "\'";

            status.addEntry(message, RefactoringStatus.ERROR);
            continue;
          }

          if (this.trimTrailingSpaces) {
            while (endColumn + 1 < line.length()
                && (line.charAt(endColumn) == ' '
                || line.charAt(endColumn) == '\t')) {
              ++endColumn;
            }
          }

          for (int lineNr = current.getStartLine();
              lineNr < current.getEndLine();
              lineNr++) {

            line = manager.getLine(getTarget(), lineNr);

            if (lineNr == current.getStartLine()) {
              //old code: line.replace(startColumn, line.length() - 1, newName);
              line.replace(startColumn, line.length(), newName);
            } else {

              //old code: line.delete(0, line.length() - 1);
              line.delete(0, line.length());
            }
          }

          line = manager.getLine(getTarget(), current.getEndLine());

          line.delete(0, endColumn);
        }

        // can be helpful on debug, since it will detect error on double rename
        // of the same node
        // breaks old tree on type rename
        //      current.setText(this.newName);
      }

    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }
    return status;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": "
        + getTarget()
        + " - " + getProbableOldName() + " -> " + this.newName;
  }


  public boolean equals(Object o) {
    if(super.equals(o)) {
      return true;
    }

    if (o instanceof RenameEditor) {
      RenameEditor editor = (RenameEditor) o;
      if (equals(editor.getTarget(), this.getTarget()) &&
          equals(editor.newName, this.newName) &&
          editor.trimTrailingSpaces == this.trimTrailingSpaces &&
          equals(editor.nodes, this.nodes)) {
        return true;
      }
    }

    return false;
  }

  public int hashCode() {
    return super.hashCode();
  }

  private boolean equals(Object o1, Object o2) {
    if((o1 == o2) || (o1 != null && o1.equals(o2))) {
      return true;
    }

    return false;
  }
}
