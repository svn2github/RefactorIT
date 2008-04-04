/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.jsp;

import net.sf.refactorit.vfs.Source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Map between source code of servlet generated from JSP pages and sources of
 * the pages.
 *
 * <p>
 * The map contains two kinds of mapping information: precise mapping and
 * approximate mapping. Precise mapping maps places in servlet code to exactly
 * same (textually) places in JSP pages. Rough mapping maps areas of servlet
 * code and corresponding areas of JSP pages. Precise mapping covers far less
 * servlet code than rough mapping, which covers most of the code.
 * </p>
 *
 * <p>
 * Precise mapping is meant to be used in Where Used, Rename, etc. to map
 * found occurrences to occurrences in JSP pages. Area of servlet code and
 * corresponding area of JSP page contain exactly same text in this case.
 * Precise mapping is populated via {@link #addExactMapping addExactMapping},
 * and then accessed via {@link #mapArea mapArea}.
 * </p>
 *
 * <p>
 * Rough mapping in indended to be used for error reporting. When errors arise
 * during servlet compilation line(:col) information of servlet code must be
 * roughly mapped to line(:col) information of JSP page in order to present
 * errors in a more user-friendly way. Rough mapping is populated via
 * {@link #addExactMapping addExactMapping} and
 * {@link #addRoughMapping addRoughMapping}, and then accessed via
 * {@link #mapPosition mapPosition}.
 * </p>
 */
public class JspServletSourceMap implements Serializable {

  /** Constructs new map. */
  public JspServletSourceMap() {}

  /**
   * Exact mapping information ({@link Entry} instances).
   */
  private List exactMap = Collections.EMPTY_LIST;

  /**
   * Approximate mapping information ({@link Entry} instances).
   */
  private List roughMap = Collections.EMPTY_LIST;

  public void printExactMap() {
    for (final Iterator i = exactMap.iterator(); i.hasNext(); ) {
      final Entry entry = (Entry) i.next();
      System.err.println(entry.toString());
    }
  }

  /**
   * Adds exact mapping between servlet source code and JSP page source.
   *
   * @param startLine starting line of servlet.
   * @param startColumn starting column on starting line.
   * @param endLine ending line of servlet.
   * @param endColumn ending column on ending line.
   * @param jspArea area of JSP page to which the above area of servlet source
   *        maps.
   */
  public void addExactMapping(int startLine,
      int startColumn,
      int endLine,
      int endColumn,
      JspPageArea jspArea) {
    if (exactMap == Collections.EMPTY_LIST) {
      exactMap = new ArrayList();
    }

    final Entry entry = new Entry();
    entry.startLine = startLine;
    entry.startColumn = startColumn;
    entry.endLine = endLine;
    entry.endColumn = endColumn;
    entry.jspArea = jspArea;

    exactMap.add(entry);
  }

  /**
   * Adds rough mapping between servlet source code and JSP page source.
   *
   * @param startLine starting line of servlet.
   * @param startColumn starting column on starting line.
   * @param endLine ending line of servlet.
   * @param endColumn ending column on ending line.
   * @param jspArea area of JSP page to which the above area of servlet source
   *        maps.
   */
  public void addRoughMapping(int startLine,
      int startColumn,
      int endLine,
      int endColumn,
      JspPageArea jspArea) {
    if (roughMap == Collections.EMPTY_LIST) {
      roughMap = new ArrayList();
    }

    final Entry entry = new Entry();
    entry.startLine = startLine;
    entry.startColumn = startColumn;
    entry.endLine = endLine;
    entry.endColumn = endColumn;
    entry.jspArea = jspArea;

    roughMap.add(entry);
  }

  /**
   * Finds exact mapping from servlet source code area to an area of a JSP page.
   *
   * @param startLine starting line in servlet source.
   * @param startColumn starting column in servlet source on starting line.
   * @param endLine ending line in servlet source.
   * @param endLine ending column in servlet source on ending line.
   *
   * @return area in JSP source corresponding to the area in servlet source;
   *         or <code>null</code> if mapping cannot be established.
   */
  public JspPageArea mapArea(int startLine,
      int startColumn,
      int endLine,
      int endColumn) {

    for (final Iterator i = exactMap.iterator(); i.hasNext(); ) {
      final Entry entry = (Entry) i.next();
      final JspPageArea jspArea = entry.map(startLine,
          startColumn,
          endLine,
          endColumn);
      if (jspArea != null) {
        return jspArea; // Mapping found
      }
    }

    return null; // Mapping not found
  }

  /**
   * Roughly maps position from servlet source code to a position in a JSP page.
   * This mapping is intended to be used for servlet compilation error
   * reporting, when position in servlet where error occurred must be
   * mapped to an approximate position in a JSP page.
   *
   * @param line line in servlet code.
   * @param column column on the line in servlet code.
   *
   * @return area in JSP source corresponding to the position in
   *         servlet source. Area is zero-length if precise mapping could be
   *         established. Approximate (non-zero length) area is returned in
   *         case only rough mapping exists. Returns <code>null</code> if
   *         neither precise nor approximate mapping can be established.
   */
  public JspPageArea mapPosition(int line, int column) {
    // Try precise mapping first. Next, use rough mapping as fallback.
    final JspPageArea preciseResult = mapArea(line, column, line, column);
    if (preciseResult != null) {
      return preciseResult;
    }

    // Precise mapping couldn't be established.
    // Try rough mapping
    for (final Iterator i = exactMap.iterator(); i.hasNext(); ) {
      final Entry entry = (Entry) i.next();
      final JspPageArea jspArea = entry.map(line,
          column,
          line,
          column);
      if (jspArea != null) {
        // Return area of whole entry, since mapping is approximate and
        // we don't know exactly where line:col is.
        return entry.jspArea; // Rough mapping found
      }
    }

    return null; // Mapping couldn't be established
  }

  /**
   * Maps area of servlet code to area of a JSP page.
   */
  private static class Entry implements Serializable {
    /** Starting line. */
    public int startLine;
    /** Starting column on starting line. */
    public int startColumn;
    /** Ending line. */
    public int endLine;
    /** Ending column on ending line. */
    public int endColumn;

    public JspPageArea jspArea;

    /**
     * Maps area of servlet code into area of a JSP page.
     *
     * @param startLine starting line in servlet source.
     * @param startColumn starting column in servlet source on starting line.
     * @param endLine ending line in servlet source.
     * @param endLine ending column in servlet source on ending line.
     *
     * @return area of JSP page correspoding to area in servlet source or
     *         <code>null</code> if this entry does not map the area.
     */
    public JspPageArea map(int startLine,
        int startColumn,
        int endLine,
        int endColumn) {
      // Most of the time the requested area is either outside this area
      // or deeply inside the area. Code is optimized based on the fact.

      if ((startLine < this.startLine) || (endLine > this.endLine)) {
        return null; // Outside
      }

      // System.out.println("Trying: " + this);

      final int jspStartColumn;
      if (startLine > this.startLine) {
        jspStartColumn = startColumn;
      } else {
        // startLine == this.startLine
        jspStartColumn = jspArea.startColumn + (startColumn - this.startColumn);
        if (jspStartColumn < jspArea.startColumn) {
          return null; // Outside
        }
      }

      final int jspEndColumn;
      if (endLine == this.startLine) {
        // Single line servlet area
        jspEndColumn = jspStartColumn + (endColumn - startColumn);
        if ((endLine == this.endLine) && (jspEndColumn > jspArea.endColumn)) {
          return null; // Outside
        }
      } else if (endLine < this.endLine) {
        // Deep inside the area
        jspEndColumn = endColumn;
      } else {
        // endLine == this.endLine
        // endLine != startLine
        jspEndColumn = jspArea.endColumn + (endColumn - this.endColumn);
        if (jspEndColumn > jspArea.endColumn) {
          return null; // Outside
        }
      }

      final int jspStartLine =
          jspArea.startLine + (startLine - this.startLine);
      final int jspEndLine = jspStartLine + (endLine - startLine);

      final JspPageArea result = new JspPageArea();
      result.startLine = jspStartLine;
      result.startColumn = jspStartColumn;
      result.endLine = jspEndLine;
      result.endColumn = jspEndColumn;
      result.page = jspArea.page;
      return result;
    }

    /**
     * Gets string representation of this entry.
     *
     * @return string representation.
     */
    public String toString() {
      final StringBuffer result = new StringBuffer();
      result.append("Entry[");
      result.append(startLine).append(':').append(startColumn);
      result.append(" - ").append(endLine).append(':').append(endColumn);
      result.append(" -> ").append(jspArea);
      result.append(']');
      return result.toString();
    }
  }


  /**
   * Area of JSP page. Line and column numbers start with zero.
   */
  public static class JspPageArea implements Serializable {
    /** Starting line. */
    public int startLine;
    /** Starting column on starting line. */
    public int startColumn;
    /** Ending line. */
    public int endLine;
    /** Ending column on ending line. */
    public int endColumn;

    /** JSP page. */
    public Source page;

    public int hashCode() {
      return startLine;
    }

    /**
     * Checks whether this page area equals with the object.
     *
     * @param o object to check this page area for equality with.
     *
     * @return <code>true</code> if <code>o</code> is instance of
     *         <code>JspPageArea</code> and equals with this area;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof JspPageArea)) {
        return false;
      }

      final JspPageArea other = (JspPageArea) o;
      return ((startLine == other.startLine)
          && (endLine == other.endLine)
          && (startColumn == other.startColumn)
          && (endColumn == other.endColumn)
          && ((page == null)
          ? (other.page == null)
          : (page.equals(other.page))));
    }

    /**
     * Gets string representation of this area.
     *
     * @return string representation.
     */
    public String toString() {
      final StringBuffer result = new StringBuffer();
      result.append("JspPageArea[");
      result.append(startLine).append(':').append(startColumn);
      result.append(" - ").append(endLine).append(':').append(endColumn);
      if (page != null) {
        result.append(", page: ").append(page.getAbsolutePath());
      }
      result.append(']');
      return result.toString();
    }
  }
}
