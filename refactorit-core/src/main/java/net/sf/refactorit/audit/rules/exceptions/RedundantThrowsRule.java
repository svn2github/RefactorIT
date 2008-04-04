/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.exceptions;



import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.DelegateVisitor;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.CommentAllocator;
import net.sf.refactorit.utils.CommentOutHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Villu Ruusmann
 */
public class RedundantThrowsRule extends AuditRule {
  public static final String NAME = "redundant_throws";

  private boolean includeRuntime = false;

  private boolean includeError = false;

  /* Cache */
  private BinTypeRef runtimeRef;

  private BinTypeRef errorRef;

  private List methods = new ArrayList();

  public void init() {
    this.includeRuntime = AuditProfileUtils.getBooleanOption(
        getConfiguration(), "skip", "include_runtime", false);
    this.includeError = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "include_error", false);
    super.init();
  }

  public void postProcess() {
    while (!methods.isEmpty()) {
      BinMethod method = (BinMethod) methods.get(0);

      List hierarchy = method.findAllOverridesOverriddenInHierarchy();
      hierarchy.add(method);
      List requiredThrows = RedundantSearchHelper.getRequiredThrows(method,
          hierarchy);

      // process each method in hierarchy, check for redundant throws
      for (int i = 0; i < hierarchy.size(); i++) {
        BinMethod tmpMeth = (BinMethod) hierarchy.get(i);
        if (tmpMeth.isSynthetic() || tmpMeth.getCompilationUnit() == null) {
          continue;
        }
        BinMethod.Throws[] throwsClause = tmpMeth.getThrows();
        Map throwsUsage = new HashMap();
        for (int j = 0; j < throwsClause.length; j++) {
          BinTypeRef throwableRef = throwsClause[j].getException();
          if (throwsClause[j].getRootAst() != null) {
            throwsUsage.put(throwableRef, throwsClause[j]);
          }
        }

        // find all redundants including Error and Runtime hierarchies for not
        // to check twice
        for (int j = 0; j < requiredThrows.size(); j++) {
          BinTypeRef required = (BinTypeRef) requiredThrows.get(j);
          if (throwsUsage.keySet().contains(required)) {
            throwsUsage.put(required, null);
            continue;
          }
          for (Iterator iter = throwsUsage.keySet().iterator(); iter.hasNext();) {
            BinTypeRef thrown = (BinTypeRef) iter.next();
            if (required.isDerivedFrom(thrown)) {
              BinMethod.Throws violThrow = (BinMethod.Throws) throwsUsage
                  .get(thrown);
              if (violThrow != null) {
                throwsUsage.put(thrown, null);
              }
            }
          }
        }
        for (Iterator it = throwsUsage.keySet().iterator(); it.hasNext();) {
          BinMethod.Throws violThrow = (BinMethod.Throws) throwsUsage.get(it
              .next());
          if (violThrow != null
              && (includeRuntime || !violThrow.getException().isDerivedFrom(
                  getRuntimeRef()))
              && (includeError || !violThrow.getException().isDerivedFrom(
                  getErrorRef()))) {
            addViolation(new RedundantThrows(violThrow));
          }
        }
      }
      methods.removeAll(hierarchy);
      requiredThrows.clear();
    }
    methods.clear();
  }

  public void visit(BinMethod method) {
    if (!method.isSynthetic()) {
      // damn references! dirty hack
      getRuntimeRef();
      getErrorRef();
      if (method.getThrows().length > 0) {
        methods.add(method);
      }
    }
    super.visit(method);
  }

  public void visit(BinConstructor constructor) {
    visit((BinMethod) constructor);
  }

  private BinTypeRef getRuntimeRef() {
    if (this.runtimeRef == null) {
      this.runtimeRef = getBinTypeRef("java.lang.RuntimeException");
    }
    return this.runtimeRef;
  }

  private BinTypeRef getErrorRef() {
    if (this.errorRef == null) {
      this.errorRef = getBinTypeRef("java.lang.Error");
    }
    return this.errorRef;
  }
}

class RedundantThrows extends AwkwardSourceConstruct {
  private BinMethod.Throws throwable;

  // private BinTypeRef required;

  RedundantThrows(BinMethod.Throws throwable) {
    super(throwable, "Redundant throwable: "
        + throwable.getException().getQualifiedName(),
        "refact.audit.redundant_throws");
    this.throwable = throwable;
  }

  public BinMethod.Throws getThrowable() {
    return throwable;
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveRedundantThrows.INSTANCE);
  }
}

class RemoveRedundantThrows extends MultiTargetGroupingAction {
  static final RemoveRedundantThrows INSTANCE = new RemoveRedundantThrows();

  public String getKey() {
    return "refactorit.audit.action.redundant_throws.remove_throws";
  }

  public String getName() {
    return "Remove redundant throw";
  }

  public String getMultiTargetName() {
    return "Remove redundant throws";
  }

  public Set run(TransformationManager manager, TreeRefactorItContext context,
      List violations) {
    HashSet sources = new HashSet();

    MultiValueMap methodViolations = new MultiValueMap();
    for (int i = 0; i < violations.size(); i++) {
      RuleViolation violation = (RuleViolation) violations.get(i);
      if (!(violation instanceof RedundantThrows)) {
        continue;
      }
      RedundantThrows rtViolation = (RedundantThrows) violation;
      BinMethod.Throws throwable = rtViolation.getThrowable();
      BinMethod meth = (BinMethod) throwable.getParentMember();
      methodViolations.put(meth, rtViolation);
    }

    sources.addAll(performChanges(methodViolations, manager));

    return sources;
  }

  private Set performChanges(MultiValueMap methodViolations,
      TransformationManager manager) {
    HashSet sources = new HashSet();
    MultiValueMap performed = new MultiValueMap();

    while (!methodViolations.isEmpty()) {
      BinMethod meth = (BinMethod) methodViolations.keySet().iterator().next();

      Set redundantThrows = prepareRemovedSet(methodViolations.get(meth));

      // we should check and change all methods hierarchy, if trhows are changed
      List hierarchy = meth.findAllOverridesOverriddenInHierarchy();
      hierarchy.add(meth);

      List requiredThrows = RedundantSearchHelper.getRequiredThrows(meth,
          hierarchy);

      for (Iterator it = hierarchy.iterator(); it.hasNext();) {
        BinMethod tmpMeth = (BinMethod) it.next();

        if (tmpMeth.getCompilationUnit() != null) {
          List allThrows = RedundantSearchHelper.getThrownTypeList(tmpMeth
              .getThrows());
          List substs = prepareSubstitutedSet(tmpMeth, requiredThrows);

          boolean performChanges = smartRemove(allThrows, redundantThrows);//allThrows.removeAll(redundantThrows);
          performChanges |= addNew(allThrows, substs); //allThrows.addAll(substs);

          // allThrows contains new throw clause now
          if (performChanges) {
            addMethodEditors(tmpMeth, manager, substs, allThrows);
            sources.add(tmpMeth.getCompilationUnit());
            performed.putAll(tmpMeth, redundantThrows);
          }
        } else {
          it.remove();
        }
      }
      methodViolations.keySet().removeAll(hierarchy);
    }

    addTryCatchEditors(sources, manager, performed);
    return sources;
  }
  
  private static boolean smartRemove(List allThrows, Set redundantThrows) {
    boolean result = false;
    for(Iterator i = allThrows.iterator(); i.hasNext();) {
      BinTypeRef thrownType = (BinTypeRef)i.next();
      if(redundantThrows.contains(thrownType)) {
        i.remove();
        result |= true;
        continue;
      }
      for(Iterator i2 = redundantThrows.iterator(); i2.hasNext();) {
        BinTypeRef redundantThrownType = (BinTypeRef)i2.next();
        if(thrownType.isDerivedFrom(redundantThrownType)) {
          i.remove();
          result |= true;
          break;
        }
      }
    }
    return result;
  }
  
  private static boolean addNew(List to, List from) {
    boolean result = false;
    for(int i = 0; i < from.size(); i++) {
      Object o = from.get(i);
      if(!to.contains(o)) {
        to.add(o);
        result |= true;
      }
    }
    return result;
  }

  private void addMethodEditors(BinMethod currMeth,
      TransformationManager manager, List substs, List allThrows) {
    CompilationUnit cu = currMeth.getCompilationUnit();
    BinMethod.Throws methThrows[] = currMeth.getThrows();

    // erase previous throws clause
    SourceCoordinate throwsCoord = currMeth.getThrowsCoordinate();
    int sLine = throwsCoord.getLine();
    int sCol = throwsCoord.getColumn();

    int eLine = methThrows[methThrows.length - 1].getEndLine();
    int eCol = methThrows[methThrows.length - 1].getEndColumn();

    StringEraser eraser = new StringEraser(cu, sLine, sCol - 1, eLine, eCol - 1);
    manager.add(eraser);

    // hacky way to avoid extra space after all throw clause is erased
    SourceCoordinate bracket = currMeth.getParamsClosingBracket();
    manager.add(new StringEraser(cu, bracket.getLine(),
        bracket.getColumn() - 1, sLine, sCol - 1));
    manager.add(new StringInserter(cu, bracket.getLine(),
        bracket.getColumn() - 1, ")"));

    // insert throws if remain
    if (!allThrows.isEmpty()) {
      BinMethod.Throws reqThrows[] = RedundantSearchHelper
          .getThrowsArray(allThrows);
      String throwsString = new BinMethodFormatter(currMeth)
          .getHeaderThrowsString(reqThrows, sCol);
      StringInserter inserter = new StringInserter(cu, sLine, sCol - 1,
          throwsString);
      manager.add(inserter);
    }

    // save throw clause comments
    List comments = Comment.getCommentsIn(cu, sLine, sCol - 1, eLine, eCol);
    if (!comments.isEmpty()) {
      StringInserter inserter = new StringInserter(currMeth
          .getCompilationUnit(), sLine, sCol - 1, FormatSettings.LINEBREAK);
      manager.add(inserter);
      int indent = currMeth.getIndent()
          + FormatSettings.getContinuationIndent();

      for (int i = 0; i < comments.size(); i++) {
        Comment c = (Comment) comments.get(i);
        String commentText = CommentAllocator.indentifyComment(c.getText(),
            indent, true);
        inserter = new StringInserter(currMeth.getCompilationUnit(), sLine,
            sCol - 1, commentText + FormatSettings.LINEBREAK);
        manager.add(inserter);
      }
    }
  }

  private Set prepareRemovedSet(List violations) {
    if (violations == null) {
      return Collections.EMPTY_SET;
    }
    Set removed = new HashSet();
    for (int i = 0; i < violations.size(); i++) {
      RedundantThrows rtViolation = (RedundantThrows) violations.get(i);
      removed.add(rtViolation.getThrowable().getException());
    }
    return removed;
  }

  private List prepareSubstitutedSet(BinMethod meth, List requiredThrows) {
    if (meth == null || requiredThrows == null) {
      return Collections.EMPTY_LIST;
    }
    List result = new ArrayList();
    List existing = RedundantSearchHelper.getThrownTypeList(meth.getThrows());
    for (int i=0; i < requiredThrows.size(); i++) {
      BinTypeRef ref = (BinTypeRef) requiredThrows.get(i);
      for (Iterator iter = existing.iterator(); iter.hasNext();) {
        BinTypeRef tmp = (BinTypeRef) iter.next();
        if (ref.isDerivedFrom(tmp)) {
          result.add(ref);
        }
      }
    }
    return result;
  }

  private void addTryCatchEditors(Set sources, TransformationManager manager,
      MultiValueMap performed) {
    if (!performed.isEmpty()) {
      TryClauseIndexer indexer = new TryClauseIndexer(performed);
      final Project p = ((BinMethod) performed.keySet().iterator().next())
          .getProject();
      final DelegatingVisitor supervisor = new DelegatingVisitor(true);
      supervisor.setDelegates(new DelegateVisitor[] { indexer });
      Runnable task = new Runnable() {
        public void run() {
          supervisor.visit(p);
        }
      };

      try {
        JProgressDialog.run(IDEController.getInstance().createProjectContext(),
            task, false);
      } catch (SearchingInterruptedException e) {
        // can not be interrupted, but still...
        sources.clear();
        return;
      }

      // find in what try blocks violated methods are invoked
      MultiValueMap tryBlockMethods = indexer.getTryBlockMethods();
      MultiValueMap alienThrows = indexer.getAlienThrows();
      for (Iterator it = tryBlockMethods.keySet().iterator(); it.hasNext();) {
        BinTryStatement.TryBlock tryBlock = (BinTryStatement.TryBlock) it
            .next();
        BinTryStatement tryStatement = (BinTryStatement) tryBlock.getParent();
        CompilationUnit cu = tryStatement.getCompilationUnit();

        BinTryStatement.CatchClause catches[] = tryStatement.getCatches();
        if (alienThrows.get(tryBlock) == null) {

          // comment out all redundant catches
          boolean commentTry = true;
          for (int i = 0; i < catches.length; i++) {
            if (!isRuntimeRef(catches[i].getParameter().getTypeRef())) {
              CommentOutHelper.commentOutLa(manager, catches[i]);
            } else {
              commentTry = false;
            }
          }

          // if no cathes remain uncommented - comment out try block braces
          if (tryStatement.getFinally() == null && commentTry) {
            BinStatementList stList = tryBlock.getStatementList();
            CommentOutHelper.commentOutBlock(cu, manager, tryStatement
                .getStartLine(), tryStatement.getStartColumn() - 1, stList
                .getStartLine(), stList.getStartColumn(), false, false);
            CommentOutHelper.commentOutBlock(cu, manager, stList.getEndLine(),
                stList.getEndColumn() - 2, stList.getEndLine(), stList
                    .getEndColumn() - 1, false, false);
          }
        } else {
          List aliens = alienThrows.get(tryBlock);

          // set representation to avoid duplicates
          Set alienTypes = new HashSet();
          for (int j = 0; j < aliens.size(); j++) {
            alienTypes.add(aliens.get(j));
          }
          Set catchTypes = new HashSet(Arrays.asList(catches));

          // exclude catches occupied by alien throws
          for (Iterator iter = catchTypes.iterator(); iter.hasNext();) {
            BinTryStatement.CatchClause clause = (BinTryStatement.CatchClause) iter
                .next();
            BinTypeRef param = clause.getParameter().getTypeRef();
            if (alienTypes.contains(param) || isRuntimeRef(param)) {
              iter.remove();
            } else {
              for (Iterator itr = alienTypes.iterator(); itr.hasNext();) {
                BinTypeRef tmpRef = (BinTypeRef) itr.next();
                if (tmpRef.isDerivedFrom(param)) {
                  iter.remove();
                  break;
                }
              }
            }
          }

          // comment out remaining i.e. redundant catches
          for (Iterator iter = catchTypes.iterator(); iter.hasNext();) {
            BinTryStatement.CatchClause clause = (BinTryStatement.CatchClause) iter
                .next();
            CommentOutHelper.commentOutLa(manager, clause);
          }
        }
      }
    }
  }

  private boolean isRuntimeRef(BinTypeRef ref) {
    if (ref == null) {
      return false;
    }
    Project p = ref.getProject();
    return ref.isDerivedFrom(p.getTypeRefForName("java.lang.RuntimeException"))
        || ref.equals(p.getTypeRefForName("java.lang.exception"));
  }
}
