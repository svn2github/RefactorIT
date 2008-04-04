/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.vfs.Source;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import java.awt.Component;

/**
 * A <code>java.swing.ListCellRenderer</code> implementation based on
 * <code>java.swing.DefaultListCellRenderer</code>, in it's 
 * <code>getListCellRendererComponent</code> method extracts absolute path from 
 * provided value objects (if value is of type <code>Source</code>) and sets it 
 * as a lable for the returned component.
 * 
 * @author juri
 */
public class SourceListRenderer extends DefaultListCellRenderer{
  /**
   * If passed 'value' object is instanceof <code>Source</code> extracts
   * source absolute path via <code>Source's</code> <code>getAbsolutePath</code>
   * and renders it. Otherwise delegates up to DefaultListCellRenderer
   */
  public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {
    //as the component returned by a call to a super method is 'this' object then:
    super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
    if(value instanceof Source)
      setText(((Source)value).getAbsolutePath());
    return this;
  }

}
