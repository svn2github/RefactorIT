/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.loader.Comment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Comparator;


public class TimestampBasedCommentComparator implements Comparator {
  private DateFormat format;

  public TimestampBasedCommentComparator(DateFormat format) {
    this.format = format;
  }

  public int compare(java.lang.Object a, java.lang.Object b) {
    Calendar cal1 = CommentBodyEditor.extractTimestamp(((Comment) a).getText(),
        format);
    Calendar cal2 = CommentBodyEditor.extractTimestamp(((Comment) b).getText(),
        format);

    if (cal1 == null && cal2 == null) {
      return 0;
    } else if (cal1 == null) {
      return 1;
    } else if (cal2 == null) {
      return -1;
    }

    if (cal1.before(cal2)) {
      return 1;
    } else if (cal2.before(cal1)) {
      return -1;
    } else {
      return 0;
    }
  }

  public boolean equals(java.lang.Object obj) {
    return obj == this;
  }
}
