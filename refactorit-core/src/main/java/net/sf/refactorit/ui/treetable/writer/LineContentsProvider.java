/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

public interface LineContentsProvider {
  public String getTypeColumn();

  public String getNameColumn();

  public String getColumn(int i);

  public String getPackageColumn();

  public String getClassColumn();
}
