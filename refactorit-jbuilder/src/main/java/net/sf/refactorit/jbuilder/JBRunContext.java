/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import net.sf.refactorit.ui.module.RunContext;

import com.borland.primetime.node.Node;


/**
 * @author Anton Safonov
 */
public class JBRunContext extends RunContext {
  private static Node[] nodes = null;

  public JBRunContext(int type, Class targetClass, boolean checkMultiTarget,
      Node[] nodes) {
    super(type, targetClass, checkMultiTarget);
    setNodes(nodes);
  }

  public JBRunContext(int type, Class[] targetClasses, boolean checkMultiTarget,
      Node[] nodes) {
    super(type, targetClasses, checkMultiTarget);
    setNodes(nodes);
  }

  public static Node[] getNodes() {
    return JBRunContext.nodes;
  }

  public static void setNodes(Node[] nodes) {
    JBRunContext.nodes = nodes;
  }
}
