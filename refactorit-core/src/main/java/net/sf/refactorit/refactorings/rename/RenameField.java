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
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.query.usage.FieldNameIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.MethodNameIndexer;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.PropertyNameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.GetterSetterUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Renames field.
 * @author Anton Safonov
 */
public class RenameField extends RenameMember {
  public static String key = "refactoring.rename.field";
  
  private ManagingIndexer supervisor = null;
  private boolean renameGettersAndSetters = false;

  private List renameMethodsForGetters = new ArrayList();
  private List renameMethodsForSetters = new ArrayList();

  MultiValueMap placesToQualify;

  public RenameField(final RefactorItContext context, final BinField field) {
    super("Rename Field", context, field);

    this.renameGettersAndSetters = "true".equals(
        GlobalOptions.getOption("rename.getters_and_setters", "true"));
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus result = super.checkPreconditions();

    for (int i = 0; i < renameMethodsForGetters.size(); i++) {
      result.merge(((RenameMethod) renameMethodsForGetters.get(i))
          .checkPreconditions());
    }

    for (int i = 0; i < renameMethodsForSetters.size(); i++) {
      result.merge(((RenameMethod) renameMethodsForSetters.get(i))
          .checkPreconditions());
    }

    return result;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();
    status.merge(super.checkUserInput());

    BinField field = (BinField) getItem();

    if (!NameUtil.isValidIdentifier(getNewName())) {
      status.merge(
          new RefactoringStatus("Not a valid Java 2 field identifier",
          RefactoringStatus.ERROR));
    }

    final BinCIType type = field.getOwner().getBinCIType();

    if (type.getDeclaredField(getNewName()) != null) {
      status.merge(
          new RefactoringStatus("Such field already exists in this type",
          RefactoringStatus.ERROR));
    }

    List conflicts
        = findConflictsInSupertypes(
        type.getTypeRef().getSupertypes(), getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing fields in supertypes of the owner",
          conflicts, RefactoringStatus.WARNING);
    }

    conflicts = findConflictsInSubtypes(
        type.getTypeRef().getDirectSubclasses(), getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing fields in subtypes of the owner",
          conflicts, RefactoringStatus.WARNING);
    }

    // Check the conflicts with local variables in owner, sub and super types
    conflicts = findConflictsWithLocalVariables(
        type.getTypeRef(), field, getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing local variables",
          conflicts, RefactoringStatus.WARNING);
    }

    conflicts = findConflictsWithOuterTypes(type, getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing fields in outer types",
          conflicts, RefactoringStatus.WARNING);
    }

    conflicts = findConflictsWithInnerTypes(type, getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing fields in inner types",
          conflicts, RefactoringStatus.WARNING);
    }

    for (int i = 0; i < renameMethodsForGetters.size(); i++) {
      status.merge(((RenameMethod) renameMethodsForGetters.get(i))
          .checkUserInput());
    }
    for (int i = 0; i < renameMethodsForSetters.size(); i++) {
      status.merge(((RenameMethod) renameMethodsForSetters.get(i))
          .checkUserInput());
    }

    conflicts = findStaticImportShadings(type, getNewName());
    if (conflicts.size() > 0) {
      status.addEntry("Existing fields used via static imports in this type",
          conflicts, RefactoringStatus.ERROR);
    }

    if (!field.isPrivate()) {
    	conflicts = findStaticImportShadingsInSubTypes(type, getNewName());
    	if (conflicts.size() > 0) {
    		status.addEntry("Existing fields used via static imports in subtypes",
    				conflicts, RefactoringStatus.ERROR);
    	}
    }

    conflicts = findStaticImportShadingsInInnerTypes(type, getNewName());
    if (conflicts.size() > 0) {
    	status.addEntry("Existing fields used via static imports in inner types",
    			conflicts, RefactoringStatus.ERROR);
    }


    if (field.isStatic() && (!field.isPrivate())) {
      findStaticImportConflictsForNewName(getNewName(), status);
    }
    return status;
  }


  /**
   * Checks all static-import usages of this field and checks if the
   * new name doesn't cause any shading conflicts
   * @param newName
   * @param status
   */
  private void findStaticImportConflictsForNewName(String newName, RefactoringStatus status) {
  	final List invocations = getSupervisor().getInvocations();
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		if ((invocation.getInConstruct() != null) && (invocation.getInConstruct() instanceof BinFieldInvocationExpression)) {
  			BinFieldInvocationExpression expr = (BinFieldInvocationExpression)invocation.getInConstruct();
  			if (expr.invokedViaStaticImport()) {
  				findLocalAndMemberFieldShadings(newName, status, expr.getOwner());
  				StaticImports.SingleStaticImport singleStaticImport = expr.getCompilationUnit().getStaticImports().getSingleStaticImportFor(expr.getField());
  				if (singleStaticImport != null) {
  					// field is invoked via single static import
  					BinField conflictingStaticImport = expr.getCompilationUnit().getSingleStaticImportField(getNewName(), expr.getOwner().getBinCIType());
  					if (conflictingStaticImport != null) {
  						status.addEntry("Field is used via a single-static-import in class that already has a single static import of a field '" + newName + "'",
  								RefactoringStatus.ERROR, conflictingStaticImport);
  					}
  					List conflicts = findStaticImportShadings(expr.getOwner().getBinCIType(), newName);
  					if (conflicts.size() > 0) {
  		    		status.addEntry("Existing fields named '"+ newName + "' used via static imports in classes where this field is used via single-static import",
  		    				conflicts, RefactoringStatus.ERROR);
  		    	}


  				} else {
  					BinField conflictingStaticImport = expr.getCompilationUnit().getStaticImportField(getNewName(), expr.getOwner().getBinCIType());
  					if (conflictingStaticImport != null) {
  						status.addEntry("Field is used via a static-import in class that already has a static import of a field '" + newName + "'",
  								RefactoringStatus.ERROR, conflictingStaticImport);
  					}
  				}
  			}
  		}
  	}
  }

  /**
   * Checks whether a static-imported field incocation would get shaded by any member
   * fields or local variables if renamed to <code>newName</code>
   */
  private void findLocalAndMemberFieldShadings(String newName, RefactoringStatus status, BinTypeRef typeToCheck) {
  	final Map localVarConflicts = new HashMap();
  	SinglePointVisitor visitor = createVariableCollisionFinder((BinField)getItem(), newName, localVarConflicts);
  	visitor.visit(typeToCheck.getBinCIType());
    for (Iterator iter = localVarConflicts.entrySet().iterator(); iter.hasNext();) {
    	Map.Entry entry = (Map.Entry) iter.next();
//    	BinFieldInvocationExpression x = (BinFieldInvocationExpression) entry.getKey();
    	BinVariable var = (BinVariable) entry.getValue();
    	status.addEntry("Field is used via static import in a scope where '" + newName + "' is already used as a local variable name", RefactoringStatus.ERROR, var);
    	return;
    }

  	BinTypeRef typeRef = typeToCheck;
  	while (typeRef != null) {
  		BinField shadingField = typeRef.getBinCIType().getDeclaredField(newName);
  		if ((shadingField != null) && (shadingField.isAccessible(typeRef.getBinCIType(), typeRef.getBinCIType()))) {
  			status.addEntry("Field is used via a static-import in class that already has such field declared",
  					RefactoringStatus.ERROR, shadingField);
  			return;
  		} else {
  			final BinTypeRef[] supertypes = typeRef.getSupertypes();
  			for (int i = 0, max = supertypes.length; i < max; i++) {
  				shadingField = supertypes[i].getBinCIType().getAccessibleField(newName, typeRef.getBinCIType());
  				if (shadingField != null) {
  					status.addEntry("Field is used via a static-import in class that already has such field declared in one of the supertypes",
  							RefactoringStatus.ERROR, shadingField);
  					return;
  				}
  			}
  		}
  		typeRef = typeRef.getBinCIType().getOwner();
  	}
  }

  private List findStaticImportShadingsInInnerTypes(BinCIType type, String newName) {
  	final List conflicts = new ArrayList();
  	BinTypeRef [] innerTypeRefs = type.getDeclaredTypes();
  	for (int i = 0; i < innerTypeRefs.length; i++) {
  		BinTypeRef innerTypeRef = innerTypeRefs[i];
  		conflicts.addAll(findStaticImportShadings(innerTypeRef.getBinCIType(), newName));
  	}
  	return conflicts;
  }

  private List findStaticImportShadingsInSubTypes(BinCIType type, String newName) {
  	final List conflicts = new ArrayList();
  	for (Iterator i = type.getTypeRef().getDirectSubclasses().iterator(); i.hasNext();) {
  		BinTypeRef subTypeRef = (BinTypeRef) i.next();
  		conflicts.addAll(findStaticImportShadings(subTypeRef.getBinCIType(), newName));
  	}
  	return conflicts;
  }

  /**
   * Finds places where fields with the same name are used via static import.
   * Such fields would be shaded by the field with the new  name.
   *
   * @param type
   * @param newName
   * @return
   */
  private List findStaticImportShadings(BinCIType type, final String newName) {
  	final List conflicts = new ArrayList();
  	BinItemVisitor visitor = new AbstractIndexer() {
  		public void visit(BinFieldInvocationExpression expression) {
        if (newName.equals(expression.getField().getName()) && (expression.invokedViaStaticImport())) {
        	// FIXME: wouldn't it be better to put expression into conflicts? But it shows ugly in the conflicts window...
          conflicts.add(expression.getField());
        }
  		}
  	};
  	visitor.visit(type);
  	return conflicts;
  }

  public void setRenameGettersAndSetters(boolean b) {
    if (this.renameGettersAndSetters != b) {
      invalidateCache();
    }
    this.renameGettersAndSetters = b;
  }

  public TransformationList performChange() {
    TransformationList transList = super.performChange();

    if (!transList.getStatus().isOk()) {
      return transList;
    }

    final BinField field = (BinField) getItem();

    // Show dialog where user can see and confirm usages to be renamed
    final List invocations = getSupervisor().getInvocations();

    ConfirmationTreeTableModel model = new ConfirmationTreeTableModel(field,
        invocations);
// changed to generic preview
//    model = (ConfirmationTreeTableModel)
//        DialogManager.getInstance().showConfirmations(
//        getContext(), model, "refact.rename.field");

    if (model == null) {
      transList.getStatus().merge(
          new RefactoringStatus("", RefactoringStatus.CANCEL));
      return transList;
    }

    final MultiValueMap usages
        = ManagingIndexer.getInvocationsMap(model.getCheckedUsages());

    for (final Iterator i = usages.entrySet().iterator(); i.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) i.next();

      addRenameEditorsForCompilationUnit(
          (CompilationUnit) entry.getKey(), transList, (List) entry.getValue());
    }

    addQualifierEditors(transList);

    //addStaticImportEditors(transList, usages);
    if (field.isStatic() && (!field.isPrivate())) {
    	addStaticImportChanges(transList, invocations);
    }

    return transList;
  }



  private void addRenameEditorsForCompilationUnit(final CompilationUnit compilationUnit,
      final TransformationList transList, final List allAsts) {
    if (this.renameGettersAndSetters) {
      List getterAsts = new ArrayList();
      List setterAsts = new ArrayList();
      List usageAsts = new ArrayList();

      divideAstsIntoGroups(allAsts, getterAsts, setterAsts, usageAsts);
      BinMethod method = GetterSetterUtils.getGetterMethodFor((BinField) getItem());
      transList.add(
          new RenameTransformation(compilationUnit, usageAsts, getNewName()));

      transList.add(
          new RenameTransformation(compilationUnit, getterAsts, getNewGetterName(method)));

      transList.add(
          new RenameTransformation(compilationUnit, setterAsts, getNewSetterName()));
    } else {
      transList.add(
          new RenameTransformation(compilationUnit, allAsts, getNewName()));
    }
  }

  private String getNewSetterName() {
    if (getNewName() != null) {
      return PropertyNameUtil.getDefaultSetterName(getNewName());
    } else {
      return null;
    }
  }

//  private String getNewGetterName() {
//    if (getNewName() != null) {
//      return PropertyNameUtil.getDefaultGetterName(getNewName(),
//          ((BinField) getItem()).getTypeRef().getBinType()
//          == BinPrimitiveType.BOOLEAN)[0];
//    } else {
//      return null;
//    }
//  }

  private String getNewGetterName(BinMethod method) {
    if (getNewName() != null && method != null) {
//      String possibleGetters[] = PropertyNameUtil.getDefaultGetterName(getNewName(),
//          ((BinField) getItem()).getTypeRef().getBinType()
//          == BinPrimitiveType.BOOLEAN);
//    for(int i = 0; i < possibleGetters.length; i++) {
      String methodName = method.getName();

      if(((BinField) getItem()).getTypeRef().getBinType() == BinPrimitiveType.BOOLEAN) {
        if(methodName.startsWith("is")) {
          return "is" + PropertyNameUtil.getPropertyName(getNewName());
        }
      }

      if(methodName.startsWith("get")) {
        return "get" + PropertyNameUtil.getPropertyName(getNewName());
      }
    }

    return null;
  }

  private void divideAstsIntoGroups(final List astsToSort,
      final List getterAsts,
      final List setterAsts,
      final List fieldAsts) {
    String getterName =
        GetterSetterUtils.getGetterMethodFor((BinField) getItem()) != null ?
        GetterSetterUtils.getGetterMethodFor((BinField) getItem()).getName() :
        null;
    String setterName =
        GetterSetterUtils.getSetterMethodFor((BinField) getItem()) != null ?
        GetterSetterUtils.getSetterMethodFor((BinField) getItem()).getName() :
        null;

    for (int ii = 0; ii < astsToSort.size(); ii++) {
      ASTImpl ast = (ASTImpl) astsToSort.get(ii);
      if (ast.getText().equals(getterName)) {
        getterAsts.add(ast);
      } else if (ast.getText().equals(setterName)) {
        setterAsts.add(ast);
      } else {
        fieldAsts.add(ast);
      }
    }
  }

  protected ManagingIndexer getSupervisor() {
    BinField field = (BinField) getItem();

    if (supervisor == null) {
      supervisor = new ManagingIndexer(true);

      new FieldNameIndexer(supervisor, field, isRenameInJavadocs());

      if (this.renameGettersAndSetters) {
        if (GetterSetterUtils.getGetterMethodFor(field) != null) {
          BinMethod method = GetterSetterUtils.getGetterMethodFor(field);
          addMethodSignatureIndexerForEntireInheritanceHierarchy(
              method, getNewGetterName(method),
              renameMethodsForGetters);
        }
        if (GetterSetterUtils.getSetterMethodFor(field) != null) {
          addMethodSignatureIndexerForEntireInheritanceHierarchy(
              GetterSetterUtils.getSetterMethodFor(field), getNewSetterName(),
              renameMethodsForSetters);
        }
      }

      // Find usages
      if (field.isPrivate() && (!this.renameGettersAndSetters
          || (GetterSetterUtils.getGetterMethodFor(field) == null
          && GetterSetterUtils.getSetterMethodFor(field) == null))) {
        supervisor.visit(field.getCompilationUnit());
      } else {
        supervisor.visit(field.getOwner().getProject());
      }
    }

    return supervisor;
  }

  /**
   * RenameMethod is used for finding conflicts only, not for renaming.
   * Reason: could not quickly get RenameMethod working well for some reason.
   */
  private void addMethodSignatureIndexerForEntireInheritanceHierarchy(
      final BinMethod method, String newName, List renameMethods) {

    renameMethods.add(createRenameMethod(method, newName));

    if(isHierarchyRenameAllowed(method)) {
      new MethodNameIndexer(supervisor, method, true, true, isRenameInJavadocs());
    }
  }
  
  private boolean isHierarchyRenameAllowed(BinMethod method) {
    List hierarchy = method.findAllOverrides();
    for(int i = 0, max = hierarchy.size(); i < max; i++) {
      BinMethod tmpMeth = (BinMethod)hierarchy.get(i);
      if(tmpMeth.getCompilationUnit() == null) {
        return false;
      }
    }
    return true;
  }

  public void setNewName(String name) {
    super.setNewName(name);

    for (int i = 0; i < renameMethodsForGetters.size(); i++) {
      RenameMethod rename = (RenameMethod) renameMethodsForGetters.get(i);
      rename.setNewName(getNewGetterName((BinMethod)rename.getItem()));
    }
    for (int i = 0; i < renameMethodsForSetters.size(); i++) {
      ((RenameMethod) renameMethodsForSetters.get(i))
          .setNewName(getNewSetterName());
    }
  }

  private RenameMethod createRenameMethod(BinMethod method, String newName) {
    RenameMethod result = new RenameMethod(getContext(), method);
    result.setNewName(newName);
    result.setMustCheckOverrides(false);

    return result;
  }

  public static List findConflictsWithInnerTypes(BinCIType type, String newName) {
    List conflicts = new ArrayList();

    BinTypeRef[] inners = type.getDeclaredTypes();
    for (int i = 0; i < inners.length; ++i) {
      BinCIType inner = inners[i].getBinCIType();
      final BinField field = inner.getDeclaredField(newName);
      if (field != null) {
        CollectionUtil.addNew(conflicts, field);
      }
    }

    return conflicts;
  }

  public static List findConflictsWithOuterTypes(BinCIType type,
      String newName) {
    List conflicts = new ArrayList();

    if (type.getOwner() == null) {
      return conflicts;
    }

    BinCIType outer = type.getOwner().getBinCIType();

    final BinField field = outer.getDeclaredField(newName);
    if (field != null) {
      CollectionUtil.addNew(conflicts, field);
    }

    return conflicts;
  }

  public static List findConflictsInSupertypes(BinTypeRef[] types, String newName) {
    final List conflicts = new ArrayList();

    for (int i = 0, max = types.length; i < max; i++) {
      final BinTypeRef type = types[i];
      final BinField field = type.getBinCIType().getDeclaredField(newName);
      if (field != null) {
        CollectionUtil.addNew(conflicts, field);
      }

      conflicts.addAll(
          findConflictsInSupertypes(type.getSupertypes(), newName));
    }

    return conflicts;
  }

  public static List findConflictsInSubtypes(Collection types, String newName) {
    List conflicts = new ArrayList();

    Iterator it = types.iterator();
    while (it.hasNext()) {
      final BinTypeRef type = (BinTypeRef) it.next();
      BinField field = type.getBinCIType().getDeclaredField(newName);
      if (field != null) {
        CollectionUtil.addNew(conflicts, field);
      }

      conflicts.addAll(findConflictsInSubtypes(
          type.getDirectSubclasses(), newName));
    }

    return conflicts;
  }

  private List findConflictsWithLocalVariables(BinTypeRef typeRef,
      final BinField field,
      final String newName) {
    final List conflicts = new ArrayList();
    placesToQualify = new MultiValueMap();

    final Map conflictingExpressions = new HashMap();
    BinItemVisitor visitor = createVariableCollisionFinder(field, newName, conflictingExpressions);

    typeRef.getBinCIType().accept(visitor);

    if (!field.isPrivate()) {
      Iterator subs = typeRef.getDirectSubclasses().iterator();
      while (subs.hasNext()) {
        ((BinTypeRef) subs.next()).getBinCIType().accept(visitor);
      }
    }

    for (Iterator iter = conflictingExpressions.entrySet().iterator(); iter.hasNext();) {
    	Map.Entry entry = (Map.Entry) iter.next();

    	BinFieldInvocationExpression x = (BinFieldInvocationExpression) entry.getKey();
    	BinVariable var = (BinVariable) entry.getValue();
    	CollectionUtil.addNew(conflicts, var);
    	ASTImpl place = new SimpleASTImpl();
    	place.setStartLine(x.getStartLine());
    	place.setStartColumn(x.getStartColumn());
    	placesToQualify.putNew(x.getCompilationUnit(), place);
    }

    return conflicts;
  }

  /**
   * @param field
   * @param newName
   * @param conflictingExpressions
   * @return
   */
  private SinglePointVisitor createVariableCollisionFinder(final BinField field, final String newName, final Map conflictingExpressions) {
  	return new SinglePointVisitor() {
  		private FastStack scopedVars = new FastStack();

      private void startScope() {
        HashSet newVars = new HashSet();
        if (!scopedVars.empty()) {
          newVars.addAll((Collection) scopedVars.peek());
        }
        scopedVars.push(newVars);
      }

      private void endScope() {
        if (!scopedVars.empty()) {
          scopedVars.pop();
        }
      }

      public void onEnter(Object object) {
        if (object instanceof Scope) {
          startScope();
        }
      }

      public void onLeave(Object object) {
        if (object instanceof Scope) {
          endScope();
        }
      }

      private void declareVar(BinVariable var) {
        ((HashSet) scopedVars.peek()).add(var);
      }

      public void visit(BinFieldInvocationExpression x) {
        if (x.getField() == field) {
          if (x.getExpression() == null) { // no DOT infront, i.e. implicit this
            HashSet declaredVars = (HashSet) scopedVars.peek();
            Iterator it = declaredVars.iterator();
            while (it.hasNext()) {
              BinVariable var = (BinVariable) it.next();
              if (var == field) {
                continue;
              }

              if (newName.equals(var.getName())) {
              	conflictingExpressions.put(x, var);
              }
            }
          }
        }
        super.visit(x);
      }

      public void visit(BinLocalVariable x) {
        // no reason to store all variables
        if (x.getName() != null && x.getName().equals(newName)) {
          declareVar(x);
        }

        super.visit(x);
      }
    };
}

  protected void invalidateCache() {
  	this.supervisor = null;
  	this.renameMethodsForGetters.clear();
    this.renameMethodsForSetters.clear();
  }

  private void addQualifierEditors(final TransformationList transList) {
    Iterator entries = this.placesToQualify.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      List places = (List) entry.getValue();
      for (int i = 0, max = places.size(); i < max; i++) {
        ASTImpl place = (ASTImpl) places.get(i);
        transList.add(new StringInserter(
            (CompilationUnit) entry.getKey(),
            place.getStartLine(), place.getStartColumn() - 1, "this."));

      }
    }
  }

  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }
}
