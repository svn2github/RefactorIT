/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.query.text.PackageQualifiedNameIndexer;
import net.sf.refactorit.query.text.QualifiedNameIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.InvocationTreeTableNode;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.NonJavaTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.SwingUtilities;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * TreeTableModel for JTreeTable component
 * Also creates root node for the JTreeTable.
 * Contains low-level code for the retrieving of all BinItems.
 *
 * @author  Vladislav Vislogubov
 * @author  Anton Safonov
 */
public class WhereModel extends BinTreeTableModel {

  /** we couldn't show message right here, because model is being created within
    {@link net.sf.refactorit.ui.JProgressDialog}, so:<UL>
    <LI>null - show panel
    <LI>!null && length == 0 - do nothing
    <LI>!null && length > 0 - 1 - title, 2 - message</UL>*/
  private String[] message = null;

  //private Runtime runtime = Runtime.getRuntime();

  String[] getMessage() {
    return this.message;
  }

  WhereModel(RefactorItContext context, Object bin, SearchFilter filter) {
    super(new BinTreeTableNode(
        ((bin instanceof Object[]) ? ((Object[]) bin)[0] : bin), false));

    boolean isArray = (bin instanceof Object[]);
    String name = "";
    if (isArray) {
      Object[] data = (Object[]) bin;
      if (data.length > 7) {
        name = "Many Items";
      } else {
        for (int i = 0; i < data.length; i++) {
          if (i > 0) {
            name += ", ";
          }
          name += getBinName(data[i]);
        }
      }
    } else {
      name = getBinName(bin);
    }

    BinTreeTableNode root = (BinTreeTableNode) getRoot();
    root.setDisplayName(name + (isArray ? " are used at" : " is used at:"));

    initChildren(context, root, bin, filter);

    root.sortAllChildren();
    root.reflectLeafNumberToParentName();

    /*    runtime.runFinalization();
        runtime.gc();
        runtime.gc();
        runtime.gc();

        int totalMemory =
            (int) (runtime.totalMemory() / 1024);
        int usedMemory =
            totalMemory - (int) (runtime.freeMemory() / 1024);
        System.err.println(usedMemory + "kB / " + totalMemory + "kB");*/

  }

  private String getBinName(Object bin) {
    String name;
    if (bin instanceof BinMemberInvocationExpression) {
      name = ((BinMemberInvocationExpression) bin).getDetails();
    } else if (bin instanceof BinLocalVariable) {
      name = ((BinLocalVariable) bin).getDetails();
    } else if (bin instanceof BinPackage) {
      name = ((BinPackage) bin).getQualifiedName();
    } else if (bin instanceof BinLabeledStatement) {
      name = "Label " + ((BinLabeledStatement)bin).getLabelIdentifierName();
    } else if (bin instanceof BinMember) {
      if (bin instanceof BinMethod.Throws) {
        bin = ((BinMethod.Throws) bin).getException().getBinCIType();
      } else if (bin instanceof BinThrowStatement) {
        bin = ((BinThrowStatement) bin).getExpression().getReturnType().
            getBinType();
      }

      name = BinFormatter.format(bin);
      if (!(bin instanceof BinConstructor)) {
        if (((BinMember) bin).getOwner() != null) {
          name = ((BinMember) bin).getOwner().getName() + '.' + name;
        }
      }
    } else {
      if (bin != null) {
        AppRegistry.getLogger(this.getClass()).debug(
            "Unhandled bin: " + bin + " - " + bin.getClass());
        name = bin.toString();
      } else {
        AppRegistry.getLogger(this.getClass()).debug("Unhandled bin: null");
        name = "<unknown>";
      }
    }

    return name;
  }

  public int getColumnCount() {
    return 3;
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Location";
      case 1:
        return "Line";
      case 2:
        return "Source";
      default:
        return null;
    }
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {
    if (node instanceof ParentTreeTableNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          String lineNumber = ((ParentTreeTableNode) node).getLineNumber();
          return lineNumber + NUMBER_PADDING;
        case 2:
          return ((ParentTreeTableNode) node).getLineSource();
      }
    }

    return null;
  }

  public Class getColumnClass(int col) {
    switch (col) {
      case 1:
        return Integer.class;
      case 2:
        return String.class;
      default:
        return super.getColumnClass(col);
    }
  }

  private void initChildren(final RefactorItContext context,
      BinTreeTableNode root,
      Object bin, SearchFilter filter) {
    List datas = null;

    datas = Finder.getInvocations(context.getProject(), bin, filter);
    datas = filter.filter(datas, context.getProject());

    if (filter.isGoToSingleUsage() && datas.size() <= 1) {
      this.message = new String[2];
      if (datas.size() == 0) {
        this.message[0] = "Not used";
        this.message[1] = "No usages found";
      } else {
        final InvocationData invocationData = ((InvocationData) datas.get(0));
        final int line = invocationData.getLineNumber();
        final CompilationUnit source = invocationData.getCompilationUnit();
        if (source != null && line > 0) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              context.show(source, line,
                  GlobalOptions.getOption("source.selection.highlight").equals("true"));
            }
          });
        } else {
          // shouldn't get here!
          this.message[0] = "Source not available";
          this.message[1] = "Can not go to the file outside of the source path";
        }
      }
    } else {
      appendNodes(root, filter, datas, bin);
    }
    datas.clear();
    initNonJavaNodes(context, root, bin, filter);
  }

  private void initNonJavaNodes(final RefactorItContext context,
      BinTreeTableNode root,
      Object bin, SearchFilter filter) {
    if (filter.isSearchNonJavaFiles()) {
      List results = getNonJavaOccurrences(context, filter, bin);
      if (results.size() > 0) {
        BinTreeTableNode parent
            = new BinTreeTableNode("Occurrences in non-java files", false);
        root.addChild(parent);
        for (Iterator i = results.iterator(); i.hasNext(); ) {
          Occurrence o = (Occurrence) i.next();
          parent.addChild(new NonJavaTreeTableNode(o));
        }
      }
    }
  }

  private List getNonJavaOccurrences(
      final RefactorItContext context,
      final SearchFilter filter, final Object bin
      ) {
    ManagingNonJavaIndexer supervisor = new ManagingNonJavaIndexer(
        context.getProject().getOptions().getNonJavaFilesPatterns());

    if (bin instanceof BinPackage) {
      new PackageQualifiedNameIndexer(supervisor, (BinPackage) bin,
          QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH, false);
      supervisor.visit(context.getProject());
      return supervisor.getOccurrences();
    }

    if (bin instanceof BinMember) {
      String qualifiedName = ((BinMember) bin).getQualifiedName();
      new QualifiedNameIndexer(supervisor, qualifiedName, 
          QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH);
      supervisor.visit(context.getProject());
      return supervisor.getOccurrences();
    }

    return Collections.EMPTY_LIST;
  }

  private void appendNodes(final BinTreeTableNode root,
      final SearchFilter filter,
      final List datas, final Object bin) {

    for (int i = 0, max = datas.size(); i < max; i++) {
      final InvocationData data = (InvocationData) datas.get(i);

      BinTreeTableNode node = null;
      Object item = data.getWhere();

      if (item instanceof BinCIType) {
        node = (BinTreeTableNode) root.findParent(((BinCIType) item).getPackage(), true);
      } else if (item instanceof BinMember) {
        if (Assert.enabled) {
          Assert.must(((BinMember) item).getOwner() != null,
              "Owner of " + ((BinMember) item).getName() + " is null, class: " +
              item.getClass().getName());
        }
        node = (BinTreeTableNode) root.findParent(((BinMember) item).getOwner(), true);
      } else if (item instanceof BinTypeRef) {
        node = (BinTreeTableNode) root.findParent((BinTypeRef) item, true);
        item = ((BinTypeRef) item).getBinType();
      } else if (item instanceof CompilationUnit) {
        final BinPackage pack = ((CompilationUnit) item).getPackage();
        final List independentTypes
            = ((CompilationUnit) item).getIndependentDefinedTypes();
        if (independentTypes.size() == 1) {
          node = (BinTreeTableNode) root.findParent(
              (BinTypeRef) independentTypes.get(0), true);
        } else if (pack != null) {
          node = (BinTreeTableNode) root.findParent(pack, true);
        } else {
          node = root;
        }
      } else {
        Assert.must(false, "Unsupported Location in WhereModel: "
            + item.getClass().getName());
      }

      CompilationUnit source = data.getCompilationUnit();

      if (source == null) {
        Assert.must(false, "Unsupported item in WhereModel: "
            + item.getClass().getName());
      }

      InvocationTreeTableNode bn = null;
      if (!filter.isShowDuplicates()) {
//System.err.println("Item: " + item);
        bn = (InvocationTreeTableNode) node.findChildByType(item,
            data.getLineNumber());
        if (bn == null && item instanceof BinConstructor) {
          bn = (InvocationTreeTableNode) node.findChildByType(
              ((BinConstructor) item).getOwner().getBinCIType(),
              data.getLineNumber());
        }
      }

      if (bn == null) {
        ASTImpl ast = data.getWhereAst();

        // For "import package.*" -- turns the "." into "package" for syntax highlighting.
        if (ast.getFirstChild() != null
            && ast.getFirstChild().getNextSibling() != null
            && ast.getFirstChild().getNextSibling().getType()
            == JavaTokenTypes.STAR) {
          ast = new CompoundASTImpl((ASTImpl) ast.getFirstChild());
        }

        bn = new InvocationTreeTableNode(item);
        bn.addAst(ast);
        bn.setSourceHolder(source);
        bn.setInvocationData(data);
        node.addChild(bn);
      } else {
        bn.addAst(data.getWhereAst());
        bn.addInvocationData(data);
      }
    }
  }
}
