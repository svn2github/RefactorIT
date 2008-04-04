/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;

import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;


public abstract class ParameterInfo {
  private BinTypeRef type;
  private String name;
  private int index;

  private int modifiers;

  public ParameterInfo(BinTypeRef type, String name, int index) {
    this.type = type;
    this.name = name;
    this.index = index;

    modifiers = BinModifier.NONE;
  }

  public int getModifiers() {
    return modifiers;
  }

  public String getName() {
    return this.name;
  }

  public BinTypeRef getType() {
    return this.type;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setType(final BinTypeRef type) {
//    Assert.must( type != null,"type == null");
    this.type = type;
  }

  public void setModifiers(final int modifiers) {
    this.modifiers = modifiers;
  }

  public int getIndex() {
    return this.index;
  }

  public void setIndex(final int index) {
    this.index = index;
  }

  public void changeName(String newName) {
    setName(newName);
  }

  public void changeType(BinTypeRef newType) {
    setType(newType);
  }
}
