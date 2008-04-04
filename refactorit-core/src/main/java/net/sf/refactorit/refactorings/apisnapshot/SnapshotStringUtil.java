/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


class SnapshotStringUtil {
  static String[] split(String line, String separator, int expectedResultSize) {
    return split(line, new String[] {separator}
        , expectedResultSize);
  }

  /** All separators must have the same length; there must be at least one separator in the array */
  static String[] split(String line, String[] separators,
      int expectedResultSize) {
    String[] result = new String[expectedResultSize];

    for (int i = 0; i < expectedResultSize; i++) {
      if (separatorPos(line, separators) >= 0) {
        result[i] = line.substring(0, separatorPos(line, separators));
        line = line.substring(separatorPos(line,
            separators) + separatorLength(line, separators));
      } else {
        result[i] = line;
        line = "";
      }
    }

    return result;
  }

  private static int separatorPos(String line, String[] separators) {
    int result = Integer.MAX_VALUE;

    for (int i = 0; i < separators.length; i++) {
      int pos = line.indexOf(separators[i]);
      if (pos < result && pos >= 0) {
        result = pos;
      }
    }

    if (result == Integer.MAX_VALUE) {
      return -1;
    } else {
      return result;
    }
  }

  private static int separatorLength(String line, String[] separators) {
    return separators[0].length();
  }

  static String[] getAllLines(String snapshot) throws IOException {
    ArrayList result = new ArrayList();

    BufferedReader linesReader = new BufferedReader(new StringReader(snapshot));

    String aLine;
    while ((aLine = linesReader.readLine()) != null) {
      aLine = aLine.trim();
      result.add(aLine);
    }

    linesReader.close();
    removeEmptyLinesFromEnd(result);

    return (String[]) result.toArray(new String[result.size()]);
  }

  private static void removeEmptyLinesFromEnd(List list) {
    while ("".equals(list.get(list.size()).toString())) {
      list.remove(list.size());
    }
  }

  static String[] removeLinesFromBeginning(String[] lines, int amountToRemove) {
    String[] result = new String[lines.length - amountToRemove];

    for (int i = 0; i < result.length; i++) {
      result[i] = lines[i + amountToRemove];

    }
    return result;
  }
}
