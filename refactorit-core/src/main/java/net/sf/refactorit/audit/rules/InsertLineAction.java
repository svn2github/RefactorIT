/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Set;


abstract class InsertLineAction extends MultiTargetCorrectiveAction {

	  protected String textToInsert;

	  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
	    if (!(violation instanceof SwitchCaseFallthrough)) {
	      return Collections.EMPTY_SET; // Foreign violation - do nothing
	    }

	    CompilationUnit compilationUnit = violation.getCompilationUnit();
	    BinStatementList list = ((SwitchCaseFallthrough)
	        violation).getStatementList();

	    // Add a break in user's IDE
	    manager.add(new StringInserter(compilationUnit, list.getEndLine(),
	        list.getEndColumn(), getTextWithIndents(list, textToInsert)));

	    return Collections.singleton(compilationUnit);
	  }

	  public static String getTextWithIndents(BinStatement list, String text) {
	    int indent = list.getIndent() + FormatSettings.getBlockIndent();

	    String result = "";
	    /*
	    if (list.getParent() instanceof AbstractLocationAware){
	      AbstractLocationAware parentElement
	          = (AbstractLocationAware) list.getParent();
	      if (parentElement.getEndLine() == list.getEndLine()){
	        result += "\n";
	      }
	    } */

	    result += FormatSettings.getIndentString(indent) + text
	        + FormatSettings.LINEBREAK;

	    return result;
	  }
	}
