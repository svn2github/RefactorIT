/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.ArrayList;



/**
 * Defines class as BinCIType place holder.
 */
public final class LoadingSourceBinCIType extends BinCIType {

  public LoadingSourceBinCIType(CompilationUnit compilationUnit, ASTImpl rootAST,
      BinTypeRef owner,
      String name, int modifiers,
      String superclassName, String[] interfaceNames,
      Project project) {
    super(compilationUnit.getPackage(), name, owner, modifiers, project);

    setCompilationUnit(compilationUnit);
    setOffsetNode(rootAST);

    this.superclassName = superclassName;
    this.interfaceNames = interfaceNames;
  }

  public String[] getInterfaceNames() {
    return interfaceNames;
  }

  public String getSuperclassName() {
    return superclassName;
  }

  public void addDeclaredType(BinTypeRef typeRef) {
    myDeclaredTypes.add(typeRef);
  }

  public BinTypeRef[] getDeclaredTypes() {
    return (BinTypeRef[]) myDeclaredTypes.toArray(BinTypeRef.NO_TYPEREFS);
  }

  public void defaultTraverse(BinItemVisitor visitor) {
    if (Assert.enabled) {
      Assert.must(false, "Detect traverse of LoadingSourceBinCIType!");
    }
  }

  /**
   * Checks whether this type is a class.
   *
   * @return <code>true</code> if and only if this type is a class;
   *        <code>false</code> otherwise.
   */
  public boolean isClass() {
    return !isInterface() && !isEnum() && !isAnnotation();
  }

  /**
   * Checks whether this type is an interface.
   *
   * @return <code>true</code> if and only if this type is an interface;
   *        <code>false</code> otherwise.
   */
  public boolean isInterface() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.INTERFACE);
  }

  public boolean isAnnotation() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.ANNOTATION);
  }

  /**
   * Checks whether this type is an enum.
   *
   * @return <code>true</code> if and only if this type is an enum;
   *        <code>false</code> otherwise.
   */
  public boolean isEnum() {
    return BinModifier.hasFlag(getModifiers(), BinModifier.ENUM);
  }

  public final String getMemberType() {
    return memberType;
  }

  public static final LoadingSourceBinCIType build(
      final ASTImpl offsetNode, final BinCIType owner,
      final CompilationUnit compilationUnit) {

    final String definedTypeName = LoadingASTUtil.getTypeNameFromDef(offsetNode);
    if (Assert.enabled && definedTypeName == null) {
      Assert.must(false, "Type has no name in: " + compilationUnit);
      LoadingASTUtil.ASTDebugOn(offsetNode);
    }

    String superclass = null;
    String[] interfaces = StringUtil.NO_STRINGS;
    if (offsetNode.getType() == JavaTokenTypes.CLASS_DEF) {
      final String[] tmp = LoadingASTUtil.extractExtendsOrImplements(
          LoadingASTUtil.findExtendsNode(offsetNode));
      if (Assert.enabled) {
        Assert.must(tmp.length < 2, "multiple names while extending class");
      }
      if (tmp.length == 1) {
        superclass = tmp[0];
      } else {
        if (!"Object".equals(definedTypeName)
            || !"java.lang".equals(compilationUnit.getPackage().getQualifiedName())) {
          superclass = Project.OBJECT;
        }
      }
      interfaces = LoadingASTUtil.extractExtendsOrImplements(
          LoadingASTUtil.findImplementsNode(offsetNode));
    } else if (offsetNode.getType() == JavaTokenTypes.INTERFACE_DEF) {
      interfaces = LoadingASTUtil.extractExtendsOrImplements(
          LoadingASTUtil.findExtendsNode(offsetNode));
    } else if (offsetNode.getType() == JavaTokenTypes.ENUM_CONSTANT_DEF) {
      superclass = owner.getQualifiedName();
    } else if (offsetNode.getType() == JavaTokenTypes.ENUM_DEF) {
      superclass = "java.lang.Enum";
      interfaces = LoadingASTUtil.extractExtendsOrImplements(
          LoadingASTUtil.findImplementsNode(offsetNode));
    } else if(offsetNode.getType() == JavaTokenTypes.ANNOTATION_DEF) {
      interfaces = new String[] {"java.lang.annotation.Annotation"};
    } else {
      if (Assert.enabled) {
        Assert.must(false, "Invalid node type :" + offsetNode.getType());
      }
    }

    int modifiers = 0;
    if (offsetNode.getType() != JavaTokenTypes.ENUM_CONSTANT_DEF) {
      try {
        modifiers = LoadingASTUtil.extractTypeModifiers(offsetNode);
      } catch (IllegalArgumentException e) {
        new rantlr.debug.misc.ASTFrame("wrong modifier", offsetNode).setVisible(true);
      }
    }

    if (owner != null && owner.isInterface()) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.PUBLIC | BinModifier.STATIC);
    }

    if (offsetNode.getType() == JavaTokenTypes.INTERFACE_DEF) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.ABSTRACT | BinModifier.INTERFACE | BinModifier.STATIC);
    }

    // must ensure static, it is missing usually
    if (offsetNode.getType() == JavaTokenTypes.ENUM_DEF
         || offsetNode.getType() == JavaTokenTypes.ENUM_CONSTANT_DEF) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.ENUM | BinModifier.PUBLIC
          | BinModifier.STATIC | BinModifier.FINAL);
    }

    if(offsetNode.getType() == JavaTokenTypes.ANNOTATION_DEF) {
      modifiers = BinModifier.setFlags(modifiers,
          BinModifier.ANNOTATION | BinModifier.FINAL | BinModifier.STATIC);
    }

    BinTypeRef ownerRef = null;
    if (owner != null) {
      ownerRef = owner.getTypeRef();
    }

    if (Assert.enabled && compilationUnit.getProject() == null) {
      Assert.must(false, "Compilation unit has no project yet");
    }

    final LoadingSourceBinCIType aType
        = new LoadingSourceBinCIType(compilationUnit, offsetNode,
        ownerRef, definedTypeName, modifiers,
        superclass, interfaces, compilationUnit.getProject());

    return aType;
  }

  private static final String memberType = "loading type";

  private final String[] interfaceNames;
  private final String superclassName;
  private final ArrayList myDeclaredTypes = new ArrayList(1);
}
