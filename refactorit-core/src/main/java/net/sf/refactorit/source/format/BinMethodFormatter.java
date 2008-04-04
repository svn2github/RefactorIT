/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;


import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.SourceCoordinate;

import java.util.Arrays;


public class BinMethodFormatter extends BinItemFormatter {

  private BinMethod method = null;
  private int baseIndent;

  public BinMethodFormatter(BinMethod method) {
    this.method = method;
    baseIndent = new BinTypeFormatter(
        method.getOwner() != null ? method.getOwner().getBinCIType() : null
        ).getMemberIndent();
  }

  public String print() {
    return formHeader()+formBody()+formFooter();
//    String modifierString =
//      new BinModifierFormatter(method.getModifiers()).print();
//
//    BinTypeRef ref = method.getReturnType();
//    BinType type = ref.getBinType();
//    String ResultType = type.getName();
//
//    String identifier = method.getName();
//
//    BinParameter[] params = method.getParameters();
//    String parameters = new String();
//    for(int i = 0; i < params.length; i++) {
//    // params[i]
//      parameters += params[i].toString();
//    }
//    //System.out.println(parameters);
//
//    return new String();
  }

  public String formHeader() {
    String result = getSignatureString();

    String throwsString = getHeaderThrowsString(method.getThrows(), result.length());
    result += throwsString;  
    
    boolean movedThrowsToNewline = 
      throwsString.indexOf(FormatSettings.LINEBREAK) >= 0;

    if (!method.isAbstract()) {
      if (FormatSettings.isNewlineBeforeBrace()) {
        result += FormatSettings.LINEBREAK
        + FormatSettings.getIndentString(baseIndent)
        + "{" + FormatSettings.LINEBREAK;
      } else {
//        if (throwses.length > 0) {
//          result += ' ';
//        }
        result += " {" + FormatSettings.LINEBREAK;

        if (movedThrowsToNewline) {
          result += FormatSettings.LINEBREAK;
        }
      }
    } else {
      result += ";";
    }
    return result;
  }

  public String getHeaderThrowsString(BinMethod.Throws[] throwses, int length) {
    
    boolean movedThrowsToNewline = false;
    String throwsString = "";
    
    for (int i = 0; i < throwses.length; i++) {
      String except = "";
      String comma = "";
      if (i > 0) {
        comma = ",";
      } else {
        except += " throws" + ' ';
      }
      except += formatTypeName(throwses[i].getException());

      if (length + comma.length() + except.length() > 80) {
        String newLine = FormatSettings.LINEBREAK +
            FormatSettings.getIndentString(
            baseIndent + FormatSettings.getContinuationIndent());
        movedThrowsToNewline = true;
        length = newLine.length();
        throwsString += comma + newLine;
      } else if (comma.length() > 0) {
        length += comma.length() + 1;
        throwsString += comma + ' ';
      }

      length += except.length();

      if (movedThrowsToNewline && i == 0) {
        throwsString += except.substring(1);
      } else {
        throwsString += except;
      }
    }
    return throwsString;
  }
  
  public String getSignatureString() {
    int modifier = method.getModifiers();
    BinModifierFormatter formatter = new BinModifierFormatter(modifier);
    formatter.needsPostfix(true);
    //formatter.setIndent(baseIndent);
    String modifierString = formatter.print();


    // FIXME: BinModifierFormatter should not add 'abstract'
    // when it is interface method
    if (method.getOwner() != null
        && method.getOwner().getBinCIType().isInterface()) {
      modifierString = StringUtil.replace(modifierString, "abstract ", "");
    }

    String result = FormatSettings.getIndentString(baseIndent) + modifierString;

    if (!(method instanceof BinConstructor)) {
      result += formatTypeName(method.getReturnType()) + ' ';
    }

    result += method.getName();
    if (FormatSettings.isSpaceBeforeParenthesis()) {
      result += " ";
    }
    result += "(";

    BinVariable[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) {
        result += "," + ' ';
      }
      BinParameterFormatter parameterFormatter =
        new BinParameterFormatter((BinParameter)params[i]);
      parameterFormatter.setFqnTypes(this.getFqnTypes());
      result += parameterFormatter.print();
    }

    result += ")";
    return result;
  }
  
  public String formBody() {
    return StringUtil.EMPTY_STRING;
  }

  public String formFooter() {
    return
    	(method.isAbstract() ? "" : FormatSettings.getIndentString(baseIndent) + "}")
    	+ FormatSettings.LINEBREAK;
  }

  public int getMemberIndent() {
    return getIndent(Arrays.asList(
        method.getBody().getStatements()),
        method.getStartColumn());
  }

  public SourceCoordinate findNewMemberPosition() {
    return method.findNewStatementPositionAtEnd();
  }
}
