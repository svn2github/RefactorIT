/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;


/**
 * @author Igor Malinin
 */
public class FixedLayout extends Layout {
  private Point size;

  public FixedLayout(Point size) {
    this.size = size;
  }
  
  protected Point computeSize(
      Composite composite, int wHint, int hHint, boolean flushCache
  ) {
    return size;
  }
  
  protected void layout(Composite composite, boolean flushCache) {
    // no children expected
  }
}
