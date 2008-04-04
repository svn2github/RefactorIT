/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;

/**
 * @author Oleg Tsernetsov
 */

public class UnusedTagsRule extends AuditRule {
  public static final String NAME = "unused_tags";
  public static UnusedTagsRule instance = new UnusedTagsRule();
  
  public UnusedTagsRule() {
    this.setPriority(Priority.LOW);
    setKey(NAME);
    setName("Unused @refactorit tags");
    setCategory("Redundant code");
  }
}
