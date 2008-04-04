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
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.transformations.TransformationActuator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Part of the frozen Transformation Framework. Implementation of
 *  {@link ProjectView}. Is used by TransformationFramework, to register
 *  changes made by refactorings.
 *
 * @author  Arseni Grigorjev
 */
public class ProjectViewImpl implements ProjectView {
  
  private List transactions = new ArrayList(20);
  private ProjectViewTransaction currentTransaction = null;
  
  /** Map, that allows to find all triads for specified object */
  private MultiValueMap objectToTriad = new MultiValueMap();
  
  /** Map, that allows to find all triads for specified subject */
  private MultiValueMap subjectToTriad = new MultiValueMap();
  
  private boolean mappingExpired = false;

  public ProjectViewImpl() {
  }
  
  private void expireMapping(){
    mappingExpired = true;
  }
  
  private void checkTransactionStarted() {
    if (currentTransaction == null){
      throw new IllegalStateException("Should start transaction first.");
    }
  }

  public int beginTransaction(TransformationActuator actuator){
    currentTransaction = new ProjectViewTransaction();
    transactions.add(currentTransaction);
    return transactions.size()-1;
  }
  
  public void endTransaction(){
    currentTransaction = null;
  }

  /**
   * @return unique number of current active transaction or -1, if there is no
   *   transaction
   */
  public int getCurrentTransactionId(){
    return (currentTransaction != null) ? transactions.size()-1 : -1;
  }
  
  public boolean isKilledTransaction(int transactionId){
    return transactions.get(transactionId) == null;
  }
  
  public boolean isActiveTransaction(int id){
    return ((ProjectViewTransaction) transactions.get(id)).isActive();
  }

  public void disableTransaction(int id) {
    expireMapping();
    ProjectViewTransaction transaction = ((ProjectViewTransaction) transactions
        .get(id));
    if (transaction != null){
      transaction.setActive(false);
    }
  }
  
  public void enableTransaction(int id){
    expireMapping();
    ProjectViewTransaction transaction = ((ProjectViewTransaction) transactions
        .get(id));
    if (transaction != null){
      transaction.setActive(true);
    }
  }
      
  public void killTransaction(int transactionId){
    expireMapping();
    if (transactions.get(transactionId) == currentTransaction){
      currentTransaction = null;
    }
    // do not *remove* from list, to ensure correct indexing!
    transactions.set(transactionId, null);
  }

  public void add(List triads) {
    checkTransactionStarted();
    for (int i = 0, max_i = triads.size(); i < max_i; i++){
      Triad currentTriad = (Triad) triads.get(i);
      createMappingFor(currentTriad);
      currentTransaction.add(currentTriad);
    }
  }
  
  public void add(Triad triad){
    checkTransactionStarted();
    createMappingFor(triad);
    currentTransaction.add(triad);
  }
  
  private void createMappingFor(Triad triad){
    objectToTriad.put(triad.getObject(), triad);
    subjectToTriad.put(triad.getSubject(), triad);
  }
  
  private void clearMapping() {
    objectToTriad.clear();
    subjectToTriad.clear();
  }
  
  private void ensureMappingIsUpToDate(){
    if (mappingExpired){
      clearMapping();
      
      ProjectViewTransaction currentTransaction;
      List triads;
      for (int i = 0, max_i = transactions.size(); i < max_i; i++){
        currentTransaction = (ProjectViewTransaction) transactions.get(i);
        if (currentTransaction != null && currentTransaction.isActive()){
          triads = currentTransaction.getTriads();
          for (int j = 0, max_j = triads.size(); j < max_j; j++){
            createMappingFor((Triad) triads.get(j));
          }
        }
      }
    }
  }
  
  // Interface methods implementation
 
  public List getAllTriads(){
    ensureMappingIsUpToDate();
    final List result = new ArrayList(40);
    
    ProjectViewTransaction currentTransaction;
    for (int i = 0, max_i = transactions.size(); i < max_i; i++){
      currentTransaction = (ProjectViewTransaction) transactions.get(i);
      if (currentTransaction != null && currentTransaction.isActive()){
        result.addAll(currentTransaction.getTriads());
      }
    }

    return result;
  }
  
  public List getTriads(Object subject, Class predicate, Object object){
    ensureMappingIsUpToDate();
    List result;
    if (subject == null && object == null){
      result = getAllTriads();
    } else if (subject == null) {
      result = new ArrayList(ensureNotNull(objectToTriad.get(object)));
    } else if (object == null){
      result = new ArrayList(ensureNotNull(subjectToTriad.get(subject)));
    } else {
      List bySubject = ensureNotNull(subjectToTriad.get(subject));
      List byObject = ensureNotNull(objectToTriad.get(object));
      result = CollectionUtil.intersection(bySubject, byObject);
    }
    
    if (predicate != null){
      Triad triad;
      for (int i = 0; i < result.size(); i++){
        triad = (Triad) result.get(i);
        if (!predicate.isAssignableFrom(triad.getClass())){
          result.remove(i--);
        }
      }
    }

    return result;
  }

  /**
   * @return EMPTY_LIST, if list is null; else returns same list instance.
   */
  private List ensureNotNull(List list) {
    return (list == null) ? Collections.EMPTY_LIST : list;
  }
}
