/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Arseni Grigorjev
 */
public class FieldWriteAccessFinder extends BinItemVisitor {
  public class WriteAccessFoundException extends RuntimeException{}

  private BinItem target = null;

  private Comparator comparator = new ItemTechComparator();

  private Map alreadyVisited = new TreeMap(comparator);
  private Set curSet;


  public boolean checkWriteAccess(final BinField field){
    setTarget(field);

    // Find usages
    try {
      if (field.isPrivate()){
        field.getCompilationUnit().accept(this);
      } else {
        //field.getProject().accept(visitor);
        CompilationUnit cUnit = field.getCompilationUnit();
        List sources = new ArrayList(field.getProject().getCompilationUnits());
        cUnit.accept(this);
        sources.remove(cUnit);

        for (Iterator it = sources.iterator(); it.hasNext();){
          ((CompilationUnit) it.next()).accept(this);
        }
      }
    } catch (FieldWriteAccessFinder.WriteAccessFoundException e){

      return true;
    }

    return false;
  }

  public FieldWriteAccessFinder(){

  }

  public void setTarget(BinField field){
    this.target = field;
  }

//  public final boolean containsName(final ASTImpl ast, final String name){
//    boolean result;
//
//    if (ast == null){
//      result = false;
//    } else if (ast.getText() != null && ast.getText().equals(name)){
//      result = true;
//    } else {
//      result = containsName((ASTImpl) ast.getFirstChild(), name);
//      if (result){
//        return result;
//      }
//
//      result = containsName((ASTImpl) ast.getNextSibling(), name);
//      if (result){
//        return result;
//      }
//    }
//
//    return result;
//  }

  public void visit(final CompilationUnit cUnit){
    curSet = (Set) alreadyVisited.get(cUnit);
    if (curSet == null){
      /*
       * run through asts and see wherever this compilation unit contains
       * name of this field or not (if not - no sense to check the classmodel!)
       */
      if (!cUnit.getSource().getASTTree().getIdents().contains(
          ((BinField) target).getName())) {
        return;
      }

      curSet = new TreeSet(comparator);
      alreadyVisited.put(cUnit, curSet);
      super.visit(cUnit);
    }

    if (curSet.contains(this.target)){
      throw new WriteAccessFoundException();
    }
  }

  public void visit(final BinFieldInvocationExpression expression) {
    if (BinVariableSearchFilter.isWriteAccess(expression)){
      curSet.add(expression.getField());
    }
    super.visit(expression);
  }
}

class ItemTechComparator implements Comparator{

  public int compare(Object o1, Object o2) {
    if (o1 instanceof BinField && o2 instanceof BinField){
      return compare((BinField) o1, (BinField) o2);
    } else if (o1 instanceof CompilationUnit && o2 instanceof CompilationUnit){
      return compare((CompilationUnit) o1, (CompilationUnit) o2);
    } else {
      return 0;
    }
  }

  private int compare(BinField o1, BinField o2){
    return o1.hashCode() - o2.hashCode();
  }

  private int compare(CompilationUnit o1, CompilationUnit o2){
    return o1.hashCode() - o2.hashCode();
  }

}
