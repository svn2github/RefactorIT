/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.ui.module.ActionProxy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public interface StandaloneAction extends ActionListener {

  void actionPerformed(ActionEvent event);

  String getName();

  ActionProxy getAction();

}
