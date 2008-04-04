/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.source.edit;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.format.BinModifierFormatter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Corrects order of member modifier.
 * 
 * @author Arseni Grigorjev
 */
public class ModifierOrderer extends DefaultEditor {
  private BinMember member;

  public ModifierOrderer(BinMember member) {
    super(member.getCompilationUnit());
    this.member = member;
  }

  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();
    int memberModifiers = member.getModifiers();
    List modifiers = BinModifier.splitModifier(memberModifiers);

    List modifierNodes = member.getModifierNodes();

    ASTUtil.setAnnotationsNodesAsCompoundNodes(modifierNodes);

    try {
      int modifierIndex = 0;
      for (Iterator it = modifierNodes.iterator(); it.hasNext();) {
        ASTImpl node = (ASTImpl) it.next();

        if (node.getType() == JavaTokenTypes.ANNOTATION) {
          continue;
        }

        int code = 0;
        do {
          code = ((Integer) modifiers.get(modifierIndex)).intValue();
          modifierIndex++;
        }
        while(code == 0);
        
        if (code > 0) {
          final String modifStr = new BinModifierFormatter(code).print();
          Line line = manager.getLine(getTarget(), node.getStartLine());
          line.replace(node.getStartColumn() - 1, node.getEndColumn() - 1,
              modifStr);
        }
      }
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    } catch (IndexOutOfBoundsException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }

    return status;
  }

}
