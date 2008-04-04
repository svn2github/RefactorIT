/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ant;


import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.Cli;
import net.sf.refactorit.cli.ProjectInitException;
import net.sf.refactorit.cli.SupportedArguments;
import net.sf.refactorit.standalone.StartUp;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;


public class RefactorIt extends Task implements SupportedArguments {
  String profile = "";
  String format = "";
  String output = "";
  String action = "";

  PathParameter classpath = new PathParameter();
  PathParameter sourcepath = new PathParameter();

  public void setClasspath(Path p) {
    classpath.set(p);
  }

  public Path createClasspath() {
    return classpath.create(getProject());
  }

  public void setClasspathRef(Reference r) {
    classpath.setRef(r, getProject());
  }

  public void setSourcepath(Path p) {
    sourcepath.set(p);
  }

  public Path createSourcepath() {
    return sourcepath.create(getProject());
  }

  public void setSourcepathRef(Reference r) {
    sourcepath.setRef(r, getProject());
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Arguments getArguments() {
    return new AntTaskArguments();
  }

  protected class AntTaskArguments extends Arguments {
    public String getSourcepath() {
      return sourcepath.getStringForm();
    }

    public String getClasspath() {
      return classpath.getStringForm();
    }

    public String getFormat() {
      return format;
    }

    public String getOutputFile() {
      return output;
    }

    public String getProfile() {
      return profile;
    }

    protected boolean hasParameter(int param) {
      return true;
    }

    public boolean isAuditAction() {
      return isAction(AUDIT);
    }

    public boolean isMetricsAction() {
      return isAction(METRICS);
    }

    public boolean isNotUsedAction() {
      return isAction(NOTUSED);
    }

    public boolean isAction(int actionNr) {
      String switchName = (String) ARGS.get(actionNr);
      String actionName = switchName.substring(1);

      return actionName.equalsIgnoreCase(action);
    }
  }


  public void execute() throws BuildException {
    StartUp.initCliEnvironment();

    try {
      new Cli().run(getArguments());
    } catch (ProjectInitException e) {
      throw new BuildException(e.getMessage());
    }
  }
}
