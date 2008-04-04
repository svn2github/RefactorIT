/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


/**
 * To put type arguments in cases like this <code>List<String> list;</code>,
 * here String is an argument, so original BinCITypeRef for List is wrapped into
 * specific ref and filled with given arguments.
 * @author Anton Safonov
 */
public class BinGenericTypeRef extends BinSpecificTypeRef {
  private BinTypeRef[] typeArguments;

  public BinGenericTypeRef(final BinTypeRef typeRef) {
    super(typeRef);
  }

  public final BinTypeRef[] getTypeArguments() {
    return this.typeArguments;
  }

  public final void setTypeArguments(final BinTypeRef[] typeArguments) {
    this.typeArguments = typeArguments;
  }

  public void traverse(final BinTypeRefVisitor visitor) {
    super.traverse(visitor);

    if (this.typeArguments != null) {
      for (int i = 0, max = this.typeArguments.length; i < max; i++) {
        this.typeArguments[i].accept(visitor);
      }
    }
  }
}
