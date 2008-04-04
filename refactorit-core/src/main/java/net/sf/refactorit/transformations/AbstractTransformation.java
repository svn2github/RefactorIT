/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.source.SourceHolder;



/**
 * @author Jevgeni Holodkov
 * Abstract Transformation with the common transformation functions
 */
public abstract class AbstractTransformation implements Transformation {
  private final SourceHolder source;

  public AbstractTransformation(final SourceHolder source) {
    this.source = source;
    if (Assert.enabled) {
      Assert.must(this.source != null, "Source cannot be null!");
    }
  }

  protected SourceHolder getSource() {
    return this.source;
  }

}
