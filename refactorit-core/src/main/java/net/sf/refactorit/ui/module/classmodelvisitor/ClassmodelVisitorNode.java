/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.classmodelvisitor;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class ClassmodelVisitorNode extends BinTreeTableNode {
  private String name = null;
  private static final String IDENT_COLOR = "#D00000";

  public ClassmodelVisitorNode(Object bin) {
    super(bin);
  }

  public String getDisplayName() {
    if (this.name == null) {
      if (getBin() instanceof String) {
        this.name = (String) getBin();
      } else {
        this.name = ClassUtil.getShortClassName(getBin());

        if (getBin() instanceof BinConstructor) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + ((BinMember) getBin()).getName()
              + "</FONT>"
              + (((BinConstructor) getBin()).isSynthetic() ? " - synthetic"
              : "");
        } else if (getBin() instanceof BinMethod || getBin() instanceof BinType) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + ((BinMember) getBin()).getName()
              + "</FONT>";
        } else if (getBin() instanceof BinVariable) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + fieldToString((BinVariable) getBin())
              + "</FONT>";
        } else if (getBin() instanceof BinFieldInvocationExpression) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + fieldToString(((BinFieldInvocationExpression) getBin()).getField())
              + "</FONT>";
        } else if (getBin() instanceof BinVariableUseExpression) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + fieldToString(((BinVariableUseExpression) getBin()).getVariable())
              + "</FONT>";
        } else if (getBin() instanceof BinMethodInvocationExpression) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + ((BinMethodInvocationExpression) getBin()).getMethod().getName()
              + "</FONT>"
              + ", " + BinFormatter.formatNotQualified(((BinMethodInvocationExpression) getBin()).getReturnType())
              + ", " + ((BinMethodInvocationExpression) getBin()).getMethod()
              .getOwner().getName();
          this.name += " - "
              + ((BinMethodInvocationExpression) getBin()).getMethod().getName();
        } else if (getBin() instanceof BinCastExpression) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + BinFormatter.formatNotQualified(((BinCastExpression) getBin()).getReturnType())
              + "</FONT>";
        } else if (getBin() instanceof BinNewExpression) {
          this.name = this.name
              + " - <FONT color='" + IDENT_COLOR + "'>"
              + BinFormatter.formatNotQualified(((BinNewExpression) getBin()).getTypeRef())
              + "</FONT>";
        } else if (getBin() instanceof BinLiteralExpression) {
          this.name = this.name
              + " - <FONT color=\"#600000\">"
              + ((BinLiteralExpression) getBin()).getLiteral()
              + "</FONT>";
          if (((BinLiteralExpression) getBin()).getReturnType() != null) {
            this.name += ", "
                + BinFormatter.formatNotQualified(
                ((BinLiteralExpression) getBin()).getReturnType());
          }
        } else if (getBin() instanceof BinExpression) {
          try {
            this.name = this.name
                + " - <FONT color='" + IDENT_COLOR + "'>"
                + BinFormatter.formatNotQualified(((BinExpression) getBin()).getReturnType())
                + "</FONT>";
          } catch (NullPointerException x) {
            // BinEmptyExpression causes it
            //System.err.println("NPE:"+getBin());
          }
        }
      }

      if (getBin() instanceof BinSourceConstruct) {
        this.name += " (" + ((BinSourceConstruct) getBin()).getRootAst() + ")";
//        this.name += ", parent: " + ((BinSourceConstruct) getBin()).getParent();
      }

      if (getBin() instanceof BinTypeRefManager) {
        TypeRefCollector collector = new TypeRefCollector();
        ((BinTypeRefManager) getBin()).accept(collector);
        this.name += ", " + collector.getTypeRefs();
      }

      this.name = HtmlUtil.styleBody(this.name, (Font) null);
    }

    return this.name;
  }

  class TypeRefCollector extends BinTypeRefVisitor {
    private final ArrayList typeRefs = new ArrayList(3);

    public TypeRefCollector() {
      setCheckTypeSelfDeclaration(true);
      setIncludeNewExpressions(true);
    }

    public List getTypeRefs() {
      return this.typeRefs;
    }

    public void visit(BinTypeRef typeRef) {
      this.typeRefs.add(typeRef);
      super.visit(typeRef);
    }
  }

  private String fieldToString(BinVariable variable) {
    return variable.getName() + ", "
        + (variable.isLocalVariable() ? "local" : "member");
  }
}
