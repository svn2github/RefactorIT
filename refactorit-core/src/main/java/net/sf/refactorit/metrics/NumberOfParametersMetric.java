/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.metrics;

import net.sf.refactorit.classmodel.BinMethod;


/**
 * Number of parameters (NP) metric.

 */

public class NumberOfParametersMetric {

  /** Hidden constructor. */

  private NumberOfParametersMetric() {}

  /**
   * Calculates number of parameters metric for the method.

   *

   * @param method method.

   *

   * @return number of parameters metric for the <code>method</code>.

   */

  public static int calculate(BinMethod method) {

    return method.getParameters().length;

  }

}
