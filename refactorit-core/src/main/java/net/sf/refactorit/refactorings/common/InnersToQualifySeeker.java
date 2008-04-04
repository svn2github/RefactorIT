/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: InnersToQualifySeeker.java,v 1.18 2005/03/07 11:56:13 tsern Exp $ */
package net.sf.refactorit.refactorings.common;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Anton Safonov
 */
public class InnersToQualifySeeker extends SinglePointVisitor {
  private final BinCIType targetType;
  private final Map editMap = new HashMap();

  private TypeRefVisitor refVisitor = new TypeRefVisitor();

  private class TypeRefVisitor extends BinTypeRefVisitor {
    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(false);
      setIncludeNewExpressions(true);
    }

    public void visit(final BinTypeRef typeRef) {
      updateOwnerForInner(typeRef.getTypeRef(), typeRef.getNode());

      super.visit(typeRef);
    }
  }

  public InnersToQualifySeeker(final BinCIType targetType) {
    this.targetType = targetType;
  }

  public void onEnter(Object o) {
    if (o instanceof BinTypeRefManager) {
      ((BinTypeRefManager) o).accept(refVisitor);
    }
  }

  public void onLeave(Object o) {
  }

  private final void updateOwnerForInner(BinTypeRef potentialInnerType,
      ASTImpl typeAST) {
    if (!potentialInnerType.isReferenceType()) {
      return;
    }

    BinCIType type = potentialInnerType.getBinCIType();
    if (type.isInnerType() && !type.isLocal() && type != targetType) {
      if (isInnerTypeOfTarget(type)) {
        if (typeAST.getParent().getType() == JavaTokenTypes.DOT) {
          this.editMap.put(typeAST.getParent().getFirstChild(), null);
        }
      } else {
        if (typeAST.getType() != JavaTokenTypes.DOT) {
          if (typeAST.getParent().getType() == JavaTokenTypes.DOT
              && typeAST.getParent().getFirstChild() != typeAST) {
            return; // has something infront already
          }

          String ownerName = type.getOwner().getBinCIType().getNameWithAllOwners().replace('$', '.'); 
          
          ASTImpl placePointer = new SimpleASTImpl(typeAST.getType(), "");
          placePointer.setStartLine(typeAST.getStartLine());
          placePointer.setStartColumn(typeAST.getStartColumn());
          placePointer.setEndLine(typeAST.getStartLine());
          placePointer.setEndColumn(typeAST.getStartColumn());
          this.editMap.put(placePointer, ownerName + '.');
        }
      }
    }
  }

  /** 
   * Determines if an inner type is inner type of target type 
   * 
   * @param type an inner type
   * @return
   */
  protected boolean isInnerTypeOfTarget(BinCIType type) {
  	return (type.getTopLevelEnclosingType() == targetType)
				|| targetType.getTypeRef().getAllSubclasses().contains(
						type.getTopLevelEnclosingType());
  }
  
  /**
   * @return map: place -> edit; edit == null - erase,
   * otherwise edit is String to insert
   */
  public Map getEditMap() {
    return this.editMap;
  }

  public void generateEditors(final TransformationList transList,
      final CompilationUnit source) {
    Iterator edits = this.editMap.entrySet().iterator();
    while (edits.hasNext()) {
      Map.Entry entry = (Map.Entry) edits.next();
      ASTImpl place = (ASTImpl) entry.getKey();
      if (entry.getValue() == null) {
        transList.add(new StringEraser(source, place, true));
      } else {
        transList.add(new StringInserter(source,
            place.getStartLine(), place.getStartColumn() - 1,
            (String) entry.getValue()));
      }
    }
  }
}
