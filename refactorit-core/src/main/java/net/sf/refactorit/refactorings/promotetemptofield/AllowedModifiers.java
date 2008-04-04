/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;


/**
 *
 * @author  RISTO A
 * @author juri
 */
public class AllowedModifiers {
  public boolean initializationAllowed(FieldInitialization initLocation,
      BinLocalVariable var) {
    return initLocation.supports(var);
  }

  public boolean finalAllowed(FieldInitialization initLocation,
      BinLocalVariable var) {
    return (!var.isAssignedTo()) && var.getExpression() != null &&
        (!initLocation.initializesInMethod());
  }

  public boolean mustBeStatic(BinLocalVariable var) {
    return var.getParentMember().isStatic();
  }

  public boolean staticAllowed(BinLocalVariable var) { 
    boolean mustBeStatic=mustBeStatic(var);
    boolean mustBeNonStatic=mustBeNonStatic(var);
    return mustBeStatic||!mustBeNonStatic;
  }
  
  public boolean mustBeNonStatic(BinLocalVariable var) {    
//    return!mustBeStatic(var);
    //hack by Juri Reinsalu while adding "static" modifier to promoted var
    return false;
  }
}
