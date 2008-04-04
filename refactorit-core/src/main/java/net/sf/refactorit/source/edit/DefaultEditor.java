/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.source.SourceHolder;



/**
 * @author Anton Safonov
 */
public abstract class DefaultEditor implements Editor {
  private SourceHolder target;

  public DefaultEditor(final SourceHolder target) {
    this.target = target;
    if (Assert.enabled) {
      Assert.must(this.target != null, "Target must not be null");
    }
  }

  public SourceHolder getTarget() {
    return this.target;
  }
}
