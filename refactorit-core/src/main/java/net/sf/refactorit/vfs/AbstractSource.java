/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.utils.LinePositionUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;



public abstract class AbstractSource implements Source, Comparable {

  private LineIndexer lineIndexer = null;
  private ASTTree astTree;

  private Reference cachedContent = null;

  private static final class CachedContent {
    public CachedContent(final String content, final long lastModified) {
      this.content = content;
      this.lastModified = lastModified;
    }

    public final String content;
    public final long lastModified;
  }

  /**
   * @see net.sf.refactorit.vfs.Source#getRelativePath()
   */
  public String getRelativePath() {
    Source parent = getParent();
    if (parent == null) {
      return "";
    }

    String path = parent.getRelativePath();
    if (path.length() == 0) {
      return getName();
    }

    return path + RIT_SEPARATOR + getName();
  }

  public final String getContentString() {
    if (this.cachedContent != null) {
      final CachedContent cont = (CachedContent) this.cachedContent.get();
      if (cont != null && cont.lastModified == this.lastModified()) {
        return cont.content;
      }
      this.cachedContent = null;
    }

    try {
      final byte[] contentBytes = getContent();
      final String content = new String(contentBytes, GlobalOptions.getEncoding());

      this.cachedContent
          = new WeakReference(new CachedContent(content, lastModified()));

      return content;
    } catch (UnsupportedEncodingException e) {
      AppRegistry.getExceptionLogger().error(e,
          "Failed to read input from " + getRelativePath() + ": " + e, this);
      throw new RuntimeException("Unsupported encoding: " + e.getMessage());
    } catch (IOException exception) {
      AppRegistry.getExceptionLogger().error(exception,
          "Failed to read input from " + getRelativePath() + ": " + exception, this);
    }

    return new String();
  }

  public final String getContentOfLine(int line) {
    return LinePositionUtil.extractLine(line, getContentString());
  }

  public void invalidateCaches() {
    this.lineIndexer = null;
    if (this.cachedContent != null) {
      this.cachedContent.clear();
      this.cachedContent = null;
    }
  }

  public final String getText(final ASTImpl node) {
    return getText(node.getStartLine(), node.getColumn(),
        node.getEndLine(), node.getEndColumn());
  }

  public final String getText(final int startLine, final int startColumn,
      final int endLine, final int endColumn) {
    return getText(getPosition(startLine, startColumn),
        getPosition(endLine, endColumn));
  }

  public final String getText(final int startPosition, final int endPosition) {
    return getContentString().substring(startPosition, endPosition);
  }

  public final int getPosition(final int line, final int column) {
    return getLineIndexer().lineColToPos(line, column);
  }

  public final LineIndexer getLineIndexer() {
    if (this.lineIndexer == null
        || lastModified() != this.lineIndexer.lastModified()) {
      this.lineIndexer = makeLineIndexer();
    }

    return lineIndexer;
  }

  private LineIndexer makeLineIndexer() {
    return new LineIndexer(getContentString(), lastModified());
  }

  // FIXME: why can't be used getLineIndexer().getLineCount()??? that one is cached
  // test and remove!
  public final int getLineCount() {
    return StringUtil.lineBreakCount(getContentString()) + 1;
  }

  /**
   * Currently only legal place to use this is in move type, where
   * AST nodes need to be moved from one file to another.
   */
  public final ASTTree getASTTree() {
    return astTree;
  }

  public final void setASTTree(final ASTTree astTree) {
    this.astTree = astTree;
  }

  public final ASTImpl getFirstNode() {
    if (astTree == null) {
      return null;
    } else {
      return astTree.getAstAt(0);
    }
  }

  public final ASTImpl getASTByIndex(final int index) {
    if (astTree == null || index == -1) {
      return null; // -1 is 'secret code' for null
    }
    return astTree.getAstAt(index);
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    return getIdentifier().equals(((Source) o).getIdentifier());
  }

  public final int hashCode() {
    return getIdentifier().hashCode();
  }

  public String toString() {
    return ClassUtil.getShortClassName(this) + ": "
        + getIdentifier() + " " + Integer.toHexString(hashCode())
        + "(" + Integer.toHexString(System.identityHashCode(this)) + ")";
  }

  public Source mkdirs(String path, boolean addIntoVcs) {
    return mkdirs(path);
  }

  protected final Source getChildIgnoreCase(String name) {
    Source[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i].getName().equalsIgnoreCase(name)) {
        return children[i];
      }
    }

    return null;
  }

  /**
   * @see net.sf.refactorit.vfs.Source#getDisplayPath()
   */
  public String getDisplayPath() {
    return getAbsolutePath();
  }

  /**
   * @see net.sf.refactorit.vfs.Source#getChildren(net.sf.refactorit.vfs.Source.SourceFilter)
   */
  public Source[] getChildren(SourceFilter sourceFilter) {
    Source children[] = getChildren();

    if (children == null) {
      return null;
    }

    List result = new ArrayList();

    for (int i = 0; i < children.length; i++) {
      if (sourceFilter.accept(children[i])) {
        result.add(children[i]);
      }
    }

    return (Source[]) result.toArray(new Source[result.size()]);
  }


  public final List getAllFiles() {
    ArrayList result = new ArrayList(2);
    if (this.isFile()) {
      result.add(this);
    } else {
      Source[] children = this.getChildren();
      for (int i = 0; i < children.length; i++) {
        result.addAll(children[i].getAllFiles());
      }
    }

    return result;
  }

  public final List getFiles() {
    ArrayList result = new ArrayList();

    if (this.isFile()) {
      result.add(this);
    } else {
      Source[] children = this.getChildren();
      for (int i = 0; i < children.length; i++) {
        if (children[i].isFile()) {
          result.add(children[i]);
        }
      }
    }

    return result;
  }

  /**
   * deletes subdirectories and files recursively
   * NB! Doesn't delete current directory
   * @param filter or null if all
   * @return true if successful
   */
  public final boolean emptyDirRecursively(SourceFilter filter) {

    if ( !isDirectory()) {
      return false;
    }
    Source children[]=getChildren();

    if ( children == null ) {
      return true;
    }
    boolean result=true;


    for (int i = 0; i < children.length; i++) {

      if ( children[i].isDirectory()) {
        if ( filter != null && !filter.accept(children[i])) {
          continue;
        }
        // empty the dir
        result= ((AbstractSource)children[i]).emptyDirRecursively(filter);
        if ( result ) {
          // delete dir

          IUndoableTransaction transaction = RitUndoManager.
              getCurrentTransaction();
          IUndoableEdit undo = null;
          if (transaction != null) {
            undo = transaction.createDeleteFileUndo(children[i]);
          }

          result = children[i].delete();

          if(transaction != null && undo != null && result) {
            transaction.addEdit(undo);
          }

        }
      } else {

        IUndoableTransaction transaction = RitUndoManager.
            getCurrentTransaction();
        IUndoableEdit undo = null;
        if (transaction != null) {
          undo = transaction.createDeleteFileUndo(children[i]);
        }

        result=children[i].delete();

        if(transaction != null && undo != null && result) {
          transaction.addEdit(undo);
        }

      }
      if ( !result ) {
        return false;
      }
    }

    return true;
  }

  /**
   * Try to create new Source directory/directories.
   * Path components are delimited by slash '/'.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param path  path to created directory
   */
  public Source mkdirs(String path) {
    Source src = this;

    path = normalize(path);
    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();


      // first check if exist then create when needed
      Source currentNode=src.getChild(name);

      if ( currentNode == null ) {
         src = src.mkdir(name);
      } else {
        src=currentNode;
      }
      if (src == null) {
        break;
      }
    }

    return src;
  }

  public static boolean inVersionControlDirList(String childName) {
    StringTokenizer tokens = new StringTokenizer(
        GlobalOptions.getOption("version.control.dir.list", ""), ";");
    while (tokens.hasMoreTokens()) {
      if (tokens.nextToken().equals(childName)) {
        return true;
      }
    }

    return false;
  }

  /** Converts alien separators to RIT internal ones */
  public final static String normalize(final String path) {
    return path.replace(ALIEN_SEPARATOR_CHAR, RIT_SEPARATOR_CHAR);
  }

  public final static String denormalize(final String path) {
    return path.replace(RIT_SEPARATOR_CHAR, File.separatorChar);
  }

  public int compareTo(Object o) {
    Source s2 = (Source) o;
    if ((this.isDirectory() && (!s2.isDirectory()))) {
      return -1;
    }
    if ((s2.isDirectory() && (!this.isDirectory()))) {
      return 1;
    }
    return this.getName().compareTo(s2.getName());
  }

  public boolean isIgnored(Project project) {
    List ignoredSources = project.getPaths().getSourcePath().getIgnoredSources();

    String checkedPath =
          BinPackage.convertPathToPackageName(this.getAbsolutePath()) + ".";

    for (Iterator iter = ignoredSources.iterator(); iter.hasNext(); ) {
      String ignoredPath = (String) iter.next();
      ignoredPath = BinPackage.convertPathToPackageName(ignoredPath) + ".";

      if((checkedPath).startsWith(ignoredPath)) {
        return true;
      }
    }

    return false;
  }
}
