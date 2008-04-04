/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.source.format.BinFormatter;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * A <code>RefactoringStatus</code> object represents the outcome of a
 * checking/refactoring operation.
 * It keeps a list of <code>Entries</code>.
 * Clients can instantiate.
 *
 * @author Anton Safonov
 */
public final class RefactoringStatus {
  /** same as OK, but doesn't show the icon; for those single message entries */
  public static final int UNDEFINED = -1;
  public static final int OK = 0;
  /** INFO like WARNING but less dangerous */
  public static final int INFO = 1;
  /** WARNING is when light problem detected which however doesn't prevent refactoring from being run */
  public static final int WARNING = 2;
  /** QUESTION notifies action that user has a choice */
  public static final int QUESTION = 3;
  /** ERROR when check detected a problem in users source code which prevents refactoring from being run at all */
  public static final int ERROR = 4;
  /** FATAL is for our logic errors, e.g. when editor failed with coordinates */
  public static final int FATAL = 5;
  /** CANCEL notifies action that checking algorithm requested undeclineble stop */
  public static final int CANCEL = 6;

  private List entries = new ArrayList(1);

  private int severity = OK;

  public final class Entry {
    int severity = UNDEFINED;

    private int parentSeverity = severity;

    private String message;

    /** To be able to click on a message and go to the source, can be e.g.
     * BinItem or CompilationUnit */
    private Object bin;

    private List subEntries = new ArrayList(1);
    private Conflict conflict;
    private static final String NEWLINE = "\n";

    /** Message with an icon */
    public Entry(String message, int severity) {
      if (Assert.enabled) {
        Assert.must(severity >= UNDEFINED && severity <= CANCEL,
            "Wrong severity: " + severity);
        Assert.must(message != null, "Message is null");
      }

      this.message = message;
      this.severity = severity;
    }

    /** Clickable message with an icon */
    public Entry(String message, int severity, Object bin) {
      this(message, severity);
      this.bin = bin;
    }

    /** Message without any icon */
    public Entry(String message) {
      this(message, UNDEFINED);
    }

    public Entry(Object bin) {
      this(bin, UNDEFINED);
    }

    public Entry(Object bin, int severity) {
      this.bin = bin;
      this.severity = severity;
    }

    /** Just a string node */
    public Entry addSubEntry(Entry subEntry) {
      subEntries.add(subEntry);
      return subEntry;
    }

    /** Just a string node */
    public Entry addSubEntry(String message) {
      final Entry subEntry = new Entry(message);
      subEntry.parentSeverity = getParentSeverity();
      subEntries.add(subEntry);
      return subEntry;
    }

    /** Clickable BinItem node */
    public Entry addSubEntry(Object bin) {
      final Entry subEntry = new Entry((Object)null);
      subEntry.bin = bin;
      subEntry.parentSeverity = getParentSeverity();
      subEntries.add(subEntry);
      return subEntry;
    }

    /** Clickable string node */
    public Entry addSubEntry(String message, Object bin) {
      return addSubEntry(message, UNDEFINED, bin);
    }

    /** Clickable string node */
    public Entry addSubEntry(String message, int severity, Object bin) {
      final Entry subEntry = new Entry(message);
      subEntry.bin = bin;
      subEntry.severity = severity;
      subEntry.parentSeverity = getParentSeverity();

      subEntries.add(subEntry);
      return subEntry;
    }

    public void addSubEntries(List items) {
      addSubEntries(items, this);
    }

    public void addSubEntries(List items, Entry entry) {
      for (int i = 0, max = items.size(); i < max; i++) {
        entry.addSubEntry(items.get(i));
      }
    }

    public void addSubEntries(HashMap items) {
      addSubEntries(items, this);
    }

    public void addSubEntries(HashMap items, Entry entry) {
      List keys = new ArrayList(items.keySet());

      for (int i = 0, max = keys.size(); i < max; i++) {
        Object key = keys.get(i);
        Object value = items.get(key);

        Entry subEntry = entry.addSubEntry(key);

        if (value instanceof HashMap) {
          addSubEntries((HashMap) value, subEntry);
        } else if (value instanceof List) {
          addSubEntries((List) value, subEntry);
        }
      }
    }

    public void setConflict(Conflict conflict) {
      this.conflict = conflict;
    }

    public Conflict getConflict() {
      return conflict;
    }

    public String getMessage() {
      if (message == null) {
        if (bin instanceof BinMethod) {
          message = ((BinMember) bin).getOwner().getBinCIType()
              .getNameWithAllOwners();
          message += '.' + BinFormatter.formatWithoutReturn((BinMethod) bin);
        } else if (bin instanceof BinMember) {
          message = ((BinMember) bin).getNameWithAllOwners();
        } else if (bin instanceof BinTypeRef) {
          message = ((BinTypeRef) bin).getQualifiedName();
        } else if (bin instanceof BinPackage) {
          message = ((BinPackage) bin).getQualifiedName();
        } else if (bin instanceof CompilationUnit) {
          message = ((CompilationUnit) bin).getSource().getDisplayPath();
        } else if (bin instanceof BinSelection) {
          message = ((BinSelection) bin).getText();
        } else if (bin instanceof Exception) {
          message = ((Exception) bin).getMessage();
        } else {
          try {
            message = bin.toString();
          } catch (NullPointerException e) {
            message = "<empty message>";
          }
        }
      }

      if (message == null) {
        message = "<empty message>";
      }

      return this.message;
    }

    public String getAllMessages() {
      final StringBuffer result = new StringBuffer(getMessage());
      for (int i = 0, max = this.subEntries.size(); i < max; i++) {
        Entry entry = (Entry)this.subEntries.get(i);
        result.append(NEWLINE);
        String subMessages = entry.getAllMessages();
        StringTokenizer subRows = new StringTokenizer(subMessages, NEWLINE);
        while (subRows.hasMoreTokens()) {
          result.append("  ").append(subRows.nextToken()).append(NEWLINE);
        }
      }

      return result.toString();
    }

    public int getSeverity() {
      return this.severity;
    }

    public int getParentSeverity() {
      if (this.severity != UNDEFINED) {
        return this.severity;
      } else {
        return this.parentSeverity;
      }
    }

    public Object getBin() {
      return bin;
    }

    public List getSubEntries() {
      return this.subEntries;
    }

    /** For compatibility with legacy code */
    public List getItems() {
      List result;
      if (subEntries != null) {
        result = new ArrayList(subEntries.size());
        for (int i = 0, max = subEntries.size(); i < max; i++) {
          result.add(((Entry) subEntries.get(i)).getBin());
        }
      } else {
        result = CollectionUtil.EMPTY_ARRAY_LIST;
      }
      return result;
    }

    public String toString() {
      return getMessage();
    }
  }


  public RefactoringStatus() {
  }

  public RefactoringStatus(String message, int severity) {
    addEntry(message, severity);
  }

  public RefactoringStatus(String message, List items, int severity) {
    addEntry(message, items, severity);
  }

  public RefactoringStatus(String message, int severity, HashMap mapItems) {
    addEntry(message, mapItems, severity);
  }

  /**
   * @param bin needed to be able to show clickable/gotoable message,
   * message is autogenerated from the bin
   */
  public RefactoringStatus.Entry addEntry(Object bin) {
    return addEntry(new Entry(bin));
  }

  /**
   * @param bin needed to be able to show clickable/gotoable message,
   * message is autogenerated from the bin
   */
  public RefactoringStatus.Entry addEntry(Object bin, int severity) {
    return addEntry(new Entry(bin, severity));
  }

  /**
   * @param bin needed to be able to show clickable/gotoable message
   */
  public RefactoringStatus.Entry addEntry(String message, int severity,
      Object bin) {
    return addEntry(new Entry(message, severity, bin));
  }

  public RefactoringStatus.Entry addEntry(String message, int severity) {
    return addEntry(new Entry(message, severity));
  }

  /**
   * @param items items affected by this entry
   */
  public RefactoringStatus.Entry addEntry(String message, List items,
      int severity) {
    Entry entry = addEntry(new Entry(message, severity));
    entry.addSubEntries(items);

    return entry;
  }

  /**
   * @param items map, there key is subentry for the given message and values
   * (List or Map) are subentries for the key entry, i.e. the following
   * structure is formed:<pre>
   * message
   *   key1
   *     value1
   *     value2
   *   key2
   *     value3
   *     value4
   * </pre>
   */
  public RefactoringStatus.Entry addEntry(String message, HashMap items,
      int severity) {
    Entry entry = addEntry(new Entry(message, severity));
    entry.addSubEntries(items);

    return entry;
  }

  protected RefactoringStatus.Entry addEntry(final Entry entry) {
    CollectionUtil.addNew(this.entries, entry);
    this.severity = Math.max(this.severity, entry.severity);

    return entry;
  }

  /**
   * Merges the receiver and the parameter statuses.
   * The resulting list of entries in the receiver will contain entries from
   * both.
   * The resuling severity in the reciver will be the more severe of its current
   * severity and the parameter's severity.
   * Merging with <code>null</code> is allowed - it has no effect.
   */
  public RefactoringStatus merge(RefactoringStatus that) {
    if (this == that) {
      return this;
    }

    if (that != null) {
      this.entries.addAll(that.getEntries());
      this.severity = Math.max(this.severity, that.getSeverity());
    }

    return this;
  }

  /**
   * Returns the current severity.
   * Severities are ordered as follows:<br>
   * <code>OK &lt; INFO &lt; WARNING &lt; ERROR &lt; FATAL &lt; CANCEL</code>
   */
  public int getSeverity() {
    return this.severity;
  }

  public boolean isOk() {
    return this.severity == OK || this.severity == UNDEFINED;
  }

  public boolean isInfoOrWarning() {
    return this.severity == INFO || this.severity == WARNING;
  }

  public boolean isInfo() {
    return this.severity == INFO;
  }

  public boolean isQuestion() {
    return this.severity == QUESTION;
  }

  public boolean isErrorOrFatal() {
    return this.severity == ERROR || this.severity == FATAL;
  }

  public boolean isCancel() {
    return this.severity == CANCEL;
  }

  public boolean hasSomethingToShow() {
    for (int i = 0, max = this.entries.size(); i < max; i++) {
      if (((Entry) this.entries.get(i)).getMessage().trim().length() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all entries.
   * Returns a List of <code>RefactoringStatus$Entry</code>.
   * This list is empty if there are no entries.
   */
  public List getEntries() {
    return entries;
  }

  public int getEntriesNum() {
    return entries.size();
  }

  /**
   * Returns the first entry which severity is equal or greater than the given
   * severity. Returns <code>null</code> if no element exists with
   * the given severity.
   * @param severity must be one of <code>FATAL</code>, <code>ERROR</code>,
   * 	<code>WARNING</code> or <code>INFO</code>.
   */
  private Entry getFirstEntry(int severity) {
    Assert.must(severity >= UNDEFINED && severity <= CANCEL,
        "Wrong severity: " + severity);
    if (severity > severity) {
      return null;
    }

    Iterator iter = entries.iterator();
    while (iter.hasNext()) {
      Entry entry = (Entry) iter.next();
      if (entry.getSeverity() >= severity) {
        return entry;
      }
    }
    return null;
  }

  /**
   * @param severity must me one of <code>FATAL</code>, <code>ERROR</code>,
   * 	<code>WARNING</code> or <code>INFO</code>.
   * @return the first message which severity is equal or greater than the given
   * severity. Returns <code>null</code> if no element exists with the given
   * severity.
   */
  public String getFirstMessage(int severity) {
    Entry entry = getFirstEntry(severity);
    if (entry == null) {
      return null;
    }
    return entry.getMessage();
  }

  /**
   * @return the first message with current severity,
   * <code>null</code> if no entries exists.
   */
  public String getFirstMessage() {
    Entry entry = getFirstEntry(this.severity);
    if (entry == null) {
      return null;
    }
    return entry.getMessage();
  }

  /**
   * @return returns all messages from all entries,
   * <code>null</code> if no entries exists.
   */
  public String getAllMessages() {
    StringBuffer result = new StringBuffer();

    for (int i = 0, max = entries.size(); i < max; i++) {
      final Entry entry = (Entry) entries.get(i);
      if (entry != null) {
        final String message = entry.getAllMessages();
        if (message != null && message.length() > 0) {
          if (result.length() > 0) {
            result.append("\n\n");
          }
          result.append(message);
        }
      }
    }

    return result.toString();
  }

  // FIXME: rename to getMessageType
  public int getJOptionMessageType() {
    switch (getSeverity()) {
      case RefactoringStatus.UNDEFINED:
      case RefactoringStatus.OK:
      case RefactoringStatus.INFO:
      case RefactoringStatus.CANCEL:
        return JOptionPane.INFORMATION_MESSAGE;

      case RefactoringStatus.WARNING:
        return JOptionPane.WARNING_MESSAGE;

      case RefactoringStatus.QUESTION:
        return JOptionPane.QUESTION_MESSAGE;

      default:
        return JOptionPane.ERROR_MESSAGE;
    }
  }

  private static String getSeverityString(int severity) {
    Assert.must(severity >= UNDEFINED && severity <= CANCEL,
        "Wrong severity: " + severity);
    switch (severity) {
      case UNDEFINED:
        return "UNDEFINED";
      case OK:
        return "OK";
      case INFO:
        return "INFO";
      case WARNING:
        return "WARNING";
      case QUESTION:
        return "QUESTION";
      case ERROR:
        return "ERROR";
      case FATAL:
        return "FATALERROR";
      case CANCEL:
        return "CANCEL";
    }
    return null;
  }

  public void becomeCloneOf(RefactoringStatus other) {
    this.entries = new ArrayList(other.entries);
    this.severity = other.severity;
  }

  public void clear() {
    entries.clear();
    this.severity = UNDEFINED;
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("<")
        .append(getSeverityString(this.severity))
        .append("\n");
    for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
      buff.append("\t").append(iter.next()).append("\n");
    }
    buff.append(">");

    return buff.toString();
  }

  public void setSeverity(int severity) {
    this.severity = severity;
  }

}
