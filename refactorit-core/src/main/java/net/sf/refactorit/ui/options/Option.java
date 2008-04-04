/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

/**
 * Describes individual option.
 *
 * @author Igor Malinin
 */
public interface Option {
  String getKey();

  String getValue();

  Class getType();

  void setVisible(boolean b);

  boolean isVisible();
}
