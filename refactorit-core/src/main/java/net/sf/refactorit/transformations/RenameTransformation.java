/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.RenameEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;

import java.util.List;


/**
 * Does a rename of a chosen item.
 * @author Jevgeni Holodkov
 */
public final class RenameTransformation extends AbstractTransformation {

  private String newName;
  private final String oldName = null;

  private final int RENAME_CLASS_MODEL = 0;
  private final int RENAME_NON_JAVA_FILES = 1;
  private int trasformationType = RENAME_CLASS_MODEL;
  // for non java files
  private String oldQualifiedName;
  private String newQualifiedName;
  private Occurrence occurrence;

  // for java files
  private List nodes;
  private boolean trimTrailingSpaces = false;
  private final RefactoringStatus status = new RefactoringStatus();

  public RenameTransformation(SourceHolder input, ASTImpl node, String newName) {
    this(input, CollectionUtil.singletonArrayList(node), newName, false);
  }

  public RenameTransformation(SourceHolder input, BinCIType item, String newName) {
    this(input, item.getNameAstOrNull(), newName);
  }

  public RenameTransformation(SourceHolder input, List nodes, String newName) {
    this(input, nodes, newName, false);
  }

  public RenameTransformation(SourceHolder input, List nodes, String newName,
      boolean trimTrailingSpaces) {
    super(input);
    this.newName = newName;
    this.nodes = nodes;
    this.trimTrailingSpaces = trimTrailingSpaces;
  }

  public RenameTransformation(SourceHolder input, String oldQualifiedName,
      String newQualifiedName, Occurrence occurrence) {
    super(input);
    this.oldQualifiedName = oldQualifiedName;
    this.newQualifiedName = newQualifiedName;
    this.occurrence = occurrence;
    trasformationType = RENAME_NON_JAVA_FILES;
  }

  public RefactoringStatus apply(EditorManager editor) {
    RefactoringStatus status = new RefactoringStatus();
    switch (trasformationType) {
      case RENAME_CLASS_MODEL:
        editor.addEditor(new RenameEditor(getSource(),
            nodes, newName,
            trimTrailingSpaces));
        break;

      case RENAME_NON_JAVA_FILES:
        editor.addEditor(new StringEraser(getSource(),
            occurrence.getLine().getLineNumber(), occurrence.getStartPos(),
            oldQualifiedName.length()));
        editor.addEditor(new StringInserter(getSource(),
            occurrence.getLine().getLineNumber(), occurrence.getStartPos(),
            newQualifiedName));
        break;

      default:
        throw new RuntimeException(
            "Unknown transformation type usage in Rename Transformation!");
    }
    return status;
  }
}
