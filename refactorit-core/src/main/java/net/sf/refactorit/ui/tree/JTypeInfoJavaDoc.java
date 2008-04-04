/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.Project;

import javax.swing.JPanel;


/**
 * The object used to show javadoc info for specified BinMember
 * object.
 *
 * Implement this interface to dock and show javadoc information
 * on some JPanel object.
 */
public interface JTypeInfoJavaDoc {
  /**
   * Update javadoc info on the panel where this instance of
   * JTypeInfoJavaDoc was docked into.
   *
   * @param member the member for what the javadoc info must be shown.
   * @param project used to get javadoc info for specified BinMember
   */
  void updateJavaDoc(BinMember member, Project project);

  /**
   * Dock this instance of JTypeInfoJavaDoc into panel,
   * then use {@link #updateJavaDoc(BinMember, Project)} to show javadoc info
   * on that panel.
   *
   * @param panel into where this instance of JTypeInfoJavaDoc
   * is being docked. i.e. Added into that panel.
   */
  void dockInto(JPanel panel);
}
