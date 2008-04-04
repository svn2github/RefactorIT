/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;

/**
 * @author  RISTO A
 */
public class StringArrayArguments extends Arguments {
  ArgumentsParser p;

  public StringArrayArguments(String[] args) {
    p = new ArgumentsParser(args);
  }

  public StringArrayArguments(String args) {
    p = new ArgumentsParser(args);
  }

  public StringArrayArguments() {
    p = new ArgumentsParser();
  }

  public String getSourcepath() {
    return p.getPathParameterValue(SOURCEPATH);
  }

  public String getClasspath() {
    return p.getPathParameterValue(CLASSPATH);
  }

  public String getFormat() {
    return p.getParameterValue(FORMAT);
  }

  public String getOutputFile() {
    return p.getParameterValue(OUTPUT);
  }

  public String getProfile() {
    return p.getParameterValue(PROFILE);
  }

  public boolean isNotUsedAction() {
    return p.hasParameter(NOTUSED);
  }

  public boolean isMetricsAction() {
    return p.hasParameter(METRICS);
  }

  public boolean isAuditAction() {
    return p.hasParameter(AUDIT);
  }

  protected boolean hasParameter(int param) {
    return p.hasParameter(param);
  }
}
