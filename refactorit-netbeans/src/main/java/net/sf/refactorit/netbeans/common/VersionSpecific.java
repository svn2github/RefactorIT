/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import javax.swing.JMenuItem;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;

import java.io.File;
import java.util.List;


/**
 * Incohesive; I'm slowly moving all functionality out of this hierarchy.
 *
 * @author risto
 */
public abstract class VersionSpecific {
  private static VersionSpecific instance;

  public static VersionSpecific getInstance() {
      if(instance == null) {
          try {
              // order matters! It checks using <isVersionAtLeast>, therefore, the 
              // the older version should be check first
              if(RefactorItActions.isNetBeansFive()) {
                instance = (VersionSpecific) Class.forName(
                		"net.sf.refactorit.netbeans.ide5.Nb5VersionSpecific").newInstance();
              } else if(RefactorItActions.isNetBeansFour()) {
                instance = (VersionSpecific) Class.forName(
                    "net.sf.refactorit.netbeans.ide4.Nb4VersionSpecific").newInstance();
              } else {
                instance = (VersionSpecific) Class.forName(
                    "net.sf.refactorit.netbeans.ide3.Nb3VersionSpecific").newInstance();
              }
            } catch (Exception e) {
              AppRegistry.getExceptionLogger().error(e, VersionSpecific.class);
              throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
            }
      }
    return instance;
  }

  public abstract String getProjectNameFor(Object nbIdeProject);

  public abstract FileObject[] getIdeSourcepath(Object ideProject);

  public abstract List getClasspath(Object ideProject);

  public abstract List getBootClasspath(Object ideProject);

  /** If null, boot classpath is autodetected */
  public abstract void setFakeBootClasspath(List pathItemReferences);

  public abstract DataFolder getCurrentProjectFolder();

  public abstract Object getCurrentProject();

  public abstract String getAttr(String key);

  public abstract void setAttr(String key, String string);

  public abstract ProjectId getProjectId();

  public abstract String getLongDisplayName(FileObject fileObject);

  public abstract Object getAllPropertiesCloned();

  public abstract void setAllPropertiesFrom(Object previousSnapshot);

  public abstract PathItemReference getPathItemReference(File file);

  public abstract FileObject getArchiveRoot(FileObject object);

  public abstract Object[] getRequiredProjectKeys(Object ideProject);

  public abstract Object getUniqueProjectIdentifier(Object ideProject);

  /**
   * Netbeans 5 differ from previous netbeans. It uses DynamicMenuContent 
   * elements to handle dynamic menuItems, while Netbeans 3 and 4 use
   * JInlineMenu for this purpose.
   */
  public abstract JMenuItem createMenuItem();

  /**
   * Temporary method. NB5 integration does not support VCS, so it must 
   * always return false. Other implementation should use the GlobalOptions
   * class. After VCS support is implemented, it is necessary to delete this
   * method and use the GlobalOptions directly.
   * @return
   */
  public abstract boolean isVcsEnabled();
}
