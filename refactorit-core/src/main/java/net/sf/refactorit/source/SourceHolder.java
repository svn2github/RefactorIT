/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.vfs.Source;


/**
 * @author Anton Safonov
 */
public interface SourceHolder {

  String getName();

  String getDisplayPath();

  Source getSource();

  void setSource(Source source);

  Project getProject();

  void setPackage(BinPackage binPackage);

  BinPackage getPackage();

}
