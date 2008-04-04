/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.FileEraser;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.List;


/**
 * FIXME: very specific, find other usage, pls %^)
 */
public final class RenamePackageTransformation extends AbstractTransformation {

  private Source oldDir;
  private final Project project;
  private SourceHolder newDirObj;

  private List packages;
  private String oldPrefix;
  private String newPrefix;

  public static final int RELOCATE_FILES_TRANSACTION = 1;
  public static final int RELOCATE_SIMPLE = 2;

  private int type = 0;

  public RenamePackageTransformation(Source oldDir, Project project,
      SourceHolder newDir, int type) {
    super(new SimpleSourceHolder(project));
    setType(type);
    this.oldDir = oldDir;
    this.project = project;
    this.newDirObj = newDir;
  }

  public RenamePackageTransformation(List packages, String oldPrefix, String newPrefix,
      Project project, int type) {
    super(new SimpleSourceHolder(project));
    setType(type);
    this.project = project;
    this.packages = packages;
    this.oldPrefix = oldPrefix;
    this.newPrefix = newPrefix;
  }

  private void setType(int type) {
    switch(type) {
      case RELOCATE_FILES_TRANSACTION:
      case RELOCATE_SIMPLE:
        this.type = type;
      	break;
      default:
        if(Assert.enabled) {
          Assert.must(false, "Illegal flag usage in Relocate Transformation");
        }
    }
  }


  public RefactoringStatus apply(EditorManager manager) {
    RefactoringStatus status = new RefactoringStatus();
    SourceRelocator sourceRelocator = new SourceRelocator(project, manager);

    switch(type) {
      case RELOCATE_FILES_TRANSACTION:
        sourceRelocator.relocateFilesTransaction(oldDir.getChildren(), newDirObj);
        manager.addEditor(new FileEraser(
            new SimpleSourceHolder(oldDir, project)));
        break;

      case RELOCATE_SIMPLE:
        List errors = new ArrayList();
        sourceRelocator.relocate(packages, oldPrefix, newPrefix, errors);
        status.merge(createRelocationWarningsFor(errors));
        break;
    }

//    for(int x=0; x < sourceRelocator.getTransList().getTransformationList().size(); x++){
//
//      manager.addEditor((Editor)sourceRelocator.getTransList().getTransformationList().get(x));
//
//    }
    return status;
  }


  private RefactoringStatus createRelocationWarningsFor(List sources) {
    RefactoringStatus result = new RefactoringStatus();
    for (int i = 0; i < sources.size(); i++) {
      Source source = (Source) sources.get(i);
      result.addEntry(FileRenamer.FILE_NOT_RELOCATED_MSG
          + source.getRelativePath(), // FIXME fix getDisplayPath and use here
          RefactoringStatus.WARNING);

    }
    return result;
  }

}
