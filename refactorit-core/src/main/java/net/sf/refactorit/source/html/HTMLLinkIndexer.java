/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.html;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class HTMLLinkIndexer extends AbstractIndexer {
  private static final String DEF_STYLE = "def";
  private static final String URL_STYLE = "url";

  private HashMap entities = new HashMap(128);

  private final TypeRefVisitor typeRefVisitor = new TypeRefVisitor();

  private class TypeRefVisitor extends BinTypeRefVisitor {
    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(false);
      setIncludeNewExpressions(true);
    }

    public void visit(final BinTypeRef data) {
      // Discard types that are primitive or are not part of the project
      BinTypeRef type = data.getNonArrayType();
      if (type.isPrimitiveType()
          || !type.getBinCIType().isFromCompilationUnit()) {
        return;
      }

      // <A href=container#id>type</A>
      addEntity(new HTMLLinkEntity(data.getNode(), URL_STYLE,
          getTypeID(type.getBinType()),
          getContainer(type.getBinCIType())));

      super.visit(data);
    }
  }

  public HTMLLinkIndexer() {
    super(true);
  }

  public void visit(CompilationUnit source) {
    // List all semantic tokens
    new HTMLASTIndexer(this).visitAST(source.getSource().getFirstNode());

    source.accept(typeRefVisitor);

    super.visit(source);
  }

  //
  // Type-related functions
  //

  public void visit(BinCastExpression expression) {
    expression.accept(typeRefVisitor);
    super.visit(expression);
  }

  public void visit(BinNewExpression expression) {
    expression.accept(typeRefVisitor);
    super.visit(expression);
  }

  public void visit(BinCITypeExpression expression) {
    expression.accept(typeRefVisitor);
    super.visit(expression);
  }

  public void visit(BinLocalVariable var) {
    var.accept(typeRefVisitor);
    super.visit(var);
  }

  public void visit(BinCIType binCIType) {
    List data = binCIType.getSpecificSuperTypeRefs();
    for (int pos = 0, max = (data != null ? data.size() : 0); pos < max; pos++) {
      // Simple <CODE>extends</CODE> or <CODE>implements</CODE>
      typeRefVisitor.visit((BinTypeRef) data.get(pos));
    }

    addEntity(new HTMLNameEntity(binCIType.getNameAstOrNull(), DEF_STYLE,
        getTypeID(binCIType)));

    super.visit(binCIType);
  }

  public void visit(BinConstructor method) {
    method.accept(typeRefVisitor);
    super.visit(method);
  }

  public void visit(BinMethod method) {
    // <A name=id>method</A>
    if (method.getNameAstOrNull() != null) {
      addEntity(new HTMLNameEntity(
          method.getNameAstOrNull(), DEF_STYLE, getMethodID(method)));
    }

    method.accept(typeRefVisitor);
    super.visit(method);
  }

  public void visit(BinMethodInvocationExpression expression) {
    // Create HTMLEntity
    scope: {
      BinMethod method = expression.getMethod();

      // Discard members that are not from $SOURCEPATH
      if (!method.getOwner().getBinCIType().isFromCompilationUnit()) {
        break scope;
      }

      // <A href=container#id>method</A>
      addEntity(new HTMLLinkEntity(expression.getNameAst(), URL_STYLE,
          getMethodID(method), getContainer(method.getOwner().getBinCIType())));
    }

    super.visit(expression);
  }

  public void visit(BinField field) {
    // Create HTMLEntity only for fields
    if (!field.isLocalVariable()) {
      // <A name=id>method</A>
      addEntity(new HTMLNameEntity(
          field.getNameAstOrNull(), DEF_STYLE, getFieldID(field)));
    }

    field.accept(typeRefVisitor);
    super.visit(field);
  }

  public void visit(BinFieldInvocationExpression expression) {
    // Create HTMLEntity
    scope: {
      BinField field = expression.getField();

      // Discard members that are not from $SOURCEPATH
      if (!field.getOwner().getBinCIType().isFromCompilationUnit()) {
        break scope;
      }

      // <A href=container#id>field</A>
      addEntity(new HTMLLinkEntity(expression.getNameAst(), URL_STYLE,
          getFieldID(field), getContainer(field.getOwner().getBinCIType())));
    }

    super.visit(expression);
  }

  //
  // Helper routines
  //

  public ArrayList getLine(int line) {
    ArrayList result = (ArrayList) entities.get(new Integer(line));

    // Make sure the elements are properly ordered
    if (result != null) {
      Collections.sort(result);
    }

    // Return result
    return result;
  }

  private String getContainer(BinCIType owner) {
    if (!owner.isFromCompilationUnit()) {
      return ""; // FIXME: will it ever happen?
    }

    owner = getOwner(owner);

    if (owner != null) {
      return HTMLLinkIndexer.createFileName(
          owner.getCompilationUnit().getSource().getRelativePath());
    }

    return "";
  }

  private String getMethodID(BinMethod method) {
    return String.valueOf(Math.abs(method.getQualifiedNameWithParamTypes().
        hashCode()));
  }

  private String getTypeID(BinType type) {
    return String.valueOf(Math.abs(type.getQualifiedName().hashCode()));
  }

  private String getFieldID(BinField field) {
    return String.valueOf(Math.abs(field.getQualifiedName().hashCode()));
  }

  protected void addEntity(HTMLEntity entity) {
    // The identifier of the line
    Integer key = new Integer(entity.getNode().getLine());

    // Append to line
    ArrayList line = (ArrayList) entities.get(key);

    // Allocate if one didn't exist yet
    if (line == null) {
      entities.put(key, line = new ArrayList(4));
    }

    // BUG 2033 fix
    // [tonis]

    if (!line.contains(entity)) {
      line.add(entity);
    }
  }

  private BinCIType getOwner(BinCIType type) {
    if (type instanceof BinArrayType) {
      BinType array = (((BinArrayType) type).getArrayType()).getBinType();

      return (array instanceof BinCIType) ? (BinCIType) array : null;
    }

    return type;
  }

  public static final String createFileName(final String resource) {
    String path = resource;
    path = path.replace('/', '.');
    path = path.replace('\\', '.');
    path = StringUtil.replace(path, Source.LINK_SYMBOL, ".");

    if (path.endsWith(".java")) {
      path = path.substring(0, path.length() - "java".length()) + "html";
    } else {
      path += ".html";
    }

    return path;
  }
}
