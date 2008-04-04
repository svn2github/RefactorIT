/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;


/**
 * ProjectSettingsListener - listener for Project setting changes
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.3 $ $Date: 2005/01/05 09:30:29 $
 */
public interface ProjectSettingsListener {

  void settingsChanged(ProjectOptions projectOptions);
}
