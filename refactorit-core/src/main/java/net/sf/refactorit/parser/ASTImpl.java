/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.vfs.Source;

import rantlr.collections.AST;
import rantlr.collections.ASTEnumeration;


public interface ASTImpl extends AST, Comparable {

  int getTextLength();

  int getLine();

  int getStartLine();

  int getColumn();

  int getStartColumn();

  int getEndLine();

  int getEndColumn();

  void setLine(int line);

  void setStartLine(int line);

  void setColumn(int column);

  void setStartColumn(int line);

  void setEndLine(int line);

  void setEndColumn(int column);

  public void setParent(final ASTImpl parent);

  public ASTImpl getParent();

  public Source getSource();

  public void setSource(final Source source);

  // from AST
  void addChild(AST aST);

  boolean equals(AST aST);

  boolean equalsList(AST aST);

  boolean equalsListPartial(AST aST);

  boolean equalsTree(AST aST);

  boolean equalsTreePartial(AST aST);

  ASTEnumeration findAll(AST aST);

  ASTEnumeration findAllPartial(AST aST);

  AST getFirstChild();

  AST getNextSibling();

  String getText();

  int getType();

  void initialize(rantlr.Token token);

  void initialize(AST aST);

  void initialize(int param, String str);

  void setFirstChild(AST aST);

  void setNextSibling(AST aST);

  void setText(String str);

  void setType(int param);

  String toStringList();

  String toStringTree();

  int compareTo(Object obj);

  int getLevel();

  int[] LEVEL13 = {
      JavaTokenTypes.ASSIGN,
      JavaTokenTypes.PLUS_ASSIGN,
      JavaTokenTypes.MINUS_ASSIGN,
      JavaTokenTypes.STAR_ASSIGN,
      JavaTokenTypes.DIV_ASSIGN,
      JavaTokenTypes.MOD_ASSIGN,
      JavaTokenTypes.SR_ASSIGN,
      JavaTokenTypes.BSR_ASSIGN,
      JavaTokenTypes.SL_ASSIGN,
      JavaTokenTypes.BAND_ASSIGN,
      JavaTokenTypes.BXOR_ASSIGN,
      JavaTokenTypes.BOR_ASSIGN
  };
  int[] LEVEL12 = {JavaTokenTypes.QUESTION};
  int[] LEVEL11 = {JavaTokenTypes.LOR};
  int[] LEVEL10 = {JavaTokenTypes.LAND};
  int[] LEVEL9 = {JavaTokenTypes.BOR};
  int[] LEVEL8 = {JavaTokenTypes.BXOR};
  int[] LEVEL7 = {JavaTokenTypes.BAND};
  int[] LEVEL6 = {
      JavaTokenTypes.NOT_EQUAL, JavaTokenTypes.EQUAL};
  int[] LEVEL5 = {JavaTokenTypes.LT, JavaTokenTypes.GT,
      JavaTokenTypes.LE, JavaTokenTypes.GE};
  int[] LEVEL4 = {JavaTokenTypes.SL, JavaTokenTypes.SR,
      JavaTokenTypes.BSR};
  int[] LEVEL3 = {JavaTokenTypes.PLUS, JavaTokenTypes.MINUS};
  int[] LEVEL2 = {JavaTokenTypes.STAR, JavaTokenTypes.DIV,
      JavaTokenTypes.MOD};
  int[] LEVEL1 = {JavaTokenTypes.INC, JavaTokenTypes.DEC,
      JavaTokenTypes.POST_INC, JavaTokenTypes.POST_DEC,
      JavaTokenTypes.UNARY_MINUS, JavaTokenTypes.UNARY_PLUS,
      JavaTokenTypes.LNOT, JavaTokenTypes.BNOT};

  Object[] LEVELS
      = {LEVEL13, LEVEL12, LEVEL11, LEVEL10, LEVEL9,
      LEVEL8, LEVEL7, LEVEL6, LEVEL5, LEVEL4, LEVEL3, LEVEL2, LEVEL1};
}
