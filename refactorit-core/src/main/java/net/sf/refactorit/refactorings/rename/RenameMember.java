/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * @author Jevgeni
 *
 */
public abstract class RenameMember extends RenameRefactoring {
  public RenameMember(String name, RefactorItContext context, BinItem item) {
    super(name, context, item);
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = super.checkPreconditions();

    if (getItem() instanceof BinMember) {
      BinMember item = (BinMember) getItem();
      if (item.getOwner() != null
          && !item.getOwner().getBinCIType().isFromCompilationUnit()) {
        status.addEntry("Specified item is not within your source path"
            + "\nand can not be renamed.", RefactoringStatus.ERROR);
      }
    }

    return status;
  }

  /**
   * Checks all places where member is invoked via single static import and
   * either adds a new single static import or changes the existing import,
   * depending whether some other invocatons depend on that single static import.
   *
   * @param transList
   * @param invocations
   */
  protected void addStaticImportChanges(TransformationList transList, List invocations) {
  	Map staticImportChanges = new HashMap();
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		CompilationUnit compilationUnit = invocation.getCompilationUnit();
  		if (staticImportChanges.get(compilationUnit) == null) {
	  		if ((invocation.getInConstruct() != null) && (invocation.getInConstruct() instanceof BinMemberInvocationExpression)) {
	  			BinMemberInvocationExpression expr = (BinMemberInvocationExpression) invocation.getInConstruct();
	  			BinMember renamedMember = expr.getMember();
	  			if (expr.invokedViaStaticImport()) {
	  				StaticImports.SingleStaticImport single =  compilationUnit.getStaticImports().getSingleStaticImportFor(renamedMember);
	  				if (single != null) {
	  					// member is invoked via single static import
	  					MultiValueMap singleStaticImportUsages = single.getUsages();
	  					boolean otherUsages = false;
	  					if (singleStaticImportUsages.keySet().size() > 1) {
 								otherUsages = true;
	  					}
	  					if (!otherUsages) {
	  						staticImportChanges.put(compilationUnit,
	  								new RenameTransformation(
	  		            compilationUnit,
				            single.getMemberNameNode(), getNewName()));
	  					} else {
	  						staticImportChanges.put(compilationUnit,
	  								new StringInserter(
	  				            compilationUnit,
	  				            single.getMemberNameNode().getEndLine(), single.getMemberNameNode().getEndColumn(),
	  										FormatSettings.LINEBREAK + "import static " + this.getItem().getParentType().getQualifiedName() + "." + getNewName() + ";"));
	  					}

	  				}
	  			}
	  		}
  		}

  	}
  	for (Iterator iter = staticImportChanges.values().iterator(); iter.hasNext();) {
  		transList.add(iter.next());

  	}
  }

  public String getDescription() {
    return super.getDescription();
  }

}
