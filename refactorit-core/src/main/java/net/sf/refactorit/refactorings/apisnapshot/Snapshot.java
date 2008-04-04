/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;


/**
 * Use SnapshotBuilder and SnapshotIO for creating, saving and loading instances of this class.
 *
 * It can serialize/deserialize itself.
 */
public class Snapshot {
  private SnapshotItem[] items;
  private String description;
  private Calendar date;

  private static final int HEADER_LENGTH = 3;

  Snapshot(String description, Calendar date, Set items) {
    this.description = description;
    this.date = date;
    this.items = toArray(items);
  }

  Snapshot(String[] serializedForm) {
    this.description = serializedForm[0];
    this.date = stringToCalendar(serializedForm[1]);

    this.items = SnapshotItem.createItems(SnapshotStringUtil.
        removeLinesFromBeginning(serializedForm, HEADER_LENGTH));
  }

  public SnapshotItem[] getItems() {
    return this.items;
  }

  /** @return   Object() if not found */
  public Object getBinItemForDescription(String desc) {
    for (int i = 0; i < this.items.length; i++) {
      if (this.items[i].getDescription().equals(desc)) {
        return this.items[i].getBinItem();
      }
    }

    return new Object();
  }

  public String getDescription() {
    return this.description;
  }

  public Calendar getDate() {
    return this.date;
  }

  public String getDateAsString() {
    return calendarToString(this.getDate());
  }

  private static Calendar stringToCalendar(String string) {
    String[] calendarItems = SnapshotStringUtil.split(string, new String[] {":",
        ".", " "}
        , 5);

    return new GregorianCalendar(
        Integer.parseInt(calendarItems[2]),
        Integer.parseInt(calendarItems[0]) - 1,
        Integer.parseInt(calendarItems[1]),
        Integer.parseInt(calendarItems[3]),
        Integer.parseInt(calendarItems[4])
        );
  }

  private static String calendarToString(Calendar calendar) {
    return
        addNumberPadding(calendar.get(Calendar.MONTH) + 1) + "." +
        addNumberPadding(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
        calendar.get(Calendar.YEAR) + " " +
        addNumberPadding(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
        addNumberPadding(calendar.get(Calendar.MINUTE));
  }

  private static String addNumberPadding(int a) {
    if (a < 10) {
      return "0" + a;
    } else {
      return "" + a;
    }
  }

  String[] getSerializedForm() {
    String[] result = new String[HEADER_LENGTH + this.items.length];

    result[0] = this.description;
    result[1] = getDateAsString();
    result[2] = "";

    for (int i = 0; i < items.length; i++) {
      result[i + HEADER_LENGTH] = this.items[i].getSerializedForm();
    }

    return result;
  }

  private static SnapshotItem[] toArray(Collection items) {
    SnapshotItem[] result = new SnapshotItem[items.size()];

    Iterator itemIterator = items.iterator();
    for (int i = 0; i < items.size(); i++) {
      result[i] = (SnapshotItem) itemIterator.next();
    }

    return result;
  }
}
