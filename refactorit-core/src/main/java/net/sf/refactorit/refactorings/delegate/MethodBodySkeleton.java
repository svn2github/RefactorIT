/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.StringTokenizer;


/**
 *
 *
 * @author Tonis Vaga
 */
public class MethodBodySkeleton {
  String content;

  /**
   * @param content
   */
  public MethodBodySkeleton(String content) {
    this.content = content;
  }

  public String getBodyAsString(int blockIdent) {
    StringTokenizer tokenizer = new StringTokenizer(content,
        FormatSettings.LINEBREAK);
    StringBuffer result = new StringBuffer(content.length());

    while (tokenizer.hasMoreTokens()) {
      result.append(FormatSettings.getIndentString(blockIdent) +
          tokenizer.nextToken());

      if (tokenizer.hasMoreTokens()) {
        result.append(FormatSettings.LINEBREAK);
      }
    }
    return result.toString();
  }

  public boolean isEmpty() {
    return content == "" || content == null;
  }

  /**
   * @param item
   */
  public static String createSuperCallStr(final MethodSkeleton skeleton) {

    BinMethod item = skeleton.getMethod();

    StringBuffer bodyStr = new StringBuffer();

    if (item.getReturnType().getBinType() != BinPrimitiveType.VOID) {
      bodyStr.append("return ");
    }
    bodyStr.append("super." + item.getName() + "(");

    BinParameter[] parameters = item.getParameters();

    for (int i = 0; i < parameters.length; i++) {
      bodyStr.append(parameters[i].getName());
      if ((i + 1) != parameters.length) {
        bodyStr.append(", ");
      }
    }
    bodyStr.append(");");

    return bodyStr.toString();
  }
}
