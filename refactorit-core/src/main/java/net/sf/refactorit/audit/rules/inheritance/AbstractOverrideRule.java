/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.inheritance;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.classmodel.BinMethod;

import java.util.Iterator;
import java.util.List;


/**
 *
 *
 * @author  Igor Malinin
 */
public class AbstractOverrideRule extends AuditRule {
  public static final String NAME = "abstract_override";

  public void visit(BinMethod method) {
    if (method.isAbstract()) {
      List overrides = method.findOverrides();
      for (Iterator i = overrides.iterator(); i.hasNext(); ) {
        BinMethod override = (BinMethod) i.next();
        if (override.getParentType().isClass()) {
          if (!override.isAbstract()) {
            addViolation(new AbstractOverride(method));
          }
        }
      }
    }

    super.visit(method);
  }
}

class AbstractOverride extends AwkwardMember {
  public AbstractOverride(BinMethod method) {
    super(method, "Abstract method overrides non-abstract " +
        "method: " + method.getName(), null);
  }
}
