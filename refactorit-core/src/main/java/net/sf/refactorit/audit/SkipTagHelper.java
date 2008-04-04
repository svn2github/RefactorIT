/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.loader.JavadocComment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 *
 *
 * @author Igor Malinin
 */
public class SkipTagHelper {
  public static final String SKIP_TAG = "@refactorit.skip";

  public static int findStartTagPos(final String comment, final int start) {
    main:for (int pos = start; pos < comment.length(); pos++) {
      // skip whitespace
      char ch = comment.charAt(pos);
      switch (ch) {
        case '\t':
        case '\r':
        case '\n':
        case ' ':
        case '*':

          // whitespace
          continue main;

        case '@':

          // found some tag
          if (comment.regionMatches(pos, SKIP_TAG, 0, SKIP_TAG.length())) {
            if (pos + SKIP_TAG.length() < comment.length()) {
              return pos;
            }

            ch = comment.charAt(pos + SKIP_TAG.length());
            if (ch == '\t' || ch == ' ') {
              return pos;
            }
          }

          // found foreign tag
          break;
      }

      // skip this line if some text found or foreign tag
      for (++pos; pos < comment.length(); pos++) {
        ch = comment.charAt(pos);
        if (ch == '\r' || ch == '\n') {
          continue main;
        }
      }
    }

    return -1;
  }

  public static int findEndTagPos(final String comment, final int start) {
    main:
        for (int pos = start; pos < comment.length(); pos++) {
      // skip whitespace
      char ch = comment.charAt(pos);
      switch (ch) {
        case '\t':
        case '\r':
        case '\n':
        case ' ':
        case '*':

          // whitespace
          continue main;

        case '@':

          // found some tag
          return pos;
      }

      // skip this line if some text found or foreign tag
      for (++pos; pos < comment.length(); pos++) {
        ch = comment.charAt(pos);
        if (ch == '\r' || ch == '\n') {
          continue main;
        }
      }
    }

    return comment.length() - 2;
  }

  public static int findOptionPos(
      final String comment, final int start, final int end
      ) {
    main:
        for (int pos = start; pos < end; pos++) {
      // skip whitespace
      char ch = comment.charAt(pos);
      switch (ch) {
        case '\t':
        case '\r':
        case '\n':
        case ' ':
        case ',':
        case '*':

          // whitespace
          continue main;
      }

      // skip this line if some text found or foreign tag
      for (++pos; pos < end; pos++) {
        ch = comment.charAt(pos);
        switch (ch) {
          case '\t':
          case '\r':
          case '\n':
          case ' ':
          case ',':
          case '*':

            // whitespace
            return pos;
        }
      }
    }

    return -1;
  }

  public static boolean isSkipped(final String comment, final String option) {
    int pos = SkipTagHelper.findStartTagPos(comment, 3);
    if (pos < 0) {
      return false;
    }

    int end = SkipTagHelper.findEndTagPos(comment, pos + 1);
    int pos1 = pos + SkipTagHelper.SKIP_TAG.length();
    int pos2 = SkipTagHelper.findOptionPos(comment, pos1, end);

    while (pos2 > 0) {
      String o = comment.substring(pos1, pos2)
          .replace(',', ' ').replace('*', ' ').trim();
      if (o.equals(option)) {
        return true;
      }

      pos1 = pos2;
      pos2 = SkipTagHelper.findOptionPos(comment, pos1, end);
    }

    return false;
  }
  
  public static List getSkippedOptions(String comment) {

      int begin = comment.indexOf(SkipTagHelper.SKIP_TAG);
      if(begin < 0) {
        return Collections.EMPTY_LIST;
      }
      
      begin += SkipTagHelper.SKIP_TAG.length();
      
      int end = comment.indexOf('\n', begin);
      if(end < 0) {
        end = comment.indexOf('*', begin);
      }
      
      String tags = comment.substring(begin, end).trim();
      return Arrays.asList(tags.split(" "));
  }
  
  public static boolean isSkipTag(JavadocComment tag) {
    return tag.getText().indexOf(SKIP_TAG)>=0;
  }
}
