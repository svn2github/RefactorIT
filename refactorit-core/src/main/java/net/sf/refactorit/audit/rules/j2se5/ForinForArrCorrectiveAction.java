/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;


import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Juri Reinsalu
 */
public class ForinForArrCorrectiveAction extends J2Se5CorrectiveAction {

  private static ForinForArrCorrectiveAction instance;
  private List usedNames = new ArrayList();

  static ForinForArrCorrectiveAction getInstance() {
    if (instance == null) {
      instance = new ForinForArrCorrectiveAction();
    }
    return instance;
  }
  
  public boolean isMultiTargetsSupported(){
    return true;
  }
  
  public Set run(TreeRefactorItContext context, List violations) {
    Set result = super.run(context, violations);
    usedNames.clear();
    return result;
  }

  protected Set process(TreeRefactorItContext context,
      TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof ForinForArrViolation)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    ForinForArrViolation forinViolation = (ForinForArrViolation) violation;
    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinSourceConstruct srcConstr = ((AwkwardSourceConstruct) violation)
            .getSourceConstruct();
    BinForStatement forStatement = (BinForStatement) srcConstr;

    StringEraser eraser = new StringEraser(compilationUnit, forStatement
            .getInitSourceConstruct().getStartPosition(), forStatement
            .iteratorExpressionList().getEndPosition());
    manager.add(eraser);
    String itemVarName = createItemVarName(forinViolation);
    BinTypeRef arrayTypeRef = forinViolation.getArrayVariable().getTypeRef();
    BinTypeRef nonArrayTypeRef = arrayTypeRef.getNonArrayType();
		/*System.out.println("Array: " + forinViolation.getArrayVariable().getText() + "\n" + 
				"BinTypeRef: " + arrayTypeRef.getName() + "\n" + 
				"nonArrayTypeRef: " + nonArrayTypeRef.getName() + "\n" + 
				"Qualified name: " + arrayTypeRef.getQualifiedName() + "\n" + 
				": " + arrayTypeRef.getBinCIType().getText() + nonArrayTypeRef.getBinCIType().isInnerType() + "\n" + 
				"NAT CLASS: " + nonArrayTypeRef.getClass() + "\n" + 
				"BinType: " + arrayTypeRef.getBinType() + "\n" + 
				((BinArrayType) arrayTypeRef.getBinType()).getArrayType() + ((BinArrayType) arrayTypeRef.getBinType()).getDimensionString());
		getNonArrayType(arrayTypeRef);*/
    //arrayType.g
		

    createForinInit(manager, arrayTypeRef, forinViolation, forStatement,
            itemVarName);

    BinArrayUseExpression[] arrayUses = forinViolation.getArrayUses();
    StringInserter inserter;
    for (int i = 0; i < arrayUses.length; i++) {
      eraser = new StringEraser(compilationUnit, arrayUses[i]
              .getStartPosition(), arrayUses[i].getEndPosition());
      manager.add(eraser);

      inserter = new StringInserter(compilationUnit, arrayUses[i]
              .getStartLine(), arrayUses[i].getStartColumn(), itemVarName);
      manager.add(inserter);
    }
    return Collections.singleton(compilationUnit);
  }

  /**
   * @param forinViolation
   * @param compilationUnit
   * @param forStatement
   * @return
   */
  private void createForinInit(TransformationManager manager,
          BinTypeRef itemTypeRef, ForinForArrViolation forinViolation,
          BinForStatement forStatement, String itemVarName){
		final SourceCoordinate insertAt = new SourceCoordinate(forStatement
            .getInitSourceConstruct().getStartLine(), forStatement
            .getInitSourceConstruct().getStartColumn());
		
		//BinLocalVariable local = new BinLocalVariable(itemVarName, itemTypeRef, 0);
    //local.setExpression(forinViolation.getArrayUses()[0].getArrayExpression());
		//String dimensions =((BinArrayType) itemTypeRef.getBinType()).getDimensionString(); 
		String newVar = formatDecreasedDimensions(itemTypeRef, true) + " " + itemVarName + " : " + 
				forinViolation.getArrayUses()[0].getArrayExpression().getText();
    //String newVar = ((BinLocalVariableFormatter)local.getFormatter()).formHeader() + ": " + 
		//forinViolation.getArrayUses()[0].getArrayExpression().getText();
    StringInserter inserter = new StringInserter(forStatement
            .getCompilationUnit(), insertAt, newVar);
    manager.add(inserter);
  }
	
	//private String getName

  /**
   * @param forinViolation
   * @return
   */
  private String createItemVarName(ForinForArrViolation forinViolation) {
    String conveinientVarName = NameUtil
            .extractConvenientVariableNameForType(((BinArrayType) forinViolation
                    .getArrayVariable().getTypeRef().getBinType())
                    .getArrayType());
    return createNonConflictingName(forinViolation, conveinientVarName);
  }

  /**
   * if conflicts occur, tries the same name with a concatenated integer 1, if
   * still not ok, then 2,3,... and so on
   * 
   * @param forinViolation
   * @param conveinientVarName
   * @return
   */
  private String createNonConflictingName(ForinForArrViolation forinViolation,
          String conveinientVarName) {
    int incrementBy = 0;
    LocalVariableDuplicatesFinder duplicatesFinder;
    String newName;
    do {
      incrementBy++;
      if (incrementBy > 1)
        newName = conveinientVarName + incrementBy;
      else
        newName = conveinientVarName;
      duplicatesFinder = new LocalVariableDuplicatesFinder(null, newName,
              ((BinForStatement) forinViolation.getSourceConstruct())
                      .getInitSourceConstruct());
      forinViolation.getSourceConstruct().getParentMember().defaultTraverse(
              duplicatesFinder);

      Iterator i = duplicatesFinder.getDuplicates().iterator();
      while (i.hasNext()) {
        BinItem element = (BinItem) i.next();
        if (element.isSame(forinViolation.getIteratorVariable()))
          i.remove();
      }
    } while (duplicatesFinder.getDuplicates().size() > 0 || 
        usedNames.contains(newName));
    usedNames.add(newName);
    return newName;
  }

  public String getKey() {
    return "refactorit.audit.action.forin.introduce.from.for.arr";
  }

  public String getName() {
    return "Introduce jdk5.0 for/in construct (for-loops array traversal)";
  }
	
	private static String formatDecreasedDimensions(BinTypeRef ref, boolean allOwners){
		String result = "";
		if(ref.isArray()){
			BinArrayType type = (BinArrayType) ref.getBinType();
			result += (allOwners ? type.getArrayType().getBinType().getNameWithAllOwners() : type.getName());
			for(int i=0; i<type.getDimensions() - 1; i++)
				result += "[]";
			
		}
		return result.replace('$', '.');
		
	}

}
