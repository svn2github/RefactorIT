/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.classmodel.BinSourceConstruct;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class AwkwardExpression extends AwkwardSourceConstruct {
  public AwkwardExpression(BinSourceConstruct expression, String message, String helpId) {
    super(expression, message, helpId);
  }
}
