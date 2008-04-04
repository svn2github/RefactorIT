/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.inter;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.refactorings.InternationalUtils;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.ArrayList;
import java.util.Iterator;


public class InterAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.InterAction";
  public static final String NAME = "Internationalization";

  public boolean isAvailableForType(Class type) {
    return Project.class.equals(type)
      || BinPackage.class.equals(type)
      || BinCIType.class.isAssignableFrom(type)
      || BinConstructor.class.isAssignableFrom(type)
      || BinMethod.Throws.class.equals(type)
      || BinThrowStatement.class.equals(type);
  }

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public String getName() {
    return NAME;
  }

  public boolean isReadonly() {
    return true;
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, final Object object) {
    final Object target = RefactorItActionUtils.unwrapTarget(object);

    try {
      final ArrayList results = new ArrayList();

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          final CompilationUnit visitables[] = ModuleManager.getCompilationUnits(target,
              context);
          LiteralSearchVisitor visitor = new LiteralSearchVisitor(results);

          for (int i = 0; i < visitables.length; ++i) {
            visitor.visit(visitables[i]);
          }
        }
      }, true);

      BinTreeTableNode rootNode = new BinTreeTableNode(
          "Internationalization report");

      BinTreeTableNode listRootNode = new BinTreeTableNode(
          "Strings to internationalize");
      for (int i = 0; i < results.size(); ++i) {
        BinLiteralExpression aLiteral = (BinLiteralExpression) results.get(i);
        ParentTreeTableNode parentNode = listRootNode.findParent(aLiteral.
                getOwner(), true);
        BinTreeTableNode childNode = new BinTreeTableNode(aLiteral.getLiteral());
        CompilationUnit childSource = aLiteral.getCompilationUnit();
        ASTImpl childAst = aLiteral.getRootAst(childSource);
        childNode.setSourceHolder(childSource);
        childNode.addAst(childAst);
        childNode.setLine(childAst.getLine());
        parentNode.addChild(childNode);
      }

      listRootNode.sortAllChildren();
      listRootNode.reflectLeafNumberToParentName();

      BinTreeTableNode duplicateRootNode = new BinTreeTableNode(
          "Duplicate literals report (with filters)");
      InternationalUtils.duplicateLiteralsReport(results, duplicateRootNode);

      duplicateRootNode.reflectLeafNumberToParentName();

      rootNode.addChild(listRootNode);
      rootNode.addChild(duplicateRootNode);

      BinTreeTableModel model = new BinTreeTableModel(rootNode) {};
      BinTreeTable table = new BinTreeTable(model, context);
      ResultArea binComp = ResultArea.create(table, context, this);

      binComp.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(context, getName(), binComp);
      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.inter");
    } catch (SearchingInterruptedException ex) {
    }

    return false;
  }
}


class LiteralSearchVisitor extends AbstractIndexer {
  FastJavaLexer specialLexer = new FastJavaLexer("");

  ArrayList results;

  LiteralSearchVisitor(ArrayList results) {
    this.results = results;
  }

  public void visit(BinIfThenElseStatement ifStmt) {
    final StringBuffer fieldNameList = new StringBuffer();
    AbstractIndexer fieldSearcher = new AbstractIndexer() {
      public void visit(BinFieldInvocationExpression inv) {
        BinField field = inv.getField();
        fieldNameList.append("|" + field.getOwner().getName() + "."
            + field.getName() + "|");
        super.visit(inv);
      }
    };

    ifStmt.getCondition().accept(fieldSearcher);

    // N.B! we just check for the existance of condition and assume that all tests are
    // if (isDebugMode)
    // and never as (if!isDebugMode)
    String fnL = fieldNameList.toString();
    if (fnL.indexOf("RebuildLogic.debug") != -1 ||
        fnL.indexOf("ExtractMethodAnalyzer.showDebugMessages") != -1 ||
        fnL.indexOf("Assert.enabled") != -1 ||
        fnL.indexOf("Settings.debugLevel") != -1 ||
        fnL.indexOf("SourceEditor.debug") != -1 ||
        fnL.indexOf("Line.debug") != -1 ||
        fnL.indexOf("LocalSource.debug") != -1 ||
        fnL.indexOf("MemoryTrack.isMemoryDebugMode") != -1) {
      BinStatementList falseList = ifStmt.getFalseList();
      if (falseList != null) {
        falseList.accept(this);
      }
    } else {
      super.visit(ifStmt);
    }

  }

  public void visit(BinMethodInvocationExpression inv) {
    BinMethod method = inv.getMethod();
    // we skip all mains
    if (method.getName().equals("main")) {
      return;
    }

    BinTypeRef invokedOn = method.getOwner();

    if ("org.apache.log4j.Category".equals(invokedOn.getQualifiedName())) {
      return;
    }

    super.visit(inv);
  }

  public void visit(BinCIType type) {
    // we skip all the test cases and Assert.java
    String typeName = type.getQualifiedName();
    if (typeName.startsWith("test.")) {
      return;
    }
    if (typeName.indexOf("JavaRecognizer") != -1) {
      return;
    }
    if (typeName.indexOf("JavaLexer") != -1) {
      return;
    }
    if (typeName.indexOf("BinItemReference") != -1) {
      return;
    }
    if (typeName.indexOf("JavaDoc") != -1) {
      return;
    }
    if (typeName.indexOf("TypeInfoJavadoc") != -1) {
      return;
    }
    if (typeName.indexOf("StringUtil") != -1) {
      return;
    }

    Iterator superTypes = type.getTypeRef().getAllSupertypes().iterator();
    while (superTypes.hasNext()) {
      BinTypeRef superTypeRef = (BinTypeRef) superTypes.next();
      if (superTypeRef.getName().indexOf("TestCase") != -1) {
        return;
      }
    }

    super.visit(type);
  }

  public void visit(BinLiteralExpression literal) {
    String asString = literal.getLiteral();
    if (asString.startsWith("\"")) {
      asString = asString.substring(1, asString.length() - 1);

      if (!specialLexer.isLiteral(asString) &&
          !asString.startsWith("java.lang") &&
          !"\\n".equals(asString) &&
          !(StringUtil.replace(asString, " ", "").length() == 0) &&
          !"(".equals(asString) &&
          !")".equals(asString) &&
          !"$".equals(asString) &&
          !".".equals(asString) &&
          !",".equals(asString) &&
          !"[]".equals(asString) &&
          !asString.startsWith("tree.") &&
          !asString.startsWith("source.") &&
          !asString.startsWith("message.") &&
          !asString.startsWith("button.") &&
          !asString.startsWith("update.") &&
          !asString.startsWith("window.")) {
        results.add(literal);
        return;
      }
    }
    super.visit(literal);
  }
}
