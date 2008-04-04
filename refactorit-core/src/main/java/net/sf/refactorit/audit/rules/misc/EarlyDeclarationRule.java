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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 *
 * @author  Aleksei Sosnovski
 */


public class EarlyDeclarationRule extends AuditRule {
  public static final String NAME = "early_declaration";

  private HashMap blocksAndVars = new HashMap();
  private HashMap usagesAndVars = new HashMap();
  private HashMap statementsAndVars = new HashMap();
  private HashSet declarations = new HashSet();
  private BinExpressionStatement lastExpr;
  private BinExpressionStatement firstExpr;

  public void visit(BinVariableUseExpression expr) {

    if (lastExpr != null && !lastExpr.contains(expr)) {
      addFirstUse(firstExpr);
      declarations.clear();
      lastExpr = null;
      firstExpr = null;
    }

    BinVariable var = expr.getVariable();
    BinLocalVariableDeclaration dec;

    // if not parameter
    if (var.getParent() instanceof BinLocalVariableDeclaration) {
      dec = (BinLocalVariableDeclaration) var.getParent();
    } else { // end of if (var.getParent() instanceof...
      blocksAndVars.remove(var);
      usagesAndVars.remove(var);
      return;
    }

    // if first use in same block as declaration and no expression between
    if (declarations.contains(dec) &&
        blocksAndVars.containsKey(var) &&
        getParentBlock(expr) == getParentBlock(dec) &&
        !usagesAndVars.containsKey(var)) {
      blocksAndVars.remove(var);
      usagesAndVars.remove(var);
    } else // end of if (declarations.contains(dec) &&....

    // if first use in another block than declaration and no expression between
    if (declarations.contains(dec) &&
        blocksAndVars.containsKey(var) &&
        !usagesAndVars.containsKey(var)) {
      blocksAndVars.put(var, getParentBlock(expr));
      usagesAndVars.put(var, expr);
    } else // end of  if (declarations.contains(dec) &&....

    // if first use in same block and expression between
    if (getParentBlock(expr) == getParentBlock(dec) &&
        blocksAndVars.containsKey(var) &&
        !usagesAndVars.containsKey(var)) {
      usagesAndVars.put(var, expr);
    } else // end of if (getParentBlock(expr) == getParentBlock(dec) &&...

    // if first use in another block and expression between
    if (!usagesAndVars.containsKey(var) && blocksAndVars.containsKey(var)) {
      blocksAndVars.put(var, getParentBlock(expr));
      usagesAndVars.put(var, expr);
    } else // end of if (!usagesAndVars.containsKey(var))

    if (blocksAndVars.containsKey(var) && usagesAndVars.containsKey(var)) {
      //if not first use
      BinItemVisitable block = (BinItemVisitable) blocksAndVars.get(var);
      blocksAndVars.put(var, getCommonParentBlock(block, expr));
    } // end of if (blocksAndVars.containsKey(var))

    super.visit(expr);
  } // end of visit(BinVariableUseExpression expr)

  public void visit(BinLocalVariableDeclaration blvd) {
    if (lastExpr != null) {

      addFirstUse(firstExpr);
      declarations.clear();
      lastExpr = null;
      firstExpr = null;
    } // end of   if (lastExpr != null)

    if (!(blvd.getParent() instanceof BinForStatement)) {
      declarations.add(blvd);
      BinItemVisitable parent = getParentBlock(blvd);
      BinVariable[] var = blvd.getVariables();

      for (int i = 0; i < var.length; i++) {

        declarationExpressionVisitor visitor =
            new declarationExpressionVisitor();
        BinExpression expr = var[i].getExpression();

        if (expr != null) {
          expr.accept(visitor);
        } // end of if (expr != null)

        // if doesnt't include field/method invocation or variable use
        if (visitor.isOk()) {
          blocksAndVars.put(var[i], parent);
        } // end of if (visitor.isOk())
      } // end of for (int i = 0; i < var.length; i++)
    } // end of id (!blvd.getParent() instanceof BinForStatement)

     super.visit(blvd);
  } // end of visit(BinLocalVariableDeclaration blvd)

  public void visit(BinExpressionStatement bes) {
    if (lastExpr == null) {
      lastExpr = bes;
      firstExpr = bes;
    } else {

      addFirstUse(firstExpr);
      declarations.clear();
      lastExpr = bes;
    }
     super.visit(bes);
  } // end of visit(BinExpressionStatement bes) method

  private void addFirstUse(LocationAware statement) {
    //through all declarations
    for (Iterator iter = declarations.iterator(); iter.hasNext(); ) {
      BinLocalVariableDeclaration dec =
          (BinLocalVariableDeclaration) iter.next();
      BinVariable[] var = dec.getVariables();

      //for all variables in declaration
      for (int i = 0; i < var.length; i++) {

        if (blocksAndVars.containsKey(var[i])) {
          statementsAndVars.put(var[i], statement);
        } // end of if (blocksAndVars.containsKey(var[i]))
      } // end of for (int i = 0; i < var.length; i++)
    } // end of for (Iterator iter = declarations.iterator();
  } // end of addFirstUse method

  private BinItemVisitable ifMoveIntoCycle(BinItemVisitable decBlock,
      BinItemVisitable commonBlock) {
    BinItemVisitable candidateBlock = commonBlock;
    BinItemVisitable currentBlock = commonBlock;
    while (currentBlock != decBlock) {

      if (currentBlock instanceof BinForStatement ||
          currentBlock instanceof BinWhileStatement ||
          currentBlock instanceof BinSwitchStatement ||
          currentBlock instanceof BinNewExpression) {
        candidateBlock = currentBlock;
      } // end of if (currentBlock instanceof BinForStatement

      currentBlock = currentBlock.getParent();
    } // end of  while (currentBlock != decBlock)

    return candidateBlock;
  } // end of ifMoveIntoCycle method

  private BinItemVisitable getCommonParentBlock
      (BinItemVisitable block, BinItemVisitable use) {
    int line = ((LocationAware) use).getStartLine();
    HashSet blocks = new HashSet();
    HashSet blocks2 = new HashSet();
//    if (!(block instanceof BinStatementList)) {
//      block = getParentBlock (block);
//      System.out.println("Block in fact is not block!!!");
//    }
    BinItemVisitable parent = block;
    BinItemVisitable parent2 = getParentBlock(use);
    blocks.add(parent);
    blocks2.add(parent2);

    while (parent != null) {
      parent = getParentBlock(parent);
      blocks.add(parent);
    }

    while (parent2 != null) {
      parent2 = getParentBlock(parent2);
      blocks2.add(parent2);
    }

    parent = block;
    parent2 = getParentBlock(use);

    while (true) {
      if (blocks2.contains(parent) && parent != null) {
        return parent;
      }
      if (blocks.contains(parent2) && parent2 != null) {
        return parent2;
      }
      if (parent != null) {
        parent = getParentBlock(parent);
      }
      if (parent2 != null) {
        parent2 = getParentBlock(parent2);
      }
      if (parent == null && parent2 == null) {
        System.err.println("Error in getCommonParentBlock, line " + line);
        break;
      }
    } // end of  while (true)

    return parent;
  } // end of getCommonBlock method

  public static BinItemVisitable getParentBlock(BinItemVisitable item) {
    BinItemVisitable parent = item.getParent();

    while (!(parent instanceof BinStatementList) && parent != null) {
      parent = parent.getParent();
    }
    return parent;
  }

  private BinItemVisitable getParentBeforeBlock(BinItemVisitable item) {
    BinItemVisitable previousParent = item;
    BinItemVisitable parent = item.getParent();

    while (!(parent instanceof BinStatementList) && parent != null) {
      previousParent = parent;
      parent = parent.getParent();
    }

    return previousParent;
  }

  private BinItemVisitable getBlockBeforeBlock
      (BinItemVisitable commonBlock, BinItemVisitable firstUse) {
    BinItemVisitable block1 = getParentBlock(firstUse);
    BinItemVisitable block2 = null;
    while (block1 != commonBlock) {
      block2 = block1;
      block1 = getParentBlock(block1);
    }
    return block2;
  }

  public void postProcess() {
    for (Iterator iter = usagesAndVars.keySet().iterator(); iter.hasNext(); ) {
      BinItemVisitable var = (BinItemVisitable) iter.next();
      BinVariableUseExpression firstUse =
          (BinVariableUseExpression) usagesAndVars.get(var);
      BinItemVisitable commonBlock = (BinItemVisitable) blocksAndVars.get(var);

      // if declaration and first use in same block
      if (getParentBlock(var.getParent()) == getParentBlock(firstUse)) {
        BinItemVisitable coordinateSrc = getParentBeforeBlock(firstUse);
        int line = ((LocationAware) coordinateSrc).getStartLine();
        int column = ((LocationAware) coordinateSrc).getStartColumn() - 1;
        SourceCoordinate src = new SourceCoordinate(line, column);
        addViolation(new EarlyDeclarationViolation
            ((BinMember) var, src, false, firstUse));
      } else // end of if (getParentBlock(var.getParent()) == ....

      // if want to move into another block
      if (getParentBlock(var.getParent()) != commonBlock) {
        LocationAware target = (LocationAware)
            ifMoveIntoCycle(getParentBlock(var.getParent()), commonBlock);

        // if for or while, for or switch expression found
        if (target instanceof BinForStatement ||
            target instanceof BinWhileStatement ||
            target instanceof BinSwitchStatement ||
            target instanceof BinNewExpression) {

          BinItemVisitable tmp = ((BinItemVisitable) target).getParent();
          if (!(tmp instanceof BinStatementList)) {
            target = (LocationAware)
                getParentBeforeBlock((BinItemVisitable) target);
          }

          // if target and declaration in same block and statement between
          if (statementsAndVars.containsKey(var) &&
              ((LocationAware) statementsAndVars.get(var)).getStartPosition() <
              target.getStartPosition() &&
              getParentBlock((BinItemVisitable) target) ==
              getParentBlock(var.getParent())) {
            int line = target.getStartLine();
            int column = target.getStartColumn() - 1;
            SourceCoordinate src = new SourceCoordinate(line, column);
            addViolation(new EarlyDeclarationViolation
                ((BinMember) var, src, false, (BinItemVisitable) target));
          } else // end of if (((LocationAware) statementsAndVars.get(var))....

          // if target and declaration in different blocks
          if (getParentBlock(((BinItemVisitable) target))
              != getParentBlock(var.getParent())) {
            BinStatementList parentBlock =
                (BinStatementList) getParentBlock((BinItemVisitable) target);

            //if block starts with '{'
            if (addBlockViolation(var, (BinItemVisitable) parentBlock)) {
              int line = parentBlock.getStartLine();
              int column = parentBlock.getStartColumn();
              SourceCoordinate src = new SourceCoordinate(line, column);
              addViolation(new EarlyDeclarationViolation
                  ((BinMember) var, src, true, parentBlock));
            }
          }
        } else { // end of if (target instanceof BinForStatement...

          // if no cycle found
          //if block starts with '{'
          if (addBlockViolation(var, (BinItemVisitable) target)) {
            int line = target.getStartLine();
            int column = target.getStartColumn();
            SourceCoordinate src = new SourceCoordinate(line, column);
            addViolation(new EarlyDeclarationViolation
                ((BinMember) var, src, true, (BinItemVisitable) target));
          } // end of
        } // end of else
      } else // end of if (getParentBlock(var.getParent()) != commonBlock)

      // if move into block with declaration, but first use in another
      // and statement between declaration and item containing first use
      if (getParentBlock(var.getParent()) == commonBlock &&
          statementsAndVars.containsKey(var) &&
          ((LocationAware) statementsAndVars.get(var)).getStartPosition() <
          ((LocationAware) getParentBeforeBlock(getBlockBeforeBlock(commonBlock,
          (BinItemVisitable) usagesAndVars.get(var)))).getStartPosition()) {

        BinItemVisitable block = getBlockBeforeBlock(commonBlock,
            (BinItemVisitable) usagesAndVars.get(var));
        BinItemVisitable coordinateSrc = getParentBeforeBlock(block);

        int line = ((LocationAware) coordinateSrc).getStartLine();
        int column = ((LocationAware) coordinateSrc).getStartColumn() - 1;
        SourceCoordinate src = new SourceCoordinate(line, column);
        addViolation(new EarlyDeclarationViolation
            ((BinMember) var, src, false, coordinateSrc));
      } // end of if (getParentBlock(var.getParent()) == commonBlock &&...
    } // end of  for (Iterator iter = usagesAndVars.keySet().iterator();...

    usagesAndVars.clear();
    blocksAndVars.clear();
    statementsAndVars.clear();
    lastExpr = null;
    firstExpr = null;

  } // end of postProcess() method

  private boolean addBlockViolation
      (BinItemVisitable var, BinItemVisitable block) {
    if (((BinStatementList) block).getText().startsWith("{")) {
      return true;
    }
    return false;
  }
} // end of EarlyDeclaration class


class EarlyDeclarationViolation extends AwkwardMember {

  SourceCoordinate coordinate;
  BinItemVisitable var;
  boolean isBlock;
  BinItemVisitable block;

  public EarlyDeclarationViolation
      (BinMember variable, SourceCoordinate src,
      boolean isBlck, BinItemVisitable blck) {
    super(variable, "Statements between declaration and first use",
        "early_declaration");
    coordinate = src;
    var = (BinItemVisitable) variable;
    isBlock = isBlck;
    block = blck;
  }

  public List getCorrectiveActions() {
    List actions = new ArrayList();
    actions.add(EarlyDeclarationCorrectiveAction.INSTANCE);
    return actions;
  }
}


class EarlyDeclarationCorrectiveAction extends MultiTargetGroupingAction {
  static final EarlyDeclarationCorrectiveAction INSTANCE =
      new EarlyDeclarationCorrectiveAction();
  MultiValueMap map = new MultiValueMap();
  List violationsGlobal;
  MultiValueMap commentsMap = new MultiValueMap();

  public String getKey() {
    return "refactorit.audit.action.early_declaration.move_closer_to_first_use";
  }

  public String getName() {
    return "Move declaration closer to first use case";
  }

  public Set run(TransformationManager manager,
      TreeRefactorItContext context, List violations) {
    Set sources = new HashSet(violations.size());

    violationsGlobal = violations;

    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation violation = (RuleViolation) i.next();

      // considering only early declaration violations
      if (violation instanceof EarlyDeclarationViolation) {
        BinItemVisitable variable = ((EarlyDeclarationViolation) violation).var;
        BinLocalVariableDeclaration declaration =
            (BinLocalVariableDeclaration) variable.getParent();

        map.put(declaration, violation);
      } // end of if (violation instanceof EarlyDeclarationViolation)
    } // end of for (Iterator i = violations.iterator(); i.hasNext(); )

    Set keyset = map.keySet();
    Iterator iter = keyset.iterator();

    while (iter.hasNext()) { // through all declarations with violations
      BinLocalVariableDeclaration key =
          (BinLocalVariableDeclaration) iter.next();
      Iterator iter2 = map.get(key).iterator();
      sources.addAll(declarationBreaker(context, manager, key));

      while (iter2.hasNext()) { // through all violations in declaration
        RuleViolation violation = (RuleViolation) iter2.next();
        sources.addAll(firstViolationManager(context, manager, violation));
      } // end of while(iter2.hasNext())

    } // end of while (iter.hasNext())

    violationsGlobal.clear();
    map.clear();
    commentsMap.clear();
    return sources;
  } // end of run method

  protected Set declarationBreaker(TreeRefactorItContext context,
      final TransformationManager manager,
      BinLocalVariableDeclaration declaration) {

    CompilationUnit compilationUnit = declaration.getCompilationUnit();

    BinVariable[] variables = declaration.getVariables();
    BinItemVisitable variable = variables[0];
    manageComments(variable);

    StringEraser eraser = new StringEraser(declaration);
    manager.add(eraser);

    ArrayList leftVars = new ArrayList();

    //find variables that will not be moved from violated declatration
    //through all variables in declaration
    for (int j = 0; j < variables.length; j++) {
      int tmp = 0;

      // through all violatons
      for (Iterator iter = violationsGlobal.iterator(); iter.hasNext(); ) {
        RuleViolation violation2 = (RuleViolation) iter.next();

        // considering only early declaration violations
        if (violation2 instanceof EarlyDeclarationViolation) {
          BinLocalVariable var =
              (BinLocalVariable) ((EarlyDeclarationViolation) violation2).var;

          if (var == variables[j]) {
            tmp = 1;
            break;
          } // end of if (var == variables[j])
        } // end of if (violation2 instanceof EarlyDeclarationViolation)
      } // end of for (Iterator iter = violationsGlobal.iterator(); ....

      if (tmp == 0) {
        leftVars.add(variables[j]);
      } // end of  if (tmp == 0)
    } // end of  for (int j=0; j < variables.length; j++)

    // Write declarations that don't need to be moved
    StringInserter inserter = writeUnmovedDeclaration
        (compilationUnit, declaration, leftVars);
    if (inserter != null) {
      manager.add(inserter);
    } // end of if (inserter != null)

    // Delete comments for last variable in declaration if not moved
    BinItemVisitable lastVar = variables[variables.length - 1];

    if (leftVars.contains(lastVar)) {
      for (Iterator iter =
          commentsMap.get(lastVar).iterator(); iter.hasNext(); ) {
        Comment comment = (Comment) iter.next();
        if (comment != null) {
          eraser = new StringEraser(comment);
          manager.add(eraser);
        }
      }
    } // end of if (leftVars.contains(variables[variables.length - 1]))

    return Collections.singleton(compilationUnit);
  } // end of declarationBreaker method

  protected Set firstViolationManager(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation violation) {

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinItemVisitable variable = ((EarlyDeclarationViolation) violation).var;
    BinLocalVariableDeclaration declaration =
        (BinLocalVariableDeclaration) variable.getParent();

    // Write moved declarations
    manager.add(writeMovedDeclaration(compilationUnit, violation));

    // Delete comments for moved declarations
    for (Iterator iter =
        commentsMap.get(variable).iterator(); iter.hasNext(); ) {
      Comment comment = (Comment) iter.next();
      if (comment != null) {
        StringEraser eraser = new StringEraser(comment);
        manager.add(eraser);
      }
    }

    return Collections.EMPTY_SET;
  } // end of firstViolationManager method

  private void manageComments(BinItemVisitable var) {
    //if this declaration was already parsed
    if (commentsMap.containsKey(var)) {
      return;
    }

    BinLocalVariableDeclaration dec =
        (BinLocalVariableDeclaration) var.getParent();
    BinVariable[] variables = dec.getVariables();
    BinItemVisitable declarationBlock =
        EarlyDeclarationRule.getParentBlock((BinItemVisitable) dec);
    List comments =
        Comment.getCommentsIn((LocationAware) declarationBlock);

    int lastInd = variables.length - 1;

    // for every variable in declaration except last
    for (int i = 0; i < variables.length; i++) {

      // for every comment
      for (Iterator iter = comments.iterator(); iter.hasNext(); ) {
        Comment comment = (Comment) iter.next();

        //coordinates of comment
        int line = comment.getStartLine();
        int column = comment.getStartColumn();
        SourceCoordinate src = new SourceCoordinate(line, column);

        // coordinates of variavle
// NOTE it is better to take end of expression, not start of variable name
        SourceCoordinate src2 = variables[i].getNameStart();
        int line2 = src2.getLine();

        // if first variable
        if (i == 0 && src.isBefore(src2)) {
          SourceCoordinate src3 = new SourceCoordinate(
              dec.getStartLine(), dec.getStartColumn());
          if (!src.isBefore(src3)) {
            commentsMap.put(variables[0], comment);
          }
        }

        // if comment between 2 variables
        //if comment on same line as first variable
        if (i != lastInd &&
            !src.isBefore(src2) &&
            src.isBefore(variables[i + 1].getNameStart()) &&
            line2 == line) {
          commentsMap.put(variables[i], comment);
        } else // end of if (!src.isBefore(src2) && ...

        // if comment on another line than first variable
        if (i != lastInd &&
            !src.isBefore(src2) &&
            src.isBefore(variables[i + 1].getNameStart()) &&
            line2 != line) {
          commentsMap.put(variables[i + 1], comment);
        } else // end of if (!src.isBefore(src2) && ...

        // for last variable in declaration
        if (i == lastInd &&
            !src.isBefore(src2) && line2 == line) {
          commentsMap.put(variables[lastInd], comment);
        }
      } // end of for (Iterator iter = comments.iterator(); iter.hasNext(); )

      //if no comments found for variable
      if (!commentsMap.containsKey(variables[i])) {
        commentsMap.put(variables[i], null);
      } // end of if (!commentsMap.containsKey(variables[i]))
    } // end of for (int i = 0; i < variables.length - 1; i++)
  } // end of manageComments method

  private String getCommentText(BinItemVisitable var) {
    String commentText = "";

    for (Iterator iter =
        commentsMap.get(var).iterator(); iter.hasNext(); ) {
      Comment comm = (Comment) iter.next();

      if (comm != null) {
        commentText += " " + comm.getText();
      }
    }

    return commentText;
  }

  private StringInserter writeUnmovedDeclaration
      (CompilationUnit compilationUnit,
      BinLocalVariableDeclaration declaration, ArrayList leftVars) {

    String LINEBREAK = FormatSettings.LINEBREAK;
    String indent = "";

    for (int i = 0; i < declaration.getIndent(); i++) {
      indent += " ";
    }

    if (leftVars.size() > 0) {
      String text = getDeclarationText((BinLocalVariable) leftVars.get(0));

      text += getCommentText((BinItemVisitable) leftVars.get(0));

      for (int i = 1; i < leftVars.size(); i++) {
        text += LINEBREAK + indent +
            getDeclarationText((BinLocalVariable) leftVars.get(i));

        text += getCommentText((BinItemVisitable) leftVars.get(i));
      }

      StringInserter inserter = new StringInserter(
          compilationUnit,
          declaration.getStartLine(),
          declaration.getStartColumn() - 1,
          text);
      return inserter;
    }
    return null;
  } // end of writeUnmovedDeclaration method

  private StringInserter writeMovedDeclaration
      (CompilationUnit compilationUnit, RuleViolation violation) {
    String LINEBREAK = FormatSettings.LINEBREAK;

    BinItemVisitable variable = ((EarlyDeclarationViolation) violation).var;
    boolean isblock = ((EarlyDeclarationViolation) violation).isBlock;
    BinItemVisitable block = ((EarlyDeclarationViolation) violation).block;
    SourceCoordinate coordinate =
        ((EarlyDeclarationViolation) violation).coordinate;

    int indentt;

    if (!isblock) {
      block = EarlyDeclarationRule.getParentBlock(block);
    }

    indentt = ((BinStatementList) block).getIndent();
    indentt += FormatSettings.getBlockIndent();

    String indent = "";

    for (int i = 0; i < indentt; i++) {
      indent += " ";
    }

    String text;

    if (isblock) {
      text = LINEBREAK + indent +
          getDeclarationText((BinLocalVariable) variable);
      text += getCommentText(variable);
    } else {
      text = getDeclarationText((BinLocalVariable) variable);
      text += getCommentText(variable);
      text += LINEBREAK + indent;
    }

    StringInserter inserter = new StringInserter(
        compilationUnit,
        coordinate,
        text);

    return inserter;
  } // end of writeMovedDeclaration

  private String getDeclarationStart(BinVariable var) {
    //Gets modifiers and type of variable

    String type = BinFormatter.format(var.getTypeRef());
    String tmp = type.replace('[', ' ');
    tmp = tmp.replace(']', ' ');
    tmp = tmp.trim();

    String declarationstart = var.getTypeAndModifiersNodeText();

    int typePos = declarationstart.indexOf(tmp);

    try {
      declarationstart = declarationstart.substring(0, typePos) + type + " ";
    } catch (Exception e) {
      declarationstart = type + " ";
    }

    return declarationstart;
  } // end of getDeclarationStart method

  private String getDeclarationExpression(BinVariable var) {
    String declarationExpression = var.getName();
    String expressionNode = var.getExprNodeText();

    if (expressionNode != null) {
      declarationExpression += " = " + expressionNode;
    }
    return declarationExpression + ";";
  } // end of getDeclarationExpression method

  protected String getDeclarationText(BinVariable var) {
    String declarationstart =
        getDeclarationStart(var); ;
    String declarationexpression =
        getDeclarationExpression(var);
    return declarationstart + declarationexpression;
  } // end of getDeclarationText method
} // end of EarlyDeclarationCorrectiveAction class


class declarationExpressionVisitor extends BinItemVisitor {
  private boolean isOk = true;

  public void visit(BinFieldInvocationExpression fieldInvoc) {
    isOk = false;
  }

  public void visit(BinMethodInvocationExpression methodInvoc) {
    isOk = false;
  }

  public void visit(BinVariableUseExpression varUse) {
    isOk = false;
  }

  public boolean isOk() {
    return isOk;
  }
}
