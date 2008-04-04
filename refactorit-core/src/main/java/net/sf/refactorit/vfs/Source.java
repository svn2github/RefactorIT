/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.query.LineIndexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Interface for any node in virtual source file system.
 * Is analogous to <code>File</code> in <code>java.io</code>
 *
 *
 * @author Igor Malinin
 * @author Anton Safonov
 * @author Juri Reinsalu
 */
public interface Source {

  Source[] NO_SOURCES = new Source[0];

  /**
   * Default file buffer size
   */
  int DEFAULT_BUFFER_SIZE = 1024;

  /**
   *  lastModifed timestamp maximal error
   */
  long MAX_TIMESTAMP_ERROR = 1000;

  char ALIEN_SEPARATOR_CHAR = '\\';
  char RIT_SEPARATOR_CHAR = '/';
  String RIT_SEPARATOR = "/";

  String LINK_SYMBOL = " -> ";

  /**
   * Parent directory.
   * Note: if current source is sourcepath root, then getParent() == null
   * @return  parent Source directory
   */
  Source getParent();

  /**
   * this should return something that identifies the source and is fast to
   * operate with when getting hashcode or testing equals
   *  Must be serializable in order to serialize source map.

   */
  Object getIdentifier();

  /**
   * Child Source.
   * Returns null if does not exists.
   *
   * @param path  relative name of source
   * @return  child Source
   */
  Source getChild(String path);

  /**
   * Name of the file/directory.
   *
   * @return  name of the file/directory
   */
  String getName();

  /**
   * Low-level OS path for this source.
   *
   * @return absolute path in sense of OS
   */
  String getAbsolutePath();

  /**
   * In some environments (e.g. NB) file system names look bad in dialogs.
   * This one is almost the same as {@link #getAbsolutePath()}, but looks
   * better.
   * @return displayable absolute path
   */
  String getDisplayPath();

  /**
   * Path from the root of filesystem this file belongs to.<br>
   * Path components are delimited by slash '/'.<br>
   * Example: "com/package/CompilationUnit.java"
   * <p>
   *  Note that relative path is path from root, which can be different from sourcepath root!
   *  But following condition must hold:<br>
   * <pre>
   *   Source parent=src;
   *   while( parent.getParent() != null ) {
   *     parent=parent.getParent();
   *   }
   *   assert parent.getChild(src.getRelativePath()) == src;
   *
   * </pre>
   *
   * @return path relative to filesystem source root; "" for root
   */
  String getRelativePath();

  /**
   * Tests if this file is writable.
   *
   * @return  true if this file is writable
   */
  boolean canWrite();

  /**
   * Makes file editable if possible. Usable with files from VisualSourceSafe
   * and when files are checked out as read-only.
   *
   * @return true if succeeded to make file writable
   */
  boolean startEdit();

  /**
   * Time of last modification
   *
   * @return  last modified time
   */
  long lastModified();

  /**
   * Set time of last modification
   *
   * @param time  last modified time
   */
  boolean setLastModified(long time);

  /**
   * Only for files!!!
   *
   * @return  lenght of the file
   */
  long length();

  /**
   * Tests if this is the file
   *
   * @return  true if this Source object represents a file
   */
  boolean isFile();

  /**
   * Tests if this is the directory
   *
   * @return  true if this Source object represents a directory
   */
  boolean isDirectory();

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to read contents of the file
   */
  InputStream getInputStream() throws IOException;

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to write contents of the file
   */
  OutputStream getOutputStream() throws IOException;

  /**
   * Try to delete this file/directory.
   * @return true if deleted successfully
   */
  boolean delete();

  /**
   * Array of children (subdirectories and files). Is analogous to
   * <code>listFile</code> method in <code>java.io.File</code> as
   * <code>Source</code> in <code>net.sf.refactorit.vfs</code> is
   * ideologicaly the same as <code>File</code> in <code>java.io</code>
   *
   * To get the children complient to custom view of the vfs (ie get only those
   * allowed by some rule) use <code>@see #getChildren(Source.SourceFilter)</code>
   * Only for directories!!!
   *
   * NB! Should never return null!
   *
   * @return Source childrens (directories and files)
   */
  Source[] getChildren();

  List getAllFiles();

  /**
   * Array of children (subdirectories and files). Is analogous to
   * <code>listFile</code> method in <code>java.io.File</code> as
   * <code>Source</code> in <code>net.sf.refactorit.vfs</code> is
   * ideologicaly the same as <code>File</code> in <code>java.io</code> To
   * get all the direct children under this <code>Source</code> use
   * <code>@see #getChildren()</code>
   *
   * @return Source children (directories and/or files) that are accepted by
   *         the provided filter
   */
  Source[] getChildren(Source.SourceFilter sourceFilter);

  /**
   * Try to create new Source directory.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param name  name of file to create
   */
  Source mkdir(String name);

  /**
   * Try to create new Source directory/directories.
   * Path components are delimited by slash '/'.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param path  path to created directory
   */
  Source mkdirs(String path);

  Source mkdirs(String path, boolean addIntoVcs);

  /**
   * Try to create new Source file.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created file Source if succeed; null otherwise
   * @param name  name of file to create
   */
  Source createNewFile(String name) throws IOException;

  /**
   * Try to rename Source.
   * Returns null if can't rename.
   * NB! For implementation remember to call setAstTree after creating new source!!!
   * @return  renamed file/directory Source if succeed; null otherwise
   *
   * @param dir  target directory
   * @param name  new name
   */
  Source renameTo(Source dir, String name);

  /**
   * Is used in <code>@see getChildren(Source.SourceFilter)</code>
   */
  public static interface SourceFilter {
    /*
     * @return true if source was not outfiltered
     */
    public boolean accept(Source source);
  }


  LineIndexer getLineIndexer();

  int getLineCount();

  public File getFileOrNull();

  void invalidateCaches();

  byte[] getContent() throws IOException;

  String getContentString();

  String getContentOfLine(int line);

  String getText(int startLine, int startColumn, int endLine, int endColumn);

  String getText(ASTImpl node);

  String getText(int startPosition, int endPosition);

  int getPosition(int line, int column);

  ASTTree getASTTree();

  void setASTTree(ASTTree astTree);

  ASTImpl getFirstNode();

  ASTImpl getASTByIndex(int index);

  void renameFormFileIfExists(String oldName, String newName);

  void moveFormFileIfExists(Source destination);

  boolean shouldSupportVcsInFilesystem();

  boolean inVcs();

  boolean isIgnored(Project project);

  /**
   * @return separator character which {@link Source} instance uses
   */
  char getSeparatorChar();

  /**
   * @return true if exists
   */
  boolean exists();
  List getFiles();

}
