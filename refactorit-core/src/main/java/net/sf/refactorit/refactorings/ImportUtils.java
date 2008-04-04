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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public final class ImportUtils {
  public static final class ImportPosition {
    public int line = 0; // where to start adding imports
    public int before = 0; // how many lines to add before our import
    public int after = 1; // how many lines to add after our import

    public int column = 0;

    public String toString() {
      return "line: " + line + ", before: " + before + ", after: " + after;
    }
  }


  public static ASTImpl getPackageAndOwnersNode(CompilationUnit source,
      ASTImpl typeNode) {
    if (Assert.enabled) {
      Assert.must(typeNode != null, "Node is null! File: " + source);

      // This assertion does not seem to be valid when this method is called from isFqnUsage() method
      /*Assert.must(node.getType() == JavaTokenTypes.IDENT || node.getType() == JavaTokenTypes.SUPER_CTOR_CALL,
        "Node is not IDENT or SUPER_CTOR_CALL: "
          + node + ", file: " + source);*/
    }

    if (typeNode.getType() == JavaTokenTypes.SUPER_CTOR_CALL) {
      return null;
    }

    ASTImpl parent = typeNode.getParent();

    if (parent != null && parent.getType() == JavaTokenTypes.DOT) {
      parent = (ASTImpl) parent.getFirstChild();
    } else {
      parent = null;
    }

    // HACK: works this way at the moment, but the reasons should be explored
    if (typeNode == parent) {
      parent = null;
      //(new rantlr.debug.misc.ASTFrame("getPackageNode", parent)).setVisible(true);
    }

    /*    if (parent != null) {
     (new rantlr.debug.misc.ASTFrame("getPackageNode", parent)).setVisible(true);
        }
//    System.err.println("Node: " + node + ", parent: " + parent);*/

    return parent;
  }

  private static String combineIdentsAndDots(ASTImpl node) {
    return combineIdentsAndDotsOfChildren(getTopDotNodeParent(node));
  }

  public static boolean isFqnUsage(final String qName, ASTImpl typeNode,
      ASTImpl topNode) {
    String combinedName = LoadingASTUtil.combineIdentsAndDots(topNode,
        typeNode);
    if (combinedName.startsWith(StringUtil.replace(qName, '$', '.'))) {
      return true;
    }
    return false;
  }

  public static ASTImpl getTopDotNodeParent(ASTImpl node) {
    while (node.getParent() != null
        && node.getParent().getType() == JavaTokenTypes.DOT) {
      node = node.getParent();
    }

    return node;
  }

  public static String getTopDotNodeText(ASTImpl node) {
    ASTImpl rightNode = ImportUtils.getTopDotNodeParent(node);
    return new CompoundASTImpl(rightNode).getText();
  }

  private static String combineIdentsAndDotsOfChildren(ASTImpl topDotNode) {
    return uniteWithDots(LoadingASTUtil.extractIdentNodesFromDot(topDotNode));
  }

  private static String uniteWithDots(ASTImpl[] nodes) {
    return uniteWithDots(nodes, nodes.length);
  }

  private static String uniteWithDots(final ASTImpl[] nodes,
      int amountOfNodesToTake) {
    if (amountOfNodesToTake == 0) {
      return null;
    }

    StringBuffer result = new StringBuffer(nodes[0].getText());
    for (int i = 1; i < amountOfNodesToTake; i++) {
      result.append('.');
      result.append(nodes[i].getText());
    }

    return result.toString();
  }

  /**
   * Checks if this is FQN (incl package.Class.Inner.member)
   * or just a Class.Inner reference
   */
  public static boolean isFqnUsage(CompilationUnit source, ASTImpl node) {
    // This discards asterisks (if any)
    ASTImpl[] nodes = LoadingASTUtil.extractIdentNodesFromDot(getTopDotNodeParent(
        node));
    if (nodes.length <= 1) {
      // items in default pacakge cannot have an FQN name
      return false;
    }

    // Check for types: package.Type, package.Type.Inner and so on
    if (isFqnTypeUsage(source, uniteWithDots(nodes))) {
      return true;
    }

    // Check for methods and fields: package.Type.member,
    // package.Type.Inner.member and so on
    String fqn = uniteWithDots(nodes, nodes.length - 1);
    String lastNodeName = nodes[nodes.length - 1].getText();

    return isFqnTypeUsage(source, fqn) && typeHasMethodOrFieldWithName(
        source.getProject().getTypeRefForSourceName(fqn).getBinCIType(),
        lastNodeName);
  }

  private static boolean typeHasMethodOrFieldWithName(BinCIType type,
      String memberName) {
    if (typeHasDeclaredFieldWithName(type, memberName) ||
        typeHasDeclaredMethodWithName(type, memberName)) {
      return true;
    }

    BinTypeRef[] supertypes = type.getTypeRef().getSupertypes();
    for (int i = 0; i < supertypes.length; i++) {
      BinCIType supertype = supertypes[i].getBinCIType();
      if (typeHasMethodOrFieldWithName(supertype, memberName)) {
        return true;
      }
    }

    return false;
  }

  private static boolean typeHasDeclaredFieldWithName(BinCIType type,
      String fieldName) {
    return type.getDeclaredField(fieldName) != null;
  }

  private static boolean typeHasDeclaredMethodWithName(BinCIType type,
      String methodName) {
    BinMethod[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().equals(methodName)) {
        return true;
      }
    }

    return false;
  }

  /** Incl inner types; excl member usages. */
  private static boolean isFqnTypeUsage(CompilationUnit source, String fqn) {
    if (fqn != null) {
      BinTypeRef ref = source
          .getProject().getTypeRefForSourceName(fqn);

      if (ref == null) {
        return false;
      }

      // Default package items cannot have a FQN name
      return!ref.getPackage().isDefaultPackage();
    }

    return false;
  }

  /**
   * "package.Class.Inner" and "package.Class.*" both match,
   * but member usages do not.
   */
  public static boolean isFqnInnerTypeUsage(CompilationUnit source, ASTImpl node) {
    String fqn = combineIdentsAndDots(node);
    if (fqn == null) {
      return false;
    }

    BinTypeRef typeRef = source
        .getProject().getTypeRefForSourceName(fqn);

    if (typeRef == null) {
      return false;
    }

    if (typeRef.getPackage().isDefaultPackage()) {
      return false;
    }

    // If we're here then we have a FQN usage of a type that really exists.
    // Now we need to check if the used type is an inner class.

    return typeRef.getBinCIType().isInnerType() ||
        ImportUtils.dotExpressionContainsAsterisk(getTopDotNodeParent(node));
  }

  public static StringBuffer generateImportClause(String fqnTypeName) {
    fqnTypeName = fqnTypeName.replace('$', '.');

    StringBuffer result = new StringBuffer(fqnTypeName.length() + 8);
    result.append("import ");
    result.append(fqnTypeName);
    result.append(";");

    return result;
  }

  public static StringBuffer generateImportClause(String packageName,
      String typeName) {
    StringBuffer result = new StringBuffer(packageName.length()
        + typeName.length() + 9);
    result.append("import ");
    result.append(packageName);
    result.append('.');
    result.append(typeName);
    result.append(';');
    return result;
  }

  public static ImportPosition calculateNewImportPosition(
      CompilationUnit compilationUnit, boolean willBeAddingPackageDef) {
    return calculateNewImportPosition(compilationUnit, willBeAddingPackageDef, null);
  }

  public static ImportPosition calculateNewImportPosition(
      CompilationUnit compilationUnit, boolean willBeAddingPackageDef, Set nodesToGo) {
//System.err.println("calculateNewImportPosition: "
//    + compilationUnit + ", willBeAddingPackageDef: " + willBeAddingPackageDef
//    + "nodesToGo: " + nodesToGo);
    ImportPosition position = new ImportPosition();
    ASTImpl node = compilationUnit.getSource().getFirstNode();
    ASTImpl prev = node;

    while (node != null) {
      if (nodesToGo != null && nodesToGo.contains(node)) {
        continue;
      }

      if (node.getType() == JavaTokenTypes.CLASS_DEF
          || node.getType() == JavaTokenTypes.ENUM_DEF
          || node.getType() == JavaTokenTypes.ANNOTATION_DEF
          || node.getType() == JavaTokenTypes.INTERFACE_DEF
          || node.getType() == JavaTokenTypes.PACKAGE_DEF) {
        break;
      }

      prev = node;
      node = (ASTImpl) node.getNextSibling();
    }
//System.err.println("node: " + node + ", prev: " + prev);

    if (node.getType() == JavaTokenTypes.PACKAGE_DEF) {
      prev = node;
      node = (ASTImpl) node.getNextSibling();
      while (nodesToGo != null && nodesToGo.contains(node)) {
        node = (ASTImpl) node.getNextSibling();
      }

      if (node == null) {
        node = prev;
        position.before = 1;
      } else {
        if (node.getStartLine() == prev.getEndLine()) {
          position.before = 2;
        } else {
          position.before = 1;
        }
      }
    } else if (willBeAddingPackageDef) {
      // we will be adding package declaration ourselves so should leave a space
      position.before = 1;
    } else {
      position.before = 0;
    }
//System.err.println("node2: " + node + ", prev: " + prev);

    int prevLine = 0;
    if (prev != node) {
      prevLine = prev.getLine();
    }

    if (node.getType() == JavaTokenTypes.CLASS_DEF
        || node.getType() == JavaTokenTypes.INTERFACE_DEF
        || node.getType() == JavaTokenTypes.ENUM_DEF
        || node.getType() == JavaTokenTypes.ANNOTATION_DEF) {
      position.after = 3;
      position.column = node.getColumn() - 1;
    } else {
      position.after = 1;
    }

    position = findCommentBetweenLines(compilationUnit, prevLine,
        node.getLine(), position);

    if (position.line < 0) {
      position.line = node.getLine();
    }
//System.err.println("position: " + position);
    int gap = position.line - prevLine;
//System.err.println("Line: " + position.line);
//System.err.println("Gap: " + gap);
    if (gap > 1) {
      int diff = gap - position.before;
//System.err.println("Diff: " + diff);
      if (diff < 0) {
        position.before += diff;
      } else {
        position.before = 0;
        position.after -= diff - 1;
        if (position.after < 0) {
          position.after = 0;
        }
        position.line -= diff - 1;
      }
    }

//System.err.println("Calculated position: " + position);

    return position;
  }

  public static ImportPosition findCommentBetweenLines(CompilationUnit compilationUnit,
      int begin, int end,
      ImportPosition position) {
    final List simpleComments = compilationUnit.getSimpleComments();
    final List javadocComments = compilationUnit.getJavadocComments();
    List comments = new ArrayList(simpleComments.size()
        + javadocComments.size());
    comments.addAll(simpleComments);
    comments.addAll(javadocComments);
    Collections.sort(comments);

    position.line = -1;

    for (int i = 0, max = comments.size(); i < max; i++) {
      final int line = ((Comment) comments.get(i)).getStartLine();

      if (line >= begin && line <= end) {
        if (position.line >= 0) { // If found a second comment in the area
          position.after--;
          if (position.after < 0) {
            position.after = 0;
          }
          break;
        } else { // If found the first comment the area
          position.line = line;
        }
      }
    }

    return position;
  }

  /**
   * Checks that this source doesn't have this type imported in any form,
   * thus it needs it to be imported to satisfy dependencies.
   *
   * @param source source file we are checking in
   * @param member which we want to have imported if needed
   * @param aPackage new package of a type -- if that package is already
   * imported then the type is considered as imported
   * (so shadowing by FQN imports is not considered here).
   */
  public static boolean needsTypeImported(CompilationUnit source,
      BinMember member,
      BinPackage aPackage) {
    BinCIType type = (member instanceof BinCIType)
        ? (BinCIType) member : member.getOwner().getBinCIType();

//    System.out.println(source);
    if (source == null) {
      return false;
    }

    if (source.getPackage().isIdentical(aPackage)
        && type.getPackage().isIdentical(aPackage) // not moving
        && !type.isInnerType()) { // and not inner
//      System.err.println("needsTypeImported1: " + source + ", type: " + type
//          + ", aPackage: " + aPackage + " - false");
      return false;
    }

    if (source.getPackage().isIdentical(aPackage)
        && source.importsOnlyOnDemand(type)) {
//      System.err.println("needsTypeImported2: " + source + ", type: " + type
//          + ", aPackage: " + aPackage + " - false");
      return false;
    }

//    if( source.getPackage().isIdentical(aPackage) && source.importsOnlyOnDemand(type)) {
//      return false;
//    }

    boolean result = (!source.definesType(type) && // FIXME could be a bug here
        // on Move type will be defined in source but AFTER refactoring
        !hasTypeImported(source,
        type.getQualifiedName(), aPackage));
//    System.err.println("needsTypeImported3: " + source + ", type: " + type
//        + ", aPackage: " + aPackage + " - " + result);
    return result;
  }

  /**
   * @param member member to import
   * @param targetType type to import into
   */
  public static boolean isAmbiguousImport(
      BinMember member, BinCIType targetType) {

    final CompilationUnit targetSource = targetType.getCompilationUnit();

    if (targetSource == null) {
      return false;
    }

    final BinCIType typeToImport = (member instanceof BinCIType)
        ? (BinCIType) member
        : member.getOwner().getBinCIType();
//    System.out.println("TYPE: " + targetType);
//    System.out.println("UNIT: " + targetSource);

    if (targetSource.importsTypeDirectly
        (typeToImport.getQualifiedName())) {
      return false;
    }

//    //Look at another implementation of static imports below.
//
//    if (member instanceof BinCIType) {
//    	if (((BinCIType) member).isInnerType() && ((BinCIType) member).isStatic()) {
//    		StaticImports staticImports = targetSource.getStaticImports();
//    		if (staticImports != null) {
//    			StaticImports.StaticImport staticImport = staticImports.getImport(member.getQualifiedName());
//    			if ((staticImport != null) && (staticImport instanceof StaticImports.SingleStaticImport)) {
//    				// single static import of inner static type -- not ambiguous
//    				return false;
//    			}
//
//    			if (staticImports.getSingleStaticImport(member.getQualifiedName()) != null) {
//    				// there's another single static import that imports a member with the same name
//    				return true;
//    			}
//    		}
//    	}
//    }
//
//    //TODO: also on-demand static imports

    final List importedNames = targetSource.getImportedTypeNames();
    if (importedNames != null) {
      for (int i = 0, max = importedNames.size(); i < max; i++) {
        String importedName = (String) importedNames.get(i);
        String typeName = importedName.substring(importedName.lastIndexOf('.')
            + 1);
        if (typeToImport.getName().equals(typeName)) {
          return true;
        }
      }
    }

    // static imports
    StaticImports staticImports = targetSource.getStaticImports();
    if (staticImports != null){
      final BinTypeRef singleImportType = staticImports.getSingleImportType(
          typeToImport.getName());
      if (singleImportType != null){
        // If this type is already imported through static import -> false
        // If there is another static import of type with same name -> true
        return !singleImportType.getBinType().equals(typeToImport);
      }

      final List staticOnDemandImports = staticImports.getOnDemandImports();
      BinTypeRef onDemandType;
      if (staticOnDemandImports != null){
        for (int i = 0, max = staticOnDemandImports.size(); i < max; i++){
          onDemandType = ((StaticImports.OnDemandStaticImport)
              staticOnDemandImports.get(i)).getType(typeToImport.getName());
          if (onDemandType != null
              && !onDemandType.equals(typeToImport.getTypeRef())){
            return true; // another type with this name is imported on demand
          }
        }
      }
    }
    // end of static impoorts

    if (targetType.getPackage() == typeToImport.getPackage()) {
      return false;
    }

    List importedPackages = targetSource.getImportedPackages();
    for (int i = 0, max = importedPackages.size(); i < max; i++) {
      BinPackage importedPackage = (BinPackage) importedPackages.get(i);

      if (typeToImport.getPackage() == importedPackage) {
        continue;
      }

      Iterator iter = importedPackage.getAllTypes();
      while (iter.hasNext()) {
        BinCIType type = ((BinTypeRef) iter.next()).getBinCIType();
        if (type.getName().equals(typeToImport.getName())
            && targetSource.importsOnlyOnDemand(type)) {
          return true;
        }
      }
    }

    Iterator iter = targetType.getPackage().getAllTypes();
    while (iter.hasNext()) {
      BinCIType type = ((BinTypeRef) iter.next()).getBinCIType();
      if (type.getName().equals(typeToImport.getName()) && !type.isInnerType()){
        return true;
      }
    }

    return false;
  }

  /**
   * Checks that this source has this type imported,
   * thus it does _not_ need it to be imported to satisfy dependencies.
   *
   * @param source source file we are checking in
   * @param fqn the name of the type whose being imported we are checking
   * @param aPackage new package of a type -- if that package is already
   * imported then the type is considered as imported (shadowing by other FQN imports is not considered here)
   */
  public static boolean hasTypeImported(final CompilationUnit source,
  		final String fqn, final BinPackage aPackage) {
  	return source.importsTypeDirectly(fqn) || source.importsPackage(aPackage)
		|| ((source.getStaticImports() != null) && (source.getStaticImports().getImport(fqn) != null));
  }

  public static StringBuffer generateNewlines(int amount) {
    if (amount < 0) {
      amount = 0;
    }

    StringBuffer buf = new StringBuffer(amount);

    for (int i = 0; i < amount; i++) {
      buf.append(FormatSettings.LINEBREAK); // NOTE: don't care here about correct
      // newline, will be replaced by SourceEditor
    }

    return buf;
  }

  public static ASTImpl getImportNode(final CompilationUnit source,
      final ASTImpl node) {
    if (Assert.enabled) {
      Assert.must(source != null && node != null,
          "Couldn't get import node for null node");
    }

    ASTImpl result = node;
    while (result != null && result.getType() != JavaTokenTypes.IMPORT && result.getType() != JavaTokenTypes.STATIC_IMPORT) {
      result = result.getParent();
    }

    return result;
  }

  public static List getImportNodes(List nodes, CompilationUnit compilationUnit) {
    List result = new ArrayList(nodes.size());

    for (int i = 0; i < nodes.size(); i++) {
      result.add(getImportNode(compilationUnit, (ASTImpl) nodes.get(i)));
    }

    return result;
  }

  public static boolean isChildOfImportNode(CompilationUnit source, ASTImpl node) {
    return getImportNode(source, node) != null;
  }

  public static List extractAllTypeRefManagers(final BinMember member){
    final List result = new ArrayList(10);
    SinglePointVisitor typeFinder = new SinglePointVisitor() {
      public void onEnter(Object o) {
        if (o instanceof BinTypeRefManager) {
          result.add(o);
        }
      }

      public void onLeave(Object o) {}
    };

    member.accept(typeFinder);
    return result;
  }

  public static Set extractTypesFromTypeRefManagers(
      final List typeRefManagers){
    final Set types = new HashSet();
    class TypeRefVisitor extends BinTypeRefVisitor {
      TypeRefVisitor(){
        setCheckTypeSelfDeclaration(false);
        setIncludeNewExpressions(true);
      }

      public void visit(BinTypeRef type){
        types.add(type);
      }
    };
    TypeRefVisitor visitor = new TypeRefVisitor();
    for (int i = 0, i_max = typeRefManagers.size(); i < i_max; i++){
      ((BinTypeRefManager) typeRefManagers.get(i)).accept(visitor);
    }
    return types;
  }

  private static Set getPackagesForTypes(Set typeRefSet) {
    Set result = new HashSet();
    for (Iterator i = typeRefSet.iterator(); i.hasNext(); ) {
      BinTypeRef ref = (BinTypeRef) i.next();
      result.add(ref.getPackage());
    }

    return result;
  }

  /**
   * @param compilationUnit compilation unit to list unused imports for
   * @param discardedUsages set of usages, that should not be counted as type
   *    usage
   *
   * @return ast node array of unused imports
   */
  public static ASTImpl[] listUnusedImports(CompilationUnit compilationUnit) {
    TypesUsedThroughImportFinder finder = new TypesUsedThroughImportFinder();
    Set validTypes = finder.findFor(compilationUnit);

    ArrayList result = new ArrayList();

    List packageNodes = compilationUnit.getImportedPackageNodes();
    List typeNameNodes = compilationUnit.getImportedTypeNameNodes();

    Set foundSingleImport = new HashSet();

    Set existingTypeImportString = new HashSet();
    for (int i = 0; i < typeNameNodes.size(); i++) {
      ASTImpl node = (ASTImpl) typeNameNodes.get(i);
      if(node.getSource() != compilationUnit.getSource()) {
        // avoid imports from other jsp page areas
        continue;
      }
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
      // avoid imports from other jsp page areas
      if (node == null || node.getSource() != compilationUnit.getSource()) {
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
    }
    ;
    return (ASTImpl[]) result.toArray(new ASTImpl[result.size()]);
  }

  public static boolean dotExpressionContainsAsterisk(final ASTImpl dotNode) {
    ASTImpl node = dotNode;

    while (node.getType() == JavaTokenTypes.DOT) {
      if ("*".equals(node.getFirstChild().getNextSibling().getText())) {
        return true;
      }

      node = (ASTImpl) node.getFirstChild();
    }

    if (node.getType() == JavaTokenTypes.IDENT && ("*".equals(node.getText()))) {
      return true;
    }

    return false;
  }

  public static List getAllSingleStaticImports(BinMember member) {

  	final String qualifiedName = member.getQualifiedName();
  	final List result = new ArrayList();

  	AbstractIndexer indexer = new AbstractIndexer() {
  		public void visit(CompilationUnit u) {
  			StaticImports staticImports = u.getStaticImports();
  			if (u != null) {
  				List singleStatics = staticImports.getSingleImports();
  				if (singleStatics != null) {
  					for (Iterator iter = singleStatics.iterator(); iter.hasNext();) {
  						StaticImports.SingleStaticImport singleStatic = (StaticImports.SingleStaticImport) iter.next();
  						if (qualifiedName.equals(singleStatic.getQualifiedName())) {
  							result.add(singleStatic);
  						}
  					}
  				}
  			}
  		}
  	};
  	indexer.visit(member.getProject());
  	return result;
  }

  /**
   * Removes single static imports that would get invalid if the given member is moved
   * away from the class. Single static import is invalid if there are no accessible
   * static members having that name.
   *
   * @param transList
   * @param movedMember
   */
  public static void removeInvalidSingleStaticImports(TransformationList transList, BinMember movedMember) {
  	boolean hasSameNamedPublicMembers = false;
  	boolean hasSameNamedNonPrivateMembers = false;
  	BinCIType type = movedMember.getOwner().getBinCIType();
  	List members = new ArrayList();
  	BinField field = type.getAccessibleField(movedMember.getName(), type);
  	if ((field != null) && (field != movedMember)) {
  		members.add(field);
  	}
  	BinMethod [] methods = type.getAccessibleMethods(movedMember.getName(), type);
  	if ((methods != null)) {
  		members.addAll(Arrays.asList(methods));
  	}
  	List types = type.getAccessibleInners(type);
  	if ((types != null)) {
  		members.addAll(types);
  	}
  	for (Iterator iter = members.iterator(); iter.hasNext();) {
  		BinMember member = (BinMember) iter.next();
  		if ((member.isStatic()) && (member.getName().equals(movedMember.getName())) && (member != movedMember)) {
  			if (member.isPublic()) {
  				hasSameNamedPublicMembers = true;
  				hasSameNamedNonPrivateMembers = true;
  				break;
  			} else if (!hasSameNamedNonPrivateMembers && member.isProtected() || member.isPackagePrivate()) {
  				hasSameNamedNonPrivateMembers = true;
  			}
  		}
  	}
  	if (!hasSameNamedPublicMembers) {
  		List allSingleStaticImports = ImportUtils.getAllSingleStaticImports(movedMember);
  		if (allSingleStaticImports.size() > 0) {

  			for (Iterator iter = allSingleStaticImports.iterator(); iter.hasNext();) {
  				StaticImports.SingleStaticImport singleStatic = (StaticImports.SingleStaticImport) iter.next();
  				CompilationUnit c = singleStatic.getCompilationUnit();
  				if (!hasSameNamedNonPrivateMembers || (!c.getPackage().isIdentical(movedMember.getPackage()))) {
  					ASTImpl importNode = ImportUtils.getImportNode(c, singleStatic.getMemberNameNode());
  					if (importNode != null) {
  						CompoundASTImpl compoundNode = new CompoundASTImpl(importNode);
  						transList.add(new StringEraser(c, compoundNode, true));
  					}
  				}
  			}
  		}
  	}
  }

}
