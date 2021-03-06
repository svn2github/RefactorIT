/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.BinTypeRef;

import javax.swing.JPanel;


public abstract class AbstractClassSearchPanel extends JPanel implements
    CancelOkListener {

//  /** Autogenerated proxy constructor. */
//  public SearchPane(LayoutManager a) {
//    super(a);
//  }

  public abstract void doCancel();

  public abstract void doOk();

  public abstract void onShow();

  public abstract BinTypeRef getTypeRef();
}
