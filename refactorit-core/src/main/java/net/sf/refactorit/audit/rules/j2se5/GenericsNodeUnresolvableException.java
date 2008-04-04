/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.j2se5;

/**
 *
 * @author  Arseni Grigorjev
 */
public class GenericsNodeUnresolvableException extends Exception {
  GenericsNode node;
  
  /** Creates a new instance of GenericsRemoveFromGraphException */
  public GenericsNodeUnresolvableException(final String msg) {
    super(msg);
    node = null;
  }
  
  public GenericsNodeUnresolvableException(final String msg,
      final GenericsNode node){
    super(msg);
    this.node = node;
  }
  
  public GenericsNode getNode(){
    return node;
  }

  public void setNode(GenericsNode node) {
    this.node = node;
  }
}
