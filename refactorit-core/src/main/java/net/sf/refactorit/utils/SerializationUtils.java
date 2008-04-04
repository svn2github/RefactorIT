/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;

/**
 *
 * @author  Arseni Grigorjev
 */
public final class SerializationUtils {

  /**
   *  This method checks recursively, if a class is serializable (consider
   *  serializable class, which implements java.io.Serializable interface
   *  or extends an other serializable class.
   */
  public static boolean isSerializable(BinCIType clas){
    if (clas == null){
      return false;
    }
    BinTypeRef serializableRef
        = clas.getProject().getTypeRefForName("java.io.Serializable");

    BinTypeRef[] interfaces = clas.getTypeRef().getInterfaces();
    boolean serializable = false;
    for (int i = 0; i < interfaces.length; i++){
      if (interfaces[i].equals(serializableRef)){
        serializable = true;
        break;
      }
    }
    if (serializable){
      return true;
    }
    if (clas.getTypeRef().getSuperclass() == null){
      return false;
    }
    return isSerializable(clas.getTypeRef().getSuperclass().getBinCIType());
  }
}
