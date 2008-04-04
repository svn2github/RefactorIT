/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.ui.JWordDialog;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class IsFixmeCommentChecker {
  private List fixmeWords;

  private int matchingPositionOnTrimmedLine;
  private int trimmedChars;
  private JWordDialog.Word matchedWord;

  private int matchingLineStartPos;

  public IsFixmeCommentChecker(List fixmeWords) {
    this.fixmeWords = fixmeWords;
  }

  public boolean isFixmeComment(String text) {
    matchingLineStartPos = 0;

    String lines[] = net.sf.refactorit.common.util.StringUtil.split(text, "\n");

    for (int i = 0; i < lines.length; ++i) {
      if (matchesFixmeWord(removeWhitespaceBeforeContent(lines[i]))) {
        trimmedChars = lines[i].length()
            - removeWhitespaceBeforeContent(lines[i]).length();
        return true;
      }

      matchingLineStartPos += lines[i].length() + 1; // The 1 is for the "\n" character.
    }

    return false;
  }

  public int getMatchingPositionOnTrimmedLine() {
    return matchingLineStartPos + trimmedChars + matchingPositionOnTrimmedLine;
  }

  public JWordDialog.Word getMatchedWord() {
    return matchedWord;
  }

  private boolean matchesFixmeWord(String line) {
    JWordDialog.Word.convertStringsToWordInstances(this.fixmeWords);

    for (int i = 0, max = this.fixmeWords.size(); i < max; i++) {
      JWordDialog.Word fixmeWord = (JWordDialog.Word)this.fixmeWords.get(i);
      if (fixmeWord.isSelected && matchesFixmeWord(line, fixmeWord)) {
        matchedWord = fixmeWord;
        return true;
      }
    }

    return false;
  }

  private boolean matchesFixmeWord(String line, JWordDialog.Word word) {
    if (word.isRegularExpression) {
      try {
        matchingPositionOnTrimmedLine = 0;
        return Pattern.compile(word.word).matcher(line).find();
      } catch (PatternSyntaxException e) {
        System.out.println("EXCEPTION, PLEASE REPORT");
        e.printStackTrace();
        return false;
      }
    } else {
      matchingPositionOnTrimmedLine = line.toLowerCase().indexOf((word.word).
          toLowerCase());
      return line.toLowerCase().startsWith((word.word).toLowerCase());
    }
  }

  private String removeWhitespaceBeforeContent(String text) {
    String[] extraWhitespace = new String[] {"/", "*", " ", "\t", "\r", "\n"};

    boolean somethingRemoved;
    do {
      somethingRemoved = false;

      for (int i = 0; i < extraWhitespace.length; i++) {
        if (text.startsWith(extraWhitespace[i])) {
          text = text.substring(extraWhitespace[i].length());
          somethingRemoved = true;
        }
      }
    } while (somethingRemoved);

    return text;
  }
}
