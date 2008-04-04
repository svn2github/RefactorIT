/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.javadoc;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSourceTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.LocalVariableIndexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Javadoc comment. Consists of description and tag section containing
 * standalone tags.
 *
 * @author vadim
 */
public class Javadoc implements JavaTokenTypes {
  private final List standaloneTags;
  private List javadocParams = new ArrayList();
  private List javadocMembers = new ArrayList();

  /**
   * Tag. Either standalone or in-line one.
   */
  public abstract static class Tag {
    private String name;

    private int startRow = -1;

    /**
     * Constructs javadoc tag with specified name
     *
     * @param name name.
     */
    public Tag(String name) {
      this.name = name;
    }

    /**
     * Tag. Either standalone or in-line one.
     */
    public abstract void parseText(String text, int wantedTags, int row,
        int column,
        List standaloneTags, boolean tagSectionStarted);

    public abstract String getLinkName();

    public abstract int getLinkStartColumn();

    public abstract void setLinkStartColumn(int start);

    public abstract List getDescriptionList();

    public abstract void addDescription(String description, int column);

    public abstract String getHTMLRepresentation();

    public String getName() {
      return name;
    }

    public int getStartRow() {
      return startRow;
    }

    public void setStartRow(int startRow) {
      this.startRow = startRow;
    }
  }


// description
  public static class TextType extends Tag {
    private DescriptionData curDescription;
    private List descriptionList = new ArrayList();

    TextType(String tagName) {
      super(tagName);
    }

    public class DescriptionData {
      private final int column;
      private final String description;
      private List innerStart = new ArrayList();
      private List innerEnd = new ArrayList();
      private List innerTags = new ArrayList();

      DescriptionData(String description, int column) {
        this.description = description;
        this.column = column;
      }

      void addTag(
          int wantedTags, Tag tag, List standaloneTags, int start, int end
          ) {
        if (tag == null) {
          return;
        }

        if (wantedTags == 0) {
          setInnerStartEnd(start, end);
          addInnerTag(tag);
        } else {
          standaloneTags.add(tag);
        }
      }

      private void setInnerStartEnd(int start, int end) {
        setInnerStart(start);
        setInnerEnd(end);
      }

      public List getInnerStart() {
        return innerStart;
      }

      public List getInnerEnd() {
        return innerEnd;
      }

      public void setInnerStart(int start) {
        innerStart.add(new Integer(start));
      }

      public void setInnerEnd(int end) {
        innerEnd.add(new Integer(end));
      }

      public String getDescriptionText() {
        return description;
      }

      public int getColumn() {
        return column;
      }

      private void addInnerTag(Tag tag) {
        innerTags.add(tag);
      }

      public List getInnerTags() {
        return innerTags;
      }

      public boolean areInnerTagsEmpty() {
        return (innerTags.size() > 0 ? false : true);
      }
    }


// end of DescriptionData

    public void parseText(String text, int wantedTags, int row, int column,
        List standaloneTags, boolean tagSectionStarted) {
      processDescription(text, column);
      lookForInnerTags(getCurDescription(), wantedTags, row,
          standaloneTags, tagSectionStarted);
    }

    protected void processDescription(String description, int column) {
      if (description.length() == 0) {
        curDescription = new DescriptionData("", 0);
        return;
      }

      Javadoc.Position position = new Javadoc.Position();
      description = skipWhitespace(position, description);

      addDescription(description, (column + position.column));
    }

    protected void lookForInnerTags(DescriptionData data, int wantedTags,
        int row,
        List standaloneTags, boolean tagSectionStarted) {
      String text = data.getDescriptionText();

      if (text.length() == 0) {
        return;
      }

      int column = data.getColumn();
      int end;
      char[] chars = text.toCharArray();
      String newText;
      Tag tag = null;

      for (int i = 0; i < chars.length; i++) {
        if (chars[i] == '{') {
          if ((end = findClosingBracket(chars, i + 1)) != -1) {
            newText = new String(chars, i + 1, end - i - 1);
            tag = Javadoc.createTag(null, newText, wantedTags, row,
                (column + i + 1),
                standaloneTags, tagSectionStarted);
            data.addTag(wantedTags, tag, standaloneTags, i, end);

            i = end + 1;
          }
        }
      }
    }

    private int findClosingBracket(char[] chars, int index) {
      int count = 1;
      int i = 0;

      if (index < chars.length && chars[index] == '@') {
        for (i = index; i < chars.length; i++) {
          if (chars[i] == '{') {
            count++;
          }
          if (chars[i] == '}') {
            count--;
          }

          if (count == 0) {
            break;
          }
        }
      }

      return count == 0 ? i : -1;
    }

    public String getHTMLRepresentation() {
      StringBuffer result = new StringBuffer();
      String description = composeDescription();
      result.append("<DT>");
      result.append(description);
      result.append("</DT>\n");

      return result.toString();
    }

    protected String composeDescription() {
      List descriptionList = getDescriptionList();
      StringBuffer description = new StringBuffer("");
      DescriptionData data = null;
      String withInnersIfExist;

      for (int i = 0, max = descriptionList.size(); i < max; i++) {
        if (i > 0) {
          description.append('\n');
        }
        data = (DescriptionData) descriptionList.get(i);
        withInnersIfExist = replaceInnersIfExist(data);
        description.append(withInnersIfExist);
      }

      return description.toString();
    }

    private String replaceInnersIfExist(DescriptionData data) {
      String description = data.getDescriptionText();

      if (data.areInnerTagsEmpty()) {
        return description;
      }

      int textStart = 0;
      int innerStart, innerEnd;
      Tag innerTag;
      List innerTags = data.getInnerTags();
      List innerStartList = data.getInnerStart();
      List innerEndList = data.getInnerEnd();
      StringBuffer result = new StringBuffer("");

      for (int i = 0, max = innerTags.size(); i < max; i++) {
        innerTag = (Tag) innerTags.get(i);
        innerStart = ((Integer) innerStartList.get(i)).intValue();
        innerEnd = ((Integer) innerEndList.get(i)).intValue();

        result.append(description.substring(textStart, innerStart));
        result.append(innerTag.getHTMLRepresentation());
        textStart = innerEnd + 1;
      }

      if (textStart < description.length()) {
        result.append(description.substring(textStart));
      }

      return result.toString();
    }

    protected DescriptionData getCurDescription() {
      return curDescription;
    }

    public List getDescriptionList() {
      return descriptionList;
    }

    public void addDescription(String description, int column) {
      curDescription = new DescriptionData(description, column);
      descriptionList.add(curDescription);
    }

    public String getLinkName() {
      return "";
    }

    public int getLinkStartColumn() {
      return 0;
    }

    public void setLinkStartColumn(int start) {
    }

    public String toString() {
      return ClassUtil.getShortClassName(this) + ": "
          + getLinkName() + " - " + composeDescription();
    }
  }


// super for
// @param, @throws, @exception
// and
// @see, @link
  public static class TagLinkType extends TextType {
    private String link = "";
    private int linkStartColumn = 0;

    TagLinkType(String tagName) {
      super(tagName);
    }

    protected void processText(String text, int column) {
      if (getLinkName().length() > 0) {
        processDescription(text, column);
      } else {
        extractLinkAndDescription(text, column);
      }
    }

    protected void extractLinkAndDescription(String text, int column) {
      text = text.trim();

      if (text.length() == 0) {
        return;
      }

      link = extractLinkName(text);
      if (link.length() == 0) {
        return;
      }
      setLinkStartColumn(column);

      String description = text.substring(link.length());
      processDescription(description, column + link.length());
    }

    private String extractLinkName(String text) {
      char[] newChars = new char[text.length()];
      char[] oldChars = text.toCharArray();

      boolean openParen = false;
      int count = 0;
      for (int i = 0; i < oldChars.length; i++) {
        if (oldChars[i] == ' ' || oldChars[i] == '\t') {
          if (!openParen) {
            return new String(newChars, 0, count);
          }
        } else if (oldChars[i] == '(') {
          openParen = true;
        } else if (oldChars[i] == ')') {
          openParen = false;
        }
        newChars[count++] = oldChars[i];
      }

      return new String(newChars, 0, count);
    }

    public String getLinkName() {
      return link;
    }

    public int getLinkStartColumn() {
      return linkStartColumn;
    }

    public void setLinkStartColumn(int start) {
      linkStartColumn = start;
    }

  }


// @param, @throws, @exception
  public static class TagLinkTextType extends TagLinkType {
    TagLinkTextType(String tagName) {
      super(tagName);
    }

    public void parseText(String text, int wantedTags, int row, int column,
        List standaloneTags, boolean tagSectionStarted) {
      processText(text, column);
      lookForInnerTags(getCurDescription(), wantedTags, row,
          standaloneTags, tagSectionStarted);
    }

    public String getHTMLRepresentation() {
      String linkName = getLinkName();
      StringBuffer result = new StringBuffer();
      boolean isNotLink = "param".equals(getName());

      result.append("<CODE>");

      if (isNotLink) {
        result.append(linkName);
      } else {
        result.append("<A HREF=\"");
        result.append(linkName);
        result.append("\">");
        result.append(linkName);
        result.append("</A>");
      }

      result.append("</CODE>");
      result.append(" - ");
      result.append(composeDescription());

      return result.toString();
    }
  }


// @see, @link
  public static class TagLinkLabelType extends TagLinkType {
    TagLinkLabelType(String tagName) {
      super(tagName);
    }

    public void parseText(String text, int wantedTags, int row, int column,
        List standaloneTags, boolean tagSectionStarted) {
      processText(text, column);
    }

    public String getHTMLRepresentation() {
      StringBuffer result = new StringBuffer();

      result.append("<A HREF=\"");
      result.append(getLinkName());
      result.append("\">");
      result.append("<CODE>");
      result.append(composeDescription());
      result.append("</CODE>");
      result.append("</A>");

      return result.toString();
    }

    protected String composeDescription() {
      List descriptionList = getDescriptionList();
      StringBuffer description = new StringBuffer("");
      DescriptionData data = null;

      if (descriptionList.size() > 0) {
        for (int i = 0, max = descriptionList.size(); i < max; i++) {
          if (i > 0) {
            description.append(' ');
          }
          data = (DescriptionData) descriptionList.get(i);
          description.append(data.getDescriptionText());
        }
      }

      if (description.length() > 0) {
        return description.toString();
      } else {
        return removeHashSign(getLinkName());
      }
    }

    private String removeHashSign(String text) {
      int index;
      if ((index = text.indexOf('#')) != -1) {
        if (text.indexOf('.') != -1) {
          return text.replace('#', '.');
        } else {
          return text.substring(index + 1);
        }
      }

      return text;
    }

    public String toString() {
      return ClassUtil.getShortClassName(this) + ": "
          + getLinkName() + " - " + composeDescription();
    }
  }


// @return, @deprecated, @author, @since, @version
  public static class TagTextType extends TextType {

    TagTextType(String tagName) {
      super(tagName);
    }

    public void parseText(String text, int wantedTags, int row, int column,
        List standaloneTags, boolean tagSectionStarted) {
      super.parseText(text, wantedTags, row, column, standaloneTags,
          tagSectionStarted);
    }

    public String getHTMLRepresentation() {
      return composeDescription();
    }
  }


  /**
   * Hidden constructor. Use {@link #parse parse} to create instances.
   *
   * @param standaloneTags list of standalone tags
   *                       ({@link Javadoc.Tag} instances).
   *                       Cannot be <code>null</code>.
   */
  private Javadoc(List standaloneTags) {
    this.standaloneTags = standaloneTags;
  }

  /**
   * Gets list of standalone tags of this javadoc comment.
   *
   * @return list of standalone tags ({@link Javadoc.Tag} instances).
   *         Never returns <code>null</code>.
   */
  public List getStandaloneTags() {
    return standaloneTags;
  }

  private void addJavadocMember(Object obj) {
    javadocMembers.add(obj);
  }

  public void callVisit(LocalVariableIndexer indexer) {
    for (int i = 0, max = javadocParams.size(); i < max; i++) {
      indexer.visit((BinVariableUseExpression) javadocParams.get(i));
    }
  }

  public void accept(BinItemVisitor visitor) {
    for (int i = 0, max = javadocMembers.size(); i < max; i++) {
      ((BinItem) javadocMembers.get(i)).accept(visitor);
    }
  }

  public String toString() {
    final StringBuffer result = new StringBuffer();

    for (Iterator i = getStandaloneTags().iterator(); i.hasNext(); ) {
      final Tag tag = (Tag) i.next();
      if (result.length() > 0) {
        result.append("\n");
      }

      result.append("<TAG>").append(tag.getLinkName()).append("</TAG>");
    }

    result.append(Integer.toHexString(hashCode()));

    return result.toString();
  }

  /**
   * Finds all javadoc comments from standard input.
   * Prints results to console.
   */
  public static void main(String params[]) throws Exception {
    // Doesn't really parse the file. So if /** or */ is encountered
    // somewhere in string literals... Doesn't work.
    final BufferedReader source =
        new BufferedReader(new InputStreamReader(System.in));
    while (source.ready()) {
      final Javadoc javadoc = parse(source, 0, 0);
      if (javadoc != null) {
        System.out.println("*** JAVADOC *** ");
        System.out.println(javadoc);
        System.out.println("*** END JAVADOC ***");
      }
    }
  }

  private ASTImpl createASTImpl(Javadoc.JdocBinMemberData data) {
    ASTImpl astNode = new JavadocASTImpl(IDENT, data.memberName);
    astNode.setStartColumn(data.memberColStart);
    astNode.setEndColumn(data.memberColEnd);
    astNode.setStartLine(data.memberRowStart);
    astNode.setEndLine(data.memberRowStart);

    return astNode;
  }

  private void createVarUseExpression(BinParameter param,
      Javadoc.JdocBinMemberData data) {
    ASTImpl astNode = createASTImpl(data);

    BinVariableUseExpression expr = new BinVariableUseExpression(param, astNode) {
      private ASTImpl node;

      protected void setRootAst(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getRootAst() {
        return node;
      }
    };
    javadocParams.add(expr);
  }

  private BinMethodInvocationExpression createMethodInvocationExpression(
      BinMethod method, BinCITypeExpression typeExpr,
      BinExpressionList expressionList, BinTypeRef type,
      Javadoc.JdocBinMemberData data
      ) {
    ASTImpl astNode = createASTImpl(data);

    BinMethodInvocationExpression expr = new BinMethodInvocationExpression(
        method, typeExpr, expressionList, type,
        astNode) {
      private ASTImpl node;

      public void setNameAst(ASTImpl nodeName) {
        this.node = nodeName;
      }

      public ASTImpl getNameAst() {
        return node;
      }

      protected void setRootAst(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getRootAst() {
        return node;
      }
    };

    return expr;
  }

  private BinFieldInvocationExpression createFieldInvocationExpression(
      BinField field, BinCITypeExpression typeExpr,
      BinTypeRef type,
      Javadoc.JdocBinMemberData data
      ) {
    ASTImpl astNode = createASTImpl(data);

    BinFieldInvocationExpression expr = new BinFieldInvocationExpression(
        field, typeExpr, type, astNode) {
      private ASTImpl node;

      public void setNameAst(ASTImpl nodeName) {
        this.node = nodeName;
      }

      public ASTImpl getNameAst() {
        return node;
      }

      protected void setRootAst(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getRootAst() {
        return node;
      }
    };

    return expr;
  }

  private BinCITypeExpression createBinCITypeExpression(BinTypeRef type,
      CompilationUnit compilationUnit,
      Javadoc.JdocBinMemberData data) {
    ASTImpl astNode = createASTImpl(data);

    BinTypeRef typeData = new BinSourceTypeRef(compilationUnit, astNode, type) {
      private ASTImpl node;

      public void setNode(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getNode() {
        return this.node;
      }
    };

    BinCITypeExpression expr = new BinCITypeExpression(typeData, null, astNode) {
      private ASTImpl node;

      protected void setRootAst(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getRootAst() {
        return node;
      }
    };

    return expr;
  }

  private void createPackageUsageInfo(BinPackage _package,
      CompilationUnit compilationUnit,
      Javadoc.JdocBinMemberData data) {
    ASTImpl astNode = createASTImpl(data);

    PackageUsageInfo usageInfo = new PackageUsageInfo(astNode, _package,
        true, compilationUnit) {
      private ASTImpl node;

      public void setNode(ASTImpl node) {
        this.node = node;
      }

      public ASTImpl getNode() {
        return this.node;
      }
    };

    compilationUnit.addPackageUsageInfo(usageInfo);
  }

  static class Position {
    int row = 0;
    int column = 0;
  }


  static class JdocBinMemberData {
    int memberColStart;
    int memberColEnd;
    int memberRowStart;

    String memberName = "";
  }


  public static class SplitName {
    String memberName = "";
    private String ownerName = "";
    private String packageName = "";
    String params = "";
    boolean isMethod = false;

    public SplitName(String text) {
      checkForMemberInFullName(text);
    }

    private void checkForMemberInFullName(String fullName) {
      int index;

      if ((index = fullName.indexOf('#')) != -1) {
        memberName = fullName.substring(index + 1, fullName.length());
        ownerName = fullName.substring(0, index);

        checkForParametersInMemberName();
      } else {
        ownerName = fullName;
      }
    }

    private void checkForParametersInMemberName() {
      int index, index2;

      if ((index = memberName.indexOf('(')) != -1) {
        if ((index2 = memberName.indexOf(')')) != -1) {
          params = memberName.substring(index + 1, index2);
          memberName = memberName.substring(0, index);
          isMethod = true;

          leaveOnlyParamType();
        }
      }
    }

    private void leaveOnlyParamType() {
      StringBuffer result = new StringBuffer();
      StringTokenizer st1 = new StringTokenizer(params, ",");

      if (st1.countTokens() == 0) {
        return;
      }

      while (st1.hasMoreTokens()) {
        String nextToken = st1.nextToken();
        if (nextToken.length() == 0) {
          continue;
        }

        StringTokenizer st2 = new StringTokenizer(nextToken);
        result.append(st2.nextToken().trim() + " ");
      }

      params = result.toString();
    }

    public String extractTypeName(String text) {
      int index;

      if ((index = text.lastIndexOf('.')) != -1) {
        return text.substring(index + 1, text.length());
      }
      return text;
    }

    public String[] getOwnerNameByParts() {
      int count = 0;

      StringTokenizer st = new StringTokenizer(ownerName, ".");
      count = st.countTokens();

      if (count > 0) {
        String[] parts = new String[count];
        for (int i = 0; i < count; i++) {
          if (i > 0) {
            parts[i] = parts[i - 1] + '.' + st.nextToken();
          } else {
            parts[i] = st.nextToken();
          }
        }
        return parts;
      } else {
        String[] tmp = {ownerName};
        return tmp;
      }
    }

    public boolean isSplitNameValid() {
      if (ownerName.length() == 0 && memberName.length() == 0) {
        if (packageName.length() == 0) {
          return false;
        }
      }
      return true;
    }

    public boolean memberIsPresent() {
      return (memberName.length() != 0);
    }

    public boolean ownerIsPresent() {
      return (ownerName.length() != 0);
    }

    public String getOwnerName() {
      return ownerName;
    }

    public String getMemberName() {
      return memberName;
    }

    public String getParams() {
      return params;
    }

    public boolean isMethod() {
      return isMethod;
    }

    public void printNames() {
      System.out.println("package:" + packageName);
      System.out.println("type:" + ownerName);
      System.out.println("member:" + memberName);
      System.out.println("params:" + params);
    }

    public String toString() {
      return ClassUtil.getShortClassName(this) + ": "
          + packageName + ", " + ownerName + ", " + memberName + ", " + params;
    }
  }


  public static boolean equalParameters(BinMethod method, String stringParams) {
    BinParameter[] params = method.getParameters();
    StringTokenizer st = new StringTokenizer(stringParams);
    if (params.length != st.countTokens()) {
      return false;
    }

    Project project = method.getProject();

    for (int i = 0; i < params.length; i++) {
      String nextToken = st.nextToken().trim();
      BinTypeRef ref = null;
      if (nextToken.charAt(nextToken.length() - 1) == ']') {
        nextToken = BinArrayType.toInternalForm(nextToken);
      } else {
        ref = project.findPrimitiveTypeForName(nextToken);
      }

      if (ref == null) {
        ref = tryToResolveName(nextToken, null, method.getCompilationUnit(), project);
      }

      if (!params[i].getTypeRef().equals(ref)) {
        return false;
      }
    }

    return true;
  }

  private void tryToCreateMethodInvocationExpr(BinTypeRef currentType,
      Javadoc.SplitName splitName,
      Tag tag,
      int startCommentLine,
      BinCITypeExpression expr) {
//System.err.println("tryToCreateMethodInvocationExpr: " + currentType
//  + ", " + splitName + ", " + tag + ", " + startCommentLine
//  + ", " + expr);
    String name = splitName.memberName;
    String params = splitName.params;

    Javadoc.JdocBinMemberData data;

    BinMethod[] methods = currentType.getBinCIType()
        .getAccessibleMethods(name, currentType.getBinCIType());

    for (int i = 0; i < methods.length; i++) {
      if (equalParameters(methods[i], params)) {
        data = getJavadocData(tag, startCommentLine, name);
        javadocMembers.add(
            createMethodInvocationExpression(methods[i], expr,
            new BinExpressionList(methods[i].getParameters()),
            currentType, data));
        ((DependencyParticipant) methods[i].getOwner()).addDependable(currentType);
      }
    }
  }

  private void tryToCreateFieldInvocationExpr(BinTypeRef currentType,
      Javadoc.SplitName splitName,
      Tag tag,
      int startCommentLine,
      BinCITypeExpression expr) {
    String name = splitName.memberName;
    Javadoc.JdocBinMemberData data;

    List fields = currentType.getBinCIType().
        getAccessibleFields(currentType.getBinCIType());

    BinField field;
    for (int i = 0, max = fields.size(); i < max; i++) {
      field = (BinField) fields.get(i);
      if (name.equals(field.getName())) {
        data = getJavadocData(tag, startCommentLine, name);

        javadocMembers.add(
            createFieldInvocationExpression(field, expr, currentType, data));

        ((DependencyParticipant) field.getOwner()).addDependable(currentType);
      }
    }
  }

  private BinCITypeExpression tryToCreateBinCITypeExpression(BinTypeRef
      currentType,
      Javadoc.SplitName splitName,
      Tag tag,
      int startCommentLine) {
    String[] parts = splitName.getOwnerNameByParts();
    BinTypeRef owner = null;
    BinCITypeExpression expr = null;
    Javadoc.JdocBinMemberData data;
    CompilationUnit compilationUnit = currentType.getBinCIType().getCompilationUnit();
    String typeName;

    for (int j = 0; j < parts.length; j++) {
      owner = tryToResolveName(parts[j], currentType, compilationUnit,
          currentType.getProject());
      if (owner != null) {
        typeName = splitName.extractTypeName(parts[j]);
        if (typeName.length() > 0) {
          data = getJavadocData(tag, startCommentLine, typeName);
          expr = createBinCITypeExpression(owner, compilationUnit, data);
          addJavadocMember(expr);

          ((DependencyParticipant) owner).addDependable(currentType);
        }
      }
    }
    return expr;
  }

  private void tryToAddIntoPackageUsageInfo(BinTypeRef currentType,
      CompilationUnit compilationUnit,
      Javadoc.SplitName splitName,
      Tag tag,
      int startCommentLine) {
    BinPackage[] packages = compilationUnit.getProject().getAllPackages();
    Javadoc.JdocBinMemberData data;
    BinTypeRef owner = null;
    String[] parts = splitName.getOwnerNameByParts();

    for (int i = parts.length - 1; i > -1; i--) {
      owner = tryToResolveName(parts[i], currentType, compilationUnit,
          compilationUnit.getProject());
      if (owner == null) {
        for (int j = 0; j < packages.length; j++) {
          if (packages[j].getQualifiedName().equals(parts[i])) {
            data = getJavadocData(tag, startCommentLine, parts[i]);
            createPackageUsageInfo(packages[j], compilationUnit, data);
          }
        }
        break;
      }
    }
  }

  public static Javadoc parseIntoFakeClassmodel(
      BinMember member, JavadocComment comment, int startColumn
      ) {
    int startCommentLine = comment.getStartLine();
    Tag tag;
    Javadoc.JdocBinMemberData data;

    int wantedTags = JavadocComment.PARAM_TAG | JavadocComment.AUDIT_TAG;

    Javadoc javadoc = createJavadocInstance(wantedTags, comment, startColumn);
    if (javadoc == null) {
      return null;
    }

    if (member instanceof BinMethod){
      List tags = javadoc.getStandaloneTags();

      for (int i = 0, max = tags.size(); i < max; i++) {
        tag = (Tag) tags.get(i);

  //      String tagName = tag.getName();

        String paramName = tag.getLinkName();
        BinParameter[] params = ((BinMethod) member).getParameters();

        for (int j = 0; j < params.length; j++) {
          if (params[j].getName().equals(paramName)) {
            data = javadoc.getJavadocData(tag, startCommentLine, paramName);
            javadoc.createVarUseExpression(params[j], data);
          }
        }
      }
    }

    return javadoc;
  }

  public static Javadoc parseIntoFakeClassmodel(JavadocComment comment,
      BinTypeRef currentType,
      CompilationUnit compilationUnit,
      int startColumn) {
    int startCommentLine = comment.getStartLine();

    int wantedTags = JavadocComment.LINK_TAG | JavadocComment.SEE_TAG |
        JavadocComment.THROWS_TAG1 | JavadocComment.THROWS_TAG2 |
        JavadocComment.AUDIT_TAG;

    Javadoc javadoc = createJavadocInstance(wantedTags, comment, startColumn);
    if (javadoc == null) {
      return null;
    }

    Tag tag;
    List tags = javadoc.getStandaloneTags();

    for (int i = 0, max = tags.size(); i < max; i++) {
      tag = (Tag) tags.get(i);

      Javadoc.SplitName splitName = new Javadoc.SplitName(tag.getLinkName());

      if (!splitName.isSplitNameValid()) {
        continue;
      }
      if (!splitName.ownerIsPresent()) { // {@link #member}
        // create member
        if (!splitName.isMethod()) {
          javadoc.tryToCreateFieldInvocationExpr(currentType, splitName, tag,
              startCommentLine, null);
        }
        javadoc.tryToCreateMethodInvocationExpr(currentType, splitName, tag,
            startCommentLine, null);
      } else { // {@link owner#member} or {@link owner}
        javadoc.tryToAddIntoPackageUsageInfo(currentType, compilationUnit, splitName,
            tag, startCommentLine);

        BinCITypeExpression expr = javadoc.tryToCreateBinCITypeExpression(
            currentType,
            splitName,
            tag,
            startCommentLine);
        if (expr == null) {
          continue;
        }

        BinTypeRef owner = expr.getReturnType();

        if (splitName.memberIsPresent()) { // {@link owner#member}
          // create member
          if (!splitName.isMethod()) {
            javadoc.tryToCreateFieldInvocationExpr(owner, splitName, tag,
                startCommentLine, expr);
          }
          javadoc.tryToCreateMethodInvocationExpr(owner, splitName, tag,
              startCommentLine, expr);
        }
      }
    }

    return javadoc;
  }

  public static Javadoc createJavadocInstance(int wantedTags,
      JavadocComment comment,
      int startColumn) {
    String commentText = comment.getText();
    final BufferedReader text = new BufferedReader(new StringReader(commentText));
    Javadoc javadoc = null;
    try {
      javadoc = Javadoc.parse(text, 0, startColumn, wantedTags);
    } catch (IOException e) {
      return null;
    }

    return javadoc;
  }

  public static BinTypeRef tryToResolveName(final String name,
      final BinTypeRef currentType, final CompilationUnit compilationUnit,
      final Project project) {
    BinTypeRef owner = null;

    try {
      if (currentType != null && currentType.getBinCIType().isFromCompilationUnit()) {
        owner = currentType.getResolver().resolve(name);
      }
    } catch (Exception e) {
      owner = null;
    }

    try {
      if (owner == null && compilationUnit != null) {
        owner = compilationUnit.resolve(name, null);
      }
    } catch (Exception e) {
      owner = null;
    }

    try {
      if (owner == null && project != null) {
        owner = project.getTypeRefForName(name);
      }
    } catch (Exception e) {
      owner = null;
    }

    return owner;
  }

  private Javadoc.JdocBinMemberData getJavadocData(final Tag tag,
      int startCommentLine, String memberName) {
    Javadoc.JdocBinMemberData data = new Javadoc.JdocBinMemberData();

    data.memberName = memberName;

//    String linkName = tag.getLinkName();

    data.memberColStart = tag.getLinkStartColumn() +
        tag.getLinkName().indexOf(data.memberName) + 1;
    data.memberColEnd = data.memberColStart + data.memberName.length();
    data.memberRowStart = tag.getStartRow() + startCommentLine;

    return data;
  }

  public static Javadoc parse(
      BufferedReader source, int startRow, int startColumn
      ) throws IOException {
    return parse(source, startRow, startColumn, 0);
  }

  /**
   * Finds and parses first javadoc comment found in the source.
   *
   * @param source source.
   * @param startRow zero-based row source starts on.
   * @param startColumn zero-based column on row source starts on.
   *
   * @return javadoc comment found or <code>null</code> if no javadoc
   *         comment found in <code>source</code>.
   *
   * @throws IOException if an I/O error occurs.
   */
  private static Javadoc parse(BufferedReader source,
      int startRow,
      int startColumn, int wantedTags) throws IOException {
    Tag lastTag = null;
    boolean tagSectionStarted = false;
    boolean firstLine = true;
    boolean lastLine = false;
    boolean javadocFound = false;
    boolean firstSourceLine = true;
    String line;
    String tagName = null;
    List standaloneTags = new ArrayList();
    Tag tag = null;

    final Position position = new Position();
    position.row = startRow;
    position.column = startColumn;

    while ((line = source.readLine()) != null) {
      if (firstSourceLine) {
        firstSourceLine = false;
      } else {
        position.row++;
        position.column = 0;
      }
      if (lastLine) {
        break;
      }

      if (!javadocFound) {
        if (line.indexOf("/**") != -1) {
          javadocFound = true;
        } else {
          continue;
        }
      }

      if ((javadocFound) && (line.indexOf("*/") != -1)) {
        // last line of javadoc
        line = line.substring(0, line.indexOf("*/"));
        lastLine = true;
      }

      line = extractLine(position, line, firstLine);
      firstLine = false;
      if (line == null) {
        break; // end of javadoc
      }
      final String trimmedLine = skipWhitespace(position, line);

      if (trimmedLine.startsWith("@")) {
        tagSectionStarted = true;
        lastTag = null;
      }

      tag = createTag(lastTag, trimmedLine, wantedTags, position.row,
          position.column, standaloneTags, tagSectionStarted);

      if (tag != null) {
        lastTag = tag;

        tagName = tag.getName();

        if (tagName.length() > 0 && isTagWanted(wantedTags, tagName)) {
          standaloneTags.add(tag);
        }
      }

      if (lastLine) {
        break;
      }
    }

    if (javadocFound) {
//			printAllTags(standaloneTags);
      return new Javadoc(standaloneTags);
    } else {
      return null;
    }
  }

  private static Tag createTagInstanceByName(String tagName) {
    if ("see".equals(tagName) || "link".equals(tagName)) {
      return new TagLinkLabelType(tagName);
    }

    if ("param".equals(tagName) || "throws".equals(tagName) ||
        "exception".equals(tagName)) {
      return new TagLinkTextType(tagName);
    }

    if ("return".equals(tagName) || "deprecated".equals(tagName) ||
        "since".equals(tagName) || "author".equals(tagName) ||
        "version".equals(tagName)) {
      return new TagTextType(tagName);
    }

    if ("description".equals(tagName)) {
      return new TextType(tagName);
    }

    // XXX: Temp
    if ("audit".equals(tagName) || "violations".equals(tagName)) {
      return new TagLinkTextType(tagName);
    }

    return null;
  }

  public static Tag createTag(Tag lastTag, String line, int wantedTags,
      int row, int column,
      List standaloneTags, boolean tagSectionStarted) {
    String tagName = "";
    String tagText = "";
    Javadoc.Position position = new Javadoc.Position();
    Tag tag = null;
    int tagEnd = 0;
    int textStart = column;

    if (line.length() == 0) {
      return null;
    }

    final char[] chars = line.toCharArray();
    if (chars[0] == '@') {
      tagEnd = extractTagName(wantedTags, chars);

      if (tagEnd != -1 && tagEnd < chars.length) {
        tagName = (new String(chars, 1, tagEnd - 1));
        tagText = (new String(chars, tagEnd + 1, chars.length - tagEnd - 1));
        tagText = skipWhitespace(position, tagText);

        tag = createTagInstanceByName(tagName);

        if (tag != null) {
          tag.setStartRow(row);
          textStart = column + tagEnd + position.column + 1;
        }
      }
    } else {
      if (lastTag != null) {
        tag = lastTag;
      }

      if (!tagSectionStarted && tag == null) {
        // description
        tag = createTagInstanceByName("description");
      }
    }

    if (tag != null) {
      tag.parseText(tagText.length() > 0 ? tagText : line, wantedTags,
          row, textStart, standaloneTags, tagSectionStarted);

      if (lastTag != tag) {
        return tag;
      }
    }

    return null;
  }

  private static int extractTagName(int wantedTags, char[] chars) {
    int i = 1;
    while (i < chars.length && chars[i] != ' ' && chars[i] != '\t') {
      i++;
      if (i == chars.length) {
        return -1;
      }
    }

    return i;
  }

  private static boolean isTagWanted(int wantedTags, String tagName) {
    return (
        wantedTags == 0 ||
        ((wantedTags & JavadocComment.SEE_TAG) > 0 && "see".equals(tagName)) ||
        ((wantedTags & JavadocComment.LINK_TAG) > 0 && "link".equals(tagName)) ||
        ((wantedTags & JavadocComment.THROWS_TAG1) > 0
        && "throws".equals(tagName)) ||
        ((wantedTags & JavadocComment.THROWS_TAG2) > 0
        && "exception".equals(tagName)) ||
        ((wantedTags & JavadocComment.PARAM_TAG) > 0 && "param".equals(tagName)) ||
        ((wantedTags & JavadocComment.AUDIT_TAG) > 0 && "audit".equals(tagName)) // XXX: Temp
        );
  }

  /**
   * Extracts relevant javadoc line from the line.
   *
   * @param position position raw line starts at in source. Position is changed
   *        to reflect starting position of extracted line.
   * @param line raw line.
   * @param firstLine whether this is first line of javadoc.
   *
   * @return relevant part of the line. <code>null</code> if this line is end of javadoc.
   */
  private static String extractLine(Position position,
      String line,
      boolean firstLine) {

    if (line.indexOf('*') == -1) {
      if (firstLine) {
        return null; // end of javadoc
      } else {
        return line;
      }
    }

    // Skip util *
    final char[] chars = line.toCharArray();
    final int charsLength = chars.length;
    int pos = 0;
    while ((pos < charsLength) && (chars[pos] != '*')) {
      pos++;
    }

    pos++;

    if (firstLine) {
      if (pos >= charsLength) {
        return null; // not a first line javadoc
      } else {
        if (chars[pos] == '*') {
          pos++; // OK. javadoc started
        } else {
          return null; // not a first line javadoc
        }
      }
    }

    position.column += pos;
    if (pos >= charsLength) {
      return "";
    }

    if (chars[pos] == '/') {
      return null; // end of javadoc
    }

    return String.valueOf(chars, pos, charsLength - pos);
  }

//  /**
//   * Removes leading whitespace of the line.
//   *
//   * @param line line.
//   *
//   * @return line without leading whitespace.
//   */
//  private static String skipWhitespace(String line) {
//    return skipWhitespace(new Position(), line);
//  }

  /**
   * Removes leading whitespace of the line.
   *
   * @param position position line starts at in source. Position is changed to
   *        reflect starting position of returned line.
   * @param line line.
   *
   * @return line without leading whitespace.
   */
  static String skipWhitespace(Position position, String line) {
    if (line.length() == 0) {
      return line;
    }

    if (line.charAt(0) != ' ' && line.charAt(0) != '\t') {
      return line;
    }

    final char[] chars = line.toCharArray();
    final int charsLength = chars.length;
    int pos = 1;
    while ((pos < charsLength)
        && ((chars[pos] == ' ') || (chars[pos] == '\t'))) {
      pos++;
    }

    position.column += pos;
    if (pos >= charsLength) {
      return "";
    }

    return String.valueOf(chars, pos, charsLength - pos);
  }

//	private static void printAllTags(List standaloneTags) {
//		System.out.println("");
//		for(int i = 0, max = standaloneTags.size(); i < max; i++) {
//			Tag tmp = (Tag)standaloneTags.get(i);
//			System.out.println("name:" + tmp.getName() + ";");
////			System.out.println("text:" + tmp.getText() + ";");
//			System.out.println("link:" + tmp.getLinkName() + ";");
//			System.out.println("row:" + tmp.getStartRow() + ";");
////			System.out.println("name column:" + tmp.getNameStartColumn() + ";");
////			System.out.println("text column:" + tmp.getTextStartColumn() + ";");
//			System.out.println("");
//			if(!tmp.areInnerTagsEmpty()) {
//				List inners = tmp.getInnerTags();
//				for(int j = 0, maxj = inners.size(); j < maxj; j++) {
//					Tag tmp2 = (Tag)inners.get(j);
//					System.out.println("	name:" + tmp2.getName() + ";");
////					System.out.println("	text:" + tmp2.getText() + ";");
//					System.out.println("	link:" + tmp2.getLinkName() + ";");
//					System.out.println("	row:" + tmp2.getStartRow() + ";");
////					System.out.println("	name column:" + tmp.getNameStartColumn() + ";");
////					System.out.println("	text column:" + tmp.getTextStartColumn() + ";");
//					System.out.println("");
//				}
//			}
//		}
//	}
}
