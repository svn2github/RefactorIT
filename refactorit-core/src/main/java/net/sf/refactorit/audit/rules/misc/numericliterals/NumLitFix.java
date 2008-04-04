/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.Line;
import net.sf.refactorit.source.edit.LineManager;
import net.sf.refactorit.source.preview.Diff;
import net.sf.refactorit.transformations.TransformationList;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Arseni Grigorjev
 */
public abstract class NumLitFix {
  protected NumericLiteral violation;
  protected TransformationList editors = new TransformationList();
  private LineManager previewManager = null;

  protected NumLitFix(NumericLiteral violation){
    this.violation = violation;
  }

  public String getLinePreview(){
    if (previewManager == null){
      previewManager = new LineManager();
      createPreviewEditors();

      for(Iterator it = editors.iterator(); it.hasNext(); ){
        Editor editor = (Editor) it.next();
        editor.apply(previewManager);
      }
    }

    SourceConstruct construct = violation.getSourceConstruct();
    Line previewLine;
    try {
      previewLine = previewManager.getLine(
          construct.getCompilationUnit(), construct.getStartLine());
    } catch (IOException e){
      return "[error, please report]";
    }
    Diff diff = new Diff(previewLine.getOriginalContent(), previewLine.getContent());
    diff.setTagBeforeNew("<font color=\"#C02040\">");
    diff.setTagAfterNew("</font>");
    diff.runDiff();
    return diff.getMarkedIsNow();
  }

  public abstract BinField getField();

  protected TransformationList getTransformationList(){
    createOtherEditors();
    return editors;
  }

  protected abstract void createPreviewEditors();

  protected abstract void createOtherEditors();
}
