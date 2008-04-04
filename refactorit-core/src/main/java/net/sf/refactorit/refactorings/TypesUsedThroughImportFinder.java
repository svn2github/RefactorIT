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
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.source.SourceHolder;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author  Arseni Grigorjev
 */
public class TypesUsedThroughImportFinder extends BinTypeRefVisitor {

  final Set result = new HashSet();
  private SourceHolder currentSourceHolder;
  private BinTypeRefVisitor typeRefVisitor;
  
  public TypesUsedThroughImportFinder(){
    setCheckTypeSelfDeclaration(false);
    setIncludeNewExpressions(true);
    typeRefVisitor = this;
  }

  public Set findFor(BinCIType binCIType){
    currentSourceHolder = binCIType.getCompilationUnit();
    return find(binCIType);
  }
  
  public Set findFor(CompilationUnit compilationUnit){
    currentSourceHolder = compilationUnit;
    return find(compilationUnit);
  }
  
  private Set find(final BinItemVisitable visitFrom) {
    result.clear();
    
    SinglePointVisitor typeFinder = new SinglePointVisitor() {
      private boolean insideMovedType = false;
      private Object movedType = null;

      public void onEnter(Object o){
        if (o instanceof BinTypeRefManager){
          ((BinTypeRefManager) o).accept(typeRefVisitor);
        }
      }

      public void onLeave(Object o) {
      }
    };

    visitFrom.accept(typeFinder);
    
    return result;
  }
  
  public void visit(BinTypeRef data) {
    try {
      BinTypeRef aTypeRef = data.getTypeRef();
			if (aTypeRef == null || aTypeRef.isPrimitiveType()) {
        return;
      }

      ASTImpl aNode = data.getNode();
      if (aNode == null) {
        return;
      }
      ASTImpl topNode = ImportUtils.getTopDotNodeParent(aNode);
			
      if (topNode.getParent().getType() == JavaTokenTypes.IMPORT) {
        return;
      }

      // skip FQN usages
      if (ImportUtils.isFqnUsage(aTypeRef.getQualifiedName(), aNode,
          topNode)){
        return;
      }

      // exclude types from java.lang package
      if ("java.lang".equals(aTypeRef.getPackage().getQualifiedName())) {
        return;
      }

      // top level types from same package are excluded by default
      if (aTypeRef.getPackage() == currentSourceHolder.getPackage()
          && aTypeRef.getBinCIType().getOwner() == null) {
        return;
      }

      // FIXME: moved inner classes?
      if (aTypeRef.getBinCIType().isInnerType()) {
        ASTImpl ownerNode = aTypeRef.getBinCIType().getOwner()
            .getBinCIType().getOffsetNode();

        boolean skip = false;
        ASTImpl parent = topNode.getParent();
        while (parent != null) {
          if (parent == ownerNode) {
            skip = true;
            break;
          }
          parent = parent.getParent();
        }
				
        if (skip) {
          return;
        }

        
				if (topNode.getType() == JavaTokenTypes.DOT && aNode.getParent().getFirstChild() != aNode) {
					return;
        }
				
      }

      result.add(aTypeRef);
    } finally {
      super.visit(data);
    }
  }
}
