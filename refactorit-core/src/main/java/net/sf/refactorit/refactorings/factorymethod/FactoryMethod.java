/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.factorymethod;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TargetIndexer;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.MemberVisibilityAnalyzer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.ConfirmationTreeTableModel;
import net.sf.refactorit.source.edit.ASTReplacingEditor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.TypeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Sander M?gi
 * @author Anton Safonov
 */
public class FactoryMethod extends AbstractRefactoring {
  public static String key = "refactoring.factorymethod";

  BinConstructor cnstr;

  private String methodName;
  private BinClass hostingClass;
  private boolean optimizeVisibility;

  private List invocations;
  private List extraUsageTypes;

  public FactoryMethod(BinConstructor cnstr, RefactorItContext context) {
    super("Factory Method", context);

    this.cnstr = cnstr;
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();

    BinCIType owner = this.cnstr.getOwner().getBinCIType();

    if (owner.isInnerType() && !owner.isStatic()) {
      status.addEntry(
          "Can not create factory method for nonstatic inner classes",
          RefactoringStatus.ERROR);
    } else if (owner.isAbstract()) {
      status.addEntry(
          "Can not create factory method for abstract classes",
          RefactoringStatus.ERROR);
    }

    // getInvocations();

    return status;
  }

  /** Run {@link #checkPreconditions} before calling this */
  public List getInvocations() {
    if (this.invocations == null) {
      final ManagingIndexer supervisor = new ManagingIndexer();
      FactoryMethodAnalyzer analyzer
          = new FactoryMethodAnalyzer(supervisor, cnstr);

      // running with GUI
      try {
        JProgressDialog.run(getContext(), new Runnable() {
          public void run() {
            supervisor.visit(cnstr.getProject());
          }
        }, true);
      } catch (SearchingInterruptedException ex) {
      }

      this.invocations = supervisor.getInvocations();
      this.extraUsageTypes = analyzer.getExtraUsageTypes();
    }

    return this.invocations;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();

    // TODO check new method name

    return status;
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    ConfirmationTreeTableModel model =
        new ConfirmationTreeTableModel(cnstr, getInvocations());

    List allUsages = model.getCheckedUsages();

    List usages = new ArrayList();

    if (allUsages.size() > 0) {
      model = (ConfirmationTreeTableModel)
          DialogManager.getInstance().showConfirmations(getName(), getContext(), model,
          "Select places to replace constructor ", "refact.factory_method");
      if (model == null) {
        // User cancelled rename process
        transList.getStatus().addEntry("", RefactoringStatus.CANCEL);
        return transList;
      }

      usages = model.getCheckedUsages();
    }

    Map sourceMaps = new HashMap();
    for (int i = 0; i < usages.size(); ++i) {
      InvocationData id = (InvocationData) usages.get(i);
      CompilationUnit sf = id.getCompilationUnit();
      HashMap sfMap = (HashMap) sourceMaps.get(sf);
      if (sfMap == null) {
        sfMap = new HashMap();
        sourceMaps.put(sf, sfMap);
      }
      handleReplacements(sfMap, id);
      //sfMap.put( id.getAst(), new String[] {"new", "/*REPLACED*/new"} );
    }

    for (Iterator i = sourceMaps.keySet().iterator(); i.hasNext(); ) {
      CompilationUnit compilationUnit = (CompilationUnit) i.next();
      transList.add(new ASTReplacingEditor(compilationUnit,
          (Map) sourceMaps.get(compilationUnit)));
    }

    if (optimizeVisibility) {
      MemberVisibilityAnalyzer analyzer = new MemberVisibilityAnalyzer(cnstr);
      int optimalVisibility = analyzer.getPosterioriFieldAccess(allUsages,
          usages);
      optimalVisibility = analyzer.checkLocation(hostingClass.getTypeRef(),
          optimalVisibility);
      for (int i = 0; i < this.extraUsageTypes.size(); i++) {
        BinTypeRef extraUsageType = (BinTypeRef)this.extraUsageTypes.get(i);
        optimalVisibility = analyzer.checkLocation(extraUsageType,
            optimalVisibility);
      }

      if (BinModifier.getPrivilegeFlags(cnstr.getModifiers())
          != optimalVisibility) {
        transList.add(
            new ModifierEditor(cnstr,
            BinModifier.setFlags(cnstr.getModifiers(), optimalVisibility)));
      }
    }

    // FIXME use BinCIType.findNewMemberPosition
    int column = 0;
    int line = 0;
    if (hostingClass == cnstr.getOwner().getBinCIType()) {
      line = cnstr.getEndLine() + 1;
    } else {
      line = hostingClass.getEndLine();
    }

    // FIXME use BinTypeFormatter and BinMethodFormatter!!!
    String insertIdent = FormatSettings.getIndentString(
        FormatSettings.getBlockIndent());
    for (BinTypeRef outer = hostingClass.getOwner(); outer != null;
        outer = outer.getBinCIType().getOwner()) {
      insertIdent += FormatSettings.getIndentString(FormatSettings.
          getBlockIndent());
    }

    StringInserter inserter =
        new StringInserter(hostingClass.getCompilationUnit(),
        line,
        column,
        createFactoryMethodBody(insertIdent));
    transList.add(inserter);

    return transList;
  }

  private String createFactoryMethodBody(String insertIdent) {

    /*String name1 = methodName;
    BinParameter param[] = cnstr.getParameters();
    int modifiers = BinModifier.PUBLIC | BinModifier.STATIC;
    BinTypeRef returnType = cnstr.getOwner();
    BinMethod.Throws[] throwses = cnstr.getThrows();
    BinMethod factoryMethod = new BinMethod(name1, param, returnType,
        modifiers, throwses);



    BinStatementList statementList = new BinStatementList(statements, null);


    BinMethodBodyStatement bodyStatement = new BinMethodBodyStatement(
        statementList, method, node);


    BinMethodFormatter formatter = new BinMethodFormatter(factoryMethod);
    String str = formatter.formWhole();

    System.out.println(str);*/

    //factoryMethod.setBodyStatement(new BinMethodBodyStatement(statementList, factoryMethod))

    //String result = FormatSettings.LINEBREAK + insertIdent + str;
    String result = FormatSettings.LINEBREAK + insertIdent + "public static ";
    result += getCorrectTypeName(cnstr.getOwner());
    result += " " + methodName;
    if (FormatSettings.isSpaceBeforeParenthesis()) {
      result += " ";
    }
    result += "(";
    BinParameter params[] = cnstr.getParameters();
    ArrayList usedNames = new ArrayList(params.length);

    for (int i = 0; i < params.length; ++i) {
      if (i != 0) {
        result += ", ";
      }
      result += getCorrectTypeName(params[i].getTypeRef()) + " ";
      String name = params[i].getName();
      if (name != null) {
        result += name;
      } else {
        name = "p" + i; // fixme - conflict check
        for (int a = 0; a < 10; ++a) {
          if (usedNames.contains(name)) {
            name = "p" + name;
          } else {
            break;
          }
        }
      }
      usedNames.add(name);
    }
    result += ")";
    BinMethod.Throws[] exc = cnstr.getThrows();
    for (int i = 0; i < exc.length; ++i) {
      if (i == 0) {
        result += " throws ";
      } else {
        result += ", ";
      }
      result += getCorrectTypeName(exc[i].getException());
    }

    //
    //
    if (FormatSettings.isNewlineBeforeBrace()) {
      result += FormatSettings.LINEBREAK + insertIdent;
    } else {
      result += " ";
    }

    result += "{" + FormatSettings.LINEBREAK;
    result += insertIdent
        + FormatSettings.getIndentString(FormatSettings.getBlockIndent())
        + "return new ";
    result += getCorrectTypeName(cnstr.getOwner()) + "(";
    for (int i = 0; i < usedNames.size(); ++i) {
      if (i != 0) {
        result += ", ";
      }
      result += usedNames.get(i);
    }
    result += ");";
    result += FormatSettings.LINEBREAK + insertIdent + "}"
        + FormatSettings.LINEBREAK;

    return result;
  }

  private void handleReplacements(Map aMap, InvocationData id) {
    BinCIType invokedIn = id.getWhereType().getBinCIType();

    String shortestName = TypeUtil.getShortestUnderstandableName(hostingClass,
        invokedIn);
    String replaceTo = null;
    if ("".equals(shortestName)) {
      replaceTo = methodName;
    } else {
      replaceTo = shortestName + "." + methodName;
    }

    ASTImpl newNode = id.getWhereAst();
    ASTImpl[] nameNodes = ASTUtil.getWithSubtree((ASTImpl) newNode.
        getFirstChild());

    aMap.put(newNode, new String[] {
        "new", replaceTo, " ", "\t"});
    for (int i = 0; i < nameNodes.length; ++i) {
      aMap.put(nameNodes[i], new String[] {
          null, ""});
    }
  }

  private String getCorrectTypeName(BinTypeRef aTypeRef) {
    if (aTypeRef.isPrimitiveType()) {
      return aTypeRef.getName();
    }

    if (aTypeRef.isArray()) {
      BinArrayType at = (BinArrayType) aTypeRef.getBinType();
      return getCorrectTypeName(at.getArrayType()) + at.getDimensionString();
    }

    String name = TypeUtil.getShortestUnderstandableName(
        aTypeRef.getBinCIType(), hostingClass);
    if ("".equals(name)) {
      name = aTypeRef.getName();
    }

    return name;
  }

  /**
   * TODO: add also support for determining the minimum access modifiers for
   * constructor
   * actually, it is already done now in performChange, why it should be changed???
   */
  class FactoryMethodAnalyzer extends TargetIndexer {

    /** types where can't be replaced (usually subtypes), but affects future
     * access */
    List extraUsageTypes = new ArrayList();

    public FactoryMethodAnalyzer(ManagingIndexer supervisor,
        BinConstructor target) {
      super(supervisor, target, null);
    }

    public void visit(BinConstructorInvocationExpression expression) {
      BinConstructor invoked = expression.getConstructor();

      if (invoked == getTarget()) {
        CollectionUtil.addNew(this.extraUsageTypes, getSupervisor().getCurrentType());
      }

      super.visit(expression);
    }

    public void visit(BinNewExpression expression) {
      BinConstructor invoked = expression.getConstructor();

      if (getTarget() == invoked) {
        // NOTE even if new is anonymous class of target class it will be
        // checked on visit above

        getSupervisor().addInvocation(invoked,
            getSupervisor().getCurrentLocation(),
            expression.getRootAst(), expression);
      }

      super.visit(expression);
    }

    public List getExtraUsageTypes() {
      return this.extraUsageTypes;
    }
  }


  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public void setHostingClass(final BinClass hostingClass) {
    this.hostingClass = hostingClass;
  }

  public void setOptimizeVisibility(final boolean optimizeVisibility) {
    this.optimizeVisibility = optimizeVisibility;
  }


  public String getDescription() {
    return "Create factory method " + methodName + "() for " + cnstr.getName();
  }

  public String getKey() {
    return key;
  }

}
