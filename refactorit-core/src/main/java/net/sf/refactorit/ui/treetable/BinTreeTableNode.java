/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.audit.AuditRootNode;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.tree.UITreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Node for the BinTreeTable component with binary node, source and line.<br>
 *
 * @author  Anton Safonov
 */
public class BinTreeTableNode extends ParentTreeTableNode {
  public static final NodeComparator nodeComparator = new NodeComparator();

  /**
   * true - show line number and source line for item
   */
  private boolean showSource;

  protected String name;
  private String text;

  /** String for root/subbranches and BinXXX for others */
  private Object bin;

  // FIXME: crazy, crazy to hold these all the time in all nodes!!!
  private Object binItemReference;
  private BinItemReference compilationUnitReference;

  /* Cached variables */
  private int type = -1;
  private String source; // source line containing binary call or defenition
  private int iLine = -1;
  private SourceHolder sourceHolder;
  private ArrayList asts;

  private static final String EMPTY_STRING = "";

  public final boolean isShowSource() {
    return this.showSource;
  }

  /**
   * Shows the source line of the BinXXX by default except for String
   *
   * @param bin content, could be String or BinXXX
   */
  public BinTreeTableNode(final Object bin) {
    this(bin, !(bin instanceof String || bin instanceof BinPackage));
  }

  /**
   * @param bin content, could be String or BinXXX
   * @param showSource defines if to show source line and number
   */
  public BinTreeTableNode(final Object bin, final boolean showSource) {
    this.bin = bin;
    if (Assert.enabled) {
      Assert.must(this.bin != null, "Bin shouldn't be null in BinTreeTableNode");
    }
    this.showSource = showSource;

//    if (bin instanceof TreeASTImpl) {
//      final StringWriter sw = new StringWriter();
//      new Exception("Leak trace").printStackTrace(new PrintWriter(sw));
//      final String message = sw.getBuffer().toString();
//      if (message.indexOf("module.classmodelvisitor") < 0) {
//        System.err.println("MARKED MEMORY LEAK HERE!!! PLEASE REPORT THE TRACE");
//        System.err.println(message);
//      }
//    }
  }

  /**
   * @return line number of binary declaration.
   */
  public String getLineNumber() {
    if (!showSource) {
      return EMPTY_STRING;
    }

    final String strLine;
    final int line = queryLineNumber();
    if (line > 0) {
      strLine = new Integer(line).toString();
    } else {
      strLine = EMPTY_STRING;
    }

    return strLine;
  }

  public final void setLine(final int iLine) {
    this.iLine = iLine;
  }

  public final void setSourceHolder(final SourceHolder sourceHolder) {
    if (sourceHolder == null){
      setShowSource(false);
    }
    this.sourceHolder = sourceHolder;
  }

  public final void setLineSource(final String source) {
    this.source = source;
  }


  /**
   * @return line number of binary declaration.
   */
  public int queryLineNumber() {
    if (this.iLine != -1) {
      return this.iLine;
    }

    final Object bin = getBin();
    if (bin instanceof BinPackage || bin instanceof String) {
      this.iLine = 0;
      return this.iLine;
    }

    if (bin instanceof BinSelection) {
      this.iLine = ((BinSelection) bin).getStartLine();
      return this.iLine;
    }

    final ArrayList asts = getAstsImpl();

    if (asts != null && asts.size() > 0 && asts.get(0) != null) {
      Object ast = asts.get(0);
      if (ast instanceof Integer) {
        this.iLine = getSource().getSource().getASTTree().getAstStartLine(
            ((Integer) ast).intValue());
      } else {
        this.iLine = ((ASTImpl) ast).getLine();
      }
    } else {
//      if (!((bin instanceof BinMethod)
//          && ((BinMethod) bin).isSynthetic())) {
//        System.err.println("BinTreeTableNode#queryLineNumber: "
//            + "Couldn't get AST node for bin: \"" + this + "\" of type: "
//            + bin.getClass().getName());
//      }
      this.iLine = 0;
    }

    return this.iLine;
  }

  /**
   * @return contents of that source code line where the declaration of the BinItem occurs
   *         (in plain text).
   */
  public String getLineSource() {
    if (!showSource) {
      return EMPTY_STRING;
    }

    if (source != null) {
      return source;
    }

    final SourceHolder sf = this.getSource();
    if (sf == null) {
      return EMPTY_STRING;
    }

    int line = queryLineNumber();
    if (line == 0) {
      return EMPTY_STRING;
    }

    String content;
    try {
      content = sf.getSource().getContentString();
      if (line >= sf.getSource().getLineIndexer().getLineCount()) { // last line
        source = content.substring(
            sf.getSource().getLineIndexer().lineColToPos(line, 1));
      } else {
        source = content.substring(
            sf.getSource().getLineIndexer().lineColToPos(line, 1),
            sf.getSource().getLineIndexer().lineColToPos(line + 1, 1) - 1);
      }
    } catch (Exception e) {
      source = ""; // file has gone already or got shorter
      return source;
    }

    int pos = source.length() - 1;
    while (pos >= 0) {
      char ch = source.charAt(pos);
      if (ch == '\r' || ch == '\n') {
        --pos;
      } else {
        break;
      }
    }

    source = source.substring(0, pos + 1);

    if (source.length() == 0) {
      return source;
    }

    // highlight AST nodes

//    System.out.println("Highlight: " + this.source);

    if (false/*noHighlight*/) {
      source = source.trim();
      return source;
    }

    final ArrayList asts = getAstsImpl();
    if (asts.size() == 0) {
      return source;
    }

    // here we expect that:
    // 1) all are either Integer or ASTImpl
    // 2) consequent ASTs have increasing integer index
    Collections.sort(asts);

    final int excess = 52;
    final StringBuffer buf = new StringBuffer(source.length() + excess);
    int firstAstColumn = 0;

    try {
      int end = 0, start;
      for (int i = 0, max = asts.size(); i < max; i++) {
        final Object ast = asts.get(i);
        int column;
        String astText;
        if (ast instanceof Integer) {
          column = getSource().getSource().getASTTree().getAstStartColumn(
              ((Integer) ast).intValue());
          astText = getSource().getSource().getASTTree().getAstText(
              ((Integer) ast).intValue());
        } else {
          column = ((ASTImpl) ast).getColumn();
          astText = ((ASTImpl) ast).getText();
        }

        if (column < 0 || astText == null) {
          AppRegistry.getLogger(this.getClass()).error(
              "Strange AST without text: " + ast + " - " + ast.getClass()
              + ", node: " + this);
          continue;
        }
//System.err.println("AST: " + ast.getText() );

        if (source.indexOf(astText) < 0) {
          return source;
        }

        start = column - 1;
        if (i == 0) {
          firstAstColumn = start;
        }

        buf.append(StringUtil.tagsIntoHTML(source.substring(end, start)));
        buf.append("<FONT color='#C02040'>");
        end = start + astText.length();
//System.err.println("ints: " + start + " - " + end);
        buf.append(source.substring(start, end));
        buf.append("</FONT>");
      }
      buf.append(StringUtil.tagsIntoHTML(source.substring(end)));
    } catch (StringIndexOutOfBoundsException e) {
      AppRegistry.getLogger(this.getClass()).debug("SourceLine: " + source, e);
    }

    content = buf.toString();
    if (content.length() > 80 + 27 * asts.size()) {
      int ind = content.lastIndexOf(' ', firstAstColumn);
      if (ind > 0) {
        if (ind >= 3 && "new".equals(content.substring(ind - 3, ind))) {
          ind -= 3;
        }
        content = content.substring(ind);
      }
    }

    content = content.trim();
    if (!IDEController.runningTest()) {
      source = HtmlUtil.styleText(content, BinTree.getFontProperty());
    }

    return source;
  }

  public ArrayList getAsts() {
    final ArrayList transfer = getAstsImpl();
    final ArrayList result = new ArrayList(transfer.size());

    for (int i = 0, max = transfer.size(); i < max; ++i) {
      final Object o = transfer.get(i);
      if (o instanceof Integer) {
        CollectionUtil.addNew(result,
            getSource().getSource().getASTByIndex(((Integer) o).intValue()));
      } else {
        CollectionUtil.addNew(result, o);
      }
    }

    return result;
  }

  private ArrayList getAstsImpl() {
    if (this.asts == null) {
      this.asts = new ArrayList(1);

      try {
        final Object bin = getBin();
        if (bin instanceof Project) {
          // ignore - it is a project wide node
        } else if (bin instanceof BinTypeRef) {
          addAst(((BinTypeRef) bin).getBinType().getNameAstOrNull());
        } else if (bin instanceof BinMember) {
          addAst(((BinMember) bin).getNameAstOrNull());
        } else if (bin instanceof BinSourceConstruct) {
          addAst(ASTUtil.getFirstNodeOnLine(
              ((BinSourceConstruct) bin).getRootAst()));
        } else if (bin instanceof SourceHolder) {
//          final ASTImpl ast = ((SourceHolder) bin).getSource().getFirstNode();
//          if (ast != null) { // happens when severe parsing errors
//            addAst(ast);
//          }
          if (((SourceHolder) bin).getSource() != null
              && ((SourceHolder) bin).getSource().getASTTree() != null
              && ((SourceHolder) bin).getSource().getASTTree().getASTCount() > 0) {
            addAst(0);
          }
        } else if (bin instanceof UserFriendlyError) {
          // let's make a virtual node
          final ASTImpl stubNode = new SimpleASTImpl();
          stubNode.setLine(((UserFriendlyError) bin).getLine());
          stubNode.setColumn(((UserFriendlyError) bin).getColumn());
          stubNode.setText(((UserFriendlyError) bin).getNodeText());
          addAst(stubNode);
        } else {
          AppRegistry.getLogger(this.getClass()).debug(
              "BinTreeTableNode#getAsts: no AST for: \""
              + bin + "\" of type: " + bin.getClass().getName());
        }
//        this.noHighlight = true; // for auto-generated AST's
      } catch (NullPointerException e) {
        AppRegistry.getExceptionLogger()
            .debug(e, "Failed to get ASTs for: " + getBin(), this);
      }
    }

    return this.asts;
  }

  public final void addAst(final int astIndex) {
    if (this.asts == null) {
      this.asts = new ArrayList(1);
    }
    this.asts.add(new Integer(astIndex));
  }

  public final void addAst(final ASTImpl ast) {
    if (Assert.enabled) {
      final Object bin = getBin();
      if (ast == null && !((bin instanceof BinMethod)
          && ((BinMethod) bin).isSynthetic())) {
        AppRegistry.getLogger(this.getClass()).debug(
            "Adding null AST to BinTreeTableNode, bin: "
            + bin + " of type: " + bin.getClass().getName());
      }
    }

    if (ast == null) {
      return;
    }

    if (asts == null) {
      asts = new ArrayList(1);
    }

    Object addable;

    // there are also some synthetic pseudo asts added here
    int index = ASTUtil.indexFor(ast);
    if (index >= 0) {
      addable = new Integer(index);
    } else {
      addable = ast;
    }

    if (!asts.contains(addable)) {
      asts.add(addable);
    }
  }

  public SourceHolder getSource() {
    if (this.sourceHolder == null) {
      try {
        final Object bin = getBin();
        if (bin instanceof SourceHolder) {
          this.sourceHolder = (SourceHolder) bin;
        } else if (bin instanceof LocationAware) {
          this.sourceHolder = ((LocationAware) bin).getCompilationUnit();
        } else if (bin instanceof BinTypeRef) {
          this.sourceHolder
              = ((BinTypeRef) bin).getBinType().getCompilationUnit();
        }
      } catch (NullPointerException e) {
        AppRegistry.getExceptionLogger()
            .debug(e, "Failed to get source for: " + getBin(), this);
      }
    }
    return this.sourceHolder;
  }

  /**
   * Returns content (BinXXX or String).
   */
  public final Object getBin() {
    return this.bin;
  }

  /**
   * Indicates the type of the node's content.
   * @see #getBin()
   */
  public int getType() {
    if (this.type == -1) {
      Object bin = getBin();
      if (bin instanceof RefactoringStatus.Entry) {
        bin = ((RefactoringStatus.Entry) bin).getBin();
      }

      if (bin instanceof BinPackage) {
        this.type = UITreeNode.NODE_PACKAGE;
      } else if (bin instanceof BinCIType) {
        if (((BinCIType) bin).isClass()) {
          this.type = UITreeNode.NODE_CLASS;
        } else if (((BinCIType) bin).isEnum()) {
          this.type = UITreeNode.NODE_ENUM;
        } else {
          this.type = UITreeNode.NODE_INTERFACE;
        }
      } else if (bin instanceof BinTypeRef) {
        if (((BinTypeRef) bin).isReferenceType()) {
          if (((BinTypeRef) bin).getBinCIType().isClass()) {
            this.type = UITreeNode.NODE_CLASS;
          } else if (((BinTypeRef) bin).getBinCIType().isEnum()) {
            this.type = UITreeNode.NODE_ENUM;
          } else {
            this.type = UITreeNode.NODE_INTERFACE;
          }
        }
      } else if (bin instanceof SourceHolder) {
        this.type = UITreeNode.NODE_SOURCE;
      } else if (bin instanceof BinVariable
          || bin instanceof BinFieldInvocationExpression) {
        this.type = UITreeNode.NODE_TYPE_FIELD;
      } else if (bin instanceof BinConstructor
          || bin instanceof BinInitializer) {
        this.type = UITreeNode.NODE_TYPE_CNSTOR;
      } else if (bin instanceof BinMethod
          || bin instanceof BinMethodInvocationExpression) {
        this.type = UITreeNode.NODE_TYPE_METHOD;
      } else {
        this.type = UITreeNode.NODE_UNKNOWN;
      }
    }

    return this.type;
  }

  public final void setType(final int type) {
    this.type = type;
  }

  public String getDisplayName() {
    if (this.name == null) {
      final Object bin = getBin();
      if (bin instanceof String) {
        this.name = (String) bin;
      } else if (bin instanceof ASTImpl) {
        this.name = ((ASTImpl) bin).toString();
      } else if (bin instanceof SourceHolder) {
        this.name = ((SourceHolder) bin).getName();
      } else if (bin instanceof BinPackage) {
        this.name = ((BinPackage) bin).getQualifiedName();
        final ParentTreeTableNode parent = getParent();
        if (parent != null && parent.getParent() != null // parent is not root
            && parent.isClassmodelType()) {
          String parentFullName = parent.getFullName();
          if (this.name.startsWith(parentFullName)
              && this.name.length() > parentFullName.length()) {
            int pos = parentFullName.length();
            if (this.name.charAt(pos) == '.') {
              ++pos;
              this.name = this.name.substring(pos);
            }
          }
        }
      } else if (bin instanceof BinTypeRef) {
        this.name = BinFormatter.format((BinTypeRef) bin);
        if (((BinTypeRef) bin).getBinType().isLocal()) {
          this.name = ((BinTypeRef) bin).getBinCIType().getLocalPrefix()
              + this.name;
        }
      } else if (bin instanceof BinType) {
        this.name = BinFormatter.format(((BinType) bin).getTypeRef());
        if (((BinType) bin).isLocal()) {
          this.name = ((BinCIType) bin).getLocalPrefix() + this.name;
        }
      } else if (bin instanceof BinMethod) {
        this.name = BinFormatter.format((BinMethod) bin);
      } else if (bin instanceof BinField) {
        this.name = BinFormatter.format((BinField) bin);
      } else if (bin instanceof BinMember) {
        this.name = ((BinMember) bin).getName();
      } else if (bin instanceof BinMethod.Throws) {
        this.name = "throws "
            + ((BinMethod.Throws) bin).getException().getQualifiedName();
      } else if (bin instanceof BinTryStatement
          || bin instanceof BinTryStatement.TryBlock) {
        final BinItem parent = ((BinSourceConstruct) bin).getParentMember();
        if (parent instanceof BinMethod) {
          this.name = BinFormatter.formatQualified(((BinMethod) parent).
              getOwner())
              + '.' + BinFormatter.format((BinMethod) parent)
              + " - try";
        } else {
          this.name = "try";
        }
      } else if (bin instanceof BinTryStatement.CatchClause) {
        this.name = "catch("
            + ((BinTryStatement.CatchClause) bin).getParameter().getTypeRef().
            getQualifiedName()
            + ' '
            + ((BinTryStatement.CatchClause) bin).getParameter().getName()
            + ')';
      } else if (bin instanceof BinThrowStatement) {
        this.name = "throw "
            + BinFormatter.formatQualified(((BinThrowStatement) bin)
            .getExpression().getReturnType());
      } else if (bin instanceof BinSelection) {
        this.name = ((BinSelection) bin).getText();
        if (this.name != null) {
          this.name = this.name.trim();
        }
      } else if (bin instanceof RefactoringStatus.Entry) {
        this.name = ((RefactoringStatus.Entry) bin).getMessage();
      } else if (bin instanceof Object[]) {
        this.name = "";
        for (int i = 0; i < ((Object[]) bin).length; i++) {
          if (i > 0) {
            this.name += ", ";
          }
          this.name += BinFormatter.format(((Object[]) bin)[i]);
        }
      } else if (bin instanceof Project) {
        this.name = "Project";
      } else if (bin instanceof UserFriendlyError) {
        this.name = ((UserFriendlyError) bin).getDescription();
      } else if (bin instanceof AuditRootNode) {
        this.name = ((AuditRootNode) bin).getDisplayName();
      } else {
        AppRegistry.getLogger(this.getClass()).debug("Failed to get name for: "
            + bin + " of type: " + bin.getClass().getName());
        this.name = bin.toString();
      }

      if (this.name == null) {
        this.name = "";
      }
    }

//    System.err.println("Name for: "
//        + bin.getClass().getName() + " - " + this.name);

    return this.name;
  }

  public final void setDisplayName(final String name) {
    this.name = name;
  }

  public final String getSecondaryText() {
    return text;
  }

  public final void setSecondaryText(final String text) {
    this.text = text;
  }

  public static class NodeComparator implements Comparator {
    public int compare(final Object o1, final Object o2) {
      if (!(o1 instanceof BinTreeTableNode) || !(o2 instanceof BinTreeTableNode)) {
        return 0;
      }
      final BinTreeTableNode node1 = (BinTreeTableNode) o1;
      final BinTreeTableNode node2 = (BinTreeTableNode) o2;
//System.err.println("comp1: " + node1.getBin() + " - " + node2.getBin());
//System.err.println("comp2: " + node1.getBin().getClass() + " - " + node2.getBin().getClass());
      int res = 0;

      // FIXME: enhance later to cover all major types of nodes, but VERY carefully!
      boolean isPack1 = node1.getBin() instanceof BinPackage
          || node1.getBin() instanceof String;
      boolean isPack2 = node2.getBin() instanceof BinPackage
          || node2.getBin() instanceof String;
      if (isPack1 && !isPack2) {
        res = -1;
      } else if (!isPack1 && isPack2) {
        res = +1;
      }

      if (res == 0) {
        SourceHolder source1 = node1.getSource();
        if (source1 != null && source1 == node2.getSource()
            && (node1.isShowSource() || node2.isShowSource())) {
          res = node1.queryLineNumber() - node2.queryLineNumber();
        }
      }

      if (res == 0) {
        boolean unknownError1 = node1.getBin() instanceof UserFriendlyError
            && ((UserFriendlyError) node1.getBin()).getLine()
            == UserFriendlyError.UNKNOWN;
        boolean unknownError2 = node2.getBin() instanceof UserFriendlyError
            && ((UserFriendlyError) node2.getBin()).getLine()
            == UserFriendlyError.UNKNOWN;
        if (unknownError1 && !unknownError2) {
          res = -1;
        }
        if (!unknownError1 && unknownError2) {
          res = +1;
        }
      }

      if (res == 0) {
        res = node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
      }

      return res;
    }

//    private final boolean isClassOrFile(final BinTreeTableNode node) {
//      final Object bin = node.getBin();
//      return (bin instanceof BinTypeRef && ((BinTypeRef) bin).isReferenceType())
//          || bin instanceof BinCIType
//          || bin instanceof SourceHolder;
//    }
  }

  public final void sortAllChildren() {
    sortAllChildren(nodeComparator);
  }

  /** Recursive */
  public final void sortAllChildren(final Comparator comparator) {
    final List children = this.getAllChildren();

    Collections.sort(children, comparator);

    for (int i = 0, max = children.size(); i < max; i++) {
      if (children.get(i) instanceof BinTreeTableNode) {
        final BinTreeTableNode child = (BinTreeTableNode) children.get(i);
        if (child.getAllChildrenCount() > 0) {
          child.sortAllChildren(comparator);
        }
      }
    }
    invalidateCacheOfVisibleChildren();
  }

  public final void sortDirectChildren(final Comparator comp) {
    Collections.sort(getAllChildren(), comp);
    invalidateCacheOfVisibleChildren();
  }

  // FIXME it still works wrong in rare cases for subbranches, e.g. inners in Metrics tree
  /** find class node
   * @param parent type to find parent node for
   * @param flatPackages flat packages
   * @return parent node
   */
  public final ParentTreeTableNode findParent(final BinTypeRef parent,
      final boolean flatPackages) {
    if (Assert.enabled) {
      Assert.must(parent != null,
          "Parent is null in BinTreeTableNode.findParent");
    }

    ParentTreeTableNode parentNode = findChild(parent.getBinType().getNameWithLocals(true));
    if (parentNode == null) {
      final ParentTreeTableNode ownerNode;
      if (parent.getBinCIType().isInnerType()) {
        ownerNode = findParent(parent.getBinCIType().getOwner(), flatPackages);
      } else {
        ownerNode = findParent(parent.getPackage(), flatPackages);
      }
      parentNode = new BinTreeTableNode(parent.getBinCIType(), false);
      ownerNode.addChild(parentNode);
    }
    return parentNode;
  }

  /** find package node */
  public final ParentTreeTableNode findParent(final BinPackage parent,
      final boolean flatPackages) {
    ParentTreeTableNode parentNode = findChild(parent.getQualifiedName());
    if (parentNode == null) {
      parentNode = createPackageNode(parent, this, flatPackages);
    }
    return parentNode;
  }

  /**
   * Creates node for the package.
   *
   * @param pkg package.
   * @param parentNode parent node of the node to be created.
   *
   * @return node. Never returns <code>null</code>.
   */
  protected ParentTreeTableNode createPackageNode(
      final BinPackage pkg,
      final BinTreeTableNode parentNode,
      final boolean flatPackages) {
    final BinTreeTableNode newNode = new BinTreeTableNode(pkg, false);

    if (flatPackages) {
      parentNode.addChild(newNode);
    } else {
      // FIXME probably it produces wrong result - super package doesn't exist
      // if it has no own real types
      // should be BinPackage parentPackage = Project.getPackage(pkg.lastName);
      // to be precisely correct... don't know if it was planned
      BinPackage parentPackage = pkg.getSuperPackage();

      if (parentPackage != null) {
        ParentTreeTableNode node = parentNode.findParent(parentPackage, false);
        if (Assert.enabled) {
          Assert.must(node != null, "parent package must exist!");
        }
//    		System.err.println("adding "+pkg.getQualifiedName()+" to children of "+parentPackage.getQualifiedName());

        node.addChild(newNode);
      } else {
//    		System.out.println("package "+pkg.getQualifiedName()+" doesn't have superpackage");
        parentNode.addChild(newNode);
      }
    }

    return newNode; // create package node
  }

  /** checks if it has a child with such bin inside */
  public final BinTreeTableNode findChildByType(final Object bin) {
    BinTreeTableNode node;

    for (int i = 0, max = this.getChildCount(); i < max; i++) {
      node = (BinTreeTableNode)this.getChildAt(i);
      if (node.getBin() == bin || node.getBin().equals(bin)) {
        return node;
      } else if (node.getChildCount() > 0) {
        // has children
        node = node.findChildByType(bin);
        if (node != null) {
          return node;
        }
      }
    }

    return null;
  }

  private boolean removeEmptyPackages(){
    if (getBin() instanceof BinPackage && getAllChildren().size() == 0) {
      /*
       * We will not remove the node using getParent().removeChild(this) because
       * it will cause the parent`s children list become shorter and not all
       * nodes will be checked in it`s for-statement. That`s why return 'true'.
       */
      return true;
    }

    List children = this.getAllChildren();
    List childrenToBeRemoved = new ArrayList();
    for (int i = 0; i < children.size(); i++) {
      BinTreeTableNode node = (BinTreeTableNode) children.get(i);
      if (node.removeEmptyPackages()){
        // if returned true -> add node to deletion list
        childrenToBeRemoved.add(node);
      }
    }

    // remove all nodes that were added for removing
    for (int i = 0; i < childrenToBeRemoved.size(); i++){
      removeChild(childrenToBeRemoved.get(i));
    }

    return false;
  }

  /**
   * @param childrenRemoved is the result, but for speed reasons it's a parameter
   */
  private boolean removeChildrenOf(
      final SourceHolder compilationUnit, final List childrenRemoved, int depth
      ) {
    SourceHolder source = getSource();

    if (source == null) {
      Object bin = getBin();
      if (bin instanceof LocationAware) {
        source = ((LocationAware) bin).getCompilationUnit();
      }
    }

    if (source == compilationUnit) {
      /*
       * We will not remove the node using getParent().removeChild(this) because
       * it will cause the parent`s children list become shorter and not all
       * nodes will be checked in it`s for-statement. That`s why return 'true'.
       */
      return true;
    }

    // Recursive call on all children (hidden or not)
    final List children = getAllChildren();
    List childrenToBeRemoved = new ArrayList();
    for (int i = 0; i < children.size(); i++) {
      BinTreeTableNode node = (BinTreeTableNode) children.get(i);
      if (node.removeChildrenOf(compilationUnit, childrenRemoved, depth+1)){
        // if returned true -> add node to deletion list
        childrenToBeRemoved.add(node);
      }
    }

    // remove all nodes that were added for removing
    for (int i = 0; i < childrenToBeRemoved.size(); i++){
      removeChild(childrenToBeRemoved.get(i));
      childrenRemoved.add(childrenToBeRemoved.get(i));
    }

    return false;
  }

  public final List removeChildrenOf(final Collection compilationUnits) {
    final List result = new ArrayList();

    for (Iterator i = compilationUnits.iterator(); i.hasNext(); ) {
      SourceHolder cUnit = (SourceHolder) i.next();
      if (removeChildrenOf(cUnit/*(SourceHolder) i.next()*/, result, 0)){
        if (getParent() != null){
          result.add(this);
          getParent().removeChild(this);
          break;
        }
      }
    }

    if (removeEmptyPackages() && getParent() != null){
      getParent().removeChild(this);
    }

    return result;
  }

  public final void saveBinItemReferences(final Project project) {
    // First save the bin
    binItemReference = BinItemReference.create(getBin());
    if (binItemReference == null){
      binItemReference = getBin();
    }

    if (binItemReference == null){
      AppRegistry.getLogger(this.getClass()).error("Item reference is null!",
          new Exception());
    }

    // Then its source file
    if (getSource() == null) {
      compilationUnitReference = null;
    } else {
      compilationUnitReference = ((CompilationUnit) getSource()).createReference();
    }

    // Recursive
    final List allChildren = getAllChildren();
    for (int i = 0, max = allChildren.size(); i < max; i++) {
      ((BinTreeTableNode) allChildren.get(i)).saveBinItemReferences(project);
    }
  }

  public final void restoreFromBinItemReferences(final Project project) {
    // First restore the bin
    final Object newBin;
    if (binItemReference instanceof BinItemReference) {
      newBin = ((BinItemReference) binItemReference).restore(project);
    } else {
      newBin = binItemReference;
    }
    this.bin = newBin;

    if (bin == null){
      bin = "NULL (lost item)";
      setShowSource(false);
    }

    // Then its sourcefile
    if (compilationUnitReference != null) {
      SourceHolder compilationUnit
          = (SourceHolder) compilationUnitReference.restore(project);
      setSourceHolder(compilationUnit);
      if (compilationUnit == null){
        setShowSource(false);
      }
    }

    // Recursive
    final List allChildren = getAllChildren();
    for (int i = 0; i < allChildren.size(); i++) {
      ((BinTreeTableNode) allChildren.get(i)).restoreFromBinItemReferences(
          project);
    }
  }

  /** checks if it has a child with such bin inside */
  public final BinTreeTableNode findChildByType(final Object bin,
      final int line) {
    BinTreeTableNode node;

    for (int i = 0, max = this.getChildCount(); i < max; i++) {
      node = (BinTreeTableNode)this.getChildAt(i);

      Object nodeBin = node.getBin();
      if ((nodeBin == bin || nodeBin.equals(bin))
          && line == node.queryLineNumber()) {
        return node;
      }

      if (node.getChildCount() > 0) {
        // has children
        if ((node = node.findChildByType(bin, line)) != null) {
          return node;
        }
      }
    }

    return null;
  }

  public void setShowSource(final boolean showSource) {
    this.showSource = showSource;
  }
}
