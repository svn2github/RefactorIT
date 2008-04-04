/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.module.cleanimports.CleanImportsAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



public class UnusedImportRule extends AuditRule {
  public static final String NAME = "unused_import";

  public void visit(CompilationUnit sf) {
    super.visit(sf);
    if (sf.getMainType() != null) {
      ASTImpl[] unuseds = ImportUtils.listUnusedImports(sf);
      if (unuseds.length > 0) {
        for (int i = 0; i < unuseds.length; i++) {
          addViolation(new UnusedImport(sf, unuseds[i]));
        }
      }
    }
  }
}


/**
 * IDEA: it could/should actually create the (closed by default)
 * subnodes, that each click to one import
 *
 * @author  Sander Magi
 * @author  Igor Malinin
 */
class UnusedImport extends SimpleViolation {
  //final ASTImpl[] unusedNodes;

  /**
   * Creates a new instance of UnusedImport currently in
   * exploration mode and does not care about memory leaks through
   * binItemReferences
   */
  public UnusedImport(CompilationUnit compilationUnit, ASTImpl unusedNodes) {
    // IDEA: maybe this shows that it would be better to actually sort the
    // violations by CompilationUnits?
    super(compilationUnit.getMainType(), unusedNodes,
        "Unnecessary import statements", "refact.audit.unused_imports");
    setTargetItem(compilationUnit.getMainType());
    //this.unusedNodes = unusedNodes;
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(CleanImports.instance);
  }

}


class CleanImports extends MultiTargetGroupingAction {
  static final CleanImports instance = new CleanImports();

  public String getKey() {
    return "refactorit.audit.action.import.clean";
  }

  public String getName() {
    return "Clean Imports";
  }

  public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
    Set compilationUnits = new HashSet(violations.size());
    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation violation = (RuleViolation) i.next();

      if (violation instanceof UnusedImport) {
        compilationUnits.add(violation.getCompilationUnit());
      }
    }

    if (compilationUnits.isEmpty()) {
      return Collections.EMPTY_SET;
    }

    ArrayList types = new ArrayList();

    for (Iterator i = compilationUnits.iterator(); i.hasNext(); ) {
      CompilationUnit compilationUnit = (CompilationUnit) i.next();
      BinCIType type = compilationUnit.getMainType().getBinCIType();
      types.add(type);
    }
    Object[] target = types.toArray(new BinCIType[types.size()]);

    RefactorItAction clean = ModuleManager.getAction(target[0].getClass(),
        CleanImportsAction.KEY);
    clean.run(context, target);

    return compilationUnits;
  }
}
