/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.TypesUsedThroughImportFinder;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.view.ProjectView;
import net.sf.refactorit.transformations.view.ProjectViewQuery;
import net.sf.refactorit.transformations.view.Triad;
import net.sf.refactorit.transformations.view.triads.NameTriad;
import net.sf.refactorit.transformations.view.triads.OwnerTriad;
import net.sf.refactorit.transformations.view.triads.PackageTriad;
import net.sf.refactorit.transformations.view.triads.SourceHolderTriad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *  Part of the frozen Transformation Framework. Listens to virtual project
 *  view`s updates, analyzes virtual state, reports import conflicts to
 *  other actuators on this {@link TransformationManager} and generates
 *  editors, that add and remove import statements.
 *
 * @author  Arseni Grigorjev
 */
public class NewImportManager extends AbstractAnalyzer {

  private MultiValueMap unusedImportsBefore = new MultiValueMap();
  private MultiValueMap unusedImportsAfter = new MultiValueMap();
  private MultiValueMap neededImports = new MultiValueMap();
  private MultiValueMap conflicts = new MultiValueMap();
  
  private ProjectViewQuery query;
  
  public NewImportManager(TransformationManager transformationManager) {
    super(transformationManager);
  }

  public TransformationList performChange() {
    return null; // FIXME: should generate import statements
  }
  
  public void notifyViewUpdated() {
    clear();
    resolveImports();
    
    // FIXME: clear?
  }
  
  public void notifyConflicts() {
    // TODO
  }
  
  public void clear() {
    unusedImportsBefore.clear();
    unusedImportsAfter.clear();
    neededImports.clear();
    conflicts.clear();
    
    renamedTypes.clear();
    renamedPackages.clear();
    movedTypes.clear();
    movedMembers.clear();
  }
  
  private Set renamedTypes = new HashSet();
  private Set renamedPackages = new HashSet();
  private Set movedTypes = new HashSet();
  private Set movedMembers = new HashSet();
  private Set movedSources = new HashSet();
  
  List involvedCompilationUnits = new ArrayList();
  private ProjectView projectView;
  
  private void resolveImports() {
    query = new ProjectViewQuery(getTransformationManager().getProjectView());
    projectView = getTransformationManager().getProjectView();
    
    collectRenamedItems();
    
    collectMovedItems();
    
    collectInvolvedCompilationUnits();

    //query.get
    /*for (Iterator it = getInvolvedCompilationUnits(importVirtualProjectView).iterator(); it.hasNext();){
      SourceHolder sourceHolder = (SourceHolder) it.next();
      if (sourceHolder instanceof CompilationUnit){
        unusedImportsBefore.putAll(sourceHolder, listUnusedImports(
            (CompilationUnit) sourceHolder, false));
        unusedImportsAfter.putAll(sourceHolder, listUnusedImports(
            (CompilationUnit) sourceHolder, true));
      } else {
        // it`s a new source holder, that has no imports yet but is gonna need them
        neededImports.putAll(sourceHolder, getUsedTypesThatNeedImport(
            importVirtualProjectView, sourceHolder));
      }

      analyzeRequestedImports(importVirtualProjectView, sourceHolder);
    }*/
  }
  
  public Set getInvolvedCompilationUnits(){
    Set result = new HashSet();
    /*for (Iterator it = importVirtualProjectView.getDiscardedUsages().iterator(); it.hasNext(); ){
      BinTypeRefManager discardedUsage = (BinTypeRefManager) it.next();
      
      final BinTypeRef type;
      if (discardedUsage instanceof BinSourceConstruct){
        type = ((BinSourceConstruct) discardedUsage).getOwner();
      } else if (discardedUsage instanceof BinMember){
        type = ((BinMember) discardedUsage).getOwner();
      } else {
        type = null;
      }
      
      if (type != null){
        //result.add(getSourceInViewForType(importVirtualProjectView, type));
      }
    }
    for (Iterator it = importVirtualProjectView.getImportRequests().keySet().iterator(); it.hasNext(); ){
     // result.add(getSourceInViewForType(importVirtualProjectView, (BinTypeRef) it.next()));
    }
    // FIXME: ivolved in move and rename also (++ cUnits, where are used names of moved/renamed types)*/
    return result;
  }

  public List listUnusedImports(CompilationUnit compilationUnit,
      boolean forVirtualState) {
    TypesUsedThroughImportFinder typesFinder = new TypesUsedThroughImportFinder();
    Set validTypes = typesFinder.findFor(compilationUnit);
    if (forVirtualState){
     // FIXME: add requested for import types? 
    }

    ArrayList result = new ArrayList();

    List packageNodes = compilationUnit.getImportedPackageNodes();
    List typeNameNodes = compilationUnit.getImportedTypeNameNodes();

    Set foundSingleImport = new HashSet();

    Set existingTypeImportString = new HashSet();
    for (int i = 0; i < typeNameNodes.size(); i++) {
      ASTImpl node = (ASTImpl) typeNameNodes.get(i);

      String name = LoadingASTUtil.combineIdentsDotsAndStars(node);

      boolean wasOnly = existingTypeImportString.add(name);
      if (!wasOnly) {
        result.add(node);
        continue;
      }

      BinTypeRef nameRef = compilationUnit.getProject().getTypeRefForSourceName(name);

      if (nameRef == null || !validTypes.contains(nameRef)) {
        result.add(node);
      } else {
        foundSingleImport.add(nameRef);
      }
    }

    validTypes.removeAll(foundSingleImport);

    Set validPackages = getPackagesForTypes(validTypes);

    // FIXME: did the packageNodes list contain also the typeImports?

    Set existingPackageImportString = new HashSet();

    for (int i = 0; i < packageNodes.size(); ++i) {
      ASTImpl node = (ASTImpl) packageNodes.get(i);
      if (node == null) {
        // skip the java.lang pseudopackage
        continue;
      }

      String importName = LoadingASTUtil.combineIdentsDotsAndStars(node);

      boolean wasOnly = existingPackageImportString.add(importName);
      if (!wasOnly) {
        result.add(node);
        continue;
      }

      String packageName = LoadingASTUtil.extractUntilLastDot(importName);
      BinPackage aPackage = compilationUnit.getProject().getPackageForName(packageName);

      if (aPackage == null || !validPackages.contains(aPackage)) {
        result.add(node);
      }
    };

    return result;
  }
  
  static Set getPackagesForTypes(Set typeRefSet) {
    Set result = new HashSet();
    for (Iterator i = typeRefSet.iterator(); i.hasNext(); ) {
      BinTypeRef ref = (BinTypeRef) i.next();
      result.add(ref.getPackage());
    }

    return result;
  }

  public void analyzeRequestedImports() {
//    final List typesInThisSource;
//    BinTypeRef typeRef = (BinTypeRef) importVirtualProjectView.getTypeToLocation().getKeyByValue(
//        sourceHolder);
//    if (typeRef != null){
//      /* If type has new source holder, then it was a) moved; b) it is the only
//          type in this compilation unit. */
//      typesInThisSource = CollectionUtil.singletonArrayList(typeRef);
//    } else {
//      CompilationUnit compilationUnit = (CompilationUnit) sourceHolder;
//      typesInThisSource = new ArrayList(compilationUnit.getDefinedTypes());
//      // filter types, that have been moved from this compilation unit
//      BinTypeRef currentType;
//      for (int i = 0, i_max = typesInThisSource.size(); i < i_max; i++){
//        currentType = (BinTypeRef) typesInThisSource.get(i);
//        /*if (getSourceInViewForType(importVirtualProjectView, currentType) != sourceHolder){
//          typesInThisSource.remove(i--);
//        }*/
//      }
//    }
//    
//    BinTypeRef currentType;
//    for (int i = 0, i_max = typesInThisSource.size(); i < i_max; i++){
//      currentType = (BinTypeRef) typesInThisSource.get(i);
//      List requestedTypes = importVirtualProjectView.getImportRequests().get(currentType);
//      if (requestedTypes != null){
//        neededImports.putAll(sourceHolder, requestedTypes);
//      }
//    }
  }

  public Set getUsedTypesThatNeedImport(SourceHolder newSource) {
    /*BinTypeRef typeRef = (BinTypeRef) importVirtualProjectView.getTypeToLocation().getKeyByValue(newSource);
    TypesUsedThroughImportFinder typesFinder = new TypesUsedThroughImportFinder();
    //typesFinder.setForVirtualView(true);
    //typesFinder.setVirtualView(importVirtualProjectView);
    return typesFinder.findFor(typeRef.getBinCIType());*/
    return null;
  }
  
  public void debugImportsToAdd(){
    System.out.println("[ARS>>] Imports to add");
    for (Iterator it = neededImports.keySet().iterator(); it.hasNext(); ){
      SourceHolder source = (SourceHolder) it.next();
      System.out.println("[ARS>>]     For source: " + source.getName());
      List types = unusedImportsBefore.get(source);
      for (Iterator it2 = types.iterator(); it2.hasNext(); ){
        BinTypeRef type = (BinTypeRef) it2.next();
        /*System.out.println("[ARS>>]             need import: "
            + getQualifiedNameInViewForType(importVirtualProjectView, type));*/
      }
    }
  }

  public void debugUnusedImports() {
    System.out.println("[ARS>>] Unused imports");
    for (Iterator it = unusedImportsAfter.keySet().iterator(); it.hasNext(); ){
      SourceHolder source = (SourceHolder) it.next();
      System.out.println("[ARS>>]     For source: " + source.getName());
      System.out.println("[ARS>>]         before:");
      List unusedImports = unusedImportsBefore.get(source);
      for (Iterator it2 = unusedImports.iterator(); it2.hasNext(); ){
        ASTImpl importAst = (ASTImpl) it2.next();
        debugLineFromAst(importAst);
      }
      System.out.println("[ARS>>]         after:");
      unusedImports = unusedImportsAfter.get(source);
      for (Iterator it2 = unusedImports.iterator(); it2.hasNext(); ){
        ASTImpl importAst = (ASTImpl) it2.next();
        debugLineFromAst(importAst);
      }
    }
  }

  /**
   * @param importAst
   */
  private void debugLineFromAst(final ASTImpl importAst) {
    System.out.println("[ARS>>]             unused import: "
        + importAst.getSource().getContentOfLine(importAst.getLine()));
  }

  
  private void collectRenamedItems() {
    List renameTriads = projectView.getTriads(null, NameTriad.class, null);
    for (int i = 0, max_i = renameTriads.size(); i < max_i; i++){
      final Object subject = ((Triad) renameTriads.get(i)).getSubject();
      if (subject instanceof BinCIType){
        renamedTypes.add(subject);
      } else if (subject instanceof BinPackage){
        renamedPackages.add(subject);
      }
    }
  }

  private void collectMovedItems() {
    // Types and members, that have changes their owners:
    List triads = projectView.getTriads(null, OwnerTriad.class, null);
    for (int i = 0, max_i = triads.size(); i < max_i; i++){
      final Object subject = ((Triad) triads.get(i)).getSubject();
      if (subject instanceof BinCIType){
        movedTypes.add(subject);
      } else if (subject instanceof BinMember){
        movedMembers.add(subject);
      }
    }
    
    // Types that changed compilation unit
    triads = projectView.getTriads(null, SourceHolderTriad.class, null);
    for (int i = 0, max_i = triads.size(); i < max_i; i++){
      movedTypes.add(((Triad) triads.get(i)).getSubject());
    }

    // compilation units, that changed package
    triads = projectView.getTriads(null, PackageTriad.class, null);
    for (int i = 0, max_i = triads.size(); i < max_i; i++){
      final Object subject = ((Triad) triads.get(i)).getSubject();
      //movedTypes.addAll(((SourceHolder) subject).getDefinedTypes());
      //movedSources.add(((Triad) triads.get(i)).getSubject());
    }
  }

  private void collectInvolvedCompilationUnits() {
    final List projects = getTransformationManager().getProjects();
    for (Iterator it = projects.iterator(); it.hasNext(); ){
      Project project = (Project) it.next();
      List compilationUnits = project.getCompilationUnits();
      for (int i = 0, max_i = compilationUnits.size(); i < max_i; i++){
        final CompilationUnit compilationUnit = (CompilationUnit)
            compilationUnits.get(i);
        if (checkIsInvolved(compilationUnit)){
          involvedCompilationUnits.add(compilationUnit);
        }
      }
    }
  }

  private boolean checkIsInvolved(CompilationUnit compilationUnit) {
    // sources that import renamed types
    for (Iterator it = renamedTypes.iterator(); it.hasNext(); ){
      if (compilationUnit.importsTypeDirectly(((BinCIType) it.next())
          .getQualifiedName())){
        return true;
      }
    }

    // sources that import moved types
    for (Iterator it = movedTypes.iterator(); it.hasNext(); ){
      BinCIType movedType = (BinCIType) it.next();
      if (compilationUnit.importsTypeDirectly(movedType.getQualifiedName())){
        return true;
      }
    }

    // sources that import renamed packages
    for (Iterator it = renamedPackages.iterator(); it.hasNext(); ){
      BinPackage curPackage = (BinPackage) it.next();
      if (compilationUnit.importsPackage(curPackage)){
        return true;
      } else {
        List typeNames = compilationUnit.getImportedTypeNames();
        if (typeNames != null){
          for (int i = 0, max = typeNames.size(); i < max; i++){
            String importedTypeFQN = (String) typeNames.get(i);
            if (importedTypeFQN.startsWith(curPackage.getQualifiedName())){
              return true;
            }
          }
        }
      }
    }

    return false;
  }
  
  public void debugInvolvedSources(){
    System.out.println("Involved compilation units: " + involvedCompilationUnits);
  }
}
