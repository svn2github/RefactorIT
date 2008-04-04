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
import net.sf.refactorit.audit.AwkwardMemberModifiers;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SingleAssignmentFinalRule extends AuditRule {
	public static final String NAME = "single_private_assignment";

	HashSet fields = new HashSet();

	HashSet incrementedFields = new HashSet();

	HashSet condFields = new HashSet();

	HashSet constructors = new HashSet();

	MultiValueMap assignments = new MultiValueMap();

	MultiValueMap fieldUsages = new MultiValueMap();

	MultiValueMap fieldAssignments = new MultiValueMap();

	HashSet visited = new HashSet();

	public void visit(BinFieldDeclaration decl) {
		BinField[] bva = (BinField[]) decl.getVariables();
		BinExpression be;
		for (int i = 0; i < bva.length; i++)
			if (bva[i].isPrivate() && !bva[i].isFinal() && !bva[i].isStatic())
				if ((be = bva[i].getExpression()) == null && !fields.contains(bva[i]))
					fields.add(bva[i]);

		super.visit(decl);
	}

	public void visit(BinIncDecExpression x) {
		if (x.getExpression() instanceof BinFieldInvocationExpression)
			incrementedFields.add(((BinFieldInvocationExpression) x.getExpression())
					.getField());

		super.visit(x);
	}

	public void visit(BinConstructor x) {
		constructors.add(x);

		super.visit(x);
	}

	public void visit(BinAssignmentExpression aex) {
		if (aex.getLeftExpression() instanceof BinFieldInvocationExpression
				&& (aex.getParentMember() instanceof BinMethod)) {
			assignments.put(aex.getParentMember(), aex);
			if (isConditional(aex))
				condFields.add(((BinFieldInvocationExpression) aex.getLeftExpression())
						.getField());
		}

		super.visit(aex);
	}

	private boolean isConditional(BinAssignmentExpression aex) {
		BinItemVisitable biv = aex.getParent();
		while (!((biv instanceof BinForStatement)
				|| (biv instanceof BinIfThenElseStatement)
				|| (biv instanceof BinSwitchStatement)
				|| (biv instanceof BinTryStatement)
				|| (biv instanceof BinWhileStatement) || (biv instanceof BinMethod))) {
			biv = biv.getParent();
		}

		if (biv instanceof BinMethod)
			return false;
		else
			return true;

	}

	public void visit(BinConstructorInvocationExpression x) {
		if (x.getParentMember() instanceof BinMethod)
			assignments.put(x.getParentMember(), x);

		super.visit(x);
	}

	public void visit(BinFieldInvocationExpression x) {
		if (!(x.getParent() instanceof BinAssignmentExpression)) {
			fieldUsages.put(x.getField(), x);
		}

		super.visit(x);
	}

	public void visit(BinMethodInvocationExpression x) {
		if (!(x.getParent() instanceof BinAssignmentExpression)
				&& (x.getParentMember() instanceof BinMethod))
			assignments.put(x.getParentMember(), x);

		super.visit(x);
	}

	private int countAssignments(BinField field, BinMethod m){
    int result = 0;

		if (visited.contains(m))
			return 0;
		visited.add(m);
		if (field.getOwner() == m.getOwner()) {
			List l = assignments.get(m);
			if (l == null)
				return 0;
			BinExpression[] exprs = (BinExpression[]) l
					.toArray(new BinExpression[] {});
			List inv = fieldUsages.get(field);
			boolean assigned = false;
			for (int i = 0; i < exprs.length; i++) {
				if (exprs[i] instanceof BinAssignmentExpression) {
					BinField fieldLocal = ((BinFieldInvocationExpression) ((BinAssignmentExpression) exprs[i])
							.getLeftExpression()).getField();
					BinExpression rightExpr = ((BinAssignmentExpression) exprs[i])
							.getRightExpression();
					if (field.isSame(fieldLocal)) {
						fieldAssignments.put(field, m);
						if (!(m instanceof BinConstructor))
							result = 2;
						if (rightExpr instanceof BinLiteralExpression)
							result++;
						else if (rightExpr instanceof BinMethodInvocationExpression)
							result += countAssignments(field,
									(BinMethod) ((BinMethodInvocationExpression) rightExpr)
											.getMethod()) + 1;
            else if (rightExpr instanceof BinNewExpression)
              result += 1;
					} else if (rightExpr instanceof BinMethodInvocationExpression)
						result += countAssignments(field,
								(BinMethod) ((BinMethodInvocationExpression) rightExpr)
										.getMethod());
				} else if (exprs[i] instanceof BinConstructorInvocationExpression)
					result += countAssignments(field,
							((BinConstructorInvocationExpression) exprs[i]).getMethod());
				else if (exprs[i] instanceof BinMethodInvocationExpression)
					result += countAssignments(field,
							((BinMethodInvocationExpression) exprs[i]).getMethod());
				if (result != 0) {
					if (assigned == false) {
						assigned = true;
						if (inv != null) {
							for (int j = 0; j < inv.size(); j++)
								if (!((BinFieldInvocationExpression) inv.get(j))
										.isAfter(exprs[i])
										&& ((BinFieldInvocationExpression) inv.get(j)).getOwner() == exprs[i]
												.getOwner()) {
									result = 2;
								}
						}
					}
				}
			}

		}
		if (result != 0)
			fieldAssignments.put(field, m);

		return result;
	}

	public void postProcess() {
		fields.removeAll(incrementedFields);
		fields.removeAll(condFields);

		BinField[] keys = (BinField[]) fields.toArray(new BinField[] {});
		for (int i = 0; i < keys.length; i++) {
			BinField f = keys[i];
			boolean canBeFinal = true;
			int counter = 0;

			for (Iterator j = assignments.keySet().iterator(); j.hasNext()
					&& canBeFinal;) {
				BinMethod m = (BinMethod) j.next();
				visited.clear();
				int c = countAssignments(f, m);
				;
				if (c > 1)
					canBeFinal = false;
				else if (c == 1)
					counter++;
			}

			if (overlaps(fieldAssignments.get(keys[i]), constructors) && canBeFinal
					&& counter != 0)
				addViolation(new SingleAssignmentFinalViolation(f));
		}
		
		fields.clear();
		assignments.clear();
		visited.clear();
		condFields.clear();
		incrementedFields.clear();
		fieldUsages.clear();
		fieldAssignments.clear();
	}

	private boolean noViolationsFor(BinField f) {
		ArrayList v = getViolations();
		for (int i = 0; i < v.size(); i++) {
			SingleAssignmentFinalViolation safv = (SingleAssignmentFinalViolation) v
					.get(i);
			BinField field = (BinField) safv.getOwnerMember();
			if (field.isSame(f))
				return false;
		}
		
		return true;
	}

	private boolean overlaps(List a, Set b) {
		HashSet temp = new HashSet();

		if (a == null || b == null)
			return false;

		BinMethod f = (BinMethod) a.get(0);

		for (Iterator i = b.iterator(); i.hasNext();) {
			BinMethod m = (BinMethod) i.next();
			if (f.getOwner() == m.getOwner())
				temp.add(m);
		}
		
		return a.containsAll(temp);
	}
}

class SingleAssignmentFinalViolation extends AwkwardMemberModifiers {
	public SingleAssignmentFinalViolation(BinField f) {
		super(f, "Field " + f.getName() + " is assigned only "
				+ "once in constructors and can be made final.", null);
	}

	public List getCorrectiveActions() {
		return Collections.singletonList(FinalizePrivateAction.INSTANCE);
	}
}

class FinalizePrivateAction extends MultiTargetGroupingAction {
	public static FinalizePrivateAction INSTANCE = new FinalizePrivateAction();

	public String getName() {
		return "Make field final.";
	}

	public String getMultiTargetName() {
		return "Make all proposed fields final.";
	}

	public String getKey() {
		return "refactorit.audit.action.make_a_single_assignment_field_final";
	}
  
	public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
		Set result = new HashSet();
		Map m = new HashMap();

		// Group the violations on the base of common declarations
		for (Iterator i = violations.iterator(); i.hasNext();) {
			SingleAssignmentFinalViolation v = (SingleAssignmentFinalViolation) i
					.next();
			BinField field = (BinField) v.getOwnerMember();
			BinFieldDeclaration decl = (BinFieldDeclaration) field.getParent();
			ArrayList finalFields = (ArrayList) m.get(decl);
			if (finalFields == null)
				finalFields = new ArrayList();
			finalFields.add(field);
			m.put(decl, finalFields);
		}

		// The main cycle. Violations are processed on the common declaration basis.
		for (Iterator i = m.keySet().iterator(); i.hasNext();) {
			BinFieldDeclaration decl = (BinFieldDeclaration) i.next();
			Map commentsMap = getCommentsMap(decl); // Only those that are inside the
			// declaration, not "//"
			List fieldsToFinalize = (ArrayList) m.get(decl);
			BinField[] allFields = (BinField[]) decl.getVariables();
			String indent = FormatSettings.getIndentString(decl.getIndent());
			List comments = Comment.getCommentsIn(decl);
			for (int j = 0; j < allFields.length; j++) {
				BinField field = allFields[j];
				BinExpression be;
				String res;
				Comment lec;
				String modifiers = field.getTypeAndModifiersNodeText().trim();

				if (fieldsToFinalize.contains(field))
					res = indent
							+ (new BinModifierFormatter(field.getModifiers()
									| BinModifier.FINAL)).print() + " "
							+ BinFormatter.format(field.getTypeRef()) + " " + field.getName();
				else {
					res = indent
							+ (new BinModifierFormatter(field.getModifiers())).print() + " "
							+ BinFormatter.format(field.getTypeRef()) + " " + field.getName();
					if ((be = field.getExpression()) != null)
						res += " = " + be.getText();
				}
				if (commentsMap.containsKey(field))
					res += " " + ((Comment) commentsMap.get(field)).getText();
				res += ";";
				if ((lec = getLineEndComment(decl)) != null
						&& j == allFields.length - 1) {
					res += " " + lec.getText();
					manager.add(new StringEraser(lec));
				}
				if (j < allFields.length - 1)
					res += FormatSettings.LINEBREAK;
				manager.add(new StringInserter(decl.getCompilationUnit(), decl
						.getCompoundAst().getStartLine(), 0, res));
			}
			manager.add(new StringEraser(decl.getCompilationUnit(), decl
					.getStartLine(), decl.getStartColumn() - 1, getDeclEnd(decl)
					.getLine(), getDeclEnd(decl).getColumn()));
			result.add(decl.getCompilationUnit());
		}
		return result;
	}

	private SourceCoordinate getDeclEnd(BinFieldDeclaration decl) {
		int line = decl.getEndLine();
		String s = decl.getCompilationUnit().getSource().getContentOfLine(line);
		int column = decl.getEndColumn() - 2;

		while (s.charAt(column) == '\t' || s.charAt(column) == ' '
				|| s.charAt(column) == '\r' || s.charAt(column) == '\n') {
			if (++column == s.length()) {
				s = decl.getCompilationUnit().getSource().getContentOfLine(++line);
				if (line == decl.getCompilationUnit().getSource().getLineCount())
					return null;
				column = 0;
			}
		}

		return new SourceCoordinate(line, column + 1);
	}

	private Map getCommentsMap(BinFieldDeclaration decl) {
		Map m = new HashMap();
		List comments = Comment.getCommentsIn(decl);
		BinField[] fields = (BinField[]) decl.getVariables();
		CompilationUnit cu = decl.getCompilationUnit();

		for (int i = 0; i < comments.size(); i++) {
			Comment cmt = (Comment) comments.get(i);
			SourceCoordinate commentSC = new SourceCoordinate(cmt.getStartLine(), cmt
					.getStartColumn());
			for (int j = 0; j < fields.length; j++) {
				SourceCoordinate fieldSC = fields[j].getNameStart();
				if (commentSC.compareTo(fieldSC) > 0)
					if (getCommaRight(cu, commentSC).equals(getCommaRight(cu, fieldSC)))
						if (m.containsKey(fields[j]))
							m.put(fields[j], cmt);
						else
							m.put(fields[j], cmt);
				if (commentSC.compareTo(fieldSC) < 0)
					if (getCommaLeft(cu, commentSC).equals(getCommaLeft(cu, fieldSC)))
						m.put(fields[j], cmt);
			}
		}

		return m;
	}

	private SourceCoordinate getCommaRight(CompilationUnit cu, SourceCoordinate sc) {
		boolean inComment = false;
		int i, j = 0;

		for (i = sc.getLine(); i <= cu.getSource().getLineCount(); i++) {
			String s = cu.getSource().getContentOfLine(sc.getLine());
			if (i == sc.getLine())
				j = sc.getColumn();
			else
				j = 0;
			for (; j < s.length(); j++) {
				if (j < s.length() - 1 && s.substring(j, j + 2).equals("/*"))
					inComment = true;
				if (j < s.length() - 1 && s.substring(j, j + 2).equals("*/"))
					inComment = false;
				if (s.charAt(j) == ',' && !inComment)
					return new SourceCoordinate(i, j);
			}
		}

		return new SourceCoordinate(i, j);
	}

	private SourceCoordinate getCommaLeft(CompilationUnit cu, SourceCoordinate sc) {
		boolean inComment = false;
		int i, j = 0;

		for (i = sc.getLine(); i >= 0; i--) {
			String s = cu.getSource().getContentOfLine(sc.getLine());
			if (i == sc.getLine())
				j = sc.getColumn() - 1;
			else
				j = s.length() - 1;
			for (; j >= 0; j--) {
				if (j < s.length() - 1 && s.substring(j, j + 2).equals("*/"))
					inComment = true;
				if (j < s.length() - 1 && s.substring(j, j + 2).equals("/*"))
					inComment = false;
				if (s.charAt(j) == ',' && !inComment)
					return new SourceCoordinate(i, j);
			}
		}

		return new SourceCoordinate(i, j);
	}

	private Comment getLineEndComment(BinFieldDeclaration decl) {
		List comments = decl.getCompilationUnit().getSimpleComments();
		Comment c;

		for (int i = 0; i < comments.size(); i++) {
			c = (Comment) comments.get(i);
			if (decl.getEndLine() == c.getEndLine()
					&& decl.getEndColumn() < c.getStartColumn()) {
				return c;
			}
		}

		return null;
	}
}
