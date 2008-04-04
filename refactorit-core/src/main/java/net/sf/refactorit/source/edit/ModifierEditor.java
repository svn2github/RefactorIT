/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.classmodel.BinAnnotation;
import net.sf.refactorit.classmodel.BinEnum;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.format.BinModifierFormatter;

import java.io.IOException;
import java.util.List;


/**
 * Changes modifier of the member.
 *
 * @author Anton Safonov
 * @author Arseni Grigorjev
 */
public class ModifierEditor extends DefaultEditor {
  private BinMember member;
  private int newModifier;

  public ModifierEditor(BinMember member, int newModifier) {
    super(member.getCompilationUnit());
    this.member = member;
    this.newModifier = newModifier;
  }

  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    try {
      if (member.getModifiers() == newModifier) {
        // add-hoc, interface has default modifiers
        BinTypeRef owner = member.getOwner();
        if (owner != null && !owner.getBinCIType().isInterface()) {
          return status;
        }
      }

      List oldModifNodes = member.getModifierNodes();

      //<FIX> Aleksei Sosnovski 09.2005
      //annotations should remain untouched,
      //so we must only consider non-annotation modifiers
      for (int i = 0; i < oldModifNodes.size(); i++) {
        if (((ASTImpl) oldModifNodes.get(i)).getType() == JavaTokenTypes.ANNOTATION) {
          oldModifNodes.remove(i);
          i--;
        }
      }
      //ASTUtil.setAnnotationsNodesAsCompoundNodes(oldModifNodes);
      //</FIX>

      List newModif = BinModifier.splitModifier(newModifier);



      for (int i = 0; i < newModif.size(); i++) {
        // remove package private modifier, it has no string representation)
        if (((Integer) newModif.get(i)).intValue() == BinModifier.PACKAGE_PRIVATE) {
          newModif.remove(i);
        }
        // remove final modifier for enum and annotation, which are implicitly final
        if ((member instanceof BinEnum) || (member instanceof BinAnnotation)) {
          if (((Integer) newModif.get(i)).intValue() == BinModifier.FINAL) {
            newModif.remove(i);
          }
        }
      }

      final int upperBound = Math.min(oldModifNodes.size(), newModif.size());

      // First and most dificult case: there were no modifiers at all before
      // edit.

      if (oldModifNodes.size() == 0){
        // create line to insert
        String stringToInsert = createModifierString(newModif, upperBound);

        ASTImpl offsetNode = member.getOffsetNode();
        Line line = manager.getLine(member.getCompilationUnit(), offsetNode.getLine());

        char firstChar = line.charAt(offsetNode.getColumn()-1);
        stringToInsert = stringToInsert + " " + firstChar;
        line.replace(offsetNode.getColumn()-1, offsetNode.getColumn(),
            stringToInsert);
      }

      // Number of modifiers largened -> must add the rest in completely
      // new places (can`t reuse unexisting 'slots').

      else if (oldModifNodes.size() < newModif.size()){

        String insertString = createModifierString(newModif, upperBound);
        insertString = insertString + " ";

        ASTImpl lastNode = (ASTImpl) oldModifNodes.get(upperBound-1);
        Line line = manager.getLine(getTarget(), lastNode.getEndLine());
        line.insert(lastNode.getEndColumn(), insertString);
      }

      // Reuse 'slots' while possible

      for (int i = 0; i < upperBound; i++){
        ASTImpl node = (ASTImpl) oldModifNodes.get(i);

        final String modifStr;
        if(node.getType() == JavaTokenTypes.ANNOTATION) {
          modifStr =
              member.getCompilationUnit().getSource().getText(node) + " ";
        } else {
          modifStr = new BinModifierFormatter(((Integer) newModif.get(i))
            .intValue()).print();
        }
        Line line = manager.getLine(getTarget(), node.getStartLine());
        line.replace(node.getStartColumn()-1, node.getEndColumn()-1, modifStr);
      }

      // Number of modifiers shortened -> delete unused 'slots'
      // (if there are comments between modifiers - they will stay untouched)

      if (oldModifNodes.size() > newModif.size()){
        int curLineNum = 0, rem_start = 0, rem_end = 0;
        ASTImpl node = null;
        Line line = null;
        for (int i = upperBound; i < oldModifNodes.size(); i++){
          node = (ASTImpl) oldModifNodes.get(i);

          //to be uncommented if fix above (<FIX> Aleksei Sosnovski 09.2005)
          //is removed for some reasons
          /*
            //if it is an annotation, we should not remove it!
            if (node.getType() == JavaTokenTypes.ANNOTATION) {
              continue;
            }
          */

          // get line to work with
          if (line == null || node.getStartLine() != curLineNum){
            curLineNum = node.getStartLine();
            line = manager.getLine(getTarget(), curLineNum);
          }

          // from the very beginning we remove only modifier text
          rem_start = node.getStartColumn()-1;
          rem_end = node.getEndColumn()-1;

          // now we will try to enlarge remove bounds
          char c, c2;

          // remove whitespaces before modifier until '*/' characters
          // or do nothing if there is no comment on line.
          int ind = rem_start;
          while (ind-2 >= 0){
            c = line.charAt(ind-1);
            c2 = line.charAt(ind-2);
            if ((c == ' ' || c == '\t') && c2 != '/'){
              ind--;
            } else if (c == ' ' || c == '\t'){
              rem_start = ind;
              break;
            } else {
              break;
            }
          }

          // remove whitespaces after modifier
          while (rem_end < line.length()){
            c = line.charAt(rem_end);
            if (c == ' ' || c == '\t'){
              rem_end++;
            } else {
              break;
            }
          }
          line.delete(rem_start, rem_end);

          // if line is empty after deletion -> delete whole line
          if (line.getContent().trim().length() == 0){
            line.delete(0, line.length());
          }
        }
      }

//      old Anton`s code:
//
//      ASTImpl visibilityNode = this.member.getVisibilityNode();
//      if (EditorManager.debug) {
//        System.err.println("visibility node: " + visibilityNode + ", class: "
//            + visibilityNode.getClass());
//      }
//      int modifiers = this.newModifier;
//
//      List stripPrefixWhiteSpaceNodes = new ArrayList();
//
//      int lastLineNum = -1; // for verbose exception messages
//
//      List existingNodes = this.member.getModifierNodes();
//      try {
//        for (int i = 0, max = existingNodes.size(); i < max; i++) {
//          ASTImpl node = (ASTImpl) existingNodes.get(i);
//          int modifier = ASTUtil.getModifierForAST(node);
//
//          if (EditorManager.debug) {
//            //  System.err.println("modifiers: " + modifiers + ", modifier: " + modifier);
//          }
//
//          if (BinModifier.hasFlag(modifiers, modifier)) {
//            modifiers = BinModifier.clearFlags(modifiers, modifier);
//            if (EditorManager.debug) {
//              //  System.err.println("past clear - modifiers: " + modifiers + ", modifier: " + modifier);
//            }
//            continue;
//          }
//
//          lastLineNum = node.getStartLine();
//          Line line = manager.getLine(getInputSource(), getProject(),
//              lastLineNum);
//          int startColumn = node.getStartColumn() - 1;
//          int endColumn = node.getEndColumn() - 1;
//
//          if (BinModifier.hasFlag(BinModifier.PRIVILEGE_MASK, modifier)) {
//            // reuse
//            int newAccess = modifiers & BinModifier.PRIVILEGE_MASK;
//            if (newAccess == BinModifier.PACKAGE_PRIVATE) {
//              // fetch following space
//              int len = line.length();
//              while (endColumn < len
//                  && Character.isWhitespace(line.charAt(endColumn))) {
//                ++endColumn;
//              }
//
//              if (line.charAt(endColumn - 1) == '\n'
//                  || line.charAt(endColumn - 1) == '\r') {
//                stripPrefixWhiteSpaceNodes.add(node);
//              }
//
//              if (EditorManager.debug) {
//                System.err.println(lastLineNum + " - line before delete0: "
//                    + line);
//              }
//
//              line.delete(startColumn, endColumn);
//
//              if (EditorManager.debug) {
//                System.err.println(lastLineNum + " - line after delete0: "
//                    + line);
//              }
//            } else {
//              if (EditorManager.debug) {
//                System.err.println(lastLineNum + " - line before replace: "
//                    + line);
//              }
//              line.replace(
//                  startColumn,
//                  endColumn,
//                  new BinModifierFormatter(newAccess, false).print());
//              if (EditorManager.debug) {
//                System.err.println(lastLineNum + " - line after replace: "
//                    + line);
//              }
//            }
//            modifiers = BinModifier.clearFlags(modifiers, newAccess);
//          } else {
//            // fetch following space
//            int len = line.length();
//            while (endColumn < len
//                && Character.isWhitespace(line.charAt(endColumn))) {
//              ++endColumn;
//            }
//
//            if (line.charAt(endColumn - 1) == '\n'
//                || line.charAt(endColumn - 1) == '\r') {
//              stripPrefixWhiteSpaceNodes.add(node);
//            }
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line before delete1: "
//                  + line);
//            }
//
//            line.delete(startColumn, endColumn);
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line after delete1: " + line);
//            }
//          }
//        }
//
//        List modifiersToAdd = BinModifier.splitModifier(modifiers);
//        for (int i = modifiersToAdd.size() - 1; i >= 0; i--) {
//          int modifierToAdd = ((Integer) modifiersToAdd.get(i)).intValue();
//
//          if (EditorManager.debug) {
//            System.err.println("modifierToAdd: " + modifierToAdd);
//          }
//
//          if (modifierToAdd == 0) {
//            // package private has no string representation
//            continue;
//          }
//
//          ASTImpl node = visibilityNode;
//          String str = new BinModifierFormatter(modifierToAdd).print();
//
//          if (!BinModifier.hasFlag(BinModifier.PRIVILEGE_MASK, modifierToAdd) &&
//              existingNodes.size() > 0) {
//            node = (ASTImpl) existingNodes.get(existingNodes.size() - 1);
//            str = ' ' + str;
//
//            lastLineNum = node.getEndLine();
//            Line line = manager.getLine(getInputSource(), getProject(),
//                lastLineNum);
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line before insert: " + line);
//            }
//
//            line.insert(node.getEndColumn() - 1, str);
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line after insert: " + line);
//            }
//          } else {
//            // here is a trick to get LocationAware include this modifier into
//            // himself, so that when it moves it goes together with modifier
//
//            lastLineNum = node.getEndLine();
//            int column = 0;
//            Line line = null;
//            while (lastLineNum <= manager.getLineNumber(getInputSource(),
//                getProject())) {
//              line = manager.getLine(getInputSource(), getProject(),
//                  lastLineNum);
//
//              if (lastLineNum == node.getEndLine()) {
//                column = node.getStartColumn() - 1;
//              } else {
//                column = 0;
//              }
//
//              if (line.substring(column).trim().length() > 0) {
//                break;
//              }
//
//              ++lastLineNum;
//            }
//
//            if (Assert.enabled) {
//              Assert.must(
//                  lastLineNum <= manager.getLineNumber(getInputSource(),
//                  getProject()), "Found no code till the end of file: "
//                  + getInputSource().getRelativePath()
//                  );
//            }
//
//            while (column < line.length() && line.charAt(column) == 0) {
//              ++column;
//            }
//
//            final String lastChar =
//                line.substring(column, line.getNextIndex(column));
//
//            if (EditorManager.debug) {
//              System.err.println("lastChar: \'" + lastChar + "\'");
//            }
//
//            if (!" ".equals(lastChar)) {
//              str += ' ';
//            }
//            str += lastChar;
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line before replace: "
//                  + line);
//              System.err.println("substr: \"" + line.substring(column,
//                  line.getNextIndex(column)) + "\"");
//            }
//
//            line.replace(column, line.getNextIndex(column), str);
//
//            if (EditorManager.debug) {
//              System.err.println(lastLineNum + " - line after replace: " + line);
//            }
//          }
//
//          stripPrefixWhiteSpaceNodes.remove(node);
//        }
//
//        for (int i = 0, max = stripPrefixWhiteSpaceNodes.size(); i < max; i++) {
//          ASTImpl node = (ASTImpl) stripPrefixWhiteSpaceNodes.get(i);
//          lastLineNum = node.getStartLine();
//          Line line = manager.getLine(getInputSource(), getProject(),
//              node.getStartLine());
//          int startColumn = node.getStartColumn() - 1;
//          while (startColumn > 0
//              && Character.isWhitespace(line.charAt(startColumn - 1))) {
//            --startColumn;
//          }
//          line.delete(startColumn, node.getStartColumn() - 1);
//        }
//      } catch (IndexOutOfBoundsException e) {
//        e.printStackTrace(System.err);
//
//        status.addEntry(getInputSource().getDisplayPath() +
//            " - " + lastLineNum,
//            CollectionUtil.singletonArrayList(e), RefactoringStatus.FATAL);
//      }
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }
    return status;
  }

  private String createModifierString(final List newModif,final int upperBound){
    String insertString = "";
    for (int i = upperBound; i < newModif.size(); i++){
      String modifStr = new BinModifierFormatter(((Integer) newModif.get(i))
          .intValue()).print();
      insertString += ((i != upperBound) ? " " : "") + modifStr;
    }

    return insertString;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) +
        ": " + getTarget().getSource().getRelativePath() + " - " + member +
        " - newModifier: " + new BinModifierFormatter(newModifier, true).print();
  }

  public BinMember getMember() {
    return this.member;
  }

  public void merge(ModifierEditor editor) {
    AppRegistry.getLogger(this.getClass()).debug("[tonisdebug]:merging modiferEditor " +
    new BinModifierFormatter(newModifier).print() + " with " +
    new BinModifierFormatter(editor.newModifier).print());

    if (!member.equals(editor.member)) {
      AppRegistry.getLogger(this.getClass()).debug("ModifierEditor merge called with different members");
      return;
    }

    BinModifierBuffer buffer = new BinModifierBuffer(member.getModifiers());

    buffer.keepChangedFlags(newModifier, editor.newModifier);

    int privilegeMask1 = BinModifier.getPrivilegeFlags(newModifier);
    int privilegeMask2 = BinModifier.getPrivilegeFlags(editor.newModifier);

    int privilegeMask = privilegeMask2;

    // set strongest privilege mask
    if (BinModifier.compareAccesses(privilegeMask1, privilegeMask2) > 0) {
      privilegeMask = privilegeMask1;
    }

    buffer.setFlag(privilegeMask);

    newModifier = buffer.getModifiers();
  }
}
