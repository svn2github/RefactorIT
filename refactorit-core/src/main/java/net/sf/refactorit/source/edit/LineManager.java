/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.source.preview.PreviewModelSelectionProcessor;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public final class LineManager {
  /** key - SourceHolder, value - list of Line */
  private final Map sources = new HashMap();

  /** key - Source, value - linebreak String */
  private final HashMap linebreaks = new HashMap();

  private final ArrayList filesystemEditors = new ArrayList();

  private final HashMap sourcesThroughRebuildMapping = new HashMap();

  /** list of SourceHolders that should not be saved */
  private final List shouldNotSave = new ArrayList();

  public LineManager() {
  }

  /** For tests
   * @param source temporary object
   * @param line single line
   */
  LineManager(SourceHolder source, String line) {
    this(source, CollectionUtil.singletonArrayList(line));
  }

  /** For tests
   * @param source temporary object
   * @param lines list of String
   */
  LineManager(SourceHolder source, List lines) {
    List lineObjects = new ArrayList(lines.size());
    for (int i = 0; i < lines.size(); i++) {
      lineObjects.add(new Line((String) lines.get(i)));
    }
    sources.put(source, lineObjects);
  }

  public void addFilesystemEditor(final FilesystemEditor editor) {
    if (editor instanceof FileRenamer){
      FileRenamer renamer = (FileRenamer) editor;
      SourceHolder targetSource = renamer.getTarget();
      renamer.getOldName();
      renamer.getNewName();
    }

    this.filesystemEditors.add(editor);
  }

  /**
   * @param source source file
   * @param lineNumber starts with 1; to append to the end of file call with -1
   * @return line
   * @throws IOException
   */
  public final Line getLine(final SourceHolder source, final int lineNumber) throws IOException {
    if (Assert.enabled && source == null) {
      new Exception("source is null").printStackTrace();
      Assert.must(false, "Source is null!");
    }


    List lines = getLines(source);

    // FIXME lines should start from 0, but this will affect all editors and refactorings
    int index = lineNumber - 1;

    final int size = lines.size();
    if (index < 0) { // to append to the end
      index = size;
    }

    // asked for line beyond the end of original file, usually happens when file
    // is just created, so let's add empty lines
    if (index >= size) {
      for (int i = 0, max = index - size + 1; i < max; i++) {
        lines.add(new Line(new StringBuffer(32)));
      }
    }

    return (Line) lines.get(index);
  }

  public final void setLinebreak(SourceHolder source, String linebreak) {
    if (linebreak != null && linebreak.length() > 0) {
      if (!linebreaks.containsKey(source)) {
        linebreaks.put(source, linebreak);
      }
    } else {
      if (Assert.enabled) {
        System.err.println("Setting empty linebreak for: " + source);
      }
    }
  }

  private final String getLinebreak(final SourceHolder source) {
    String linebreak = (String) linebreaks.get(source);

    if (linebreak != null && linebreak.length() == 0) {
      if (Assert.enabled) {
        System.err.println("Detected wrong linebreak for: " + source);
      }
      linebreak = null;
    }

    if (linebreak == null || linebreak.length() == 0) {
      if (Assert.enabled) {
        System.err.println("Linebreak wasn't explicitly set for: " + source);
      }

      Iterator sourceIt = this.sources.keySet().iterator();
      while (sourceIt.hasNext() && linebreak == null) {
        linebreak = (String) linebreaks.get(sourceIt.next());
      }

      if (linebreak == null || linebreak.length() == 0) {
        linebreak = FormatSettings.LINEBREAK;
        if (Assert.enabled) {
          System.err.println("Failed to predict linebreak for: " + source);
        }
      }
    }

    return linebreak;
  }

  public final int getLineNumber(final SourceHolder source) throws IOException {
    return getLines(source).size();
  }

  public final void loadSource(final SourceHolder source, boolean noSave) throws IOException {
    if(noSave) {
      this.shouldNotSave.add(source);
    }
    getLines(source);
  }

  private final List getLines(final SourceHolder source) throws IOException {
    List lines = (List) sources.get(source);
    if (lines != null) {
      return lines;
    }

    lines = new ArrayList(256);
    LineReader reader = null;

    if (source.getSource() != null) { // not just created, so load
      try {
        try {
          reader = new LineReader(
              new InputStreamReader(source.getSource().getInputStream(),
              GlobalOptions.getEncoding()), 4096);
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException("Unsupported encoding: " + e.getMessage());
        }

        String buffer = null;

        while ((buffer = reader.readLine()) != null) {
          lines.add(new Line(buffer));
        }

        // detect linebreak for the file by the first line
        if (lines.size() > 0) {
          setLinebreak(source,
              StringUtil.getLinebreak(((Line) lines.get(0)).getContent()));
        }

      } catch (IOException e) {
        if (Assert.enabled) {
          e.printStackTrace(System.err);
        }
        throw e;
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            e.printStackTrace(System.err);

            throw e;
          }
        }
      }
    }

    sources.put(source, lines);
    return lines;
  }

  public void dumpAllSources(final PrintStream stream) {
    Iterator sourceIt = this.sources.keySet().iterator();
    while (sourceIt.hasNext()) {
      final SourceHolder source = (SourceHolder) sourceIt.next();
      dumpSource(stream, source);
    }
  }

  public void dumpSource(final PrintStream stream, final SourceHolder source) {
    stream.println("Source: " + source.getSource().getRelativePath());

    List lines = (List)this.sources.get(source);
    for (int i = 0, max = lines.size(); i < max; i++) {
      final Line line = ((Line) lines.get(i));
      if (line.getContent().length() > 0) {
        stream.println(line.toString());
      } else {
        stream.println("<empty line>");
      }
    }
  }

  //public final void writeSources() throws IOException {
  public final RefactoringStatus writeSources() {
    RefactoringStatus status = new RefactoringStatus();



    IUndoableTransaction transaction = null;
    boolean createUndo = false;
    transaction = RitUndoManager.getCurrentTransaction();
    if (transaction != null) {
      createUndo = true;
    }
    // invariant: createUndo == true => transaction!= null
    if (createUndo) {
      Set sourceKeys = sources.keySet();
      IUndoableEdit undo = transaction.createModifiedSourcesUndo(
          extractSources(sourceKeys)); // FIXME: remove extracting after migration of undo to SourceHolders/Editors
      transaction.addEdit(undo);
    }

    for (int i = 0; i < filesystemEditors.size(); i++) {
      FilesystemEditor editor = (FilesystemEditor) filesystemEditors.get(i);
      if (editor.isEnabled()) {
        status.merge(editor.changeInFilesystem(this));
      }
      if (status.isCancel() || status.isErrorOrFatal()) {
        return status;
      }
    }

//    if (status.isCancel() || status.isErrorOrFatal()) {
//      return status;
//    }



    final Iterator sourcesIterator = sources.keySet().iterator();
    while (sourcesIterator.hasNext()) {
      final SourceHolder source = (SourceHolder) sourcesIterator.next();

      if(shouldNotSave.contains(source)) {
        source.getProject().getProjectLoader()
        .forceSourceModified(source.getSource());
        continue;
      }

      if (source.getSource() == null) {
        status.addEntry("One of the sources is null, can not continue",
            RefactoringStatus.ERROR);
        break;
      }

      int len = (int) source.getSource().length();
      if (len < 1024) { // this is e.g. for the case the file is just created
        len = 4096;
      }

      Writer writer = null;
      long lastModifiedBeforeWrite = source.getSource().lastModified();

      try {
        writer = new BufferedWriter(
            new OutputStreamWriter(source.getSource().getOutputStream(),
            GlobalOptions.getEncoding()), len);

        final String linebreak = getLinebreak(source);

        mapSourceHolderThroughRebuild(source);

        final List lines = (List) sources.get(source);
        for (int i = 0, max = lines.size(); i < max; i++) {
          final Line line = (Line) lines.get(i);
          String content = line.getContent();
          if (content.length() > 0) {
            content = StringUtil.replace(content, FormatSettings.LINEBREAK,
                linebreak);
            writer.write(content);
          }
        }
      } catch (IOException e) {
        if (Assert.enabled) {
          e.printStackTrace(System.err);
        }
        status.addEntry(e, RefactoringStatus.FATAL);
        // FIXME: continue with other sources? may be better rollback all done?
      } finally {
        if (writer != null) {
          try {
            writer.flush();
            writer.close();

            // we opened the file, did something and closed successfully,
            // so at least something must have been changed
            source.getProject().getProjectLoader()
                .forceSourceModified(source.getSource());

            // if VFS failed to update lastModified itself, we should help it
            //System.err.println("compare LM_: " + getSource().lastModified() + " = " + lastModifiedBeforeWrite);
            if (source.getSource().lastModified() == lastModifiedBeforeWrite ||
                lastModifiedBeforeWrite == 0) {
              // Forcely notify our VFS that the source has been altered
              source.getSource().setLastModified(System.currentTimeMillis());
            }
          } catch (IOException e) {
            e.printStackTrace(System.err);
            status.addEntry(e, RefactoringStatus.FATAL);
          }
        }
      }
    }

    return status;
  }

  private static ArrayList extractSources(final Set sources) {
    ArrayList result = new ArrayList(sources.size());
    for (Iterator it = sources.iterator(); it.hasNext(); ) {
      Object obj = it.next();

      if (obj == null) {
        continue;
      }

      if (obj instanceof SourceHolder) {
        if (((SourceHolder) obj).getSource() != null) {
          CollectionUtil.addNew(result, ((SourceHolder) obj).getSource());
        }
      } else if (Assert.enabled) {
        Assert.must(false, "Got strange source: " + obj);
      }
    }

    return result;
  }

  public final RefactoringStatus canWrite() {
    Iterator sources = this.sources.keySet().iterator();
    RefactoringStatus status = new RefactoringStatus();
    while (sources.hasNext()) {
      final SourceHolder source = (SourceHolder) sources.next();

      // Check if the source is writable
      // Null - source is not yet created by special editor
      if (source.getSource() != null && !source.getSource().canWrite()) {
        if (!source.getSource().startEdit()) {
          Exception e = new IOException("Can not modify source - "
              + source.getSource().getAbsolutePath() + "!");
          status.addEntry(e, RefactoringStatus.FATAL);
        }
      }
    }

    return status;
  }

  public final void clear() {
    sources.clear();
    linebreaks.clear();
    filesystemEditors.clear();
    sourcesThroughRebuildMapping.clear();
  }

  public final void clearFilesystemEditors(){
    filesystemEditors.clear();
  }

  public char charAt(SourceHolder source, int position) throws IOException {
    SourceCoordinate coordinate
        = source.getSource().getLineIndexer().posToLineCol(position);
    Line line = getLine(source, coordinate.getLine());
    return line.charAt(coordinate.getColumn() - 1);
  }

  public ChangesPreviewModel getPreviewModel() {
    return new ChangesPreviewModel("Changes", sources, this.filesystemEditors);
  }

  public void checkUserInput(ChangesPreviewModel model) {
    new PreviewModelSelectionProcessor(model, sources).process();
  }

  private void mapSourceHolderThroughRebuild(SourceHolder source) {
    sourcesThroughRebuildMapping.put(source, source.getSource()
        .getRelativePath());
  }

  public void remapSources(RefactorItContext context){
    Project project = context.getProject();
    Iterator it = sourcesThroughRebuildMapping.entrySet().iterator();
    while (it.hasNext()){
      Map.Entry entry = (Map.Entry) it.next();
      SourceHolder key = (SourceHolder) entry.getKey();
      String value = (String) entry.getValue();


      Object sourceLines = sources.get(key);
      if (sourceLines != null){
        sources.remove(key);
        SourceHolder newHolder = project.getCompilationUnitForName(value);
        if (newHolder != null){
          sources.put(newHolder, sourceLines);
        }
      }
    }
  }
}
