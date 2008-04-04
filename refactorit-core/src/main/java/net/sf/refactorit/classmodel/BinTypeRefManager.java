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
 * Indicates that class is using BinTypeRef and enable to browse through those
 * BinTypeRef-s by using BinTypeRefVisitor.
 * 
 * For example, BinCastExpression is using BinTypeRef as a return type. This
 * interface enables to browse through this type.
 * 
 * @author Anton Safonov
 */
public interface BinTypeRefManager {

  void accept(BinTypeRefVisitor visitor);

}
