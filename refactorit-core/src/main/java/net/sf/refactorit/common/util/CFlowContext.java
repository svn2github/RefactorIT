/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import org.apache.log4j.Logger;

import java.util.Hashtable;


/**
 * It is responsible for holding the context objects
 * that are needed by other objects in the current control flow
 * (e.g. in the current thread execution context).
 *
 * For example: if you want to get access to the object in the beginning
 * of control flow, then you add(..) that object into the CFlowContext and get(..)
 * the added object for example in the end of control flow.
 *
 * IMPORTANT: in the current executing function if you call add(..) then
 * you MUST also call remove(..) in the same function.
 *
 */
public final class CFlowContext {
  /**
   * contains the context objects.
   */
  private static final Hashtable objects = new Hashtable();

  /**
   * Adds the object into the context of current thread.
   *
   * @param objectName the name the user can later use to retrieve the
   * object as specified by the second parameter.
   */
  public static synchronized void add(String objectName, Object object) {
    Logger logger = AppRegistry.getLogger(CFlowContext.class);

    // Check the preconditions
    if ((objectName == null) || (object == null)) {
      logger.error("",
          new Exception("Precondition violated in CFlowContext.add(..): "));
      //new Exception("Precondition violated in CFlowContext.add(..): ")
      //    .printStackTrace(System.err);
    }
    int oldAmount = objects.size();
    String objectId = getCurrentThreadId() + objectName;
    Object inObject = objects.get(objectId);
    if (inObject != null) {
      // there is already such object with the key
      logger.error("",
          new Exception("Precondition violated in CFlowContext.add(..): "));
      //new Exception("Precondition violated in CFlowContext.add(..): ")
      //    .printStackTrace(System.err);
    }

    // put specified object into hashtable
    objects.put(objectId, object);

    // check the postconditions
    if ((oldAmount + 1) != objects.size()) {
      logger.error("",
          new Exception("Postcondition violated in CFlowContext.add(..): "));
      //new Exception("Postcondition violated in CFlowContext.add(..)")
      //    .printStackTrace(System.err);
    }
  }

  /**
   * removes the added object from the context of control flow.
   *
   * @param objectName the object with the name to be removed from
   * the context of control flow.
   */
  public static synchronized void remove(String objectName) {
    // check the preconditions
    if (objectName == null) {
      return;
    }

    // remove the object with the name from current context of control flow.
    String objectId = getCurrentThreadId() + objectName;
    objects.remove(objectId);

    // check the postconditions
//        if (removedObject == null) {
//            new Exception("Postcondition violated in CFlowContext.remove("
//                + objectId + ")").printStackTrace(System.err);
//        }
  }

  /**
   * returns the object with the specified name from the context
   * of control flow.
   *
   * @param objectName of object that was added previously into the context
   * of control flow.
   * @return Object with the name of objectName that was added previously,
   * or null if no object with the name of objectName is in the context of current
   * control flow.
   */
  public static Object get(String objectName) {
    String objectId = getCurrentThreadId() + objectName;
    return objects.get(objectId);
  }

  /**
   * returns the unical id for the current executing thread.
   *
   * it is needed to separate the executing threads and their
   * contexts.
   */
  private static String getCurrentThreadId() {
    int currentThreadId = Thread.currentThread().hashCode();
    return Integer.toString(currentThreadId);
  }
}
