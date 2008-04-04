/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.refactorings.LocationAwareImpl;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.vfs.local.LocalSource;

import org.apache.log4j.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Moves given BinMembers to another BinCIType or moves given LocationAware
 * entities within the same file to the given line. <br>
 * Smartly indents moved entities .
 *
 * @author Anton Safonov
 */
public class MoveEditor extends DefaultEditor {
  private MultiValueMap entities = new MultiValueMap();

  // for constructs
  private SourceCoordinate constructTarget = null;

  private int alignBase = -1;

  private boolean alignFirstLine = true;

  // for members
  private SourceCoordinate fieldsTarget = null;

  private SourceCoordinate methodsTarget = null;

  private boolean copyOnly = false;

  private Map renameMap = null;

  private List astsToRename = null;

  private MultiValueMap multipleDecl = new MultiValueMap();

  private boolean useOriginalContent = false;

  private MoveEditor(final SourceHolder target, final List entities) {
    super(target);
    for (int i = 0, max = entities.size(); i < max; i++) {
      LocationAware entity = (LocationAware) entities.get(i);
      this.entities.putAll(entity.getCompilationUnit(), entity);
    }
  }

  /**
   * This one for members. They will detect target line automagically.
   *
   * @param entities
   *          defines what to move
   * @param target
   *          type to move to
   */
  public MoveEditor(List entities, BinCIType target) {
    this(target.getCompilationUnit(), entities);
    this.fieldsTarget = target.findNewFieldPosition();
    this.methodsTarget = target.findNewMethodPosition();
    this.alignBase = new BinTypeFormatter(target).getMemberIndent();
    this.alignFirstLine = true;
  }

  /**
   * This one for members. They will detect target line automagically.
   *
   * @param entities
   *          defines what to move
   * @param target
   *          file to move to, it is just created, so have no type yet
   */
  public MoveEditor(List entities, SourceHolder target,
      SourceCoordinate targetCoordinate, int alignBase) {
    this(target, entities);

    boolean movingMembers = false;
    for (Iterator i = entities.iterator(); i.hasNext();) {
      if (i.next() instanceof BinMember) {
        movingMembers = true;
      }
    }
    if (movingMembers) {
      this.fieldsTarget = targetCoordinate;
      this.methodsTarget = targetCoordinate;
      this.alignFirstLine = true;
    } else {
      this.constructTarget = targetCoordinate;
      this.alignFirstLine = false;
    }
    this.alignBase = alignBase;
  }

  /**
   * This one for moving constructs within the same type.
   *
   * @param entities
   *          defines what to move
   * @param constructTarget
   *          defines coordinate to move to
   * @param alignBase
   *          an indent
   * @param alignFirstLine
   *          put false here if will be putting manually something infront of
   *          this construct later, e.g. "return" infront of expression in
   *          ExtractMethod
   */
  public MoveEditor(List entities, SourceCoordinate constructTarget,
      int alignBase, boolean alignFirstLine) {
    this(((LocationAware) entities.get(0)).getCompilationUnit(), entities);
    this.constructTarget = constructTarget;
    this.alignBase = alignBase;
    this.alignFirstLine = alignFirstLine;
  }

  public void setCopyOnly(final boolean copyOnly) {
    this.copyOnly = copyOnly;
  }

  /**
   * @param renameMap
   *          ast -> newName
   */
  public void setRenameMap(final Map renameMap) {
    this.renameMap = renameMap;
    this.astsToRename = new ArrayList(this.renameMap.keySet());
    Collections.sort(this.astsToRename);
  }

  public void setAlignFirstLine(final boolean alignFirstLine) {
    this.alignFirstLine = alignFirstLine;
  }

  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();
    try {
      Iterator sources = this.entities.keySet().iterator();
      while (sources.hasNext()) {
        List las = this.entities.get(sources.next());
        Collections.sort(las, LocationAware.PositionSorter.getInstance());
        status.merge(moveLas(manager, las));
      }
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }
    return status;
  }

  private RefactoringStatus moveLas(LineManager manager, List las)
      throws IOException {
    RefactoringStatus status = new RefactoringStatus();

    LocationAware aligner = null;
    LocationAware followUp = null;
    for (int i = 0; i < las.size(); ++i) {
      final LocationAware toMove = (LocationAware) las.get(i);

      SourceCoordinate targetCoordinate = null;
      if (constructTarget == null) {
        aligner = toMove;
        if (toMove instanceof Comment) {
          for (int n = i + 1; n < las.size(); n++) {
            LocationAware following = (LocationAware) las.get(n);
            targetCoordinate = getTargetFor(following);
            if (targetCoordinate != null) {
              break;
            }
          }
        } else {
          targetCoordinate = getTargetFor(toMove);
        }
        if (Assert.enabled) {
          Assert.must(targetCoordinate != null,
              "Can't find target coordinate for: " + toMove);
        }

        manageSpaceAround(manager, toMove, targetCoordinate, status);

      } else {
        if (aligner == null) {
          aligner = toMove;
        }
        if (aligner.getEndLine() == toMove.getStartLine()) {
          followUp = toMove;
        } else if (followUp == null
            || followUp.getEndLine() < toMove.getStartLine()) {
          aligner = toMove;
        }
        targetCoordinate = this.constructTarget;
      }

      try {
        if (!isFromMultipleDeclaration(toMove)) {
          status.merge(moveLa(manager, toMove, targetCoordinate,
              toMove == aligner && this.alignFirstLine));
        } else {
          multipleDecl.put(((BinField) toMove).getParent(), toMove);
        }

      } catch (IndexOutOfBoundsException e) {
        e.printStackTrace(System.err);

        status.addEntry(toMove.getCompilationUnit().getSource()
            .getDisplayPath()
            + " - " + toMove.getStartLine(), CollectionUtil
            .singletonArrayList(e), RefactoringStatus.FATAL);
      }
    }

    if (!multipleDecl.isEmpty()) {
      for (Iterator it = multipleDecl.keySet().iterator(); it.hasNext();) {
        BinFieldDeclaration decl = (BinFieldDeclaration) it.next();
        moveMultipleDeclarations(manager, decl, multipleDecl.get(decl));
      }
      multipleDecl.clear();
    }

    return status;
  }

  private SourceCoordinate getTargetFor(LocationAware toMove) {
    SourceCoordinate targetCoordinate = null;

    if (toMove instanceof Comment) {
      // not possible to find, upper level should manage it
    } else if (toMove instanceof BinField || toMove instanceof BinCIType) {
      targetCoordinate = this.fieldsTarget;
    } else if (toMove instanceof BinMethod) {
      targetCoordinate = this.methodsTarget;
    }

    return targetCoordinate;
  }

  private void manageSpaceAround(LineManager manager, final LocationAware la,
      SourceCoordinate targetCoordinate, RefactoringStatus status)
      throws IOException {
    // System.err.println("spaceAround: " + la);
    final CompilationUnit source = la.getCompilationUnit();
    Line text = manager.getLine(source, la.getStartLine());

    // there was nothing infront of la, so let's take a linebreak also
    boolean willTakeLinebreak = text.substring(0, la.getStartColumn() - 1)
        .trim().length() == 0;
    boolean shouldLeaveLinebreak = false;
    boolean prefixHasAlreadyGone = false;
    // System.err.println("willTake1: " + willTakeLinebreak);
    if (willTakeLinebreak) {
      text = manager.getLine(source, la.getStartLine() - 1);
      if (text.charAt(text.length() - 1) == 0) {
        willTakeLinebreak = false; // someone deleted the end already
        prefixHasAlreadyGone = true;
      }
    }
    // System.err.println("willTake2: " + willTakeLinebreak);

    boolean enoughLinebreaksBefore = false;
    if (willTakeLinebreak) {
      text = manager.getLine(source, la.getStartLine() - 1);
      int column = text.length() - 1;
      while (column >= 0
          && (Character.isWhitespace(text.charAt(column)) || text
              .charAt(column) == 0)) {
        --column;
      }
      if (column >= 0 && text.charAt(column) == '{') {
        shouldLeaveLinebreak = true;
      }
      if (column < 0) {
        enoughLinebreaksBefore = true;
      }
    }
    // System.err.println("shouldLeave1: " + shouldLeaveLinebreak + ", enough: "
    // + enoughLinebreaksBefore);

    if (!shouldLeaveLinebreak && !enoughLinebreaksBefore) {
      text = manager.getLine(source, la.getEndLine() + 1);
      int column = 0;
      while (column < text.length()
          && (Character.isWhitespace(text.charAt(column)) || text
              .charAt(column) == 0)) {
        ++column;
      }
      if (column < text.length() && text.charAt(column) == '}') {
        shouldLeaveLinebreak = true;
      }
    }
    // System.err.println("shouldLeave2: " + shouldLeaveLinebreak + ", enough: "
    // + enoughLinebreaksBefore);

    if (willTakeLinebreak) {
      int line = la.getStartLine() - 1;
      text = manager.getLine(source, line);
      int column = text.length();
      while (column > 0
          && (Character.isWhitespace(text.charAt(column - 1)) || text
              .charAt(column - 1) == 0)) {
        --column;
      }
      if (text.length() - column > 0) {
        status.merge(moveLa(manager, new LocationAwareImpl(source, line,
            column + 1, line, text.length() + 1), targetCoordinate, false));
      }

    } else if (!prefixHasAlreadyGone) {
      // entity should always have a linebreak infront of it in a new location
      Line line = manager.getLine(getTarget(), targetCoordinate.getLine());
      line.insert(targetCoordinate.getColumn(), FormatSettings.LINEBREAK);
    }

    if (shouldLeaveLinebreak) {
      // let's leave a linebreak at the old place also to keep right formatting
      text = manager.getLine(source, la.getStartLine() - 1);
      text.append(FormatSettings.LINEBREAK);
    }

  }

  private RefactoringStatus moveLa(LineManager manager, LocationAware entity,
      SourceCoordinate targetCoordinate, boolean alignFirst) throws IOException {
    RefactoringStatus status = new RefactoringStatus();

    int minimalIndent = 0;
    List linesToShift = new ArrayList(entity.getEndLine()
        - entity.getStartLine() + 1);

    if (EditorManager.debug) {
      System.err.println("moveLa - entity: " + entity);
    }

    int startLine = entity.getStartLine();
    int endLine = entity.getEndLine();

    for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
      Line line = manager.getLine(entity.getCompilationUnit(), lineNum);
      if (EditorManager.debug) {
        System.err.println(lineNum + " - line before: \"" + line.toString()
            + "\"");
      }

      int begin, end;

      if (lineNum == startLine) {
        begin = entity.getStartColumn() - 1;
      } else {
        begin = 0;
      }

      if (lineNum == endLine) {
        end = entity.getEndColumn() - 1;
      } else {
        end = line.length();
      }

      int indent = 0;
      final int tabSize = FormatSettings.getTabSize();
      // NOTE it is not a bug here - to correctly calculate indent we must use
      // initial content
      final String content = useOriginalContent ? line.getOriginalContent()
          : line.getContent();
      int pos = 0;
      while (pos < content.length()
          && (content.charAt(pos) == ' ' || content.charAt(pos) == '\t')) {
        if (content.charAt(pos) == '\t') {
          indent += tabSize;
        } else {
          ++indent;
        }
        ++pos;
      }

      if (begin != 0) {
        if (!(entity instanceof LocationAwareImpl)
            && !(entity instanceof BinExpression)) {
          while (begin > 0
              && (line.charAt(begin - 1) == ' ' || line.charAt(begin - 1) == '\t')) {
            --begin;
          }
        }
      }

      // include linebreak if LA is the last one on lineNum
      {
        int search = end;
        // skip whitespace
        while (search < line.length()
            && Character.isWhitespace(line.charAt(search))
            && line.charAt(search) != '\r' && line.charAt(search) != '\n') {
          ++search;
        }

        if (search == line.length()
            || (search < line.length() && (line.charAt(search) == '\r' || line
                .charAt(search) == '\n'))) {
          end = line.length();
        }
      }

      if (Assert.enabled) {
        Assert.must(begin >= 0 && end <= line.length(),
            "LocationAware position is beyond real source lineNum:" + "\nLA: "
                + entity + "\nBegin: " + begin + ", end: " + end + "\nLine: \""
                + line + "\", len: " + line.length());
      }

      if (EditorManager.debug) {
        System.err.println(lineNum + " - found begin: " + begin + ", end: "
            + end);
      }

      if (minimalIndent <= 0 || indent < minimalIndent) {

        minimalIndent = indent;
      }
      String moveLine;
      // cmall hack for inlining recursive methods
      if (useOriginalContent) {
        moveLine = line.getOriginalContent().substring(begin, end);
      } else {
        moveLine = line.substring(begin, end);
      }

      moveLine = renameAsts(lineNum, moveLine, begin);

      linesToShift.add(moveLine);
      if (!this.copyOnly) {
        line.delete(begin, end);
      }

      if (EditorManager.debug) {
        System.err.println(lineNum + " - line after: \"" + line.toString()
            + "\"");
      }
    }

    // fetch old linebreak on type extract
    if (entity instanceof BinCIType) {
      String linebreak = null;
      for (int i = 0; i < linesToShift.size(); i++) {
        linebreak = StringUtil.getLinebreak((String) linesToShift.get(i));
        if (linebreak != null && linebreak.length() > 0) {
          break;
        }
      }
      manager.setLinebreak(getTarget(), linebreak);
    }

    // flush collected content to the new place
    Line line = manager.getLine(getTarget(), targetCoordinate.getLine());
    int align = this.alignBase;
    int shift = align - minimalIndent;
    if (EditorManager.debug) {
      System.err.println("TargetLine: " + targetCoordinate.getLine() + " - \""
          + line + "\", minimal indent: " + minimalIndent);
    }
    int tabSize = FormatSettings.getTabSize();

    for (int i = 0; i < linesToShift.size(); i++) {
      String str = (String) linesToShift.get(i);

      if ((alignFirst || i != 0)
          && (minimalIndent > 0 || !(entity instanceof Comment))) {
        if (i == 1
            && (entity instanceof BinExpression || entity instanceof LocationAwareImpl)) {
          align += FormatSettings.getContinuationIndent();
        }

        int k = 0;
        int x = 0;
        while (x < str.length() && k < align
            && Character.isWhitespace(str.charAt(x))) {
          if (str.charAt(x) == '\t') {
            k += tabSize;
          } else {
            ++k;
          }
          ++x;
        }

        if (EditorManager.debug) {
          System.err.println("Line str: \""
              + StringUtil.printableLinebreaks(str) + "\"");
          System.err.println("Align: " + align + ", shift: " + shift);
        }

        if (k < align) {
          if (str.trim().length() > 0) {
            line.insert(targetCoordinate.getColumn(), FormatSettings
                .getIndentString(align - k));
          }
        } else {
          if (shift > 0) {
            line.insert(targetCoordinate.getColumn(), FormatSettings
                .getIndentString(shift));
          } else if (shift < 0) {
            int ind = 0;
            int curShift = 0;
            while (ind < str.length() && curShift < -shift
                && (str.charAt(ind) == ' ' || str.charAt(ind) == '\t')) {
              if (str.charAt(ind) == ' ') {
                curShift++;
              } else {
                curShift += tabSize;
              }
              ++ind;
            }
            str = str.substring(ind);
          }
        }
      }

      line.insert(targetCoordinate.getColumn(), str);

      // small hack ensure linebreak after BinCIType on extract
      if (i == linesToShift.size() - 1 && entity instanceof BinCIType
          && !str.endsWith("\n") && !str.endsWith("\r")) {
        line.insert(targetCoordinate.getColumn(), FormatSettings.LINEBREAK);
      }

      if (EditorManager.debug) {
        System.err.println("After insert: \"" + line + "\"");
      }
    }

    return status;
  }

  private final String renameAsts(final int lineNum, final String moveLine,
      final int startShift) {
    if (this.astsToRename == null) {
      return moveLine;
    }

    String result = moveLine;

    final List asts = ASTUtil.getAstsForLine(lineNum, this.astsToRename);
    for (int i = asts.size() - 1; i >= 0; i--) {
      final ASTImpl ast = (ASTImpl) asts.get(i);
      final String newName = (String) this.renameMap.get(ast);
      result = result.substring(0, ast.getStartColumn() - startShift - 1)
          + newName + result.substring(ast.getEndColumn() - startShift - 1);
    }

    return result;
  }

  public void setUseOriginalContent(boolean useOriginalContent) {
    this.useOriginalContent = useOriginalContent;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": " + " target: "
        + getTarget().getSource() + " - " + entities.values();
  }




  public static boolean isFromMultipleDeclaration(LocationAware entity) {
    if (entity instanceof BinField
        && ((BinField) entity).getParent() instanceof BinFieldDeclaration) {
      BinFieldDeclaration decl = (BinFieldDeclaration) ((BinField) entity)
          .getParent();
      List declOrder = Arrays.asList(decl.getVariables());
      if (declOrder.size() > 1) {
        return true;
      }
    }
    return false;
  }

  private String getDeclarationText(LineManager manager, BinField field)
      throws IOException {
    String result = "";
    int sLine = field.getNameStart().getLine();
    int eLine = field.getExpressionEnd().getLine();
    int sCol = field.getNameStart().getColumn() - 1;
    int eCol = field.getExpressionEnd().getColumn() - 1;

    for (int i = sLine; i <= eLine; i++) {
      Line line = manager.getLine(field.getCompilationUnit(), i);
      int begin = (i == sLine) ? sCol : 0;
      int end = (i == eLine) ? eCol : line.length();
      result = result + line.substring(begin, end);
    }
    return result;
  }

  private void moveMultipleDeclarations(LineManager manager,
      BinFieldDeclaration decl, List movedDecls) throws IOException {

    Collections.sort(movedDecls, new BinFieldComparator());
    List allDecls = Arrays.asList(decl.getVariables());
    List simpleComments = new ArrayList(decl.getCompilationUnit()
        .getSimpleComments());
    List remainingDecls = new ArrayList(allDecls);

    remainingDecls.removeAll(movedDecls);

    // handle comments
    if (remainingDecls.size() == 0) {
      List declComments = Comment.findAllFor(decl);
      for (int i = 0; i < declComments.size(); i++) {
        Comment c = (Comment) declComments.get(i);
        BinField sampleField = (BinField) movedDecls.get(i);
        Line line = manager.getLine(
            getTarget(),
            getTargetFor(sampleField).getLine());
        line.insert(0, FormatSettings.getIndentString(alignBase)
            + c.getText()+FormatSettings.LINEBREAK);
        deleteLocationAware(c, manager);
      }
    }

    // handle moved lines
    for (int i = 0; i < movedDecls.size(); i++) {
      BinField field = (BinField) movedDecls.get(i);
      SourceCoordinate targetCoordinate = getTargetFor(field);
      String res = FormatSettings.getIndentString(alignBase)
          + getTypeAndModifiersNodeText(field, manager, false);
      res += " " + getDeclarationText(manager, field) + ";";
      res += getCommentText(field, simpleComments);
      res = res + FormatSettings.LINEBREAK;

      //res = renameAsts(field.getStartLine(), res, field.getStartColumn());

      Line line = manager.getLine(getTarget(), targetCoordinate.getLine());
      line.insert(0, res);
    }

    Line declLine = manager.getLine(decl.getCompilationUnit(), decl
        .getStartLine());

    // insert remaining declarations
    for (int i = 0; i < remainingDecls.size(); i++) {
      BinField field = (BinField) remainingDecls.get(i);
      String res = FormatSettings.getIndentString(alignBase)
          + getTypeAndModifiersNodeText(field, manager, true);
      res += " " + getDeclarationText(manager, field) + ";";
      res += getCommentText(field, simpleComments);
      res += FormatSettings.LINEBREAK;
      declLine.insert(0, res);
    }

    //  delete old declaration code
    deleteLocationAware(decl, manager);
  }

  private String getTypeAndModifiersNodeText(BinField field, LineManager manager,
      boolean useOriginalContent) throws IOException{
    String result = "";
    int sLine = field.getStartLine();
    int sCol = field.getStartColumn()-1;
    int eLine = field.getTypeNodeEndLine();
    int eCol = field.getTypeNodeEndColumn()-1;

    for(int k=sLine;k<=eLine;k++) {
      String tmp;
      Line line = manager.getLine(field.getCompilationUnit(),k);
      tmp = useOriginalContent ? line.getOriginalContent() : line.getContent();

      if(k==eLine) {
        tmp = tmp.substring(0,eCol);
      }

      if(k==sLine){
        tmp = tmp.substring(sCol);
      }

      result += tmp;
    }
    result = result.replaceAll(FormatSettings.LINEBREAK,"");
    result = result.replaceAll("\n","");
    return result.trim();
  }

  private void deleteLocationAware(LocationAware entity, LineManager manager)
      throws IOException {
    for (int ln = entity.getStartLine(), end = entity.getEndLine(); ln <= end; ln++) {
      Line line = manager.getLine(entity.getCompilationUnit(), ln);

      int sCol = (ln == entity.getStartLine()) ? entity.getStartColumn() - 1
          : 0;
      // hack
      if(entity.getIndent()==sCol){
        sCol = 0;
      }

      int eCol = (ln == end) ? entity.getEndColumn() - 1 : line.length();
      line.delete(sCol, eCol);
    }
  }

  private String getCommentText(BinField f, List comments) {
    String result = "";
    List removed = new ArrayList();
    for (int i = 0; i < comments.size(); i++) {
      Comment c = (Comment) comments.get(i);
      if (c.getEndLine() == f.getExpressionEnd().getLine() &&
          !removed.contains(c)) {
        result += " " + c.getText();
        removed.add(c);
      }
    }

    comments.removeAll(removed);
    return result;
  }


  /**
   * Test driver for {@link MoveEditor}.
   */
  public static final class TestDriver extends TestCase {

    /** Logger instance. */
    private static final Category cat = Category.getInstance(TestDriver.class
        .getName());

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("MoveEditor tests");
      return suite;
    }

    protected void setUp() throws Exception {
      FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    }

    /**
     * Tests moving of a single line.
     */
    public void testWholeLineMove() throws Exception {
      cat.info("Testing single line move.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 1, 1, 11);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 0,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "", manager.getLine(source, 1).getContent());

      assertEquals("Second line", "1234567890", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the beginning part of a single line.
     */
    public void testBeginningMove() throws Exception {
      cat.info("Testing single line beginning move.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 1, 1, 6);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 0,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "67890", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", "12345", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the ending part of a single line.
     */
    public void testEndingMove() throws Exception {
      cat.info("Testing single line ending move.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 6, 1, 11);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 0,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "12345", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", "67890", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the middle part of a single line.
     */
    public void testMiddleMove() throws Exception {
      cat.info("Testing single line middle move.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 4, 1, 8);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 0,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "123890", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", "4567", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the ending of a single line with existing indent and
     * shifting.
     */
    public void testEndingIndentShift() throws Exception {
      cat.info("Testing single line ending move with indent and shift.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "  1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 7, 1, 13);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 1,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "  1234", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", " 567890", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the beginning of a single line with existing indent and
     * shifting.
     */
    public void testBeginningIndentShift() throws Exception {
      cat.info("Testing single line beginning move with indent and shift.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "  1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 3, 1, 7);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 1,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "  567890", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", " 1234", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving the beginning of a single line with existing indent and
     * shifting.
     */
    public void testBeginningIndentShift2() throws Exception {
      cat.info("Testing single line beginning move with indent and shift.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "  1234567890");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 3, 1, 7);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 3,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "  567890", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", "   1234", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving multiply lines.
     */
    public void testMoveMultiplyLines() throws Exception {
      cat.info("Testing moving multiply lines.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      List lines = new ArrayList(2);
      lines.add("1234567890\r\n");
      lines.add("abcdefghij\r\n");
      LineManager manager = new LineManager(source, lines);

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 6, 2, 6);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(3, 0), 0,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "12345", manager.getLine(source, 1)
          .getContent());

      assertEquals("Second line", "fghij\r\n", manager.getLine(source, 2)
          .getContent());

      // note: here we see ContinuationIndent on the second line of la
      assertEquals("Third line", "67890\r\n    abcde", manager.getLine(source,
          3).getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving single indented line.
     */
    public void testMoveSingleIndentedLine() throws Exception {
      cat.info("Testing moving single indented line.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      LineManager manager = new LineManager(source, "  1234567890\r\n");

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 3, 1, 13);
      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(2, 0), 1,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "  ", manager.getLine(source, 1).getContent());

      assertEquals("Second line", " 1234567890\r\n", manager.getLine(source, 2)
          .getContent());

      cat.info("SUCCESS");
    }

    /**
     * Tests moving multiply lines.
     */
    public void testMoveMultiplyLines2() throws Exception {
      cat.info("Testing moving multiply lines 2.");

      CompilationUnit source = new CompilationUnit(
          new LocalSource(new File("")), null);
      List lines = new ArrayList(2);
      lines.add("  1234567890\r\n");
      lines.add("    abcdefghij\r\n");
      LineManager manager = new LineManager(source, lines);

      final LocationAwareImpl la = new LocationAwareImpl(source, 1, 3, 2, 15);

      MoveEditor shifter = new MoveEditor(
          CollectionUtil.singletonArrayList(la), new SourceCoordinate(3, 0), 4,
          true);
      RefactoringStatus status = shifter.apply(manager);
      assertTrue(status.getAllMessages(), status.getEntries().size() == 0);

      assertEquals("First line", "  ", manager.getLine(source, 1).getContent());

      assertEquals("Second line", "", manager.getLine(source, 2).getContent());

      assertEquals("Third line", "    1234567890\r\n        abcdefghij\r\n",
          manager.getLine(source, 3).getContent());

      cat.info("SUCCESS");
    }
  }

  private class BinFieldComparator implements Comparator {
    public int compare(Object o1, Object o2){
      if(o1 instanceof BinField && o2 instanceof BinField){
        BinField f1 = (BinField)o1;
        BinField f2 = (BinField)o2;
        int result = f1.getNameStart().getLine()-f2.getNameStart().getLine();
        if(result == 0){
          result = f1.getNameStart().getColumn() - f2.getNameStart().getColumn();
        }
        return result;
      }
      return Integer.MIN_VALUE;
    }

    public boolean equals(Object o){
      return false;
    }
  }
}
