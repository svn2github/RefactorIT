/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;


import net.sf.refactorit.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ArgumentsParser implements SupportedArguments {

  private List params;

  public ArgumentsParser(String[] params) {
    this.params = Arrays.asList(params);
  }

  public ArgumentsParser(String paramline) {
    this(StringUtil.split(paramline, " "));
  }

  public ArgumentsParser() {
    this(new String[0]);
  }

  protected String getParameterValue(int param) {
    String name = ARGS.get(param).toString();

    if (params.indexOf(name) != -1) {
      return getParamAsString(params.indexOf(name) + 1);
    } else {
      return "";
    }
  }

  public boolean hasParameter(int param) {
    return params.contains(ARGS.get(param));
  }

  public String getPathParameterValue(int param) {
    String result = "";

    final String paramName = (String) ARGS.get(param);
    for (int valueIdx = params.indexOf(paramName) + 1;
        valueIdx < params.size() && (!parameterStartsAt(valueIdx));
        valueIdx++) {

      result = appendPathItem(result, valueIdx);
    }

    return result;
  }

  private String appendPathItem(String input, int valueIdx) {
    if (input.length() > 0) {
      input += StringUtil.PATH_SEPARATOR;
    }

    return input + getParamAsString(valueIdx);
  }

  private String getParamAsString(int valueIdx) {
    return (String) params.get(valueIdx);
  }

  private boolean parameterStartsAt(int valueIdx) {
    return getParamAsString(valueIdx).startsWith("-");
  }

  public List getUnknownTags() {
    List result = new ArrayList();
    for (int i = 0; i < params.size(); i++) {
      String param = getParamAsString(i);
      if (parameterStartsAt(i) && (!isReckognizedParameter(param))) {
        result.add(param);
      }
    }
    return result;
  }

  private boolean isReckognizedParameter(String name) {
    return ARGS.contains(name);
  }
}
