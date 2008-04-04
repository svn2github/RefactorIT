/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class TransformationList {
  private final RefactoringStatus status;
  private final ArrayList transformationList = new ArrayList();
  
  
  public TransformationList() {
    status = new RefactoringStatus();
  }
  
  /**
   * Addes to the end of transformationList all transformations from the other
   * transformationList and merges statuses.
   */
  public void merge(TransformationList transList) {
    this.getStatus().merge(transList.getStatus());
     transformationList.addAll(transList.getTransformationList());
  }
  
  public void merge(RefactoringStatus status) {
    this.getStatus().merge(status);
  }
  
  public void add(Object o) {
    CollectionUtil.addNew(transformationList, o);
  }
  
  public Iterator iterator() {
    return transformationList.iterator();
  }
  
  public int getTransformationsCount() {
    return transformationList.size();
  }
  
  public RefactoringStatus getStatus() {
    return this.status;
  }
  
  public void clear() {
    transformationList.clear();
  }
  
  public List getTransformationList() {
    return transformationList;
  }
}
