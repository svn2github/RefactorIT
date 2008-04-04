/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteral;
import net.sf.refactorit.ui.audit.AuditTreeTableNode;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitTreeTableNode extends AuditTreeTableNode {
    
  public NumLitTreeTableNode(final NumericLiteral violation) {
    super(violation, null);
  }
  
  public final NumericLiteral getNumericViolation(){
    return (NumericLiteral) getRuleViolation();
  }
}
