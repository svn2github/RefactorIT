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
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;


/**
 *
 *
 * @author  Igor Malinin
 */
public class AbstractSubclassRule extends AuditRule {
  public static final String NAME = "abstract_subclass";

  /* Cache */
  private BinTypeRef objectRef;

  public void visit(BinCIType type) {
    if (type.isClass() && type.isAbstract()) {
      BinTypeRef superclass = type.getTypeRef().getSuperclass();
      if (!superclass.equals(getObjectRef())) {
        if (!superclass.getBinCIType().isAbstract()) {
          addViolation(new AbstractSubclass(type));
        }
      }
    }

    super.visit(type);
  }

  private BinTypeRef getObjectRef() {
    if (objectRef == null) {
      objectRef = getBinTypeRef("java.lang.Object");
    }

    return objectRef;
  }
}


class AbstractSubclass extends AwkwardMember {

  public AbstractSubclass(BinCIType type) {
    super(type, "Abstract subclass of non-abstract superclass: "
        + type.getName(), null);
  }
}
