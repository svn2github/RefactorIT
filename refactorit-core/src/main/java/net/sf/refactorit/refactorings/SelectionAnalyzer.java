/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.refactorings.extract.ExtractMethodAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public class SelectionAnalyzer extends SinglePointVisitor {
  private String errorMessage = null;
  private boolean isBreakSelected = false;
  private boolean isContinueSelected = false;
  private boolean isReturnSelected = false;
  private boolean isSuperOrThisSelected = false;

  private BinSelection selection;

  private BinMember rangeMember = null;
  private List beforeRangeConstructs = new ArrayList();
  private List inRangeConstructs = new ArrayList();
  private String partlyInRangeConstructs = null;
  private List afterRangeConstructs = new ArrayList();

  private Set localsDefinedWithin = new HashSet();

  private boolean trackAfters = false;

  private List selectedItems = new ArrayList();

  public boolean isBreakSelected() {
    return isBreakSelected;
  }

  protected boolean isReturnSelected() {
    return isReturnSelected;
  }

  public boolean isContinueSelected() {
    return isContinueSelected;
  }

  public boolean isSuperOrThisSelected() {
    return isSuperOrThisSelected;
  }

  public Set getLocalTypesDefinedWithin() {
    return localsDefinedWithin;
  }

  public BinSelection getSelection() {
    return selection;
  }

  public boolean isBracketsBalanced() {
    final String text = selection.getText();
    final LineIndexer indexer = selection.getCompilationUnit().getLineIndexer();

    int selectionStart = selection.getStartPosition();
    int selectionEnd = selection.getEndPosition();

    List simpleComments = selection.getCompilationUnit().getSimpleComments();
    List javadocComments = selection.getCompilationUnit().getJavadocComments();

    // guess number of elements to preallocate
    final List ignoredElements = new ArrayList(simpleComments.size()
        + javadocComments.size() + 5);

    copyElementsWithinSelection(ignoredElements, simpleComments, selectionStart,
        selectionEnd, indexer);
    /** FIXME: is this unneccesary? it is possible to embed javadoc style comments into code, but does the parser treat them as such? */
    copyElementsWithinSelection(ignoredElements, javadocComments,
        selectionStart, selectionEnd, indexer);
    findLiterals(ignoredElements, this.selectedItems);

    final StringBuffer codeBuffer = new StringBuffer(text);
    int brackets = 0;
    int blockBrackets = 0;
    synchronized (codeBuffer) {
      // blank out any literals or comments that could confuse us by containing (,),{,}  - just like this comment ;)
      Iterator ignoredIterator = ignoredElements.iterator();
      while (ignoredIterator.hasNext()) {
        LocationAware la = (LocationAware) ignoredIterator.next();
        int startInText = indexer.lineColToPos(la.getStartLine(),
            la.getStartColumn()) - selectionStart;
        int endInText = indexer.lineColToPos(la.getEndLine(),
            la.getEndColumn()) - selectionStart;

        startInText = Math.max(0, startInText);
        endInText = Math.min(text.length(), endInText);

        for (int i = startInText; i < endInText; i++) {
          codeBuffer.setCharAt(i, '@');
        }
      }

      for (int i = 0; i < codeBuffer.length(); i++) {
        switch (codeBuffer.charAt(i)) {
          case '(':
            ++brackets;
            break;

          case ')':
            --brackets;
            break;

          case '{':
            ++blockBrackets;
            break;

          case '}':
            --blockBrackets;
            break;
        }

        if (brackets < 0 || blockBrackets < 0) {
          return false; // illegal brackets order
        }
      }
    }

    return brackets == 0 && blockBrackets == 0;
  }

  private void copyElementsWithinSelection(List ignoredElements,
      List possibleElements, int selectionStart, int selectionEnd,
      LineIndexer indexer) {
    Iterator i = possibleElements.iterator();
    while (i.hasNext()) {
      LocationAware la = (LocationAware) i.next();

      int elementStart = indexer.lineColToPos(la.getStartLine(),
          la.getStartColumn());
      int elementEnd = indexer.lineColToPos(la.getEndLine(), la.getEndColumn());

      if (overlap(selectionStart, selectionEnd, elementStart, elementEnd)) {
        ignoredElements.add(la);
      }
    }
  }

  private boolean overlap(int selectionStart, int selectionEnd,
      int elementStart, int elementEnd) {
    return ((elementStart >= selectionStart && elementStart < selectionEnd)
        || (elementEnd > selectionStart && elementEnd <= selectionEnd)
        || (elementStart < selectionStart && elementEnd > selectionEnd));
  }

  /**
   * Deep traverse the <i>locations</i> list and add any string or char literals
   * that are encountered.
   */
  private void findLiterals(final List ignoredElements, List locations) {
    BinItemVisitor deepVisitor = new SelectionAnalyzer.LiteralSearchVisitor(
        ignoredElements);

    for (int i = 0, max = locations.size(); i < max; i++) {
      final LocationAware la = (LocationAware) locations.get(i);
      if (la instanceof BinSourceConstruct) {
        ((BinSourceConstruct) la).accept(deepVisitor);
      }
    }
  }

  public boolean isAllChildrenSelected(BinExpression topExpression) {
    List constructs = getBeforeRangeConstructs();
    for (int i = 0, max = constructs.size(); i < max; i++) {
      final LocationAware la = (LocationAware) constructs.get(i);
      if (la instanceof BinSourceConstruct
          && ((BinSourceConstruct) la).getParent() == topExpression) {
        if (ExtractMethodAnalyzer.showDebugMessages) {
          System.err.println("LA: " + la);
        }
        return false;
      }
    }

    constructs = getAfterRangeConstructs();
    for (int i = 0, max = constructs.size(); i < max; i++) {
      final LocationAware la = (LocationAware) constructs.get(i);
      if (la instanceof BinSourceConstruct
          && ((BinSourceConstruct) la).getParent() == topExpression) {
        if (ExtractMethodAnalyzer.showDebugMessages) {
          System.err.println("LA: " + la);
        }
        return false;
      }
    }

    return true;
  }

  private class LiteralSearchVisitor extends BinItemVisitor {
    private List ignoredElements;

    public LiteralSearchVisitor(List ignoredElements) {
      this.ignoredElements = ignoredElements;
    }

    public void visit(BinLiteralExpression literal) {
      BinTypeRef literalType = literal.getReturnType();

      if (literalType != null) {
        if (literalType.isString()
            || literalType.equals(BinPrimitiveType.CHAR_REF)) {
          ignoredElements.add(literal);
        }
      }
    }
  }


  public SelectionAnalyzer(final BinSelection selection) {
    this.selection = selection;

    List types = selection.getCompilationUnit().getDefinedTypes();
    for (int i = 0, max = types.size(); i < max; ++i) {
      BinTypeRef aType = (BinTypeRef) types.get(i);
      if (aType.getBinCIType().contains(selection)) {
        aType.getBinCIType().accept(this);
      }
    }

    filterOutSubConstructs();

    addCommentsAndEmptyLines();

    Collections.sort(this.selectedItems,
        LocationAware.PositionSorter.getInstance());

    analyzePartlyInRange();
  }

  private void filterOutSubConstructs() {
    final Iterator it = this.selectedItems.iterator();
    while (it.hasNext()) {
      LocationAware la = (LocationAware) it.next();
      if (la instanceof BinSourceConstruct
          && (this.selectedItems.contains(((BinSourceConstruct) la).getParent())
          || !isLaSingle(la))) {
        it.remove();
      }
    }
  }

  private void analyzePartlyInRange() {
    final int offset = this.selection.getStartPosition();
    this.partlyInRangeConstructs = this.selection.getText();
//System.err.println("selection: " + StringUtil.printableLinebreaks(this.partlyInRangeConstructs));
    for (int i = this.selectedItems.size() - 1; i >= 0; --i) {
      final LocationAware la = (LocationAware)this.selectedItems.get(i);
//System.err.println("removing: " + la);
      int start = la.getStartPosition() - offset;
      if (start < 0) {
        continue; // some crappy half-selected la got into the moving las list
      }
      int end = la.getEndPosition() - offset;
      if (end > this.partlyInRangeConstructs.length()) {
        end = this.partlyInRangeConstructs.length();
      }
      this.partlyInRangeConstructs = this.partlyInRangeConstructs.substring(
          0, start)
          + this.partlyInRangeConstructs.substring(
          end, this.partlyInRangeConstructs.length());
    }

    this.partlyInRangeConstructs = StringUtil.replace(
        this.partlyInRangeConstructs, "\n", "");
    this.partlyInRangeConstructs = StringUtil.replace(
        this.partlyInRangeConstructs, "\r", "");

    this.partlyInRangeConstructs = this.partlyInRangeConstructs.trim();
//System.err.println("left: " + StringUtil.printableLinebreaks(this.partlyInRangeConstructs));
  }

  public void visit(BinConstructor x) {
    if (!x.isSynthetic()) {
      super.visit(x);
    }
  }

  /**
   * onEnter will be called on visiting *before* starting to traverse children
   */
  public void onEnter(Object o) {
    //System.err.println("Entering: " + o + ", range: " + rangeMember);
    if (Assert.enabled) {
      Assert.must(o instanceof LocationAware,
          o.getClass().getName() + " is not LocationAware");
    }

    testRange((LocationAware) o);
  }

  /**
   * onLeave will be called on visiting *after* starting to traverse children
   */
  public void onLeave(Object o) {
    //System.err.println("Leaving: " + o + ", range: " + rangeMember);
    if (trackAfters && rangeMember == o) {
      trackAfters = false;
    }
  }

  private void addCommentsAndEmptyLines() {
    List commentsToMove = new ArrayList();

    List comments = selection.getCompilationUnit().getSimpleComments();
    for (int i = 0, max = comments.size(); i < max; ++i) {
      Comment comment = (Comment) comments.get(i);
      if ((isStartInSelection(comment) || isEndInSelection(comment))
          && isLaSingle(comment)) {
        commentsToMove.add(comment);
      }
    }

    comments = selection.getCompilationUnit().getJavadocComments();
    for (int i = 0, max = comments.size(); i < max; ++i) {
      Comment comment = (Comment) comments.get(i);
      if ((isStartInSelection(comment) || isEndInSelection(comment))
          && isLaSingle(comment)) {
        commentsToMove.add(comment);
      }
    }

    this.selectedItems.addAll(commentsToMove);

    // FIXME: bad implementation of moving empty lines
    LineIndexer li = selection.getCompilationUnit().getLineIndexer();
    String content = selection.getCompilationUnit().getContent();
    List emptyLines = new ArrayList();
    for (int line = selection.getStartLine(); line <= selection.getEndLine();
        ++line) {
      int linestartPos = li.lineColToPos(line, 1);
      int nextLineStart = li.lineColToPos(line + 1, 1);

      String contentPart = content
          .substring(linestartPos, nextLineStart);

      boolean isEmptyLine = true;
      for (int i = 0; i < contentPart.length(); ++i) {
        char c = contentPart.charAt(i);
        if (!Character.isWhitespace(c)) {
          isEmptyLine = false;
          break;
        }
      }

      if (isEmptyLine) {
        LocationAware lw = new EmptyLine(selection.getCompilationUnit(),
            line, 1, line, li.posToLineCol(nextLineStart - 1).getColumn());

        if (isLaSingle(lw)) {
          emptyLines.add(lw);
        }
      }
    }

    selectedItems.addAll(emptyLines);
  }

  private boolean isLaSingle(final LocationAware laToCheck) {
    for (int i = 0, max = this.selectedItems.size(); i < max; i++) {
      final LocationAware la = (LocationAware)this.selectedItems.get(i);
      if (laToCheck != la && la.contains(laToCheck)) {
        return false;
      }
    }

    return true;
  }

  public boolean hasErrors() {
    return errorMessage != null;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void addErrorMessage(String errorMessage) {
    // FIXME: improve later
    this.errorMessage = errorMessage;
  }

  private void testRange(LocationAware la) {
//System.err.println("Selection: " + startLine + ":" + startColumn
//    + " - " + endLine + ":" + endColumn);
//System.err.println("LA: " + + getStartLine(la) + ":" + getStartColumn(la)
//    + " - " + getEndLine(la) + ":" + getEndColumn(la));

    boolean startIn = isStartInSelection(la);
    boolean endIn = isEndInSelection(la);

    if (rangeMember == null && (startIn || endIn)) {
      rangeMember = (BinMember) getCurrentLocation();
      if (Assert.enabled) {
        Assert.must(rangeMember != null, "Current location is null!");
      }
      trackAfters = true;
    }

    if (startIn && endIn) {
      inRangeConstructs.add(la);
      if (la instanceof BinReturnStatement
          && getCurrentLocation() == rangeMember) {
        isReturnSelected = true;
      }

      if (la instanceof BinBreakStatement) {
        BinBreakStatement bStmt = (BinBreakStatement) la;
        if (!inRangeConstructs.contains(bStmt.getBreakTarget())) {
          if (bStmt.isBreakStatement()) {
            isBreakSelected = true;
          } else {
            isContinueSelected = true;
          }
        }
      }

      if (la instanceof BinConstructorInvocationExpression) {
        isSuperOrThisSelected = true;
      }

      if (la instanceof BinCIType && ((BinCIType) la).isLocal()) {
        this.localsDefinedWithin.add(la);
      }

      if (la instanceof BinSourceConstruct) {
        BinItemVisitable parent = ((BinSourceConstruct) la).getParent();
        if (Assert.enabled) {
          Assert.must(parent != null, "Parent is null for LA: " + la);
          Assert.must(parent instanceof LocationAware,
              "Parent is not LocationAware: " + parent.getClass().getName());
        }
//System.err.println("LA: " + la + ", PARENT: " + parent);
        if (parent == rangeMember || isTop(la)) {
//System.err.println("Added!!!!");
          CollectionUtil.addNew(selectedItems, la);
        }
      }
    } else if ((startIn && !endIn) || (endIn && !startIn)) {
      if (la instanceof BinArithmeticalExpression) {
        // let's it try to automorph to match our selection better
        analyzeArithmeticalExpression((BinArithmeticalExpression) la);
      }
    }

    if (trackAfters) {
      boolean isAfter = la.getStartLine() >= this.selection.getEndLine();
      if (la.getStartLine() == this.selection.getEndLine()
          && la.getStartColumn() < this.selection.getEndColumn()) {
        isAfter = false;
      }

      if (isAfter) {
        this.afterRangeConstructs.add(la);
      }
    } else if (!startIn && !endIn) {
      this.beforeRangeConstructs.add(la);
    }
  }

  private boolean isTop(LocationAware entity) {
    if (selectedItems.contains(entity)) {
      return true;
    }

    if (!(entity instanceof BinSourceConstruct)) {
      return false;
    }

    LocationAware parent = (LocationAware) ((BinSourceConstruct) entity)
        .getParent();

    boolean isWithin = false;

    while (inRangeConstructs.contains(parent)) {
      if (!(parent instanceof BinSourceConstruct)) {
        isWithin = true;
        break;
      }

      //System.err.println("Parent: " + parent);
      //System.err.println("Child: " + entity);
      isWithin = parent.getStartLine() <= entity.getStartLine()
          && parent.getEndLine() >= entity.getEndLine();
      //System.err.println("Check1: " + isInside);

      if (isWithin && parent.getStartLine() == entity.getStartLine()
          && parent.getStartColumn() > entity.getStartColumn()) {
        isWithin = false;
      }
      //System.err.println("Check2: " + isInside);

      if (isWithin && parent.getEndLine() == entity.getEndLine()
          && parent.getEndColumn() < entity.getEndColumn()) {
        isWithin = false;
      }
      //System.err.println("Check3: " + isInside);

      if (isWithin) {
        break;
      }

      parent = (LocationAware) ((BinSourceConstruct) parent).getParent();
    }

    return!isWithin;
  }

  private boolean isEndInSelection(LocationAware la) {
    boolean endIn = la.getEndLine() >= this.selection.getStartLine()
        && la.getEndLine() <= this.selection.getEndLine();
    /*if (endIn) {
     System.err.println("End - LA: " + la);
     System.err.println("End - sel: " + startColumn + " - " + endColumn);
     }*/

    if (la.getEndLine() == this.selection.getStartLine()
        && la.getEndColumn() <= this.selection.getStartColumn()) {
      endIn = false;
//System.err.println("check1");
    }
    if (la.getEndLine() == this.selection.getEndLine()
        && la.getEndColumn() > this.selection.getEndColumn()) {
      endIn = false;
//System.err.println("check2");
    }

    return endIn;
  }

//  private boolean isExactlyEndInSelection(LocationAware la) {
//    LineIndexer indexer = this.selection.getCompilationUnit().getLineIndexer();
//    int len = indexer.lineColToPos(
//        this.selection.getEndLine(), this.selection.getEndColumn())
//        - indexer.lineColToPos(
//            la.getEndLine(), la.getEndColumn());
//    //System.err.println("Len: " + len);
//    if (len >= 0) {
//      String text = this.selection.getText();
//      text = text.substring(text.length() - len).trim();
//      //System.err.println("Text: " + text);
//      return text.length() == 0;
//    } else {
//      return false;
//    }
//  }

  private boolean isStartInSelection(LocationAware la) {
    boolean startIn = la.getStartLine() >= this.selection.getStartLine()
        && la.getStartLine() <= this.selection.getEndLine();

//if (startIn) {
//  System.err.println("Start - LA: " + la);
//  System.err.println("Start - sel: " + startColumn + " - " + endColumn);
//}

    if (la.getStartLine() == this.selection.getStartLine()
        && la.getStartColumn() < this.selection.getStartColumn()) {
      startIn = false;
//System.err.println("check3");
    }

    if (la.getStartLine() == this.selection.getEndLine()
        && la.getStartColumn() >= this.selection.getEndColumn()) {
      startIn = false;
//System.err.println("check4");
    }

    return startIn;
  }

  public BinMember getRangeMember() {
    return this.rangeMember;
  }

  public List getBeforeRangeConstructs() {
    return this.beforeRangeConstructs;
  }

  public List getInRangeConstructs() {
    return this.inRangeConstructs;
  }

  public String getPartlyInRangeConstructs() {
    return this.partlyInRangeConstructs;
  }

  public List getAfterRangeConstructs() {
    return this.afterRangeConstructs;
  }

//  private String getSelectionMessage() {
//    StringBuffer result = new StringBuffer(200);
//    result.append("Selection " + selection.toString() + "\n");
//    result.append("In range constructs:\n");
//    for (int i = 0; i < inRangeConstructs.size(); ++i) {
//      result.append(i + "." + BinFormatter.formatLocationAware(
//          (LocationAware) inRangeConstructs.get(i)) + "\n");
//    }
//
//    result.append("\n\nAfter constructs:\n");
//    for (int i = 0; i < afterRangeConstructs.size(); ++i) {
//      result.append(i + "." + BinFormatter.formatLocationAware(
//          (LocationAware) afterRangeConstructs.get(i)) + "\n");
//    }
//
//    result.append("\n\nPartly in range constructs:\n"
//        + this.partlyInRangeConstructs);
//
//    return result.toString();
//  }

  public List getSelectedItems() {
    return this.selectedItems;
  }

  public BinExpression findTopExpression() {
    BinSourceConstruct la = null;

    for (int i = 0; i < this.selectedItems.size(); i++) {
      LocationAware cur = (LocationAware)this.selectedItems.get(i);
      if (!(cur instanceof BinExpression) && !(cur instanceof Comment)) {
        la = null;
        break;
      }

      while (cur instanceof BinSourceConstruct
          && ((BinSourceConstruct) cur).getParent() != null
          && this.selectedItems.contains(((BinSourceConstruct) cur).getParent())) {
        cur = (BinSourceConstruct) ((BinSourceConstruct) cur).getParent();
      }

      if (cur instanceof BinSourceConstruct) {
        la = (BinSourceConstruct) cur;
      } else if (cur instanceof Comment) {
        continue;
      } else {
        la = null;
        break;
      }

      break;
    }

    return (BinExpression) la;
  }

  public LocationAware constructExpressionsLA() {
    if (findTopExpression() == null) {
      return null;
    }

    /*    Collections.sort(inRangeConstructs, new LocationAware.PositionSorter());
        int startLine
            = ((LocationAware) inRangeConstructs.get(0)).getStartLine();
        int startColumn
            = ((LocationAware) inRangeConstructs.get(0)).getStartColumn();
        int endLine
     = ((LocationAware) inRangeConstructs.get(inRangeConstructs.size() - 1))
            .getEndLine();
        int endColumn
     = ((LocationAware) inRangeConstructs.get(inRangeConstructs.size() - 1))
            .getEndColumn();

        int startPos = indexer.lineColToPos(startLine, startColumn);
        if (startPos > selection.getStartPosition()) {
          --startPos;
        }
        int endPos = indexer.lineColToPos(endLine, endColumn);
        final String content = selection.getCompilationUnit().getContent();

        while (startPos > selection.getStartPosition()
            && (Character.isWhitespace(content.charAt(startPos))
            || content.charAt(startPos) == '(')) {
          --startPos;
        }

        while (endPos < selection.getEndPosition()
            && (Character.isWhitespace(content.charAt(endPos))
            || content.charAt(endPos) == ')')) {
          ++endPos;
        }

        return new LocationAwareImpl(selection.getCompilationUnit(),
            indexer.posToLineCol(startPos).getLine(),
            indexer.posToLineCol(startPos).getColumn(),
            indexer.posToLineCol(endPos).getLine(),
            indexer.posToLineCol(endPos).getColumn());*/

    return new LocationAwareImpl(getSelection().getCompilationUnit(),
        getSelection().getStartLine(), getSelection().getStartColumn(),
        getSelection().getEndLine(), getSelection().getEndColumn());
  }

  private boolean analyzeArithmeticalExpression(BinArithmeticalExpression expr) {
    List decomposed = expr.decompose();
    int startInd = -2;
    int endInd = -2;
    for (int i = 0; i < decomposed.size(); i++) {
      LocationAware la = (LocationAware) decomposed.get(i);
      if (isStartInSelection(la) && isEndInSelection(la)) {
        if (startInd < 0) {
          startInd = i;
        }
        endInd = i + 1;
      }
    }

    if (startInd < 0 || endInd < 0) {
      return false;
    }

    if (startInd % 2 != 0 || (endInd + 1) % 2 != 0) {
      return false;
    }

    if (endInd - startInd > 1) {
      expr.group(new Integer[] {new Integer(startInd)}
          ,
          new Integer[] {new Integer(endInd)});
    }

    return true; // seems ok
  }
}
