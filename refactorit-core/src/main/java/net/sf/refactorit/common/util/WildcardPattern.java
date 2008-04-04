/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 *
 * @author  tanel
 */
public final class WildcardPattern {
  private String pattern;

  /** Creates a new instance of WildcardPattern */
  public WildcardPattern(String pattern) {
    this.pattern = pattern;
  }

  public final boolean matches(String str) {
    return matches(pattern, str, true);
  }

  /**
   * Tests whether or not a string matches against a pattern.
   * The pattern may contain two special characters:<br>
   * '*' means zero or more characters<br>
   * '?' means one and only one character
   * <p>
   * Based on code from Apache Ant.
   * </p>
   * @param pattern The pattern to match against.
   *                Must not be <code>null</code>.
   * @param str     The string which must be matched against the pattern.
   *                Must not be <code>null</code>.
   * @param isCaseSensitive Whether or not matching should be performed
   *                        case sensitively.
   *
   *
   * @return <code>true</code> if the string matches against the pattern,
   *         or <code>false</code> otherwise.
   */
  public static boolean matches(String pattern, String str,
      boolean isCaseSensitive) {
    char[] patArr = pattern.toCharArray();
    char[] strArr = str.toCharArray();
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;
    char ch;

    boolean containsStar = false;

    for (int i = 0; i < patArr.length; i++) {
      if (patArr[i] == '*') {
        containsStar = true;

        break;
      }
    }

    if (!containsStar) {
      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }

      for (int i = 0; i <= patIdxEnd; i++) {
        ch = patArr[i];

        if (ch != '?') {
          if (isCaseSensitive && ch != strArr[i]) {
            return false; // Character mismatch
          }

          if (!isCaseSensitive
              && Character.toUpperCase(ch) != Character.toUpperCase(strArr[i])) {
            return false; // Character mismatch
          }
        }
      }

      return true; // String matches against pattern
    }

    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }

    // Process characters before first star
    while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?') {
        if (isCaseSensitive && ch != strArr[strIdxStart]) {
          return false; // Character mismatch
        }

        if (!isCaseSensitive
            && Character.toUpperCase(ch)
            != Character.toUpperCase(strArr[strIdxStart])) {
          return false; // Character mismatch
        }
      }

      patIdxStart++;
      strIdxStart++;
    }

    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }

      return true;
    }

    // Process characters after last star
    while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?') {
        if (isCaseSensitive && ch != strArr[strIdxEnd]) {
          return false; // Character mismatch
        }

        if (!isCaseSensitive
            && Character.toUpperCase(ch)
            != Character.toUpperCase(strArr[strIdxEnd])) {
          return false; // Character mismatch
        }
      }

      patIdxEnd--;
      strIdxEnd--;
    }

    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }

      return true;
    }

    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;

      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;

          break;
        }
      }

      if (patIdxTmp == patIdxStart + 1) {
        // Two stars next to each other, skip the first one.
        patIdxStart++;

        continue;
      }

      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
          for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          ch = patArr[patIdxStart + j + 1];

          if (ch != '?') {
            if (isCaseSensitive && ch != strArr[strIdxStart + i + j]) {
              continue strLoop;
            }

            if (!isCaseSensitive
                && Character.toUpperCase(ch)
                != Character.toUpperCase(strArr[strIdxStart
                + i
                + j])) {
              continue strLoop;
            }
          }
        }

        foundIdx = strIdxStart + i;

        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (patArr[i] != '*') {
        return false;
      }
    }

    return true;
  }

  public final String toString() {
    return pattern;
  }

  public static String arrayToString(WildcardPattern[] array) {
    return StringUtil.mergeArrayIntoString(array, ", ");
  }

  public static WildcardPattern[] stringToArray(String desc) {
    if ((desc == null) || (desc.trim().length() == 0)) {
      return new WildcardPattern[0];
    }
    List result = new ArrayList();
    for (StringTokenizer tokens = new StringTokenizer(desc, ";, ");
        tokens.hasMoreTokens(); ) {
      result.add(new WildcardPattern(tokens.nextToken()));
    }
    return (WildcardPattern[]) result.toArray(new WildcardPattern[0]);
  }

  public final String getPattern() {
    return this.pattern;
  }

  public final void setPattern(final String pattern) {
    this.pattern = pattern;
  }
}
