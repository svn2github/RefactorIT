/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.classfile.ClassFormatException;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.utils.TypeUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Loades java VM classes.
 */
public final class VMClassFilesLoader extends ClassFilesLoader {
  public VMClassFilesLoader(Project project) {
    super(project);
  }

  public BinType findTypeForQualifiedName(String qName) {
    BinType retVal = null;
    try {
      Class clazz = Class.forName(qName, false, this.getClass().getClassLoader());
      retVal = loadBinType(clazz);
    } catch (ClassNotFoundException cnf) {

    } catch (ClassFormatException e) { //current implementation for debugging purposes only
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      //System.out.println(sw);
      throw new RuntimeException(e.toString() + "\n" + e.getMessage());
    }

    return retVal;
  }

  private BinType loadBinType(Class clazz) throws ClassFormatException {
    String qClassname = clazz.getName();

    String className = (clazz.getConstructors())[0].getName();

    String packageName = clazz.getPackage().getName();
    BinPackage aPackage = getProject().createPackageForName(packageName);
    BinTypeRef curTypeRef = getProject().createCITypeRefForName(qClassname,
        getProject().getProjectLoader().getClassLoader());
    curTypeRef.setResolver(Resolver.getForClassFile(curTypeRef));

    BinTypeRef b_superclass = null;
    Class superClazz = clazz.getSuperclass();
    if (superClazz != null) {
      if (!clazz.isInterface()) {
        b_superclass = getFakeOrRealTypeRef(superClazz.getName());
      }
    }

    BinTypeRef b_owner = null;
    Class ownerClazz = clazz.getDeclaringClass();
    if (ownerClazz != null) {
      String ownerName = ownerClazz.getName();
      b_owner = getFakeOrRealTypeRef(ownerName);
      if (b_owner == null) {
        throw new ClassFormatException("Unable to locate owner:'" + ownerName
            + "' for class:'" + qClassname + "'");
      }
    }

    Class[] interfaces = clazz.getInterfaces();
    BinTypeRef[] b_interfaces = new BinTypeRef[interfaces.length];
    for (int i = 0; i < interfaces.length; ++i) {
      b_interfaces[i] = getFakeOrRealTypeRef(interfaces[i].
          getName(), true);
    }

    Method[] methods = clazz.getMethods();
    BinMethod[] b_methods = new BinMethod[methods.length];
    for (int q = 0; q < methods.length; q++) {
      Method method = methods[q];
      b_methods[q] = buildMethod(method.getModifiers(), method.getName(),
          method.getParameterTypes(), method.getReturnType(),
          method.getExceptionTypes());
    }

    int b_modifiers = clazz.getModifiers();

    BinConstructor[] b_constructors = null;
    if (!clazz.isInterface()) {
      Constructor[] constructors = clazz.getConstructors();
      b_constructors = new BinConstructor[constructors.length];
//      int defaultCnstrs = 0;
      for (int q = 0; q < constructors.length; q++) {
        Constructor ctor = constructors[q];

        b_constructors[q] = buildConstructor(ctor.getModifiers(),
            ctor.getParameterTypes(),
            ctor.getExceptionTypes());
//        if (b_constructors[q].getParameters().length == 0) {
//          ++defaultCnstrs;
//        }
//        if (defaultCnstrs > 1) {
//          b_constructors[q] = buildConstructor(
//              ctor.getModifiers(), ctor.getParameterTypes(),
//              ctor.getExceptionTypes());
//        }
      }
    }

    Field fields[] = clazz.getDeclaredFields();
    BinField[] b_fields = new BinField[fields.length];
    for (int q = 0; q < fields.length; q++) {
      Field field = fields[q];
      b_fields[q] = buildField(field.getModifiers(), field.getName(),
          field.getType());
    }

    Class[] inners = clazz.getDeclaredClasses();
    BinTypeRef[] b_inners;
    if (inners != null) {
      b_inners = new BinTypeRef[inners.length];
      for (int q = 0; q < inners.length; q++) {
        BinTypeRef curInner = getFakeOrRealTypeRef(inners[q].getName());
        if (curInner == null) {
          throw new ClassFormatException("Could not locate inner ref for name:"
              + inners[q].getName());
        }
        b_inners[q] = curInner;
      }
    } else {
      b_inners = BinTypeRef.NO_TYPEREFS;
    }

    BinCIType bc;
    if (clazz.isInterface()) {
      bc = new BinInterface(aPackage, className,
          b_methods, b_fields, null, b_inners, b_owner, b_modifiers, getProject());
    } else {
      bc = new BinClass(aPackage, className,
          b_methods, b_fields, null, b_constructors,
          BinInitializer.NO_INITIALIZERS, b_inners, b_owner, b_modifiers,
          getProject());
    }

    if (b_owner == null) {
      aPackage.addType(curTypeRef);
    }

    //System.err.println("Name is now: " + bc.getQualifiedName());
    bc.setOwners(curTypeRef);
    bc.setDeprecated(false);

    curTypeRef.setSuperclass(b_superclass);
    curTypeRef.setInterfaces(b_interfaces);

    return bc;
  }

  private BinMethod buildMethod(int modifiers, String name, Class[] paramTypes,
      Class returnType, Class[] exceptionTypes) throws ClassFormatException {

    BinParameter[] params = new BinParameter[paramTypes.length];
    for (int q = 0; q < paramTypes.length; q++) {
      Class parameter = paramTypes[q];
      params[q] = new BinParameter(parameter.getName(),
          getRefForMyType(parameter), parameter.getModifiers());
    }

    BinTypeRef _return = getRefForMyType(returnType);

    BinMethod.Throws[] exceptions;
    if (exceptionTypes != null) {
      exceptions = new BinMethod.Throws[exceptionTypes.length];
      for (int q = 0; q < exceptionTypes.length; q++) {
        exceptions[q] = new BinMethod.Throws(
            getFakeOrRealTypeRef(exceptionTypes[q].getName()));
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    BinMethod retVal = new BinMethod(name, params, _return, modifiers,
        exceptions, true);
//    retVal.setDeprecated(false);
    return retVal;
  }

  private BinConstructor buildConstructor(int modifiers, Class[] paramTypes,
      Class[] exceptionTypes) throws ClassFormatException {

    BinParameter[] params = new BinParameter[paramTypes.length];
    for (int q = 0; q < paramTypes.length; q++) {
      Class parameter = paramTypes[q];
      params[q] = new BinParameter(parameter.getName(),
          getRefForMyType(parameter), parameter.getModifiers());
    }

    BinMethod.Throws[] exceptions;
    if (exceptionTypes != null) {
      exceptions = new BinMethod.Throws[exceptionTypes.length];
      for (int q = 0; q < exceptionTypes.length; q++) {
        exceptions[q] = new BinMethod.Throws(
            getFakeOrRealTypeRef(exceptionTypes[q].getName()));
      }
    } else {
      exceptions = BinMethod.Throws.NO_THROWS;
    }

    BinConstructor retVal = new BinConstructor(params, modifiers, exceptions);
//    retVal.setDeprecated(false);
    retVal.setBody(null);
    return retVal;
  }

  private BinField buildField(int modifiers, String name,
      Class fieldType) throws ClassFormatException {

    BinTypeRef type = getRefForMyType(fieldType);

    BinField retVal = new BinField(name, type, modifiers, true);
    retVal.setDeprecated(false);
    return retVal;
  }

  private BinTypeRef getRefForMyType(Class param) {
    String qName = param.getName();

    if (param.isArray()) {
      BinTypeRef arrayType = getFakeOrRealTypeRef(qName);
      return getProject().createArrayTypeForType(arrayType,
          TypeUtil.getDimension(qName));
    } else {
      return getFakeOrRealTypeRef(qName);
    }
  }

  private BinTypeRef getFakeOrRealTypeRef(String qName) {
    return getFakeOrRealTypeRef(qName, false);
  }

  private BinTypeRef getFakeOrRealTypeRef(String qName, boolean expectInterface) {
    BinTypeRef result = getProject().getTypeRefForName(qName);
    if (result == null) {
      result = new BinCITypeRef(qName, getProject().getProjectLoader().getClassLoader());
      ((BinCITypeRef) result).setClazz(!expectInterface);
      getProject().addLoadedType(qName, result);
    }
    return result;
  }
}
