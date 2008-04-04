/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.refactorings.conflicts.ImportNotPossibleConflict;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ImportManager {
  class ImportCollector {
    HashMap importsForMembers = new HashMap();
    List extraImports = new ArrayList();

    public void clear() {
      this.extraImports.clear();
      this.importsForMembers.clear();
    }
  }


  class ImportChecker {
    private List importsForMember = new ArrayList();
    private List importConflicts = new ArrayList();
    private List checkedTypes = new ArrayList();

    private final BinMember member;
    private BinCIType targetType;
    private boolean isCheckOnlySignature;

    ImportChecker(BinMember member,
        BinCIType targetType,
        boolean isCheckOnlySignature) {
      this.member = member;
      this.targetType = targetType;
      this.isCheckOnlySignature = isCheckOnlySignature;

      getCollector(targetType.getTypeRef()).importsForMembers.put(member,
          importsForMember);
    }

    public List findImportsAndConflicts() {
      if (isCheckOnlySignature) {
        if (member instanceof BinMethod) {
          BinMethod method = (BinMethod) member;
          checkImport(method.getReturnType());

          BinParameter[] params = method.getParameters();
          for (int i = 0; i < params.length; i++) {
            checkImport(params[i].getTypeRef());
          }

          BinMethod.Throws[] allThrows = method.getThrows();
          for (int i = 0; i < allThrows.length; i++) {
            checkImport(allThrows[i].getException());
          }
        } else if (member instanceof BinVariable) {
          BinVariable variable = (BinVariable) member;
          checkImport(variable.getTypeRef());
        }
      } else {
        SinglePointVisitor visitor = new SinglePointVisitor() {
          TypeRefVisitor typeRefVisitor = new TypeRefVisitor();

          class TypeRefVisitor extends BinTypeRefVisitor {
            TypeRefVisitor() {
              setCheckTypeSelfDeclaration(false);
              setIncludeNewExpressions(true);
            }

            public void visit(BinTypeRef typeRef) {
              ASTImpl ast = typeRef.getNode();
              if (ast != null
                  && ast.getType() == JavaTokenTypes.IDENT
                  && ast.getFirstChild() == null
                  && ast.getParent().getFirstChild() == ast
                  || ast.getParent().getFirstChild() != ast
                  && ast.getParent().getType() == JavaTokenTypes.LITERAL_throws) {

                BinTypeRef called = typeRef.getTypeRef();
                if (needToCheckImport(called.getBinCIType())) {
                  checkImport(called);
                }
              }

              super.visit(typeRef);
            }

            private boolean needToCheckImport(final BinCIType called) {
              return !(called.isInnerType() && !called.isLocal())
                 && !(called.isLocal() && member.contains(called));
            }
          }

          public void onEnter(Object o) {
            if (o instanceof BinParameter
                && ImportManager.this.paramsToSkip.contains(o)) {
              return;
            }

            if (o instanceof BinTypeRefManager) {
              ((BinTypeRefManager) o).accept(typeRefVisitor);
            }
          }

          public void onLeave(Object o) {
          }
        };

        member.accept(visitor);
      }

      return importConflicts;
    }

    void checkImport(BinTypeRef typeToCheckRef) {
      typeToCheckRef = typeToCheckRef.getNonArrayType();
      if (typeToCheckRef.isPrimitiveType()) {
        return;
      }

      if (checkedTypes.contains(typeToCheckRef)) {
        return;
      }

      checkedTypes.add(typeToCheckRef);

      if (isAmbiguousImportFound(member, typeToCheckRef.getBinCIType(),
          targetType)) {

        importConflicts.add(new ImportNotPossibleConflict(member, targetType,
            typeToCheckRef));
        return;
      }

      if (!ImportUtils.needsTypeImported(targetType.getCompilationUnit(),
          typeToCheckRef.getBinCIType(), typeToCheckRef.getPackage())) {
        return;
      }

      Conflict importConflict = checkIfImportPossible(
          member, typeToCheckRef, targetType.getTypeRef());

      if (importConflict == null) {
        CollectionUtil.addNew(importsForMember, typeToCheckRef);
      } else {
        importConflicts.add(importConflict);
      }
    }
  }


  private HashMap collectedImports = new HashMap();
  List paramsToSkip = new ArrayList();
  private List ambiguousImports = new ArrayList();

  public ImportManager() {
  }

  ImportCollector getCollector(BinTypeRef type) {
    ImportCollector collector = (ImportCollector)this.collectedImports.get(type);
    if (collector == null) {
      collector = new ImportCollector();
      this.collectedImports.put(type, collector);
    }

    return collector;
  }

  public void setParamsToSkip(List paramsToSkip) {
    this.paramsToSkip = paramsToSkip;
  }

  /**
   * Copy-paste from CreateMissingMethod
   * Adds info about the needed imports for the new method to the ImportManager.
   *
   * @param member the new method
   * @return a <code>Set</code> of typeRefs that can not be imported and for which
   * fully qualified name should be used
   */
  public Set manageImports(BinMember member) {
    List conflicts = addImportsForMember(member, member.getOwner(), true);
    Set fqnImports = new HashSet();
    if ((conflicts != null) && (conflicts.size() > 0)) {
      for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
        fqnImports.add(((ImportNotPossibleConflict) i.next()).getTypeToImport());
      }
    }

    return fqnImports;
  }

  public List addImportsForMember(final BinMember member,
      final BinTypeRef targetRef) {
    return addImportsForMember(member, targetRef, false);
  }

  public List addImportsForMember(final BinMember member,
      final BinTypeRef targetRef,
      final boolean checkOnlySignature) {
    final ImportChecker importChecker = new ImportChecker(member,
        targetRef.getBinCIType(), checkOnlySignature);
//System.err.println("addImportsForMember: "
//    + member + ", " + targetRef);
    return importChecker.findImportsAndConflicts();
  }

  private static BinTypeRef extractImportableType(BinTypeRef typeToImport){
    if (typeToImport == null){
      return null;
    } else {
      typeToImport = typeToImport.getNonArrayType(); // Object[] => Object
      if (typeToImport.isPrimitiveType()){
        return null;
      } else if (typeToImport.getBinCIType().isAnonymous()) {
        return typeToImport.getSupertypes()[0];
      }
      return typeToImport;
    }
  }

  /**
   *
   * @param typeToImport
   * @param typeIntoImport
   * @throws AmbiguousImportImportException when such import would cause import ambiguity and is
   * thus not possible
   */
  public void addExtraImports(BinTypeRef typeToImport, BinTypeRef typeIntoImport) throws AmbiguousImportImportException {
    typeToImport = extractImportableType(typeToImport);
    if (typeToImport == null){
  		return;
  	}

    if (!typeToImport.getBinCIType().isLocal() // FIXME ImportUtils allow to import locals :(((
        && ImportUtils.needsTypeImported(
            typeIntoImport.getBinCIType().getCompilationUnit(),
            typeToImport.getBinCIType(), typeToImport.getBinType().getPackage())) {
      if (ImportUtils.isAmbiguousImport(typeToImport.getBinCIType(), typeIntoImport.getBinCIType())) {
        throw new AmbiguousImportImportException("Cannot add ambiguous import!", typeToImport);
      } else {
        List extraImports = getCollector(typeIntoImport).extraImports;
        CollectionUtil.addNew(extraImports, typeToImport);
      }
    }
  }

  public Conflict addExtraImport(BinMember member, BinTypeRef typeToImport,
      BinTypeRef targetType) {
    Conflict conflict = null;

    if (!isAmbiguousImportFound(member, typeToImport.getBinCIType(),
        targetType.getBinCIType())) {
      if (ImportUtils.needsTypeImported(
          targetType.getBinCIType().getCompilationUnit(),
          typeToImport.getBinCIType(), typeToImport.getPackage())) {
        conflict = checkIfImportPossible(member, typeToImport, targetType);
        if (conflict == null) {
          CollectionUtil.addNew(getCollector(targetType).extraImports, typeToImport);
        }
      }
    }

    return conflict;
  }

  public void removeExtraImport(BinTypeRef targetType,
      BinTypeRef typeToImport) {
    ImportCollector collector = getCollector(targetType);
    if (collector != null) {
      collector.extraImports.remove(typeToImport);
      collector.extraImports.remove(typeToImport.getQualifiedName());
    }
  }

  public List getExtraImportsFor(BinTypeRef type) {
    ImportCollector collector = getCollector(type);
    if (collector != null) {
      return collector.extraImports;
    }

    return null;
  }

  Conflict checkIfImportPossible(
      BinMember member, BinTypeRef typeToImport, BinTypeRef targetType
      ) {
//    System.out.println("typeToImport:" + typeToImport); //innnnn
//    System.out.println("targetType:" + targetType); //innnnn
//    System.out.println("isAccessible:" + typeToImport.getBinCIType().isAccessible(targetType.getBinCIType())); //innnnn
    if (!typeToImport.getBinCIType().isAccessible(targetType.getBinCIType()) ||
        isSameNameDifferentPackage(targetType, typeToImport)) {
      return new ImportNotPossibleConflict(member,
          targetType.getBinCIType(),
          typeToImport);
    }

    return null;
  }

  boolean isAmbiguousImportFound(
      BinMember member, BinCIType toImport, BinCIType importInto
      ) {
    if (ImportUtils.isAmbiguousImport(toImport, importInto)) {
      ManagingIndexer supervisor = new ManagingIndexer(true);

      new TypeNameIndexer(supervisor, toImport, false);
      member.accept(supervisor);

      ambiguousImports.addAll(supervisor.getInvocations());

      return true;
    }

    return false;
  }

  public boolean isSameNameDifferentPackage(BinTypeRef targetType,
      BinTypeRef typeToImport) {
    CompilationUnit source = targetType.getBinCIType().getCompilationUnit();

    List importedTypeNames = source.getImportedTypeNames();
    if (importedTypeNames != null) {
      for (int i = 0, max = importedTypeNames.size(); i < max; i++) {
        final String fqn = (String) importedTypeNames.get(i);
//        String packageName = fqn.substring(0, fqn.lastIndexOf('.'));
        String typeName = fqn.substring(fqn.lastIndexOf('.') + 1, fqn.length());

        if (typeName.equals(typeToImport.getName())) {
          return true;
        }
      }
    }

    return false;
  }

  public void clear() {
    this.collectedImports.clear();
    this.paramsToSkip.clear();
  }

  // adds ALL imports. If possible, use
  // createEditors(final TransformationList transList, List toMove) method
  public void createEditors(final TransformationList transList) {
    Iterator types = collectedImports.keySet().iterator();

    while (types.hasNext()) {
      BinTypeRef targetType = (BinTypeRef) types.next();

      List imports = getAllImports(targetType);
      Collections.sort(imports, BinTypeRef.QualifiedNameSorter.getInstance());
      CompilationUnit compilationUnit = targetType.getBinCIType().getCompilationUnit();

      ImportUtils.ImportPosition importPosition
          = ImportUtils.calculateNewImportPosition(compilationUnit, false);

      for (int i = 0, max = imports.size(); i < max; i++) {
        String importClause;
        importClause = ImportUtils.generateImportClause(
            ((BinTypeRef) imports.get(i)).getQualifiedName()).toString();

        insertImportClauseToSource(transList, compilationUnit, importPosition,
            imports, i, importClause);
      }
    }

    processAmbiguousImports(transList);
  }

  // adds only imports necessary for members that are in toMove list
  public void createEditors(final TransformationList transList, List toMove) {
    Iterator types = collectedImports.keySet().iterator();

    while (types.hasNext()) {
      BinTypeRef targetType = (BinTypeRef) types.next();

      List imports = getAllImports(targetType, toMove);
      Collections.sort(imports, BinTypeRef.QualifiedNameSorter.getInstance());
      CompilationUnit compilationUnit = targetType.getBinCIType().getCompilationUnit();

      ImportUtils.ImportPosition importPosition
          = ImportUtils.calculateNewImportPosition(compilationUnit, false);

      for (int i = 0, max = imports.size(); i < max; i++) {
        String importClause;
        importClause = ImportUtils.generateImportClause(
            ((BinTypeRef) imports.get(i)).getQualifiedName()).toString();

        insertImportClauseToSource(transList, compilationUnit, importPosition,
            imports, i, importClause);
      }
    }

    processAmbiguousImports(transList);
  }

  private void processAmbiguousImports(final TransformationList transList) {
    Set processed = new HashSet();
    for (int i = 0, max = ambiguousImports.size(); i < max; i++) {
      InvocationData data = (InvocationData) ambiguousImports.get(i);
      if (processed.contains(data)) {
        continue;
      } else {
        processed.add(data);
      }
      BinType type = (BinType) data.getWhat();
      transList.add(new StringEraser(data.getWhereMember().getCompilationUnit(),
          data.getWhereAst().getStartLine(),
          data.getWhereAst().getStartColumn() - 1,
          type.getName().length()));
      transList.add(new StringInserter(data.getWhereMember().getCompilationUnit(),
          data.getWhereAst().getStartLine(),
          data.getWhereAst().getStartColumn() - 1,
          type.getQualifiedName()));
    }
  }

  // gets ALL imports, for all checked members, even those thet ae not moved
  public List getAllImports(BinTypeRef targetType) {
    List result = new ArrayList(getCollector(targetType).extraImports);
    Iterator lists = getCollector(targetType).importsForMembers.values().
        iterator();
    while (lists.hasNext()) {
      CollectionUtil.addAllNew(result, (List) lists.next());
    }

    return result;
  }

  // gets imports only for those members that are in toMove list,
  // but not sure about getCollector(targetType).extraImports, this may
  // still contain some redundant imports
  public List getAllImports(BinTypeRef targetType, List toMove) {
    List result = new ArrayList(getCollector(targetType).extraImports);

    for (int i = 0; i < toMove.size(); i++) {
      List li = (List) getCollector(targetType).
          importsForMembers.get(toMove.get(i));

      if (li != null) {
        CollectionUtil.addAllNew(result, li);
      }
    }
    return result;
  }

  public void insertImportClauseToSource(
      final TransformationList transList,
      CompilationUnit compilationUnit,
      ImportUtils.ImportPosition importPosition,
      List toBeImported,
      int i,
      String importClause) {
    StringBuffer importLine = new StringBuffer(80);

    if (i == 0) {
      importLine.append(
          ImportUtils.generateNewlines(importPosition.before).toString());
    }

    importLine.append(importClause);

    if (i == toBeImported.size() - 1) {
      importLine.append(
          ImportUtils.generateNewlines(importPosition.after).toString());
    }

    if (i >= 0 && i < toBeImported.size() - 1) {
      importLine.append(ImportUtils.generateNewlines(1).toString());
    }

    StringInserter e = new StringInserter(
        compilationUnit, importPosition.line, importPosition.column,
        importLine.toString());

//    e.setWantingToReceiveUpdatesForLineResize(false);
    transList.add(e);
  }

  public void addExtraImports(List typesToImport,
      BinTypeRef typeIntoImport) throws AmbiguousImportImportException {
  	for (int i = 0; i < typesToImport.size(); i++) {
  		BinTypeRef typeToImport = null;
  		if (typesToImport.get(i) instanceof BinType) {
  			typeToImport = ((BinType) typesToImport.get(i)).getTypeRef();
  		} else if (typesToImport.get(i) instanceof BinTypeRef) {
  			typeToImport = (BinTypeRef) typesToImport.get(i);
  		}
  		addExtraImports(typeToImport, typeIntoImport);
  	}
  }
}
