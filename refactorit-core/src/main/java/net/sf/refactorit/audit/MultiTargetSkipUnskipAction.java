/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class MultiTargetSkipUnskipAction extends SkipUnskipAction {
  private Set skipTargets = new HashSet();
  private boolean skipAction;

  public MultiTargetSkipUnskipAction(String violation, boolean skipAction) {
    super(violation);
    this.skipAction = skipAction;
  }
  
  public String getKey() {
    return KEY;
  }
  
  public String getViolation(){
    return violation;
  }
  
  public boolean isSkipAction() {
    return this.skipAction;
  }
  
  public String getName() {
    return (skipAction) 
        ? "Add @refactorit.skip " + violation + " to selection" 
        : "Remove @refactorit.skip " + violation + " from selection";
  }
  
  public Set run(TreeRefactorItContext context, List violations) {
    TransformationManager manager = new TransformationManager(null);
    manager.setShowPreview(true);
    Set changedUnits = new HashSet();

    for (Iterator iter = skipTargets.iterator(); iter.hasNext(); ){
      BinMember owner = (BinMember) iter.next();
      changedUnits.add(owner.getCompilationUnit());
      if (skipAction) {
        insertTag(violation, manager, owner);
      } else {
        deleteTag(violation, manager, owner);
      }
    }

    if (manager.isContainsEditors()) {
      if (manager.performTransformations().isOk()) {
        return changedUnits;
      }
    }

    return Collections.EMPTY_SET;
  } 
  
  public void addTarget(Object anotherOwner){
    this.skipTargets.add(anotherOwner);
  }
}
