/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.pmd;

  import net.sf.refactorit.audit.Audit;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


  // MyRender().render() returns list of violations were put in ReportContainers
  // which is related to checked file
  // uses PMD class&methods for working, like: RuleViolation etc
public class MyRender{

  protected List out_report = new ArrayList();

  // @param report -object are got from PMD
  public List render(Report report) {

    for (Iterator i = report.iterator(); i.hasNext();) {
      RuleViolation rv = (RuleViolation) i.next();
      out_report.add(
          new ReportContainer(rv.getLine(), rv.getRule().getPriorityName(),
              StringUtil.replaceString(rv.getDescription(), '\"', "'"),new Audit(rv.getRule()).getKey()));
    }
    return out_report;
  }

  // print all
  public void print() {
    ReportContainer rc;
    if (out_report.size()==0) {
      System.out.println("there is no problems, maybe");
    }
    for(int x=out_report.size();x>0;x--) {
      rc=(ReportContainer)out_report.get(x-1);
      System.out.println(rc);
    }
  }

}
