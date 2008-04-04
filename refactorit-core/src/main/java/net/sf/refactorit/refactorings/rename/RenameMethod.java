/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.MethodNameIndexer;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Renames method.
 */
public class RenameMethod extends RenameMember {
  public static String key = "refactoring.rename.method";
  private ManagingIndexer supervisor;

  /** Whether to rename methods renamed method overrides. */
  private boolean supertypes = true;

  /** Whether to rename methods overriding renamed method. */
  private boolean subtypes = true;

  private boolean mustCheckOverrides = true;

  public RenameMethod(final RefactorItContext context, final BinMethod method) {
    super("RenameMethod", context, method);
  }

  public RefactoringStatus checkPreconditions() {
    return super.checkPreconditions();
  }

  public void setMustCheckOverrides(boolean b) {
    this.mustCheckOverrides = b;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();
    status.merge(super.checkUserInput());

    if (!NameUtil.isValidIdentifier(getNewName())) {
      status.merge(new RefactoringStatus(
          "Not a valid Java 2 method identifier",
          RefactoringStatus.ERROR));
    }

    if (((BinMethod) getItem()).isNative()) {
      status.merge(new RefactoringStatus(
          "Can not rename native methods",
          RefactoringStatus.ERROR));
    }

    final List invocations = getSupervisor().getInvocations();
    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData invocation = (InvocationData) invocations.get(i);
      if (getNewName().equals(invocation.getWhereMember().getName())
          && (invocation.getWhereType().getBinCIType().isAnonymous()
          || invocation.getWhereType().getBinCIType().isLocal())) {
        // as described in bug #2056
        status.merge(new RefactoringStatus(
            "Can not rename method which is called in a local class "
            + "in a method with the same name",
            CollectionUtil.singletonArrayList(invocation.getWhereMember()),
            RefactoringStatus.ERROR));
      }
    }

    final BinMethod oldMethod = (BinMethod) getItem();

    final BinMethod newMethod =
        new BinMethod(getNewName(),
        oldMethod.getParameters(),
        oldMethod.getReturnType(),
        oldMethod.getModifiers(),
        oldMethod.getThrows()) {

      /**
       * Override default traverse;
       * Take no actions here because this is a *VIRTUAL* member (and has no
       * content)
       */
      public void defaultTraverse(BinItemVisitor visitor) {
      }
    };

    // Create fake owner reference
    newMethod.setOwner(oldMethod.getOwner());

    if (mustCheckOverrides) {
      status.merge(checkOverrides(oldMethod));
      if (status.isCancel()) {
        return status;
      }
    }

    // The type to check for
    final BinCIType type = oldMethod.getOwner().getBinCIType();

    List conflicts = checkTypeForMethod(type, newMethod, false, false);
    if (conflicts.size() > 0) {
      status.addEntry(
          "Method with the same signature already exists in this type",
          conflicts,
          RefactoringStatus.ERROR);
    }

    conflicts = checkTypeForMethod(type, newMethod, true, false);
    if (conflicts.size() > 0) {
      List differentReturn = findMethodsWithDifferentReturnType(conflicts,
          newMethod);
      if (differentReturn.size() == 0) {
        status.addEntry(
            "WARNING: Method with the same signature already exists in super type",
            conflicts,
            RefactoringStatus.QUESTION);
      } else {
        status.addEntry(
            "WARNING: Method with the same signature already exists in super type, but has other return type",
            differentReturn,
            RefactoringStatus.ERROR);
      }
    }

    conflicts = checkTypeForMethod(type, newMethod, false, true);
    if (conflicts.size() > 0) {
      List differentReturn = findMethodsWithDifferentReturnType(conflicts,
          newMethod);
      if (differentReturn.size() == 0) {
        status.addEntry(
            "WARNING: Method with the same signature already exists in sub type",
            conflicts,
            RefactoringStatus.QUESTION);
      } else {
        status.addEntry(
            "WARNING: Method with the same signature already exists in sub type, but has other return type",
            differentReturn,
            RefactoringStatus.ERROR);
      }
    }

    status.merge(checkResolutionConflicts(type, newMethod));

    conflicts = findStaticImportShadings(type, newMethod);
    if (conflicts.size() > 0) {
      status.addEntry("Existing methods used via static imports in this type",
          conflicts, RefactoringStatus.ERROR);
    }

    if (!newMethod.isPrivate()) {
    	conflicts = findStaticImportShadingsInSubTypes(type, newMethod);
    	if (conflicts.size() > 0) {
    		status.addEntry("Existing methods used via static imports in subtypes",
    				conflicts, RefactoringStatus.ERROR);
    	}
    }

    conflicts = findStaticImportShadingsInInnerTypes(type, newMethod);
    if (conflicts.size() > 0) {
    	status.addEntry("Existing methods used via static imports in inner types",
    			conflicts, RefactoringStatus.ERROR);
    }

    if (newMethod.isStatic() && (!newMethod.isPrivate())) {
      findStaticImportConflictsForNewMethod(newMethod, status);
    }

    return status;
  }

  /**
   * Checks all static-import usages of this method and checks if the
   * new name doesn't cause any shading conflicts
   * @param newMethod
   * @param status
   */
  private void findStaticImportConflictsForNewMethod(BinMethod newMethod, RefactoringStatus status) {
  	final List invocations = getSupervisor().getInvocations();
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		if ((invocation.getInConstruct() != null) && (invocation.getInConstruct() instanceof BinMethodInvocationExpression)) {
  			BinMethodInvocationExpression expr = (BinMethodInvocationExpression)invocation.getInConstruct();
  			if (expr.invokedViaStaticImport()) {
  				findLocalAndMemberFieldShadings(newMethod, status, expr);
  				StaticImports.SingleStaticImport singleStaticImport = expr.getCompilationUnit().getStaticImports().getSingleStaticImportFor(expr.getMethod());
  				if (singleStaticImport != null) {
  					// method is invoked via single static import
  					List singleStaticImportMethods = expr.getCompilationUnit().getStaticImports().getSingleStaticImportMethods(expr.getOwner().getBinCIType());
  					for (Iterator iterator = singleStaticImportMethods
  							.iterator(); iterator.hasNext();) {
  						BinMethod existing = (BinMethod) iterator.next();
  						if (newMethod.isApplicable(existing)) {
  							status.addEntry("Method is used via a single-static-import in class that already has a single static import of a method '" + newMethod.getName() + "' with the same signature",
  									RefactoringStatus.ERROR, existing);
  						}
  					}
  					List conflicts = findStaticImportShadings(expr.getOwner().getBinCIType(), newMethod);
  					if (conflicts.size() > 0) {
  		    		status.addEntry("Existing methods named '"+ newMethod.getName() + "' and with the same signature used via static imports in classes where this field is used via single-static import",
  		    				conflicts, RefactoringStatus.ERROR);
  		    	}


  				} else {
            // FIXME: never used - something is missing?
  					BinField conflictingStaticImport = expr.getCompilationUnit().getStaticImportField(getNewName(), expr.getOwner().getBinCIType());

  					List existingStaticImportMethods = expr.getCompilationUnit().getStaticImportMethods(expr.getOwner().getBinCIType());
  					for (Iterator iterator = existingStaticImportMethods
  							.iterator(); iterator.hasNext();) {
  						BinMethod existing = (BinMethod) iterator.next();
  						if (newMethod.isApplicable(existing)) {
  							status.addEntry("Method is used via a on-demand-import in class that already has a static import of a method '" + newMethod.getName() + "' with the same signature",
  									RefactoringStatus.ERROR, existing);
  						}
  					}
  				}
  			}
  		}
  	}
  }

  /**
   * Checks whether a static-imported method invocation would get shaded by any member
   * methods or if renamed to <code>newMethod</code>
   *
   * @param newMethod
   * @param status
   * @param expr
   */
  private void findLocalAndMemberFieldShadings(BinMethod newMethod, RefactoringStatus status, BinMethodInvocationExpression expr) {
  	BinTypeRef typeRef = expr.getOwner();
  	while (typeRef != null) {
  		BinMethod [] declaredMethods = typeRef.getBinCIType().getDeclaredMethods();
  		for (int i = 0; i < declaredMethods.length; i++) {
  			if (newMethod.isApplicable(declaredMethods[i])) {
    			status.addEntry("Method is used via a static-import in class that already has such method declared",
    					RefactoringStatus.ERROR, declaredMethods[i]);
    			return;
  			}
  		}
			final BinTypeRef[] supertypes = typeRef.getSupertypes();
			for (int i = 0, max = supertypes.length; i < max; i++) {
				BinMethod [] superMethods = supertypes[i].getBinCIType().getDeclaredMethods();
				for (int j = 0; j < superMethods.length; j++) {
					if (superMethods[j].isAccessible(expr.getOwner().getBinCIType(),  expr.getOwner().getBinCIType())
							&&	newMethod.isApplicable(superMethods[j])) {
						status.addEntry("Method is used via a static-import in class that already has such method declared in one of the supertypes",
								RefactoringStatus.ERROR, superMethods[j]);
						return;
					}
				}
			}
  		typeRef = typeRef.getBinCIType().getOwner();
  	}
  }

  private List findStaticImportShadingsInSubTypes(BinCIType type, BinMethod newMethod) {
  	final List conflicts = new ArrayList();
  	for (Iterator i = type.getTypeRef().getDirectSubclasses().iterator(); i.hasNext();) {
  		BinTypeRef subTypeRef = (BinTypeRef) i.next();
  		conflicts.addAll(findStaticImportShadings(subTypeRef.getBinCIType(), newMethod));
  	}
  	return conflicts;
  }

  private List findStaticImportShadingsInInnerTypes(BinCIType type, BinMethod newMethod) {
  	final List conflicts = new ArrayList();
  	BinTypeRef [] innerTypeRefs = type.getDeclaredTypes();
  	for (int i = 0; i < innerTypeRefs.length; i++) {
  		BinTypeRef innerTypeRef = innerTypeRefs[i];
  		conflicts.addAll(findStaticImportShadings(innerTypeRef.getBinCIType(), newMethod));
  	}
  	return conflicts;
  }

  /**
   * Finds places where method with the same name and signature are used via static import.
   * Such fields would be shaded by the method with the new name.
   */
  private List findStaticImportShadings(BinCIType type, final BinMethod newMethod) {
  	final List conflicts = new ArrayList();
  	BinItemVisitor visitor = new AbstractIndexer() {
  		public void visit(BinMethodInvocationExpression expression) {
  			if (newMethod.getName().equals(expression.getMethod().getName())
  					&&(expression.invokedViaStaticImport())
						&& (expression.getMethod().isApplicable(newMethod))) {
          conflicts.add(expression.getMethod());
        }
  		}
  	};
  	visitor.visit(type);
  	return conflicts;
  }

  private List findMethodsWithDifferentReturnType(List overridables,
      BinMethod target) {
    List result = new ArrayList();

    for (int i = 0, max = overridables.size(); i < max; i++) {
      final BinMethod overridable = (BinMethod) overridables.get(i);
      if (!target.getReturnType().equals(overridable.getReturnType())) {
        result.add(overridable);
      }
    }

    return result;
  }

  private RefactoringStatus checkResolutionConflicts(final BinCIType type,
      final BinMethod newMethod) {
    // The list of discovered bad resolvings
    List conflicts = null;

    // Do not allow other threads to access that BinCIType - we break it's
    // integrity for a moment
    // NOTE: this synchronize won't help!!! But seems nobody cares!
    synchronized (type) {

      try {
        // Append "virtual" method
        type.addDeclaredMethod(newMethod);

        // Resolve (all?) method invocations and check whether
        // the resolution has changed
        conflicts = new MethodResolutionChecker().checkInvocations(newMethod);
        if (conflicts.size() > 0) {
          return new RefactoringStatus(
              "Method resolution changes",
              conflicts,
              RefactoringStatus.ERROR);
        }

      } finally {
        // Remove "virtual" method
        type.removeDeclaredMethod(newMethod);
      }

    } // End Of sync

    return null;
  }

  public TransformationList performChange() {
    TransformationList transList = super.performChange();

    if (!transList.getStatus().isOk()) {
      return transList;
    }

    final BinMethod method = (BinMethod) getItem();

    final List invocations
        = getSupervisor().getInvocations();

    ConfirmationTreeTableModel model
        = new ConfirmationTreeTableModel(method, invocations);

// changed to generic preview
//    model = (ConfirmationTreeTableModel) DialogManager.getInstance()
//        .showConfirmations(getContext(), model, "refact.rename.method");
    if (model == null) {
      // User cancelled rename process
      transList.getStatus().addEntry("", RefactoringStatus.CANCEL);
      return transList;
    }

    final MultiValueMap usages =
        ManagingIndexer.getInvocationsMap(model.getCheckedUsages());

    // Alter sources
    for (final Iterator i = usages.entrySet().iterator(); i.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) i.next();

      transList.add(
          new RenameTransformation(
          (CompilationUnit) entry.getKey(),
          (List) entry.getValue(),
          getNewName()));
    }

    if (method.isStatic() && (!method.isPrivate())) {
    	addStaticImportChanges(transList, invocations);
    }

    return transList;
  }

  protected ManagingIndexer getSupervisor() {
    if (supervisor == null || !this.subtypes || !this.supertypes) {
      BinMethod method = (BinMethod) getItem();

      supervisor = new ManagingIndexer(true);

      new MethodNameIndexer(supervisor,
          method,
          this.subtypes,
          this.supertypes,
          this.isRenameInJavadocs()
          );

      // FIXME remove second parameter, when comments are also indexed in ASTTree
      supervisor.callVisit(method, this.isRenameInJavadocs(), false);
    }

    return supervisor;
  }

  private RefactoringStatus checkOverrides(BinMethod method) {
    final List topMethods = method.getTopMethods();
    final List subMethods
        = method.getOwner().getBinCIType().getSubMethods(method);

    this.subtypes = true;
    this.supertypes = true;

    if (topMethods.size() == 0 && subMethods.size() == 0) {
      return null;
    }

    final List notEditableMethods = new ArrayList();

//    //System.out.println( "topMethods.size() " + topMethods.size() );
//    for (int i = 0, max = topMethods.size(); i < max; i++) {
//      final BinMethod curMethod = (BinMethod) topMethods.get(i);
//      if (!isEditable(curMethod)) {
//        CollectionUtil.addNew(notEditableMethods, curMethod);
//      }
//    }
//
//    //System.out.println( "subMethods.size() " + subMethods.size() );
//    for (int i = 0, max = subMethods.size(); i < max; i++) {
//      final BinMethod curMethod = (BinMethod) subMethods.get(i);
//      if (!isEditable(curMethod)) {
//        CollectionUtil.addNew(notEditableMethods, curMethod);
//      }
//    }
    //System.out.println( "subMethods.size() " + subMethods.size() );
    List hierarchyMethods=method.findAllOverridesOverriddenInHierarchy();
    for (int i = 0, max = hierarchyMethods.size(); i < max; i++) {
      final BinMethod curMethod = (BinMethod) hierarchyMethods.get(i);
      if (!isEditable(curMethod)) {
        CollectionUtil.addNew(notEditableMethods, curMethod);
      }
    }

    if (notEditableMethods.size() == 0) {
      String text = "Method "
          + BinFormatter.formatQualified(method.getOwner())
          + "." + method.getName()
          + " overrides or overridden by the following methods:";

      List allMethods = new ArrayList(hierarchyMethods);
//      CollectionUtil.addAllNew(allMethods, subMethods);
      Collections.sort(allMethods, new BinMember.QualifiedNameSorter());

      for (int i = 0; i < allMethods.size(); i++) {
        final BinMethod xMethod = (BinMethod) allMethods.get(i);
        text += "  " + BinFormatter.formatQualified(xMethod.getOwner())
            + "." + xMethod.getName() + "\n";
      }

      text += "\n" + "Would you like to rename the whole inheritance tree?";

      final int res = DialogManager.getInstance().showYesNoCancelQuestion(
          getContext(), "question.rename.whole.tree",
          text, DialogManager.YES_BUTTON);
      if (res == DialogManager.CANCEL_BUTTON) {
        return new RefactoringStatus("", RefactoringStatus.CANCEL);
      }

      if (res == DialogManager.NO_BUTTON) {
        this.supertypes = false;
        this.subtypes = false;
      }
    } else {
      return new RefactoringStatus(
          "Overrides or overriden by methods not from the source path",
          notEditableMethods,
          RefactoringStatus.ERROR);
    }

    return null;
  }

  private boolean isEditable(BinMethod method) {
    return method.getOwner().getBinCIType().isFromCompilationUnit()
        && !method.isNative();
  }

  private List checkTypeForMethod(BinCIType type,
      BinMethod method,
      boolean supertypes,
      boolean subtypes) {

    List conflicts = new ArrayList(1);

    // Check given type
    if (!supertypes && !subtypes) {
      BinMethod[] methods = type.getDeclaredMethods();

      // Iterate through methods
      for (int pos = 0, max = methods.length; pos < max; pos++) {
        BinMethod current = methods[pos];

        if (current.sameSignature(method)) {
          CollectionUtil.addNew(conflicts, current);
        }
      }
    }

    // Check sub types
    if (subtypes) {
      for (final Iterator i = type.getTypeRef().getAllSubclasses().iterator();
          i.hasNext(); ) {
        BinCIType subtype = ((BinTypeRef) i.next()).getBinCIType();
        BinMethod[] methods = subtype.getDeclaredMethods();
        for (int pos = 0, max = methods.length; pos < max; pos++) {
          BinMethod current = methods[pos];

          if (current.sameSignature(method) && method.isAccessible(type,
              subtype)
              && !current.isAbstract()) {
            CollectionUtil.addNew(conflicts, current);
          }
        } // end for methods
      } // end for subtypes
    } // end if

    // Check super types
    if (supertypes) {
      for (final Iterator i = type.getTypeRef().getAllSupertypes().iterator();
          i.hasNext(); ) {
        BinCIType supertype = ((BinTypeRef) i.next()).getBinCIType();
        BinMethod[] methods = supertype.getDeclaredMethods();
        for (int pos = 0, max = methods.length; pos < max; pos++) {
          BinMethod current = methods[pos];

          if (current.sameSignature(method) && current.isAccessible(supertype,
              type)
              && !current.isAbstract()) {
            CollectionUtil.addNew(conflicts, current);
          }
        } // end for methods
      } // end for subtypes

    }

    return conflicts;
  }

  private static class MethodResolutionChecker extends AbstractIndexer {

    /* The method that MUST not be called */
    private BinMethod forbidden = null;

    /* The collection of BinMethodInvocations that were misresolved */
    private List conflicts = new ArrayList(16);

    public MethodResolutionChecker() {
    }

    public void visit(BinMethodInvocationExpression expression) {
      BinMethod method = expression.getMethod();

      // All methods whose name match to <CODE>forbidden.getName()</CODE> must
      // resolve back to themselves
      if (method.getName().equals(this.forbidden.getName())) {

        // Method after rename
        final BinMethod resolved;
        // HACK: Need to distinguish whether invocation was with DOT or without
        //       it.
        if (expression.getExpression() == null) {
          // Invocation without DOT
          final BinCIType type =
              MethodInvocationRules.getTypeForDotlessInvocation(
              getCurrentType().getBinCIType(),
              method.getName());

          if (type == null) {
            resolved = null;
          } else {
            resolved =
                MethodInvocationRules.getMethodDeclaration(
                getCurrentType().getBinCIType(),
                type.getTypeRef(),
                method.getName(),
                expression.getExpressionList().getExpressionTypes());
          }
        } else {
          // Invocation with DOT
          resolved =
              MethodInvocationRules.getMethodDeclaration(
              getCurrentType().getBinCIType(), expression.getInvokedOn(),
              method.getName(),
              expression.getExpressionList().getExpressionTypes());
        }

        //        System.out.println("Original: "
        //            + method.getQualifiedNameWithParamTypes() + "; Resolved: "
        //            + resolved.getQualifiedNameWithParamTypes());

        // Compare products with the original method
        if ((resolved == null) || (!method.sameSignature(resolved))) {
          CollectionUtil.addNew(this.conflicts, expression);
        }
      }

      super.visit(expression);
    }

    List checkInvocations(BinMethod forbidden) {
      this.forbidden = forbidden;

      visit(forbidden.getProject());

      //System.err.println("Conflicts: " + this.conflicts);
      return this.conflicts;
    }
  }


  protected void invalidateCache() {
    this.supervisor = null;
  }

  public void setSupertypes(final boolean supertypes) {
    this.supertypes = supertypes;
  }

  public void setSubtypes(final boolean subtypes) {
    this.subtypes = subtypes;
  }

  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
