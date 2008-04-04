/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;

/**
 * This one is called by {@link CallbackVisitor} on visiting each item during
 * classmodel tree traversal.
 *
 * @author Anton Safonov
 */
public interface CallbackModel {

  /** Going deeper in the classmodel tree. */
  void goDown();

  /** Going upper in the classmodel tree. */
  void goUp();

  /**
   * @param object BinXXX which is being visited.
   */
  void callback(Object object);

}
