/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.ui.module.RunContext;

import java.awt.Point;


/**
 * @author Tonis Vaga
 */
public class StandaloneRunContext extends RunContext {

  private Object target;

  private Point point;
  public Point getClickPoint() {
    return point;
  }

  public Object getTargetItem() {
    return target;
  }

  public StandaloneRunContext(int type, Object target, Point point,
      boolean checkMultiTarget) {
    super(type, ClassUtil.getClassesArray(target), checkMultiTarget);
    this.target = target;
    this.point = point;
  }
}
