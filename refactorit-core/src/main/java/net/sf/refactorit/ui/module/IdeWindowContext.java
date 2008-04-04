/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;



import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.RitDialog;

import javax.swing.JComponent;

import java.awt.Point;


/**
 * IdeWindowContext - context needed for window operations
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.4 $ $Date: 2005/03/24 12:01:25 $
 */
public interface IdeWindowContext {

  /**
   */
  Object addTab(String title, JComponent component);

  // Factoring of derived contexts
  /**
   * Currently implemented as a shallow copy -- refers to the same instances as
   * the original.
   * ResultArea needs a new clone of context when actions are run on
   * BinTreeTables.
   */
  RefactorItContext copy();

  /**
   * Currently implemented as a shallow copy -- refers to the same instances as
   * the original.
   * ResultArea needs a new clone of context when actions are run on
   * BinTreeTables.
   */
  IdeWindowContext copy(RitDialog owner);

  /**
   * @return graphics point of module execution to show popups correctly
   */
  Point getPoint();

  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @return a data holder needed to recreate module execution state
   * on e.g. reload
   */
  Object getState();

  // Tabbed views manipulations
  /** Window ID for environments with multiple root windows */
  String getWindowId();

  // Source editing area manipulations
  /**
   * Just open a file in editor.
   */
  void open(SourceHolder src);

  // XXX: is it still needed? check, refactor and remove
  /** This is some legacy hack for JB */
  void postponeShowUntilNotified();

  /**
   * Reloads file opened in editor. Tries to keep first found highlight.
   */
  void reload();

  /**
   */
  void removeTab(Object category);

  // TODO: refactor?
  /**
   * @param point  graphics point of module execution;
   *     needed to show popups in correct place
   */
  void setPoint(Point point);

  // Temporary state storage
  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @param state a data holder needed to recreate module execution state
   * on e.g. reload
   */
  void setState(Object state);

  /**
   * Open a file in editor and show specified line
   * in file. If mark is true then highlight this
   * line where possible (might be ignored).
   */
  void show(SourceHolder src, int line, boolean mark);

  /** This is some legacy hack for JB */
  void showPostponedShows();

  /**
   * @param tab which tab to show
   * @return false if tab is gone and it's not possible to show
   */
  boolean showTab(Object tab);

}
