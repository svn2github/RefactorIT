/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinAnnotationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.audit.duplicatestrings.NewFinalStringsDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
* @author Aleksei Sosnovski
*/

public class DuplicateStringsRule extends AuditRule {
  public static final String NAME = "duplicate_strings";

  private MultiValueMap map = new MultiValueMap();
  private int classCnt = 0;

  public void visit(BinCIType cls) {
    classCnt++;
    super.visit(cls);
  }

  // check if expr is not an annotation expression
  private boolean isAnnotationExpression(BinLiteralExpression expr) {
    BinItemVisitable item = expr.getParent();
    while(item instanceof BinExpression || item instanceof BinExpressionList) {
      if(item instanceof BinAnnotationExpression) {
        return true;
      }

      item = item.getParent();
    }
    return false;
  }

  public void visit(BinLiteralExpression expr) {
    if(isAnnotationExpression(expr)) {
      return;
    }

    String str = expr.getLiteral();

    if (str.startsWith("\"")) {
      boolean added = false;

      for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
        String key = (String) iter.next();

        if (key.equals(str)) {
          map.put(key, expr);
          added = true;
        }
      }
      if (!added) {
        map.put(str, expr);
      }// end of  for (Iterator iter = map.keySet().iterator()...
    }// end of if (str.startsWith("\""))

    super.visit(expr);
  }// end of visit(BinLiteralExpression expr method


  public void leave(BinCIType cls) {
    classCnt--;
    if (classCnt != 0) {
      return;
    }

    //through all encountered strings
    for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
      String key = (String) iter.next();
      List expressions = map.get(key);

      if (expressions.size() > 1) {

        String str =
            ((BinLiteralExpression) expressions.iterator().next()).getLiteral();

        BinCIType commonOwner = getCommonOwner(expressions);

        BinField field = getField(str, commonOwner);

        // through all encountered expressions
        for (Iterator iter2 = expressions.iterator(); iter2.hasNext(); ) {
          BinExpression expr = (BinExpression) iter2.next();
          boolean isFinalField = isFinalField(expr);

          try {
            if (field == null) {
              addViolation(new DuplicateStringsViolation(expr, commonOwner));
            } else

              if (!isFinalField){
                addViolation(new DuplicateStringsViolation(expr, field));
              }
          } catch (Exception e) {
            // ignore - sometimes somehow users have expression without owner, which is actually strange, but can't reproduce
          }
        }// end of  for (Iterator iter2 = expressions.iterator()...
      }// end of  if (strings.size() > 1)
    }// end of for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )

    map.clear();
  }// end of leave(BinClass cls) method

  private BinField getField(String str, BinCIType commonOwner) {
    BinCIType owner = commonOwner;

    while (owner != null) { // through all outer classes
      BinFieldDeclaration[] declarations = owner.getFieldDeclarations();

      for (int i = 0; i < declarations.length; i++) { // through all field declarations
        BinFieldDeclaration dec = declarations[i];
        BinVariable[] vars = dec.getVariables();

        if (((BinField) vars[0]).isFinal()) {// if fields declared as final

          for (int j = 0; j < vars.length; j++) {
            BinExpression expr = vars[j].getExpression();

            if (expr instanceof BinLiteralExpression) {
              String str2 = ((BinLiteralExpression) expr).getLiteral();

              if (str2.equals(str)) {
                return (BinField) vars[j];
              }
            }// end of if (expr instanceof BinLiteralExpression)
          }// end of for (int j = 0; j < vars.length; j++)
        }// end of if (((BinField) vars[0]).isFinal())
      }// end of for (int i = 0; i > declarations.length; i++)

      BinTypeRef ref = owner.getOwner();

      if (ref != null) {
        owner = ref.getBinCIType();
      } else {
        owner = null;
      }
    }// end of   while (owner != null)
    return null;
  }// end of getField method

  private BinCIType getCommonOwner(List expressions) {
    if (expressions.size() == 0) {
      return null;
    }

    Iterator i = expressions.iterator();

    BinCIType commonOwner =
        ((BinLiteralExpression) i.next()).getOwner().getBinCIType();

    for (; i.hasNext(); ) {
      BinCIType owner =
          ((BinLiteralExpression) i.next()).getOwner().getBinCIType();

      while (!owner.contains((LocationAware) commonOwner) &&
          !commonOwner.contains((LocationAware) owner)) {
        boolean endlessLoop = false;

        BinTypeRef ref1 = owner.getOwner();
        BinTypeRef ref2 = commonOwner.getOwner();

        if (ref1 != null) {
          owner = ref1.getBinCIType();
        }

        if (ref2 != null) {
          commonOwner = ref2.getBinCIType();
        }

        if (ref1 == null && ref2 == null) {//DEBUG
          endlessLoop = true;

          if (endlessLoop) {
            return null;
          }
        }

      }
      if (owner.contains((LocationAware) commonOwner)) {
        commonOwner = owner;
      }

    }
    return commonOwner;
  }// end of getCommonOwner method

  private boolean isFinalField (BinExpression expr) {
    BinItemVisitable parent = expr;

    while (!(parent instanceof BinFieldDeclaration)) {
      parent = parent.getParent();

      if (parent == null) {
        return false;
      }
    }

    BinField fld = (BinField) ((BinFieldDeclaration) parent).getVariables()[0];
    if (fld.isFinal()) {
      return true;
    }

    return false;
  }// end of isField() method

}// end of DuplicateStringsRule class


class DuplicateStringsViolation extends AwkwardExpression {
  public BinField field = null;
  public BinCIType targetOwner = null;

  public DuplicateStringsViolation(BinExpression expr, BinCIType targetOwner) {
    super (expr, "String " + ((BinLiteralExpression) expr).getLiteral() +
        " was already used in this class, final string can be created.",
        "dublicate_strings");
    this.targetOwner = targetOwner;
  }

  public DuplicateStringsViolation(BinExpression expr, BinField field) {
    super (expr, "There is final field with expression " +
        ((BinLiteralExpression) expr).getLiteral(),
        "dublicate_strings");
    this.field = field;
  }

  public List getCorrectiveActions() {
    List actions = new ArrayList();
    actions.add(DuplicateStringsCorrectiveAction.INSTANCE);
    return actions;
  }
}

class DuplicateStringsCorrectiveAction extends MultiTargetGroupingAction {
  static final DuplicateStringsCorrectiveAction INSTANCE =
      new DuplicateStringsCorrectiveAction();
  List exprs = new ArrayList();
  List names = new ArrayList();;
  List violationsGlobal;
  MultiValueMap map = new MultiValueMap();

  public String getKey() {
    return "refactorit.audit.action.duplicate_strings.substitute_with_constant";
  } // end of getKey()

  public String getName() {
    return "Substitute duplicate strings with constant";
  } // end of getName()

  public Set run(TransformationManager manager, TreeRefactorItContext context,
      List violations) {
    Set sources = new HashSet(violations.size());
    violationsGlobal = violations;

    // sorting violations
    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation viol = (RuleViolation) i.next();

      if (!(viol instanceof DuplicateStringsViolation)) {
        continue;
      }

      DuplicateStringsViolation violation = (DuplicateStringsViolation) viol;
      BinLiteralExpression expr =
          (BinLiteralExpression) violation.getSourceConstruct();
      String literal = expr.getLiteral();

      boolean flag = true;

      for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
        BinLiteralExpression key = (BinLiteralExpression) iter.next();

        if (literal.equals(key.getLiteral()) &&
            getLastOwner(expr) == getLastOwner(key)) {
          map.put(key, violation);
          flag = false;
          break;
        }
      }// end of for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )

      if (flag) {
        map.put(expr, violation);
      }
    }// end of  for (Iterator i = violations.iterator(); i.hasNext(); )


    sources.addAll(fieldCreator(context, manager, sortedList(map.keySet())));

    for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
      BinLiteralExpression key = (BinLiteralExpression) iter.next();

      for (Iterator iter2 = map.get(key).iterator(); iter2.hasNext(); ) {
        RuleViolation violation = (RuleViolation) iter2.next();
        sources.addAll(renamer(context, manager, violation));
      }// end of for (Iterator iter2 = map.get(key).iterator()...
    }// end of for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )

    map.clear();
    violationsGlobal.clear();
    exprs.clear();
    names.clear();
    violationsGlobal.clear();
    map.clear();
    return sources;
  }// end of run method

  private ArrayList sortedList(Set unsorted) {

    class ExpressionHolder implements Comparable{
      final BinExpression expression;
      final int startPosition;

      private ExpressionHolder(BinExpression expression) {
        this.expression = expression;
        this.startPosition = expression.getStartPosition();
      }

      public int compareTo (Object o) {
        ExpressionHolder holder = (ExpressionHolder) o;
        if (this.startPosition > holder.startPosition) {
          return 1;
        } else
        if (this.startPosition == holder.startPosition) {
          return 0;
        } else {
          return -1;
        }
      }
    }

    ArrayList expressionHolderList = new ArrayList();

    for (Iterator iter = unsorted.iterator(); iter.hasNext(); ) {
      expressionHolderList.add
          (new ExpressionHolder((BinExpression) iter.next()));
    }

    Collections.sort(expressionHolderList);

    ArrayList sorted = new ArrayList();

    for (int i = 0, max = expressionHolderList.size(); i < max; i++) {
      sorted.add(((ExpressionHolder) expressionHolderList.get(i)).expression);
    }

    return sorted;
  }

  protected Set fieldCreator(TreeRefactorItContext context,
      final TransformationManager manager, List expressions) {
    List toCreate = new ArrayList(); // list of expressions for which to create fields
    List classes = new ArrayList();

    for (Iterator iter = expressions.iterator(); iter.hasNext(); ) {
      BinLiteralExpression expr = (BinLiteralExpression) iter.next();
      List li = map.get(expr);
      DuplicateStringsViolation viol = (DuplicateStringsViolation) li.get(0);

      if (viol.field == null) { // if need to create field
        toCreate.add(expr);
        classes.add(viol.targetOwner);
      }
    }// end of for (Iterator iter = expressions.iterator(); iter.hasNext(); )

    if (toCreate.size() == 0) {
      return Collections.EMPTY_SET;
    }

    if (isTestRun()) {
      int cnt = 0;
      exprs = toCreate;
      names = new ArrayList();

      for (int i = 0; i < exprs.size(); i++) {
        cnt++;
        names.add("str" + cnt);
      }
    } else {
      NewFinalStringsDialog dlg =
          new NewFinalStringsDialog(toCreate, context, classes);
      dlg.show();

      if (!dlg.isOkPressed()) {
        names.clear();
        exprs.clear();
        return Collections.EMPTY_SET;
      }

      exprs = dlg.panel.getExpressions();
      names = dlg.panel.getFieldNames();
    }

    HashSet compilationUnits = new HashSet();

    Iterator namesIter = names.iterator();

    for (Iterator iter = exprs.iterator(); iter.hasNext(); ) {
      BinLiteralExpression expr = (BinLiteralExpression) iter.next();
      List li = map.get(expr);
      DuplicateStringsViolation viol = (DuplicateStringsViolation) li.get(0);
      BinCIType cls = viol.targetOwner;
      SourceCoordinate srcc =
          new SourceCoordinate(cls.getBodyAST().getStartLine(),
          cls.getBodyAST().getStartColumn());

      CompilationUnit compilationUnit = expr.getCompilationUnit();

      compilationUnits.add(compilationUnit);

      int indentt = cls.getIndent() + FormatSettings.getBlockIndent();

      String msg = FormatSettings.LINEBREAK;
      for (int i = 0; i < indentt; i++) {
        msg += " ";
      }
      msg += "final static String " + (String) namesIter.next() +
          " = " + expr.getLiteral() + ";";
      StringInserter inserter = new StringInserter
          (compilationUnit, srcc, msg);
      manager.add(inserter);
    }

    return compilationUnits;
  }

  protected Set renamer(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation viol)
      throws RuntimeException {

    DuplicateStringsViolation violation = (DuplicateStringsViolation) viol;

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinLiteralExpression expr =
        (BinLiteralExpression) violation.getSourceConstruct();

    SourceCoordinate srcc;

    String name = null;

    if (violation.field == null) {

      if (names.size() == 0) {
        return Collections.EMPTY_SET;
      }

      Iterator iter2 = names.iterator();

      boolean flag = true;
      for (Iterator iter = exprs.iterator(); iter.hasNext(); ) {
        BinLiteralExpression expr2 = (BinLiteralExpression) iter.next();
        String nam = (String) iter2.next();

        if (expr2.getLiteral().equals(expr.getLiteral()) &&
            getLastOwner(expr2) == getLastOwner(expr)) {
          flag = false;
          name = nam;
          break;
        }
      } // end of for (Iterator iter = exprs.iterator()....

      if (flag) { // DEBUG
        //System.out.println("Net polja, no i imja ne sozdano!");
        throw new RuntimeException("No private field with value " +
            expr.getLiteral() + " was found or cpecified");
      }
    } else {
      name = violation.field.getName();
    }

    StringEraser eraser = new StringEraser
        (expr);
    manager.add(eraser);

    srcc = new SourceCoordinate(expr.getStartLine(), expr.getStartColumn());
    StringInserter inserter =
        new StringInserter(compilationUnit, srcc, name);
    manager.add(inserter);
    return Collections.singleton(compilationUnit);
  }

  private BinCIType getLastOwner(BinExpression expr) {
    BinCIType owner = null;
    BinTypeRef type = expr.getOwner();

    while (type != null) {
      owner = type.getBinCIType();
      type = owner.getOwner();
    }

    return owner;
  }// end of getLastOwner method
}// end of DuplicateStringsCorrectiveAction class
