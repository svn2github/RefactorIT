/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CommonTypeFinder;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.utils.GenericsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author  Arseni Grigorjev
 */
public class GenericsVariantsManager {

  /** Current common type for this variant */
  private BinTypeRef type;
  /** Is this a wildcard variant? */
  private boolean wildcardVariant = false;
  /** Set of variants */
  private Set variants = new HashSet(10);
  /** Type arguments */
  private GenericsVariantsManager[] arguments = null;
  
  /** Can this variant be changed? */
  private boolean constant = false;
  /** Is wildcard type allowed for this variant? */
  private boolean wildcardAllowed = true;

  /**
   * Creates empty instance of GenericsVariantsManager
   */
  public GenericsVariantsManager(){
    type = null;
  }

  /**
   * Creates instance of GenericsVariantsManager for given type.
   *  @param type type for this variant
   *  @param constant can it be changed during imports?
   */
  public GenericsVariantsManager(final BinTypeRef type, final boolean constant){
    addVariant(type);
    setType(type);

    this.constant = constant;

    if (type != null){
      final BinTypeRef[] typeArguments = type.getTypeArguments();
      if (typeArguments != null && typeArguments.length > 0){
        declareArguments(typeArguments.length);
        for (int i = 0; i < typeArguments.length; i++){
          setArgument(i, new GenericsVariantsManager(typeArguments[i],
              constant));
        }
      }
    }
  }
  
  /**
   * Creates mutable copy of given GenericsVariantsManager object.
   *  @param manager GenericsVariantsManager to be cloned
   */
  public GenericsVariantsManager(final GenericsVariantsManager manager){
    this.type = manager.getType();
    this.constant = manager.isConstant();
    this.wildcardAllowed = manager.isWildcardAllowed(); // FIXME: always true?
    this.variants.addAll(manager.getVariants());

    GenericsVariantsManager[] arguments = manager.getArguments();
    if (arguments != null){
      declareArguments(arguments.length);
      for (int i = 0; i < arguments.length; i++){
        setArgument(i, new GenericsVariantsManager(arguments[i]));
      }
    }
  }

  public boolean consumeType(BinTypeRef newType)
      throws GenericsVariantsImportException {
    if (newType == null){
      return false;
    } else if (typeIsObject(newType)) {
      forceWildcardVariant();
    } else if (newType.equals(this.type)){
      return false; // types are the same, nothing changed
    }

    transformArgumentsIfNeeded(newType);
    setType(newType);

    return true;
  }

  // arguments handling methods
  public void declareArguments(int n){
    arguments = new GenericsVariantsManager[n];
  }
  
  public void fillArgumentsWithEmptyManagers(){
    if (arguments != null){
      for (int i = 0; i < arguments.length; i++){
        arguments[i] = new GenericsVariantsManager();
      }
    }
  }
  
  public GenericsVariantsManager[] getArguments() {
    return this.arguments;
  }

  public void removeArguments() {
    arguments = null;
  }
  
  public void setArgument(int i, GenericsVariantsManager variant) {
    if (!wildcardAllowed){
      variant.forbidWildcardRecursively();
    }
    arguments[i] = variant;
  }

  public BinTypeRef getType() {
    return this.type;
  }

  public void setType(final BinTypeRef type) {
    this.type = type.getTypeRef();
  }

  public void forbidWildcardRecursively(){
    if (wildcardAllowed){
      this.wildcardAllowed = false;
      if (arguments != null){
        for (int i = 0; i < arguments.length; i++){
          arguments[i].forbidWildcardRecursively();
        }
      }
    }
  }

  public boolean isWildcardAllowed() {
    return this.wildcardAllowed;
  }

  public void setWildcardAllowed(final boolean wildcardAllowed) {
    this.wildcardAllowed = wildcardAllowed;
  }
  
  public boolean isWildcardVariant(){
    return this.wildcardVariant;
  }

  public void forceWildcardVariant() throws GenericsVariantsImportException {
    if (isWildcardAllowed()){
      clearVariants();
      this.type = null;
      this.wildcardVariant = true;
    } else {
      throw new GenericsVariantsImportException("Wildcard not allowed");
    }
  }

  public boolean isConstant() {
    return this.constant;
  }

  public void setConstant(final boolean constant) {
    this.constant = constant;
  }

  private boolean filterVariants() throws GenericsVariantsImportException {
    boolean madeChanges = false;
    
    boolean forceWildcard = false;
    BinTypeRef singleType = null;
    
    BinTypeRef variant;
    for (final Iterator it = variants.iterator(); it.hasNext(); ){
      variant = (BinTypeRef) it.next();

      if (typeIsObject(variant)){ // Object => <?>
        forceWildcard = true;
        madeChanges = true;
        break;
      } else if (variants.size() == 1) {
        break;
      } else if (variant.getBinCIType().isTypeParameter() && !madeChanges){
        singleType = variant;
        madeChanges = true;
      } else if (variant.getBinCIType().isTypeParameter()){
        forceWildcard = true;
        madeChanges = true;    
        break;
      }
    }
    
    if (forceWildcard){
      forceWildcardVariant();
    } else if (singleType != null){
      clearVariants();
      setType(singleType);
      addVariant(singleType);
    }

    return madeChanges;
  }
  
  public boolean importSoft(final List externManagers,
      final LocationAware context) throws GenericsVariantsImportException {
    if (isWildcardVariant()){
      return false;
    }

    boolean madeChanges = false;

    final Set constantVariants = new HashSet(externManagers.size());
    final Set collectedVariants = new HashSet(externManagers.size()*2);

    GenericsVariantsManager nextManager;
    for (int i = 0, max_i = externManagers.size(); i < max_i; i++){
      nextManager = (GenericsVariantsManager) externManagers.get(i);
      if (nextManager.isWildcardVariant()){
        forceWildcardVariant();
      } else if (nextManager.isConstant()){
        constantVariants.addAll(nextManager.getVariants());
      }
      collectedVariants.addAll(nextManager.getVariants());
    }

    madeChanges |= variants.addAll(collectedVariants);
    switch (constantVariants.size()){
      case 0:
        boolean filtered = filterVariants();
        if (madeChanges && !filtered){
          BinTypeRef commonType = findCommonType();
          madeChanges |= consumeType(commonType);
        } else {
          madeChanges |= filtered;
        }
        break;

      case 1:
        BinTypeRef constantType = (BinTypeRef) constantVariants.iterator()
            .next();
        if (allVariantsAreDerivedFrom(constantType)){
          madeChanges |= consumeType(constantType);
        } else {
          forceWildcardVariant();
        }
        break;

      default:
        forceWildcardVariant();
    }

    if (typeIsForeignTypeParameter(type, context)){
      forceWildcardVariant();
    }
    
    if (type != null && type.getBinCIType().getTypeParameters() != null
        && type.getBinCIType().getTypeParameters().length > 0){
      madeChanges = importArgumentsSoft(externManagers, context);
    }

    return madeChanges;
  }

  /**
   * @param externManagers
   * @param context
   * @return
   * @throws GenericsVariantsImportException
   */
  private boolean importArgumentsSoft(final List externManagers,
      final LocationAware context) throws GenericsVariantsImportException {
    boolean madeChanges = false;
    GenericsVariantsManager nextManager;
    final List managerCopies = createCopiesOfManagers(externManagers);

    final List[] argumentsManagers = new ArrayList[type.getBinCIType()
        .getTypeParameters().length];
    for (int a = 0; a < argumentsManagers.length; a++){
      argumentsManagers[a] = new ArrayList(10);
      for (int i = 0, max_i = managerCopies.size(); i < max_i; i++){
        nextManager = (GenericsVariantsManager) managerCopies.get(i);
        nextManager.consumeType(type);
        if (nextManager.getArguments() != null){
          argumentsManagers[a].add(nextManager.getArguments()[a]);
        }
      }
    }

    if (containsManagers(argumentsManagers)){
      if (arguments == null){
        declareArguments(argumentsManagers.length);
        fillArgumentsWithEmptyManagers();
      }
      for (int i = 0; i < arguments.length; i++){
        madeChanges |= arguments[i].importSoft(argumentsManagers[i],
            context);
      }
    }

    return madeChanges;
  }
  
  public boolean importHard(final GenericsVariantsManager externManager,
      LocationAware context) throws GenericsVariantsImportException {
//    System.out.println("forceVariant()");
    boolean madeChanges = false;
    
    if (externManager.isWildcardVariant() || isWildcardVariant()){
      return false;
    } else if (typeIsForeignTypeParameter(externManager.getType(), context)){
      forceWildcardVariant();
    } else if (!isConstant()){
      madeChanges |= addVariants(externManager.getVariants());
      madeChanges |= consumeType(externManager.getType());
      madeChanges |= importArgumentsHard(externManager, context);
    } else {
      madeChanges |= importArgumentsHard(externManager, context);
    }

    return madeChanges;
  }
  
  private boolean importArgumentsHard(GenericsVariantsManager externManager,
      LocationAware context) throws GenericsVariantsImportException {
    boolean madeChanges = false;
    final GenericsVariantsManager[] externArguments = externManager
        .getArguments();
    final GenericsVariantsManager[] thisArguments = getArguments();
    if (externArguments != null){
      if (thisArguments == null){
        copyExternalArguments(externArguments);
        madeChanges = true;
      } else {
        // synchronize variants recursively
        for (int i = 0; i < externArguments.length; i++){
          madeChanges |= thisArguments[i].importHard(externArguments[i],
              context);
        }
      }
    }
    return madeChanges;
  }

  private BinTypeRef findCommonType() {
    BinTypeRef commonType = null;
    switch(variants.size()){
      case 0:
        commonType = null;
      case 1:
        commonType = (BinTypeRef) variants.iterator().next();
      default:
        final CommonTypeFinder typeFinder = new CommonTypeFinder(variants);
        typeFinder.setInterfacesHigherPriority(true);
        commonType = typeFinder.getCommonType();

        if (commonType == null){
          Iterator it = variants.iterator();
          if (it.hasNext()){
            commonType = ((BinTypeRef) it.next()).getProject().getObjectRef();
          } else {
            commonType = null;
          }
        }
    }

    return commonType;
  }
  
  private static boolean typeIsForeignTypeParameter(final BinTypeRef typeToImport,
      final LocationAware importContext) {
    return typeToImport != null
        && typeToImport.getBinCIType().isTypeParameter()
        && !((BinMember) typeToImport.getBinCIType().getParent()).contains(
        importContext);
  }

  private static List createCopiesOfManagers(final List listOfManagers) {
    List managerCopies = new ArrayList(listOfManagers.size());
    for (int i = 0, max_i = listOfManagers.size(); i < max_i; i++){
      managerCopies.add(new GenericsVariantsManager(
          (GenericsVariantsManager) listOfManagers.get(i)));
    }

    return managerCopies;
  }
  
  private static boolean containsManagers(List[] argumentsManagers){
    for (int i = 0; i < argumentsManagers.length; i++){
      if (argumentsManagers[i].size() > 0){
        return true;
      }
    }
    return false;
  }

  public void transformArgumentsIfNeeded(final BinTypeRef newType){
    if (type == null || newType == null){
      return;
    }

    final BinTypeRef[] newTypeTypeParameters = newType.getTypeParameters();
    
    if (newType.equals(type) // type hasn`t changed
        || arguments == null // no arguments to recalculate
        || newTypeTypeParameters == null // no type parameters - no arguments
        || newTypeTypeParameters.length == 0){
      return; // do nothing
    }
    
    final Map variantsLinking = new HashMap(10);

    final BinTypeRef[] oldTypeTypeParameters = type.getTypeParameters();
    for (int i = 0; i < oldTypeTypeParameters.length; i++){
      variantsLinking.put(oldTypeTypeParameters[i], arguments[i]);
    }

    final List path = new ArrayList(10);
    GenericsUtil.findInheritancePath(newType, type, path, new HashSet(10));

    GenericsVariantsManager.linkSupertypesPath(path, 0, oldTypeTypeParameters,
        variantsLinking);

    declareArguments(newTypeTypeParameters.length);
    for (int i = 0; i < newTypeTypeParameters.length; i++){
      final GenericsVariantsManager argumentVariant
          = (GenericsVariantsManager) variantsLinking.get(newTypeTypeParameters[i]);
      if (argumentVariant == null){
        removeArguments();
        break;
      } else {
        setArgument(i, argumentVariant);
      }
    }
  }
  
  private void copyExternalArguments(
      final GenericsVariantsManager[] externArguments) {
    declareArguments(externArguments.length);
    for (int i = 0; i < externArguments.length; i++){
      setArgument(i, new GenericsVariantsManager(externArguments[i]));
    }
  }

  public boolean isLinkedToManagers(final GenericsVariantsManager[] managers) {
    int i;
    for (i = 0; i < managers.length; i++){
      if (this == managers[i]){
        return true;
      }
    }

    if (arguments != null){
      for (i = 0; i < arguments.length; i++){
        if (arguments[i].isLinkedToManagers(managers)){
          return true;
        }
      }
    }

    return false;
  }

  public StringBuffer generateTypeString() {
    final StringBuffer buf = new StringBuffer(getTypeString());
    if (arguments != null){
      buf.append('<');
      for (int i = 0; i < arguments.length; i++){
        if (i > 0){
          buf.append(", ");
        }
        buf.append(arguments[i].generateTypeString());
      }
      buf.append('>');
    }
    return buf;
  }

  private String getTypeString() {
    if (isWildcardVariant()){
      return "?";
    } else if (type == null){
      return "(null)"; // illegal situation, should check completness first!
    } else {
      return type.getName();
    }
  }
  
  public Set getVariants(){
    return variants;
  }
  
  public boolean addVariants(Set externVariants) {
    return variants.addAll(externVariants);
  }
  
  public void setVariants(Set externVariants){
    this.variants = externVariants;
  }
  
  public void addVariant(BinTypeRef variant){
    if (variant != null){
      variants.add(variant);
    }
  }
  
  private boolean allVariantsAreDerivedFrom(final BinTypeRef type) {
    boolean result = true;
    for (final Iterator it = variants.iterator(); it.hasNext(); ){
      if (!((BinTypeRef) it.next()).isDerivedFrom(type)){
        result = false;
        break;
      }
    }
    return result;
  }

  // static linking utilities
  
  public static void linkTypeArguments(final BinTypeRef currentSupertype,
      final BinTypeRef[] subtypeTypeParameters, final Map variantsLinking) {

    final BinTypeRef[] typeArguments = currentSupertype.getTypeArguments();
    if (typeArguments != null){
      for (int i = 0; i < typeArguments.length; i++){
        variantsLinking.put(((BinSpecificTypeRef) typeArguments[i])
            .getCorrespondingTypeParameter(), linkTypeArgument(
            typeArguments[i], subtypeTypeParameters, variantsLinking));
      }
    }
  }

  public static GenericsVariantsManager linkTypeArgument(
      final BinTypeRef typeArgument,
      final BinTypeRef[] subtypeTypeParameters,
      final Map variantsLinking) {
    GenericsVariantsManager result = null;
    for (int i = 0; i < subtypeTypeParameters.length; i++){
      if (typeArgument.equals(subtypeTypeParameters[i])){
        result = (GenericsVariantsManager) variantsLinking.get(
            subtypeTypeParameters[i]);
        break;
      }
    }
    
    if (result == null){
      result = new GenericsVariantsManager(typeArgument, true);

      final BinTypeRef[] nextTypeArguments = typeArgument.getTypeArguments();
      if (nextTypeArguments != null && nextTypeArguments.length > 0){

        result.declareArguments(nextTypeArguments.length);
        
        for (int i = 0; i < nextTypeArguments.length; i++){
          result.setArgument(i, linkTypeArgument(
              nextTypeArguments[i], subtypeTypeParameters, variantsLinking));
        }

      }
    }

    return result;
  }
  
  public static void linkSupertypesPath(final List path, final int i,
      final BinTypeRef[] subtypeTypeParameters, final Map variantsLinking){
    if (i >= path.size()){
      return;
    }

    final BinTypeRef currentSupertype = (BinTypeRef) path.get(i);

    linkTypeArguments(currentSupertype, subtypeTypeParameters, variantsLinking);

    linkSupertypesPath(path, i + 1, currentSupertype.getBinCIType()
        .getTypeParameters(), variantsLinking);
  }
  
  public static void linkAllSupertypes(final BinTypeRef type,
      final BinTypeRef[] typeParameters, final Map variantsLinking){

    final BinTypeRef[] supertypes = type.getSupertypes();
    final HashSet visited = new HashSet(10);
    for (int i = 0; i < supertypes.length; i++){
      linkAllSupertypes(supertypes[i], typeParameters, variantsLinking,
          visited);
    }
  }
  
  private static void linkAllSupertypes(final BinTypeRef currentSupertype,
      final BinTypeRef[] subtypeTypeParameters, final Map variantsLinking,
      final HashSet visitedSupertypes){
    if (visitedSupertypes.contains(currentSupertype)){
      return;
    }
    visitedSupertypes.add(currentSupertype);

    linkTypeArguments(currentSupertype, subtypeTypeParameters, variantsLinking);
    
    final BinTypeRef[] currentTypeParameters = currentSupertype.getBinCIType()
        .getTypeParameters();

    final BinTypeRef[] supertypes = currentSupertype.getSupertypes();
    for (int i = 0; i < supertypes.length; i++){
      linkAllSupertypes(supertypes[i], currentTypeParameters, variantsLinking,
          visitedSupertypes);
    }
  }

  public void clearVariants() {
    variants.clear();
  }

  public boolean hasUnresolvedVariants() {
    if (type == null){
      return !isWildcardVariant();
    } else if (arguments != null){
      boolean foundNullValues = false;
      for (int i = 0; i < arguments.length; i++){
        foundNullValues |= arguments[i].hasUnresolvedVariants();
        if (foundNullValues){
          return true;
        }
      }
    }
    return false;
  }

  private static int depth = 0;
  public String toString(){
    depth++;
    String INDENT = StringUtil.getIndent(depth, 6);
    String result = INDENT + this.getClass().getName() + ": \n";
    result += INDENT + "- variant: <" + getTypeString() + ">\n";
    result += INDENT + "- wildcard allowed: " + isWildcardAllowed() + "\n";
    result += INDENT + "- type arguments: ";
    if (arguments == null){
      result += "(none)\n";
    } else {
      for (int i = 0; i < arguments.length; i++){
        result += '\n' + INDENT + "  arg[" + i + "]: \n" + arguments[i] + "\n";
      }
    }
    
    depth--;
    return result;
  }

  public static boolean typeIsObject(final BinTypeRef type) {
    return type != null && type.equals(type.getProject()
        .getObjectRef());
  }

  public boolean hasWildcardVariants() {
    if (isWildcardVariant()){
      return true;
    } else if (arguments != null){
      for (int i = 0; i < arguments.length; i++){
        if (arguments[i].hasWildcardVariants()){
          return true;
        }
      }
    }
    return false;
  }
}
