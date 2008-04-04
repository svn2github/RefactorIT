/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import javax.swing.JEditorPane;


/**
 * @author vlad
 *
 * It keeps all info that is neccessary for the BackAction operation
 *
 */
public class BackInfo {
  public BackInfo(JEditorPane editorPane, int positionInPane) {
    this.editorPane = editorPane;
    this.positionInPane = positionInPane;
  }

  public JEditorPane editorPane;
  public int positionInPane;
}
