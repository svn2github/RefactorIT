/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.commonIDE.IDEController;

import org.apache.log4j.Logger;

import java.util.Collection;


/**
 * @author Arseni Grigorjev
 */
public abstract class BinItemReference {
  public static final Logger log = AppRegistry.getLogger(BinItemReference.class);

  public static boolean cacheEnabled = true;

  public abstract Object findItem(Project project);

  public Object restore(Project project) {
    try {
      Object item = findItem(project);
      if (item == null) {
        log.warn("failed to restore: " + this + ", using project: " + project);
      }
      return item;
    } catch (Exception e){
      log.warn("failed to restore: " + this + ", using project: " + project, e);
      return null;
    }
  }

  /**
   * Use this factory method if:<br/>
   * - You have a variable of type Object, but you know, that actually
   *    it is an item, that can be wrapped into BinItemReference<br/>
   * - You have a Collection or an array of items, that can be wrapped into
   *    BinItemReference<br/>
   * - To wrap null or Object into FakProjectReference<br/>
   *
   * @param obj item You want to wrap into BinItemReference
   * @return instance of BinItemReference or null, if doesn`t know how to handle
   *    this type.
   */
  public static BinItemReference create(final Object obj) {
    try {
      if (obj instanceof Referable) {
        return ((Referable) obj).createReference();
      } else if (obj instanceof Object[]) {
        return new ArrayReference((Object[]) obj);
      } else if (obj instanceof Collection) {
        return new CollectionReference((Collection) obj);
      } else if (obj == null || Object.class.equals(obj.getClass())) {
        // to support legacy code
        return IDEController.getInstance().getActiveProject().createReference();
      }
    } catch (UnsupportedOperationException e){
      // not supported for this type
    } catch (Exception e){
      log.debug("Could not create BinItemReference!", e);
    }
    return null;
  }

  public String toString(){
    return ClassUtil.getShortClassName(this);
  }
}
