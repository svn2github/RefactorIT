/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.source.preview.Diff;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class Line {

  private static final boolean debug = false;

  private final StringBuffer line;
  private final String originalContent;
  private List shifts = null;

  private String markedNewContent = null;
  private String markedOldContent = null;

  private boolean changed;

  private class Shift {
    public int index;
    public int diff;

    public Shift(int index, int diff) {
      this.index = index;
      this.diff = diff;
    }

    public String toString() {
      return index + " - " + (diff > 0 ? "+" : "") + diff;
    }
  }


  public Line(final StringBuffer buffer) {
    this.line = buffer;
    this.originalContent = buffer.toString();
  }

  public Line(final String line) {
    this.line = new StringBuffer(line);
    this.originalContent = line;
  }

  public String getContent() {
    return line.toString();
  }

  /** @return <code>0</code> if the char was deleted already */
  public char charAt(int index) {
    if (this.line.length() == 0) {
      return 0;
    }

    final int newIndex = getNewIndex(index, true);
    if (newIndex >= this.line.length() || newIndex < 0) {
      return 0;
    }

    return this.line.charAt(newIndex);
  }

  public void insert(int offset, String str) {
    changed = true;
    final int newOffset = getNewEndIndex(offset);
    this.line.insert(newOffset, str);
    addShift(newOffset, str.length());
  }
  
  public void delete(final int start, final int end) {
    changed = true;
    final int newStart = getNewIndex(start);
    final int newEnd = getNewIndex(end);
    this.line.delete(newStart, newEnd);
    addShift(newEnd, newStart - newEnd);
  }

  public void clear() {
    delete(0, length());
  }

  public void append(String str) {
    changed = true;
    // this shouldn't affect old indexes
    this.line.append(str);
  }

  public int length() {
//    recreates original line length
//    int length = this.line.length();
//    for (int i = 0, max = shifts.size(); i < max; i++) {
//      final Shift shift = (Shift) shifts.get(i);
//      length -= shift.diff;
//    }
//    return length;

    return this.originalContent.length();
  }

  public String substring(int start, int end) {
    if (this.line.length() == 0) {
      return this.line.toString();
    }

    int newStartIndex = getNewIndex(start);
    if (newStartIndex > this.line.length()) {
      newStartIndex = this.line.length();
    }
    int newEndIndex = getNewIndex(end);
    if (newEndIndex > this.line.length()) {
      newEndIndex = this.line.length();
    }

    return this.line.substring(newStartIndex, newEndIndex);
  }

  public String substring(int start) {
    return substring(start, this.originalContent.length());
  }

  public void replace(final int start, final int end, final String str) {
    changed = true;
    final int newStart = getNewStartIndex(start);
    final int newEnd = getNewEndIndex(end);
    this.line.replace(newStart, newEnd, str);
    addShift(newEnd, newStart - newEnd + str.length());
  }

  private final void addShift(final int index, final int diff) {
    if (diff == 0) {
      return;
    }

    if (shifts == null) {
      shifts = new ArrayList(3);
    }

    shifts.add(new Shift(index, diff));
  }

  public int getNextIndex(int oldIndex) {
    int addressableOldIndex = oldIndex;
    int newIndex = getNewIndex(addressableOldIndex);
    while (addressableOldIndex < length()
        && getNewIndex(++addressableOldIndex) == newIndex) {}

    return addressableOldIndex;
  }

  public int getPreviousIndex(int oldIndex) {
    int addressableOldIndex = oldIndex;
    int newIndex = getNewIndex(addressableOldIndex);
    while (addressableOldIndex > 0
        && getNewIndex(--addressableOldIndex) == newIndex) {}

    if (getNewIndex(addressableOldIndex) == newIndex) {
      --addressableOldIndex;
    }

    return addressableOldIndex;
  }

  private final int getNewStartIndex(final int oldIndex) {
    final int newIndex = getNewIndex(oldIndex);

    if (newIndex >= line.length()) {
      throw new IndexOutOfBoundsException(
          "Requested an index beyond the end of line: "
          + newIndex + ", line: \"" + toString() + "\"");
    }

    return newIndex;
  }

  private final int getNewEndIndex(final int oldIndex) {
    final int newIndex = getNewIndex(oldIndex);

    if (newIndex > line.length()) {
      throw new IndexOutOfBoundsException(
          "Requested an index beyond the end of line: "
          + newIndex + ", line: \"" + toString() + "\"");
    }

    return newIndex;
  }

  private final int getNewIndex(final int oldIndex) {
    return getNewIndex(oldIndex, false);
  }

  private final int getNewIndex(final int oldIndex,
      final boolean returnNegativeForDeleted) {

    int newIndex = oldIndex;
    
    
    if (debug) {
      System.err.println("asked for: " + oldIndex + ", line: " + this.toString());
    }

    
    if (shifts != null) {  
      for (int i = 0, max = shifts.size(); i < max; i++) {
        final Shift shift = (Shift) shifts.get(i);
        if (debug) {
          System.err.println("newIndex before: " + newIndex + ", shift: " + shift);
        }
        if (shift.diff > 0) {
          if (newIndex >= shift.index) {
            newIndex += shift.diff;
          } else {
            // no change
          }
        } else {
          if (newIndex >= shift.index) {
            newIndex += shift.diff;
          } else if (newIndex == shift.index + shift.diff) {
            // it is ok here - we can delete and then insert into the same position
          } else if (newIndex > shift.index + shift.diff) {
            if (returnNegativeForDeleted) {
              newIndex = -1;
              break;
            } else {
              // all indexes of deleted interval will collapse to its start
              newIndex += (shift.index - newIndex) + shift.diff;
            }
          }
        }
  
        if (debug) {
          System.err.println("newIndex after: " + newIndex);
        }
      }
  
      if (debug) {
        System.err.println("getNewIndex: " + oldIndex + " -> " + newIndex
            + ", line: " + this.toString());
      }
    }
    
    if(newIndex>line.length()){
      newIndex = 0;
    }
    
    return newIndex;
  }

  public boolean isChanged() {
    if (changed){
      changed = !originalContent.equals(getContent()); 
    }
    return this.changed;
  }

  public void restoreOriginalContent() {
    this.line.replace(0, this.line.length(), this.originalContent);
    this.changed = false;
  }

  public boolean isImportStatement() {
    return StringUtil.startsWith(this.line, "import ") ||
        StringUtil.startsWith(new StringBuffer(getOriginalContent()), "import ");
  }

  public String toString() {
    return StringUtil.printableLinebreaks(getContent());
  }

  public static final class TestDriver extends TestCase {

    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("Line tests");
      return suite;
    }

    protected void setUp() throws Exception {
      FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    }

    public void testReplaceDelete() throws Exception {
      cat.info("Testing single line replace/delete.");
      Line line = new Line("1234abcd");

      line.replace(0, 4, "12345678");

      assertEquals("12345678abcd", line.getContent());
      assertEquals("char at 3", '4', line.charAt(3));
      assertEquals("char at 4", 'a', line.charAt(4));
      assertEquals("abcd", line.substring(4, 8));
      assertEquals("12345678", line.substring(0, 4));

      line.delete(4, 8);

      assertEquals("12345678", line.getContent());

      line.delete(0, 4);

      assertEquals("", line.getContent());

      cat.info("SUCCESS");
    }

    public void testInsertDelete() throws Exception {
      cat.info("Testing single line insert/delete.");
      Line line = new Line("1234abcd");
      line.insert(4, "5678");

      assertEquals("12345678abcd", line.getContent());
      assertEquals("char at 3", '4', line.charAt(3));
      assertEquals("char at 4", 'a', line.charAt(4));
      assertEquals("abcd", line.substring(4, 8));
      assertEquals("12345678", line.substring(0, 4));

      line.delete(2, 6);

      assertEquals("12cd", line.getContent());

      line.insert(2, "xy");

      assertEquals("char at 6", 'c', line.charAt(6));
      assertEquals("cd", line.substring(6, 8));
      assertEquals("12xycd", line.getContent());

      line.delete(6, 8);

      assertEquals("12xy", line.getContent());

      cat.info("SUCCESS");
    }

    public void testRequestOfDeleted() throws Exception {
      cat.info("Testing request of position in already deleted interval.");
      Line line = new Line("1234abcd");

      line.delete(4, 8);

      assertEquals("char at 4", 0, line.charAt(4));

      assertEquals("char at 3", '4', line.charAt(3));

      line.insert(4, "efgh");

      assertEquals("char at 4", 0, line.charAt(4));

      assertEquals("1234efgh", line.substring(0, 4));
      assertEquals("4efgh", line.substring(3, 4));
      assertEquals("", line.substring(4));
      assertEquals("", line.substring(5, 8));
      assertEquals("1234efgh", line.getContent());

      line.insert(4, "ij");

      assertEquals("1234efghij", line.substring(0, 4));
      assertEquals("1234efghij", line.getContent());

      line.delete(0, 4);

      assertEquals("char at 1", 0, line.charAt(1));
      assertEquals("", line.substring(2, 5));

      cat.info("SUCCESS");
    }

  }

  public String getOriginalContent() {
    return this.originalContent;
  }


  public String getMarkedNewContent() {
    if(this.markedNewContent == null) {
      applyDiff();
    }
    return this.markedNewContent;
  }

  public String getMarkedOldContent() {
    if(this.markedOldContent == null) {
      applyDiff();
    }
    return this.markedOldContent;
  }

  private void applyDiff() {
    String oldContent = this.originalContent;
    String newContent = this.getContent();

    Diff diff = new Diff(oldContent, newContent);

    diff.runDiff();

    this.markedNewContent = diff.getMarkedIsNow();
    this.markedOldContent = diff.getMarkedWas();
  }

}
