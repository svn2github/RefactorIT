/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.inlinemethod;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.EmptyLine;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.common.InnersToQualifySeeker;
import net.sf.refactorit.refactorings.extract.RenamingVariableUseAnalyzer;
import net.sf.refactorit.refactorings.extract.VariableUseAnalyzer;
import net.sf.refactorit.refactorings.extract.VariableUseAnalyzer.VarInfo;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.MemberEraser;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.MoveEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.BinStatementListFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;

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
 * @author Anton Safonov
 */
public class InlineMethod extends AbstractRefactoring {
  public static String key = "refactoring.inlinemethod";

  public static int DELETE_METHOD_DECLARATION = 0;
  public static int COMMENT_METHOD_DECLARATION = 1;
  public static int LEAVE_METHOD_DECLARATION = 2;

  BinMethod method;

  private BinItem target;
  private List invocations;

  private RenamingVariableUseAnalyzer methodVarAnalyzer;

  private List extrasToRemove = new ArrayList();

  private ImportManager importManager = new ImportManager();

  private Map changeAccessMap = new HashMap();

  private int methodDeclarationAction = 0;


  public InlineMethod(RefactorItContext context, BinItem target) {
    super("Inline Method", context);

    this.target = target;

    if (this.target instanceof BinMethodInvocationExpression) {
      this.method = ((BinMethodInvocationExpression) this.target).getMethod();
    } else if (this.target instanceof BinMethod) {
      this.method = (BinMethod) this.target;
    } else if (Assert.enabled) {
      Assert.must(false, "Unexpected target: " + this.target);
    }

    this.invocations = Finder.getInvocations(this.method);

    // inline just one invocation
    if (this.target instanceof BinMethodInvocationExpression) {
      this.invocations = new ArrayList(this.invocations);
      for (Iterator it = this.invocations.iterator(); it.hasNext();) {
        InvocationData data = (InvocationData) it.next();
        if (data.getInConstruct() != this.target) {
          it.remove();
          methodDeclarationAction = LEAVE_METHOD_DECLARATION;
        }

      }
    }


    BinStatement[] stats;
    if (this.method.getBody() != null) { // binary methods doesn't have body
      stats = this.method.getBody().getStatements();
    } else {
      stats = BinStatement.NO_STATEMENTS;
    }

    this.methodVarAnalyzer = new RenamingVariableUseAnalyzer(getContext(),
            this.method, Arrays.asList(stats));
  }


  public RefactoringStatus checkPreconditions() {
    final RefactoringStatus status = new RefactoringStatus();

    if (!this.method.getOwner().getBinCIType().isFromCompilationUnit()) {
      status.addEntry("Can not inline method having no source",
              RefactoringStatus.ERROR);
    }

    boolean checkOverrides = true;
    if (this.target instanceof BinMethodInvocationExpression
            && ((BinMethodInvocationExpression) this.target).getExpression() instanceof BinLiteralExpression
            && ((BinLiteralExpression) ((BinMethodInvocationExpression) this.target)
                    .getExpression()).isSuper()) {
      checkOverrides = false;
    }

    if (checkOverrides
            && (this.method.findOverrides().size() > 0 || this.method
                    .getOwner().getBinCIType().getSubMethods(this.method)
                    .size() > 0)) {
      status
              .addEntry(
                      "Can not inline overriden or overriding method in all places.\n"
                              + "However, single super invocations can be still inlined by clicking directly on them.",
                      RefactoringStatus.ERROR);
    }

    if (!usedOnlyInReturn()) {
    	status.merge(checkReturnPoints());
    }
    if (usedInDirectReturn()) {
    	status.merge(checkLastReturnStatement());
    }

    status.merge(checkAccess());

    if ((methodDeclarationAction == DELETE_METHOD_DECLARATION)
        && (isRecursiveMethod(method))) {
      methodDeclarationAction = LEAVE_METHOD_DECLARATION;
    }


    return status;
  }

  /**
   * @return <code>true</code> is method is recursive, i.e. includes
   * calls to itself
   */
  private static boolean isRecursiveMethod(final BinMethod method) {
    final List recursiveCalls = new ArrayList(2);
		SinglePointVisitor visitor = new SinglePointVisitor() {
			public void onEnter(Object o) {
			  if (o instanceof BinMethodInvocationExpression) {
			    if (((BinMethodInvocationExpression)o).getMethod() == method) {
			      recursiveCalls.add(o);
			    }
			  }
			}

			public void onLeave(Object o) {
			}
		};
		visitor.visit(method);
		return recursiveCalls.size() > 0;
  }


//  /**
//   * Asks user whether to delete method and sets <code>doDeleteMethod</code>
//   * accordingly.
//   *
//   * @return
//   */
//  private RefactoringStatus checkDeleteMethod() {
//    final RefactoringStatus status = new RefactoringStatus();
//    String text = "Delete method after inlining all invocations?";
//    final int res = DialogManager.getInstance().showYesNoCancelQuestion(
//        getContext(), "question.inline.delete_method",
//        text, DialogManager.NO_BUTTON);
//    if (res == DialogManager.CANCEL_BUTTON) {
//      return new RefactoringStatus("", RefactoringStatus.CANCEL);
//    }
//    if (res == DialogManager.NO_BUTTON) {
//      doDeleteMethod = false;
//    }
//    return status;
//  }


  /**
   * Checks whether all classes and members are accessible in inlined
   * places. If not, asks whether to automatically broaden access.
   *
   * @return
   */
	private RefactoringStatus checkAccess() {
		final RefactoringStatus status = new RefactoringStatus();
		// collect all members used in method
		final Set usedMembers = new HashSet();
		final Set usedTypeRefs = new HashSet();
		final AbstractIndexer methodVisitor = new AbstractIndexer() {
			public void visit(BinMethodInvocationExpression x) {
				usedMembers.add(x.getMethod());
				super.visit(x);
			}

			public void visit(BinFieldInvocationExpression x) {
				usedMembers.add(x.getField());
				super.visit(x);
			}

			public void visit(BinCastExpression x) {
				usedTypeRefs.add(x.getReturnType());
				super.visit(x);
			}

			public void visit(BinCITypeExpression x) {
				usedTypeRefs.add(x.getReturnType());
				super.visit(x);
			}

			public void visit(BinNewExpression x) {
				BinMember constructor = x.getConstructor();
				if (constructor != null) {
					usedMembers.add(constructor);
				}
				super.visit(x);
			}

			public void visit(BinLocalVariable x) {
				BinTypeRef type = x.getTypeRef().getNonArrayType();
				if (type.isReferenceType()) {
					usedTypeRefs.add(x.getTypeRef());
				}
				super.visit(x);
			}
		};
		methodVisitor.visit(method);
		for (Iterator iterator = usedTypeRefs.iterator(); iterator.hasNext();) {
			BinTypeRef typeRef = (BinTypeRef) iterator.next();
			if (typeRef == null || typeRef.getBinCIType() == null) { // bug 2425?
			  continue; // bug somewhere else, let's just skip such class
			}
			int possibleAccesses[] = MinimizeAccessUtil.findAccessRights(typeRef.getBinCIType(), invocations);
			int newAccess = possibleAccesses[possibleAccesses.length - 1];
			int currentAccess = typeRef.getBinCIType().getAccessModifier();
	    if (BinModifier.compareAccesses(newAccess, currentAccess) == 1) {
	    	changeAccessMap.put(typeRef.getBinCIType(), new Integer(newAccess));
	    }
		}

		for (Iterator iterator = usedMembers.iterator(); iterator.hasNext();) {
			BinMember member = (BinMember) iterator.next();
      int newAccess = MinimizeAccessUtil.getNewAccessForMember(
          member, member.getOwner().getBinCIType(), invocations);
	    if (BinModifier.compareAccesses(newAccess, member.getAccessModifier()) == 1) {
	    	changeAccessMap.put(member, new Integer(newAccess));
	    }
		}

	  if (changeAccessMap.size() > 0) {
	  	String text = "Inlined method uses some members or classes that wouldn't be accessible in places where the method is invoked. " +
	  			"Do you want to automatically widen access of such members and classes?";
      final int res = DialogManager.getInstance().showYesNoCancelQuestion(
          getContext(), "question.inline.change_access",
          text, DialogManager.YES_BUTTON);
      if (res == DialogManager.CANCEL_BUTTON) {
        return new RefactoringStatus("", RefactoringStatus.CANCEL);
      }
      if (res == DialogManager.NO_BUTTON) {
      	changeAccessMap.clear();
      }
	  }

		return status;
	}

	/**
   * @return
   */
  private RefactoringStatus checkLastReturnStatement() {
  	final RefactoringStatus status = new RefactoringStatus();
		final BinStatement last = method.getBody().getStatements()[method.getBody().getStatements().length -1];
		if (!(last instanceof BinReturnStatement)) {
			status.addEntry("Method is used in return statement and must return a value in a last statemnt  to be inlinable!", RefactoringStatus.ERROR);
		}
  	return status;
  }

  /**
   * @return
   */
  private boolean usedInDirectReturn() {
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		SourceConstruct inConstruct = invocation.getInConstruct();
  		if ((inConstruct.getParent() instanceof BinReturnStatement)) {
  			return true;
  		}
  	}
  	return false;
  }


  /**
   * @return
   */
  private boolean usedOnlyInReturn() {
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		SourceConstruct inConstruct = invocation.getInConstruct();
  		if (!(inConstruct.getParent() instanceof BinReturnStatement)) {
  			return false;
  		}
  	}

  	return true;
  }

  /**
   * Returns status with <code>RefactoringStatus.ERROR</code> if method is not inlinable because
   * it contains other returns than the last statement.
   * @return
   */
  private RefactoringStatus checkReturnPoints() {
  	final RefactoringStatus status = new RefactoringStatus();

  	if (method.getBody().getStatements().length > 0) {
  		final List deepReturns = new ArrayList(3);
  		final BinStatement last = method.getBody().getStatements()[method.getBody().getStatements().length -1];
  		SinglePointVisitor visitor = new SinglePointVisitor() {
  			int inner = 0;
  			public void onEnter(Object o) {
  				if (o instanceof BinCITypesDefStatement) {
  					inner++;
  				} else if (o instanceof BinReturnStatement) {
  					if ((inner == 0) && (o != last)) {
  						deepReturns.add(o);
  					}
  				}
  			}

  			public void onLeave(Object o) {
  				if (o instanceof BinCITypesDefStatement) {
  					inner--;
  				}
  			}
  		};
  		visitor.visit(method);
  		if (deepReturns.size() > 0) {
  			status.addEntry("Method is too complex to be inlined because it contains return statements from nested statement blocks!", RefactoringStatus.ERROR);
  		}
  	}
  	return status;
  }

  public RefactoringStatus checkUserInput() {
    final RefactoringStatus status = new RefactoringStatus();

    return status;
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    //createEraseEditorsForExtras(transList); // this is for tests
    final List stmtsToInline = new ArrayList(Arrays.asList(this.method
            .getBody().getStatements()));
    stmtsToInline.addAll(getSingleComments(stmtsToInline, Comment
            .getCommentsIn(this.method)));
    Collections.sort(stmtsToInline, LocationAware.PositionSorter.getInstance());
//
//    for (int i = stmtsToInline.size() - 1; i >= 0; i--) {
//      if (!(stmtsToInline.get(i) instanceof BinStatement)) {
//        stmtsToInline.remove(i);
//      }
//    }

    removeEmptyReturn(stmtsToInline);

    final Iterator invocationsIt = invocations.iterator();
    while (invocationsIt.hasNext()) {
      final InvocationData data = (InvocationData) invocationsIt.next();
      final BinMethodInvocationExpression where = (BinMethodInvocationExpression) data
              .getInConstruct();
      final BinMember whereMember = data.getWhereMember();

      BinSourceConstruct last = handleLastStatementSeparately(stmtsToInline,
              where, whereMember);

      //eraseInvocationExpression(transList, where, last);


      final int indent = where.getIndent();

      final BinStatement enclosingStatement = where.getEnclosingStatement();

      int varsCreated = transList.getTransformationsCount();
      final Map toRename = generateVarsForParams(transList, where);
      toRename.putAll(getVarsToRename(where));
      toRename.putAll(getLocalTypesToRename(where));
      Map renameMap = generateRenameMap(where, toRename);
      renameMap.putAll(getThisOrSuperToRename(transList, where));
      varsCreated = transList.getTransformationsCount() - varsCreated;
      renameMap.putAll(getInnersToQualify(where));

      int finalRowsCount = stmtsToInline.size() + varsCreated;
      if (last != null && analyzeLastStatementRedundancy(where, last) == null) {
        --finalRowsCount;
      }

      boolean addCurlyBrackets = false;
      boolean addSemicolon = false;

      if (enclosingStatement.getParent() instanceof BinStatementList
              && !"{"
                      .equals(((BinStatementList) enclosingStatement
                              .getParent()).getRootAst().getText())
              && !(enclosingStatement.getParent().getParent() instanceof BinStatementList)
              && !(enclosingStatement.getParent().getParent() // not sure about
              // this one
              instanceof BinSwitchStatement.Case)
              && !(enclosingStatement.getParent().getParent() // not sure about
              // this one
              instanceof BinSwitchStatement.CaseGroup)
              && !(enclosingStatement.getParent().getParent() // not sure about
              // this one
              instanceof BinSwitchStatement)) {
        if (finalRowsCount > 1) {
          addCurlyBrackets = true;
        } else if (finalRowsCount <= 0) {
          addSemicolon = true;
        }
      }

      if (addCurlyBrackets || addSemicolon) {
        BinStatementListFormatter formatter = (BinStatementListFormatter) ((BinItem) enclosingStatement
                .getParent()).getFormatter();
        ASTImpl openingBrace = formatter.getOpeningBrace();
        SourceCoordinate start = SourceCoordinate.getForStart(openingBrace);
        start.setColumn(start.getColumn() - 1);
        transList.add(new StringInserter(data.getCompilationUnit(), start,
                addSemicolon ? ";" : openingBrace.getText()));
      }

      // FIXME probably adds too much, e.g. return or param types sometimes
      // might be not needed after inline, i.e. implicitly used
      importManager.addImportsForMember(this.method, where.getParentMember()
              .getOwner(), false);

      if ((last == null && stmtsToInline.size() > 0)
              || stmtsToInline.size() > 1) {
        final SourceCoordinate insertAt = new SourceCoordinate(
                enclosingStatement.getStartLine(), 0);
        final List subList = stmtsToInline.subList(0, last == null
                ? stmtsToInline.size()
                : stmtsToInline.size() - 1);
        addEmptyLines(subList);
        final MoveEditor copier = new MoveEditor(subList, data
                .getCompilationUnit(), insertAt, indent);
        copier.setCopyOnly(true);
        copier.setUseOriginalContent(true);
        copier.setRenameMap(renameMap);
        copier.setAlignFirstLine(true);
        transList.add(copier);
      }



      if (last != null) {
        final SourceCoordinate insertAt2 = new SourceCoordinate(where
                .getStartLine(), where.getStartColumn() - 1);
        last = analyzeLastStatementRedundancy(where, last);
        if (last != null) {
          boolean addBrackets = last instanceof BinExpression
                  && ((BinExpression) last).isNeedBrackets(where.getParent());
          if (addBrackets) {
            transList.add(new StringInserter(data.getCompilationUnit(),
                    insertAt2, "("));
          }
          if (isMustCast(where, last)) {
            transList.add(new StringInserter(data.getCompilationUnit(),
                    insertAt2, "("
                            + BinFormatter.formatNotQualified(where
                                    .getReturnType()) + ") "));
          } else if ((last instanceof BinExpression && (!(last instanceof BinMethodInvocationExpression)))
          		&& ((where.getParent() instanceof BinExpressionStatement)
          				&& (where.getParent().getParent() instanceof BinStatementList))) {
          	BinExpression expr = (BinExpression) last;
          	String newVarName = generateTempVarName(where);
            transList.add(new StringInserter(data.getCompilationUnit(),
                insertAt2, BinFormatter.formatNotQualified(expr.getReturnType()) + " " + newVarName + " = " ));
          } else if (where.getParent() instanceof BinExpressionStatement
              && !(last instanceof BinStatement)) {
            transList.add(new StringInserter(data.getCompilationUnit(),
                    insertAt2, FormatSettings.getIndentString(indent)));
          }
          // FIXME looses empty lines between previous and last statements

          final MoveEditor inserter = new MoveEditor(CollectionUtil
                  .singletonArrayList(last), data.getCompilationUnit(),
                  insertAt2, indent);
          inserter.setCopyOnly(true);
          inserter.setUseOriginalContent(true);
          inserter.setRenameMap(renameMap);
          inserter.setAlignFirstLine(false);
          transList.add(inserter);

          if (addBrackets) {

            transList.add(new StringInserter(data.getCompilationUnit(),
                    insertAt2, ")"));

          }
        }
      }

      if (addCurlyBrackets) {
        BinStatementListFormatter formatter = (BinStatementListFormatter) ((BinItem) enclosingStatement
                .getParent()).getFormatter();
        ASTImpl closingBrace = formatter.getClosingBrace();
        SourceCoordinate end = SourceCoordinate.getForStart(closingBrace);
        end.setColumn(end.getColumn() - 1);
        transList.add(new StringInserter(data.getCompilationUnit(), end,
                closingBrace.getText()));
      }
      eraseInvocationExpression(transList, where, last);
    }
    importManager.createEditors(transList);
    createAccessModifierEditors(transList);
    if (methodDeclarationAction == DELETE_METHOD_DECLARATION) {
      createDeleteMethodEditor(transList);
    } else if (methodDeclarationAction == COMMENT_METHOD_DECLARATION) {
      createCommentMethodEditor(transList);
    }
    return transList;
  }

  /**
   * @param transList
   */
  private void createCommentMethodEditor(TransformationList transList) {
    for (int i = method.getRootAst().getStartLine(); i <= method.getRootAst().getEndLine(); i++) {
      StringInserter commentInserter = new StringInserter(method.getCompilationUnit(), i, 0, "//");
      transList.add(commentInserter);
    }
  }


  /**
   * @param transList
   */
  private void createDeleteMethodEditor(TransformationList transList) {
    transList.add(new MemberEraser(method));
  }

  /**
   * @param transList
   */
  private void createAccessModifierEditors(TransformationList transList) {
  	for (Iterator iter = changeAccessMap.entrySet().iterator(); iter.hasNext();) {
  		Map.Entry entry = (Map.Entry) iter.next();
  		BinMember member = (BinMember)entry.getKey();
  		int newAccessModifier = ((Integer) entry.getValue()).intValue();
  		transList.add(new ModifierEditor(member, BinModifier.setFlags(member.getModifiers(), newAccessModifier)));
  	}

  }

  /**
   * @param where
   * @return
   */
	private String generateTempVarName(final BinMethodInvocationExpression where) {
		String newVarName = "temp";
		int count = 1;
		while (varNameHasConflicts(where, newVarName)) {
			newVarName = "temp" + count++;
		}
		return newVarName;
	}

	private boolean isMustCast(final BinMethodInvocationExpression where,
          final BinSourceConstruct insertable) {
    boolean cast = false;
    if (insertable instanceof BinExpression
            && !((BinExpression) insertable).getReturnType().equals(
                    where.getReturnType())
            && where.getParent() instanceof BinExpressionList) {
      BinExpression invocation = (BinExpression) where.getParent().getParent();

      if (invocation instanceof BinMethodInvocationExpression) {
        BinTypeRef[] paramTypes = BinParameter
                .parameterTypes(((BinMethodInvocationExpression) invocation)
                        .getMethod().getParameters());
        int index = ((BinExpressionList) where.getParent())
                .getExpressionIndex(where);
        paramTypes[index] = ((BinExpression) insertable).getReturnType();

        BinMethod method = MethodInvocationRules.getMethodDeclaration(
                invocation.getOwner().getBinCIType(),
                ((BinMethodInvocationExpression) invocation).getInvokedOn(),
                ((BinMethodInvocationExpression) invocation).getMethod()
                        .getName(), paramTypes);

        // resolution changed?
        if (method != ((BinMethodInvocationExpression) invocation).getMethod()) {
          cast = true;
        }
      } else if (invocation instanceof BinNewExpression) {
        BinConstructor constr = ((BinNewExpression) invocation).getConstructor();
        if (constr != null) {
          BinTypeRef[] paramTypes = BinParameter.parameterTypes(constr
              .getParameters());
          int index = ((BinExpressionList) where.getParent())
              .getExpressionIndex(where);
          paramTypes[index] = ((BinExpression) insertable).getReturnType();

          BinConstructor newConstr = ((BinClass) constr.getOwner()
              .getBinCIType()).getAccessibleConstructor(where.getOwner().getBinCIType(), paramTypes);

          if (newConstr != constr) {
            cast = true;
          }
        }
      }

      // TODO more not allowed conversions?
    }

    return cast;
  }

  private BinSourceConstruct analyzeLastStatementRedundancy(
          final BinSourceConstruct where, BinSourceConstruct last) {
    if (last instanceof BinReturnStatement) {
      last = ((BinReturnStatement) last).getReturnExpression();
      if (!((BinExpression) last).isChangingAnything()
              && where.getParent() instanceof BinExpressionStatement) {
        last = null; // skip at all - meaningless expression, does nothing
      }
    }

    return last;
  }

  private BinSourceConstruct handleLastStatementSeparately(
          final List stmtsToInline, final BinMethodInvocationExpression where,
          final BinMember whereMember) {
    if (stmtsToInline == null || stmtsToInline.size() == 0) {
      return null;
    }

    BinSourceConstruct last = null;
    if (!(where.getParent() instanceof BinExpressionStatement) ) {
      for (int i = stmtsToInline.size() - 1; i >= 0; i--) {
        if (!(stmtsToInline.get(i) instanceof BinStatement)) {
          stmtsToInline.remove(i);
        }
      }
      last = (BinStatement)stmtsToInline.get(stmtsToInline.size() - 1);
    } else if (stmtsToInline.get(stmtsToInline.size() - 1) instanceof BinReturnStatement
           /* && (whereMember instanceof BinInitializer
                    || whereMember instanceof BinConstructor || ((BinMethod) whereMember)
                    .getReturnType().equals(BinPrimitiveType.VOID_REF))*/) {
      last = (BinStatement)stmtsToInline.get(stmtsToInline.size() - 1);
    }

    return last;
  }



  private void removeEmptyReturn(final List stmtsToInline) {
    for (int i = stmtsToInline.size() - 1; i >= 0; i--) {
      if (!(stmtsToInline.get(i) instanceof BinStatement)) {
        continue;
      }
      final BinStatement lastStmt = (BinStatement) stmtsToInline.get(i);
      if (lastStmt instanceof BinReturnStatement
              && ((BinReturnStatement) lastStmt).getReturnExpression() == null) {
        stmtsToInline.remove(i);
        break;
      }
    }
  }

  private final Map generateRenameMap(
          final BinMethodInvocationExpression where, final Map toRename) {
    Map renameMap = new HashMap();

    Iterator members = toRename.keySet().iterator();
    while (members.hasNext()) {
      BinMember member = (BinMember) members.next();
      List invocationASTs = this.methodVarAnalyzer.getUsageMap().get(member);

      if (invocationASTs == null) {
        if (member instanceof BinCIType) {
          ManagingIndexer supervisor = new ManagingIndexer();
          new TypeNameIndexer(supervisor, (BinCIType) member, true);
          member.getParentMember().accept(supervisor);
          invocationASTs = supervisor.getInvocationsMap().values();
        }
      }

      String newName = (String) toRename.get(member);

      Iterator asts = invocationASTs.iterator();
      while (asts.hasNext()) {
        renameMap.put(asts.next(), newName);
      }
    }

    return renameMap;
  }

  private final Map getVarsToRename(final BinMethodInvocationExpression where) {
    Map varsToRename = new HashMap();

    BinLocalVariable[] vars = this.methodVarAnalyzer.getLocalVariables();
    for (int i = 0; i < vars.length; i++) {
      if (vars[i].getParentMember() != this.method) {
        continue;
      }

      String name = vars[i].getName();
      int count = 0;
      while (varNameHasConflicts(where, name)) {
        name = vars[i].getName() + ++count;
      }
      if (count > 0) {
        varsToRename.put(vars[i], name);
      }
    }

    return varsToRename;
  }

  private final Map getLocalTypesToRename(
          final BinMethodInvocationExpression where) {
    Map typesToRename = new HashMap();

    List types = this.method.getLocalTypes();
    for (int i = 0, max = types.size(); i < max; i++) {
      BinCIType type = (BinCIType) types.get(i);

      String name = type.getName();
      int count = 0;
      while (varNameHasConflicts(where, name)) {
        name = type.getName() + ++count;
      }
      if (count > 0) {
        typesToRename.put(type, name);
      }
    }

    return typesToRename;
  }

  private final Map getThisOrSuperToRename(final TransformationList transList,
          BinMethodInvocationExpression where) {
    if (!where.isOutsideMemberInvocation()) {
      return new HashMap(); // same class probably
    }

    final String newName = where.getExpression().getText();

    class Visitor extends AbstractIndexer {
      final Map renameMap = new HashMap();

      public void visit(BinMethodInvocationExpression x) {
        updateInvocation(x);
        super.visit(x);
      }

      public void visit(BinFieldInvocationExpression x) {
        updateInvocation(x);
        super.visit(x);
      }

      public void visit(BinLiteralExpression x) {
        if ((x.isThis() || x.isSuper())
                && method.getOwner().isDerivedFrom(x.getReturnType())) {
          ASTImpl ast = new CompoundASTImpl(x.getRootAst());
          renameMap.put(ast, newName);
        }
        super.visit(x);
      }

      private void updateInvocation(BinMemberInvocationExpression x) {
        if (x.getExpression() == null
                && method.getOwner().isDerivedFrom(x.getMember().getOwner())) { // implicit
          // this
          ASTImpl ast = new SimpleASTImpl(0, "");
          ast.setLine(x.getStartLine());
          ast.setColumn(x.getStartColumn());
          if (x.getMember().isStatic()) {
            renameMap.put(ast, BinFormatter.formatNotQualified(x.getMember()
                    .getOwner())
                    + ".");
          } else {
            renameMap.put(ast, newName + ".");
          }
        }
      }
    }

    Visitor visitor = new Visitor();
    this.method.accept(visitor);

    Map result = visitor.renameMap;

    if (visitor.renameMap.size() != 1
            && where.getExpression().isChangingAnything()) {
      // TODO: use code from IntroduceTemp to get new var name
      BinTypeRef typeRef = where.getExpression().getReturnType();
      if (typeRef.getBinType() instanceof BinArrayType) {
        typeRef = ((BinArrayType) typeRef.getBinType()).getArrayType();
      }
      final String baseName = StringUtil.decapitalizeFirstLetter(typeRef
              .getName());
      String newVarName = baseName;
      int count = 1;
      while (varNameHasConflicts(where, newVarName)) {
        newVarName += baseName + count++;
      }

      createNewVar(transList, where, where.getExpression().getReturnType(),
              where.getExpression(), newVarName);

      Map newResult = new HashMap();
      Iterator keys = result.keySet().iterator();
      while (keys.hasNext()) {
        Object obj = keys.next();
        if (result.get(obj).equals(newName)) {
          newResult.put(obj, newVarName);
        } else if (result.get(obj).equals(newName + ".")) {
          newResult.put(obj, newVarName + ".");
        } else {
          newResult.put(obj, result.get(obj));
        }
      }
      result = newResult;
    }

    return result;
  }

  private void eraseTestComments(final TransformationList transList,
	BinSourceConstruct where, BinSourceConstruct last) {
		if (last == null
				|| analyzeLastStatementRedundancy(where, last) == null
				|| analyzeLastStatementRedundancy(where, last) instanceof BinStatement) {
			if (where.getParent() instanceof BinExpressionStatement) {
				where = (BinSourceConstruct) where.getParent();
			}
		}
	}

  private void eraseInvocationExpression(final TransformationList transList,
          BinSourceConstruct where, BinSourceConstruct last) {
  	final ASTImpl ast = where.getCompoundAst();
    if (last == null
            || analyzeLastStatementRedundancy(where, last) == null
            || analyzeLastStatementRedundancy(where, last) instanceof BinStatement) {
      if (where.getParent() instanceof BinExpressionStatement) {
        where = (BinSourceConstruct) where.getParent();
      }
    }
    int start = where.getStartPosition();
    int end = where.getEndPosition();

    final StringEraser eraser = new StringEraser(where.getCompilationUnit(),
            start, end);
    eraser.setTrimTrailingSpace(false);
    eraser.setRemoveLinesContainingOnlyComments(true);

    transList.add(eraser);

  }


  private Map generateVarsForParams(final TransformationList transList,
          final BinMethodInvocationExpression where) {
    Map toBeRenamed = new HashMap();

    final BinParameter[] params = where.getMethod().getParameters();
    final BinExpression[] args = where.getExpressionList().getExpressions();
    for (int i = 0; i < params.length - 1; i++) {
      generateVarForParam(params[i], args[i], toBeRenamed, where, transList);
    }
    if (params.length == 0) {
      return toBeRenamed;
    }
    if (!params[params.length - 1].isVariableArity()) {//normal param
      generateVarForParam(params[params.length - 1], args[params.length - 1],
              toBeRenamed, where, transList);
    } else {//variable arity param
      List arrElements = new ArrayList();
      for (int i = params.length - 1; i < args.length; i++) {
        //populate arrElements
        arrElements.add(args[i]);
        //end populate
      }
      generateVarForVararityParams(params[params.length - 1], where, transList,
              arrElements, toBeRenamed);
      //
      //      String newVarName = params[params.length - 1].getName();
      //      int count = 1;
      //      while (varNameHasConflicts(where, newVarName)) {
      //        newVarName = params[params.length - 1].getName() + count++;
      //      }
      //      if (!newVarName.equals(params[params.length - 1].getName())) {
      //        toBeRenamed.put(params[params.length - 1], newVarName);
      //      }
      //      createNewVar(transList, where, params[params.length - 1].getTypeRef(),
      // argument,
      //              newVarName);
      //
      //      String varArrExprStr = createArityVarExprStr(params, arrElements);
      //      toBeRenamed.put(params[params.length-1],varArrExprStr);
    }

    return toBeRenamed;
  }

  /**
   * @param params
   * @param arrElements
   * @return
   */
  private String createArityVarExprStr(final BinParameter[] params,
          List arrElements) {
    StringBuffer varArrExpr = new StringBuffer("new ");
    varArrExpr.append(params[params.length - 1].getTypeRef().getNonArrayType()
            .getName());
    int arrDimension = ((BinArrayType) (params[params.length - 1].getTypeRef()
            .getBinType())).getDimensions();
    StringBuffer startCurlyBraces = new StringBuffer();
    StringBuffer endCurlyBraces = new StringBuffer();
    for (int i = 0; i < arrDimension; i++) {
      varArrExpr.append("[]");
      startCurlyBraces.append('{');
      endCurlyBraces.append('}');
    }
    varArrExpr.append(startCurlyBraces);
    for (Iterator iter = arrElements.iterator(); iter.hasNext();) {
      String element = (String) iter.next();
      varArrExpr.append(element);
      varArrExpr.append(", ");
    }
    //wipe last ", "
    varArrExpr.setLength(varArrExpr.length() - 2);
    return varArrExpr.toString();
  }

  private void generateVarForVararityParams(BinParameter parameter,
          BinMethodInvocationExpression where, TransformationList transList,
          List arrVarsExprs, Map toBeRenamed) {
    //// final BinExpression argument = where.getExpressionList()
    //// .getExpressions()[parameter.getIndex()];
    //    final VarInfo internalVarInfo = this.methodVarAnalyzer
    //            .getVarInfo(parameter);
    //
    //    if (!argument.isChangingAnything()) { // static info, can be skipped
    //      if (!internalVarInfo.usedInside && !internalVarInfo.changedInside) {
    //        //return; // really useless param
    //        toBeRenamed.add(argument.getText());
    //      } else if (!internalVarInfo.changedInside) {
    //        String expr = argument.getText();
    //        if (argument instanceof BinCastExpression) {
    //          expr = "(" + expr + ")";
    //        }
    //        toBeRenamed.add(expr);
    //        return;
    //      }
    //    }
    //
    //    boolean hasFinalConflict = false;
    //    if (argument instanceof BinFieldInvocationExpression) {
    //      if (((BinFieldInvocationExpression) argument).getField().isFinal()
    //              && internalVarInfo.changedInside) {
    //        hasFinalConflict = true;
    //      }
    //    }
    //
    //    if (argument instanceof BinVariableUseExpression) {
    //      VariableUseAnalyzer analyzer = new VariableUseAnalyzer(getContext(),
    //              where.getParentMember(), CollectionUtil
    //                      .singletonArrayList(argument));
    //      BinLocalVariable externalVar = ((BinVariableUseExpression) argument)
    //              .getVariable();
    //      VarInfo externalVarInfo = analyzer.getVarInfo(externalVar);
    //      if (externalVar.isFinal() && internalVarInfo.changedInside) {
    //        hasFinalConflict = true;
    //      } else if (!externalVarInfo.usedAfter
    //              || externalVarInfo.changesBeforeUseAfter == VarInfo.YES) {
    //        toBeRenamed.add(((BinVariableUseExpression) argument)
    //                .getVariable().getName());
    //        return;
    //      }
    //    }
    //
    //// // in case of just one read nothing changes
    //// if (this.methodVarAnalyzer.getUsageMap().get(parameter).size() == 1) {
    //// if (internalVarInfo.usedInside && !internalVarInfo.changedInside) {
    //// String expr = argument.getText();
    //// if (argument instanceof BinCastExpression) {
    //// expr = "(" + expr + ")";
    //// }
    //// toBeRenamed.add(expr);
    //// return;
    //// }
    //// }

    String newVarName = parameter.getName();
    int count = 1;
    while (varNameHasConflicts(where, newVarName)) {
      newVarName = parameter.getName() + count++;
    }
    if (!newVarName.equals(parameter.getName())) {
      toBeRenamed.put(parameter, newVarName);
    }
    final SourceCoordinate insertAt = new SourceCoordinate(where
            .getEnclosingStatement().getStartLine(), 0);

    BinLocalVariable local = new BinLocalVariable(newVarName, parameter
            .getTypeRef().getTypeRefAsIs(), 0);
    BinLocalVariableDeclaration decl = new BinLocalVariableDeclaration(
            new BinLocalVariable[]{local}, null);
    local.setParent(decl);
    BinItemVisitable parent = where.getParent();
    if (parent instanceof BinExpressionStatement) {
      parent = parent.getParent();
    }
    decl.setParent(parent);
    if (arrVarsExprs.size() == 1
            && ((BinExpression) arrVarsExprs.get(0)).getReturnType().isArray()) {
        local.setExpression((BinExpression)arrVarsExprs.get(0));
    } else {
      BinArrayInitExpression arrInitExpression = new BinArrayInitExpression(
          (BinExpression[]) arrVarsExprs.toArray(
          new BinExpression[arrVarsExprs.size()]), null);
      BinNewExpression newExpression = new BinNewExpression(
          parameter.getTypeRef(),
          BinExpressionList.NO_EXPRESSIONLIST,
          null, arrInitExpression, null, null);
      local.setExpression(newExpression);
    }
    String newVar = decl.getFormatter().print();
    transList.add(
        new StringInserter(where.getCompilationUnit(), insertAt, newVar));
  }

  private void generateVarForParam(BinParameter parameter,
          BinExpression argumentExpr, Map toBeRenamed,
          BinMethodInvocationExpression where,
          final TransformationList transList) {
    //    final BinExpression givenAsParam = where.getExpressionList()
    //        .getExpressions()[parameter.getIndex()];
    final VarInfo internalVarInfo = this.methodVarAnalyzer
            .getVarInfo(parameter);

    if (!argumentExpr.isChangingAnything()) { // static info, can be skipped
      if (!internalVarInfo.usedInside && !internalVarInfo.changedInside) {
        return; // really useless param
      } else if (!internalVarInfo.changedInside) {
        String expr = argumentExpr.getText();
        if (argumentExpr instanceof BinCastExpression) {
          expr = "(" + expr + ")";
        }
        toBeRenamed.put(parameter, expr);
        return;
      }
    }

    boolean hasFinalConflict = false;
    if (argumentExpr instanceof BinFieldInvocationExpression) {
      if (((BinFieldInvocationExpression) argumentExpr).getField().isFinal()
              && internalVarInfo.changedInside) {
        hasFinalConflict = true;
      }
    }

    if (argumentExpr instanceof BinVariableUseExpression) {
      VariableUseAnalyzer analyzer = new VariableUseAnalyzer(getContext(),
              where.getParentMember(), CollectionUtil
                      .singletonArrayList(argumentExpr));
      BinLocalVariable externalVar = ((BinVariableUseExpression) argumentExpr)
              .getVariable();
      VarInfo externalVarInfo = analyzer.getVarInfo(externalVar);
      if (externalVar.isFinal() && internalVarInfo.changedInside) {
        hasFinalConflict = true;
      } else if (((!externalVarInfo.usedAfter
              || externalVarInfo.changesBeforeUseAfter == VarInfo.YES)) &&
							(!externalVarInfo.usedBeforeInSameStatement)) {
        toBeRenamed.put(parameter, ((BinVariableUseExpression) argumentExpr)
                .getVariable().getName());
        return;
      }
    }

    // in case of just one read nothing changes
    if (this.methodVarAnalyzer.getUsageMap().get(parameter).size() == 1) {
      if (internalVarInfo.usedInside && !internalVarInfo.changedInside) {
        String expr = argumentExpr.getText();
        if (argumentExpr instanceof BinCastExpression) {
          expr = "(" + expr + ")";
        }
        toBeRenamed.put(parameter, expr);
        return;
      }
    }

    String newVarName = parameter.getName();
    int count = 1;
    while (hasFinalConflict || varNameHasConflicts(where, newVarName)) {
      newVarName = parameter.getName() + count++;
      hasFinalConflict = false;
    }
    if (!newVarName.equals(parameter.getName())) {
      toBeRenamed.put(parameter, newVarName);
    }

    createNewVar(transList, where, parameter.getTypeRef(), argumentExpr,
            newVarName);
  }

  private void createNewVar(final TransformationList transList,
          final BinSourceConstruct where, final BinTypeRef typeRef,
          final BinExpression initExpr, final String newVarName) {
    final SourceCoordinate insertAt = new SourceCoordinate(where
            .getEnclosingStatement().getStartLine(), 0);

    BinLocalVariable local = new BinLocalVariable(newVarName, typeRef, 0);
    BinLocalVariableDeclaration decl = new BinLocalVariableDeclaration(
            new BinLocalVariable[]{local}, null);
    local.setParent(decl);
    BinItemVisitable parent = where.getParent();
    while (!(parent instanceof BinStatementList) && (parent != null)) {
    //if (parent instanceof BinExpressionStatement) {
      parent = parent.getParent();
    }
    decl.setParent(parent);
    local.setExpression(initExpr);

    String newVar = decl.getFormatter().print();

    transList.add(new StringInserter(where.getCompilationUnit(), insertAt,
            newVar));
  }

  private static final boolean varNameHasConflicts(
          final BinSourceConstruct where, final String newName) {
    LocalVariableDuplicatesFinder duplicateFinder = new LocalVariableDuplicatesFinder(
            null, newName, where);
    where.getParentMember().accept(duplicateFinder);

    return duplicateFinder.getDuplicates().size() > 0;
  }

  private void addEmptyLines(List stmtsToInline) {
    List empties = new ArrayList();

    Collections.sort(stmtsToInline, LocationAware.PositionSorter.getInstance());

    for (int i = 0; i < stmtsToInline.size() - 1; i++) {
      LocationAware first = (LocationAware) stmtsToInline.get(i);
      LocationAware next = (LocationAware) stmtsToInline.get(i + 1);
      for (int k = first.getEndLine() + 1; k < next.getStartLine(); k++) {
        empties.add(new EmptyLine(first.getCompilationUnit(), k, 1, k, first
                .getCompilationUnit().getLineIndexer().posToLineCol(
                        first.getCompilationUnit().getLineIndexer()
                                .lineColToPos(k + 1, 1) - 1).getColumn()));
      }
    }

    stmtsToInline.addAll(empties);

    Collections.sort(stmtsToInline, LocationAware.PositionSorter.getInstance());
  }

  private Map getInnersToQualify(final BinMethodInvocationExpression where) {

    InnersToQualifySeeker seeker = new InnersToQualifySeeker(where
            .getParentType());
    where.getMethod().getBody().accept(seeker);

    return seeker.getEditMap();
  }

  private List getSingleComments(List cnstrToInline, List comments) {
    List singleComments = new ArrayList();

    Iterator coms = comments.iterator();
    while (coms.hasNext()) {
      Comment com = (Comment) coms.next();
      boolean add = true;

      Iterator cnstrs = cnstrToInline.iterator();
      while (cnstrs.hasNext()) {
        LocationAware la = (LocationAware) cnstrs.next();
        if (la.contains(com)) {
          add = false;
        }
      }

      if (add) {
        singleComments.add(com);
      }
    }

    return singleComments;
  }

  public final String getDescription() {

    String str = "Inline method: ";
    List list = BinModifier.splitModifier(method.getModifiers());
    for (int i = 0; i < list.size(); i++) {
      int modifier = ((Integer) list.get(i)).intValue();
      str += new BinModifierFormatter(modifier).format(modifier, true, false) + " ";
    }
    str += method.getReturnType().getName() + " ";
    str += method.getName() + "(";

    for (int x = 0; x < method.getParameters().length; x++) {
      str += method.getParameters()[x].getTypeRef().getName() + " " +
          method.getParameters()[x].getQualifiedName() + ", ";

    }

    str = str.substring(0, str.length()-2);

    str += ")";

    return str;
  }

  public int getMethodDeclarationAction() {
    return methodDeclarationAction;
  }

  public void setMethodDeclarationAction(int methodDeclarationAction) {
    this.methodDeclarationAction = methodDeclarationAction;
  }

  public String getKey() {
    return key;
  }
}
