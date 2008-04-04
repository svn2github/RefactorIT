/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public final class BinModifierBuffer {
  int modifier;

  /**
   * @param modifier
   */
  public BinModifierBuffer(int modifier) {
    this.modifier = modifier;
  }

  public final void clearFlags(int flag) {
    modifier = BinModifier.clearFlags(modifier, flag);
  }

  public final int getModifiers() {
    return modifier;
  }

  public final void setFlag(int flag) {
    modifier = BinModifier.setFlags(modifier, flag);
  }

  public final boolean hasFlag(int flag) {
    return BinModifier.hasFlag(modifier, flag);
  }

  /**
   * Combine this modifier with argument modifiers, so if some argument modifier
   *  is different from this modifier set that parameter flag. Otherwise, keep existing flag.
   * For example, if this.modifier has STATIC flag and A has STATIC but B doesn't, clear STATIC flag.
   * Doesn't work right with privileged access, result can have all modifiers( PROTECTED, PRIVATE etc)
   * @param B
   * @param C
   */
  public final void keepChangedFlags(int B, int C) {
    int A = modifier;
    modifier = (~A & ~B & C) | (~A & B & C) | (A & B & C) | (~A & B & ~C);
  }

  public final int getPrivilegeFlags() {
    return BinModifier.getPrivilegeFlags(modifier);
  }
}
