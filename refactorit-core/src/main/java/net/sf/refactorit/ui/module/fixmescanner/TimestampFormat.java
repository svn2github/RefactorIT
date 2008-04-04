/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TimestampFormat {
  private int style;
  private Locale locale;
  private String displayString;

  private static TimestampFormat[] availableFormats = null;

  public static final TimestampFormat DEFAULT = new TimestampFormat(
      DateFormat.SHORT, Locale.getDefault());
  private static final String OPTION_NAME = "FixmeScanner_timestampFormat";

  public TimestampFormat(int dateFormatStyle, Locale dateFormatLocale) {
    this.style = dateFormatStyle;
    this.locale = dateFormatLocale;

    // Cached for speed
    this.displayString = this.locale.getDisplayName() + ": "
        + getDateFormat().format(new Date());
  }

  public DateFormat getDateFormat() {
    return DateFormat.getDateInstance(style, locale);
  }

  public String getDisplayString() {
    return this.displayString;
  }

  public String toString() {
    return getDisplayString();
  }

  public boolean equals(Object o) {
    if (o instanceof TimestampFormat) {
      return ((TimestampFormat) o).getDisplayString().equals(getDisplayString());
    } else {
      return false;
    }
  }

  public static TimestampFormat[] getAvailableFormatsSortedAlphabetically() {
    if (availableFormats != null) {
      return availableFormats;
    }

    List result = new ArrayList();

    Locale[] locales = DateFormat.getAvailableLocales();
    int[] styles = new int[] {DateFormat.SHORT, DateFormat.MEDIUM,
        DateFormat.LONG, DateFormat.FULL};

    for (int style = 0; style < styles.length; style++) {
      for (int locale = 0; locale < locales.length; locale++) {
        result.add(new TimestampFormat(styles[style], locales[locale]));
      }
    }

    Collections.sort(result, new StringUtil.ToStringComparator());

    availableFormats = (TimestampFormat[]) result.toArray(new TimestampFormat[0]);
    return availableFormats;
  }

  private String serializeToString() {
    List result = new ArrayList();
    result.add("" + this.style);
    result.add(this.locale.getLanguage());
    result.add(this.locale.getCountry());
    result.add(this.locale.getVariant());
    return StringUtil.serializeStringList(result);
  }

  private static TimestampFormat deserializeFromString(String s) {
    List result = StringUtil.deserializeStringList(s);

    try {
      return new TimestampFormat(
          Integer.parseInt(result.get(0).toString()),
          deserializeLocale(result.subList(1, result.size()))
          );
    } catch (NumberFormatException e) {
      return DEFAULT;
    } catch (IndexOutOfBoundsException e) {
      return DEFAULT;
    } catch (IllegalArgumentException e) {
      return DEFAULT;
    }
  }

  private static Locale deserializeLocale(List items) {
    if (items.size() == 1) {
      return new Locale(items.get(0).toString(), "");
    } else if (items.size() == 2) {
      return new Locale(items.get(0).toString(), items.get(1).toString());
    } else if (items.size() == 3) {
      return new Locale(items.get(0).toString(), items.get(1).toString(),
          items.get(2).toString());
    } else {
      throw new IllegalArgumentException("Bad list size: " + items.size());
    }
  }

  public static TimestampFormat load() {
    String result = GlobalOptions.getOption(OPTION_NAME);
    if (result == null) {
      return DEFAULT;
    }

    return deserializeFromString(result);
  }

  public void save() {
    GlobalOptions.setOption(OPTION_NAME, serializeToString());
  }
}
