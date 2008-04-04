/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import javax.swing.Icon;
import javax.swing.JCheckBox;

public final class DataCheckBox extends JCheckBox {
  private Object data; 
  
  public DataCheckBox(Object data, String text) {
    this(data, text, null, false);
  }
  
  public DataCheckBox(Object data, String text, boolean selected) {
    this(data, text, null, selected);
  }
  
  public DataCheckBox(Object data, String text, Icon icon, boolean selected) {
    super(text, icon, selected);
    this.setData(data);
  }

  /**
   * @param data The data to set.
   */
  public void setData(Object data) {
    this.data = data;
  }

  /**
   * @return Returns the data.
   */
  public Object getData() {
    return data;
  }
  
    
}
