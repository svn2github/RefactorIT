/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;

import javax.swing.JComponent;

/**
 * JavadocPathEditingPanel
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.4 $ $Date: 2005/01/21 08:59:22 $
 */
public class JavadocPathEditingPanel extends AbstractPathEditingPanel {
  
  /**
   * 
   */
  public JavadocPathEditingPanel() {
    addButton.addActionListener(new AddDirAction());
    addUrlButton.addActionListener(new AddUrlAction());
  }
  protected JComponent[] getButtons() {
    return new JComponent[] {
        addButton, addUrlButton, 
        removeButton, upButton, downButton
    };
  }

}
