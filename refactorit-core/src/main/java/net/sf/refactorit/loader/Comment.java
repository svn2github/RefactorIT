/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.classmodel.AbstractLocationAware;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.refactorings.LocationAwareImpl;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.UserFriendlyError;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Comment extends AbstractLocationAware implements Comparable,
    Externalizable {

  private transient WeakReference sourceRef = new WeakReference(null);

  private String body;
  private int startLine;
  private int startColumn;
  private int endLine;
  private int endColumn;

  // for serialization
  public Comment() {
  }

  public void readExternal(java.io.ObjectInput s) throws ClassNotFoundException,
      IOException {
    body = s.readUTF();
//    if(body != null) body = body.intern();
    startLine = s.readInt();
    startColumn = s.readInt();
    endLine = s.readInt();
    endColumn = s.readInt();
  }

  public void writeExternal(java.io.ObjectOutput s) throws IOException {
    s.writeUTF(body);
    s.writeInt(startLine);
    s.writeInt(startColumn);
    s.writeInt(endLine);
    s.writeInt(endColumn);
  }

  public Comment(String body, int startLine, int startColumn) {
    this(body, startLine, startColumn, -1, -1);
  }

  public Comment(String body, int startLine, int startColumn,
      int endLine, int endColumn) {
    this.body = body;
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public final String getText() {
    return this.body;
  }

  // FIXME comment should be immutable - refactoring itself should care about editing and rebuilding!!!
  public final void changeText(String newText) {
    endColumn += (newText.length() - body.length());
    this.body = newText;
  }

  /**
   * Gets line number this comment starts on.
   *
   * @return line number.
   *
   * @see #getStartColumn
   * @see #getEndLine
   * @see #getEndColumn
   */
  public final int getStartLine() {
    return this.startLine;
  }

  /**
   * Gets column this comment starts on.
   *
   * @return column.
   *
   * @see #getStartLine
   * @see #getEndLine
   * @see #getEndColumn
   */
  public final int getStartColumn() {
    return this.startColumn;
  }

  /**
   * Gets line number this comment ends on.
   *
   * @return line number.
   *
   * @see #getStartLine
   * @see #getStartColumn
   * @see #getEndColumn
   */
  public final int getEndLine() {
    ensureEndKnown();
    return endLine;
  }

  /**
   * Gets last column taken up by this comment on its last line.
   *
   * @return column.
   *
   * @see #getStartLine
   * @see #getStartColumn
   * @see #getEndLine
   */
  public final int getEndColumn() {
    ensureEndKnown();
    return endColumn;
  }

  
  public final BinItem getOwner() {
    OwnerSearchVisitor visitor = new OwnerSearchVisitor(this);
    getCompilationUnit().accept(visitor);
    return visitor.getOwner();
  }
  
  /**
   * Gets source file where this item is located.
   *
   * @return source file or <code>null</code> if it is not known.
   */
  public final CompilationUnit getCompilationUnit() {
    return (CompilationUnit)this.sourceRef.get();
  }

  /**
   * Sets source file where this item is located.
   *
   * @param source source file.
   */
  public final void setCompilationUnit(CompilationUnit source) {
    this.sourceRef = new WeakReference(source);
    invalidateCache();
  }

  public void invalidateCache() {
    // nothing, cleans up caches in JavadocComment
  }

  /**
   * Finds coordinates of the end of this comment.
   * Ensures that {@link #getEndLine getEndLine} and
   * {@link #getEndColumn getEndColumn} return meaningful values.
   */
  private void ensureEndKnown() {
    if (endLine > 0) {
      // Already found
      return;
    }

    if (startLine < 0 || startColumn < 0) {
      // invalidated
      return;
    }

    if (getText() == null) {
      endLine = startLine;
      endColumn = startColumn;
      return;
    }

    BufferedReader in = null;
    try {
      in = new BufferedReader(new StringReader(getText()));
      String tempLine;
      String lastLine = null;
      endLine = startLine - 1;
      while ((tempLine = in.readLine()) != null) {
        lastLine = tempLine;
        endLine++;
      }

      if (lastLine == null) {
        // No lines read at all...
        endLine = startLine;
        endColumn = startColumn;
        return;
      }

      if (endLine == startLine) {
        endColumn = startColumn + lastLine.length();
      } else {
        endColumn = lastLine.length() + 1;
      }
    } catch (IOException e) {
      throw new ChainableRuntimeException(
          "Failed to find end of comment (" + getStartLine() + ":"
          + getStartColumn(),
          e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          throw new ChainableRuntimeException(
              "Failed to close comment text input stream",
              e);
        }
        in = null;
      }
    }
  }

  /**
   * Gets string representation of this comment.
   *
   * @return string represenation.
   */
  public final String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": \""
        + StringUtil.printableLinebreaks(getText()) + "\" "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn();
  }

  /**
   * Checks only for files and positions, should be enough.
   * @param o another comment to compare with
   */
  public final int compareTo(Object o) {
    int res = this.getCompilationUnit().getSource().getAbsolutePath().compareTo(
        ((Comment) o).getCompilationUnit().getSource().getAbsolutePath());

    if (res == 0) {
      res = this.getStartLine() - ((Comment) o).getStartLine();
      if (res == 0) {
        res = this.getStartColumn() - ((Comment) o).getStartColumn();
      }
    }

    return res;
  }

  public static final List getCommentsIn(final LocationAware la) {
    final CompilationUnit sf = la.getCompilationUnit();
    if (sf == null) {
      return null;
    }

    final List comments = new ArrayList(sf.getJavadocComments());
    comments.addAll(sf.getSimpleComments());

    Collections.sort(comments, LocationAware.PositionSorter.getInstance());
    Iterator it = comments.iterator();
    while (it.hasNext()) {
      Comment comment = (Comment) it.next();
      if (!la.contains(comment)) {
        it.remove();
      }
    }

    return comments;
  }
  
  public static final List getCommentsIn(CompilationUnit sf, 
      int startLine, int startCol, int endLine, int endCol) {
    if (sf == null) {
      return null;
    }

    final List comments = new ArrayList(sf.getJavadocComments());
    comments.addAll(sf.getSimpleComments());

    Collections.sort(comments, LocationAware.PositionSorter.getInstance());
    Iterator it = comments.iterator();
    while (it.hasNext()) {
      Comment comment = (Comment) it.next();
      int sLine = comment.getStartLine();
      int sCol = comment.getStartColumn();
      int eLine = comment.getEndLine();
      int eCol = comment.getEndColumn();
      if(!((sLine >startLine || (sLine == startLine && sCol >= startCol))
          && (eLine < endLine || (eLine == endLine && eCol <= endCol)))) {
        it.remove();
      }
    }
    return comments;
  }
  
  public static final List getCommentsInAndAfter(final LocationAware la) {
    final CompilationUnit sf = la.getCompilationUnit();
    if (sf == null) {
      return null;
    }

    final List comments = new ArrayList(sf.getJavadocComments());
    comments.addAll(sf.getSimpleComments());
    int laEndLine = la.getEndLine();
    
    Collections.sort(comments, LocationAware.PositionSorter.getInstance());
    Iterator it = comments.iterator();
    while (it.hasNext()) {
      Comment comment = (Comment) it.next();
      if (!la.contains(comment) && comment.getStartLine() != laEndLine) {
        it.remove();
      }
    }

    return comments;
  }

  public static final JavadocComment findJavadocFor(LocationAware la) {
    return (JavadocComment) findFor(la, true);
  }

  public static final Comment findFor(LocationAware la) {
    return findFor(la, false);
  }

  public static final Comment findFor(LocationAware la, boolean onlyJavadoc) {
    final CompilationUnit sf = la.getCompilationUnit();

    if (sf == null) {
      return null;
    }

    int targetLine = la.getStartLine();
//System.err.println("Target line: " + targetLine + " - " + ast.getColumn());

    List comments = new ArrayList();
    List javadocs = sf.getJavadocComments();

    if (javadocs != null) {
      comments.addAll(javadocs);
    }

    if (!onlyJavadoc) {
      List simple = sf.getSimpleComments();
      if (simple != null) {
        comments.addAll(simple);
      }
    }

    Collections.sort(comments, LocationAware.PositionSorter.getInstance());

    int closest = -1;
    for (int i = 0, max = comments.size(); i < max; i++) {
      Comment comment = (Comment) comments.get(i);
//System.out.println("Comment seek line: " + comment.getLine());
      if (comment.getStartLine() < targetLine) {
        closest = i;
      } else if (comment.getStartLine() == targetLine &&
          comment.getEndColumn() <= la.getStartColumn()) {
        closest = i;
      } else {
        break;
      }
    }

    if (closest >= 0) {
      Comment comment = (Comment) comments.get(closest);

      LineIndexer indexer = sf.getLineIndexer();

      if ((indexer.getLineCount() < comment.getEndLine()) ||
          (indexer.getLineCount() < la.getStartLine())) {
        (sf.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(new UserFriendlyError(
                sf.getSource().getAbsolutePath() +
                " is probably out of synchronization. " +
                "Run Clean Rebuild and perform the refactoring again.", sf));
        new Exception("PLEASE REPORT TO support@refactorit.com").
            printStackTrace();
        return null;
      }

      int commentEnd = indexer.lineColToPos(
          comment.getEndLine(), comment.getEndColumn());
      int laStart = indexer.lineColToPos(
          la.getStartLine(), la.getStartColumn());

      if (commentEnd > laStart) {
// NOTE: switched off the warning - since needed it for half-rebuilded source after unskip of Audit
//				sf.getProject().addUserFriendlyError(new UserFriendlyError(
//            sf.getSource().getAbsolutePath() +
//						" is probably out of synchronization. " +
//						"Run Clean Rebuild and perform the refactoring again.", sf));
//				new Exception("PLEASE REPORT TO support@refactorit.com").printStackTrace();
        return comment;
      }

      String content = sf.getContent();
      content = content.substring(commentEnd, laStart).trim();

      if (content.length() == 0) {
        return comment;
      }
    }

    return null;
  }

  public static final Comment findAt(CompilationUnit source, int position) {
    LineIndexer indexer = source.getLineIndexer();
    SourceCoordinate coordinate = indexer.posToLineCol(position);
    return findAt(source, coordinate);
  }

  public static final Comment findAt(CompilationUnit source,
      SourceCoordinate coordinate) {
    List comments = new ArrayList();
    List javadocs = source.getJavadocComments();
    if (javadocs != null) {
      comments.addAll(javadocs);
    }
    List simple = source.getSimpleComments();
    if (simple != null) {
      comments.addAll(simple);
    }

    Collections.sort(comments, LocationAware.PositionSorter.getInstance());
    LocationAware checkLa = new LocationAwareImpl(
        source, coordinate.getLine(), coordinate.getColumn(),
        coordinate.getLine(), coordinate.getColumn());

    for (int i = 0; i < comments.size(); i++) {
      Comment comment = (Comment) comments.get(i);
      if (comment.contains(checkLa)) {
        return comment;
      }
    }

    return null;
  }

  public static List findAllFor(final LocationAware member) {
    List comments = new ArrayList(1);

    Comment comment = findFor(member);
    if (comment != null) {
      comments.addAll(findAllFor(comment));

      comments.add(comment);
    }

    return comments;
  }
  
  private class OwnerSearchVisitor extends BinItemVisitor {
    private BinItem owner = null;
    private Comment content = null;
    private int minDiff = Integer.MAX_VALUE;
    
    public OwnerSearchVisitor(Comment content) {
      this.content = content;
    }
    
    public void visit(BinCIType type) {
      check(type);
      super.visit(type);
    }
    
    public void visit(BinMethod meth) {
      check(meth);
      super.visit(meth);
    }
    
    private void check(LocationAware la) {
      int length = la.getStartLine() - content.startLine;
      if(length >= 0 && length < minDiff) {
        owner = (BinItem)la;
        minDiff = length;
      }
    }
    
    public BinItem getOwner() {
      return owner;
    }
  }
}
