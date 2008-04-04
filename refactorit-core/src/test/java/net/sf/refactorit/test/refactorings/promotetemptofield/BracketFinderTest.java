/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.transformations.BracketFinder;
import net.sf.refactorit.transformations.DeleteTransformation;
import net.sf.refactorit.transformations.TransformationManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author  RISTO A
 */
public class BracketFinderTest extends TestCase {
  public BracketFinderTest(String name) {super(name);
  }

  public static Test suite() {
    return new TestSuite(BracketFinderTest.class);
  }

  public void testRegularErase() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int i;");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testLaterVariableInMultivarDeclaration() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int j, i;");
    Project after = Utils.createTestRbProjectFromMethodBody("j, i;");

    assertTypeNodeErase(before, after);
  }

  public void testArrayType() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int[][] i;");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testNoAstsPointAtBracketEnd() throws Exception {
    BinLocalVariable var = Utils
        .createLocalVariableDeclarationFromString("int i[], j[];");

    // These values are useless -- we'd expect something like 8, not 14
    assertEquals(14, var.getEndColumn());
    assertEquals(14, var.getRootAst().getEndColumn());
    assertEquals(14, var.getOffsetNode().getEndColumn());
  }

  public void testFindingSimpleBracket() throws Exception {
    BinLocalVariable var = Utils
        .createLocalVariableDeclarationFromString("int i[  ];");

    ASTImpl typeNode = var.getTypeAst();

    assertEquals("[", typeNode.getText());
    assertEquals(6, typeNode.getStartColumn());
    assertEquals(7, typeNode.getEndColumn());

    typeNode = (ASTImpl) typeNode.getFirstChild();
    assertTrue(ASTUtil.isBefore(typeNode, var.getNameAstOrNull()));
  }

  public void testFindingDoubleBracket() throws Exception {
    BinLocalVariable var = Utils
        .createLocalVariableDeclarationFromString("int i[  ][  ];");

    ASTImpl typeNode = var.getTypeAst();

    assertEquals("[", typeNode.getText());
    assertEquals(10, typeNode.getStartColumn());
    assertEquals(11, typeNode.getEndColumn());

    typeNode = (ASTImpl) typeNode.getFirstChild();
    assertEquals("[", typeNode.getText());
    assertEquals(6, typeNode.getStartColumn());
    assertEquals(7, typeNode.getEndColumn());

    typeNode = (ASTImpl) typeNode.getFirstChild();
    assertTrue(ASTUtil.isBefore(typeNode, var.getNameAstOrNull()));
  }

  public void testFindingDoubleBracket_typeNodeHasAlsoAnArrayModifier() throws
      Exception {
    BinLocalVariable var = Utils
        .createLocalVariableDeclarationFromString("int[] i[  ][  ];");

    ASTImpl typeNode = var.getTypeAst();

    assertEquals("[", typeNode.getText());
    assertEquals(12, typeNode.getStartColumn());
    assertEquals(13, typeNode.getEndColumn());

    typeNode = (ASTImpl) typeNode.getFirstChild();
    assertEquals("[", typeNode.getText());
    assertEquals(8, typeNode.getStartColumn());
    assertEquals(9, typeNode.getEndColumn());

    typeNode = (ASTImpl) typeNode.getFirstChild();
    assertTrue(ASTUtil.isBefore(typeNode, var.getNameAstOrNull()));
  }

  public void testBefore() {
    assertTrue(new SourceCoordinate(0, 0).isBefore(new SourceCoordinate(1, 0)));
    assertFalse(new SourceCoordinate(1, 0).isBefore(new SourceCoordinate(0, 0)));

    assertTrue(new SourceCoordinate(0, 0).isBefore(new SourceCoordinate(0, 1)));
    assertFalse(new SourceCoordinate(0, 1).isBefore(new SourceCoordinate(0, 0)));

    assertFalse(new SourceCoordinate(1, 0).isBefore(new SourceCoordinate(0, 1)));
  }

  public void testCountBracketsAfterVarName() {
    assertEquals(0, BracketFinder.getBracketCountAfterName(
        Utils.createLocalVariableDeclarationFromString("int i;")));
    assertEquals(0, BracketFinder.getBracketCountAfterName(
        Utils.createLocalVariableDeclarationFromString("int[] i;")));
    assertEquals(1, BracketFinder.getBracketCountAfterName(
        Utils.createLocalVariableDeclarationFromString("int[] i[  ];")));
    assertEquals(2, BracketFinder.getBracketCountAfterName(
        Utils.createLocalVariableDeclarationFromString("int[] i[  ][];")));
    assertEquals(2, BracketFinder.getBracketCountAfterName(
        Utils.createLocalVariableDeclarationFromString("int[] a, i[][];")));
  }

  public void testFindingNextClosingBracket() {
    BinLocalVariable var;

    var = Utils.createLocalVariableDeclarationFromString("int i[];");
    assertEquals(new SourceCoordinate(2, 7),
        BracketFinder.findNextClosingBracket(var.getCompilationUnit(),
        new SourceCoordinate(2, 6)));

    var = Utils.createLocalVariableDeclarationFromString("int i[][];");
    assertEquals(new SourceCoordinate(2, 9),
        BracketFinder.findNextClosingBracket(var.getCompilationUnit(),
        new SourceCoordinate(2, 7)));

    var = Utils.createLocalVariableDeclarationFromString("int i[/*]*/" + "\n"
        + "];");
    assertEquals(new SourceCoordinate(3, 1),
        BracketFinder.findNextClosingBracket(var.getCompilationUnit(),
        new SourceCoordinate(2, 1)));
  }

  public void testHasBracketsAfterName() {
    assertFalse(BracketFinder.hasBracketsAfterName(
        Utils.createLocalVariableDeclarationFromString("int i;")));
    assertTrue(BracketFinder.hasBracketsAfterName(
        Utils.createLocalVariableDeclarationFromString("int i[];")));
  }

  public void testFindBracketsEndAfterName() {
    BinLocalVariable var;

    var = Utils.createLocalVariableDeclarationFromString("int i;");
    assertEquals(null, BracketFinder.findBracketsEndAfterName(var));

    var = Utils.createLocalVariableDeclarationFromString("int i[];");
    assertEquals(new SourceCoordinate(2, 7),
        BracketFinder.findBracketsEndAfterName(var));

    var = Utils.createLocalVariableDeclarationFromString("int i[][];");
    assertEquals(new SourceCoordinate(2, 9),
        BracketFinder.findBracketsEndAfterName(var));
  }

  public void testArrayDeclarationOnTypeNode() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int[][] i;");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testArrayDeclarationOnTypeNode_noSpace() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int[][]i;");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testMultiVariableDeclaration_variableNotFirstOne() throws
      Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int a, i[][];");
    Project after = Utils.createTestRbProjectFromMethodBody("a, i;");

    assertTypeNodeErase(before, after);
  }

  public void testArrayDeclarationAfterVariableName() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int i[][];");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testArrayDeclarationAfterVariableName_moreSpace() throws
      Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("int  i [  ][ ];");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  public void testFinalModifier() throws Exception {
    Project before = Utils.createTestRbProjectFromMethodBody("final int i[][];");
    Project after = Utils.createTestRbProjectFromMethodBody("i;");

    assertTypeNodeErase(before, after);
  }

  // Util methods

  private void assertTypeNodeErase(final Project before, final Project after) {
    BinLocalVariable var = getVariable(before);

    TransformationManager manager = new TransformationManager(null);
    manager.add(new DeleteTransformation(var,
        DeleteTransformation.DELETE_TYPE_NODE));
    manager.performTransformations();

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  private BinLocalVariable getVariable(final Project before) {
    return (BinLocalVariable) ItemByNameFinder.findVariable(before, "i");
  }
}
