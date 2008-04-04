/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.pmd;

import net.sf.refactorit.audit.AuditRule.Priority;


/**
 * Class contains any violation from PMD in useable form to RefactorIT's
 * violation system,
 * (it used to print error messages in net.sf.refactorit.audit.PMDrulesAddOn)
 * @author Kirill Buhalko
 */
public class ReportContainer {

  private int line;
  private String description;
  private Priority priority;
  private String type;

  public ReportContainer(int line, String priorityPMD, String description, String type) {

    if (priorityPMD.equals("Low")) {
      this.priority = Priority.LOW;
    } else
    if (priorityPMD.equals("High")) {
      this.priority = Priority.HIGH;
    } else {
      this.priority = Priority.NORMAL;
    }

    this.line = line;
    this.description = description;
    this.type = type;
  }

  public String toString() {
    String toReturn = "";

    toReturn += line + " " + description;
    return toReturn;
  }

  public int getErrorLine() {
    return line;
  }

  public String getErrorDescription() {
    return description;
  }

  public Priority getPriority() {
    return priority;
  }
  
  public String getType() {
    return type;
  }
}
