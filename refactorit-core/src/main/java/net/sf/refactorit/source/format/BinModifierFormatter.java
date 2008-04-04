/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinModifier;


public class BinModifierFormatter {

  private int modifier;
  private boolean nameOfModifier;
  private boolean onlyNonAccessModifiers;
  private boolean needsPostfix;
  private int indent;

  public BinModifierFormatter(int modifier) {
    this(modifier, false, false);
  }

  public BinModifierFormatter(int modifier, boolean nameOfModifier) {
    this(modifier, nameOfModifier, false);
  }

  public BinModifierFormatter(int modifier, boolean nameOfModifier, boolean onlyNonAccessModifiers) {
    this.modifier = modifier;
    this.nameOfModifier = nameOfModifier;
    this.onlyNonAccessModifiers = onlyNonAccessModifiers;
  }

  public BinModifierFormatter(int modifier, boolean nameOfModifier, boolean onlyNonAccessModifiers, boolean needsPostfix) {
    this.modifier = modifier;
    this.nameOfModifier = nameOfModifier;
    this.onlyNonAccessModifiers = onlyNonAccessModifiers;
    this.needsPostfix = needsPostfix;
  }

  public void needsPostfix(boolean needsPostfix) {
    this.needsPostfix = needsPostfix;
  }

  public void setIndent(int indent) {
    this.indent = indent;
  }

  public String print() {
    return format(modifier, nameOfModifier, onlyNonAccessModifiers);
  }



  /**
   * @param modifier contains set bits according to constants defined in BinModifier
   * @param nameOfModifier if true then return name of modifier as defined in
   * BinModifier.names and "package private" if modifier is package private
   * @param onlyNonAccessModifiers return only not access modifiers
   */
  public String format(int modifier, boolean nameOfModifier,
      boolean onlyNonAccessModifiers) {
    if (nameOfModifier && (modifier == BinModifier.PACKAGE_PRIVATE)
        && !onlyNonAccessModifiers) {
      return "package private";
    }

    StringBuffer temp = new StringBuffer(20);

    for (int i = 0; i < BinModifier.modifiers.length; i++) {
      if (((modifier & BinModifier.modifiers[i]) != 0) && (BinModifier.modifiers[i] != BinModifier.INTERFACE)) {
        if (onlyNonAccessModifiers) {
          if (BinModifier.modifiers[i] > 0x0004) {
            temp.append(BinModifier.names[i] + " ");
          }
        } else {
          temp.append(BinModifier.names[i] + " ");
        }
      }
    }

    if (temp.length() > 0) {
      temp.setLength(temp.length() - 1);
    }

    if(needsPostfix && temp.length() > 0) {
      temp.append(" ");
    }

    if(indent > 0) {
      temp.append(FormatSettings.getIndentString(indent));
    }
    return temp.toString();
  }

}
