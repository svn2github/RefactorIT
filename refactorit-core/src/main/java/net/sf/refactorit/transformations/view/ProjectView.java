/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations.view;

import net.sf.refactorit.common.util.CollectionUtil;

import java.util.List;


/**
 *  Part of the frozen Transformation Framework. Represents a virtual project
 *  state, which is updated by TransformationManagaer, on request of 
 *  refactorings.<br> This View is used by different analyzers and refactorings
 *  (TransformationActuator(s)), to analyze state of project, after several
 *  changes (transformations).<br> A basic unit of modification on this level
 *  is a {@link Triad}.
 *
 * @author  Arseni Grigorjev
 */
public interface ProjectView {

  ProjectView EMPTY_PROJECT_VIEW = new ProjectView(){
    public List getAllTriads(){
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }
    public List getTriads(Object s, Class p, Object o){
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }
  };
  
  /** @return list of all triads */
  List getAllTriads();
  
  /**
   *  Returns filtered list of triads
   *
   *  @param subject give null for all subjects
   *  @param predicate give null for all types of triads
   *  @param object give null for all objects
   *
   *  @return list of triads that match given parameters
   */
  List getTriads(Object subject, Class predicate, Object object);
}
