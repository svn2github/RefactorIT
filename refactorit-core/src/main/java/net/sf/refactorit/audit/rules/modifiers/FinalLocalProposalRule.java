/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.modifiers;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Arseni Grigorjev
 */
public class FinalLocalProposalRule extends AuditRule {
	public static final String NAME = "finalize_locals";

	// private List assignedDecl = new ArrayList();
	private MultiValueMap assignments = new MultiValueMap();

	private MultiValueMap methodAssignments = new MultiValueMap();

	public void postProcess() {
		for (Iterator it = assignments.keySet().iterator(); it.hasNext();) {
			BinVariable var = ((BinVariable) it.next());
			if (!assignments.contains(var, null)) {
				addViolationFor(var);
			}
		}

		assignments.clear();
		methodAssignments.clear();
	}

	// registers array of parameters
	private final void registerParameters(BinParameter[] array) {
		for (int i = array.length - 1; i >= 0; i--) {
			registerVariableDeclaration(array[i]);
		}
	}

	public final void addViolationFor(BinVariable var) {
		if (var instanceof BinParameter) {
			addViolation(new FinalParamProposal((BinParameter) var));
		} else if (var.getParentType() != null) {
			addViolation(new FinalLocalVarProposal(var));
		}
	}

	// entering scope: method
	public final void visit(BinMethod method) {
		if (!method.isAbstract() && !method.isSynthetic()) {
			registerParameters(method.getParameters());
		}

		super.visit(method);
	}

	// entering scope: constructor
	public final void visit(BinConstructor constructor) {
		if (!constructor.isSynthetic()) {
			registerParameters(constructor.getParameters());
		}

		super.visit(constructor);
	}

	public final void visit(BinLocalVariableDeclaration decl) {
		BinVariable[] declvars = decl.getVariables();
		if (!(decl.getParent() instanceof BinForStatement && declvars.length > 1)) {
			for (int i = 0; i < declvars.length; i++) {
				registerVariableDeclaration(declvars[i]);
			}
		}
		super.visit(decl);
	}

	public final void registerVariableDeclaration(BinVariable var) {
		if (var != null && !var.isFinal()) {
			if (var instanceof BinParameter) {
				Scope parameterScope = getParameterScope((BinParameter) var);
				if (parameterScope != null) {
					assignments.put(var, parameterScope);
				}
			} else if (var.getExpression() != null) {
				assignments.put(var, getAssignmentScope(var.getExpression()));
			}
		}
	}

	private final void registerVariableAssignment(BinVariable var,
			BinExpression expr) {
		if (var != null && expr != null && (!assignments.containsKey(var))
				|| !assignments.get(var).contains(null)) {
			if (var instanceof BinParameter || isInLoop(expr)) {
				assignments.put(var, null);
			} else {
				final Scope block = getAssignmentScope(expr);
				BinItemVisitable biv = getIfSwitchStmtIfExists(expr);
				boolean isDblAssigned = isDoubleAssigned(var, expr);
				methodAssignments.put(var, biv);
				if (isAlreadyAssignedInScope(var, block) || var.isFinal()
						|| isDblAssigned) {
					assignments.put(var, null);
				} else {
					assignments.put(var, block);
				}
			}
		}
	}

	public final void visit(BinAssignmentExpression expression) {
		final BinExpression leftExpression = expression.getLeftExpression();
		if (leftExpression instanceof BinVariableUseExpression) {
			final BinVariable var = ((BinVariableUseExpression) leftExpression)
					.getVariable();
			registerVariableAssignment(var, expression);
		}

		super.visit(expression);
	}

	private boolean isInLoop(BinExpression expr) {
		BinItemVisitable b = expr.getParent();
		while (b != null && !(b instanceof BinMember)) {
			if (b instanceof BinWhileStatement || b instanceof BinForStatement
					|| b instanceof BinTryStatement) {
				return true;
			}
			b = b.getParent();
		}
		return false;
	}

	private BinItemVisitable getIfSwitchStmtIfExists(BinExpression expr) {
		BinItemVisitable b = expr.getParent();
		while (b != null && !(b instanceof BinMember)) {
			if (b instanceof BinIfThenElseStatement
					|| b instanceof BinSwitchStatement) {
				return b;
			}
			b = b.getParent();
		}
		return null;
	}

	private boolean isDoubleAssigned(BinVariable var, BinExpression biv) {
		List l = methodAssignments.get(var);

		if (l != null) {
			if (!l.contains(getIfSwitchStmtIfExists(biv)) && l.size() > 0
					|| l.size() > 1) {
				return true;
			}
		}

		return false;
	}

	private boolean isAlreadyAssignedInScope(BinVariable var, Scope s) {
		if (var == null || s == null) {
			return false;
		}
		if (assignments.containsKey(var)) {
			for (Iterator it = assignments.get(var).iterator(); it.hasNext();) {
				Scope scp = (Scope) it.next();
				if (scp != null && scp.contains(s)) {
					return true;
				}
			}
		}
		return false;
	}

	private Scope getAssignmentScope(BinExpression expr) {
		BinItemVisitable b = expr.getParent();
		while (b != null && !(b instanceof BinItem) && !(b instanceof BinStatement)) {
			b = b.getParent();
		}
		if (b instanceof BinItem) {
			return ((BinItem) b).getScope();
		} else if (b instanceof BinStatement) {
			return ((BinStatement) b).getScope();
		} else {
			// System.err.println("Error!!!");
			return null;
		}
	}

	private Scope getParameterScope(BinParameter param) {
		BinStatementList body = param.getMethod().getBody();
		if (body != null) {
			return body.getScope();
		} else {
			return null;
		}
	}

	public final void visit(BinIncDecExpression expr) {
		final BinExpression incrementedExpr = expr.getExpression();
		if (incrementedExpr instanceof BinVariableUseExpression) {
			assignments.put(((BinVariableUseExpression) incrementedExpr)
					.getVariable(), null);
		}

		super.visit(expr);
	}
}

class FinalLocalProposal extends SimpleViolation {

	FinalLocalProposal(BinTypeRef type, ASTImpl ast, String msg) {
		super(type, ast, msg, "refact.audit.finalize_locals");
	}
}

class FinalLocalVarProposal extends FinalLocalProposal {

	FinalLocalVarProposal(BinVariable variable) {
		super(variable.getOwner(), variable.getNameAstOrNull(), "Variable '"
				+ variable.getName() + "' should be 'final' " + "(is never reassigned)");
		setTargetItem(variable);
	}

	public BinMember getSpecificOwnerMember() {
		BinVariable variable = (BinVariable) getTargetItem();
		if (variable instanceof BinParameter) {
			return ((BinParameter) variable).getMethod();
		}
		return variable.getParentMember();
	}

	public BinVariable getVariable() {
		return (BinVariable) getTargetItem();
	}

	public List getCorrectiveActions() {
		return Collections.singletonList(FinalizeLocalsAction.INSTANCE);
	}
}

class FinalParamProposal extends FinalLocalProposal {

	FinalParamProposal(BinParameter param) {
		super(param.getOwner(), param.getNameAstOrNull(), "Parameter '"
				+ param.getName() + "' should be 'final' " + "(is never reassigned)");
		setTargetItem(param);
	}

	public BinParameter getParameter() {
		return (BinParameter) getTargetItem();
	}

	public BinMember getSpecificOwnerMember() {
		return getParameter().getMethod();
	}

	public List getCorrectiveActions() {
		return Collections.singletonList(FinalizeParamsAction.INSTANCE);
	}
}

class FinalizeLocalsAction extends MultiTargetGroupingAction {
	public static FinalizeLocalsAction INSTANCE = new FinalizeLocalsAction();

	public String getKey() {
		return "refactorit.audit.action.local.add_final";
	}

	public String getName() {
		return "Accept suggested final modifier for local variable";
	}

	public String getMultiTargetName() {
		return "Accept suggested final modifier(s) for local variable(s)";
	}

	public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
		Map map = new HashMap();
		for (Iterator i = violations.iterator(); i.hasNext();) {
			RuleViolation violation = (RuleViolation) i.next();
			if (violation instanceof FinalLocalVarProposal) {
				FinalLocalVarProposal ourViolation = (FinalLocalVarProposal) violation;
				BinLocalVariableDeclaration declaration = (BinLocalVariableDeclaration) ourViolation
						.getVariable().getParent();
				List varsToFinalize = (List) map.get(declaration);

				if (varsToFinalize == null) {
					varsToFinalize = new LinkedList();
					map.put(declaration, varsToFinalize);
				}

				varsToFinalize.add(ourViolation.getVariable());
			}
		}

		final Set sources = new HashSet(map.keySet().size());

		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			BinLocalVariableDeclaration declaration = (BinLocalVariableDeclaration) i
					.next();

			CompilationUnit cu = declaration.getCompilationUnit();
			final List varsToFinalize = (List) map.get(declaration);
			process(manager, cu, declaration, varsToFinalize);
			sources.add(cu);
		}
		return sources;
	}

	public void process(TransformationManager manager,
			CompilationUnit compilationUnit, BinLocalVariableDeclaration declaration,
			List varsToFinalize) {

		final String strFinalModif = "final ";
		final BinVariable[] variables = declaration.getVariables();
		final String strType = variables[0].getTypeAndModifiersNodeText().trim()
				+ " ";

		if (varsToFinalize.contains(variables[0])) {
			manager.add(new StringInserter(compilationUnit, declaration
					.getCompoundAst().getStartLine(), declaration.getCompoundAst()
					.getStartColumn() - 1, strFinalModif));
		}

		int start_line = -1;
		int start_col = -1;

		if (variables.length > 1) {
			BinExpression tmpExpr;
			for (int i = 0; i < variables.length - 1; i++) {
				tmpExpr = variables[i].getExpression();
				start_line = (tmpExpr == null) ? variables[i].getNameAstOrNull()
						.getStartLine() : tmpExpr.getStartLine();
				start_col = (tmpExpr == null) ? variables[i].getNameAstOrNull()
						.getEndColumn() - 1 : tmpExpr.getEndColumn() - 1;

				SourceCoordinate commaCoordinates = getNextComma(compilationUnit,
						start_line, start_col);

				SourceCoordinate removeStart = expandWhitespaceRemoveLeft(
						compilationUnit, commaCoordinates);
				SourceCoordinate removeEnd = expandWhitespaceRemoveRight(
						compilationUnit, commaCoordinates);

				manager
						.add(new StringEraser(compilationUnit, removeStart.getLine(),
								removeStart.getColumn(), removeEnd.getLine(), removeEnd
										.getColumn()));

				SourceCoordinate semicolonCoordinates = findPlaceForInsertion(
						compilationUnit, removeStart);

				manager.add(new StringInserter(compilationUnit, semicolonCoordinates,
						";"));

				String strInsert;
				if (varsToFinalize.contains(variables[i + 1])) {
					strInsert = FormatSettings.LINEBREAK
							+ FormatSettings.getIndentString(declaration.getIndent())
							+ strFinalModif + strType;
				} else {
					strInsert = FormatSettings.LINEBREAK
							+ FormatSettings.getIndentString(declaration.getIndent())
							+ strType;
				}
				manager
						.add(new StringInserter(compilationUnit, removeStart, strInsert));
			}
		}
	}

	private SourceCoordinate findPlaceForInsertion(
			CompilationUnit compilationUnit, SourceCoordinate startCoordinates) {
		SourceCoordinate result = startCoordinates;

		List comments = compilationUnit.getSimpleComments();
		Comment comment;
		for (int i = 0, max = comments.size(); i < max; i++) {
			comment = (Comment) comments.get(i);
			if (comment.getText().charAt(1) == '/'
					&& comment.getStartLine() == startCoordinates.getLine()
					&& comment.getStartColumn() < startCoordinates.getColumn()) {
				result = new SourceCoordinate(comment.getStartLine(), comment
						.getStartColumn() - 1);
				result = expandWhitespaceRemoveLeft(compilationUnit, result);
				break;
			}
		}

		return result;
	}

	private SourceCoordinate expandWhitespaceRemoveRight(
			CompilationUnit compilationUnit, SourceCoordinate coordinates) {
		int line = coordinates.getLine();
		int col = coordinates.getColumn() + 1;

		String source = compilationUnit.getSource().getContentOfLine(line);

		boolean canExpand = true;
		while (canExpand) {
			try {
				while (source.charAt(col) == ' ' || source.charAt(col) == '\t') {
					col++;
				}
				canExpand = false;
			} catch (StringIndexOutOfBoundsException e) {
				source = compilationUnit.getSource().getContentOfLine(line++);
				col = 0;
			}
		}

		return new SourceCoordinate(line, col);
	}

	private SourceCoordinate expandWhitespaceRemoveLeft(
			CompilationUnit compilationUnit, SourceCoordinate coordinates) {

		int line = coordinates.getLine();
		int col = coordinates.getColumn() - 1;

		String source = compilationUnit.getSource().getContentOfLine(line);

		boolean canExpand = true;
		while (canExpand) {
			try {
				while (source.charAt(col) == ' ' || source.charAt(col) == '\t') {
					col--;
				}
				canExpand = false;
			} catch (StringIndexOutOfBoundsException e) {
				line--;
				source = compilationUnit.getSource().getContentOfLine(line);
				col = source.length() - 1;
			}
		}

		return new SourceCoordinate(line, col + 1);
	}

	private SourceCoordinate getNextComma(CompilationUnit compilationUnit,
			int start_line, int start_col) {
		boolean foundComma = false;

		while (!foundComma) {
			String source = compilationUnit.getSource().getContentOfLine(start_line);
			try {
				for (int i = start_col;; i++) {
					if (source.charAt(i) == ',') {
						foundComma = true;
						start_col = i;
						break;
					} else if (source.charAt(i) == '/') {
						if (source.charAt(i + 1) == '/') {
							// go search on next line
							start_col = 0;
							start_line++;
							break;
						} else {
							for (int k = i; k < source.length(); k++) {
								if (source.charAt(k) == '*' && source.charAt(k) == '/') {
									break;
								}
							}
						}
					}
				}
			} catch (StringIndexOutOfBoundsException e) {
				start_col = 0;
				start_line++;
			}
		}

		return new SourceCoordinate(start_line, start_col);
	}
}

class FinalizeParamsAction extends MultiTargetCorrectiveAction {
	public static final FinalizeParamsAction INSTANCE = new FinalizeParamsAction();

	public String getKey() {
		return "refactorit.audit.action.param.add_final";
	}

	public String getName() {
		return "Accept suggested final modifier for parameter";
	}

	public String getMultiTargetName() {
		return "Accept suggested final modifier(s) for parameter(s)";
	}

	protected Set process(TreeRefactorItContext context,
			TransformationManager manager, RuleViolation violation) {
		if (!(violation instanceof FinalParamProposal)) {
			return Collections.EMPTY_SET;
		}

		BinParameter parameter = ((FinalParamProposal) violation).getParameter();
		StringInserter finalInserter = new StringInserter(violation
				.getCompilationUnit(), parameter.getStartLine(), parameter
				.getStartColumn() - 1, "final ");
		manager.add(finalInserter);

		return Collections.singleton(violation.getCompilationUnit());
	}

}
