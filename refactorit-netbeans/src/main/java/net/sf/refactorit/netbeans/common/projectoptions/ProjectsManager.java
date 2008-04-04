/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.SerializeUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.netbeans.common.NBController;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;


public class ProjectsManager {
  private static final Logger log = Logger.getLogger(ProjectsManager.class);

  private static final String SETTINGS_FOLDER_NAME = ".refactorit";

  private static final String CACHE_FILE = "cache";
  private static final String CACHE_FILE_EXT = "bin";

  private static final String SETTINGS_FILE = "refactorit";
  private static final String SETTINGS_FILE_EXT = "nbp";

  public static NBProject getCurrentProject() {
    try {
      // this check ensures the forte3x chain call to this method.
      // e.g. under forte3x the filesFolder can be "null", because
      // under forte3x this method is called within the
      // application initialization. Under netbeans and forte4x this
      // method is first called after the application initialization -
      // which must be a correct behaviour.
      final DataFolder currentProjectFolder = VersionSpecific.getInstance().getCurrentProjectFolder();

      if (currentProjectFolder == null) {
        return new EmptyNBProject();
      }


      FileObject projectFile = getProjectFile(currentProjectFolder);

      // check whether the convert is needed to do.
      NBProjectConverter converter = NBProjectConverter.getConverter();
      if (converter.isUpdateNeededFor(projectFile)) {
        converter.updateProjectSettings(projectFile);
      }

      // load the project options from project file.
      Properties properties = new Properties();
      properties.load(projectFile.getInputStream());
      return new NBProject(projectFile, properties);
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return null;
    }
  }

  public static DataFolder getFilesFolder() {
    Node projectDesktop = null;
    DataFolder dataFolder = null;
    try {
      projectDesktop = (Node) NBController.getVersionState().getActiveProjectFromIDE();
      if(projectDesktop == null) {
        return null;
      }
      dataFolder = (DataFolder) projectDesktop.getCookie(DataFolder.class);
      if (dataFolder == null && Assert.enabled) {
        throw new NullPointerException("Failed to get DataFolder");
      }
      return dataFolder;
    } catch (Exception ex) {
      log.warn(ex.getMessage(), ex);
      return null;
    } finally {
      if (Assert.enabled) {
        log.warn("projectDesktop: " + projectDesktop);
        log.warn("dataFolder: " + dataFolder);
        if (dataFolder != null) {
          try {
            final FileObject primaryFile = dataFolder.getPrimaryFile();
            String systemName;
            try {
              if (primaryFile.getFileSystem().getClass().getName().indexOf(
                  "SystemFileSystem") != -1) {
                Method method = primaryFile.getFileSystem().getClass().
                    getMethod(
                    "getLayers", new Class[0]);
                FileSystem[] systems = (FileSystem[]) method.invoke(
                    primaryFile.getFileSystem(), new Object[0]);
                systemName = systems[0].getSystemName();
              } else {
                systemName = primaryFile.getFileSystem().getSystemName();
              }
            } catch (FileStateInvalidException e) {
              systemName = "Invalid filesystem";
            }
            String folderName = ClassPath.getClassPath(primaryFile,
                ClassPath.COMPILE).getResourceName(primaryFile,
                File.separatorChar, true);
            log.warn("primaryFile: " + systemName
                + Source.LINK_SYMBOL + folderName);
          } catch (Exception ex1) {
            log.warn(ex1.getMessage(), ex1);
          }
        }
      }
    }
  }

  static FileObject getProjectFile(DataFolder projectFolder) {
    return getRefactoritProjectSpecificFile(projectFolder, SETTINGS_FILE, SETTINGS_FILE_EXT);
  }

  public static FileObject getCacheFile() {
    return getRefactoritProjectSpecificFile(VersionSpecific.getInstance().
        getCurrentProjectFolder(), CACHE_FILE, CACHE_FILE_EXT);
  }

  private static FileObject getRefactoritProjectSpecificFile(
      DataFolder projectFolder, String fileName, String extension) {
    if(projectFolder == null) {
      return null;
    }
    try {
      DataFolder refactoritDataFolder = DataFolder.create(projectFolder,
          SETTINGS_FOLDER_NAME);
      if (refactoritDataFolder == null) {
        throw new NullPointerException(
            "Failed to create folder '" + SETTINGS_FOLDER_NAME + "' under '" + projectFolder + "'");
      }
      FileObject refactoritFolder = refactoritDataFolder.getPrimaryFile();
      FileObject projectFile = refactoritFolder.getFileObject(fileName,
          extension);
      if (projectFile == null) {
        projectFile = refactoritFolder.createData(fileName, extension);
      }
      return projectFile;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Sets the listener to system/Projects folder. So that the refactorit project file (.refactorit/refactorit.nbp) is created
   * under newly created project folder (e.g. system/Projects/test) just in that moment when the user creates it.
   */
  public static void addListenerToNBProjectsFolder() {
    //DataFolder projectsFolder = TopManager.getDefault().getPlaces().folders().
    //    projects();
    FileObject fo = Repository.getDefault().getDefaultFileSystem()
    	.findResource("Projects");
    if (fo != null) {
        DataFolder projectsFolder = DataFolder.findFolder(fo);

	    FileObject projectsFolderFile = projectsFolder.getPrimaryFile();
	    projectsFolderFile.addFileChangeListener(new FileChangeAdapter() {
	      public void fileFolderCreated(FileEvent fe) {
	        // fixes bug #1781. NB 3.4x (and perhaps some other versions?) copy
	        // projects by first creating a new project and then copying
	        // the contents of the old project to that new project.
	        // If we created a RefactorIT options file by ourselves here
	        // then NB would later try to copy the old RefactorIT options file
	        // into it and will fail with a stack trace (NB does not expect that
	        // the file could already exist).
	        // HACK: I did not find a better way to check for "Save As"
	        // than checking the stack trace like this:
	        boolean saveAsActive = StringUtil.getStackTrace(new Throwable()).
	            indexOf("ProjectManagerPanel$8.run") >= 0;
	        if (saveAsActive) {
	          return;
	        }

	        //System.out.println("Project:"+fe.getFile().getName()+" created");
	        FileObject projectFolderFile = fe.getFile();
	        DataFolder projectFolder = DataFolder.findFolder(projectFolderFile);
	        FileObject refactoritProjectFile = getProjectFile(projectFolder);
	        NBProject project = new NBProject(refactoritProjectFile, new Properties());
	        String serializedVersion = null;
	        try {
	          serializedVersion = SerializeUtil.serializeToString("1.2");
	        } catch (Exception e) {
	          log.warn(e.getMessage(), e);
	          return;
	        }
	        project.setProperty("RefactorIT_Settings_Version", serializedVersion);
	      }
	    });
    }
  }

  private static class EmptyNBProject extends NBProject {
    public void setProperty(String name, String value) {}

    public EmptyNBProject() {
      super(null, null);
    }

    public String getProperty(String name) {
      return null;
    }

    public void saveProperties(Properties properties) {}
  }
}
