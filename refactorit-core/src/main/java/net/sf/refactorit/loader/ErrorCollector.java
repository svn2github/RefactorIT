/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;



import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.utils.LinePositionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


public class ErrorCollector {
  private final ProjectLoader projectLoader;

  private final ArrayList userFriendlyErrors = new ArrayList(8);
  private final ArrayList userFriendlyInfos = new ArrayList(1);

  /** 0 means errors are critical - any other means recoverable */
  private int userErrorRecoverableSection = 0;

  private int criticalErrorsCount = 0;

  public ErrorCollector(final ProjectLoader projectLoader) {
    this.projectLoader = projectLoader;
  }

  public void startRecoverableUserErrorSection() {
    userErrorRecoverableSection++;
  }

  public void endRecoverableUserErrorSection() {
    userErrorRecoverableSection--;
    if (Assert.enabled) {
      Assert.must(userErrorRecoverableSection >= 0,
          "userErrorCriticalSection < 0");
    }
  }

  public void addNonCriticalUserFriendlyError(final UserFriendlyError anError) {
    if (!errorAlreadyReported(anError)) {
      userFriendlyErrors.add(anError);
    }
  }

  public void addUserFriendlyInfo(final UserFriendlyError anError) {
    if (!infoAlreadyReported(anError)) {
      userFriendlyInfos.add(anError);
    }
  }

  public void addUserFriendlyError(final UserFriendlyError anError) {
    if (!errorAlreadyReported(anError)) {
      if (userErrorRecoverableSection == 0) {
        ++criticalErrorsCount;

//        if( anError.getDescription().equals( UserFriendlyError.NO_DESCRIPTION_MSG) ) {
//          criticalErrorsCount+=MAX_CRITICAL_ERRORS;
//        } else {
//          ++criticalErrorsCount;
//        }
      }

      userFriendlyErrors.add(anError);
    }
  }

  private boolean infoAlreadyReported(final UserFriendlyError error) {
    return userFriendlyInfos.contains(error);
  }

  private boolean errorAlreadyReported(final UserFriendlyError error) {
    return userFriendlyErrors.contains(error);
  }

  public Iterator getUserFriendlyErrors() {
    return userFriendlyErrors.iterator();
  }

  public Iterator getUserFriendlyInfos() {
    return userFriendlyInfos.iterator();
  }

  public Collection getErroneousSources() {
    HashSet result = new HashSet();

    ArrayList errors = new ArrayList(userFriendlyErrors);
    errors.addAll(userFriendlyInfos);

    for (int i = 0, max = errors.size(); i < max; i++) {
      UserFriendlyError error = (UserFriendlyError) errors.get(i);
      if (error.getCompilationUnit() != null) {
        result.add(error.getCompilationUnit().getSource());
      } else {
        // FIXME should mark somehow that we might need to rebuild much more
      }
    }

    return result;
  }

  public boolean hasUserFriendlyInfos() {
    return userFriendlyErrors.size() > 0;
  }

  public int amountOfUserFriendlyInfos() {
    return userFriendlyErrors.size();
  }

  public boolean hasCriticalUserErrors() {
    return criticalErrorsCount > 0;
  }

  /** Works most of the time, but not always -- no easy way of making it work for 100% of cases */
  public boolean hasErrorsCausedByWrongJavaVersion() {
    for (Iterator i = getUserFriendlyErrors(); i.hasNext(); ) {
      final UserFriendlyError e = (UserFriendlyError) i.next();

      if (e != null && e.getCompilationUnit() != null) { // fix of #1584
        final String errorLine = LinePositionUtil.extractLine(e.getLine(),
            e.getCompilationUnit().getContent());
        if (errorLine.indexOf("assert") != -1
            || errorLine.indexOf("enum") != -1
            || errorLine.indexOf("boxing") != -1
            || errorLine.indexOf("generic") != -1) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean hasUserFriendlyErrors() {
    return userFriendlyErrors.size() > 0;
  }

  public boolean hasErrors() {
    return hasUserFriendlyErrors() || hasCriticalUserErrors();
  }

  /**
   *  @return amount of critical errors produced during loading
   */
  public int amountOfCriticalErrors() {
    return criticalErrorsCount;
  }

  public int amountOfUserFriendlyErrors() {
    return userFriendlyErrors.size();
  }

  public void forgetAllLoadingErrors() {
    userErrorRecoverableSection = 0;
    criticalErrorsCount = 0;
    userFriendlyErrors.clear();
    userFriendlyInfos.clear();
  }

}
