/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;

import java.util.List;


/**
 * A simplified implementation:
 *
 *   - Searches for EJB things by name conventions, not via deployment
 *     descriptors, as it should.
 *
 *   - Does not care that some beans cannot have some methods --
 *     session beans can't have select methods, neither can message
 *     beans, etc, but for the sake of simplicity, this is not taken
 *     into account -- usually people do not have such methods anyway
 *     (however, they might).
 */
public class EjbUtil {

  private static String[] methodNamePrefixes = {"ejbFind", "ejbSelect",
      "ejbCreate", "ejbRemove", "ejbActivate", "ejbPassivate",
      "ejbPostCreate", "ejbHome"};
  
  private static String[] beanNames = {"javax.ejb.EntityBean",
    "javax.ejb.SessionBean", "javax.ejb.MessageDrivenBean", 
    "javax.ejb.EnterpriseBean"};

  public static boolean isEjbMethod(BinMethod method) {
    if (!isEnterpriseBean(method.getOwner())) {
      return false;
    }

    return
        hasEjbNamePrefix(method) ||
        isBussinessMethod(method) ||
        isVirtualFieldMethod(method);
  }

  private static boolean isVirtualFieldMethod(BinMethod method) {
    return
        BinModifier.hasFlag(method.getModifiers(), BinModifier.ABSTRACT) &&
        (method.getName().startsWith("get")
        || method.getName().startsWith("set"));
  }

  private static boolean isBussinessMethod(BinMethod method) {
    return
        hasSameMethod(getRemoteInterface(method.getOwner()), method) ||
        hasSameMethod(getLocalInterface(method.getOwner()), method);
  }

  private static boolean hasSameMethod(BinTypeRef typeRef, BinMethod method) {
    if (typeRef != null) {
      return typeRef.getBinCIType().hasMemberWithSignature(method) != null;
    } else {
      return false;
    }
  }

  private static boolean hasEjbNamePrefix(BinMethod method) {
    for (int i = 0; i < methodNamePrefixes.length; i++) {
      if (method.getName().startsWith(methodNamePrefixes[i])) {
        return true;
      }
    }

    return false;
  }

  public static BinTypeRef getRemoteInterface(BinTypeRef bean) {
    BinTypeRef result = bean.getProject().getTypeRefForName(
        getBaseName(bean));
    if (result == null) {
      result = bean.getProject().getTypeRefForName(getBaseName(
          bean) + "Remote");
    }

    return result;
  }

  public static BinTypeRef getLocalInterface(BinTypeRef bean) {
    return bean.getProject().getTypeRefForName(
        getBaseName(bean) + "Local");
  }

  private static String getBaseName(BinTypeRef bean) {
    String beanName = bean.getQualifiedName();
    return beanName.substring(0, beanName.length() - "Bean".length());
  }

  public static final boolean isEnterpriseBean(final BinTypeRef ref) {
    
    Project p = ref.getProject();
    
    for(int i = 0; i < beanNames.length; i++) {
      BinTypeRef intface = p.getTypeRefForName(beanNames[i]);
      if(intface != null && ref.isDerivedFrom(intface)) {
        return true;
      }
    }
    return false;
    /*final String[] interfaces = ref.getInterfaceQualifiedNames();
    for (int i = 0; i < interfaces.length; i++) {
      if ("javax.ejb.EntityBean".equals(interfaces[i])
          || "javax.ejb.SessionBean".equals(interfaces[i])
          || "javax.ejb.MessageDrivenBean".equals(interfaces[i])
          || "javax.ejb.EnterpriseBean".equals(interfaces[i])) {
        return true;
      }
    }

    return false;*/
  }

  public static final boolean isServlet(final BinTypeRef ref) {
    Project p = ref.getProject();
    BinTypeRef servlet = p.getTypeRefForName("javax.servlet.Servlet");
    if(servlet != null && ref.isDerivedFrom(servlet)) {
      return true;
    }
    return false;
    
    /*final String superClass = ref.getSuperclassQualifiedName();
    if ("javax.servlet.http.HttpServlet".equals(superClass)
        || "javax.servlet.GenericServlet".equals(superClass)) {
      return true;
    }

    final String[] interfaces = ref.getInterfaceQualifiedNames();
    for (int i = 0; i < interfaces.length; i++) {
      if ("javax.servlet.Servlet".equals(interfaces[i])) {
        return true;
      }
    }
    return false;*/
  }

  public static final boolean isServletMethod(final BinMethod method) {
    if (!isServlet(method.getOwner())) {
      return false;
    }

    final List overrides = method.findOverrides();
    for (int i = 0, max = overrides.size(); i < max; i++) {
      final BinMethod overriden = (BinMethod) overrides.get(i);
      if(isServlet(overriden.getOwner())) {
        return true;
      }
      /*final String ownerName = overriden.getOwner().getQualifiedName();
      if ("javax.servlet.http.HttpServlet".equals(ownerName)
          || "javax.servlet.GenericServlet".equals(ownerName)
          || "javax.servlet.Servlet".equals(ownerName)) {
        return true;
      }*/
    }

    return false;
  }
}
