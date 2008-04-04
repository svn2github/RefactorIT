/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;



import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.ui.RuntimePlatform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Stores common project options, works under all IDEs. This is only a storage system,
 * so invoking a setXXX() method on this object only saves a value and does not
 * reconfigure currently running system (except when some code is frequently calling getXXX()). <br><br>
 *
 * Every IDE implementation needs to attach its own instance of this class to Project. <br><br>
 *
 * Only contains options that are equally present in all of the IDEs, but subclasses
 * might have addittional options that are unique to their own platforms. <br><br>
 *
 * All subclasses *must* be singletons (otherwise property editors would get confused).
 */
public abstract class ProjectOptions implements PropertyPersistance {
  protected List propertyList = new ArrayList();

  private ComboBoxProperty jvmMode;
  private NonJavaFilePatternsProperty nonJavaFilesPatterns;

  //private ProjectSettings projectSettings;

  public ProjectOptions() {
    jvmMode = new ComboBoxProperty(
        "jvm.mode",
        new ComboBoxPropertyOption[] {
        new ComboBoxPropertyOption("jvm.automatic",
        FastJavaLexer.JVM_AUTOMATIC),
        new ComboBoxPropertyOption("jvm.13",
        FastJavaLexer.JVM_13),
        new ComboBoxPropertyOption("jvm.14",
        FastJavaLexer.JVM_14),
        new ComboBoxPropertyOption("jvm.50",
        FastJavaLexer.JVM_50)
        }, this);
    propertyList.add(jvmMode);
    nonJavaFilesPatterns = new NonJavaFilePatternsProperty(
        "nonJavaFilesPatterns", this);
    propertyList.add(nonJavaFilesPatterns);
  }

  /** @return one of the FastJavaLexer.JVM_XXX constants; default is JVM_AUTOMATIC */
  public int getJvmMode() {
    return this.jvmMode.getSelectedValueInt();
  }

  /** @param jvmMode One of the FastJavaLexer.JVM_XXX constants. */
  public void setJvmMode(int jvmMode) {
    this.jvmMode.setSelectedValueInt(jvmMode);
  }

  public WildcardPattern[] getNonJavaFilesPatterns() {
    return WildcardPattern.stringToArray(this.nonJavaFilesPatterns.getText());
  }

  public void setNonJavaFilesPatterns(final WildcardPattern[]
      nonJavaFilesPatterns) {
    this.nonJavaFilesPatterns.setText(WildcardPattern.arrayToString(
        nonJavaFilesPatterns));
  }

  /**
   * @return  all properties that all platforms have in common (plus possibly some
   *          platform-specific properties, as well).
   */
  public ProjectProperty[] getCommonProperties() {
    return (ProjectProperty[])this.propertyList.toArray(
        new ProjectProperty[this.propertyList.size()]);
  }

  /** @return a new edit panel for properties that all platforms have in common */
  public CommonOptionsPanel getCommonPropertiesInOnePanel() {
    return new CommonOptionsPanel(getCommonProperties());
  }

  /**
   * This is useful when there are multiple configurations per project in an IDE (like in JDev).
   * (Then it's the caller's responsibility to make sure that info gets saved from editors
   * into the right configuration by passing the right persistance object.)
   */
  public void saveChoicesFromPropertyEditors(PropertyPersistance persistance) {
    for (Iterator i = this.propertyList.iterator(); i.hasNext(); ) {
      ProjectProperty eachProperty = (ProjectProperty) i.next();
      eachProperty.saveChoiceFromEditor(persistance);
    }
  }

  /**
   * This is useful when there are multiple configurations per project in an IDE (like in JDev).
   * (Then it's the caller's responsibility to make sure that info gets loaded into editors
   * from the right configuration by passing the right persistance object.)
   */
  public void loadChoicesToPropertyEditors(PropertyPersistance persistance) {
    for (Iterator i = this.propertyList.iterator(); i.hasNext(); ) {
      ProjectProperty eachProperty = (ProjectProperty) i.next();
      eachProperty.loadChoiceToEditor(persistance);
    }
  }

  /**
   * When user presses OK in a project options dialog, then call this method.
   * When user presses CANCEL, don't call anything specific.
   */
  public void saveChoicesFromPropertyEditors() {
    saveChoicesFromPropertyEditors(this);
  }

  public void loadChoicesToPropertyEditors() {
    loadChoicesToPropertyEditors(this);
  }

  public boolean propertyEditorsModified() {
    for (Iterator i = this.propertyList.iterator(); i.hasNext(); ) {
      ProjectProperty eachProperty = (ProjectProperty) i.next();
      if (eachProperty.editorModified()) {
        return true;
      }
    }

    return false;
  }
//  public ProjectSettings getProjectSettings() {
//    if ( projectSettings == null ) {
//      projectSettings=new ProjectSettings(this, true);
//    }
//
//    return projectSettings;
//  }
//  public void setProjectSettings(ProjectSettings settings) {
//    projectSettings=settings;
//  }

  /** For PropertyPersistance implementation; not for direct use by clients. */
  public abstract String get(String name);

  /** For PropertyPersistance implementation; not for direct use by clients. */
  public abstract void set(String name, String value);




  private Path ignoredSourcePath;
  private Path sourcePath;
  private Path classPath;
  private Path javadocPath;
  private Path dependencies;

  static final String IGNORED_PATH_KEY = "ignored_sourcepath";
  static final String SOURCEPATH_KEY = "sourcepath";
  static final String CLASSPATH_KEY = "classpath";
  static final String JAVADOCPATH_KEY = "javadocpath";
  private static final String DEPENDENCIES_KEY = "project_dependencies";
//  static final String JVM_KEY = "rit.jvm.mode";
  static final String AUTODETECT_KEY = "autodetectpath";

  private Boolean isAutoDetect;



  public final boolean canAutoDetect() {
    return !IDEController.runningStandalone();
  }

  public final boolean isAutoDetect() {
    if(isAutoDetect == null) {
	    String strVal = this.get(AUTODETECT_KEY);

	    if ( strVal != null  ) {
	      isAutoDetect = Boolean.valueOf(strVal);
	    } else {
	      isAutoDetect = Boolean.valueOf(canAutoDetect());
	    }
    }
    return isAutoDetect.booleanValue();
  }


  public void serialize() {
    serialize(this);
  }

  public void serialize(PropertyPersistance persistance) {
    persistance.set(AUTODETECT_KEY, isAutoDetect() ? "true" : "false");
    persistance.set(IGNORED_PATH_KEY, getIgnoredSourcePath().serialize());
    persistance.set(SOURCEPATH_KEY, getSourcePath().serialize());
    persistance.set(JAVADOCPATH_KEY, getJavadocPath().serialize());
    persistance.set(CLASSPATH_KEY, getClassPath().serialize());
    persistance.set(DEPENDENCIES_KEY, getDependencies().serialize());
  }


  private Path getDependencies() {
    if(dependencies == null) {
      deserialize(this, isAutoDetect());
    }
    return dependencies;
  }

  public void setClassPath(Path classPath) {
    this.classPath = classPath;
  }

  public void setIgnoredSourcePath(Path ignoredSourcePath) {
    this.ignoredSourcePath = ignoredSourcePath;
  }

  public void setJavadocPath(Path javadocPath) {
    this.javadocPath = javadocPath;
  }

  public void setSourcePath(Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  public Path getClassPath() {
    if(classPath == null) {
      deserialize(this, isAutoDetect());
    }
    return classPath;
  }

  public Path getIgnoredSourcePath() {
    if(ignoredSourcePath == null) {
      deserialize(this, isAutoDetect());
    }
    return ignoredSourcePath;
  }

  public Path getJavadocPath() {
    if(javadocPath == null) {
      deserialize(this, isAutoDetect());
    }
    return javadocPath;
  }

  public Path getSourcePath() {
    if(sourcePath == null) {
      deserialize(this, isAutoDetect());
    }
    return sourcePath;
  }

	public void deserialize(PropertyPersistance persistance, boolean canAutoDetect) {
	  boolean autoDetect =	canAutoDetect();
	if ( canAutoDetect ) {
	    String strVal = persistance.get(AUTODETECT_KEY);

	    if ( strVal != null  ) {
	      autoDetect = Boolean.valueOf(strVal).booleanValue();
	    }

	  } else {
	    autoDetect=false;
	  }


	  if (!autoDetect) {
	    ignoredSourcePath = new Path(persistance.get(IGNORED_PATH_KEY));
	    sourcePath = new Path(persistance.get(SOURCEPATH_KEY));
	    classPath = new Path(persistance.get(CLASSPATH_KEY));
	    String javadoc = persistance.get(JAVADOCPATH_KEY);
	    if(javadoc == null || javadoc.length() == 0) {
	      javadoc = "http://java.sun.com/j2se/1.5.0/docs/api";
	    }
	    javadocPath=new Path(javadoc);
	    dependencies = new Path(persistance.get(DEPENDENCIES_KEY));
	  } else {
	    // create empty paths
	    ignoredSourcePath = new Path();
	    sourcePath = new Path();
	    classPath = new Path();
	    javadocPath=new Path();
	    dependencies = new Path();
	  }
	}


	public static final String MILESTONE_DIR_KEY = "rit.milestone.dir";
  /**
  * FIXME: currently returns RIT home, not project dir
  */
 public static String getProjectHome() {
   return RuntimePlatform.getConfigDir();
 }

 public String getMilestoneDir() {
   String dir = this.get(MILESTONE_DIR_KEY);

   return dir;
 }

 public void setMilestoneDir(String path) {
   this.set(MILESTONE_DIR_KEY, path);
 }

	public void setDependencies(Path dependentProjects) {
	  this.dependencies = dependentProjects;
	}

  public void setAutoDetect(boolean b) {
    isAutoDetect = Boolean.valueOf(b);
  }


}
