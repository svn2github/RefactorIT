/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.refactorings.changesignature.analyzer.RecursiveDeleteParameterModel;

import java.util.List;


/**
 * Class for modifying existing parameter
 *
 * @author Tonis Vaga
 * @author Aleksei sosnovski
 */
public class ExistingParameterInfo extends ParameterInfo {
  private BinParameter parameter;

  private RecursiveDeleteParameterModel deleteModel;

  private List comments;

  public ExistingParameterInfo(BinParameter par, List comments) {
    super(par.getTypeRef(), par.getName(), par.getIndex());

    this.parameter = par;

    this.comments = comments;

    setModifiers(par.getModifiers());
  }

  public ExistingParameterInfo(BinParameter par) {
    super(par.getTypeRef(), par.getName(), par.getIndex());

    this.parameter = par;

    this.comments = null;

    setModifiers(par.getModifiers());
  }

  /**
   *
   * @param method method in same hierarchy
   */
  ExistingParameterInfo cloneFor(BinMethod method) {
    if (this.parameter.getMethod() == method) {
      return this;
    }

    ExistingParameterInfo result =
        new ExistingParameterInfo(method.getParameters()[getIndex()]);

    if (!getType().equals(parameter.getTypeRef())) {
      // type changed
      result.setType(getType());
    }

    if (!getName().equals(parameter.getName())) {
      // name changed
      result.setName(getName());
    }

    result.setComments(this.comments);

    return result;
  }

  public BinParameter getOriginalParameter() {
    return this.parameter;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ExistingParameterInfo)) {
      return false;
    }

    return this.parameter == ((ExistingParameterInfo) obj).parameter;
  }

  public void setDeleteParameterModel(RecursiveDeleteParameterModel model) {
    this.deleteModel = model;
  }

  public RecursiveDeleteParameterModel getDeleteModel() {
    return this.deleteModel;
  }

  public void setComments(List comments) {
    this.comments = comments;
  }

  public List getComments() {
    return this.comments;
  }
}
