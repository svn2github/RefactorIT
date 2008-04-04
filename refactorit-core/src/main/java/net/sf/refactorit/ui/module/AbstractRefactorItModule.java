/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.ui.options.Options;

import java.util.Properties;
import java.util.ResourceBundle;


/**
 * 
 * AbstractRefactorItModule
 * 
 * TODO: move duplicated code from subtypes to here!
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.4 $ $Date: 2004/10/25 20:08:05 $
 */
public abstract class AbstractRefactorItModule implements RefactorItModule {

  public Options getOptions() {
    return null;
  }

  public ResourceBundle getPropertyNames() {
    return null;
  }

  public Properties getProperties() {
    return null;
  }

  public void setProperties(Properties properties) {
  }

 

}
