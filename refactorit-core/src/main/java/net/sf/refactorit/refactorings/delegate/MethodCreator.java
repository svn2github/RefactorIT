/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Tonis Vaga
 */
public class MethodCreator {
  TransformationList transList;
  ImportManager importManager;

  /**
   * @param context
   * @param editor
   * @param importManager
   *          import manager. NB! Method body imports not considered(so far)
   */
  private Collection skeletons;

  public MethodCreator(MethodSkeleton context,
      final TransformationList transList, ImportManager importManager) {
    this(context.getOwner(), Collections.singleton(context), transList,
        importManager);
  }

  public MethodCreator(BinTypeRef owner, Collection methodSkeletons,
      final TransformationList transList, ImportManager importManager) {
    this.skeletons = methodSkeletons;
    this.transList = transList;
    this.importManager = importManager;

    for (Iterator iter = skeletons.iterator(); iter.hasNext(); ) {
      MethodSkeleton item = (MethodSkeleton) iter.next();

      item.setOwner(owner);
    }
  }

  public void createEdit() {
    for (Iterator iter = skeletons.iterator(); iter.hasNext(); ) {
      MethodSkeleton skel = (MethodSkeleton) iter.next();

      BinCIType owner = skel.getOwner().getBinCIType();

      SourceCoordinate sc;
      if (!skel.isConstructor()) {
        sc = owner.findNewMethodPosition();
      } else {
        sc = owner.findNewConstructorPosition();
      }

      transList.add(new StringInserter(owner.getCompilationUnit(), sc
          .getLine(), sc.getColumn(), getMethodAsString(skel)));
    }
  }

  public String getMethodAsString(MethodSkeleton methodContext) {
    BinMethod method = methodContext.getMethod();

    Set fqnTypes = importManager.manageImports(method);

    BinMethodFormatter formatter = (BinMethodFormatter)method.getFormatter();
    formatter.setFqnTypes(fqnTypes);

    StringBuffer result = new StringBuffer(FormatSettings.LINEBREAK
        + formatter.formHeader());

    // TODO migrate to formatting engine also?
    int baseIndent = new BinTypeFormatter(methodContext.getOwner()
        .getBinCIType()).getMemberIndent();

    int blockIndent = baseIndent + FormatSettings.getBlockIndent();

// redundant emty line
//    result.append(FormatSettings.getIndentString(blockIndent));
//    result.append(FormatSettings.LINEBREAK);

    result.append(getMethodBodyAsString(methodContext, blockIndent));
    result.append(FormatSettings.LINEBREAK);
    //    result.append(FormatSettings.getIndentString(baseIndent));

    result.append(formatter.formFooter());

    // TODO: should have an extra newline after the method/constructor if there
    // are other methods around
    // One possible way to fix this would be to try to insert the method one
    // line above (like in ExtractMethod?)

    return result.toString();
  }

  private String getMethodBodyAsString(MethodSkeleton methodContext,
      int blockIdent) {
    return methodContext.getBody().getBodyAsString(blockIdent);
  }
}
