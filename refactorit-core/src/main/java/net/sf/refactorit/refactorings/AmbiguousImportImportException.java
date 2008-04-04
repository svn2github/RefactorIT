/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.BinTypeRef;

/**
 * @author tanel
 *
 * Thrown when a refactoring tries to import a type that would cause import
 * ambiguity.
 */
public class AmbiguousImportImportException extends Exception {

	BinTypeRef typeRef;
	/**
	 * 
	 */
	public AmbiguousImportImportException(BinTypeRef typeRef) {
		super();
		this.typeRef =  typeRef;
	}

	/**
	 * @param message
	 */
	public AmbiguousImportImportException(String message, BinTypeRef typeRef) {
		super(message);
		this.typeRef =  typeRef;
	}


	public BinTypeRef getTypeRef() {
		return typeRef;
	}
}
