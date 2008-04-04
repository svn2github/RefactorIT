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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.query.notused.NotUsedIndexer;
import net.sf.refactorit.source.edit.FileEraser;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.DeclarationSplitTransformation;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.CommentAllocator;
import net.sf.refactorit.utils.CommentOutHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



public class NotUsedRulesAddOn extends AuditRule {
  public static final String NAME = "not_used";

  private ArrayList rules = new ArrayList();

  private Set[] resultMembers;
  private MultiValueMap nuFieldAssignments;

  private Set resultTypes;

  private boolean collected = false;

  public NotUsedRulesAddOn() {
    setName("Not used members and types");
    setPriority(Priority.NORMAL);
  }

  public boolean hasRulesToCheck() {
    return (rules.size() > 0);
  }

  public void addRuleToCheck(ExcludeFilterRule rule) {
    rules.add(rule);
  }

  public void visit(BinCIType type) {
    BinTypeRef typeRef = type.getTypeRef();
    if (resultTypes.contains(type.getTypeRef())) {
      addViolation(new NotUsedTypeViolation(typeRef, checkIfSafe(typeRef,
          resultTypes)));
    }
    super.visit(type);
  }

  public void visit(BinField field) {
    if (resultMembers[0].contains(field)) {
      addViolation(new NotUsedFieldViolation(field, checkIfSafe(field,
          resultMembers[0]), nuFieldAssignments.get(field)));
    }
    super.visit(field);
  }

  public void visit(BinMethod method) {
    if (resultMembers[1].contains(method)) {
      addViolation(new NotUsedMemberViolation(method, checkIfSafe(method,
          resultMembers[1])));
    }
    super.visit(method);
  }

  public void visit(CompilationUnit c) {
    if (!collected) {
      collected = true;

      ExcludeFilterRule[] castArray = new ExcludeFilterRule[rules.size()];
      for (int i = 0; i < rules.size(); i++) {
        ExcludeFilterRule rule = (ExcludeFilterRule) rules.get(i);
        castArray[i] = rule;
      }

      NotUsedIndexer nui = new NotUsedIndexer(castArray);

      nui.visit(c.getProject());

      resultTypes = nui.getNotUsedTypes();

      resultMembers = new Set[2];
      resultMembers[0] = nui.getNotUsedFields();
      resultMembers[1] = nui.getNotUsedMethods();
      nuFieldAssignments = nui.getFieldAssignments();
      //nui.clear();
    }
    super.visit(c);
  }

  // actually lame
  // FIXME: isn't it that if subclass is used then supertypes are already not in
  // the unused set, so it doesn't get here?
  private boolean checkIfSafe(BinTypeRef ref, Set resultTypes) {
    List hierarchy = ref.getAllSubclasses();
    // hierarchy.addAll(ref.getAllSupertypes());
    hierarchy.add(ref);
    for (int i = 0; i < hierarchy.size(); i++) {
      BinTypeRef tmpRef = (BinTypeRef) hierarchy.get(i);
      if (!resultTypes.contains(tmpRef)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkIfSafe(BinMember member, Set resultMembers) {
    if (member instanceof BinMethod) {
      BinMethod meth = (BinMethod) member;

      List hierarchy = meth.findAllOverrides();
      for (int i = 0; i < hierarchy.size(); i++) {
        BinMethod tmpMeth = (BinMethod) hierarchy.get(i);
        if (!hierarchy.contains(tmpMeth)) {
          return false;
        }
      }
    }
    return true;
  }
}

class NotUsedMemberViolation extends AwkwardMember {
  private boolean isSafe;

  public NotUsedMemberViolation(BinMember member, boolean safe) {
    super(member, "Not used member '" + member.getName() + "'",
        "refact.audit.notused.member");
    this.isSafe = safe;
  }

  public List getCorrectiveActions() {
    List actions = new ArrayList();
    actions.add(CommentNotUsedMemberAction.INSTANCE);
    if (isSafe) {
      actions.add(RemoveNotUsedMemberAction.INSTANCE);
    }
    return actions;
  }

  public boolean canBeDeleted() {
    return isSafe;
  }
}

class NotUsedFieldViolation extends NotUsedMemberViolation {
  private List assignments;
  
  public NotUsedFieldViolation(BinMember member, boolean safe, List assignments) {
    super(member, safe);
    this.assignments = assignments;
  }
  
  public List getAssignments() {
    return assignments;
  }
}

class NotUsedTypeViolation extends SimpleViolation {

  private boolean isSafe;

  public NotUsedTypeViolation(BinTypeRef ref, boolean isSafe) {
    super(ref, ref.getBinCIType().getVisibilityNode(), "Not used type '"
        + ref.getName() + "'", "refact.audit.notused.type");
    this.isSafe = isSafe;
  }

  public List getCorrectiveActions() {
    List actions = new ArrayList();
    if (isSafe) {
      actions.add(RemoveNotUsedTypeAction.INSTANCE);
    }
    actions.add(CommentNotUsedTypeAction.INSTANCE);
    return actions;
  }

  public boolean canBeDeleted() {
    return isSafe;
  }
}

class RemoveNotUsedTypeAction extends MultiTargetGroupingAction {
  static final RemoveNotUsedTypeAction INSTANCE = new RemoveNotUsedTypeAction();

  public String getKey() {
    return "refactorit.audit.action.not_used.remove_type";
  }

  public String getName() {
    return "Remove unused type";
  }

  public String getMultiTargetName() {
    return "Remove approved unused type(s)";
  }

  public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
    Set sources = new HashSet(violations.size());
    processTypes(manager, violations, sources);
    return sources;
  }

  public void processTypes(TransformationManager manager, List allViolations,
      Set sources) {

    MultiValueMap nuTypeRefs = new MultiValueMap();

    List types = new ArrayList();

    for(int k=0; k< allViolations.size(); k++) {
      RuleViolation v = (RuleViolation) allViolations.get(k);
      if(v instanceof NotUsedTypeViolation) {
        NotUsedTypeViolation violation = (NotUsedTypeViolation)v;
        CompilationUnit cu = violation.getCompilationUnit();
        types.add(violation.getBinTypeRef());
        nuTypeRefs.put(cu, ((NotUsedTypeViolation)v).getBinTypeRef());
      }
    }

    while(!types.isEmpty()) {
      BinTypeRef typeRef = (BinTypeRef) types.get(0);
      CompilationUnit cu = typeRef.getCompilationUnit();

      List nuTypes = nuTypeRefs.get(cu);
      if(nuTypes != null) {
        if(nuTypes.containsAll(cu.getDefinedTypes())) {
          FileEraser fEraser = new FileEraser(cu, true);
          manager.add(fEraser);

          List typesResolved = nuTypeRefs.get(cu);
          if(typesResolved != null) {
            types.removeAll(typesResolved);
          }
          nuTypeRefs.clearKey(cu);
        } else {
            StringEraser eraser = new StringEraser(typeRef.getBinCIType());
            manager.add(eraser);
            types.remove(typeRef);
        }
        sources.add(cu);
      }
      nuTypes.clear();
    }
    nuTypeRefs.clear();
  }
}

/**
 * 
 * @author Oleg Tsernetsov
 *
 * Common class for RemoveNotUsedMemberAction and 
 * CommentNotUsedMemberAction
 */
abstract class NotUsedMemberAction extends MultiTargetGroupingAction {
  public Set run(TransformationManager manager, TreeRefactorItContext context, List violations) {
    Set sources = new HashSet(violations.size());
    MultiValueMap declarationFields = new MultiValueMap();
    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation violation = (RuleViolation) i.next();
      if(!(violation instanceof NotUsedMemberViolation)) {
        continue;
      }
      if(violation instanceof NotUsedFieldViolation) {
        handleAssignments(((NotUsedFieldViolation)violation).getAssignments(), manager);
        BinField field = (BinField)violation.getOwnerMember();
        declarationFields.put(field.getParent(), field);
        sources.add(field.getCompilationUnit());
      } else {
        handleMember(violation.getOwnerMember(), manager);
      } 
    }
    
    for(Iterator it = declarationFields.keySet().iterator(); it.hasNext();) {
      BinFieldDeclaration decl = (BinFieldDeclaration) it.next();
      List fields = declarationFields.get(decl);
      if(decl.getVariables().length > 1) {
        handleDeclaration(decl, fields, manager);
      } else {
        handleMember((BinMember)fields.get(0), manager);
      }
    }
    return sources;
  }
  
  private void handleAssignments(List assignments, 
      TransformationManager manager) {
    if(assignments == null) {
      return;
    }
    
    for(int i=0; i<assignments.size(); i++) {
      BinAssignmentExpression expr = (BinAssignmentExpression)assignments.get(i);
      CompilationUnit compilationUnit = expr.getCompilationUnit();
      BinExpression rightExpr = expr.getRightExpression();
      boolean changes = rightExpr.isChangingAnything();
      
      int sLine = expr.getStartLine();
      int sCol = expr.getStartColumn();
      
      LocationAware la = expr;
      boolean lineBreak = true;
      if(expr.getParent() instanceof BinExpressionStatement) {
        la = ((BinExpressionStatement)expr.getParent());
      }
      int eLine = (changes)? rightExpr.getStartLine(): la.getEndLine();
      int eCol = (changes)? rightExpr.getStartColumn(): la.getEndColumn();
      handleSingleAssignmentBlock(compilationUnit, sLine, sCol, eLine, 
          eCol, manager);
    }
  }
  
  protected abstract void handleDeclaration(BinFieldDeclaration decl, 
      List handled, TransformationManager manager);
  protected abstract void handleMember(BinMember member, TransformationManager manager);
  protected abstract void handleSingleAssignmentBlock(CompilationUnit cu, 
      int sLine, int sCol, int eLine, int eCol, TransformationManager manager);
}


class RemoveNotUsedMemberAction extends NotUsedMemberAction {
  static final RemoveNotUsedMemberAction INSTANCE = new RemoveNotUsedMemberAction();

  public String getKey() {
    return "refactorit.audit.action.not_used.remove_member";
  }

  public String getName() {
    return "Remove unused member";
  }

  public String getMultiTargetName() {
    return "Remove potentially unused member(s)";
  }

  public void handleDeclaration(BinFieldDeclaration decl, List handled, 
      TransformationManager manager) {
    CompilationUnit compilationUnit = decl.getCompilationUnit();
    BinVariable vars[] = decl.getVariables();
    if(vars.length == handled.size()) {
      LocationAware la = decl;
      if(decl.getParent() instanceof BinExpressionStatement) {
        la = ((BinExpressionStatement)decl.getParent());
      }
      StringEraser eraser = new StringEraser(la);
      manager.add(eraser);
    } else {
      DeclarationSplitTransformation splitter = 
        new DeclarationSplitTransformation(compilationUnit, decl, handled);
      manager.add(splitter);
    }
  }
  
  public void handleMember(BinMember member, TransformationManager manager) {
    StringEraser eraser = new StringEraser(member);
    manager.add(eraser);
  }
  
  public void handleSingleAssignmentBlock(CompilationUnit cu, 
      int sLine, int sCol, int eLine, int eCol, TransformationManager manager) {
    StringEraser eraser = new StringEraser(cu, sLine, sCol-1, eLine, eCol-1);
    manager.add(eraser);
  }
}

class CommentNotUsedMemberAction extends NotUsedMemberAction {
  static final CommentNotUsedMemberAction INSTANCE = new CommentNotUsedMemberAction();

  public String getKey() {
    return "refactorit.audit.action.not_used.comment_member";
  }

  public String getName() {
    return "Comment-out unused member";
  }

  public String getMultiTargetName() {
    return "Comment-out potentially unused member(s)";
  }

  public void handleDeclaration(BinFieldDeclaration decl, List handled, 
      TransformationManager manager) {
    CompilationUnit compilationUnit = decl.getCompilationUnit();
    BinVariable vars[] = decl.getVariables();
    if(vars.length == handled.size()) {
      LocationAware la = decl;
      if(decl.getParent() instanceof BinExpressionStatement) {
        la = ((BinExpressionStatement)decl.getParent());
      }
      CommentOutHelper.commentOutBlock(compilationUnit, manager, 
          la.getStartLine(), la.getStartColumn()-1, la.getEndLine(), 
          la.getEndColumn()-1, true, false);
    } else {
      DeclarationSplitTransformation splitter = 
        new DeclarationSplitTransformation(compilationUnit, decl, handled);
      manager.add(splitter);
      commentOutExcludedFields(decl, handled, manager);
    }
  }
  
  public void handleMember(BinMember member, TransformationManager manager) {
    CommentOutHelper.commentOutMember(manager, member);
  }
  
  public void handleSingleAssignmentBlock(CompilationUnit compilationUnit, 
      int sLine, int sCol, int eLine, int eCol, TransformationManager manager) {
    CommentOutHelper.commentOutBlock(compilationUnit, manager, 
        sLine, sCol-1, eLine, eCol-1, false, false);
  }
  
  private void commentOutExcludedFields(BinFieldDeclaration decl, List fields,
      TransformationManager manager) {
    CompilationUnit cu = decl.getCompilationUnit();
    MultiValueMap varComments = CommentAllocator.allocateComments(decl);
    int insertionLine = decl.getStartLine();
    int insertionColumn = decl.getStartColumn() - 1;
    
    manager.add(new StringInserter(cu, insertionLine, insertionColumn, 
        FormatSettings.LINEBREAK + "/*" + FormatSettings.LINEBREAK));
    
    for (int i = 0; i < fields.size(); i++) {
      BinField field = (BinField) fields.get(i);
      int modifier = field.getModifiers();

      String declExpr = ((i == 0) ? "" : FormatSettings.getIndentString(decl.getIndent()));
      declExpr += new BinModifierFormatter(modifier).print() + " ";
      declExpr += BinFormatter.formatWithType(field);
      declExpr += ((field.hasExpression()) ? " = " + field.getExpression().getText() : "");
      declExpr += ";";

      List l = varComments.get(field);
      int commentIndent = DeclarationSplitTransformation.getLineEndIndent(declExpr) + 1
          + ((i == 0) ? insertionColumn + 1: 0);
      if (l != null && l.size() > 0) {
        declExpr = declExpr + " " + 
          CommentAllocator.indentifyComment(
          CommentOutHelper.openCommentTags(((Comment) l.get(0)).getText()),
          commentIndent, false);
      }
      if(i != fields.size() - 1 || (l!=null && l.size()> 1)) {
        declExpr = declExpr + FormatSettings.LINEBREAK;
      }
      manager.add(new StringInserter(cu, insertionLine, insertionColumn,
          declExpr));

      if (l != null && l.size() > 0) {
        for (int k = 1; k < l.size(); k++) {
          Comment c = (Comment) l.get(k);
          String commentStr = CommentAllocator.indentifyComment(
              c.getText(), commentIndent, true);
          if(i != fields.size() - 1 || k!=l.size()-1) {
            commentStr += FormatSettings.LINEBREAK;
          }
          commentStr = CommentOutHelper.openCommentTags(commentStr);
          manager.add(new StringInserter(cu, insertionLine,
              insertionColumn, commentStr));
        }
      }
    }
    manager.add(new StringInserter(cu, insertionLine, insertionColumn, 
        FormatSettings.LINEBREAK + "*/"));
  }
}

class CommentNotUsedTypeAction extends MultiTargetGroupingAction {
  static final CommentNotUsedTypeAction INSTANCE = new CommentNotUsedTypeAction();

  public String getKey() {
    return "refactorit.audit.action.not_used.comment_type";
  }

  public String getName() {
    return "Comment-out unused type";
  }

  public String getMultiTargetName() {
    return "Comment-out potentially unused type(s)";
  }

  public Set run(TransformationManager manager,
      TreeRefactorItContext context, List violations) {
    Set sources = new HashSet();
    Set upperTypes = new HashSet();
    
    for(int i=0; i<violations.size(); i++) {
      RuleViolation violation = (RuleViolation)violations.get(i);
      if(! (violation instanceof NotUsedTypeViolation)) {
        continue;
      }
      NotUsedTypeViolation nuViolation = (NotUsedTypeViolation) violation;
      BinCIType ciType = nuViolation.getBinTypeRef().getBinCIType();
      
      boolean isContained = false;
      for(Iterator it = upperTypes.iterator(); it.hasNext() && !isContained;) {
        BinCIType tmpType = (BinCIType) it.next();
        if(tmpType.contains((LocationAware)ciType)) {
          isContained = true;
        } else if(ciType.contains((LocationAware)tmpType)) {
          it.remove();
        }
      }
      if(!isContained) {
        upperTypes.add(ciType);
      }
    }
    
    
    for(Iterator it = upperTypes.iterator(); it.hasNext();) {
      BinCIType ciType = (BinCIType) it.next();
      sources.add(ciType.getCompilationUnit());
      CommentOutHelper.commentOutMember(manager, ciType);
    }
    return sources;
  }
}
