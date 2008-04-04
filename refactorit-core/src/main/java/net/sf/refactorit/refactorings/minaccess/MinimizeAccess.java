/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.minaccess;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.HashSet;
import java.util.List;


/**
 * @author vadim
 */
public class MinimizeAccess extends AbstractRefactoring {
  public static String key = "refactoring.minimizeaccess";

  private BinCIType type;
  private List nodes;

  public MinimizeAccess(final RefactorItContext context, final BinCIType type) {
    super("Minimize Access", context);
    this.type = type;
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();

    if (!type.isFromCompilationUnit()) {
      status.addEntry(
          StringUtil.capitalizeFirstLetter(type.getMemberType())
          + " " + type.getQualifiedName()
          + "\n"
          +
          "is outside of the source path and its members' access cannot be minimized!",
          RefactoringStatus.ERROR);

      return status;
    }

    if (type.isInterface()) {
      status.addEntry(type.getQualifiedName() + " is an interface.\n"
          +
          "It is not possible to minimize access rights of members of an interface.",
          RefactoringStatus.ERROR);
    }

    if ((type.getDeclaredMethods().length == 0) &&
        (type.getDeclaredFields().length == 0) &&
        (type.getDeclaredTypes().length == 0)) {
      if (type instanceof BinClass) {
        if (((BinClass) (type)).getDeclaredConstructors().length != 0) {
          return status;
        }
      }
      status.addEntry(type.getQualifiedName() +
          " does not declare any methods, fields or inner classes.\n" +
          " Nothing to minimize access of.",
          RefactoringStatus.ERROR);

    }

    return status;
  }

  public RefactoringStatus checkUserInput() {
    return new RefactoringStatus();
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    HashSet processed = new HashSet();

    for (int i = 0, max = nodes.size(); i < max; i++) {
      MinimizeAccessNode node = (MinimizeAccessNode) nodes.get(i);

      if (node.isSelected() && !processed.contains(node.getBin())) {
        BinMember member = (BinMember) node.getBin();

        transList.add(new ModifierEditor(
            member, BinModifier.setFlags(member.getModifiers(),
            BinModifier.toNumber(node.getSelectedAccess()))));
        processed.add(member);

        MinimizeAccessNode nextNode = node.getNextNode();
        while (nextNode != null) {
          processed.add(nextNode.getBin());
          nextNode = nextNode.getNextNode();
        }

        MinimizeAccessNode prevNode = node.getPreviousNode();
        while (prevNode != null) {
          processed.add(prevNode.getBin());
          prevNode = prevNode.getNextNode();
        }
      }
    }

    return transList;
  }

  public void setNodes(List nodes) {
    this.nodes = nodes;
  }


  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
