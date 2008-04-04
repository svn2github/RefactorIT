/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.NotImplementedBodySkeleton;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class OverrideMethodsRefactoring extends CreateClassMethodsRefactoring {
  public static String key = "refactoring.overridemethod";

  private BinClass target;

  private OverrideMethodsModel model;

  private Collection newMethods;

  public BinCIType getTarget() {
    return target;
  }

  public Collection getMethodsToCreate() {
    return newMethods;
  }

  public RefactoringStatus checkPreconditions() {
    if (!target.isFromCompilationUnit()) {
      return new RefactoringStatus("Type " + target.getName()
          + " is not in sourcepath", RefactoringStatus.ERROR);
    }
    return new RefactoringStatus();
  }

  public RefactoringStatus checkUserInput() {
    Collection selected = model.getSelectedList();

    newMethods = new HashSet(selected.size(), 1.1f);

    for (Iterator iter = selected.iterator(); iter.hasNext(); ) {
      BinMethod item = (BinMethod) iter.next();

      newMethods.add(createMethodSkeleton(item));
    }

    return new RefactoringStatus();
  }

  public OverrideMethodsRefactoring(
      RefactorItContext context, BinClass binClass
  ) {
    super("Override methods", context);

    this.target = binClass;

    Assert.must(binClass != null);

    model = new OverrideMethodsModel(target);
  }

  private MethodSkeleton createMethodSkeleton(BinMethod item) {

    MethodSkeleton method = new MethodSkeleton(target, item);

    MethodBodySkeleton body = null;

    if (item.isAbstract()) {
      body = new NotImplementedBodySkeleton(item.getName());
    } else {
      String bodyStr = "// @todo: override this " + FormatSettings.LINEBREAK
          + MethodBodySkeleton.createSuperCallStr(method);

      body = new MethodBodySkeleton(bodyStr);
    }

    method.setBody(body);

    return method;
  }

  public OverrideMethodsModel getModel() {
    return model;
  }

  public void setModel(OverrideMethodsModel model) {
    this.model = model;
  }

  public static BinMethod[] getOverridableMethods(BinCIType owner) {

    List supers = new ArrayList(owner.getTypeRef().getAllSupertypes());

    // sort by super classes from down to top , can be important, retain first overriddes
    Collections.sort(supers, new TypeRefHierarchyComparator());

    Comparator methodComparator = new Comparator() {
      public int compare(Object obj1, Object obj2) {
        if (((BinMethod) obj1).sameSignature((BinMethod) obj2)) {
          return 0;
        }
        return -1;
      }
    };

    List result = new ArrayList();

    List processedMethods = new ArrayList(50);

    for (Iterator iter = supers.iterator(); iter.hasNext(); ) {
      BinTypeRef item = (BinTypeRef) iter.next();

      BinMethod methods[] = item.getBinCIType().getDeclaredMethods();

      for (int i = 0; i < methods.length; i++) {

        if (!CollectionUtil.contains(processedMethods, methods[i], methodComparator)) {
          // add only one method with same signature
          processedMethods.add(methods[i]);

          if (methodIsOverridableIn(owner, methods[i])) {
            result.add(methods[i]);
          }
        }
      }
    }

//    BinMethod[] methods = owner.getAccessibleMethods(owner);
//
//    List result=new ArrayList( methods.length ) ;
//
//    for (int i = 0; i < methods.length; i++) {
//      BinMethod method = methods[i];
//
//      if (method.isFinal() || method.isStatic() ||
//          method.getOwner().equals(owner.getTypeRef()) ) {
//        continue;
//      }
//
//      if ( method.isClonedForInterfaceInheritance() ) {
//        System.out.println(method.overridesList());
//      }
//      if (BinModifier.hasFlag(method.getModifiers(), BinModifier.PRIVATE)) {
//        continue;
//      }
//      result.add(methods[i]);
//    }

    return (BinMethod[]) result.toArray(BinMethod.NO_METHODS);
  }

  private static boolean methodIsOverridableIn(BinCIType owner,
      BinMethod method) {

    return!method.isFinal() && !method.isStatic() &&
        !BinModifier.hasFlag(method.getAccessModifier(), BinModifier.PRIVATE) &&
        owner.hasMemberWithSignature(method) == null;

//          method.getOwner().equals(owner.getTypeRef()) ) {
//        continue;
//      }
  }


  public String getDescription() {
    String out = "Override/Implement methods";

     List methods = model.getSelectedList();

     for(int i=0; i < methods.size(); i++) {
       BinMethod item = (BinMethod) methods.get(i);
       out += " " + item.getName() + "(),";
     }
     out = out.substring(0, out.length()-1);


    return out + ".";//super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
