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
import java.util.Iterator;
import java.util.Map;


/**
 * <b>NOTE: Currently expects that replacement parts do not contain '\n'</b>
 */
public class ASTReplacingEditor extends DefaultEditor {
  private final Map replaceMap;

  // FIXME Currently expects that replacement parts do not contain '\n'
  /**
   * @param source source file
   * @param replaceMap
   * The format of map is as follows:<br>
   * ASTImpl -> String[] { from, to, optionalpostfix1, optionalpostfix2, ...}
   * <ul>
   * <li>from - what is being replaced from - if null then AST.getText() is used</li>
   * <li>to - what it is being replaced to</li>
   * <li>postfixNif - if available optional postfixes are tryed from 1st to last
   * - if optional postfix can be applied it is added also to 'from' part
   * - only one optional postfix is added</li>
   * </ul>
   *
   * So for example when the ast is 'new' ast and optionalpostfix1 = " " and
   * optionalpostfix2 = "\t" and to=callNEW then it replaces as follows:<br>
   * 'new String()' => 'callNEWString()'<br>
   * 'new//\nString()' => 'callNEW//\nString()' etc..
   */
  public ASTReplacingEditor(SourceHolder source, Map replaceMap) {
    super(source);
    this.replaceMap = replaceMap;
  }

  /**
   * @return edited source line
   */
  //public List apply(SourceManager manager) throws IOException {
  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    Iterator entries = replaceMap.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      doReplace(manager, (ASTImpl) entry.getKey(), (String[]) entry.getValue(),
          status);
    }

    return status;
  }

  private void doReplace(LineManager manager, ASTImpl node,
      String replaceTo[], RefactoringStatus status) {
    try {
      String from = replaceTo[0];
      String to = replaceTo[1];
      if (from == null) {
        from = node.getText();
      }

      Line line = manager.getLine(getTarget(), node.getStartLine());

      int startColumn = node.getStartColumn();
      int endColumn = startColumn + from.length();
      if (replaceTo.length > 2) {
        for (int i = 2; i < replaceTo.length; ++i) {
          String postfix = replaceTo[i];

          if (endColumn - 1 + postfix.length() <= line.length()) {
            String testPart = line.substring(endColumn - 1, endColumn - 1
                + postfix.length());

            if (testPart.equals(postfix)) {
              endColumn += postfix.length();
              break;
            }
          }
        }
      }

      //System.err.println("Replacing "
      //    + line.substring(startColumn - 1, endColumn - 1) + " to " + to);
      line.replace(startColumn - 1, endColumn - 1, to);

    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }
  }
}
