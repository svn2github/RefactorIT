/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extractsuper;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.AdaptiveMultiValueMap;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.ConstructorIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.source.ImportResolver;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.DirCreator;
import net.sf.refactorit.source.edit.FileCreator;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.MoveEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinComparator;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * Extracts superclass/interface basing on the given subtype and user selection
 * of members of that subtype.
 *
 * @author Anton Safonov
 */
public class ExtractSuper extends AbstractRefactoring {
  public static String key = "refactoring.extractsuper";

  private BinTypeRef typeRef;

  private String newTypeName;
  private String newPackageName;
  private boolean extractWithOldName = false;
  private boolean extractClass = true;
  private boolean forceExtractMethodsAbstract = false;
  private boolean convertPrivates = false;
  private int modifier = BinModifier.PUBLIC;
  private String linebreak = FormatSettings.LINEBREAK;
  private List membersToExtract = new ArrayList(0);

  private Set explicitlyAbstractMethods = new HashSet();

  private AdaptiveMultiValueMap usesMap = new AdaptiveMultiValueMap();
  private AdaptiveMultiValueMap usedByMap = new AdaptiveMultiValueMap();
  private final String packageName;

  public ExtractSuper(RefactorItContext context, BinTypeRef typeRef) {
    super("Extract Super", context);

    this.typeRef = typeRef;
    packageName = savePackageName();

    calculateInterMemberDependencies();
  }

  public BinTypeRef getTypeRef() {
    return typeRef;
  }

  public List getMembersToExtract() {
    return this.membersToExtract;
  }

  public void setMembersToExtract(final List membersToExtract) {
    this.membersToExtract = membersToExtract;
  }

  public String getNewTypeName() {
    return this.newTypeName;
  }

  public String getNewPackageName() {
    return this.newPackageName;
  }

  public String getOldPackageName() {
    return this.packageName;
  }

  public void setNewTypeName(final String newTypeName) {
    this.newTypeName = newTypeName;
  }

  public void setNewPackageName(final String newPackageName) {
    this.newPackageName = newPackageName;
  }

  public void setExtractClass(final boolean extractClass) {
    this.extractClass = extractClass;
  }

  public boolean isExtractClass() {
    return this.extractClass;
  }

  public void setForceExtractMethodsAbstract(
      final boolean forceExtractMethodsAbstract) {
    this.forceExtractMethodsAbstract = forceExtractMethodsAbstract;
  }

  public boolean isForceExtractMethodsAbstract() {
    return this.forceExtractMethodsAbstract;
  }

  public boolean isExtractAbstract() {
    if (isForceExtractMethodsAbstract()) {
      return true;
    }

    if (calculateAbstractMethods().size() > 0) {
      return true;
    }

    for (int i = 0; i < membersToExtract.size(); i++) {
      final BinMember member = (BinMember) membersToExtract.get(i);
      if (member.isAbstract()) {
        return true;
      }
    }

    return false;
  }

  public void setConvertPrivate(final boolean convertPrivates) {
    this.convertPrivates = convertPrivates;
  }

  public boolean isConvertPrivate() {
    return this.convertPrivates;
  }

  public void setExplicitlyAbstractMethods(Set explicitlyAbstractMethods) {
    this.explicitlyAbstractMethods = explicitlyAbstractMethods;
  }

  public Set getExplicitlyAbstractMethods() {
    return this.explicitlyAbstractMethods;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkPreconditions
   */
  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();
    BinCIType type = typeRef.getBinCIType();

    if (!type.isFromCompilationUnit()) {
      status.addEntry("Refactoring is not possible since " +
          type.getMemberType() + " " + type.getQualifiedName() +
          " is outside of the source path!", RefactoringStatus.ERROR);
    }

    return status;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkUserInput
   */
  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();

    if (newTypeName == null || newTypeName.trim().length() == 0) {
      status.addEntry("No name given for the new type.",
          RefactoringStatus.ERROR);
    }

//    if (membersToExtract.size() == 0) {
//      status.addEntry("No members selected for extraction.",
//          RefactoringStatus.ERROR);
//    }

    status.merge(checkForSuchTypeInThisPackage());

    status.merge(checkForImportConflictsInBaseType());

    final List cnstrs = findConstructorsToProxy();
    final Set abstr = calculateAbstractMethods();
    status.merge(checkForImportConflictsInNewType(cnstrs, abstr));

    status.merge(checkForInnersUsage(abstr));

    if (newPackageName.length() > 0) {
      if (!NameUtil.isValidPackageName(newPackageName)) {
        status.addEntry("Invalid package name", RefactoringStatus.ERROR);
      }
    }
    return status;
  }

  private RefactoringStatus checkForInnersUsage(final Set abstractMethods) {
    RefactoringStatus status = new RefactoringStatus();

    List inners
        = typeRef.getBinCIType().getAccessibleInners(typeRef.getBinCIType());

    if (inners.size() == 0) {
      return status;
    }

    // if inners owner will be migrated to new type, then it will be accessible still
    List types = getMigratedTypes();
    Iterator it = inners.iterator();
    while (it.hasNext()) {
      BinTypeRef owner = ((BinCIType) it.next()).getOwner();
      while (owner != null) {
        if (types.contains(owner)) {
          it.remove();
          break;
        }
        owner = owner.getBinCIType().getOwner();
      }
    }

    ManagingIndexer supervisor = new ManagingIndexer();
    for (int i = 0; i < inners.size(); i++) {
      new TypeNameIndexer(supervisor, (BinCIType) inners.get(i), true);
    }

    for (int i = 0; i < this.membersToExtract.size(); i++) {
      final Object member = this.membersToExtract.get(i);
      if (abstractMethods.contains(member)) {
        continue;
      }

      if (member instanceof BinField) {
        final BinExpression expr = ((BinField) member).getExpression();
        if (expr != null) {
          supervisor.visit(expr);
        }
      } else {
        supervisor.visit((BinMethod) member);
      }
    }

    List used = supervisor.getInvocations();
    List where = new ArrayList(used.size());
    for (int i = 0; i < used.size(); i++) {
      final InvocationData invocation = (InvocationData) used.get(i);
      CollectionUtil.addNew(where, invocation.getWhere());
    }

    it = abstractMethods.iterator();
    while (it.hasNext()) {
      final BinMethod abstractMethod = (BinMethod) it.next();
      BinType type = abstractMethod.getReturnType().getBinType();

      if (type instanceof BinArrayType) {
        type = ((BinArrayType) type).getArrayType().getBinType();
      }

      if (inners.contains(type)) {
        CollectionUtil.addNew(where, abstractMethod);
      }
      BinParameter[] params = abstractMethod.getParameters();
      for (int i = 0; i < params.length; i++) {
        if (inners.contains(params[i].getTypeRef().getBinType())) {
          CollectionUtil.addNew(where, abstractMethod);
          break;
        }
      }
      BinMethod.Throws[] throwses = abstractMethod.getThrows();
      for (int i = 0; i < throwses.length; i++) {
        if (inners.contains(throwses[i].getException().getBinType())) {
          CollectionUtil.addNew(where, abstractMethod);
          break;
        }
      }
    }

    if (where.size() > 0) {
      status.addEntry(
          "Inner types of the base type are used in extractable members",
          where,
          RefactoringStatus.ERROR);
    }

    return status;
  }

  private RefactoringStatus checkForImportConflictsInNewType(
      final List constructorsToProxy,
      final Set abstractMethods) {
    RefactoringStatus status = new RefactoringStatus();

    List imports = collectImports(constructorsToProxy, abstractMethods);
    for (int i = 0; i < imports.size(); i++) {
      final BinTypeRef importRef = (BinTypeRef) imports.get(i);
      if (newTypeName.equals(importRef.getName())) {
        addTypeRefEntry(status,
            "Conflict between new type name and a generated import",
            importRef);
      }
      for (int k = 0; k < imports.size(); k++) {
        final BinTypeRef anotherRef = (BinTypeRef) imports.get(k);
        if (i == k) {
          continue;
        }
        if (importRef.getName().equals(anotherRef.getName())) {
          List items = new ArrayList(2);
          items.add(importRef.getBinCIType());
          items.add(anotherRef.getBinCIType());
          status.addEntry("Conflict between several generated imports",
              items, RefactoringStatus.ERROR);
        }
      }
    }

    return status;
  }

  private RefactoringStatus checkForImportConflictsInBaseType() {
    RefactoringStatus status = new RefactoringStatus();

    final ImportResolver resolver = typeRef.getBinCIType().getCompilationUnit()
        .getImportResolver();
    try {
      // there will be only one if any, or there had to be ambiguity conflicts
      // already
      BinTypeRef importedType = resolver.resolve(newTypeName);
      if (importedType != null
          && !getProject().getPackageForName(newPackageName)
          .isIdentical(importedType.getPackage())) {
        // FIXME is it better to show import location instead of the imported type?
        // refactor from RenameType
        addTypeRefEntry(status,
            "There is imported type with such name in the old type",
            importedType);
      }
    } catch (Exception e) {
      status.addEntry(e.getMessage(), RefactoringStatus.WARNING);
    }

    return status;
  }

  private RefactoringStatus checkForSuchTypeInThisPackage() {
    RefactoringStatus status = new RefactoringStatus();

    Project project = getProject();

    BinPackage pack = project.getPackageForName(this.newPackageName);

    if (pack == null) {
      pack = project.createPackageForName(this.newPackageName);
    }

    if (pack == null) {
      status.addEntry( "Unable to create package for name: " +
          this.newPackageName, RefactoringStatus.ERROR);
      return status;
    }

    BinTypeRef existingTypeInThisPackage;

    if (!extractWithOldName || !newPackageName.equals(packageName)) {
      existingTypeInThisPackage
          = pack.findTypeForShortName(getSupertypeName());

      if (existingTypeInThisPackage != null) {
        addTypeRefEntry(status,
            "Type with such name already exists in the target package",
            existingTypeInThisPackage);
      }
    }

    if (extractWithOldName) {
      pack = project.getPackageForName(packageName);

      existingTypeInThisPackage
          = pack.findTypeForShortName(getSubtypeName());

      if (existingTypeInThisPackage != null) {
        addTypeRefEntry(status,
            "Type with such name already exists in the target package",
            existingTypeInThisPackage);
      }
    }
    return status;
  }

  private static final void addTypeRefEntry(final RefactoringStatus status,
      final String entry,
      final BinTypeRef typeRef) {
    List items = new ArrayList(1);
    items.add(typeRef.getBinCIType());
    status.addEntry(entry, items, RefactoringStatus.ERROR);
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#performChange
   */
  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    if (extractWithOldName){
      renameTargetType(transList);
    }

    Project project = getProject();
    BinPackage pack = project.getPackageForName(newPackageName);

    SourceHolder source = new SimpleSourceHolder(project);
    source.setPackage(pack);

    Source packDir = pack.getDir();

    SimpleSourceHolder targetDir;

    if (packDir != null) {
      targetDir = new SimpleSourceHolder(packDir, project);
    } else {
      targetDir = new SimpleSourceHolder(project);

      Source[] src = project.getPaths().getSourcePath().getRootSources();
      SourceHolder sr = new SimpleSourceHolder(src[0], project);

      transList.add(new DirCreator(sr, targetDir, newPackageName, true));
    }

    linebreak = StringUtil.findEndOfLine(
        typeRef.getBinCIType().getCompilationUnit().getContent());

    transList.add(new FileCreator(source, targetDir, getSupertypeName(),
        FileCreator.CREATE_SINGLE_FILE));

    List cnstrs = findConstructorsToProxy();
    Set abstr = calculateAbstractMethods();

    changeOldTypeDeclaration(transList);

    addHeaderFooter(transList, source, collectImports(cnstrs, abstr));
    writeMembers(transList, source, cnstrs, abstr);

    return transList;
  }

  /**
   * @param transList
   */
  private void renameTargetType(final TransformationList transList) {
    final RenameType renameType = new RenameType(getContext(), typeRef
        .getBinCIType());
    renameType.setRenameInNonJavaFiles(true);
    renameType.setNewName(newTypeName);
    transList.merge(renameType.performChange());
  }

  public String getSupertypeName() {
    return (extractWithOldName ? typeRef.getName() : this.newTypeName);
  }

  public String getSupertypeQualifiedName() {
    return (newPackageName.length() > 0 ? newPackageName + "." : "")
        + getSupertypeName();
  }

  public String getSubtypeName(){
    return (!extractWithOldName ? typeRef.getName() : this.newTypeName);
  }

  public String getSubtypeQualifiedName(){
    return (packageName.length() > 0 ? packageName + "." : "")
        + getSubtypeName();
  }

  private static List getMigratedTypeParams(List migratedTypes) {
    ArrayList typeParams = new ArrayList(3);
    for (int i = 0, max = migratedTypes.size(); i < max; i++) {
      BinTypeRef migratedType = (BinTypeRef) migratedTypes.get(i);
      BinTypeRef[] args = migratedType.getTypeArguments();
      if (args != null) {
        for (int k = 0, maxK = args.length; k < maxK; k++) {
          if (args[k].getBinType().isTypeParameter()) {
            CollectionUtil.addNew(typeParams, args[k]);
          }
        }
      }
    }

    return typeParams;
  }

  private void addHeaderFooter(final TransformationList transList,
      final SourceHolder source,
      final List imports) {
    final String header = formClassHeader(imports, false);
    transList.add(new StringInserter(source, 1, 0, header));

    final String footer = formClassFooter(false);
    transList.add(new StringInserter(source, -1, 0, footer));
  }

  private String formClassHeader(final List imports, final boolean preview) {
    StringBuffer result = new StringBuffer();

    String packageName = this.newPackageName;
    if (packageName.trim().length() > 0) {
      result.append("package ").append(packageName).append(';')
          .append(linebreak);
      result.append(linebreak);
    }

    if (!preview) {
      for (int i = 0; i < imports.size(); i++) {
        final BinTypeRef importRef = (BinTypeRef) imports.get(i);
        result.append(
            ImportUtils.generateImportClause(importRef.getQualifiedName())
            .toString());
        result.append(linebreak);
      }
      if (imports.size() > 0) {
        result.append(linebreak);
      }
    }

    if (!preview) {
      result.append(linebreak);
    }
    BinModifierFormatter modifierFormatter =
        new BinModifierFormatter(this.modifier);
    modifierFormatter.needsPostfix(true);
    result.append(modifierFormatter.print());

    if (isExtractClass() && isExtractAbstract()) {
      result.append("abstract ");
    }

    List superTypes = getMigratedTypes();

    result.append((extractClass ? "class " : "interface "));
    result.append(getSupertypeName());
    if (FastJavaLexer.getActualJvmMode() == FastJavaLexer.JVM_50) {
      List typeParams = getMigratedTypeParams(superTypes);
      if (typeParams != null && typeParams.size() > 0) {
        result.append("<");
        for (int i = 0, max = typeParams.size(); i < max; i++) {
          if (i != 0) {
            result.append(", ");
          }
          BinTypeRef typeParam = (BinTypeRef) typeParams.get(i);
          typeParam = typeParam.getBinCIType().getSelfUsageInfo(); // this looks like a hack actually
          ASTImpl node = typeParam.getNode();
          if (node == null) {
            result.append(BinFormatter.formatNotQualified(typeParam));
          } else {
            while (node != null && node.getType() != JavaTokenTypes.TYPE_PARAMETER) {
              node = node.getParent();
            }
            result.append(node.getSource().getText(node));
          }
        }
        result.append(">");
      }
    }
    result.append(' ');

    StringBuffer extend = null;
    StringBuffer implement = null;
    for (int i = 0, max = superTypes.size(); i < max; i++) {
      final BinTypeRef currentTypeRef = (BinTypeRef) superTypes.get(i);

      if (currentTypeRef.getBinCIType().isClass()) {
        if (extend == null) {
          extend = new StringBuffer("extends ");
        } else {
          extend.append(", ");
        }
        extend.append(BinFormatter.formatNotQualified(currentTypeRef));

        // FIXME: remove this after BinFormatter is fixed properly
        if (!extend.toString().endsWith(">")) {
          BinFormatter.formatTypeArguments(extend, currentTypeRef
              .getTypeArguments(), true);
        }
      } else {
        if (implement == null) {
          if (extractClass) {
            implement = new StringBuffer("implements ");
          } else {
            implement = new StringBuffer("extends ");
          }
        } else {
          implement.append(", ");
        }
        implement.append(BinFormatter.formatNotQualified(currentTypeRef));

        // FIXME: remove this after BinFormatter is fixed properly
        if (!implement.toString().endsWith(">")) {
          BinFormatter.formatTypeArguments(implement, currentTypeRef.getTypeArguments(), true);
        }
      }
    }

    if (extend != null) {
      result.append(extend.toString());
      if (implement != null) {
        result.append(' ');
      }
    }
    if (implement != null) {
      result.append(implement.toString());
    }

    if (FormatSettings.isNewlineBeforeBrace()) {
      result.append(linebreak);
    } else {
      if (extend != null || implement != null) {
        result.append(' ');
      }
    }

    result.append("{").append(linebreak);

    //System.err.println("header: " + result);
    return result.toString();
  }

  private void writeMembers(final TransformationList transList,
      final SourceHolder targetSource,
      final List constructorsToProxy,
      final Set abstrMethods) {

    // change modifier first
    Iterator privatesIt = getPrivateMembersToConvert().iterator();
    while (privatesIt.hasNext()) {
      final BinMember toChange = (BinMember) privatesIt.next();
      transList.add(
          new ModifierEditor(toChange,
          BinModifier.setFlags(toChange.getModifiers(),
          BinModifier.PROTECTED)));
    }

    List toGenerate = new ArrayList();
    toGenerate.addAll(constructorsToProxy);
    toGenerate.addAll(abstrMethods);
    Collections.sort(toGenerate, BinComparator.getInstance());
    for (int i = 0, max = toGenerate.size(); i < max; i++) {
      final BinMethod generatable = (BinMethod) toGenerate.get(i);

      // FIXME what's the??? must take indent from the originating type!
      String indent = FormatSettings.getIndentString(FormatSettings.
          getBlockIndent());

      String str = "";
      if (i == 0) {
        str += linebreak;
      }

      List comments = Comment.findAllFor(generatable);
      for (int j = 0, maxJ = comments.size(); j < maxJ; j++) {
        str += indent;
        str += (((Comment) comments.get(j)).getText() +
            FormatSettings.LINEBREAK);
      }

      if (generatable instanceof BinConstructor) {
        str += formProxyConstructor((BinConstructor) generatable, false);
      } else {
        str += indent;
        str += formMemberSignature(generatable, false, true);
        str += linebreak;
      }

      if (i < max - 1) {
        str += linebreak;
      }

      transList.add(new StringInserter(targetSource, 1, 0, str));
    }

    List toMove = new ArrayList();
    for (int i = 0, max = this.membersToExtract.size(); i < max; i++) {
      final BinMember member = (BinMember)this.membersToExtract.get(i);

      if (toGenerate.contains(member)) {
        continue;
      }

      if (extractClass || (member instanceof BinField)
      		|| (typeRef.getBinCIType().isInterface())) { // otherwise signatures were already generated above
        toMove.addAll(Comment.findAllFor(member));
        toMove.add(member);
      }
    }

    Collections.sort(toMove, LocationAware.PositionSorter.getInstance());

    if (toMove.size() > 0) {
      final MoveEditor mover = new MoveEditor(toMove, targetSource,
          new SourceCoordinate(1, 0), FormatSettings.getBlockIndent());
      transList.add(mover);
    }
  }

  private String formMemberSignature(final BinMember member,
      final boolean preview, boolean abstractMethod) {
    StringBuffer signature = new StringBuffer(128);

    if (isForceExtractMethodsAbstract()) {
      abstractMethod = true;
    }

    List privatesToBeProtected = getPrivateMembersToConvert();

    if (member instanceof BinMethod) {
      String modif = "";
      if (extractClass) {
        BinModifierBuffer modBuf = new BinModifierBuffer(member.getModifiers());

        if (abstractMethod) {
          modBuf.clearFlags(BinModifier.SYNCHRONIZED);
        }
        modif += new BinModifierFormatter(modBuf.getModifiers()).print();
        if (abstractMethod && modif.indexOf("abstract") < 0) {
          int pos = modif.indexOf(' ');
          if (pos > 0) {
            modif = modif.substring(0, pos) + " abstract" + modif.substring(pos);
          } else {
            modif += " abstract";
          }
        }
        if (abstractMethod || privatesToBeProtected.contains(member)) {
          modif = StringUtil.replace(modif, "private", "protected");
        }
        modif = modif.trim();
        if (modif.length() > 0) {
          signature.append(modif).append(' ');
        }
      }

      if (!(member instanceof BinConstructor)) {
        signature.append(
            BinFormatter.format(((BinMethod) member).getReturnType()));
        signature.append(' ');
      }

      signature.append(member.getName()).append('(');
      BinParameter[] params = ((BinMethod) member).getParameters();
      for (int p = 0; p < params.length; p++) {
        if (p > 0) {
          signature.append(", ");
        }
        if (params[p].getModifiers() == BinModifier.FINAL) {
          signature.append("final ");
        }
        signature.append(BinFormatter.format(params[p].getTypeRef()));
        signature.append(' ').append(params[p].getName());
      }
      signature.append(')');
      if (!preview) {
        BinMethod.Throws[] throwses = ((BinMethod) member).getThrows();
        if (throwses.length > 0) {
          signature.append(" throws ");
        }
        for (int t = 0; t < throwses.length; t++) {
          if (t > 0) {
            signature.append(", ");
          }
          signature.append(BinFormatter.format(throwses[t].getException()));
        }
      }
    } else if (member instanceof BinVariable) { //FIXME: Why not BinField
      String modif = new BinModifierFormatter(member.getModifiers()).print();
      if (privatesToBeProtected.contains(member)) {
        modif = StringUtil.replace(modif, "private", "protected");
      }

      if (modif.length() > 0) {
        signature.append(modif);
        signature.append(' ');
      }

      signature.append(
          BinFormatter.format(((BinVariable) member).getTypeRef()));
      signature.append(' ').append(member.getName());
      // TODO copy init expression
    }

    if (extractClass && member instanceof BinMethod && !abstractMethod
        && !member.isAbstract()) {
      signature.append(" {...}");
    } else {
      signature.append(";");
    }

    return signature.toString();
  }

  private String formClassFooter(boolean preview) {
    StringBuffer result = new StringBuffer();
    if (!preview) {
      result.append(linebreak);
    }
    result.append('}').append(linebreak);

    //System.err.println("footer: " + result);
    return result.toString();
  }

  /**
   * Possible combinations of extends and implements (defines test cases):
   * Change\Old |   0      I      E     IE
   * -----------+------------------------------
   * +I         |   +I!    +I     +I!   +I
   * +E         |   +E!    +E!    ~E    ~E
   * +E-I       |   X      +E-I?  X     ~E-I?
   * +I-I       |   X      ~I     X     ~I
   *
   * Legend: ! - clause will be created, ? - clause may left/may be not,
   * ~ - reuse place, + - add, - - remove
   *
   * @param transList
   */
  private void changeOldTypeDeclaration(final TransformationList transList) {
    ImportManager importManager = new ImportManager();

    //Somewhat a hack, needed only for ImportManager.
    BinCITypeRef artificialTypeRef = new BinCITypeRef(
        newPackageName + "." + getSupertypeName(), null);

    try {
      importManager.addExtraImports(artificialTypeRef, typeRef);
    } catch (AmbiguousImportImportException e) {
      //if extracting with old name into another package
      CompilationUnit compilationUnit = typeRef.getCompilationUnit();

      String importClause = ImportUtils.generateImportClause(
            (artificialTypeRef).getQualifiedName()).toString();

      ImportUtils.ImportPosition importPosition
          = ImportUtils.calculateNewImportPosition(compilationUnit, false);

      ArrayList imports = new ArrayList();
      imports.add(artificialTypeRef);

      importManager.insertImportClauseToSource(transList, compilationUnit,
          importPosition, imports, 0, importClause);
    }

    importManager.createEditors(transList);

    class SuperClause implements Comparable {
      ASTImpl node = null;
      boolean extend; // "extends" or "implements"
      boolean used; // false signifies that place can be reused

      /** last place goes first (to remove correctly) */
      public int compareTo(Object o) {
        return ((SuperClause) o).node.compareTo(this.node);
      }
    }

    List migrated = getMigratedTypes();

    List placesToReuse = new ArrayList();

    SuperClause superClassPlace = null, superInterfacePlace = null;
    List typeDatas = typeRef.getBinCIType().getSpecificSuperTypeRefs();
    for (int i = 0, max = typeDatas.size(); i < max; i++) {
      final BinTypeRef data = (BinTypeRef) typeDatas.get(i);
      SuperClause place = new SuperClause();
      place.node = CompoundASTImpl.compoundQualifiedNameAST(data.getNode());
      place.extend = data.getTypeRef().getBinCIType().isClass();
      place.used = !migrated.contains(data.getTypeRef());
      if (place.extend) {
        superClassPlace = place;
      } else {
        if (place.used) {
          superInterfacePlace = place;
        }
      }
      CollectionUtil.addNew(placesToReuse, place);
    }

    Collections.sort(placesToReuse);

    String extractedSuperFullName = getSupertypeName();
    if (FastJavaLexer.getActualJvmMode() == FastJavaLexer.JVM_50) {
      List typeParams = getMigratedTypeParams(migrated);
      if (typeParams != null && typeParams.size() > 0) {
        extractedSuperFullName += "<";
        for (int i = 0, max = typeParams.size(); i < max; i++) {
          if (i != 0) {
            extractedSuperFullName += ", ";
          }
          BinTypeRef typeParam = (BinTypeRef) typeParams.get(i);
          extractedSuperFullName += BinFormatter.formatNotQualified(typeParam);
        }
        extractedSuperFullName += ">";
      }
    }

    // try to reuse existing place
    boolean addedSuper = false;
    for (int i = 0, max = placesToReuse.size(); i < max; i++) {
      final SuperClause place = (SuperClause) placesToReuse.get(i);
      if (place.used) {
        continue;
      }

      if ((place.extend && extractClass) || (!place.extend && !extractClass)) {
        place.used = true;
        transList.add(new RenameTransformation(
            typeRef.getBinCIType().getCompilationUnit(),
            CollectionUtil.singletonArrayList(place.node),
            extractedSuperFullName));
        addedSuper = true;
        break;
      }
    }

    String content = typeRef.getBinCIType().getCompilationUnit().getContent();
    LineIndexer indexer = typeRef.getBinCIType().getCompilationUnit()
      .getLineIndexer();

    int startLine = typeRef.getBinCIType().getNameAstOrNull().getStartLine();
    int startColumn = typeRef.getBinCIType().getNameAstOrNull().getStartColumn();
    int endLine = typeRef.getBinCIType().getBodyAST().getStartLine();
    int endColumn = typeRef.getBinCIType().getBodyAST().getStartColumn();

    int startPos = indexer.lineColToPos(startLine, startColumn);
    int endPos = indexer.lineColToPos(endLine, endColumn);

    content = StringUtil.replaceCommentsWithWhitespaces(content, startPos,
        endPos);

    // remove migrated types which places are not reused
    boolean someInterfacesAreLeft = false;
    for (int i = 0, max = placesToReuse.size(); i < max; i++) {
      final SuperClause place = (SuperClause) placesToReuse.get(i);
      if (place.used) {
        if (!place.extend) {
          someInterfacesAreLeft = true;
        }
        continue;
      }

      startLine = place.node.getStartLine();
      startColumn = place.node.getStartColumn();
      endLine = place.node.getEndLine();
      endColumn = place.node.getEndColumn();

      startPos = indexer.lineColToPos(startLine, startColumn);
      endPos = indexer.lineColToPos(endLine, endColumn);
//      System.err.println("********** " + startLine + ":" + startColumn
//          + " - " + endLine + ":" + endColumn + " - "
//          + " - " + startPos + ":" + endPos + " - \""
//          + content.substring(startPos, endPos) + "\"");

      if (!someInterfacesAreLeft) { // force it to remove the prefix clause
        String[] clauses = new String[] {"extends", "implements"};
        for (int k = 0; k < clauses.length; k++) {
          int pos = StringUtil.findPrefixWhitespace(content, startPos);
          if (pos - clauses[k].length() >= 0 &&
              clauses[k].equals(
              content.substring(pos - clauses[k].length(), pos))) {
            startPos = pos - clauses[k].length();
            break;
          }
        }
        startPos = StringUtil.findPrefixWhitespace(content, startPos);
      }

      endPos = StringUtil.findPostfixWhitespace(content, endPos);
      if (endPos < content.length() && content.charAt(endPos) == ',') {
        ++endPos;
        endPos = StringUtil.findPostfixWhitespace(content, endPos);
      }

      if (content.charAt(endPos) == '{' && content.charAt(endPos-1) == ' '){
        --endPos;
      }

      transList.add(
          new StringEraser(typeRef.getBinCIType().getCompilationUnit(),
          startPos, endPos));
    }

    // didn't succeeded to reuse anything, so let's just add it
    if (!addedSuper) {
      int line, column = -1;
      String prefix = "";
      if (extractClass) {
        prefix = " extends ";
        line = -1;
      } else {
        if (superInterfacePlace != null) {
          line = superInterfacePlace.node.getLine();
          column = superInterfacePlace.node.getColumn()
              + superInterfacePlace.node.getTextLength() - 1;
          prefix = ", ";
        } else {
          if (superClassPlace != null) {
            line = superClassPlace.node.getLine();
            column = superClassPlace.node.getColumn()
                + superClassPlace.node.getTextLength() - 1;
          } else {
            line = -1;
          }
          if (!typeRef.getBinCIType().isInterface()) {
            prefix = " implements ";
          } else {
            prefix = " extends ";
          }
        }
      }
      if (line == -1) {
        final ASTImpl nameAst = typeRef.getBinCIType().getNameAstOrNull();
        line = nameAst.getLine();
        column = nameAst.getColumn() + nameAst.getTextLength() - 1;
      }
      transList.add(
          new StringInserter(typeRef.getBinCIType().getCompilationUnit(),
          line, column, prefix + extractedSuperFullName));
    }
  }

  private List findConstructorsToProxy() {
    BinCIType type = typeRef.getBinCIType();
    if (!type.isClass()) {
      return new ArrayList(0);
    }

    List constructorPrototypes = new ArrayList();

    List superTypes = getMigratedTypes();
    BinTypeRef superclass = null;
    for (int i = 0, max = superTypes.size(); i < max; i++) {
      final BinTypeRef typeRef = (BinTypeRef) superTypes.get(i);
      if (typeRef.getBinCIType().isClass()) {
        superclass = typeRef;
        break;
      }
    }

    if (superclass == null) {
      return constructorPrototypes;
    }

    ManagingIndexer supervisor = new ManagingIndexer();
    BinConstructor[] cnstrs
        = ((BinClass) superclass.getBinCIType()).getDeclaredConstructors();
    for (int i = 0; i < cnstrs.length; i++) {
      new ConstructorIndexer(supervisor, cnstrs[i]);
    }

    BinConstructor[] declCnstrs = ((BinClass) type).getDeclaredConstructors();
    for (int i = 0; i < declCnstrs.length; i++) {
      supervisor.visit(declCnstrs[i]);
    }

    List invocations = supervisor.getInvocations();
    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData data = (InvocationData) invocations.get(i);
      final BinItem itemCalled = data.getWhat();
      if (Assert.enabled) {
        Assert.must(itemCalled != null, "Called constructor is null");
      }
      final Object whereCalled = data.getWhere();
      if (itemCalled instanceof BinConstructor
          && whereCalled instanceof BinConstructor) {
        final BinParameter[] params = ((BinConstructor) itemCalled).
            getParameters();
        if (params.length > 0 && params[0].getName() == null) { // from .class
          boolean replicateNames = true;

          BinParameter[] otherParams
              = ((BinConstructor) whereCalled).getParameters();

          for (int p = 0; p < params.length && p < otherParams.length; p++) {
            final BinTypeRef thisParameterType =
                params[p].getTypeRef();
            final BinTypeRef otherParameterType =
                otherParams[p].getTypeRef();

            if (!thisParameterType.equals(otherParameterType)) {
              replicateNames = false; // Parameter types don't match.
            }
          }

          if (replicateNames) {
            for (int p = 0; p < params.length && p < otherParams.length; p++) {
              params[p].setName(otherParams[p].getName());
            }
          }

        }
      }
      CollectionUtil.addNew(constructorPrototypes, itemCalled);
    }

    return constructorPrototypes;
  }

  private String formProxyConstructor(final BinConstructor prototype,
      final boolean preview) {
    StringBuffer constructorSource = new StringBuffer(128);

    if (!preview) {
      constructorSource.append(
          FormatSettings.getIndentString(FormatSettings.getBlockIndent()));
      constructorSource.append("/** Autogenerated proxy constructor. */");
      constructorSource.append(linebreak);
    }

    BinParameter[] sourceParams = prototype.getParameters();
    BinParameter[] params = new BinParameter[sourceParams.length];
    for (int i = 0; i < sourceParams.length; ++i) {
      String name = sourceParams[i].getName();
      if (name == null) {
        name = new StringBuffer().append((char) ('a' + i)).toString();
      }
      params[i] = new BinParameter(name,
          sourceParams[i].getTypeRef(), sourceParams[i].getModifiers());
    }
    BinConstructor newConstructor
        = new BinConstructor(params,
        prototype.getModifiers(),
        prototype.getThrows());
    newConstructor.setBody(null);

    // XXX looks quite dangerous!
//    newConstructor.setOwner(new BinCITypeRef(newTypeName, null) {
//      public Project getProject() {
//        return ExtractSuper.this.getProject();
//      }
//
//      // it will be used for indents only!!!
//      public BinCIType getBinCIType() {
//        return ExtractSuper.this.typeRef.getBinCIType();
//      }
//    });
    newConstructor.setName(getSupertypeName());

    BinMethodFormatter formatter = (BinMethodFormatter) newConstructor.
        getFormatter();
    constructorSource.append(formatter.formHeader());

    // TODO use formatter also
    constructorSource.append(
        FormatSettings.getIndentString(FormatSettings.getBlockIndent() * 2));
    constructorSource.append("super(");
    for (int p = 0; p < params.length; p++) {
      if (p > 0) {
        constructorSource.append(", ");
      }
      constructorSource.append(params[p].getName());
    }
    constructorSource.append(");").append(linebreak);

    constructorSource.append(formatter.formFooter());

    String result = constructorSource.toString();

    if (!FormatSettings.LINEBREAK.equals(linebreak)) {
      result = StringUtil.replace(result, FormatSettings.LINEBREAK, linebreak);
    }

    return result;
  }

  /**
   * @return types which will be moved from type declaration to supertype.
   */
  private List getMigratedTypes() {
    // we will migrate only explicitly used types
    List typeDatas = typeRef.getBinCIType().getSpecificSuperTypeRefs();
    ArrayList explicitSuperTypes = new ArrayList(typeDatas.size());
    for (int i = 0, max = typeDatas.size(); i < max; i++) {
      final BinTypeRef data = (BinTypeRef) typeDatas.get(i);
      explicitSuperTypes.add(data);
    }

    ArrayList migratedTypes = new ArrayList(3);

    // only one type fits to "extends" clause, so old one MUST go :)
    if (extractClass && explicitSuperTypes.contains(typeRef.getSuperclass())) {
      CollectionUtil.addNew(migratedTypes, typeRef.getSuperclass());
    }

    List overrides = getOverriden();
    for (int i = 0, max = overrides.size(); i < max; i++) {
      final BinMethod overriden = (BinMethod) overrides.get(i);
      BinTypeRef owner = overriden.getOwner();

      // we need a specific type ref to extend, but owner is a simple one
      boolean found = false;
      for (int k = 0, kMax = explicitSuperTypes.size(); k < kMax; k++) {
        if (explicitSuperTypes.get(k).equals(owner)) {
          owner = (BinTypeRef) explicitSuperTypes.get(k);
          found = true;
          break;
        }
      }

      if (found) {
        if (extractClass || owner.getBinCIType().isInterface()) {
          CollectionUtil.addNew(migratedTypes, owner);
        }
      }
    }

    return migratedTypes;
  }

  /**
   * Selected methods which override/implement some super.
   *
   * @return both overrides from classes and implements from interfaces.
   */
  private List getOverriden() {
    List overrides = new ArrayList();

    for (int i = 0, max = this.membersToExtract.size(); i < max; i++) {
      final BinMember member = (BinMember)this.membersToExtract.get(i);
      if (member instanceof BinMethod) {
        List overriden = ((BinMethod) member).findOverrides();
        CollectionUtil.addAllNew(overrides, overriden);
      }
    }

    return overrides;
  }

  /**
   * All methods in this type which override/implement some super.
   *
   * @return both overrides from classes and implements from interfaces.
   */
  private List getAllOverriden() {
    List overrides = new ArrayList();

    BinMethod[] methods = typeRef.getBinCIType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      final BinMember member = methods[i];
      if (member instanceof BinMethod) {
        List overriden = ((BinMethod) member).findOverrides();
        CollectionUtil.addAllNew(overrides, overriden);
      }
    }

    return overrides;
  }

  private Set calculateAbstractMethods() {
    Set abstractMethods = new HashSet();

    List migratedTypes = getMigratedTypes();
    List overriden = getAllOverriden();

    for (int i = 0, max = migratedTypes.size(); i < max; i++) {
      final BinTypeRef migratedType = (BinTypeRef) migratedTypes.get(i);
      // FIXME something wrong with this check here
      if (migratedType.getBinCIType().isInterface() && !extractClass) {
        continue;
      }

      // FIXME check this code once more with clear head
      List methods = new ArrayList();
      final BinMethod[] declaredMethods
          = migratedType.getBinCIType().getDeclaredMethods();
      if (migratedType.getBinCIType().isInterface()) {
        methods.addAll(Arrays.asList(declaredMethods));
      } else {
        for (int k = 0; k < declaredMethods.length; k++) {
          final BinMember declared = declaredMethods[k];
          if (declared.isAbstract()) {
            methods.add(declared);
          }
        }
      }
      methods.retainAll(overriden);

      for (int k = 0, maxK = this.membersToExtract.size(); k < maxK; k++) {
        final BinMember extractable = (BinMember)this.membersToExtract.get(k);
        if (extractable instanceof BinMethod) {
          List extractableOverrides = ((BinMethod) extractable).findOverrides();
          for (int eo = 0; eo < extractableOverrides.size(); eo++) {
            methods.remove(extractableOverrides.get(eo));
          }
        }
      }

      abstractMethods.addAll(methods);
    }

    abstractMethods.addAll(this.explicitlyAbstractMethods);

    // FIXME not sure, added it here, but it could have failed because of some bug above
    if ((!typeRef.getBinCIType().isInterface()) && (isForceExtractMethodsAbstract() || !isExtractClass())) {
      for (int i = 0; i < this.membersToExtract.size(); i++) {
        BinMember member = (BinMember)this.membersToExtract.get(i);
        if (member instanceof BinMethod) {
          abstractMethods.add(member);
        }
      }
    }

    return abstractMethods;
  }

  private List getPrivateMembersToConvert() {
    if (!isConvertPrivate()) {
      return new ArrayList();
    }

    // to enable correct search of dependants
    setConvertPrivate(false);

    List convertables = new ArrayList();
    for (int i = 0, max = this.membersToExtract.size(); i < max; i++) {
      final BinMember extractable = (BinMember)this.membersToExtract.get(i);

      if (!extractable.isPrivate()) {
        continue;
      }

      final Iterator usedBy = this.usedByMap.iteratorFor(extractable);
      while (usedBy.hasNext()) {
        final BinMember usedByMember = (BinMember) usedBy.next();
        if (!this.membersToExtract.contains(usedByMember)) {
          convertables.add(extractable);
          break;
        }
      }
    }

    setConvertPrivate(true);

    return convertables;
  }

  public String getSuperTypePreview() {
    StringBuffer buffer = new StringBuffer(512);

    final Set abstracts = calculateAbstractMethods();
    final List cnstrs = findConstructorsToProxy();
//    List imports = collectImports(cnstrs, abstracts);

    buffer.append(formClassHeader(new ArrayList(), true));

    for (int i = 0, max = cnstrs.size(); i < max; i++) {
      final BinConstructor cnstr = ((BinConstructor) cnstrs.get(i));
      buffer.append(formProxyConstructor(cnstr, true));
      buffer.append(FormatSettings.LINEBREAK);
    }

    for (int i = 0, max = this.membersToExtract.size(); i < max; i++) {
      final BinMember member = (BinMember)this.membersToExtract.get(i);
      if (abstracts.contains(member)) {
        continue;
      }
      // FIXME what's the??? must take indent from the originating type!
      buffer.append(FormatSettings.getIndentString(FormatSettings.
          getBlockIndent()));
      buffer.append(formMemberSignature(member, true, false));
      buffer.append(FormatSettings.LINEBREAK);
    }

    Iterator abstractMethods = abstracts.iterator();
    while (abstractMethods.hasNext()) {
      final BinMethod abstractMethod = (BinMethod) abstractMethods.next();
      // FIXME what's the??? must take indent from the originating type!
      buffer.append(FormatSettings.getIndentString(FormatSettings.
          getBlockIndent()));
      buffer.append(formMemberSignature(abstractMethod, true, true));
      buffer.append(FormatSettings.LINEBREAK);
    }

    buffer.append(formClassFooter(true));

    return buffer.toString();
  }

  private void calculateInterMemberDependencies() {
    Set members = new HashSet();
    members.addAll(Arrays.asList(this.typeRef.getBinCIType().getDeclaredFields()));
    members.addAll(Arrays.asList(this.typeRef.getBinCIType().getDeclaredMethods()));

    BinTypeRef[] inners = this.typeRef.getBinCIType().getDeclaredTypes();
    for (int i = 0; i < inners.length; i++) {
      members.add(inners[i].getBinCIType());
    }
    if (this.typeRef.getBinCIType().isClass()) {
      members.addAll(Arrays.asList(
          ((BinClass)this.typeRef.getBinCIType()).getDeclaredConstructors()));
      members.addAll(Arrays.asList(
          ((BinClass)this.typeRef.getBinCIType()).getInitializers()));
    }

    Iterator it = members.iterator();
    while (it.hasNext()) {
      final BinMember member = (BinMember) it.next();
      ManagingIndexer supervisor = new ManagingIndexer();
      new DependenciesIndexer(supervisor, member);
      member.accept(supervisor);

      List invocations = supervisor.getInvocations();
      for (int k = 0, maxK = invocations.size(); k < maxK; k++) {
        final InvocationData data = (InvocationData) invocations.get(k);
        final BinItem called = data.getWhat();

        if (called instanceof BinMember && members.contains(called)) {
          this.usesMap.putNew(member, called);
          this.usedByMap.putNew(called, member);
        }
      }
    }
  }

  private boolean isPrivate(BinMember member) {
    if (convertPrivates) {
      return false;
    }

    return member.isPrivate();
  }

  public List getDependants(BinMember member, boolean select) {
    final List dependants = new ArrayList();

    if (!explicitlyAbstractMethods.contains(member)) {
      final Iterator uses = this.usesMap.iteratorFor(member);
      while (uses.hasNext()) {
        final BinMember usesMember = (BinMember) uses.next();
        if (select || isPrivate(usesMember)) {
          CollectionUtil.addNew(dependants, usesMember);
        }
      }
    }

    if (isPrivate(member) || !select) {
      final Iterator usedBy = this.usedByMap.iteratorFor(member);
      while (usedBy.hasNext()) {
        final BinMember usedByMember = (BinMember) usedBy.next();
        if (!explicitlyAbstractMethods.contains(usedByMember)) {
          CollectionUtil.addNew(dependants, usedByMember);
        }
      }
    }

    return dependants;
  }

  private class TypesIndexer extends TypeIndexer {

    public List typesUsed = new ArrayList();

    class ImportsFinder extends TypeRefVisitor {
      public ImportsFinder() {
        setIncludeNewExpressions(true);
      }

      public void visit(BinTypeRef typeRef) {
        BinTypeRef ref = typeRef.getTypeRef();
        if (ref != null) {
          if (typeRef.isSpecific() && ref.isReferenceType()
              && !ImportUtils.isFqnUsage(
              typeRef.getCompilationUnit(), typeRef.getNode())) {
            CollectionUtil.addNew(typesUsed, ref);
          }
        }
        super.visit(typeRef);
      }
    }

    public TypesIndexer(ManagingIndexer supervisor, BinCIType target) {
      super(supervisor, target, new BinClassSearchFilter(false, false));
      this.typeRefVisitor = new ImportsFinder();
    }

    public void visit(BinMethodInvocationExpression x) {
    }

    public void visit(BinFieldInvocationExpression x) {
    }

//    public void visit(BinNewExpression x) {
//    checkTypeUsageInfos(x.getTypeUsageInfos());
//    }

    protected void registerMemberDelegates() {
    }

    /**
     * Overrides method in TypeIndexer!
     */
//    protected void checkTypeUsageInfos(Object location, List datas, int start,
//        int end, Set alreadyAddedNodes, SourceConstruct inConstruct) {
//      if (datas != null) {
//        for (int i = start; i < end; i++) {
//          final BinSpecificTypeRef data = (BinSpecificTypeRef) datas.get(i);
//          BinTypeRef ref = data.getTypeRef().getNonArrayType();
//          if (ref.isReferenceType()
//              && !ImportUtils.isFqnUsage(data.getCompilationUnit(), data.getNode())) {
//            CollectionUtil.addNew(typesUsed, ref);
//          }
//        }
//      }
//    }
  }


  private List collectImports(final List constructorsToProxy,
      final Set abstractMethods) {
    List imports = new ArrayList();

    imports.addAll(getMigratedTypes());

    // JAVA5: add type parameters supertypes to imports

    ManagingIndexer supervisor = new ManagingIndexer();
    TypesIndexer typesIndexer
        = new TypesIndexer(supervisor, typeRef.getBinCIType());

    for (int i = 0, max = this.membersToExtract.size(); i < max; i++) {
      final BinMember member = (BinMember)this.membersToExtract.get(i);
      member.accept(supervisor);
    }

    for (int i = 0, max = typesIndexer.typesUsed.size(); i < max; i++) {
      final BinTypeRef newType = (BinTypeRef) typesIndexer.typesUsed.get(i);
      CollectionUtil.addNew(imports, newType);
    }

    List members = new ArrayList(constructorsToProxy);
    members.addAll(abstractMethods);
    for (int i = 0, max = members.size(); i < max; i++) {
      final BinMethod member = ((BinMethod) members.get(i));

      // bug, throws imports not added
//      if (membersToExtract.contains(member)) { // it is already visited above
//        continue;
//      }

      BinParameter[] params = member.getParameters();
      for (int p = 0; p < params.length; p++) {
        if (params[p].getTypeRef().isReferenceType()) {
          CollectionUtil.addNew(imports, params[p].getTypeRef());
        }
      }

      BinMethod.Throws[] throwses = member.getThrows();
      for (int t = 0; t < throwses.length; t++) {
        CollectionUtil.addNew(imports, throwses[t].getException());
      }

      if (!(member instanceof BinConstructor)) {
        if (member.getReturnType().isReferenceType()) {
          CollectionUtil.addNew(imports, member.getReturnType());
        }
      }
    }

    // filter out java.lang and current package
    BinPackage pack = this.typeRef.getPackage();
    Iterator it = imports.iterator();
    while (it.hasNext()) {
      BinTypeRef type = ((BinTypeRef) it.next()).getNonArrayType();

      if (type.isPrimitiveType()
          || type.getQualifiedName().startsWith("java.lang.")
          || type.getPackage().isIdentical(pack)) {
        it.remove();
      }
    }

    Collections.sort(imports, BinTypeRef.QualifiedNameSorter.getInstance());

    return imports;
  }

  private String savePackageName() {
    String packageName;
    final BinCIType binCIType = typeRef.getBinCIType();

    if (binCIType == null) {
      packageName = ""; // some strange NPE's
    }
    BinPackage pack = binCIType.getPackage();

    if (pack == null || pack.getQualifiedName() == null) {
      packageName = "";
    }

    packageName = pack.getQualifiedName();

    return packageName;
  }

  public boolean isExtractInterface() {
    return !isExtractClass();
  }

  public final String getDescription() {
    return "Extract super \"" + this.getNewTypeName() + "\"";
  }

  public String getKey() {
    return key;
  }

  public void setExtractWithOldName(boolean b) {
    this.extractWithOldName = b;
  }
}
